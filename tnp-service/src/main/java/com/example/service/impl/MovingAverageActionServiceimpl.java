package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.dto.TradeSetup;
import com.example.transactional.model.research.ResearchTechnical;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.transactional.model.ledger.BreakoutLedger;
import com.example.transactional.model.master.Stock;
import com.example.service.*;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MovingAverageActionServiceimpl implements MovingAverageActionService {

    private static double MA_ACTION_RISK_REWARD = 2.0;

    @Autowired
    private CandleStickService candleStickService;
    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Autowired
    private SupportResistanceUtilService supportResistanceService;

    @Autowired
    private StockPriceService<StockPrice> stockPriceService;

    @Autowired
    private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private TrendService trendService;

    @Override
    public TradeSetup breakDown(Stock stock, Timeframe timeframe) {

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        Trend trend = trendService.detect(stock, timeframe);

        boolean isBreakDown = Boolean.FALSE;

         if(trend.getMomentum() == Trend.Momentum.TOP){
            if(this.isBreakDown(stock, timeframe, stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100())) {
                isBreakDown = Boolean.TRUE;
            }
            else if(this.isBreakDown(stock,timeframe, stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100())) {
                isBreakDown = Boolean.TRUE;
            }

         }if(trend.getMomentum() == Trend.Momentum.ADVANCE){
            if(this.isBreakDown(stock,timeframe, stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50())) {
                isBreakDown = Boolean.TRUE;
            }
        }if(trend.getMomentum() == Trend.Momentum.RECOVERY){
            if(this.isBreakDown(stock,timeframe, stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20())) {
                isBreakDown = Boolean.TRUE;
            }
        }

                    if (isBreakDown) {
                        breakoutLedgerService.addNegative(stock, timeframe, BreakoutLedger.BreakoutCategory.BREAKDOWN_EMA20);

                        return TradeSetup.builder()
                                .active(Boolean.TRUE)
                                .strategy(ResearchTechnical.Strategy.PRICE)
                                .subStrategy(ResearchTechnical.SubStrategy.SRMA)
                                .build();
                    }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private boolean isBreakDown(Stock stock, Timeframe timeframe, double immediateLow, double average, double prevImmediateLow, double prevAverage){

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        if(average > prevAverage){
            if(immediateLow < prevImmediateLow) {
                if (!supportResistanceService.isNearSupport(stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose(), average)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
}
