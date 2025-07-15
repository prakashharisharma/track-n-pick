package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.User;
import com.example.service.*;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PositionServiceImpl implements PositionService {

    private final FormulaService formulaService;
    private final PortfolioService portfolioService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Override
    public long calculate(User user, ResearchTechnical researchTechnical) {

        StockPrice stockPrice =
                stockPriceService.get(
                        researchTechnical.getStock(), researchTechnical.getTimeframe());

        StockTechnicals stockTechnicals =
                stockTechnicalsService.get(
                        researchTechnical.getStock(), researchTechnical.getTimeframe());

        double totalCapital = portfolioService.calculateNetWorth(user);

        double riskFactor =
                this.getRiskFactor(
                        researchTechnical.getTimeframe(),
                        stockPrice,
                        stockTechnicals,
                        researchTechnical);

        double risk = formulaService.calculateFraction(totalCapital, riskFactor);

        double stopLoss = researchTechnical.getEntryPrice() - researchTechnical.getStopLoss();

        // Safety check to avoid division by zero or negative stop loss
        if (stopLoss <= 0) {
            throw new IllegalArgumentException(
                    "Invalid stop loss value: must be less than research price");
        }

        long positionSize = (long) (risk / stopLoss);

        return Math.max(positionSize, 0); // Avoid negative sizing just in case
    }

    @Override
    public long calculateAdjustedPositionSize(
            User user, ResearchTechnical researchTechnical, long positionSize) {

        double totalCapital = portfolioService.calculateNetWorth(user);

        double availableFunds = portfolioService.availableFundLimit(user);

        double entryPrice = researchTechnical.getEntryPrice();
        double originalPositionValue = positionSize * entryPrice;

        // What % of total capital is this position worth?
        double positionPercent =
                formulaService.calculatePercentage(totalCapital, originalPositionValue);

        // Adjust position value for available funds
        double adjustedPositionValue =
                formulaService.calculateFraction(availableFunds, positionPercent);

        // NEW: Boost adjusted value based on how much availableFunds you have vs totalCapital
        double availablePercentOfCapital =
                formulaService.calculatePercentage(totalCapital, availableFunds);
        adjustedPositionValue = adjustedPositionValue * (1 + (availablePercentOfCapital / 100));

        long adjustedPositionSize = (long) (adjustedPositionValue / entryPrice);

        return Math.max(adjustedPositionSize, 0);
    }

    private double getRiskFactor(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical) {

        double close = stockPrice.getClose();
        double ma200 = MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals);
        double ma100 = MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals);
        double ma50 = MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals);

        ResearchTechnical.SubStrategy subStrategy = researchTechnical.getEntrySubStrategy();

        if (subStrategy.isBreakout()) {
            if (close > ma200 && close > ma50) {
                return 1.50;
            } else if (close > ma200) {
                return 1.25;
            } else if (close > ma100) {
                return 1.0;
            } else if (close > ma50) {
                return 0.75;
            } else {
                return 0.50;
            }
        }

        if (subStrategy.isSupport()) {
            if (close > ma200 && close > ma50) {
                return 1.5;
            } else if (close > ma200) {
                return 1.25;
            } else if (close > ma100) {
                return 1.0;
            } else if (close > ma50) {
                return 0.75;
            } else {
                return 0.50;
            }
        }

        // Default fallback for other strategies
        return 0.50;
    }
}
