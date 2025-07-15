package com.example.worker.scheduler;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import com.example.external.dhan.model.Trade;
import com.example.service.*;
import com.example.service.dhan.DhanOrchestratorService;
import com.example.service.dhan.DhanTradeService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class DhanTradeScheduler {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final UserService userService;
    private final DhanOrchestratorService dhanOrchestratorService;
    private final DhanTradeService dhanTradeService;
    private final StockService stockService;
    private final CalendarService calendarService;
    private final MiscUtil miscUtil;

    private final FormulaService formulaService;

    private final PortfolioService portfolioService;

    private final ResearchTechnicalService<ResearchTechnical> researchTechnicalService;

    @Scheduled(cron = "0 16 9 * * *") // 9:16 AM
    @Scheduled(cron = "0 20 9 * * *") // 9:20 AM
    @Scheduled(cron = "0 25 9 * * *") // 9:25 AM
    @Scheduled(cron = "0 30 9 * * *") // 9:30 AM
    @Scheduled(cron = "0 35 9 * * *") // 9:35 AM
    @Scheduled(cron = "0 40 9 * * *") // 9:40 AM
    @Scheduled(cron = "0 45 9 * * *") // 9:45 AM
    @Scheduled(cron = "0 50 9 * * *") // 9:50 AM
    @Scheduled(cron = "0 55 9 * * *") // 9:55 AM
    @Scheduled(cron = "0 00 10 * * *") // 10:00 AM
    @Scheduled(cron = "0 15 10 * * *") // 10:15 AM
    @Scheduled(cron = "0 30 10 * * *") // 10:30 AM
    @Scheduled(cron = "0 45 10 * * *") // 10:45 AM
    @Scheduled(cron = "0 32 11 * * *") // 11:00 AM
    @Scheduled(cron = "0 30 11 * * *") // 11:30 AM
    @Scheduled(cron = "0 00 15 * * *") // 3:00 PM
    @Scheduled(cron = "0 15 15 * * *") // 3:15 PM
    @Scheduled(cron = "0 30 15 * * *") // 3:30 PM
    @Scheduled(cron = "0 00 16 * * *") // 4:00 PM
    public void fetchTrades() {
        log.info("Starting trade fetch at {}", LocalDateTime.now());
        try {
            if (calendarService.isWorkingDay(miscUtil.currentDate())) {
                List<User> enabledUsers = userService.getAllDhanApiEnabledUsers();
                processTradesInParallel(enabledUsers);
            }
        } catch (Exception e) {
            log.error("Error in fetching trades", e);
        }
    }

    private void processTradesInParallel(List<User> users) {
        List<CompletableFuture<Void>> futures =
                users.stream().map(this::createTradeFetchFuture).collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Completed fetching trades for {} users", users.size());
    }

    private CompletableFuture<Void> createTradeFetchFuture(User user) {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        List<Trade> trades = this.fetchTrades(user);
                        // Persist Sell Trades
                        List<Trade> sellTrades = filterTrades(user, TransactionType.SELL, trades);
                        List<Trade> processedSellTrades =
                                processTrades(sellTrades, user, TransactionType.SELL);

                        // Persist Buy Trades and place Sell Orders
                        List<Trade> buyTrades = filterTrades(user, TransactionType.BUY, trades);
                        List<Trade> processedBuyTrades =
                                processTrades(buyTrades, user, TransactionType.BUY);
                        Map<String, TradeAggregation> aggregatedTrades =
                                aggregateTradesBySymbol(processedBuyTrades);
                        placeSplitSellOrders(aggregatedTrades, user);

                    } catch (Exception e) {
                        log.error("Error fetching trades for user: {}", user.getUsername(), e);
                    }
                },
                executorService);
    }

    private List<Trade> fetchTrades(User user) {
        log.info("Fetching trades for user: {}", user.getUsername());
        return dhanOrchestratorService.getTrades(user);
    }

    private List<Trade> filterTrades(
            User user, TransactionType transactionType, List<Trade> trades) {
        log.info("Filtering trades for user: {}", user.getUsername());
        List<Trade> filteredTrades =
                trades.stream()
                        .filter(trade -> trade.getTransactionType() == transactionType)
                        .collect(Collectors.toList());

        log.info(
                "Filtered {} {} trades out of {} total trades for user: {}",
                filteredTrades.size(),
                transactionType,
                trades.size(),
                user.getUsername());

        return filteredTrades;
    }

    private List<Trade> processTrades(
            List<Trade> buyTrades, User user, TransactionType transactionType) {
        List<Trade> processedTrades = new ArrayList<>();
        buyTrades.forEach(
                trade -> {
                    log.info(
                            "Processing {} trade - Symbol: {}, Quantity: {}, Price: {}",
                            transactionType,
                            trade.getTradingSymbol(),
                            trade.getTradedQuantity(),
                            trade.getTradedPrice());

                    if (dhanTradeService.processTrade(trade, user)) {
                        processedTrades.add(trade);
                    }
                });
        return processedTrades;
    }

    private Map<String, TradeAggregation> aggregateTradesBySymbol(List<Trade> processedTrades) {
        return processedTrades.stream()
                .collect(
                        Collectors.groupingBy(
                                Trade::getTradingSymbol,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        tradeList -> {
                                            int totalQuantity =
                                                    tradeList.stream()
                                                            .mapToInt(Trade::getTradedQuantity)
                                                            .sum();
                                            double avgPrice =
                                                    tradeList.stream()
                                                            .mapToDouble(Trade::getTradedPrice)
                                                            .average()
                                                            .orElse(0.0);
                                            return new TradeAggregation(totalQuantity, avgPrice);
                                        })));
    }

    private void placeSplitSellOrders(Map<String, TradeAggregation> aggregatedTrades, User user) {
        aggregatedTrades.forEach(
                (symbol, aggregation) -> {
                    try {
                        Stock stock = stockService.getStockByNseSymbol(symbol);
                        if (stock != null) {
                            placeSellOrdersForStock(stock, aggregation, user, symbol);
                        } else {
                            log.error("Stock not found for symbol: {}", symbol);
                        }
                    } catch (Exception e) {
                        log.error("Error placing SELL orders for symbol: {}", symbol, e);
                    }
                });
    }

    private void placeSellOrdersForStock(
            Stock stock, TradeAggregation aggregation, User user, String symbol) {
        log.info(
                "Placing aggregated SELL orders - Symbol: {}, Total Quantity: {}, Avg Price: {}",
                symbol,
                aggregation.quantity,
                aggregation.averagePrice);

        double netWorth = portfolioService.calculateNetWorth(user);

        boolean isSmallOrder =
                (aggregation.quantity * aggregation.averagePrice) <= (netWorth * 0.05);

        double tickSize = researchTechnicalService.getTickSize(stock);

        double[] profitTargetsDefault = {2.0, 3.0, 4.0, 5.0};

        double[] profitTargetsPriceBand5 = {2.0, 3.0, 4.0, 4.9};
        double[] profitTargetsPriceBand10 = {2.0, 4.0, 6.0, 7.9};
        double[] profitTargetsPriceBand20 = {2.0, 5.0, 7.5, 9.9};

        double[] profitTargets = profitTargetsDefault;

        Optional<ResearchTechnical> researchTechnicalOptional =
                researchTechnicalService.getLatest(stock);

        if (researchTechnicalOptional.isPresent()) {
            ResearchTechnical researchTechnical = researchTechnicalOptional.get();
            if (researchTechnical.getPriceBand() == 20.0) {
                profitTargets = profitTargetsPriceBand20;
            } else if (researchTechnical.getPriceBand() == 10.0) {
                profitTargets = profitTargetsPriceBand10;
            } else if (researchTechnical.getPriceBand() == 5.0) {
                profitTargets = profitTargetsPriceBand5;
            }
        }

        // If small order create a single order with 2% profit Margin
        if (isSmallOrder) {
            placeSellOrder(
                    user,
                    stock,
                    aggregation.quantity,
                    formulaService.floorToNearestTick(
                            formulaService.applyPercentChange(
                                    aggregation.averagePrice, profitTargets[0]),
                            tickSize));
        } else {

            long[] splitQuantities = formulaService.splitIn40_30_20_10(aggregation.quantity);

            // First order (40%) - Nearest 10
            placeSellOrder(
                    user,
                    stock,
                    splitQuantities[0],
                    formulaService.floorToNearestTick(
                            formulaService.applyPercentChange(
                                    aggregation.averagePrice, profitTargets[0]),
                            tickSize));

            // Second order (30%) - Nearest 10
            placeSellOrder(
                    user,
                    stock,
                    splitQuantities[1],
                    formulaService.floorToNearestTick(
                            formulaService.applyPercentChange(
                                    aggregation.averagePrice, profitTargets[1]),
                            tickSize));

            // Third order (20%) - Nearest Quarter
            placeSellOrder(
                    user,
                    stock,
                    splitQuantities[2],
                    formulaService.floorToNearestTick(
                            formulaService.applyPercentChange(
                                    aggregation.averagePrice, profitTargets[2]),
                            tickSize));

            // Fourth order (10%) - Nearest Half
            placeSellOrder(
                    user,
                    stock,
                    splitQuantities[3],
                    formulaService.floorToNearestTick(
                            formulaService.applyPercentChange(
                                    aggregation.averagePrice, profitTargets[3]),
                            tickSize));
        }
    }

    private void placeSellOrder(User user, Stock stock, long quantity, double price) {
        dhanOrchestratorService.placeOrder(TransactionType.SELL, user, stock, quantity, price);
    }

    @lombok.Value
    private static class TradeAggregation {
        int quantity;
        double averagePrice;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down trade executor service");
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
