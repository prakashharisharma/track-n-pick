package com.example.service.dhan;

import static com.example.service.ResearchTechnicalService.MAX_RISK;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import com.example.external.dhan.model.Holding;
import com.example.model.type.IndiceType;
import com.example.service.PortfolioService;
import com.example.service.PositionService;
import com.example.service.StockPriceService;
import com.example.service.dhan.model.PositionDetails;
import com.example.service.impl.FundamentalResearchService;
import com.example.util.FormulaService;
import java.util.List;
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

    private static final double LARGE_ORDER_THRESHOLD = 100000.0;
    private static final long LARGE_QUANTITY_THRESHOLD = 1000;
    private static final long ORDER_DELAY_MINUTES = 10;

    private final PortfolioService portfolioService;
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
    public void executeBuy(User user, List<ResearchTechnical> researchTechnicals) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(researchTechnicals, "Research technicals cannot be null");

        log.info(
                "Executing buy orders for user: {} with {} research technicals",
                user.getUsername(),
                researchTechnicals.size());

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
                double entryPrice =
                        researchTechnical.getEntryPrice() + researchTechnical.getTickSize();

                // Adjust entry price if risk is greater than 5
                if (researchTechnical.getRisk() > MAX_RISK) {
                    double riskAdjustment = researchTechnical.getRisk() - MAX_RISK;
                    entryPrice = formulaService.applyPercentChange(entryPrice, -1 * riskAdjustment);
                    entryPrice =
                            formulaService.ceilToNearestTick(
                                    entryPrice, researchTechnical.getTickSize());
                }

                long positionSize = positionService.calculate(user, researchTechnical);

                PositionDetails position =
                        calculatePosition(
                                availableFunds,
                                limits.maxValuePerStock(),
                                limits.mimValuePerStock(),
                                limits.originalFunds(),
                                positionSize,
                                entryPrice);

                if (position.finalQuantity() > 0) {
                    logOrderDetails(stock, positionSize, position, entryPrice);

                    double orderValue = position.finalQuantity() * entryPrice;
                    long immediateQuantity = 0;
                    long delayedQuantity = position.finalQuantity();

                    if (orderValue > LARGE_ORDER_THRESHOLD
                            || position.finalQuantity() > LARGE_QUANTITY_THRESHOLD) {
                        // Split order for large values or quantities
                        immediateQuantity = position.finalQuantity() / 2;
                        delayedQuantity = position.finalQuantity() - immediateQuantity;

                        // Place immediate order for first half
                        dhanOrchestratorService.placeOrder(
                                TransactionType.BUY, user, stock, immediateQuantity, entryPrice);
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
                                                            "Error placing delayed buy order for"
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
                                            "Transaction error for delayed buy order for {}: {}",
                                            finalStock.getNseSymbol(),
                                            e.getMessage(),
                                            e);
                                }
                            },
                            ORDER_DELAY_MINUTES,
                            TimeUnit.MINUTES);

                    availableFunds = position.remainingFunds();
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

        double ratio = totalCapital == 0 ? 0 : availableFunds / totalCapital;

        final double MIN_CAP = totalCapital <= 500000.0 ? 0.050 : 0.025;
        final double MAX_CAP = totalCapital <= 500000.0 ? 0.100 : 0.050;
        final double EXPONENT = 2.0;

        // 1. Base cap % depending on funds availability
        double capPercent = MIN_CAP + (MAX_CAP - MIN_CAP) * Math.pow(1 - ratio, EXPONENT);

        // 2. Adjust for stock count (stockCount: 1–10)
        // Fewer stocks => higher multiplier, More stocks => lower multiplier
        // Map stockCount = 1 → 1.6x, 10 → 0.8x
        double stockCountAdjustment = Math.max(0.8, Math.min(1.6, 1.6 - 0.08 * stockCount));
        capPercent *= stockCountAdjustment;

        // 3. Cap the final value to max 15%
        capPercent = Math.min(capPercent, MAX_CAP);

        // 3. Calculate max and min per stock
        double rawMaxPerStock = totalCapital * capPercent;
        double maxPerStock = Math.ceil(rawMaxPerStock / 100) * 100;

        double rawMinPerStock = totalCapital * MIN_CAP;
        double minPerStock = Math.floor(rawMinPerStock / 100) * 100;

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

        double adjustedPositionValue = positionSize * entryPrice;
        long finalQuantity = 0;
        double finalValue = 0.0;

        if (adjustedPositionValue <= availableFunds) {
            double cappedValue = Math.min(adjustedPositionValue, maxValuePerStock);
            if (cappedValue >= minValuePerStock) {
                finalValue = cappedValue;
                finalQuantity = (long) Math.floor(finalValue / entryPrice);
                finalValue = finalQuantity * entryPrice;
                availableFunds -= finalValue;
            }
        } else if (availableFunds > 0) {
            long partialQty = (long) Math.floor(availableFunds / entryPrice);
            double partialValue = partialQty * entryPrice;
            double cappedValue = Math.min(partialValue, maxValuePerStock);

            if (cappedValue >= minValuePerStock) {
                finalQuantity = (long) Math.floor(cappedValue / entryPrice);
                finalValue = finalQuantity * entryPrice;
                availableFunds -= finalValue;
            }
        }

        return new PositionDetails(
                finalQuantity,
                finalValue,
                (long) (finalQuantity * 0.31), // 40% disclosed quantity
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
    public void executeSell(User user, List<ResearchTechnical> researchTechnicals) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(researchTechnicals, "Research technicals cannot be null");

        log.info(
                "Executing sell orders for user: {} with {} research technicals",
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
                                                        "Placed delayed sell order for {} quantity"
                                                                + " {} at price {}",
                                                        finalStock.getNseSymbol(),
                                                        finalDelayedQuantity,
                                                        finalExitPrice);
                                            } catch (Exception e) {
                                                log.error(
                                                        "Error placing delayed sell order for {}:"
                                                                + " {}",
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
            } catch (Exception e) {
                log.error(
                        "Error processing sell order for stock {}: {}",
                        researchTechnical.getStock().getNseSymbol(),
                        e.getMessage(),
                        e);
            }
        }
    }
}
