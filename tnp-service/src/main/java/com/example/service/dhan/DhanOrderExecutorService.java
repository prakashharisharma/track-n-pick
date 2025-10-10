package com.example.service.dhan;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import com.example.external.dhan.model.Holding;
import com.example.model.type.IndiceType;
import com.example.service.*;
import com.example.service.dhan.model.PositionDetails;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhanOrderExecutorService {

    private static final double LARGE_ORDER_THRESHOLD = 1_00_000.0;
    private static final long LARGE_QUANTITY_THRESHOLD = 10_000;
    private static final long ORDER_DELAY_MINUTES = 10;

    private final MiscUtil miscUtil;

    private final PortfolioService portfolioService;

    private final CalendarService calendarService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final PositionService positionService;
    private final FormulaService formulaService;
    private final DhanOrchestratorService dhanOrchestratorService;
    private final FundamentalResearchService fundamentalResearchService;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private final ScheduledExecutorService delayedOrderExecutor =
            Executors.newScheduledThreadPool(10);

    @PostConstruct
    public void init() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @PreDestroy
    public void cleanup() {
        delayedOrderExecutor.shutdown();
        try {
            if (!delayedOrderExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                delayedOrderExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            delayedOrderExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public void executeBuy(
            LocalDate currentDate,
            User user,
            List<ResearchTechnical> researchTechnicals,
            boolean isMockExecution) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(researchTechnicals, "Research technicals cannot be null");

        log.info(
                "Executing {} buy orders for user: {} with {} research technicals",
                isMockExecution ? "mock" : "real",
                user.getUsername(),
                researchTechnicals.size());

        List<Holding> holdings = dhanOrchestratorService.getHoldings(user);
        PortfolioLimits limits = getPortfolioLimits(user, researchTechnicals.size());
        double availableFunds = limits.availableFunds();

        if (availableFunds <= 0.0) {
            log.info("Skipping buy orders for user {} - no available funds", user.getUsername());
            return;
        }

        for (ResearchTechnical researchTechnical : researchTechnicals) {
            try {
                if (!validateResearchTechnical(researchTechnical)) {
                    continue;
                }

                Stock stock = researchTechnical.getStock();
                String nseSymbol = stock.getNseSymbol();

                Holding existingHolding =
                        holdings.stream()
                                .filter(h -> h.getTradingSymbol().equals(nseSymbol))
                                .findFirst()
                                .orElse(null);

                double entryPrice = researchTechnical.getEntryPrice();

                StockPrice stockPrice =
                        stockPriceService.get(stock, researchTechnical.getTimeframe());

                if (CandleStickUtils.isGreen(stockPrice)
                        && researchTechnical.getScore() >= 8.5
                        && researchTechnical.getEntryStrategy()
                                != ResearchTechnical.Strategy.SIMPLE) {

                    StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);

                    if (CandleStickUtils.isGreen(stockPriceDaily)
                            && (stockPriceDaily.getClose() > entryPrice
                                    || researchTechnical.getResearchDate().isEqual(currentDate))) {
                        // Give boost .5 % if risk is less min risk
                        if (researchTechnical.getRisk()
                                < RiskUtil.minRisk(researchTechnical.getTimeframe())) {
                            entryPrice = formulaService.applyPercentChange(entryPrice, 0.25);
                        }

                        // Give boost .5 % if score > 8.5
                        if (researchTechnical.getScore() >= 8.5) {

                            entryPrice = formulaService.applyPercentChange(entryPrice, 0.25);
                        }

                        if (researchTechnical.getTimeframe() != Timeframe.DAILY) {
                            double maxAboveClose =
                                    formulaService.applyPercentChange(stockPrice.getClose(), 2.0);

                            entryPrice = Math.min(maxAboveClose, entryPrice);
                        }

                        if (researchTechnical.getTimeframe() == Timeframe.DAILY) {
                            entryPrice = Math.min(stockPrice.getHigh(), entryPrice);
                        }

                        double avgRisk =
                                (RiskUtil.minRisk(researchTechnical.getTimeframe())
                                                + RiskUtil.maxRisk(
                                                        researchTechnical.getTimeframe()))
                                        / 2;

                        if (researchTechnical.getRisk() <= avgRisk
                                && researchTechnical.getScore() >= 8.5
                                && researchTechnical.getVolumeScore() >= 0.75) {
                            if (researchTechnical.getTimeframe() == Timeframe.DAILY) {
                                entryPrice = formulaService.applyPercentChange(entryPrice, 0.25);
                            }
                            if (researchTechnical.getTimeframe() != Timeframe.DAILY) {
                                entryPrice = formulaService.applyPercentChange(entryPrice, 0.35);
                            }
                        }
                    }

                    entryPrice =
                            formulaService.ceilToNearestTick(
                                    entryPrice, researchTechnical.getTickSize());
                }

                System.out.println(
                        stock.getNseSymbol()
                                + "="
                                + researchTechnical.getEntryPrice()
                                + "->"
                                + entryPrice
                                + " "
                                + limits.mimValuePerStock
                                + ":"
                                + limits.maxValuePerStock);

                long positionSize = positionService.calculate(user, researchTechnical);

                PositionDetails position =
                        calculatePosition(
                                availableFunds,
                                limits.maxValuePerStock(),
                                limits.mimValuePerStock(),
                                limits.originalFunds(),
                                positionSize,
                                entryPrice);

                long finalQuantity = position.finalQuantity();

                double valueToAdjust = 0.0;
                if (finalQuantity > 0) {
                    if (existingHolding != null
                            && existingHolding.getAvgCostPrice() != null
                            && existingHolding.getTotalQty() != null) {
                        double existingHoldingValue =
                                existingHolding.getTotalQty() * existingHolding.getAvgCostPrice();

                        if (existingHoldingValue >= position.finalValue()) {
                            log.info(
                                    "Skipping buy order for {} as existing holding value {} is"
                                            + " higher than permitted value {}",
                                    nseSymbol,
                                    existingHoldingValue,
                                    position.finalValue());
                            continue;
                        } else {

                            double valueTobeAdd = position.finalValue() - existingHoldingValue;
                            long quantityToBeAdd = (long) Math.floor(valueTobeAdd / entryPrice);
                            valueToAdjust = position.finalValue() - valueTobeAdd;
                            finalQuantity = quantityToBeAdd;
                            log.info(
                                    "Existing holding found for {} with quantity {} calculated new"
                                            + " quantity to add {} ",
                                    stock.getNseSymbol(),
                                    existingHolding.getTotalQty(),
                                    finalQuantity);
                        }
                    }

                    if (finalQuantity <= 0) {
                        continue;
                    }

                    logOrderDetails(stock, positionSize, position, entryPrice);

                    double orderValue = finalQuantity * entryPrice;
                    long immediateQuantity = 0;
                    long delayedQuantity = finalQuantity;

                    if (isMockExecution) {
                        String payload =
                                this.formatJsonPayload(
                                        user.getDhanClientId(),
                                        researchTechnical.getStock().getIsinCode(),
                                        researchTechnical.getStock().getInstrument(),
                                        String.valueOf(finalQuantity),
                                        String.valueOf(position.disclosedQuantity()),
                                        String.valueOf(entryPrice),
                                        TransactionType.BUY);

                        System.out.println(payload);
                    } else {

                        if (orderValue > LARGE_ORDER_THRESHOLD
                                || finalQuantity > LARGE_QUANTITY_THRESHOLD) {

                            // Split order for large values or quantities
                            immediateQuantity = finalQuantity / 2;
                            delayedQuantity = finalQuantity - immediateQuantity;

                            // Place immediate order for first half
                            dhanOrchestratorService.placeOrder(
                                    TransactionType.BUY,
                                    user,
                                    stock,
                                    immediateQuantity,
                                    entryPrice);
                        }

                        // Schedule delayed order
                        final Stock finalStock = stock;
                        final User finalUser = user;
                        final long finalDelayedQuantity = delayedQuantity;
                        final double finalEntryPrice = entryPrice;

                        delayedOrderExecutor.schedule(
                                () -> {
                                    try {
                                        transactionTemplate.execute(
                                                status -> {
                                                    try {
                                                        dhanOrchestratorService.placeOrder(
                                                                TransactionType.BUY,
                                                                finalUser,
                                                                finalStock,
                                                                finalDelayedQuantity,
                                                                finalEntryPrice);
                                                        log.info(
                                                                "Placed delayed buy order for {}"
                                                                    + " quantity {} at price {}",
                                                                finalStock.getNseSymbol(),
                                                                finalDelayedQuantity,
                                                                finalEntryPrice);
                                                    } catch (Exception e) {
                                                        log.error(
                                                                "Error placing delayed buy order"
                                                                        + " for {}: {}",
                                                                finalStock.getNseSymbol(),
                                                                e.getMessage(),
                                                                e);
                                                        status.setRollbackOnly();
                                                    }
                                                    return null;
                                                });
                                    } catch (Exception e) {
                                        log.error(
                                                "Transaction error for delayed buy order for {}:"
                                                        + " {}",
                                                finalStock.getNseSymbol(),
                                                e.getMessage(),
                                                e);
                                    }
                                },
                                ORDER_DELAY_MINUTES,
                                TimeUnit.MINUTES);
                    }
                    availableFunds = position.remainingFunds() + valueToAdjust;
                }
            } catch (Exception e) {
                log.error(
                        "Error processing buy order for stock {}: {}",
                        researchTechnical.getStock().getNseSymbol(),
                        e.getMessage(),
                        e);
            }
        }
    }

    private record PortfolioLimits(
            double availableFunds,
            double maxValuePerStock,
            double mimValuePerStock,
            double originalFunds) {}

    private PortfolioLimits getPortfolioLimits(User user, int stockCount) {
        double availableFunds = portfolioService.availableFundLimit(user);
        double totalCapital = portfolioService.calculateNetWorth(user);

        if (stockCount <= 0 || totalCapital == 0) {
            return new PortfolioLimits(availableFunds, 0, 0, availableFunds);
        }

        // Fixed caps (1% – 4%)
        final double MIN_CAP = 0.01; // 1%
        final double MAX_CAP = 0.05; // 5%

        // Raw fixed caps
        double rawMaxPerStock = totalCapital * MAX_CAP;
        double rawMinPerStock = totalCapital * MIN_CAP;

        // Round
        double maxPerStock = Math.ceil(rawMaxPerStock / 100) * 100;
        double minPerStock = Math.floor(rawMinPerStock / 100) * 100;

        // --- Key change ---
        // If total required (stockCount * maxPerStock) fits in availableFunds, keep fixed max
        // Else dynamically adjust the max per stock so it fits in availableFunds
        if (stockCount * maxPerStock > availableFunds) {
            maxPerStock = Math.floor((availableFunds / stockCount) / 100) * 100;
            // Ensure not lower than minCap
            maxPerStock = Math.max(maxPerStock, minPerStock);
        }

        return new PortfolioLimits(availableFunds, maxPerStock, minPerStock, availableFunds);
    }

    private boolean validateResearchTechnical(ResearchTechnical researchTechnical) {
        if (researchTechnical == null || researchTechnical.getStock() == null) {
            log.warn("Skipping null research technical or stock");
            return false;
        }

        if (!isValidScore(researchTechnical)) {
            return false;
        }

        if (researchTechnical.getEntryPrice() <= 0) {
            log.warn(
                    "Skipping order for {} due to invalid entry price",
                    researchTechnical.getStock().getNseSymbol());
            return false;
        }

        return true;
    }

    private boolean isValidScore(ResearchTechnical researchTechnical) {
        Double score = researchTechnical.getScore();
        Stock stock = researchTechnical.getStock();

        if (score == null) return false;

        MarketCapCategory capCategory =
                MarketCapCategory.classify(fundamentalResearchService.marketCap(stock));

        IndiceType indiceType = stock.getPrimaryIndice();

        double threshold =
                capCategory == MarketCapCategory.MEGACAP
                                || indiceType != null && indiceType == IndiceType.NIFTY50
                        ? 7.0
                        : 7.5;

        if (score < threshold) {
            log.warn(
                    "Skipping order for {} due to low score {} (threshold: {})",
                    stock.getNseSymbol(),
                    score,
                    threshold);
            return false;
        }

        return true;
    }

    private PositionDetails calculatePosition(
            double availableFunds,
            double maxValuePerStock,
            double minValuePerStock,
            double originalFunds,
            long positionSize,
            double entryPrice) {

        // log.info("Calculating position size {}", availableFunds);

        double adjustedPositionValue = positionSize * entryPrice;
        long finalQuantity = 0;
        double finalValue = 0.0;

        // Case 1: Requested position is affordable
        if (adjustedPositionValue <= availableFunds) {
            // log.info("Adjusted position value {}", adjustedPositionValue);

            double cappedValue = Math.min(adjustedPositionValue, maxValuePerStock);
            // log.info("Max per stock {}, capped value {}", maxValuePerStock, cappedValue);

            if (cappedValue >= minValuePerStock) {
                long qty = (long) Math.floor(cappedValue / entryPrice);
                double value = qty * entryPrice;

                // ensure within [min, max]
                if (value >= minValuePerStock && value <= maxValuePerStock) {
                    finalQuantity = qty;
                    finalValue = value;
                    availableFunds -= finalValue;
                }
            }

            // Case 2: Only partial possible
        } else if (availableFunds > 0) {
            long partialQty = (long) Math.floor(availableFunds / entryPrice);
            double partialValue = partialQty * entryPrice;

            double cappedValue = Math.min(partialValue, maxValuePerStock);
            // log.info("Partial capped value {}", cappedValue);

            if (cappedValue >= minValuePerStock) {
                long qty = (long) Math.floor(cappedValue / entryPrice);
                double value = qty * entryPrice;

                // ensure within [min, max]
                if (value >= minValuePerStock && value <= maxValuePerStock) {
                    finalQuantity = qty;
                    finalValue = value;
                    availableFunds -= finalValue;
                }
            }
        }

        // log.info("Final position -> qty: {}, value: {}", finalQuantity, finalValue);

        return new PositionDetails(
                finalQuantity,
                finalValue,
                (long) Math.ceil(finalQuantity * 0.31), // 40% disclosed quantity
                availableFunds);
    }

    private void logOrderDetails(
            Stock stock, long positionSize, PositionDetails position, double entryPrice) {
        log.info(
                "Order details - Symbol: {}, SecurityId: {}, PositionSize: {}, FinalQty: {},"
                        + " DisclosedQty: {}, Price: {}, FinalValue: {}, RemainingFunds: {}",
                stock.getNseSymbol(),
                stock.getInstrument(),
                positionSize,
                position.finalQuantity(),
                position.disclosedQuantity(),
                entryPrice,
                position.finalValue(),
                position.remainingFunds());
    }

    @Transactional
    public void executeSell(
            User user, List<ResearchTechnical> researchTechnicals, boolean isMockExecution) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(researchTechnicals, "Research technicals cannot be null");

        log.info(
                "Executing {} sell orders for user: {} with {} research technicals",
                isMockExecution ? "mock" : "real",
                user.getUsername(),
                researchTechnicals.size());

        List<Holding> holdings = dhanOrchestratorService.getHoldings(user);
        if (holdings == null || holdings.isEmpty()) {
            log.info("No holdings found for user: {}, skipping sell orders", user.getUsername());
            return;
        }

        for (ResearchTechnical researchTechnical : researchTechnicals) {
            try {
                Stock stock = researchTechnical.getStock();
                String nseSymbol = stock.getNseSymbol();

                Holding holding =
                        holdings.stream()
                                .filter(h -> h.getTradingSymbol().equals(nseSymbol))
                                .findFirst()
                                .orElse(null);

                if (holding == null
                        || holding.getTotalQty() == null
                        || holding.getTotalQty() <= 0) {
                    log.info(
                            "No holdings found for stock: {} for user: {}, skipping sell order",
                            nseSymbol,
                            user.getUsername());
                    continue;
                }

                double exitPrice =
                        researchTechnical.getExitPrice() - researchTechnical.getTickSize();

                if (exitPrice <= 0) {
                    log.warn("Invalid exit price for stock: {}, skipping sell order", nseSymbol);
                    continue;
                }

                StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);

                double upperCircuit =
                        formulaService.applyPercentChange(
                                stockPrice.getClose(), researchTechnical.getPriceBand());

                if (upperCircuit < exitPrice) {
                    log.warn(
                            "Exit price not within upper circuit for stock: {}, skipping sell"
                                    + " order",
                            nseSymbol);
                    continue;
                }

                long quantityToSell = holding.getTotalQty().longValue();

                if (researchTechnical.getExitDate() == null) {

                    LocalDate researchDate = researchTechnical.getResearchDate();
                    LocalDate currentDate = miscUtil.currentDate();
                    if (researchDate != null) {
                        long daysBetween =
                                java.time.temporal.ChronoUnit.DAYS.between(
                                        researchDate, currentDate);

                        if (calendarService.previousTradingSession(currentDate).getDayOfWeek()
                                        == DayOfWeek.FRIDAY
                                || calendarService
                                                .previousTradingSession(currentDate)
                                                .getDayOfWeek()
                                        == DayOfWeek.THURSDAY
                                || daysBetween <= 2) {
                            // Within 2 days - set target as 5% above entry
                            quantityToSell = quantityToSell / 8;

                        } else if (daysBetween <= 4) {
                            quantityToSell = quantityToSell / 5;
                        } else if (daysBetween <= 6) {
                            quantityToSell = quantityToSell / 4;
                        } else if (daysBetween <= 8) {
                            quantityToSell = quantityToSell / 3;
                            ;
                        } else {
                            quantityToSell = quantityToSell / 2;
                        }
                    }
                }

                long disclosedQuantity = (long) (quantityToSell * 0.31);

                double orderValue = quantityToSell * exitPrice;
                long immediateQuantity = 0;
                long delayedQuantity = quantityToSell;

                logOrderDetails(
                        stock,
                        quantityToSell,
                        new PositionDetails(
                                quantityToSell, quantityToSell * exitPrice, disclosedQuantity, 0),
                        exitPrice);

                if (isMockExecution) {
                    String payload =
                            this.formatJsonPayload(
                                    user.getDhanClientId(),
                                    stock.getIsinCode(),
                                    stock.getInstrument(),
                                    String.valueOf(quantityToSell),
                                    String.valueOf(disclosedQuantity),
                                    String.valueOf(exitPrice),
                                    TransactionType.SELL);

                    System.out.println(payload);
                } else {
                    if (orderValue > LARGE_ORDER_THRESHOLD
                            || quantityToSell > LARGE_QUANTITY_THRESHOLD) {
                        // Split order for large values or quantities
                        immediateQuantity = quantityToSell / 2;
                        delayedQuantity = quantityToSell - immediateQuantity;

                        // Place immediate order for first half
                        dhanOrchestratorService.placeOrder(
                                TransactionType.SELL, user, stock, immediateQuantity, exitPrice);
                    }

                    // Schedule delayed order
                    final Stock finalStock = stock;
                    final User finalUser = user;
                    final long finalDelayedQuantity = delayedQuantity;
                    final double finalExitPrice = exitPrice;

                    delayedOrderExecutor.schedule(
                            () -> {
                                try {
                                    transactionTemplate.execute(
                                            status -> {
                                                try {
                                                    dhanOrchestratorService.placeOrder(
                                                            TransactionType.SELL,
                                                            finalUser,
                                                            finalStock,
                                                            finalDelayedQuantity,
                                                            finalExitPrice);
                                                    log.info(
                                                            "Placed delayed sell order for {}"
                                                                    + " quantity {} at price {}",
                                                            finalStock.getNseSymbol(),
                                                            finalDelayedQuantity,
                                                            finalExitPrice);
                                                } catch (Exception e) {
                                                    log.error(
                                                            "Error placing delayed sell order for"
                                                                    + " {}: {}",
                                                            finalStock.getNseSymbol(),
                                                            e.getMessage(),
                                                            e);
                                                    status.setRollbackOnly();
                                                }
                                                return null;
                                            });
                                } catch (Exception e) {
                                    log.error(
                                            "Transaction error for delayed sell order for {}: {}",
                                            finalStock.getNseSymbol(),
                                            e.getMessage(),
                                            e);
                                }
                            },
                            ORDER_DELAY_MINUTES,
                            TimeUnit.MINUTES);
                }
            } catch (Exception e) {
                log.error(
                        "Error processing sell order for stock {}: {}",
                        researchTechnical.getStock().getNseSymbol(),
                        e.getMessage(),
                        e);
            }
        }
    }

    private String formatJsonPayload(
            String clientId,
            String correlationId,
            String securityId,
            String quantity,
            String disclosedQuantity,
            String price,
            TransactionType transactionType) {
        boolean isBefore9AM = LocalTime.now().isBefore(LocalTime.of(9, 0));
        boolean isAfter3_30PM = LocalTime.now().isAfter(LocalTime.of(15, 30));

        boolean isAfterMarketOrder = isBefore9AM || isAfter3_30PM;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dhanClientId", clientId);
        payload.put("correlationId", correlationId);
        payload.put("transactionType", transactionType.name());
        payload.put("exchangeSegment", "NSE_EQ");
        payload.put("productType", "CNC");
        payload.put("orderType", "LIMIT");
        payload.put("validity", "DAY");
        payload.put("securityId", securityId);
        payload.put("quantity", quantity);
        payload.put("disclosedQuantity", disclosedQuantity);
        payload.put("price", price);
        payload.put("triggerPrice", "");
        payload.put("afterMarketOrder", isAfterMarketOrder);
        payload.put("amoTime", isAfterMarketOrder ? "PRE_OPEN" : "OPEN");
        payload.put("boProfitValue", "");
        payload.put("boStopLossValue", "");

        try {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.writeValueAsString(payload);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to format payload", e);
        }
    }
}
