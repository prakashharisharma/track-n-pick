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
public class MediumTermMovingAverageSupportResistanceServiceImpl implements MediumTermMovingAverageSupportResistanceService {

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
         * 1. Breakout 50, 100 or 200 EMA
         */
        if(this.isBreakout(stock)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Support 20, 50, 100, 200
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

        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double high = stockPrice.getHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double prevPrevClose = stockPrice.getPrevPrevClose();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevPrevEma20 = stockTechnicals.getPrevPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevPrevEma50 = stockTechnicals.getPrevPrevEma50();
        double ema100 = stockTechnicals.getEma100();
        double prevEma100 = stockTechnicals.getPrevEma100();
        double prevPrevEma100 = stockTechnicals.getPrevPrevEma100();
        double ema200 = stockTechnicals.getEma200();
        double prevEma200 = stockTechnicals.getPrevEma200();
        double prevPrevEma200 = stockTechnicals.getPrevPrevEma200();


        //Check for Current Day Breakout
        if(candleStickService.isHigherHigh(stock) && candleStickService.range(stock) > CandleStickService.MIN_RANGE) {

            //Breakout EMA100

                if (breakoutConfirmationService.isBullishConfirmation(stock, ema50) && breakoutService.isBreakOut(prevClose, prevEma50, close, ema50)) {
                    return Boolean.TRUE;
                }


        }

         /*
        //Check for previous Session Breakout
        else if(candleStickService.rangePrev(stock) > CandleStickService.MIN_RANGE){
            //Breakout Previous Session EMA50
            if(trend == Trend.SHORT) {
                if (breakoutConfirmationService.isBullishFollowup(stock, ema20) && breakoutService.isBreakOut(prevPrevClose, prevPrevEma20, prevClose, prevEma20)) {
                    return Boolean.TRUE;
                }
            }
            //Breakout Previous Session EMA100
            if(trend == Trend.MEDIUM) {
                if (breakoutConfirmationService.isBullishFollowup(stock, ema50) && breakoutService.isBreakOut(prevPrevClose, prevPrevEma50, prevClose, prevEma50)) {
                    return Boolean.TRUE;
                }
            }
            //Breakout Previous Session EMA200
            if(trend == Trend.LONG) {

                /*
                if (breakoutConfirmationService.isBullishFollowup(stock, ema100) && breakoutService.isBreakOut(prevPrevClose, prevPrevEma100, prevClose, prevEma100)) {
                    return Boolean.TRUE;
                }
                 */

 /*
                if (breakoutConfirmationService.isBullishFollowup(stock, ema200) && breakoutService.isBreakOut(prevPrevClose, prevPrevEma200, prevClose, prevEma200)) {
                    return Boolean.TRUE;
                }
            }
        }*/

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
        double prevPrevClose = stockPrice.getPrevPrevClose();
        double ema5 = stockTechnicals.getEma5();
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevPrevEma10 = stockTechnicals.getPrevPrevEma10();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevPrevEma20 = stockTechnicals.getPrevPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevPrevEma50 = stockTechnicals.getPrevPrevEma50();
        double ema100 = stockTechnicals.getEma100();
        double prevEma100 = stockTechnicals.getPrevEma100();
        double prevPrevEma100 = stockTechnicals.getPrevPrevEma100();
        double ema200 = stockTechnicals.getEma200();
        double prevEma200 = stockTechnicals.getPrevEma200();
        double prevPrevEma200 = stockTechnicals.getPrevPrevEma200();


        //Check for Current Day
        if(candleStickService.isLowerHigh(stock) && candleStickService.isLowerLow(stock)) {

            //Near Support EMA 50
                if (supportResistanceService.isNearSupport(open, high, low, close, ema50)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(ema20, ema50, ema100)) {
                        // if (formulaService.applyPercentChange(ema20, FibonacciRatio.RATIO_38_2 * -100) >= ema50) {
                        return Boolean.TRUE;
                        //}
                    }
                }
        }
        //Check for Previous Session Near Support
        else if(candleStickService.isLowerHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isLowerLow(StockPriceUtil.buildStockPricePreviousSession(stock))) {

            //Near Support EMA 50
                if (supportResistanceConfirmationService.isSupportConfirmed(stock, ema50) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma50)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(prevEma20, prevEma50, prevEma100)) {
                        //if (formulaService.applyPercentChange(prevEma20, FibonacciRatio.RATIO_38_2 * -100) >= prevEma50) {
                        return Boolean.TRUE;
                        //}
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
         * 1. Breakdown 50, 100 or 200 EMA
         */
        if(this.isBreakdown(stock)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Resistance 20, 50, 100, 200
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

        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double high = stockPrice.getHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double prevPrevClose = stockPrice.getPrevPrevClose();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevPrevEma20 = stockTechnicals.getPrevPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevPrevEma50 = stockTechnicals.getPrevPrevEma50();
        double ema100 = stockTechnicals.getEma100();
        double prevEma100 = stockTechnicals.getPrevEma100();
        double prevPrevEma100 = stockTechnicals.getPrevPrevEma100();
        double ema200 = stockTechnicals.getEma200();
        double prevEma200 = stockTechnicals.getPrevEma200();
        double prevPrevEma200 = stockTechnicals.getPrevPrevEma200();


        //Check for Current Day Breakout
        if(candleStickService.isLowerLow(stock) && candleStickService.range(stock) > CandleStickService.MIN_RANGE){

            //Breakdown EMA50

                if (breakoutConfirmationService.isBearishConfirmation(stock, ema50) && breakoutService.isBreakDown(prevClose, prevEma50, close, ema50)) {
                    return Boolean.TRUE;
                }
        }

         /*
        //Check for previous Session Breakout
        else if(candleStickService.rangePrev(stock) > CandleStickService.MIN_RANGE){
            //Breakdown Previous Session EMA20
            if(trend == Trend.SHORT) {
                if (breakoutConfirmationService.isBearishFollowup(stock, ema20) && breakoutService.isBreakDown(prevPrevClose, prevPrevEma20, prevClose, prevEma20)) {
                    return Boolean.TRUE;
                }
            }
            //Breakdown Previous Session EMA50
            if(trend == Trend.MEDIUM) {
                if (breakoutConfirmationService.isBearishFollowup(stock, ema50) && breakoutService.isBreakDown(prevPrevClose, prevPrevEma50, prevClose, prevEma50)) {
                    return Boolean.TRUE;
                }
            }
            if(trend == Trend.LONG) {
                /*
                //Breakdown Previous Session EMA100
                if (breakoutConfirmationService.isBearishFollowup(stock, ema100) && breakoutService.isBreakDown(prevPrevClose, prevPrevEma100, prevClose, prevEma100)) {
                    return Boolean.TRUE;
                }*/
        /*
                //Breakdown Previous Session EMA200

                if (breakoutConfirmationService.isBearishFollowup(stock, ema200) && breakoutService.isBreakDown(prevPrevClose, prevPrevEma200, prevClose, prevEma200)) {
                    return Boolean.TRUE;
                }
            }
        }*/


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
        double prevPrevClose = stockPrice.getPrevPrevClose();
        double ema5 = stockTechnicals.getEma5();
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevPrevEma10 = stockTechnicals.getPrevPrevEma10();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevPrevEma20 = stockTechnicals.getPrevPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevPrevEma50 = stockTechnicals.getPrevPrevEma50();
        double ema100 = stockTechnicals.getEma100();
        double prevEma100 = stockTechnicals.getPrevEma100();
        double prevPrevEma100 = stockTechnicals.getPrevPrevEma100();
        double ema200 = stockTechnicals.getEma200();
        double prevEma200 = stockTechnicals.getPrevEma200();
        double prevPrevEma200 = stockTechnicals.getPrevPrevEma200();


        //Check for Current Day
        if(candleStickService.isHigherHigh(stock) && candleStickService.isHigherLow(stock)) {

            // Near Resistance EMA 50
                if (supportResistanceService.isNearResistance(open, high, low, close, ema50)) {
                    if (this.isAverageBetweenImmediateHighAndLow(ema20, ema50, ema100)) {
                        // if (formulaService.applyPercentChange(ema20, FibonacciRatio.RATIO_38_2 * -100) < ema50) {
                        return Boolean.TRUE;
                        //}
                    }
                }
        }
        //Check for Previous Session Near Resistance
        else if(candleStickService.isHigherHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isHigherLow(StockPriceUtil.buildStockPricePreviousSession(stock))){

            //Near Support EMA 50
                if (supportResistanceConfirmationService.isResistanceConfirmed(stock, ema50) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma50)) {
                    if (this.isAverageBetweenImmediateHighAndLow(prevEma20, prevEma50, prevEma100)) {
                        // if (formulaService.applyPercentChange(prevEma20, FibonacciRatio.RATIO_38_2 * -100) < prevEma50) {
                        return Boolean.TRUE;
                        //}
                    }
                }
        }

        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateHighAndLow(double low, double average, double high){
        return average > low && average < high;
    }
}
