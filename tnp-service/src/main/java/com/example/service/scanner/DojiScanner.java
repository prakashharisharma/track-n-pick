package com.example.service.scanner;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.io.StockAnalysis;
import com.example.service.CalendarService;
import com.example.service.UpdatePriceService;
import com.example.service.UpdateTechnicalsService;
import com.example.service.scanner.validator.DailyEntryValidator;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DojiScanner {

    private final MiscUtil miscUtil;
    private final CalendarService calendarService;

    private final FormulaService formulaService;

    private final DailyEntryValidator dailyEntryValidator;

    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;

    public boolean isValid(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (CandleStickUtils.isVerySmallBody(stockPrice)) {

            boolean isGreenWithNoUpperWick =
                    CandleStickUtils.isGreen(stockPrice)
                            && CandleStickUtils.upperWickSize(stockPrice)
                                    <= CandleStickUtils.lowerWickSize(stockPrice);

            if (CandleStickUtils.isRed(stockPrice) || isGreenWithNoUpperWick) {

                double ema5 = stockTechnicals.getEma5();
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();

                double close = stockPrice.getClose();
                double open = stockPrice.getOpen();
                double high = stockPrice.getHigh();

                boolean isHighRejected = ema5 > ema20 && close < ema5 && high > ema5 && open < ema5;

                if (!isHighRejected) {
                    boolean isEma5Decreasing = ema5 < stockTechnicals.getPrevEma5();
                    boolean isEma20Decreasing = ema20 < stockTechnicals.getPrevEma20();

                    boolean isEma5And20Decreasing = isEma5Decreasing && isEma20Decreasing;

                    if (!isEma5And20Decreasing) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<StockAnalysis> scan(
            StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate currentDate) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();
        /*
        LocalDate sessionDate =
                calendarService.previousTradingSession(miscUtil.currentDate().withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = miscUtil.currentDate();
        }*/

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        // if (this.isMonthlySatisfied(stockPrice, stockTechnicals, true)) {
        // double close = stockPrice.getClose();
        if (CandleStickUtils.isVerySmallBody(stockPrice)) {

            boolean isGreenWithNoUpperWick =
                    CandleStickUtils.isGreen(stockPrice)
                            && CandleStickUtils.upperWickSize(stockPrice)
                                    <= CandleStickUtils.lowerWickSize(stockPrice);

            if (CandleStickUtils.isRed(stockPrice) || isGreenWithNoUpperWick) {

                double ema5 = stockTechnicals.getEma5();
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();

                double close = stockPrice.getClose();
                double open = stockPrice.getOpen();
                double high = stockPrice.getHigh();

                boolean isHighRejected = ema5 > ema20 && close < ema5 && high > ema5 && open < ema5;

                if (!isHighRejected) {
                    boolean isEma5Decreasing = ema5 < stockTechnicals.getPrevEma5();
                    boolean isEma20Decreasing = ema20 < stockTechnicals.getPrevEma20();

                    boolean isEma5And20Decreasing = isEma5Decreasing && isEma20Decreasing;

                    if (!isEma5And20Decreasing) {

                        // Daily check

                        // Daily check
                        LocalDate previousMonthLastDay =
                                calendarService.previousTradingSession(
                                        currentDate.withDayOfMonth(1));

                        LocalDate currentMonthFirstSession =
                                calendarService.nextTradingSession(previousMonthLastDay);

                        LocalDate currentMonthSecondSession =
                                calendarService.nextTradingSession(currentMonthFirstSession);

                        LocalDate currentMonthThirdSession =
                                calendarService.nextTradingSession(currentMonthSecondSession);

                        LocalDate currentMonthForthSession =
                                calendarService.nextTradingSession(currentMonthSecondSession);

                        LocalDate currentMonthFifthSession =
                                calendarService.nextTradingSession(currentMonthSecondSession);

                        LocalDate currentMonthSixthSession =
                                calendarService.nextTradingSession(currentMonthSecondSession);
                        LocalDate sessionDate = currentMonthFirstSession;
                        LocalDate sessionDateTill = currentMonthThirdSession;
                        LocalDate ohlcvFrom = currentMonthFirstSession;

                        while (sessionDate.isBefore(sessionDateTill)) {
                            // StockPrice stockPriceDaily = getStockPriceFromMap(Timeframe.DAILY,
                            // stock, sessionDate);
                            StockPrice stockPriceDaily =
                                    updatePriceService.buildBack(
                                            Timeframe.DAILY, stock, sessionDate);
                            // StockTechnicals stockTechnicalsDaily =
                            // getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);
                            StockTechnicals stockTechnicalsDaily =
                                    updateTechnicalsService.buildBack(
                                            Timeframe.DAILY, stock, sessionDate);
                            if (CandleStickUtils.isGreen(stockPriceDaily)) {

                                boolean isPrevGreen =
                                        CandleStickUtils.isPrevSessionGreen(stockPriceDaily);
                                boolean isTweezerBottom =
                                        !isPrevGreen
                                                && stockPriceDaily.getPrevClose()
                                                        == stockPriceDaily.getOpen();
                                boolean isEngulfing =
                                        !isPrevGreen
                                                && stockPriceDaily.getOpen()
                                                        < stockPriceDaily.getPrevClose()
                                                && stockPriceDaily.getClose()
                                                        > stockPriceDaily.getPrevOpen();
                                if (isPrevGreen || isTweezerBottom || isEngulfing) {
                                    if (stockPriceDaily.getClose()
                                            > stockPriceDaily.getPrevClose()) {

                                        Optional<StockAnalysis> stockAnalysisOptional =
                                                dailyEntryValidator.isValid2(
                                                        stockPrice,
                                                        stockPriceDaily,
                                                        stockTechnicals,
                                                        stockTechnicalsDaily,
                                                        sessionDate,
                                                        ResearchTechnical.Strategy.DOJI);
                                        if (stockAnalysisOptional.isPresent()) {
                                            // System.out.println("HereN2 : " +
                                            // stock.getNseSymbol());
                                            stockAnalysed.add(stockAnalysisOptional.get());
                                            break;
                                        }
                                    }
                                }
                            }
                            sessionDate = calendarService.nextTradingSession(sessionDate);
                        }
                    }
                }
            }
            // }
        }

        return stockAnalysed;
    }

    public List<StockAnalysis> scanLastDay(
            StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate currentDate) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        LocalDate sessionDate =
                calendarService.previousTradingSession(currentDate.withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(currentDate)
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = currentDate;
        }

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        // if (this.isMonthlySatisfied(stockPrice, stockTechnicals, true)) {
        // double close = stockPrice.getClose();
        if (CandleStickUtils.isVerySmallBody(stockPrice)) {

            boolean isGreenWithNoUpperWick =
                    CandleStickUtils.isGreen(stockPrice)
                            && CandleStickUtils.upperWickSize(stockPrice)
                                    <= CandleStickUtils.lowerWickSize(stockPrice);

            if (CandleStickUtils.isRed(stockPrice) || isGreenWithNoUpperWick) {

                double ema5 = stockTechnicals.getEma5();
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();

                double close = stockPrice.getClose();
                double open = stockPrice.getOpen();
                double high = stockPrice.getHigh();

                boolean isHighRejected = ema5 > ema20 && close < ema5 && high > ema5 && open < ema5;

                if (!isHighRejected) {

                    boolean isEma5Decreasing = ema5 < stockTechnicals.getPrevEma5();
                    boolean isEma20Decreasing = ema20 < stockTechnicals.getPrevEma20();

                    boolean isEma5And20Decreasing = isEma5Decreasing && isEma20Decreasing;

                    if (!isEma5And20Decreasing) {

                        // Daily check

                        // StockPrice stockPriceDaily = getStockPriceFromMap(Timeframe.DAILY, stock,
                        // sessionDate);
                        StockPrice stockPriceDaily =
                                updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
                        // StockTechnicals stockTechnicalsDaily =
                        // getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);
                        StockTechnicals stockTechnicalsDaily =
                                updateTechnicalsService.buildBack(
                                        Timeframe.DAILY, stock, sessionDate);

                        if (CandleStickUtils.isGreen(stockPriceDaily)) {

                            boolean isPrevGreen =
                                    CandleStickUtils.isPrevSessionGreen(stockPriceDaily);
                            boolean isTweezerBottom =
                                    !isPrevGreen
                                            && stockPriceDaily.getPrevClose()
                                                    == stockPriceDaily.getOpen();
                            boolean isEngulfing =
                                    !isPrevGreen
                                            && stockPriceDaily.getOpen()
                                                    < stockPriceDaily.getPrevClose()
                                            && stockPriceDaily.getClose()
                                                    > stockPriceDaily.getPrevOpen();
                            if (isPrevGreen || isTweezerBottom || isEngulfing) {
                                if (stockPriceDaily.getClose() > stockPriceDaily.getPrevClose()) {

                                    Optional<StockAnalysis> stockAnalysisOptional =
                                            dailyEntryValidator.isValid2(
                                                    stockPrice,
                                                    stockPriceDaily,
                                                    stockTechnicals,
                                                    stockTechnicalsDaily,
                                                    sessionDate,
                                                    ResearchTechnical.Strategy.DOJI);
                                    if (stockAnalysisOptional.isPresent()) {
                                        // System.out.println("HereN2 : " + stock.getNseSymbol());
                                        stockAnalysed.add(stockAnalysisOptional.get());
                                    }
                                }
                            }
                        }
                    }
                    //  }
                }
            }
        }

        return stockAnalysed;
    }
}
