package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
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
    private FormulaService formulaService;

    @Autowired
    private TrendService trendService;

    @Override
    public TradeSetup breakDown(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();
        StockTechnicals stockTechnicals = stock.getTechnicals();

        Trend trend = trendService.isUpTrend(stock);

        boolean isBreakDown = Boolean.FALSE;

         if(trend.getMomentum() == Momentum.TOP){

            if(this.isBreakDown(stock, stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100())) {
                isBreakDown = Boolean.TRUE;
            }
            else if(this.isBreakDown(stock, stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100())) {
                isBreakDown = Boolean.TRUE;
            }

         }if(trend.getMomentum() == Momentum.CORRECTION){
            if(this.isBreakDown(stock, stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50())) {
                isBreakDown = Boolean.TRUE;
            }
        }if(trend.getMomentum() == Momentum.PULLBACK){
            if(this.isBreakDown(stock, stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20())) {
                isBreakDown = Boolean.TRUE;
            }
        }

                    if (isBreakDown) {
                        double entryPrice = stockPrice.getLow();
                        double stopLossPrice = stockPrice.getHigh();
                        double targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, MA_ACTION_RISK_REWARD);
                        double risk = formulaService.calculateChangePercentage(entryPrice, stopLossPrice);
                        log.info("MovingAverage action breakdown active for {}, trend {}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}"
                                , stock.getNseSymbol(), trend, entryPrice, targetPrice, stopLossPrice);
                        breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.BREAKDOWN_EMA20);
                        return TradeSetup.builder()
                                .active(Boolean.TRUE)
                                .strategy(ResearchLedgerTechnical.Strategy.PRICE)
                                .subStrategy(ResearchLedgerTechnical.SubStrategy.SRMA)
                                .entryPrice(entryPrice)
                                .stopLossPrice(stopLossPrice)
                                .targetPrice(targetPrice)
                                .risk(risk)
                                .build();
                    }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private boolean isBreakDown(Stock stock, double immediateLow, double average, double prevImmediateLow, double prevAverage){

        StockPrice stockPrice = stock.getStockPrice();

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
