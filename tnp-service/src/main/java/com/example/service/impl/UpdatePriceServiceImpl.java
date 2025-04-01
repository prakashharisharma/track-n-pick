package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.dto.OHLCV;

import com.example.service.StockPriceService;
import com.example.service.UpdatePriceService;

import com.example.service.*;
import com.example.data.storage.repo.PriceTemplate;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.util.MiscUtil;
import com.example.dto.io.StockPriceIO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdatePriceServiceImpl implements UpdatePriceService {


    private final PriceTemplate priceTemplate;
    private final StockPriceService<StockPrice> stockPriceService;
    private final MiscUtil miscUtil;
    private final CalendarService calendarService;

    private final YearlySupportResistanceService yearlySupportResistanceService;

    private final QuarterlySupportResistanceService quarterlySupportResistanceService;

    private final MonthlySupportResistanceService monthlySupportResistanceService;

    private final WeeklySupportResistanceService weeklySupportResistanceService;

    @Override
    public void updatePrice(Timeframe timeframe, Stock stock, com.example.data.storage.documents.StockPrice stockPrice) {

        //com.example.storage.model.StockPrice stockPrice = this.build(stockPriceIO);

        log.info("{} Updating {} price", stock.getNseSymbol(), timeframe);

        stockPriceService.createOrUpdate(stock, timeframe, stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose(), stockPrice.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());

        log.info("{} Updated {} price", stock.getNseSymbol(), timeframe);

        /*
        if (calendarService.isLastTradingSessionOfYear(miscUtil.currentDate())) {
            try{
                log.info("{} Updating yearly price", stock.getNseSymbol());
                StockPriceIO yearlyStockPriceIO = this.build(Timeframe.YEARLY, stock, stockPriceIO);
                stockPrice = this.build(yearlyStockPriceIO);
                priceTemplate.upsert(Timeframe.YEARLY, stockPrice);
                stockPriceService.createOrUpdate(stock, Timeframe.YEARLY, stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose(), stockPrice.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                log.info("{} Updated yearly price", stock.getNseSymbol());
                miscUtil.delay(25);
            }catch(Exception e){
                log.error(" {} An error occurred while updating yearly price", stock.getNseSymbol());
            }
        }

        if (calendarService.isLastTradingSessionOfQuarter(miscUtil.currentDate())) {
            try{
                log.info("{} Updating quarterly price", stock.getNseSymbol());
                StockPriceIO quarterlyStockPriceIO = this.build(Timeframe.QUARTERLY, stock, stockPriceIO);
                stockPrice = this.build(quarterlyStockPriceIO);
                priceTemplate.upsert(Timeframe.QUARTERLY, stockPrice);
                stockPriceService.createOrUpdate(stock, Timeframe.QUARTERLY, stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose(), stockPrice.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                log.info("{} Updated quarterly price", stock.getNseSymbol());
                miscUtil.delay(25);
            }catch(Exception e){
                log.error(" {} An error occurred while updating quarterly price", stock.getNseSymbol());
            }
        }*/

        /*
        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
            try{
                log.info("{} Updating monthly price", stock.getNseSymbol());
                StockPriceIO monthlyStockPriceIO = this.build(Timeframe.MONTHLY, stock, stockPriceIO);
                stockPrice = this.build(monthlyStockPriceIO);
                stockPriceService.createOrUpdate(stock, Timeframe.MONTHLY, stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose(), stockPrice.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                priceTemplate.upsert(Timeframe.MONTHLY, stockPrice);
                log.info("{} Updated monthly price", stock.getNseSymbol());
                miscUtil.delay(25);
            }catch(Exception e){
                log.error(" {} An error occurred while updating monthly price", stock.getNseSymbol());
            }
        }

        if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
            try {
                log.info("{} Updating weekly price", stock.getNseSymbol());
                StockPriceIO weeklyStockPriceIO = this.build(Timeframe.WEEKLY, stock, stockPriceIO);
                stockPrice = this.build(weeklyStockPriceIO);
                stockPriceService.createOrUpdate(stock, Timeframe.WEEKLY, stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose(), stockPrice.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                priceTemplate.upsert(Timeframe.WEEKLY, stockPrice);
                log.info("{} Updated weekly price", stock.getNseSymbol());
                miscUtil.delay(25);
            }catch(Exception e){
                log.error(" {} An error occurred while updating weekly price", stock.getNseSymbol());
            }
        }
        */
    }

    @Override
    public com.example.data.storage.documents.StockPrice build(StockPriceIO stockPriceIO) {
        return new com.example.data.storage.documents.StockPrice(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
                stockPriceIO.getLow(), stockPriceIO.getClose(), stockPriceIO.getTottrdqty());
    }

    @Override
    public com.example.data.storage.documents.StockPrice build(Timeframe timeframe, Stock stock, LocalDate sessionDate){

        LocalDate from = miscUtil.yearFirstDay(sessionDate);
        LocalDate to = sessionDate;
        OHLCV ohlcv = null;
        if(timeframe == Timeframe.YEARLY){
            ohlcv = yearlySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);
        }
        else if(timeframe == Timeframe.QUARTERLY){
            from = miscUtil.quarterFirstDay(sessionDate);
            ohlcv = quarterlySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);
        }
        else if(timeframe == Timeframe.MONTHLY){
            from = sessionDate.with(TemporalAdjusters.firstDayOfMonth());
            ohlcv = monthlySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);
        }else if(timeframe == Timeframe.WEEKLY){
            from = sessionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            ohlcv = weeklySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);
        }

        if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
            return new com.example.data.storage.documents.StockPrice(stock.getNseSymbol(), ohlcv.getBhavDate(), ohlcv.getOpen(), ohlcv.getHigh(),
                    ohlcv.getLow(), ohlcv.getClose(), ohlcv.getVolume());
        }

        return new com.example.data.storage.documents.StockPrice(stock.getNseSymbol(), from.atStartOfDay().toInstant(ZoneOffset.UTC), 0.0, 0.0,
                0.0, 0.0, 0L);

        //throw new IllegalArgumentException(stock.getNseSymbol() + "OHLCV does not exist for"+ timeframe +" " + to);
    }

    private StockPriceIO build(Timeframe timeframe, Stock stock, OHLCV ohlcv, LocalDate sessionDate) {

            StockPriceIO newStockPriceIO = new StockPriceIO("NSE",
                    stock.getCompanyName(),
                    stock.getNseSymbol(),
                    stock.getSeries(),
                    ohlcv.getOpen(),
                    ohlcv.getHigh(),
                    ohlcv.getLow(),
                    ohlcv.getClose(),
                    ohlcv.getClose(),
                    ohlcv.getOpen(),
                    ohlcv.getVolume(),
                    0.00,
                    ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                    1,
                    stock.getIsinCode());

            newStockPriceIO.setBhavDate(ohlcv.getBhavDate());

            newStockPriceIO.setTimestamp(ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
            newStockPriceIO.setTimeFrame(timeframe);

            return  newStockPriceIO;

    }

}
