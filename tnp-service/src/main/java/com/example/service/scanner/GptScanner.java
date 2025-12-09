package com.example.service.scanner;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Sector;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.utils.CandleStickUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptScanner {

    private final StockService stockService;
    private final StockPriceService stockPriceService;

    private final UpdatePriceService updatePriceService;
    private final StockTechnicalsService stockTechnicalsService;

    private final UpdateTechnicalsService updateTechnicalsService;

    private final CalendarService calendarService;
    private final FundamentalResearchService fundamentalResearchService;

    public void scan() {

        List<Stock> stocks = stockService.getActiveStocks();
        // List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();
        List<String> debugDates = new ArrayList<>();

        LocalDate startMonth = LocalDate.of(2025, 11, 1);
        LocalDate endMonth = LocalDate.of(2025, 11, 25);

        Set<String> portfolio = new HashSet<>();

        for (Stock stock : stocks) {

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // ============================================
            // MONTH LOOP
            // ============================================
            for (LocalDate monthCursor = startMonth;
                    !monthCursor.isAfter(endMonth);
                    monthCursor = monthCursor.plusMonths(1)) {

                // Last trading session of PREVIOUS month
                LocalDate monthDate = calendarService.previousTradingSession(monthCursor);

                debugDates.add(stock.getNseSymbol() + " monthDate: " + monthDate);

                StockPrice monthlyPrice =
                        updatePriceService.buildBack(Timeframe.MONTHLY, stock, monthDate);
                StockTechnicals monthlyTech =
                        updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, monthDate);

                if (!isInititalValidated(monthlyPrice, monthlyTech)) {
                    continue;
                }

                boolean monthlyOK =
                        valid(monthlyPrice)
                                && valid(monthlyTech)
                                && monthlyScan(monthlyPrice, monthlyTech);

                // ============================================
                // WEEK LOOP inside this month
                // ============================================

                // Monday after monthDate
                LocalDate firstDayOfWeek =
                        monthDate.plusDays(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

                // First weekly cursor = Friday of that week
                LocalDate weekCursor = calendarService.previousTradingSession(firstDayOfWeek);

                // End of this month boundary
                LocalDate nextMonthStart = monthCursor.plusMonths(1);

                while (!weekCursor.isAfter(nextMonthStart) && !weekCursor.isAfter(endMonth)) {

                    debugDates.add(stock.getNseSymbol() + " weekCursor: " + weekCursor);

                    StockPrice weeklyPrice =
                            updatePriceService.buildBack(Timeframe.WEEKLY, stock, weekCursor);
                    StockTechnicals weeklyTech =
                            updateTechnicalsService.buildBack(Timeframe.WEEKLY, stock, weekCursor);

                    boolean weeklyOK =
                            valid(weeklyPrice)
                                    && valid(weeklyTech)
                                    && weeklyScan(weeklyPrice, weeklyTech);

                    // ============================================
                    // DAILY LOOP inside this week
                    // ============================================

                    LocalDate sessionDate = calendarService.nextTradingSession(weekCursor);
                    LocalDate endOfWeek = weekCursor.plusDays(7);

                    while (!sessionDate.isAfter(endOfWeek) && !sessionDate.isAfter(endMonth)) {

                        debugDates.add(stock.getNseSymbol() + " sessionDate: " + sessionDate);

                        StockPrice dailyPrice =
                                updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
                        StockTechnicals dailyTech =
                                updateTechnicalsService.buildBack(
                                        Timeframe.DAILY, stock, sessionDate);

                        // ----------------------------------------
                        // ENTRY (Monthly + Weekly + Daily)
                        // ----------------------------------------
                        if (monthlyOK && weeklyOK) {
                            if (valid(dailyPrice) && valid(dailyTech)) {
                                boolean entry =
                                        dailyScan(
                                                monthlyPrice, monthlyTech,
                                                dailyPrice, dailyTech);

                                if (entry && !portfolio.contains(stock.getNseSymbol())) {
                                    results.add(
                                            stock.getNseSymbol()
                                                    + " ENTRY on "
                                                    + sessionDate
                                                    + " at "
                                                    + dailyPrice.getClose());
                                    portfolio.add(stock.getNseSymbol());
                                    System.out.println(
                                            stock.getNseSymbol()
                                                    + " here "
                                                    + sessionDate
                                                    + " "
                                                    + dailyPrice.getClose());
                                }
                            }
                        }

                        // ----------------------------------------
                        // EXIT ALWAYS RUNS
                        // ----------------------------------------
                        if (portfolio.contains(stock.getNseSymbol())) {

                            // MONTHLY EXIT
                            if (monthlyExit(monthlyPrice, monthlyTech, dailyPrice, dailyTech)) {
                                results.add(
                                        stock.getNseSymbol()
                                                + " MONTHLY EXIT on "
                                                + sessionDate
                                                + " at "
                                                + dailyPrice.getClose());
                                portfolio.remove(stock.getNseSymbol());
                                sessionDate = calendarService.nextTradingSession(sessionDate);
                                continue;
                            }

                            // WEEKLY EXIT
                            if (weeklyExit(weeklyPrice, weeklyTech, dailyPrice, dailyTech)) {
                                results.add(
                                        stock.getNseSymbol()
                                                + " WEEKLY EXIT on "
                                                + sessionDate
                                                + " at "
                                                + dailyPrice.getClose());
                                portfolio.remove(stock.getNseSymbol());
                                sessionDate = calendarService.nextTradingSession(sessionDate);
                                continue;
                            }

                            // DAILY EXIT
                            if (dailyExit(dailyPrice, dailyTech)) {
                                results.add(
                                        stock.getNseSymbol()
                                                + " DAILY EXIT on "
                                                + sessionDate
                                                + " at "
                                                + dailyPrice.getClose());
                                portfolio.remove(stock.getNseSymbol());
                                sessionDate = calendarService.nextTradingSession(sessionDate);
                                continue;
                            }
                        }

                        sessionDate = calendarService.nextTradingSession(sessionDate);
                    }

                    // Move to next week
                    weekCursor = weekCursor.plusWeeks(1);
                }
            }
        }

        // PRINT RESULTS
        results.forEach(System.out::println);
        // debugDates.forEach(System.out::println);
    }

    /** A. Close > Previous Month High B. Monthly RSI > 50 C. Monthly EMA10 sloping up */
    public boolean monthlyScan(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice.getClose() > stockPrice.getPrevHigh()) {
            if (stockTechnicals.getRsi() > 50.0) {
                if (stockTechnicals.getEma10() > stockTechnicals.getPrevEma10()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Weekly Close > Weekly EMA20 Weekly RSI > 55 Weekly ADX > 15 (trend strength)
     *
     * @param stockPrice
     * @param stockTechnicals
     * @return
     */
    public boolean weeklyScan(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice.getClose() > stockTechnicals.getEma20()) {
            if (stockTechnicals.getRsi() > 55.0) {
                if (stockTechnicals.getAdx() > 10.0
                        && stockTechnicals.getAdx() > stockTechnicals.getPrevAdx()) {
                    System.out.println(
                            stockPrice.getStock().getNseSymbol()
                                    + " Found: "
                                    + stockPrice.getSessionDate());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Daily price retests previous month high OR EMA10 Daily bullish candle Volume > 1.5× 20-day
     * average RSI(14) > 55 Structure: Higher High – Higher Low
     *
     * @return
     */
    public boolean dailyScan(
            StockPrice monthlyStockPrice,
            StockTechnicals monthlyStockTechnicals,
            StockPrice dailyStockPrice,
            StockTechnicals dailyStockTechnicals) {

        double monthlyHigh = monthlyStockPrice.getHigh();
        double monthlyEma10 = monthlyStockTechnicals.getEma10();

        // ---- Monthly High retest ----
        boolean isMonthlyHighRetested =
                (dailyStockPrice.getLow() <= monthlyHigh)
                        && dailyStockPrice.getClose() > monthlyHigh;

        // ---- Monthly High direct breakout ----
        boolean isMonthlyHighBreakout =
                dailyStockPrice.getOpen() > monthlyHigh && dailyStockPrice.getClose() > monthlyHigh;

        // ---- Monthly EMA10 retest ----
        boolean isMonthlyEma10Retested =
                (dailyStockPrice.getLow() <= monthlyEma10)
                        && dailyStockPrice.getClose() > monthlyEma10;

        // ---- Monthly EMA10 direct breakout ----
        boolean isMonthlyEma10Breakout =
                dailyStockPrice.getOpen() > monthlyEma10
                        && dailyStockPrice.getClose() > monthlyEma10;

        boolean isSetupTriggered =
                isMonthlyHighRetested
                        || isMonthlyHighBreakout
                        || isMonthlyEma10Retested
                        || isMonthlyEma10Breakout;

        if (isSetupTriggered) {
            if (CandleStickUtils.isGreen(dailyStockPrice)) {
                if (dailyStockTechnicals.getVolume()
                        > 1.5 * dailyStockTechnicals.getVolumeAvg20()) {
                    if (dailyStockTechnicals.getRsi() > 50.0) {
                        if (CandleStickUtils.isHigherHigh(dailyStockPrice)
                                && CandleStickUtils.isHigherLow(dailyStockPrice)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean monthlyExit(
            StockPrice monthlyStockPrice,
            StockTechnicals monthlyStockTechnicals,
            StockPrice dailyStockPrice,
            StockTechnicals dailyStockTechnicals) {

        if (dailyStockPrice.getClose() < monthlyStockPrice.getLow()) {
            return true;
        }
        if (monthlyStockPrice.getClose() < monthlyStockPrice.getPrevLow()
                && monthlyStockPrice.getClose() < monthlyStockTechnicals.getEma5()) {
            return true;
        }

        return false;
    }

    public boolean weeklyExit(
            StockPrice weeklyStockPrice,
            StockTechnicals weeklyStockTechnicals,
            StockPrice dailyStockPrice,
            StockTechnicals dailyStockTechnicals) {

        if (dailyStockPrice.getClose() < weeklyStockTechnicals.getEma20()) {
            return true;
        }

        return false;
    }

    public boolean dailyExit(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (CandleStickUtils.isRed(stockPrice) && CandleStickUtils.isPrevSessionRed(stockPrice)) {
            if (stockPrice.getClose() < stockTechnicals.getEma20()) {
                return true;
            }
        }
        if (CandleStickUtils.isPrevUpperWickDominant(stockPrice)
                && CandleStickUtils.isRed(stockPrice)) {
            if (stockPrice.getClose() < stockPrice.getPrevClose()) {
                return true;
            }
            if (stockPrice.getClose() < stockTechnicals.getEma5()) {
                return true;
            }
        }

        return false;
    }

    private boolean valid(StockPrice stockPrice) {
        if (stockPrice != null) {
            return true;
        }
        return false;
    }

    private boolean valid(StockTechnicals stockTechnicals) {
        if (stockTechnicals != null) {
            return true;
        }
        return false;
    }

    private boolean isInititalValidated(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        Stock stock = stockPrice.getStock();

        boolean isEqOrBE = stock.getSeries().equalsIgnoreCase("EQ");

        if (!isEqOrBE) {
            return false;
        }

        if (stock.getSector() == null || stock.getSector().getType() == Sector.Type.ETF) {
            return false;
        }

        if (!fundamentalResearchService.isPriceInRange(stockPrice)) {
            return false;
        }

        if (stockTechnicals.getVolumeAvg10() < 10_00_000) {
            return false;
        }

        return true;
    }
}
