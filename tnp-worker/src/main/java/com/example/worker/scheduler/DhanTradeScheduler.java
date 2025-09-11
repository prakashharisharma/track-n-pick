package com.example.worker.scheduler;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import com.example.external.dhan.model.Trade;
import com.example.service.*;
import com.example.service.dhan.DhanOrchestratorService;
import com.example.service.dhan.DhanTradeService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final CalendarService calendarService;
    private final MiscUtil miscUtil;

    private final FormulaService formulaService;

    private final MacdIndicatorService macdIndicatorService;

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
    @Scheduled(cron = "0 00 11 * * *") // 11:00 AM
    @Scheduled(cron = "0 30 11 * * *") // 11:30 AM
    @Scheduled(cron = "0 00 12 * * *") // 11:30 AM
    @Scheduled(cron = "0 15 12 * * *") // 11:30 AM
    @Scheduled(cron = "0 30 12 * * *") // 11:30 AM
    @Scheduled(cron = "0 00 13 * * *") // 11:30 AM
    @Scheduled(cron = "0 12 13 * * *") // 11:30 AM
    @Scheduled(cron = "0 00 14 * * *") // 11:30 AM
    @Scheduled(cron = "0 00 15 * * *") // 3:00 PM
    @Scheduled(cron = "0 05 15 * * *") // 3:15 PM
    @Scheduled(cron = "0 15 15 * * *") // 3:15 PM
    @Scheduled(cron = "0 30 15 * * *") // 3:30 PM
    @Scheduled(cron = "0 31 15 * * *") // 3:31 PM
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

                        if (LocalTime.now().isBefore(DhanOrchestratorService.MARKET_CLOSE_TIME)) {
                            Map<String, TradeAggregation> aggregatedTrades =
                                    aggregateTradesBySymbol(processedBuyTrades);
                            placeSplitSellOrders(aggregatedTrades, user);
                        }

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
                                            long totalQuantity =
                                                    tradeList.stream()
                                                            .mapToLong(Trade::getTradedQuantity)
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
                            Optional<ResearchTechnical> researchTechnicalOptional =
                                    researchTechnicalService.getLatest(stock);

                            if (researchTechnicalOptional.isPresent()
                                    && researchTechnicalOptional.get().getVolumeScore() < 0.75) {

                                placeSellOrdersForStock(stock, aggregation, user, symbol);
                            }

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
                "Processing sell orders for stock {} with quantity {} and average price {}",
                symbol,
                aggregation.quantity,
                aggregation.averagePrice);

        OrderParameters params = createOrderParameters(stock, aggregation);

        if (params.isSmallOrder()) {
            placeSmallOrder(user, stock, aggregation, params);
        } else {
            placeLargeOrder(user, stock, aggregation, params);
        }
    }

    @lombok.Value
    private static class OrderParameters {
        double tickSize;
        double[] profitTargets;
        boolean smallOrder; // lombok will create isSmallOrder() for us
    }

    private OrderParameters createOrderParameters(Stock stock, TradeAggregation aggregation) {
        double tickSize = researchTechnicalService.getTickSize(stock);
        boolean isSmallOrder =
                (aggregation.quantity <= 10
                                && (aggregation.getQuantity() * aggregation.averagePrice)
                                        < 100000.0)
                        || (aggregation.getQuantity() * aggregation.averagePrice) < 50000.0;
        double[] profitTargets = determineProfitTargets(stock);

        return new OrderParameters(tickSize, profitTargets, isSmallOrder);
    }

    private double[] determineProfitTargets(Stock stock) {
        double[] profitTargetsDefault = {3.0, 4.75, 7.5, 10.25};
        double[] profitTargetsPriceBand20 = {4.5, 7.5, 12.5, 17.5};
        double[] profitTargetsPriceBand10 = {2.5, 3.75, 6.25, 8.75};
        double[] profitTargetsPriceBand5 = {2.0, 3.0, 3.5, 4.5};

        Optional<ResearchTechnical> researchTechnicalOptional =
                researchTechnicalService.getLatest(stock);

        if (researchTechnicalOptional.isPresent()) {
            double priceBand = researchTechnicalOptional.get().getPriceBand();
            if (priceBand == 20.0) return profitTargetsPriceBand20;
            if (priceBand == 10.0) return profitTargetsPriceBand10;
            if (priceBand == 5.0) return profitTargetsPriceBand5;
        }

        return profitTargetsDefault;
    }

    private void placeSmallOrder(
            User user, Stock stock, TradeAggregation aggregation, OrderParameters params) {
        placeSellOrder(
                user,
                stock,
                aggregation.quantity,
                calculateOrderPrice(
                        aggregation.averagePrice, params.profitTargets[0], params.tickSize));
    }

    private void placeLargeOrder(
            User user, Stock stock, TradeAggregation aggregation, OrderParameters params) {
        long[] splitQuantities = formulaService.splitIn50_25_15_10(aggregation.quantity);

        // First order (50%) - Immediate placement
        placeSellOrder(
                user,
                stock,
                splitQuantities[0],
                calculateOrderPrice(
                        aggregation.averagePrice, params.profitTargets[0], params.tickSize));

        scheduleRemainingOrders(user, stock, aggregation, params, splitQuantities);
    }

    private void scheduleRemainingOrders(
            User user,
            Stock stock,
            TradeAggregation aggregation,
            OrderParameters params,
            long[] splitQuantities) {
        // Create final copies for lambda
        final User finalUser = user;
        final Stock finalStock = stock;
        final double finalAveragePrice = aggregation.averagePrice;
        final double finalTickSize = params.tickSize;
        final double[] finalProfitTargets = params.profitTargets;
        final long[] finalSplitQuantities = splitQuantities;

        CompletableFuture.runAsync(
                () -> {
                    try {
                        placeDelayedOrders(
                                finalUser,
                                finalStock,
                                finalAveragePrice,
                                finalTickSize,
                                finalProfitTargets,
                                finalSplitQuantities);
                    } catch (InterruptedException e) {
                        log.error(
                                "Error in delayed order placement for user {} and stock {}: {}",
                                finalUser.getUsername(),
                                finalStock.getNseSymbol(),
                                e.getMessage());
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.error(
                                "Unexpected error in delayed order placement for user {} and stock"
                                        + " {}: {}",
                                finalUser.getUsername(),
                                finalStock.getNseSymbol(),
                                e.getMessage());
                    }
                },
                executorService);
    }

    private void placeDelayedOrders(
            User user,
            Stock stock,
            double averagePrice,
            double tickSize,
            double[] profitTargets,
            long[] splitQuantities)
            throws InterruptedException {
        // Second order (30%) - After 2 minutes
        Thread.sleep(2 * 60 * 1000);
        placeSellOrder(
                user,
                stock,
                splitQuantities[1],
                calculateOrderPrice(averagePrice, profitTargets[1], tickSize));

        // Third order (20%) - After 5 minutes
        Thread.sleep(3 * 60 * 1000);
        placeSellOrder(
                user,
                stock,
                splitQuantities[2],
                calculateOrderPrice(averagePrice, profitTargets[2], tickSize));

        // Fourth order (10%) - After 10 minutes
        Thread.sleep(5 * 60 * 1000);
        placeSellOrder(
                user,
                stock,
                splitQuantities[3],
                calculateOrderPrice(averagePrice, profitTargets[3], tickSize));
    }

    private double calculateOrderPrice(double averagePrice, double profitTarget, double tickSize) {
        return formulaService.floorToNearestTick(
                        formulaService.applyPercentChange(averagePrice, profitTarget), tickSize)
                - tickSize;
    }

    private void placeSellOrder(User user, Stock stock, long quantity, double price) {
        dhanOrchestratorService.placeOrder(TransactionType.SELL, user, stock, quantity, price);
    }

    @lombok.Value
    private static class TradeAggregation {
        long quantity;
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
