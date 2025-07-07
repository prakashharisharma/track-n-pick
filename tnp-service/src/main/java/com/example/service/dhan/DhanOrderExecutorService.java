package com.example.service.dhan;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import com.example.external.dhan.model.Holding;
import com.example.service.PortfolioService;
import com.example.service.PositionService;
import com.example.service.dhan.model.PositionDetails;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhanOrderExecutorService {

    private final PortfolioService portfolioService;

    private final PositionService positionService;

    private final DhanOrchestratorService dhanOrchestratorService;

    @Transactional
    public void executeBuy(User user, List<ResearchTechnical> researchTechnicals) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(researchTechnicals, "Research technicals cannot be null");

        log.info(
                "Executing buy orders for user: {} with {} research technicals",
                user.getUsername(),
                researchTechnicals.size());

        PortfolioLimits limits = getPortfolioLimits(user);
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
                double entryPrice = researchTechnical.getEntryPrice();
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
                    dhanOrchestratorService.placeOrder(
                            TransactionType.BUY, user, stock, position.finalQuantity(), entryPrice);
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

    private PortfolioLimits getPortfolioLimits(User user) {
        double availableFunds = portfolioService.availableFundLimit(user);
        double totalCapital = portfolioService.calculateNetWorth(user);
        return new PortfolioLimits(
                availableFunds,
                totalCapital * 0.15, // 15% cap per stock
                totalCapital * 0.05, // 5% min
                availableFunds);
    }

    private boolean validateResearchTechnical(ResearchTechnical researchTechnical) {
        if (researchTechnical == null || researchTechnical.getStock() == null) {
            log.warn("Skipping null research technical or stock");
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
                (long) (finalQuantity * 0.40), // 40% disclosed quantity
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

        // Get user's holdings
        List<Holding> holdings = dhanOrchestratorService.getHoldings(user);
        if (holdings == null || holdings.isEmpty()) {
            log.info("No holdings found for user: {}, skipping sell orders", user.getUsername());
            return;
        }

        for (ResearchTechnical researchTechnical : researchTechnicals) {
            try {
                if (!validateResearchTechnical(researchTechnical)) {
                    continue;
                }

                Stock stock = researchTechnical.getStock();
                String nseSymbol = stock.getNseSymbol();

                // Find matching holding for the stock
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

                double exitPrice = researchTechnical.getExitPrice();
                if (exitPrice <= 0) {
                    log.warn("Invalid exit price for stock: {}, skipping sell order", nseSymbol);
                    continue;
                }

                long quantityToSell = holding.getTotalQty().longValue();
                long disclosedQuantity = (long) (quantityToSell * 0.40);

                logOrderDetails(
                        stock,
                        quantityToSell,
                        new PositionDetails(
                                quantityToSell, quantityToSell * exitPrice, disclosedQuantity, 0),
                        exitPrice);

                dhanOrchestratorService.placeOrder(
                        TransactionType.SELL, user, stock, quantityToSell, exitPrice);

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
