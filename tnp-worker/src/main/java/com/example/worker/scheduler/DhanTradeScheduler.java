package com.example.worker.scheduler;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import com.example.external.dhan.model.Trade;
import com.example.service.CalendarService;
import com.example.service.StockService;
import com.example.service.UserService;
import com.example.service.dhan.DhanOrchestratorService;
import com.example.service.dhan.DhanTradeService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Scheduled(cron = "0 19 9 * * *") // 9:20 AM
    @Scheduled(cron = "0 24 9 * * *") // 9:25 AM
    @Scheduled(cron = "0 29 9 * * *") // 9:30 AM
    @Scheduled(cron = "0 34 9 * * *") // 9:35 AM
    @Scheduled(cron = "0 39 9 * * *") // 9:40 AM
    @Scheduled(cron = "0 44 9 * * *") // 9:45 AM
    @Scheduled(cron = "0 59 14 * * *") // 3:00 PM
    @Scheduled(cron = "0 24 15 * * *") // 3:20 PM
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
                        List<Trade> buyTrades = fetchAndFilterBuyTrades(user);
                        List<Trade> processedTrades = processBuyTrades(buyTrades, user);
                        Map<String, TradeAggregation> aggregatedTrades =
                                aggregateTradesBySymbol(processedTrades);
                        placeSplitSellOrders(aggregatedTrades, user);
                    } catch (Exception e) {
                        log.error("Error fetching trades for user: {}", user.getUsername(), e);
                    }
                },
                executorService);
    }

    private List<Trade> fetchAndFilterBuyTrades(User user) {
        log.info("Fetching trades for user: {}", user.getUsername());
        List<Trade> trades = dhanOrchestratorService.getTrades(user);

        List<Trade> buyTrades =
                trades.stream()
                        .filter(trade -> trade.getTransactionType() == TransactionType.BUY)
                        .collect(Collectors.toList());

        log.info(
                "Fetched {} BUY trades out of {} total trades for user: {}",
                buyTrades.size(),
                trades.size(),
                user.getUsername());

        return buyTrades;
    }

    private List<Trade> processBuyTrades(List<Trade> buyTrades, User user) {
        List<Trade> processedTrades = new ArrayList<>();
        buyTrades.forEach(
                trade -> {
                    log.info(
                            "Processing BUY trade - Symbol: {}, Quantity: {}, Price: {}",
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

        long[] splitQuantities = formulaService.splitIn40_30_20_10(aggregation.quantity);
        double[] profitTargets = {2.0, 3.0, 4.0, 5.0};

        // First order (40%) - Nearest 10
        placeSellOrder(
                user,
                stock,
                splitQuantities[0],
                formulaService.floorToNearestTen(
                        formulaService.applyPercentChange(
                                aggregation.averagePrice, profitTargets[0])));

        // Second order (30%) - Nearest 10
        placeSellOrder(
                user,
                stock,
                splitQuantities[1],
                formulaService.floorToNearestTen(
                        formulaService.applyPercentChange(
                                aggregation.averagePrice, profitTargets[1])));

        // Third order (20%) - Nearest Quarter
        placeSellOrder(
                user,
                stock,
                splitQuantities[2],
                formulaService.floorToNearestQuarter(
                        formulaService.applyPercentChange(
                                aggregation.averagePrice, profitTargets[2])));

        // Fourth order (10%) - Nearest Half
        placeSellOrder(
                user,
                stock,
                splitQuantities[3],
                formulaService.floorToNearestHalf(
                        formulaService.applyPercentChange(
                                aggregation.averagePrice, profitTargets[3])));
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
