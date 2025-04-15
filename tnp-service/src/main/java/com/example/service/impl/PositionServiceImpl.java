package com.example.service.impl;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.User;
import com.example.service.FundsLedgerService;
import com.example.service.PositionService;
import com.example.service.RiskFactor;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PositionServiceImpl implements PositionService {

    @Autowired private FormulaService formulaService;
    @Autowired private FundsLedgerService fundsLedgerService;

    @Override
    public long calculate(User user, ResearchTechnical researchTechnical) {
        // double investmentValue = fundsLedgerService.allTimeInvestment(user);
        double investmentValue = 78000.0;
        double netProfit = 0.0; // Can be extended in future

        double capital = investmentValue + netProfit;

        double riskFactor = this.getRiskFactor(researchTechnical); // Typically a % like 1 or 2
        double risk =
                formulaService.calculateFraction(
                        capital, riskFactor); // risk = capital * (riskFactor / 100)

        double stopLoss = researchTechnical.getResearchPrice() - researchTechnical.getStopLoss();

        // Safety check to avoid division by zero or negative stop loss
        if (stopLoss <= 0) {
            throw new IllegalArgumentException(
                    "Invalid stop loss value: must be less than research price");
        }

        long positionSize = (long) (risk / stopLoss);

        return Math.max(positionSize, 0); // Avoid negative sizing just in case
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
