package com.example.service.impl;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.service.*;
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

    @Override
    public long calculate(Long userId, ResearchTechnical researchTechnical) {

        double totalCapital = this.totalCapital(userId);

        double riskFactor = this.getRiskFactor(researchTechnical); // Typically a % like 1 or 2

        double risk =
                formulaService.calculateFraction(
                        totalCapital, riskFactor); // risk = capital * (riskFactor / 100)

        double stopLoss = researchTechnical.getResearchPrice() - researchTechnical.getStopLoss();

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
        double positionValue = positionSize * researchTechnical.getResearchPrice();

        // If the available funds are less than the required position value, adjust the position
        // size
        if (availableFunds < positionValue) {
            // Calculate the maximum position size that can be accommodated within the available
            // funds
            long adjustedPositionSize =
                    (long) (availableFunds / researchTechnical.getResearchPrice());
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
        if (researchTechnical.getStrategy() == ResearchTechnical.Strategy.VOLUME) {
            return RiskFactor.VOLUME_HV;
        } else if (researchTechnical.getStrategy() == ResearchTechnical.Strategy.PRICE) {
            if (researchTechnical.getSubStrategy()
                    == ResearchTechnical.SubStrategy.STRONG_BREAKOUT) {
                return RiskFactor.PRICE_STRONG_BREAKOUT;
            } else if (researchTechnical.getSubStrategy()
                    == ResearchTechnical.SubStrategy.WEAK_BREAKOUT) {
                return RiskFactor.PRICE_WEAK_BREAKOUT;
            } else if (researchTechnical.getSubStrategy()
                    == ResearchTechnical.SubStrategy.STRONG_SUPPORT) {
                return RiskFactor.PRICE_STRONG_SUPPORT;
            } else if (researchTechnical.getSubStrategy()
                    == ResearchTechnical.SubStrategy.WEAK_SUPPORT) {
                return RiskFactor.PRICE_WEAK_SUPPORT;
            } else if (researchTechnical.getSubStrategy()
                    == ResearchTechnical.SubStrategy.BULLISH_INDICATORS) {
                return RiskFactor.PRICE_BULLISH_INDICATORS;
            }
        } else if (researchTechnical.getStrategy() == ResearchTechnical.Strategy.SWING) {
            if (researchTechnical.getSubStrategy() == ResearchTechnical.SubStrategy.STRONG_SWING) {
                return RiskFactor.SWING_STRONG_SWING;
            } else if (researchTechnical.getSubStrategy()
                    == ResearchTechnical.SubStrategy.WEAK_SWING) {
                return RiskFactor.SWING_WEAK_SWING;
            }
        }

        return 0.0;
    }
}
