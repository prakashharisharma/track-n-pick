package com.example.worker.scheduler;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.entities.User;
import com.example.service.CalendarService;
import com.example.service.ResearchTechnicalService;
import com.example.service.StockPriceService;
import com.example.service.UserService;
import com.example.service.dhan.DhanOrderExecutorService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DhanOrderScheduler {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final FormulaService formulaService;
    private final UserService userService;
    private final DhanOrderExecutorService dhanOrderExecutorService;
    private final ResearchTechnicalService<ResearchTechnical> researchTechnicalService;
    private final CalendarService calendarService;
    private final MiscUtil miscUtil;
    private final StockPriceService<StockPrice> stockPriceService;

    @Scheduled(cron = "0 05 9 * * *") // Runs at 9:05 AM daily
    public void processBuy() {
        log.info("Starting daily buy order processing at {}", LocalDateTime.now());
        try {
            LocalDate sessionDate = miscUtil.currentDate();
            if (calendarService.isWorkingDay(sessionDate)) {
                List<User> enabledUsers = userService.getAllDhanApiEnabledUsers();
                List<ResearchTechnical> researchTechnicals =
                        researchTechnicalService.getLatestBuyResearch(
                                calendarService.previousTradingSession(sessionDate));
                researchTechnicals.addAll(
                        this.getPreviousInvestmentResearches(
                                calendarService.previousTradingSession(sessionDate)));
                researchTechnicals.addAll(
                        this.getRecentHybridResearches(
                                calendarService.previousTradingSession(sessionDate)));
                processOrdersInParallel(enabledUsers, researchTechnicals, OrderType.BUY);
            }
        } catch (Exception e) {
            log.error("Error in daily buy order processing", e);
        }
    }

    private List<ResearchTechnical> getPreviousInvestmentResearches(LocalDate sessionDate) {
        List<ResearchTechnical> researchTechnicals =
                researchTechnicalService.getLatestInvestmentBuyResearch(
                        calendarService.previousTradingSession(sessionDate));

        return researchTechnicals.stream()
                .filter(rt -> rt.getEntryPrice() != null && rt.getStock() != null)
                .filter(
                        rt -> {
                            StockPrice stockPrice =
                                    stockPriceService.get(rt.getStock(), Timeframe.DAILY);
                            if (stockPrice == null || stockPrice.getClose() == null) return false;

                            // Check research date conditions
                            LocalDate researchDate = rt.getResearchDate();

                            if (researchDate != null) {
                                long daysBetween =
                                        java.time.temporal.ChronoUnit.DAYS.between(
                                                researchDate, sessionDate);

                                if (daysBetween <= 30) {
                                    return true;
                                }
                            }
                            return false;
                        })
                .collect(Collectors.toList());
    }

    private List<ResearchTechnical> getRecentHybridResearches(LocalDate sessionDate) {
        List<ResearchTechnical> researchTechnicals =
                researchTechnicalService.getRecentHybridBuyResearch(
                        calendarService.previousTradingSession(sessionDate));

        return researchTechnicals.stream()
                .filter(rt -> rt.getEntryPrice() != null && rt.getStock() != null)
                .filter(
                        rt -> {
                            StockPrice stockPrice =
                                    stockPriceService.get(rt.getStock(), Timeframe.DAILY);
                            if (stockPrice == null
                                    || stockPrice.getClose() == null
                                    || stockPrice.getLow() == null) return false;

                            LocalDate researchDate = rt.getResearchDate();
                            if (researchDate == null) return false;

                            long daysBetween =
                                    java.time.temporal.ChronoUnit.DAYS.between(
                                            researchDate, sessionDate);

                            if (daysBetween > 7) return false;

                            Double originalEntry = rt.getEntryPrice();
                            Double dayLow = stockPrice.getLow();
                            Double close = stockPrice.getClose();

                            if (dayLow < originalEntry) {
                                // If low breached entry, set to low only if close is above the
                                // original entry
                                if (close > originalEntry || close < originalEntry) {
                                    rt.setEntryPrice(dayLow);
                                }
                            }

                            if (rt.getStopLoss() >= rt.getEntryPrice()) {
                                rt.setStopLoss(rt.getEntryPrice() - 2 * rt.getTickSize());
                            }

                            rt.setRisk(
                                    Math.abs(
                                            formulaService.calculateChangePercentage(
                                                    rt.getEntryPrice(), rt.getStopLoss())));

                            return true;
                        })
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 05 9 * * *") // Runs at 9:05 AM daily
    public void processSell() {
        log.info("Starting daily sell order processing at {}", LocalDateTime.now());
        try {
            LocalDate sessionDate = miscUtil.currentDate();
            if (calendarService.isWorkingDay(sessionDate)) {
                List<User> enabledUsers = userService.getAllDhanApiEnabledUsers();
                List<ResearchTechnical> researchTechnicals =
                        researchTechnicalService.getLatestSellResearch(
                                calendarService.previousTradingSession(sessionDate));

                researchTechnicals.addAll(this.getNearTargetResearches());

                processOrdersInParallel(enabledUsers, researchTechnicals, OrderType.SELL);
            }
        } catch (Exception e) {
            log.error("Error in daily sell order processing", e);
        }
    }

    private List<ResearchTechnical> getNearTargetResearches() {
        LocalDate currentDate = miscUtil.currentDate();
        List<ResearchTechnical> researchTechnicals =
                researchTechnicalService.getAll(Trade.Type.BUY);

        return researchTechnicals.stream()
                .filter(rt -> rt.getTarget() != null && rt.getStock() != null)
                .filter(
                        rt -> {
                            StockPrice stockPrice =
                                    stockPriceService.get(rt.getStock(), Timeframe.DAILY);
                            if (stockPrice == null || stockPrice.getClose() == null) return false;

                            double close = stockPrice.getClose();
                            double entryPrice = rt.getEntryPrice();
                            double target = rt.getTarget();
                            // boolean isWithin10Percent = formulaService.isWithinPercentage(close,
                            // target, 10.0);

                            boolean isWithinPriceBand =
                                    formulaService.isWithinPercentage(
                                            close, target, rt.getPriceBand());

                            // Check research date conditions
                            LocalDate researchDate = rt.getResearchDate();

                            if (!isWithinPriceBand) {
                                if (rt.getEntryStrategy() == ResearchTechnical.Strategy.INVESTMENT
                                        || rt.getEntrySubStrategy()
                                                == ResearchTechnical.SubStrategy.LOWEST_BREAKOUT
                                        || rt.getEntrySubStrategy()
                                                == ResearchTechnical.SubStrategy.LOW_BREAKOUT
                                        || rt.getEntrySubStrategy()
                                                == ResearchTechnical.SubStrategy.MEDIUM_BREAKOUT
                                        || rt.getEntrySubStrategy()
                                                == ResearchTechnical.SubStrategy.MA200_BREAKOUT
                                        || rt.getEntrySubStrategy()
                                                == ResearchTechnical.SubStrategy.MA100_BREAKOUT
                                        || rt.getEntrySubStrategy()
                                                == ResearchTechnical.SubStrategy.MA50_BREAKOUT) {
                                    return false;
                                }
                            }

                            if (researchDate != null) {
                                long daysBetween =
                                        java.time.temporal.ChronoUnit.DAYS.between(
                                                researchDate, currentDate);

                                if (daysBetween <= 3 && !isWithinPriceBand) {
                                    // Within 3 days - set target as 5% above entry

                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.05, rt.getTickSize()));
                                    return true;
                                } else if (daysBetween <= 5 && !isWithinPriceBand) {
                                    // Within 5 days - set target as 7.5% above entry
                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.08, rt.getTickSize()));
                                    return true;
                                } else if (daysBetween <= 7 && !isWithinPriceBand) {
                                    // Within 7 days - set target as 10% above entry
                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.10, rt.getTickSize()));
                                    return true;
                                } else if (daysBetween <= 10 && !isWithinPriceBand) {
                                    // Within 7 days - set target as 10% above entry
                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.15, rt.getTickSize()));
                                    return true;
                                }
                            }

                            // Original logic for within priceBand% of target
                            if (isWithinPriceBand) {
                                rt.setExitPrice(target);
                                return true;
                            }

                            return false;
                        })
                .collect(Collectors.toList());
    }

    private void processOrdersInParallel(
            List<User> users, List<ResearchTechnical> researchTechnicals, OrderType orderType) {
        List<CompletableFuture<Void>> futures =
                users.stream()
                        .map(user -> createOrderFuture(user, researchTechnicals, orderType))
                        .collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Completed processing {} orders for {} users", orderType, users.size());
    }

    private CompletableFuture<Void> createOrderFuture(
            User user, List<ResearchTechnical> researchTechnicals, OrderType orderType) {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        log.info(
                                "Processing {} orders for user: {}", orderType, user.getUsername());
                        executeOrder(user, researchTechnicals, orderType);
                    } catch (Exception e) {
                        log.error(
                                "Error processing {} orders for user: {}",
                                orderType,
                                user.getUsername(),
                                e);
                    }
                },
                executorService);
    }

    private void executeOrder(
            User user, List<ResearchTechnical> researchTechnicals, OrderType orderType) {
        switch (orderType) {
            case BUY:
                dhanOrderExecutorService.executeBuy(user, researchTechnicals);
                break;
            case SELL:
                dhanOrderExecutorService.executeSell(user, researchTechnicals);
                break;
        }
    }

    private enum OrderType {
        BUY,
        SELL
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down order executor service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
