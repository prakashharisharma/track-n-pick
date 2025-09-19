package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntryPriceService {

    private final FormulaService formulaService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;

    public double calculate(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical) {

        double basePrice = calculateBasePrice(stockPrice, stockTechnicals);

        // Fetch higher timeframe technicals
        StockTechnicals htTechnicals =
                stockTechnicalsService.get(
                        stockTechnicals.getStock(), stockTechnicals.getTimeframe().getHigher());

        // Apply boosts
        basePrice = applyCandleBoost(basePrice, stockPrice, researchTechnical);

        basePrice = applyHtBoost(basePrice, htTechnicals);

        basePrice = applyVolumeBoost(basePrice, stockPrice, researchTechnical);

        if (researchTechnical.getTimeframe() != Timeframe.DAILY) {
            double maxAboveHigh = formulaService.applyPercentChange(stockPrice.getHigh(), 0.5);

            basePrice = Math.min(basePrice, maxAboveHigh);
        }

        if (researchTechnical.getTimeframe() == Timeframe.DAILY) {
            basePrice = Math.min(basePrice, stockPrice.getHigh());
        }

        return formulaService.ceilToNearestTick(basePrice, researchTechnical.getTickSize());
    }

    private double calculateBasePrice(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double range = CandleStickUtils.range(stockPrice);
        double upperWickSize = CandleStickUtils.upperWickSize(stockPrice);

        if (upperWickSize <= 0.2 * range) {
            return stockPrice.getHigh();
        } else {

            MovingAverageResult highestMovingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            MovingAverageLength.HIGHEST,
                            stockPrice.getTimeframe(),
                            stockTechnicals,
                            true);

            double ema5 = stockTechnicals.getEma5();

            double highestMA = highestMovingAverageResult.getValue();

            // ema5 is less than highest ma but close above highest ma
            if (ema5 < highestMA && highestMA < stockPrice.getClose()) {

                ema5 = Math.max(ema5, formulaService.applyPercentChange(highestMA, -1 * 2.0));
            }

            double maxOfOpenClose = Math.max(stockPrice.getClose(), stockPrice.getOpen());

            double weightedAvgPrice =
                    formulaService.applyPercentChange((ema5 + maxOfOpenClose) / 2, 0.5);

            double weightedClosePrice = formulaService.applyPercentChange(maxOfOpenClose, 0.2);

            double researchPrice = Math.min(weightedAvgPrice, weightedClosePrice);

            double weightedEma5 = formulaService.applyPercentChange(ema5, 0.5);

            // Ensure price within EMA and midpoint bounds
            researchPrice = Math.max(researchPrice, weightedEma5);

            double midHighMax = (stockPrice.getHigh() + maxOfOpenClose) / 2;

            researchPrice = Math.min(researchPrice, midHighMax);

            return researchPrice;
        }
    }

    private double applyHtBoost(double price, StockTechnicals stockTechnicalsHt) {
        if (MovingAverageUtil.isAllMAsIncreasing(stockTechnicalsHt)
                && MovingAverageUtil.isAllMaAlignedBullish(
                        stockTechnicalsHt.getTimeframe(), stockTechnicalsHt)) {
            return formulaService.applyPercentChange(price, 1.0);
        }
        return price;
    }

    private double applyVolumeBoost(
            double price, StockPrice stockPrice, ResearchTechnical researchTechnical) {
        if (researchTechnical.getVolumeScore() > 0 && CandleStickUtils.isGreen(stockPrice)) {
            return formulaService.applyPercentChange(price, researchTechnical.getVolumeScore());
        }
        return price;
    }

    private double applyCandleBoost(
            double price, StockPrice stockPrice, ResearchTechnical researchTechnical) {
        if (CandleStickUtils.isRed(stockPrice)) {
            return formulaService.applyPercentChange(price, -1 * 2.0);
        }
        return price;
    }
}
