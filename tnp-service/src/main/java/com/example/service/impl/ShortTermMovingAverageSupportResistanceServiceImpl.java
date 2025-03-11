package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.*;
import com.example.service.util.StockPriceUtil;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShortTermMovingAverageSupportResistanceServiceImpl implements ShortTermMovingAverageSupportResistanceService {

    @Autowired
    private SupportResistanceConfirmationService supportResistanceConfirmationService;
    @Autowired
    private BreakoutConfirmationService breakoutConfirmationService;

    @Autowired
    private SupportResistanceUtilService supportResistanceService;

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private BreakoutService breakoutService;

    @Autowired
    private FormulaService formulaService;

    @Override
    public boolean isBullish(Stock stock) {

        /*
         * 1. Breakout 20 EMA
         */
        if(this.isBreakout(stock)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Support 20
         */
        if(this.isNearSupport(stock)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBreakout(Stock stock){
        StockPrice stockPrice = stock.getStockPrice();
        StockTechnicals stockTechnicals = stock.getTechnicals();


        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();


        //Check for Current Day Breakout
        if(candleStickService.isHigherHigh(stock) && candleStickService.range(stock) > CandleStickService.MIN_RANGE) {
            //Breakout EMA20
            if (breakoutConfirmationService.isBullishConfirmation(stock, ema20) && breakoutService.isBreakOut(prevClose, prevEma20, close, ema20)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }


    @Override
    public boolean isNearSupport(Stock stock){

        StockPrice stockPrice = stock.getStockPrice();
        StockTechnicals stockTechnicals = stock.getTechnicals();

        double open = stockPrice.getOpen();
        double prevOpen = stockPrice.getPrevOpen();
        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double high = stockPrice.getHigh();
        double prevHigh = stockPrice.getPrevHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double ema5 = stockTechnicals.getEma5();
        double prevEma5 = stockTechnicals.getPrevEma5();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();

        //Check for Current Day
        if(candleStickService.isLowerHigh(stock) && candleStickService.isLowerLow(stock)) {
            // Near Support EMA 20
            if (supportResistanceService.isNearSupport(open, high, low, close, ema20)) {
                if (this.isAverageBetweenImmediateLowAndHigh(ema5, ema20, ema50)) {
                    return Boolean.TRUE;
                }
            }
        }
        //Check for Previous Session Near Support
        else if(candleStickService.isLowerHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isLowerLow(StockPriceUtil.buildStockPricePreviousSession(stock))) {
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isSupportConfirmed(stock, ema20) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma20)) {
                if (this.isAverageBetweenImmediateLowAndHigh(prevEma5, prevEma20, prevEma50)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateLowAndHigh(double low, double average, double high){
        return average < low && average > high;
    }



    @Override
    public boolean isBearish(Stock stock) {
        /*
         * 1. Breakdown 20
         */
        if(this.isBreakdown(stock)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Resistance 20
         */
        if(this.isNearResistance(stock)){
            return Boolean.TRUE;
        }

        return false;
    }

    @Override
    public boolean isBreakdown(Stock stock){
        StockPrice stockPrice = stock.getStockPrice();
        StockTechnicals stockTechnicals = stock.getTechnicals();

        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();


        //Check for Current Day Breakout
        if(candleStickService.isLowerLow(stock) && candleStickService.range(stock) > CandleStickService.MIN_RANGE){
            //Breakdown EMA20
            if (breakoutConfirmationService.isBearishConfirmation(stock, ema20) && breakoutService.isBreakDown(prevClose, prevEma20, close, ema20)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }



    @Override
    public boolean isNearResistance(Stock stock){

        StockPrice stockPrice = stock.getStockPrice();
        StockTechnicals stockTechnicals = stock.getTechnicals();

        double open = stockPrice.getOpen();
        double prevOpen = stockPrice.getPrevOpen();
        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double high = stockPrice.getHigh();
        double prevHigh = stockPrice.getPrevHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double ema5 = stockTechnicals.getEma5();
        double prevEma5 = stockTechnicals.getPrevEma5();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();


        //Check for Current Day
        if(candleStickService.isHigherHigh(stock) && candleStickService.isHigherLow(stock)) {
            // Near Resistance EMA 20
            if (supportResistanceService.isNearResistance(open, high, low, close, ema20)) {
                if (this.isAverageBetweenImmediateHighAndLow(ema5, ema20, ema50)) {
                    return Boolean.TRUE;
                }
            }
        }
        //Check for Previous Session Near Resistance
        else if(candleStickService.isHigherHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isHigherLow(StockPriceUtil.buildStockPricePreviousSession(stock))){
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isResistanceConfirmed(stock, ema20) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma20)) {
                if (this.isAverageBetweenImmediateHighAndLow(prevEma5, prevEma20, prevEma50)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateHighAndLow(double low, double average, double high){
        return average > low && average < high;
    }
}
