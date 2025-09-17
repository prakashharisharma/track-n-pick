package com.example.service;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TargetService {

    private final FormulaService formulaService;

    private final StockPriceService<StockPrice> stockPriceService;

    public double calculate(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical) {

        double target =
                formulaService.calculateTarget(
                        researchTechnical.getEntryPrice(),
                        researchTechnical.getStopLoss(),
                        this.calculateRiskRewardRatio(
                                stockPrice, stockTechnicals, researchTechnical));

        return formulaService.roundToNearestTick(target, researchTechnical.getTickSize());
    }

    private double calculateRiskRewardRatio(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical) {
        double risk = researchTechnical.getRisk();
        double minRisk = RiskUtil.minRisk(stockPrice.getTimeframe()); // e.g., 6
        double maxRisk = RiskUtil.maxRisk(stockPrice.getTimeframe()); // e.g., 8

        if (risk <= minRisk) {
            return 3.0; // full R for low risk
        } else if (risk >= maxRisk) {
            return 2.0; // minimum R for max allowable risk
        } else {
            // Linear interpolation between 3.0 and 2.0
            double ratio = 3.0 - ((risk - minRisk) / (maxRisk - minRisk)) * (3.0 - 2.0);
            return ratio;
        }
    }

    public boolean isTargetValid(double entryPrice, double targetPrice) {
        if (entryPrice <= 0 || targetPrice <= 0) {
            return false; // Invalid inputs
        }

        double percentageGain = ((targetPrice - entryPrice) / entryPrice) * 100;
        return percentageGain > 2.0;
    }
}
