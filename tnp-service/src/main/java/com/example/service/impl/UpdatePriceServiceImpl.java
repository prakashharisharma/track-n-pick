package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.storage.repo.PriceTemplate;
import com.example.data.transactional.entities.*;
import com.example.dto.common.OHLCV;
import com.example.dto.io.StockPriceIO;
import com.example.service.*;
import com.example.service.StockPriceService;
import com.example.service.UpdatePriceService;
import com.example.util.MiscUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdatePriceServiceImpl implements UpdatePriceService {

    private final PriceTemplate priceTemplate;
    private final StockPriceService<StockPrice> stockPriceService;
    private final MiscUtil miscUtil;
    private final CalendarService calendarService;

    private final OhlcvService ohlcvService;

    private final YearlySupportResistanceService yearlySupportResistanceService;

    private final QuarterlySupportResistanceService quarterlySupportResistanceService;

    private final MonthlySupportResistanceService monthlySupportResistanceService;

    private final WeeklySupportResistanceService weeklySupportResistanceService;

    @Override
    public void updatePrice(
            Timeframe timeframe,
            Stock stock,
            com.example.data.storage.documents.StockPrice stockPrice) {

        // com.example.storage.model.StockPrice stockPrice = this.build(stockPriceIO);

        log.info("{} Updating {} price", stock.getNseSymbol(), timeframe);

        stockPriceService.createOrUpdate(
                stock,
                timeframe,
                stockPrice.getOpen(),
                stockPrice.getHigh(),
                stockPrice.getLow(),
                stockPrice.getClose(),
                stockPrice.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());

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
        return new com.example.data.storage.documents.StockPrice(
                stockPriceIO.getNseSymbol(),
                stockPriceIO.getBhavDate(),
                stockPriceIO.getOpen(),
                stockPriceIO.getHigh(),
                stockPriceIO.getLow(),
                stockPriceIO.getClose(),
                stockPriceIO.getTottrdqty());
    }

    @Override
    public com.example.data.storage.documents.StockPrice build(
            Timeframe timeframe, Stock stock, LocalDate sessionDate) {

        LocalDate from = miscUtil.yearFirstDay(sessionDate);
        LocalDate to = sessionDate;
        OHLCV ohlcv = null;
        if (timeframe == Timeframe.YEARLY) {
            ohlcv =
                    yearlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), from, to);
        } else if (timeframe == Timeframe.QUARTERLY) {
            from = miscUtil.quarterFirstDay(sessionDate);
            ohlcv =
                    quarterlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), from, to);
        } else if (timeframe == Timeframe.MONTHLY) {
            from = sessionDate.with(TemporalAdjusters.firstDayOfMonth());
            ohlcv =
                    monthlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), from, to);
        } else if (timeframe == Timeframe.WEEKLY) {
            from = sessionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            ohlcv =
                    weeklySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), from, to);
        }

        if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
            return new com.example.data.storage.documents.StockPrice(
                    stock.getNseSymbol(),
                    ohlcv.getBhavDate(),
                    ohlcv.getOpen(),
                    ohlcv.getHigh(),
                    ohlcv.getLow(),
                    ohlcv.getClose(),
                    ohlcv.getVolume());
        }

        return new com.example.data.storage.documents.StockPrice(
                stock.getNseSymbol(),
                from.atStartOfDay().toInstant(ZoneOffset.UTC),
                0.0,
                0.0,
                0.0,
                0.0,
                0L);

        // throw new IllegalArgumentException(stock.getNseSymbol() + "OHLCV does not exist for"+
        // timeframe +" " + to);
    }

    @Override
    // @Cacheable(value = "stockPrices", key = "{#stock.nseSymbol, #timeframe, #sessionDate}")
    public StockPrice buildBack(Timeframe timeframe, Stock stock, LocalDate sessionDate) {

        LocalDate from = null;
        LocalDate to = sessionDate;
        OHLCV ohlcv = null;
        StockPrice stockPrice = null;
        if (timeframe == Timeframe.YEARLY) {
            from = sessionDate.minusYears(7).with(TemporalAdjusters.firstDayOfYear());
            stockPrice = new StockPriceMonthly();

        } else if (timeframe == Timeframe.MONTHLY) {
            from = sessionDate.minusMonths(7).with(TemporalAdjusters.firstDayOfMonth());
            stockPrice = new StockPriceMonthly();

        } else if (timeframe == Timeframe.WEEKLY) {
            from =
                    sessionDate
                            .minusWeeks(7)
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            stockPrice = new StockPriceWeekly();
        } else {
            from = sessionDate.minusDays(10);
            to = sessionDate;
            stockPrice = new StockPriceDaily();
        }

        List<OHLCV> ohlcvList = ohlcvService.fetch(timeframe, stock.getNseSymbol(), from, to);

        stockPrice.setStock(stock);
        stockPrice.setTimeframe(timeframe);

        if (ohlcvList == null || ohlcvList.isEmpty()) {
            return stockPrice;
        }

        int size = ohlcvList.size();
        OHLCV latest = ohlcvList.get(size - 1); // last = current day
        stockPrice.setSessionDate(
                latest.getBhavDate().atZone(ZoneId.systemDefault()).toLocalDate());
        stockPrice.setOpen(latest.getOpen());
        stockPrice.setHigh(latest.getHigh());
        stockPrice.setLow(latest.getLow());
        stockPrice.setClose(latest.getClose());

        // Helper: safely get previous OHLCV by backtrack index
        BiFunction<List<OHLCV>, Integer, OHLCV> getPrev =
                (list, n) -> {
                    int idx = size - 1 - n;
                    return (idx >= 0) ? list.get(idx) : null;
                };

        // prev (n=1)
        OHLCV p1 = getPrev.apply(ohlcvList, 1);
        if (p1 != null) {

            stockPrice.setPrevOpen(p1.getOpen());
            stockPrice.setPrevHigh(p1.getHigh());
            stockPrice.setPrevLow(p1.getLow());
            stockPrice.setPrevClose(p1.getClose());
        } else {
            stockPrice.setPrevOpen(0.0);
            stockPrice.setPrevHigh(0.0);
            stockPrice.setPrevLow(0.0);
            stockPrice.setPrevClose(0.0);
        }

        // prev2 (n=2)
        OHLCV p2 = getPrev.apply(ohlcvList, 2);
        if (p2 != null) {

            stockPrice.setPrev2Open(p2.getOpen());
            stockPrice.setPrev2High(p2.getHigh());
            stockPrice.setPrev2Low(p2.getLow());
            stockPrice.setPrev2Close(p2.getClose());
        } else {
            stockPrice.setPrev2Open(0.0);
            stockPrice.setPrev2High(0.0);
            stockPrice.setPrev2Low(0.0);
            stockPrice.setPrev2Close(0.0);
        }

        // prev3 (n=3)
        OHLCV p3 = getPrev.apply(ohlcvList, 3);
        if (p3 != null) {

            stockPrice.setPrev3Open(p3.getOpen());
            stockPrice.setPrev3High(p3.getHigh());
            stockPrice.setPrev3Low(p3.getLow());
            stockPrice.setPrev3Close(p3.getClose());
        } else {
            stockPrice.setPrev3Open(0.0);
            stockPrice.setPrev3High(0.0);
            stockPrice.setPrev3Low(0.0);
            stockPrice.setPrev3Close(0.0);
        }

        // prev4 (n=4)
        OHLCV p4 = getPrev.apply(ohlcvList, 4);
        if (p4 != null) {

            stockPrice.setPrev4Open(p4.getOpen());
            stockPrice.setPrev4High(p4.getHigh());
            stockPrice.setPrev4Low(p4.getLow());
            stockPrice.setPrev4Close(p4.getClose());
        } else {
            stockPrice.setPrev4Open(0.0);
            stockPrice.setPrev4High(0.0);
            stockPrice.setPrev4Low(0.0);
            stockPrice.setPrev4Close(0.0);
        }

        // prev5 (n=5)
        OHLCV p5 = getPrev.apply(ohlcvList, 5);
        if (p5 != null) {

            stockPrice.setPrev5Open(p5.getOpen());
            stockPrice.setPrev5High(p5.getHigh());
            stockPrice.setPrev5Low(p5.getLow());
            stockPrice.setPrev5Close(p5.getClose());
        } else {
            stockPrice.setPrev5Open(0.0);
            stockPrice.setPrev5High(0.0);
            stockPrice.setPrev5Low(0.0);
            stockPrice.setPrev5Close(0.0);
        }

        // prev6 (n=6)
        OHLCV p6 = getPrev.apply(ohlcvList, 6);
        if (p6 != null) {

            stockPrice.setPrev6Open(p6.getOpen());
            stockPrice.setPrev6High(p6.getHigh());
            stockPrice.setPrev6Low(p6.getLow());
            stockPrice.setPrev6Close(p6.getClose());
        } else {
            stockPrice.setPrev6Open(0.0);
            stockPrice.setPrev6High(0.0);
            stockPrice.setPrev6Low(0.0);
            stockPrice.setPrev6Close(0.0);
        }

        // System.out.println(stockPrice);

        return stockPrice;
    }
}
