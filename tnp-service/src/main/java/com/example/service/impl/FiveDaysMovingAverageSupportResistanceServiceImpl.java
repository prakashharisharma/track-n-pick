package com.example.service.impl;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.transactional.service.StockPriceService;
import com.example.transactional.service.StockTechnicalsService;
import com.example.transactional.utils.MovingAverageUtil;

import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Timeframe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FiveDaysMovingAverageSupportResistanceServiceImpl implements FiveDaysMovingAverageSupportResistanceService {

    private final SupportResistanceConfirmationService supportResistanceConfirmationService;

    private final BreakoutConfirmationService breakoutConfirmationService;

    private final SupportResistanceUtilService supportResistanceService;

    private final CandleStickService candleStickService;

    private final BreakoutService breakoutService;

    private final FormulaService formulaService;


    private final StockPriceService<StockPrice> stockPriceService;


    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;


    @Override
    public boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double ema5 = MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals);
        double prevEma5 = MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals);


        //Check for Current Day Breakout
        //if(candleStickService.isHigherHigh(stockPrice) && candleStickService.range(stockPrice) > CandleStickService.MIN_RANGE) {
            //Breakout EMA20
            if (breakoutConfirmationService.isBullishConfirmation(timeframe, stockPrice, stockTechnicals, ema5) && breakoutService.isBreakOut(prevClose, prevEma5, close, ema5)) {
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
        double ema5 = MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals);
        double prevEma5 = MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals);

        //Check for Current Day
        //if(candleStickService.isLowerHigh(stockPrice) && candleStickService.isLowerLow(stockPrice)) {
            // Near Support EMA 20
            if (supportResistanceService.isNearSupport(open, high, low, close, ema5)) {
                return Boolean.TRUE;
            }
        //}
        //Check for Previous Session Near Support
        //else if(candleStickService.isPrevLowerHigh(stockPrice) && candleStickService.isPrevLowerLow(stockPrice)) {
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isSupportConfirmed(timeframe, stockPrice, stockTechnicals, ema5) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, prevEma5)) {
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
        double ema5 = MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals);
        double prevEma5 = MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals);


        //Check for Current Day Breakout
        //if(candleStickService.isLowerLow(stockPrice) && candleStickService.range(stockPrice) > CandleStickService.MIN_RANGE){
            //Breakdown EMA20
            if (breakoutConfirmationService.isBearishConfirmation(timeframe, stockPrice, stockTechnicals, ema5) && breakoutService.isBreakDown(prevClose, prevEma5, close, ema5)) {
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
        double ema5 = MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals);
        double prevEma5 = MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals);


        //Check for Current Day
       // if(candleStickService.isHigherHigh(stockPrice) && candleStickService.isHigherLow(stockPrice)) {
            // Near Resistance EMA 20
            if (supportResistanceService.isNearResistance(open, high, low, close, ema5)) {
                return Boolean.TRUE;
            }
       // }
        //Check for Previous Session Near Resistance
       // else if(candleStickService.isPrevHigherHigh(stockPrice) && candleStickService.isPrevHigherLow(stockPrice)){
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isResistanceConfirmed(timeframe, stockPrice, stockTechnicals, ema5) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, prevEma5)) {
                return Boolean.TRUE;
            }
       // }

        return Boolean.FALSE;
    }

    private boolean isAverageBetweenImmediateHighAndLow(double low, double average, double high){
        return average > low && average < high;
    }
}
