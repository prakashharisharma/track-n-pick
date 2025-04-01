package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.dto.OHLCV;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.service.*;
import com.example.data.storage.documents.result.HighLowResult;
import com.example.data.storage.repo.PriceTemplate;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
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
public class YearlySupportResistanceServiceImpl implements YearlySupportResistanceService {

    @Autowired
    private SupportResistanceConfirmationService supportResistanceConfirmationService;

    @Autowired
    private StockPriceService<StockPrice> stockPriceService;

    @Autowired
    private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

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
    private PriceTemplate priceTemplate;

    @Autowired
    private OhlcvService ohlcvService;

    @Autowired
    private MiscUtil miscUtil;


    @Override
    public boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals){

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
    public boolean isNearSupport(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals){

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
       // if(candleStickService.isLowerHigh(stockPrice) && candleStickService.isLowerLow(stockPrice)) {
            // Near Support
            if (supportResistanceService.isNearSupport(open, high, low, close, support)) {
                return Boolean.TRUE;
            }
        //}

        //Check for Previous Session Near Support
        //else if(candleStickService.isPrevLowerHigh(stockPrice) && candleStickService.isPrevLowerLow(stockPrice)){
            // Near Support EMA 20
            if (supportResistanceConfirmationService.isSupportConfirmed(timeframe, stockPrice, stockTechnicals, support) && supportResistanceService.isNearSupport(prevOpen, prevHigh, prevLow, prevClose, support)) {
                return Boolean.TRUE;
            }
        //}

        return Boolean.FALSE;
    }


    @Override
    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

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
    public boolean isNearResistance(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

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

       // }
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

        LocalDate from = miscUtil.currentYearFirstDay().minusYears(1);
        LocalDate to  = miscUtil.currentYearFirstDay().minusDays(1);

        log.info(" {} from {} to {}",stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);

        List<OHLCV> ohlcvList =   ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if(!ohlcvList.isEmpty()) {
            OHLCV yearlyOpen = ohlcvList.get(0);
            OHLCV yearlyHigh = Collections.max(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV yearlyLow = Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV yearlyClose = ohlcvList.get(ohlcvList.size()-1);
            long yearlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            //Instant bhavDate = ohlcvList.get(ohlcvList.size()-1).getBhavDate();

            result.setOpen(yearlyOpen.getOpen());
            result.setHigh(yearlyHigh.getHigh());
            result.setLow(yearlyLow.getLow());
            result.setClose(yearlyClose.getClose());
            result.setVolume(yearlyVolume);
            result.setBhavDate(to.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
        }

        return result;
    }

    @Override
    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to) {

        log.info(" {} from {} to {}",nseSymbol, from, to);

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
            OHLCV yearlyOpen = ohlcvList.get(0);
            OHLCV yearlyHigh = Collections.max(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV yearlyLow = Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV yearlyClose = ohlcvList.get(ohlcvList.size()-1);
            long yearlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size()-1).getBhavDate();

            if(bhavDate!=null) {
                result.setBhavDate(bhavDate);
            }
            if(yearlyOpen.getOpen()!=null) {
                result.setOpen(yearlyOpen.getOpen());
            }
            if(yearlyHigh.getHigh()!=null) {
                result.setHigh(yearlyHigh.getHigh());
            }
            if(yearlyLow.getLow()!=null) {
                result.setLow(yearlyLow.getLow());
            }
            if(yearlyClose.getClose()!=null) {
                result.setClose(yearlyClose.getClose());
            }

            result.setVolume(yearlyVolume);

        }

        return result;
    }

    @Override
    public OHLCV supportAndResistance(String nseSymbol, LocalDate onDate) {

        LocalDate to  = onDate;

        LocalDate from = to.minusYears(1);


        log.info(" {} from {} to {}",nseSymbol, from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);

        HighLowResult highLowResult =   priceTemplate.getHighLowByDate(nseSymbol, from, to);

        if(highLowResult != null && !highLowResult.get_id().equalsIgnoreCase("NO_DATA_FOUND")) {
            result.setHigh(highLowResult.getHigh());
            result.setLow(highLowResult.getLow());
        }


        return result;
    }
}
