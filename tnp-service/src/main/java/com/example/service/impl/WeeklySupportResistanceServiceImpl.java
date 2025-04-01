package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.OHLCV;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeeklySupportResistanceServiceImpl implements WeeklySupportResistanceService {

    @Autowired private SupportResistanceConfirmationService supportResistanceConfirmationService;
    @Autowired private BreakoutConfirmationService breakoutConfirmationService;
    @Autowired private SupportResistanceUtilService supportResistanceService;
    @Autowired private CandleStickService candleStickService;
    @Autowired private BreakoutService breakoutService;
    @Autowired private CalendarService calendarService;
    @Autowired private FormulaService formulaService;
    @Autowired private OhlcvService ohlcvService;
    @Autowired private MiscUtil miscUtil;

    @Override
    public boolean isBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double open = stockPrice.getOpen();

        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();

        double support = stockPrice.getLow();
        double resistance = stockPrice.getHigh();

        if (open < support) {
            resistance = support;
        }

        // Check for Current Day Breakout
        if (breakoutConfirmationService.isBullishConfirmation(
                timeframe, stockPrice, stockTechnicals, resistance)) {
            // Breakout resistance

            if (breakoutService.isBreakOut(prevClose, resistance, close, resistance)) {

                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

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

        if (open > resistance) {
            support = resistance;
        }

        // Check for Current Day
        // if(candleStickService.isLowerHigh(stockPrice) &&
        // candleStickService.isLowerLow(stockPrice)) {
        // Near Support
        if (supportResistanceService.isNearSupport(open, high, low, close, support)) {
            return Boolean.TRUE;
        }
        // }

        // Check for Previous Session Near Support
        // else if(candleStickService.isPrevLowerHigh(stockPrice) &&
        // candleStickService.isPrevLowerLow(stockPrice)){
        // Near Support EMA 20
        if (supportResistanceConfirmationService.isSupportConfirmed(
                        timeframe, stockPrice, stockTechnicals, support)
                && supportResistanceService.isNearSupport(
                        prevOpen, prevHigh, prevLow, prevClose, support)) {
            return Boolean.TRUE;
        }
        // }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBreakdown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double open = stockPrice.getOpen();

        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double support = stockPrice.getLow();
        double resistance = stockPrice.getHigh();

        if (open > resistance) {
            support = resistance;
        }

        // Check for Current Day Breakout
        if (breakoutConfirmationService.isBearishConfirmation(
                        timeframe, stockPrice, stockTechnicals, support)
                && candleStickService.range(stockPrice) > CandleStickService.MIN_RANGE) {
            // Breakdown
            if (breakoutService.isBreakDown(prevClose, support, close, support)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

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

        if (open < support) {
            resistance = support;
        }

        // Check for Current Day
        // if(candleStickService.isHigherHigh(stockPrice) &&
        // candleStickService.isHigherLow(stockPrice)) {
        // Near Resistance EMA 20
        if (supportResistanceService.isNearResistance(open, high, low, close, resistance)) {
            return Boolean.TRUE;
        }

        // }

        // Check for Previous Session Near Resistance
        // else if(candleStickService.isPrevHigherHigh(stockPrice) &&
        // candleStickService.isPrevHigherLow(stockPrice)){
        // Near Support EMA 20
        if (supportResistanceConfirmationService.isResistanceConfirmed(
                        timeframe, stockPrice, stockTechnicals, resistance)
                && supportResistanceService.isNearResistance(
                        prevOpen, prevHigh, prevLow, prevClose, resistance)) {
            return Boolean.TRUE;
        }
        // }

        return Boolean.FALSE;
    }

    @Override
    public OHLCV supportAndResistance(Stock stock) {

        LocalDate from = miscUtil.previousWeekFirstDay();
        LocalDate to = miscUtil.previousWeekLastDay();

        log.info(" {} from {} to {}", stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);
        List<OHLCV> ohlcvList = ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if (!ohlcvList.isEmpty()) {

            OHLCV weeklyOpen = ohlcvList.get(0);
            OHLCV weeklyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV weeklyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV weeklyClose = ohlcvList.get(ohlcvList.size() - 1);
            long weeklyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            result.setOpen(weeklyOpen.getOpen());
            result.setHigh(weeklyHigh.getHigh());
            result.setLow(weeklyLow.getLow());
            result.setClose(weeklyClose.getClose());
            result.setVolume(weeklyVolume);
            result.setBhavDate(bhavDate);
        }

        return result;
    }

    @Override
    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to) {

        log.info(" {} from {} to {}", nseSymbol, from, to);

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
        } catch (Exception e) {
            log.error("{} An error occurred while fetching bhav", nseSymbol, e);
        }

        if (!ohlcvList.isEmpty()) {

            OHLCV weeklyOpen = ohlcvList.get(0);
            OHLCV weeklyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV weeklyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV weeklyClose = ohlcvList.get(ohlcvList.size() - 1);
            long weeklyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            if (bhavDate != null) {
                result.setBhavDate(bhavDate);
            }

            if (weeklyOpen.getOpen() != null) {
                result.setOpen(weeklyOpen.getOpen());
            }
            if (weeklyHigh.getHigh() != null) {
                result.setHigh(weeklyHigh.getHigh());
            }
            if (weeklyLow.getLow() != null) {
                result.setLow(weeklyLow.getLow());
            }
            if (weeklyClose.getClose() != null) {
                result.setClose(weeklyClose.getClose());
            }

            result.setVolume(weeklyVolume);
        }

        return result;
    }
}
