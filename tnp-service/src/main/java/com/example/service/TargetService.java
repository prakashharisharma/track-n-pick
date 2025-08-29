package com.example.service;

import static com.example.service.ResearchTechnicalService.MIN_RISK;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.service.utils.SupportResistanceZoneUtils;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TargetService {

    private final FormulaService formulaService;

    private final StockPriceService<StockPrice> stockPriceService;

    public double calculateTarget(StockPrice stockPrice, ResearchTechnical researchTechnical) {

        StockPrice htStockPrice =
                stockPriceService.get(
                        stockPrice.getStock(), stockPrice.getTimeframe().getHigher().getHigher());

        SupportResistanceZones supportResistanceZones =
                SupportResistanceZoneUtils.calculateSupportResistanceZones(htStockPrice);

        double entryPrice = researchTechnical.getEntryPrice();
        // Adjust entry price if risk is greater than 5
        if (researchTechnical.getRisk() > ResearchTechnicalService.MAX_RISK) {

            double riskAdjustment = researchTechnical.getRisk() - ResearchTechnicalService.MAX_RISK;

            entryPrice = formulaService.applyPercentChange(entryPrice, -1 * riskAdjustment);

            if (researchTechnical.getVolumeScore() == 0.75) {
                entryPrice = formulaService.applyPercentChange(entryPrice, 0.75);
            } else if (researchTechnical.getVolumeScore() == 0.50) {
                entryPrice = formulaService.applyPercentChange(entryPrice, 0.50);
            } else if (researchTechnical.getVolumeScore() == 0.25) {
                entryPrice = formulaService.applyPercentChange(entryPrice, 0.25);
            }

            entryPrice =
                    formulaService.ceilToNearestTick(entryPrice, researchTechnical.getTickSize());
            entryPrice = Math.min(entryPrice, stockPrice.getHigh());
        } else if (researchTechnical.getRisk() < MIN_RISK) {

            entryPrice = formulaService.applyPercentChange(entryPrice, 0.50);

            if (researchTechnical.getVolumeScore() == 0.75) {
                entryPrice = formulaService.applyPercentChange(entryPrice, 0.30);
            } else if (researchTechnical.getVolumeScore() == 0.50) {
                entryPrice = formulaService.applyPercentChange(entryPrice, 0.20);
            } else if (researchTechnical.getVolumeScore() == 0.25) {
                entryPrice = formulaService.applyPercentChange(entryPrice, 0.10);
            }

            entryPrice =
                    formulaService.ceilToNearestTick(entryPrice, researchTechnical.getTickSize());
            entryPrice = Math.min(entryPrice, stockPrice.getHigh());
        }

        double riskRewardTarget =
                formulaService.calculateTarget(
                        entryPrice,
                        researchTechnical.getStopLoss(),
                        this.calculateRiskRewardRatio(researchTechnical));

        double pivotTarget = getPivotTarget(htStockPrice, stockPrice);

        pivotTarget =
                pivotTarget > researchTechnical.getEntryPrice() ? pivotTarget : riskRewardTarget;

        double rangeTarget =
                researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.RANGE
                        ? supportResistanceZones.getResistance().getStart()
                        : riskRewardTarget;

        rangeTarget =
                rangeTarget > researchTechnical.getEntryPrice() ? rangeTarget : riskRewardTarget;

        double target = riskRewardTarget;

        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.PIVOT) {
            target = pivotTarget;
        } else if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.RANGE) {
            target = rangeTarget;
        }

        return formulaService.roundToNearestTick(target, researchTechnical.getTickSize());
    }

    private double getPivotTarget(StockPrice htStockPrice, StockPrice stockPrice) {

        double resistance1 = htStockPrice.getResistance1();
        double resistance2 = htStockPrice.getResistance2();
        double close = stockPrice.getClose();

        double percentageDiff = Math.abs(resistance1 - close) / resistance1 * 100;

        return percentageDiff < 2.0 ? resistance1 : resistance2;
    }

    private double calculateRiskRewardRatio(ResearchTechnical researchTechnical) {

        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.HYBRID) {
            return 2.0;
        } else if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.INVESTMENT) {
            return 3.0;
        }

        return researchTechnical.getEntrySubStrategy().targetPercentage();
    }

    public boolean isTargetValid(double entryPrice, double targetPrice) {
        if (entryPrice <= 0 || targetPrice <= 0) {
            return false; // Invalid inputs
        }

        double percentageGain = ((targetPrice - entryPrice) / entryPrice) * 100;
        return percentageGain > 2.0;
    }
}
