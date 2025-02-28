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
public class MovingAverageSupportResistanceServiceImpl implements MovingAverageSupportResistanceService {

    @Autowired
    private  SupportResistanceConfirmationService supportResistanceConfirmationService;
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
    public boolean isBullish(Stock stock, Trend trend) {

        /*
         * 1. Breakout 50, 100 or 200 EMA
         */
        if(this.isBreakout(stock, trend)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Support 20, 50, 100, 200
         */
        if(this.isNearSupport(stock, trend)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private boolean isBreakout(Stock stock, Trend trend){
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
            //Breakout EMA50
            if(trend == Trend.SHORT) {
                if (breakoutConfirmationService.isBullishConfirmation(stock, ema20) && breakoutService.isBreakOut(prevClose, prevEma20, close, ema20)) {
                    return Boolean.TRUE;
                }
            }
            //Breakout EMA100
            if(trend == Trend.MEDIUM) {
                if (breakoutConfirmationService.isBullishConfirmation(stock, ema50) && breakoutService.isBreakOut(prevClose, prevEma50, close, ema50)) {
                    return Boolean.TRUE;
                }
            }
            //Breakout EMA200
            if(trend == Trend.LONG) {
                /*
                if (breakoutConfirmationService.isBullishConfirmation(stock, ema100) && breakoutService.isBreakOut(prevClose, prevEma100, close, ema100)) {
                    return Boolean.TRUE;
                }*/


                if (breakoutConfirmationService.isBullishConfirmation(stock, ema200) && breakoutService.isBreakOut(prevClose, prevEma200, close, ema200)) {
                    return Boolean.TRUE;
                }
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


    private boolean isNearSupport(Stock stock, Trend trend){

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
            // Near Support EMA 20
            if(trend == Trend.SHORT) {
                if (supportResistanceService.isNearSupport(open, high, low, close, ema20)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(ema5, ema20, ema50)) {
                        //if (formulaService.applyPercentChange(ema10, FibonacciRatio.RATIO_38_2 * -100) >= ema20) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
            //Near Support EMA 50
            if(trend == Trend.MEDIUM) {
                if (supportResistanceService.isNearSupport(open, high, low, close, ema50)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(ema20, ema50, ema100)) {
                        // if (formulaService.applyPercentChange(ema20, FibonacciRatio.RATIO_38_2 * -100) >= ema50) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
            //Near Support EMA 100
            if(trend == Trend.LONG) {

                if (supportResistanceService.isNearSupport(open, high, low, close, ema100)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(ema50, ema100, ema200)) {
                        // if (formulaService.applyPercentChange(ema50, FibonacciRatio.RATIO_38_2 * -100) >= ema100) {
                        return Boolean.TRUE;
                        //}
                    }
                }
                //Near Support EMA 200
                if (supportResistanceService.isNearSupport(open, high, low, close, ema200)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(ema100, ema200, ema200 - 1)) {
                        //if (formulaService.applyPercentChange(ema100, FibonacciRatio.RATIO_38_2 * -100) >= ema200) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
        }
        //Check for Previous Session Near Support
        else if(candleStickService.isLowerHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isLowerLow(StockPriceUtil.buildStockPricePreviousSession(stock))) {
            // Near Support EMA 20
            if (trend == Trend.SHORT) {
                if (supportResistanceConfirmationService.isSupportConfirmed(stock, ema20) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma20)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(prevEma5, prevEma20, prevEma50)) {
                        //if (formulaService.applyPercentChange(prevEma10, FibonacciRatio.RATIO_38_2 * -100) >= prevEma20) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }

            //Near Support EMA 50
            if(trend == Trend.MEDIUM) {
                if (supportResistanceConfirmationService.isSupportConfirmed(stock, ema50) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma50)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(prevEma20, prevEma50, prevEma100)) {
                        //if (formulaService.applyPercentChange(prevEma20, FibonacciRatio.RATIO_38_2 * -100) >= prevEma50) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
            //Near Support EMA 100
            if(trend == Trend.LONG) {

                if (supportResistanceConfirmationService.isSupportConfirmed(stock, ema100) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma100)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(prevEma50, prevEma100, prevEma200)) {
                        // if (formulaService.applyPercentChange(prevEma50, FibonacciRatio.RATIO_38_2 * -100) >= prevEma100) {
                        return Boolean.TRUE;
                        // }
                    }
                }
                //Near Support EMA 200
                if (supportResistanceConfirmationService.isSupportConfirmed(stock, ema200) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma200)) {
                    if (this.isAverageBetweenImmediateLowAndHigh(prevEma100, prevEma200, prevEma200 - 1)) {
                        // if (formulaService.applyPercentChange(prevEma100, FibonacciRatio.RATIO_38_2 * -100) >= prevEma200) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
        }


        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateLowAndHigh(double low, double average, double high){
        return average < low && average > high;
    }



    @Override
    public boolean isBearish(Stock stock, Trend trend) {
        /*
         * 1. Breakdown 50, 100 or 200 EMA
         */
        if(this.isBreakdown(stock, trend)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Resistance 20, 50, 100, 200
         */
        if(this.isNearResistance(stock, trend)){
            return Boolean.TRUE;
        }

        return false;
    }

    private boolean isBreakdown(Stock stock, Trend trend){
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
            //Breakdown EMA20
            if(trend == Trend.SHORT) {
                if (breakoutConfirmationService.isBearishConfirmation(stock, ema20) && breakoutService.isBreakDown(prevClose, prevEma20, close, ema20)) {
                    return Boolean.TRUE;
                }
            }
            //Breakdown EMA50
            if(trend == Trend.MEDIUM) {
                if (breakoutConfirmationService.isBearishConfirmation(stock, ema50) && breakoutService.isBreakDown(prevClose, prevEma50, close, ema50)) {
                    return Boolean.TRUE;
                }
            }
            if(trend == Trend.LONG) {
                /*
                //Breakdown EMA100
                if (breakoutConfirmationService.isBearishConfirmation(stock, ema100) && breakoutService.isBreakDown(prevClose, prevEma100, close, ema100)) {
                    return Boolean.TRUE;
                }*/
                //Breakdown EMA200
                if (breakoutConfirmationService.isBearishConfirmation(stock, ema200) && breakoutService.isBreakDown(prevClose, prevEma200, close, ema200)) {
                    return Boolean.TRUE;
                }
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



    private boolean isNearResistance(Stock stock, Trend trend){

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
            // Near Resistance EMA 20
            if(trend == Trend.SHORT) {
                if (supportResistanceService.isNearResistance(open, high, low, close, ema20)) {
                    if (this.isAverageBetweenImmediateHighAndLow(ema5, ema20, ema50)) {
                        //if (formulaService.applyPercentChange(ema10, FibonacciRatio.RATIO_38_2 * -100) < ema20) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
            // Near Resistance EMA 50
            if(trend == Trend.MEDIUM) {
                if (supportResistanceService.isNearResistance(open, high, low, close, ema50)) {
                    if (this.isAverageBetweenImmediateHighAndLow(ema20, ema50, ema100)) {
                        // if (formulaService.applyPercentChange(ema20, FibonacciRatio.RATIO_38_2 * -100) < ema50) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
            if(trend == Trend.LONG) {

                // Near Resistance EMA 100
                if (supportResistanceService.isNearResistance(open, high, low, close, ema100)) {
                    if (this.isAverageBetweenImmediateHighAndLow(ema50, ema100, ema200)) {
                        // if (formulaService.applyPercentChange(ema50, FibonacciRatio.RATIO_38_2 * -100) < ema100) {
                        return Boolean.TRUE;
                        //}
                    }
                }
                // Near Resistance EMA 200
                if (supportResistanceService.isNearResistance(open, high, low, close, ema200)) {
                    if (this.isAverageBetweenImmediateHighAndLow(ema100, ema200, ema200 + 1)) {
                        //if (formulaService.applyPercentChange(ema100, FibonacciRatio.RATIO_38_2 * -100) < ema200) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
        }
        //Check for Previous Session Near Resistance
        else if(candleStickService.isHigherHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isHigherLow(StockPriceUtil.buildStockPricePreviousSession(stock))){
            // Near Support EMA 20
            if(trend == Trend.SHORT) {
                if (supportResistanceConfirmationService.isResistanceConfirmed(stock, ema20) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma20)) {
                    if (this.isAverageBetweenImmediateHighAndLow(prevEma5, prevEma20, prevEma50)) {
                        // if (formulaService.applyPercentChange(prevEma10, FibonacciRatio.RATIO_38_2 * -100) < prevEma20) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
            //Near Support EMA 50
            if(trend == Trend.MEDIUM) {
                if (supportResistanceConfirmationService.isResistanceConfirmed(stock, ema50) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma50)) {
                    if (this.isAverageBetweenImmediateHighAndLow(prevEma20, prevEma50, prevEma100)) {
                        // if (formulaService.applyPercentChange(prevEma20, FibonacciRatio.RATIO_38_2 * -100) < prevEma50) {
                        return Boolean.TRUE;
                        //}
                    }
                }
            }
            if(trend == Trend.LONG) {

                //Near Support EMA 100
                if (supportResistanceConfirmationService.isResistanceConfirmed(stock, ema100) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma100)) {
                    if (this.isAverageBetweenImmediateHighAndLow(prevEma50, prevEma100, prevEma200)) {
                        //if (formulaService.applyPercentChange(prevEma50, FibonacciRatio.RATIO_38_2 * -100) < prevEma100) {
                        return Boolean.TRUE;
                        //}
                    }
                }
                //Near Support EMA 200
                if (supportResistanceConfirmationService.isResistanceConfirmed(stock, ema200) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma200)) {
                    if (this.isAverageBetweenImmediateHighAndLow(prevEma100, prevEma200, prevEma200 + 1)) {
                        //if (formulaService.applyPercentChange(prevEma100, FibonacciRatio.RATIO_38_2 * -100) < prevEma200) {
                        return Boolean.TRUE;

                        // }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateHighAndLow(double low, double average, double high){
        return average > low && average < high;
    }
}
