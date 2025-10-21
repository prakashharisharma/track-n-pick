package com.example.service;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StopLossService {

    private final FormulaService formulaService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    public double calculate(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical) {

        double stopLoss = stockPrice.getLow();

        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.SIMPLE) {
            return stopLoss - researchTechnical.getTickSize();
        }

        if (CandleStickUtils.isPrevSessionRed(stockPrice)) {
            stopLoss = Math.min(stockPrice.getLow(), stockPrice.getPrevLow());
        }

        double stopLossHt = stopLoss;

        StockPrice stockPriceHt =
                stockPriceService.get(stockPrice.getStock(), stockPrice.getTimeframe().getHigher());
        StockTechnicals stockTechnicalsHt =
                stockTechnicalsService.get(
                        stockPrice.getStock(), stockPrice.getTimeframe().getHigher());

        if (stockPriceHt != null && stockTechnicalsHt != null) {
            if (stockPriceHt.getClose() > stockTechnicalsHt.getEma5()) {
                if (CandleStickUtils.isGreen(stockPriceHt)) {
                    stopLossHt = stockPriceHt.getLow();
                    if (CandleStickUtils.isPrevSessionRed(stockPriceHt)) {
                        stopLossHt = Math.min(stockPriceHt.getLow(), stockPriceHt.getPrevLow());
                    }
                }
            }
        }

        return formulaService.floorToNearestTick(
                Math.min(stopLoss, stopLossHt), researchTechnical.getTickSize());
    }
}
