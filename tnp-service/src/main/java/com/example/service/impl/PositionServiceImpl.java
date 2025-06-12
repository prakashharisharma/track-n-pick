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

       // double totalCapital = this.totalCapital(userId);
        double totalCapital = 1137000;
        // double riskFactor = this.getRiskFactor(researchTechnical); // Typically a % like 1 or 2
        double riskFactor =
                this.getRiskFactor(
                        researchTechnical.getTimeframe(),
                        stockPrice,
                        stockTechnicals,
                        researchTechnical);

        double risk =
                formulaService.calculateFraction(
                        totalCapital, riskFactor); // risk = capital * (riskFactor / 100)

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
        // Get the available funds in the user's portfolio
        BigDecimal totalInvestmentValue = portfolioService.getTotalInvestmentValue(userId);
        double availableFunds = totalCapital - totalInvestmentValue.doubleValue();

        // Check if there's enough available funds to allocate to the position size
        double positionValue = positionSize * researchTechnical.getEntryPrice();

        // If the available funds are less than the required position value, adjust the position
        // size
        if (availableFunds < positionValue) {
            // Calculate the maximum position size that can be accommodated within the available
            // funds
            long adjustedPositionSize = (long) (availableFunds / researchTechnical.getEntryPrice());
            return Math.max(adjustedPositionSize, 0); // Ensure position size is not negative
        }

        // If available funds are greater than or equal to the required position value, return the
        // original position size
        return positionSize;
    }

    private double totalCapital(Long userId) {
        BigDecimal investmentValue = fundsLedgerService.getTotalFundsValue(userId);

        BigDecimal netProfit = tradeService.getTotalRealizedPnl(userId);

        return investmentValue.doubleValue() + netProfit.doubleValue();
    }

    private double getRiskFactor(ResearchTechnical researchTechnical) {
        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.VOLUME) {
            return RiskFactor.VOLUME_HV;
        } else if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.PRICE) {
            if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.STRONG_BREAKOUT) {
                return RiskFactor.PRICE_STRONG_BREAKOUT;
            } else if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.WEAK_BREAKOUT) {
                return RiskFactor.PRICE_WEAK_BREAKOUT;
            } else if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.STRONG_SUPPORT) {
                return RiskFactor.PRICE_STRONG_SUPPORT;
            } else if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.WEAK_SUPPORT) {
                return RiskFactor.PRICE_WEAK_SUPPORT;
            } else if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.BULLISH_INDICATORS) {
                return RiskFactor.PRICE_BULLISH_INDICATORS;
            }
        } else if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.SWING) {
            if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.STRONG_SWING) {
                return RiskFactor.SWING_STRONG_SWING;
            } else if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.WEAK_SWING) {
                return RiskFactor.SWING_WEAK_SWING;
            }
        }

        return 0.0;
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
                return 2.0;
            } else if (close > ma200) {
                return 1.75;
            } else if (close > ma100) {
                return 1.50;
            } else if (close > ma50) {
                return 1.25;
            } else {
                return 1.0;
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
