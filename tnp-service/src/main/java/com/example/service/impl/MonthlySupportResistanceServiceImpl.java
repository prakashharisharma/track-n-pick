package com.example.service.impl;

import com.example.dto.OHLCV;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.transactional.service.StockPriceService;
import com.example.transactional.service.StockTechnicalsService;
import com.example.transactional.model.master.Stock;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.io.model.type.Timeframe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class MonthlySupportResistanceServiceImpl implements MonthlySupportResistanceService {

    @Autowired
    private StockPriceService<StockPrice> stockPriceService;

    @Autowired
    private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

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
    public  boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double support = stockPrice.getLow();
        double resistance = stockPrice.getHigh();

        if(open < support){
            resistance = support;
        }

        //Check for Current Day Breakout
        if(breakoutConfirmationService.isBullishConfirmation(timeframe, stockPrice, stockTechnicals, resistance) && candleStickService.range(stockPrice) > CandleStickService.MIN_RANGE) {

            //Breakout resistance
            if(breakoutService.isBreakOut(prevClose, resistance, close, resistance)){

                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }



    @Override
    public  boolean isNearSupport(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {


        double open = stockPrice.getOpen();
        double prevOpen = stockPrice.getPrevOpen();
        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double high = stockPrice.getHigh();
        double prevHigh = stockPrice.getPrevHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double support = stockPrice.getLow();
        double resistance = stockPrice.getHigh();

        if(open > resistance){
            support = resistance;
        }

        //Check for Current Day
        //if(candleStickService.isLowerHigh(stockPrice) && candleStickService.isLowerLow(stockPrice)) {
            // Near Support
            if (supportResistanceService.isNearSupport(open, high, low, close, support)) {
                    return Boolean.TRUE;
            }
        //}
        //Check for Previous Session Near Support
       // else if(candleStickService.isPrevLowerHigh(stockPrice) && candleStickService.isPrevLowerLow(stockPrice)){
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isSupportConfirmed(timeframe, stockPrice, stockTechnicals, support) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, support)) {
                return Boolean.TRUE;
            }
        //}

        return Boolean.FALSE;
    }




    @Override
    public  boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {


        double open = stockPrice.getOpen();

        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();

        double support = stockPrice.getLow();
        double resistance = stockPrice.getHigh();

        if(open > resistance){
            support = resistance;
        }


        //Check for Current Day Breakout
         if(breakoutConfirmationService.isBearishConfirmation(timeframe, stockPrice, stockTechnicals, support) && candleStickService.range(stockPrice) > CandleStickService.MIN_RANGE){
            //Breakdown
            if(breakoutService.isBreakDown(prevClose, support, close, support)){
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }



    @Override
    public  boolean isNearResistance(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double open = stockPrice.getOpen();
        double prevOpen = stockPrice.getPrevOpen();
        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double high = stockPrice.getHigh();
        double prevHigh = stockPrice.getPrevHigh();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double support = stockPrice.getLow();
        double resistance = stockPrice.getHigh();

        if(open < support){
            resistance = support;
        }


        //Check for Current Day
        //if(candleStickService.isHigherHigh(stockPrice) && candleStickService.isHigherLow(stockPrice)) {
            // Near Resistance EMA 20
            if (supportResistanceService.isNearResistance(open, high, low, close, resistance)) {
                    return Boolean.TRUE;
            }

        //}
        //Check for Previous Session Near Resistance
        //else if(candleStickService.isPrevHigherHigh(stockPrice) && candleStickService.isPrevHigherLow(stockPrice)){
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isResistanceConfirmed(timeframe, stockPrice, stockTechnicals, resistance) && supportResistanceService.isNearResistance(prevOpen, prevHigh, prevLow, prevClose, resistance)) {
                return Boolean.TRUE;
            }
        //}

        return Boolean.FALSE;
    }



    @Override
    public OHLCV supportAndResistance(Stock stock) {

        LocalDate from = miscUtil.previousMonthFirstDay();
        LocalDate to  = miscUtil.previousMonthLastDay();

        log.info("{} from {} to {}",stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);
        List<OHLCV> ohlcvList =   ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if(!ohlcvList.isEmpty()) {
            OHLCV monthlyOpen = ohlcvList.get(0);
            OHLCV monthlyHigh = Collections.max(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV monthlyLow = Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV monthlyClose = ohlcvList.get(ohlcvList.size()-1);
            long monthlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size()-1).getBhavDate();

            result.setOpen(monthlyOpen.getOpen());
            result.setHigh(monthlyHigh.getHigh());
            result.setLow(monthlyLow.getLow());
            result.setClose(monthlyClose.getClose());
            result.setVolume(monthlyVolume);
            result.setBhavDate(bhavDate);
        }

        return result;
    }

    @Override
    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to) {

        log.info("{} from {} to {}",nseSymbol, from, to);

        OHLCV result = new OHLCV();
        result.setOpen(0.0);
        result.setHigh(0.0);
        result.setLow(0.0);
        result.setClose(0.0);
        result.setVolume(0l);
        result.setBhavDate(to.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
        List<OHLCV> ohlcvList = new ArrayList<>();
        try {
             ohlcvList = ohlcvService.fetch(nseSymbol, from, to);
        }catch(Exception e){
            log.error("{} An error occurred while fetching bhav", nseSymbol, e);
        }

        if(!ohlcvList.isEmpty()) {
            OHLCV monthlyOpen = ohlcvList.get(0);
            OHLCV monthlyHigh = Collections.max(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV monthlyLow = Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV monthlyClose = ohlcvList.get(ohlcvList.size()-1);
            long monthlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size()-1).getBhavDate();

            if(bhavDate!=null) {
                result.setBhavDate(bhavDate);
            }

            if(monthlyOpen.getOpen()!=null) {
                result.setOpen(monthlyOpen.getOpen());
            }
            if(monthlyHigh.getHigh()!=null) {
                result.setHigh(monthlyHigh.getHigh());
            }
            if(monthlyLow.getLow()!=null) {
                result.setLow(monthlyLow.getLow());
            }
            if(monthlyClose.getClose()!=null) {
                result.setClose(monthlyClose.getClose());
            }

            result.setVolume(monthlyVolume);

        }

        return result;
    }
}
