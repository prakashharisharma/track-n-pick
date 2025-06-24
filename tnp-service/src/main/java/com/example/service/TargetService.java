package com.example.service;

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

        double riskRewardTarget =
                formulaService.calculateTarget(
                        stockPrice.getHigh(),
                        stockPrice.getLow(),
                        this.calculateRiskRewardRatio(researchTechnical.getEntrySubStrategy()));

        double pivotTarget = getPivotTarget(htStockPrice, stockPrice);

        pivotTarget =
                pivotTarget > researchTechnical.getEntryPrice() ? pivotTarget : riskRewardTarget;

        double rangeTarget =
                researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.RANGE
                        ? supportResistanceZones.getResistance().getStart()
                        : riskRewardTarget;

        rangeTarget =
                rangeTarget > researchTechnical.getEntryPrice() ? rangeTarget : riskRewardTarget;

        return formulaService.roundToNearestHalf(
                Math.min(riskRewardTarget, Math.min(pivotTarget, rangeTarget)));
    }

    private double getPivotTarget(StockPrice htStockPrice, StockPrice stockPrice) {

        double resistance1 = htStockPrice.getResistance1();
        double resistance2 = htStockPrice.getResistance2();
        double close = stockPrice.getClose();

        double percentageDiff = Math.abs(resistance1 - close) / resistance1 * 100;

        return percentageDiff < 2.0 ? resistance1 : resistance2;
    }

    private double calculateRiskRewardRatio(ResearchTechnical.SubStrategy subStrategy) {

        return 2.0;
    }

    public boolean isTargetValid(double entryPrice, double targetPrice) {
        if (entryPrice <= 0 || targetPrice <= 0) {
            return false; // Invalid inputs
        }

        double percentageGain = ((targetPrice - entryPrice) / entryPrice) * 100;
        return percentageGain > 2.0;
    }
}
