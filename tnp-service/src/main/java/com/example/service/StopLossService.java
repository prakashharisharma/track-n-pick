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

    public double calculate(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical) {

        double stopLoss = stockPrice.getLow();

        if (CandleStickUtils.isPrevSessionRed(stockPrice)) {
            stopLoss = Math.min(stockPrice.getLow(), stockPrice.getPrevLow());
        }

        return formulaService.floorToNearestTick(stopLoss, researchTechnical.getTickSize());
    }
}
