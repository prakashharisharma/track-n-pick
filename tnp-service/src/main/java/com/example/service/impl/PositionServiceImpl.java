package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PositionServiceImpl implements PositionService {

    private final FormulaService formulaService;
    private final FundsLedgerService fundsLedgerService;

    private final PortfolioService portfolioService;
    private final TradeService tradeService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Override
    public long calculate(Long userId, ResearchTechnical researchTechnical) {

        StockPrice stockPrice =
                stockPriceService.get(
                        researchTechnical.getStock(), researchTechnical.getTimeframe());

        StockTechnicals stockTechnicals =
                stockTechnicalsService.get(
                        researchTechnical.getStock(), researchTechnical.getTimeframe());

        double totalCapital = this.totalCapital(userId);

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
            Long userId, ResearchTechnical researchTechnical, long positionSize) {

        double totalCapital = this.totalCapital(userId);
        BigDecimal totalInvestmentValue = portfolioService.getTotalInvestmentValue(userId);
        double availableFunds = totalCapital - totalInvestmentValue.doubleValue();

        double entryPrice = researchTechnical.getEntryPrice();
        double originalPositionValue = positionSize * entryPrice;

        // Calculate what percentage of total capital this position represents
        double positionPercent =
                formulaService.calculatePercentage(totalCapital, originalPositionValue);

        // Adjusted capital for the same percentage, but on available funds
        double adjustedPositionValue =
                formulaService.calculateFraction(availableFunds, positionPercent);

        long adjustedPositionSize = (long) (adjustedPositionValue / entryPrice);

        return Math.max(adjustedPositionSize, 0);
    }

    private double totalCapital(Long userId) {
        BigDecimal investmentValue = fundsLedgerService.getTotalFundsValue(userId);

        BigDecimal netProfit = tradeService.getTotalRealizedPnl(userId);

        return investmentValue.doubleValue() + netProfit.doubleValue();
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
                return 1.25;
            } else if (close > ma200) {
                return 1.0;
            } else if (close > ma100) {
                return 0.75;
            } else if (close > ma50) {
                return 0.50;
            } else {
                return 0.25;
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
