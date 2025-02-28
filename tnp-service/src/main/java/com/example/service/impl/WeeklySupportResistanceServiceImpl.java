package com.example.service.impl;

import com.example.dto.OHLCV;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.*;
import com.example.service.util.StockPriceUtil;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class WeeklySupportResistanceServiceImpl implements WeeklySupportResistanceService {

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
    private CalendarService calendarService;

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private OhlcvService ohlcvService;

    @Autowired
    private MiscUtil miscUtil;

    @Override
    public boolean isBullish(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        /*
         * 1. Breakout
         */
        if(this.isBreakout(stock)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Support
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
        double support = stockTechnicals.getPrevWeekLow();
        double resistance = stockTechnicals.getPrevWeekHigh();

        if(open < support){
            resistance = support;
        }

        //Check for Current Day Breakout
         if(breakoutConfirmationService.isBullishConfirmation(stock, resistance) && candleStickService.range(stock) > CandleStickService.MIN_RANGE) {
            //Breakout resistance

            if(breakoutService.isBreakOut(prevClose, resistance, close, resistance)){

                return Boolean.TRUE;
            }
        }
         /*
        //Check for previous Session Breakout
        else if(candleStickService.rangePrev(stock) > CandleStickService.MIN_RANGE){
            //Breakout Previous Session EMA50
            if (breakoutConfirmationService.isBullishFollowup(stock, resistance) && breakoutService.isBreakOut(prevPrevClose, resistance, prevClose, resistance)) {
                return Boolean.TRUE;
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
        double support = stockTechnicals.getPrevWeekLow();
        double resistance = stockTechnicals.getPrevWeekHigh();

        if(open > resistance){
            support = resistance;
        }

        //Check for Current Day
        if(candleStickService.isLowerHigh(stock) && candleStickService.isLowerLow(stock)) {
            // Near Support
            if (supportResistanceService.isNearSupport(open, high, low, close, support)) {
                return Boolean.TRUE;
            }
        }
        //Check for Previous Session Near Support
        else if(candleStickService.isLowerHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isLowerLow(StockPriceUtil.buildStockPricePreviousSession(stock))){
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isSupportConfirmed(stock, support) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, support)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }



    @Override
    public boolean isBearish(Stock stock) {
        /*
         * 1. Breakdown support
         */
        if(this.isBreakdown(stock)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Resistance
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
        double support = stockTechnicals.getPrevMonthLow();
        double resistance = stockTechnicals.getPrevMonthHigh();

        if(open > resistance){
            support = resistance;
        }

        //Check for previous Session Breakout
        if(candleStickService.rangePrev(stock) > CandleStickService.MIN_RANGE){
            //Breakdown Previous Session EMA50
            if (breakoutConfirmationService.isBearishFollowup(stock, support) && breakoutService.isBreakDown(prevPrevClose, support, prevClose, support)) {
                return Boolean.TRUE;
            }

        }
        /*
        //Check for Current Day Breakout
        else if(breakoutConfirmationService.isBearishConfirmation(stock, support) && candleStickService.range(stock) > CandleStickService.MIN_RANGE){
            //Breakdown
            if(breakoutService.isBreakDown(prevClose, support, close, support)){
                return Boolean.TRUE;
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
        double support = stockTechnicals.getPrevWeekLow();
        double resistance = stockTechnicals.getPrevWeekHigh();

        if(open < support){
            resistance = support;
        }


        //Check for Current Day
        if(candleStickService.isHigherHigh(stock) && candleStickService.isHigherLow(stock)) {
            // Near Resistance EMA 20
            if (supportResistanceService.isNearResistance(open, high, low, close, resistance)) {
                return Boolean.TRUE;
            }

        }
        //Check for Previous Session Near Resistance
        else if(candleStickService.isHigherHigh(StockPriceUtil.buildStockPricePreviousSession(stock)) && candleStickService.isHigherLow(StockPriceUtil.buildStockPricePreviousSession(stock))){
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isResistanceConfirmed(stock, resistance) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, resistance)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }



    @Override
    public OHLCV supportAndResistance(Stock stock) {

        LocalDate from = miscUtil.previousWeekFirstDay();
        LocalDate to  = miscUtil.previousWeekLastDay();

        log.info(" {} from {} to {}",stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);
        List<OHLCV> ohlcvList =   ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if(!ohlcvList.isEmpty()) {

            OHLCV weeklyOpen = ohlcvList.get(0);
            OHLCV weeklyHigh = Collections.max(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV weeklyLow = Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV weeklyClose = ohlcvList.get(ohlcvList.size()-1);

            result.setOpen(weeklyOpen.getOpen());
            result.setHigh(weeklyHigh.getHigh());
            result.setLow(weeklyLow.getLow());
            result.setClose(weeklyClose.getClose());
        }

        return result;
    }
}
