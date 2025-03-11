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
public class DailySupportResistanceServiceImpl implements DailySupportResistanceService {

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
        double support = stockPrice.getPrevLow();
        double resistance = stockPrice.getPrevHigh();

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
        double support = stockPrice.getPrevLow();
        double resistance = stockPrice.getPrevHigh();

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
        double support = stockPrice.getPrevLow();
        double resistance = stockPrice.getPrevHigh();

        if(open > resistance){
            support = resistance;
        }

        //Check for Current Day Breakout
         if(breakoutConfirmationService.isBearishConfirmation(stock, support) && candleStickService.range(stock) > CandleStickService.MIN_RANGE){
            //Breakdown
            if(breakoutService.isBreakDown(prevClose, support, close, support)){
                return Boolean.TRUE;
            }
        }
        //Check for previous Session Breakout
         /*
        else if(candleStickService.rangePrev(stock) > CandleStickService.MIN_RANGE){
            //Breakdown Previous Session EMA50
            if (breakoutConfirmationService.isBearishFollowup(stock, support) && breakoutService.isBreakDown(prevPrevClose, support, prevClose, support)) {
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
        double support = stockPrice.getPrevLow();
        double resistance = stockPrice.getPrevHigh();

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


        return Boolean.FALSE;
    }


    @Override
    public OHLCV supportAndResistance(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();;
        OHLCV result = new OHLCV();
        result.setOpen(stockPrice.getPrevOpen());
        result.setHigh(stockPrice.getPrevHigh());
        result.setLow(stockPrice.getPrevLow());
        result.setClose(stockPrice.getPrevClose());

        return result;
    }
}
