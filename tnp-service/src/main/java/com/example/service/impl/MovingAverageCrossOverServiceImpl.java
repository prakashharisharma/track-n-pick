package com.example.service.impl;

import com.example.data.common.type.Timeframe;

import com.example.service.CrossOverUtil;
import com.example.service.MovingAverageCrossOverService;
import com.example.service.StockTechnicalsService;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockTechnicals;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MovingAverageCrossOverServiceImpl implements MovingAverageCrossOverService {

    @Autowired
    private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Override
    public boolean isGoldenCross(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma200(), stockTechnicals.getEma50(), stockTechnicals.getEma200());

    }

    @Override
    public boolean isBullishCrossOver50(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getEma20(), stockTechnicals.getEma50());
    }

    @Override
    public boolean isBullishCrossOver20(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma20(), stockTechnicals.getEma5(), stockTechnicals.getEma20());
    }

    @Override
    public boolean isBullishCrossOver10(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getEma5(), stockTechnicals.getEma10());
    }

    @Override
    public boolean isDeathCrossOver(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma200(), stockTechnicals.getEma50(), stockTechnicals.getEma200());
    }

    @Override
    public boolean isBearishCrossOver50(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getEma20(), stockTechnicals.getEma50());
    }

    @Override
    public boolean isBearishCrossOver20(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma20(), stockTechnicals.getEma5(), stockTechnicals.getEma20());
    }

    @Override
    public boolean isBearishCrossOver10(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getEma5(), stockTechnicals.getEma10());
    }
}
