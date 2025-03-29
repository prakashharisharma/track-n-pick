package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.service.utils.MovingAverageUtil;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.service.*;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FiftyDaysMovingAverageSupportResistanceServiceImpl implements FiftyDaysMovingAverageSupportResistanceService {

    @Autowired
    private SupportResistanceConfirmationService supportResistanceConfirmationService;
    @Autowired
    private BreakoutConfirmationService breakoutConfirmationService;

    @Autowired
    private SupportResistanceUtilService supportResistanceService;

    @Autowired
    private StockPriceService<StockPrice> stockPriceService;

    @Autowired
    private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private BreakoutService breakoutService;

    @Autowired
    private FormulaService formulaService;



    @Override
    public boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();

        double ema50 = MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals);
        double prevEma50 = MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals);

        //Check for Current Day Breakout
       // if(candleStickService.isHigherHigh(stockPrice) && candleStickService.range(stockPrice) > CandleStickService.MIN_RANGE) {

            //Breakout EMA100

                if (breakoutConfirmationService.isBullishConfirmation(timeframe, stockPrice, stockTechnicals, ema50) && breakoutService.isBreakOut(prevClose, prevEma50, close, ema50)) {
                    return Boolean.TRUE;
                }


        //}


        return Boolean.FALSE;
    }


    @Override
    public boolean isNearSupport(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {



        double open = stockPrice.getOpen();
        double prevOpen = stockPrice.getPrevOpen();
        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double high = stockPrice.getHigh();
        double prevHigh = stockPrice.getPrevHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();


        double ema50 = MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals);
        double prevEma50 = MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals);


        //Check for Current Day
        //if(candleStickService.isLowerHigh(stockPrice) && candleStickService.isLowerLow(stockPrice)) {

            //Near Support EMA 50
                if (supportResistanceService.isNearSupport(open, high, low, close, ema50)) {
                        return Boolean.TRUE;
                }
        //}
        //Check for Previous Session Near Support
        //else if(candleStickService.isPrevLowerHigh(stockPrice) && candleStickService.isPrevLowerLow(stockPrice)) {

            //Near Support EMA 50
                if (supportResistanceConfirmationService.isSupportConfirmed(timeframe, stockPrice, stockTechnicals, ema50) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma50)) {
                        return Boolean.TRUE;

                }
        //}


        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateLowAndHigh(double low, double average, double high){
        return average < low && average > high;
    }





    @Override
    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {


        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();

        double ema50 = MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals);
        double prevEma50 = MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals);


        //Check for Current Day Breakout
        //if(candleStickService.isLowerLow(stockPrice) && candleStickService.range(stockPrice) > CandleStickService.MIN_RANGE){
            //Breakdown EMA50
                if (breakoutConfirmationService.isBearishConfirmation(timeframe, stockPrice, stockTechnicals, ema50) && breakoutService.isBreakDown(prevClose, prevEma50, close, ema50)) {
                    return Boolean.TRUE;
                }
        //}

        return Boolean.FALSE;
    }



    @Override
    public boolean isNearResistance(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {


        double open = stockPrice.getOpen();
        double prevOpen = stockPrice.getPrevOpen();
        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double high = stockPrice.getHigh();
        double prevHigh = stockPrice.getPrevHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();


        double ema50 = MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals);
        double prevEma50 = MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals);

        //Check for Current Day
        //if(candleStickService.isHigherHigh(stockPrice) && candleStickService.isHigherLow(stockPrice)) {

            // Near Resistance EMA 50
                if (supportResistanceService.isNearResistance(open, high, low, close, ema50)) {

                        return Boolean.TRUE;

                }
        //}
        //Check for Previous Session Near Resistance
        //else if(candleStickService.isPrevHigherHigh(stockPrice) && candleStickService.isPrevHigherLow(stockPrice)){

            //Near Support EMA 50
                if (supportResistanceConfirmationService.isResistanceConfirmed(timeframe, stockPrice, stockTechnicals, ema50) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma50)) {

                        return Boolean.TRUE;

                }
        //}

        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateHighAndLow(double low, double average, double high){
        return average > low && average < high;
    }
}
