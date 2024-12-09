package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.CrossOverUtil;
import com.example.service.MovingAverageCrossOverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MovingAverageCrossOverServiceImpl implements MovingAverageCrossOverService {

    @Override
    public boolean isGoldenCross(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevSma50(), stockTechnicals.getPrevSma200(), stockTechnicals.getSma50(), stockTechnicals.getSma200());

    }

    @Override
    public boolean isBullishCrossOver50(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getEma20(), stockTechnicals.getEma50());
    }

    @Override
    public boolean isBullishCrossOver20(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma20(), stockTechnicals.getEma5(), stockTechnicals.getEma20());
    }

    @Override
    public boolean isBullishCrossOver10(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getEma5(), stockTechnicals.getEma10());
    }

    @Override
    public boolean isDeathCrossOver(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevSma50(), stockTechnicals.getPrevSma200(), stockTechnicals.getSma50(), stockTechnicals.getSma200());
    }

    @Override
    public boolean isBearishCrossOver50(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getEma20(), stockTechnicals.getEma50());
    }

    @Override
    public boolean isBearishCrossOver20(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma20(), stockTechnicals.getEma5(), stockTechnicals.getEma20());
    }

    @Override
    public boolean isBearishCrossOver10(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getEma5(), stockTechnicals.getEma10());
    }
}
