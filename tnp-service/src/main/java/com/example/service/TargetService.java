package com.example.service;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.utils.SignalEvaluatorHelperService;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TargetService {

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
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

        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.SIMPLE) {
            if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.WEEKLY_BREAKOUT) {
                target = formulaService.applyPercentChange(researchTechnical.getEntryPrice(), 6.0);
            } else if (researchTechnical.getEntrySubStrategy()
                    == ResearchTechnical.SubStrategy.MONTHLY_BREAKOUT) {
                target = formulaService.applyPercentChange(researchTechnical.getEntryPrice(), 24.0);
            }
        }

        return formulaService.roundToNearestTick(target, researchTechnical.getTickSize());
    }

    private double calculateRiskRewardRatio(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical) {

        double risk = researchTechnical.getRisk();
        double minRisk = RiskUtil.minRisk(stockPrice.getTimeframe()); // e.g., 6
        double maxRisk = RiskUtil.maxRisk(stockPrice.getTimeframe()); // e.g., 8

        double ratio = 1.5;

        if (risk <= minRisk) {
            ratio = 3.0; // full R for low risk
        } else if (risk >= maxRisk) {
            ratio = 2.0; // minimum R for max allowable risk
        } else {
            // Linear interpolation between 3.0 and 2.0
            ratio = 3.0 - ((risk - minRisk) / (maxRisk - minRisk)) * (3.0 - 2.0);
        }

        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.CANDLESTICK) {
            if (!signalEvaluatorHelperService.isHigherTimeframeConfirmed(stockTechnicals, false)) {
                ratio = 2.0;
            }
        }

        return ratio;
    }

    public boolean isTargetValid(double entryPrice, double targetPrice) {
        if (entryPrice <= 0 || targetPrice <= 0) {
            return false; // Invalid inputs
        }

        double percentageGain = ((targetPrice - entryPrice) / entryPrice) * 100;
        return percentageGain > 2.0;
    }
}
