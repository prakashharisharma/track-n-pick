package com.example.service.scanner;

import static com.example.data.common.type.Timeframe.*;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestScanner {

    private final CalendarService calendarService;

    private final MASupportChecker maSupportChecker;
    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;

    private final WeeklyLevelScannerService scannerService;

    private final MiscUtil miscUtil;

    private final StockPriceService stockPriceService;

    private final FormulaService formulaService;
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;

    private final FundamentalResearchService fundamentalResearchService;

    private final StockService stockService;

    private final CandleStickService candleStickService;

    public void candleStickScanner() {
        List<Stock> stocks = stockService.getActiveStocks();

        List<String> results = new ArrayList<>();
        // sessionDateWeekly + ", " + stock.getNseSymbol() + ", " + marketCapCategory + " ," +
        // stockPrice.getClose() + " ," + entry + " ," + stopLoss + " ," + currPer
        results.add(
                "sessionDate"
                        + ", "
                        + "symbol"
                        + ", "
                        + "pattern"
                        + ", "
                        + "mcap"
                        + ", "
                        + "Close"
                        + ", "
                        + "entry"
                        + ", "
                        + "stoploss"
                        + ", "
                        + "currPr"
                        + " ,"
                        + "ema5Weekly"
                        + " ,"
                        + "ema10Weekly"
                        + " ,"
                        + "ema20Weekly"
                        + " ,"
                        + "ema50Weekl"
                        + " ,"
                        + "ema100Weekly"
                        + " ,"
                        + "ema200Weekly");
        List<LocalDate> sessionDates = new ArrayList<>();
        // sessionDates.add(LocalDate.of(2025, 8, 31));
        sessionDates.add(LocalDate.of(2025, 9, 30));
        sessionDates.add(LocalDate.of(2025, 10, 31));
        sessionDates.add(LocalDate.of(2025, 11, 30));
        // sessionDates.add(LocalDate.of(2025, 11, 30));

        for (LocalDate sessionDate : sessionDates) {

            for (Stock stock : stocks) {

                if (!this.isInititalValidated(stock)) {
                    continue;
                }

                StockPrice stockPrice = updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                MarketCapCategory marketCapCategory =
                        MarketCapCategory.classify(
                                fundamentalResearchService.marketCap(stockPrice));

                if (!this.isInititalValidated(stockPrice)) {
                    continue;
                }

                if (!this.isInititalValidated(stockTechnicals)) {
                    continue;
                }

                if (stockTechnicals.getEma20() == 0.0) {
                    continue;
                }

                double close = stockPrice.getClose();
                double prevClose = stockPrice.getPrevClose();
                double low = stockPrice.getLow();

                // double ema5 = stockTechnicals.getEma5();
                double ema10 = stockTechnicals.getEma10();
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();
                double ema100 =
                        MovingAverageUtil.getMovingAverage100(
                                stockTechnicals.getTimeframe(), stockTechnicals);
                double ema200 =
                        MovingAverageUtil.getMovingAverage200(
                                stockTechnicals.getTimeframe(), stockTechnicals);
                double prevEma200 =
                        MovingAverageUtil.getPrevMovingAverage200(
                                stockTechnicals.getTimeframe(), stockTechnicals);

                boolean isDownTrend =
                        stockPrice.getPrevClose() < stockPrice.getPrev2Close()
                                && stockPrice.getPrev2Close() < stockPrice.getPrev3Close();

                boolean isPrevDownTrend =
                        stockPrice.getPrev2Close() < stockPrice.getPrev3Close()
                                && stockPrice.getPrev3Close() < stockPrice.getPrev4Close();

                boolean isEma10Support =
                        low <= ema10
                                && close > Math.floor(ema10)
                                && ema10 > ema20
                                && ema20 >= ema50
                                && ema50 >= ema100
                                && ema100 >= ema200;
                ;
                boolean isEma20Support =
                        low <= ema20
                                && close > Math.floor(ema20)
                                && ema20 > ema50
                                && ema50 >= ema100
                                && ema100 >= ema200;
                boolean isEma50Support =
                        low <= ema50
                                && close > Math.floor(ema50)
                                && ema50 > ema100
                                && ema100 >= ema200;
                boolean isEma100Support =
                        low <= ema100 && close > Math.floor(ema100) && ema100 >= ema200;
                boolean isEma200Support =
                        low <= ema200 && close > Math.floor(ema200) && ema200 >= prevEma200;

                boolean isSupportBounce =
                        isEma10Support
                                || isEma20Support
                                || isEma50Support
                                || isEma100Support
                                || isEma200Support;

                System.out.println(stock.getNseSymbol() + " isSupportBounce " + isSupportBounce);
                System.out.println(stock.getNseSymbol() + " isDownTrend " + isDownTrend);

                boolean isBullishEngulfing = candleStickService.isBullishEngulfing(stockPrice);
                boolean isPiercingPAttern = candleStickService.isPiercingPattern(stockPrice);
                boolean isTweezerBottom = candleStickService.isTweezerBottom(stockPrice);
                boolean isDoubleBottom = candleStickService.isDoubleBottom(stockPrice);

                // Confirmation Needed
                boolean isHammer = candleStickService.isPrevHammer(stockPrice) && close > prevClose;
                boolean isInvertedHammer =
                        candleStickService.isPrevInvertedHammer(stockPrice) && close > prevClose;
                boolean isBullishHarami =
                        candleStickService.isPrevBullishHarami(stockPrice) && close > prevClose;
                boolean isDoji = candleStickService.isPrevDoji(stockPrice) && close > prevClose;
                boolean isSpinningTop =
                        candleStickService.isPrevSpinningTop(stockPrice) && close > prevClose;

                boolean isCandleStickPattern =
                        isBullishEngulfing
                                || isPiercingPAttern
                                || isBullishHarami
                                || isTweezerBottom
                                || isDoubleBottom;

                boolean isPrevCandleStickPattern =
                        isHammer || isInvertedHammer || isDoji || isSpinningTop;

                double monthlyChngPct =
                        formulaService.calculateChangePercentage(
                                stockPrice.getPrevClose(), stockPrice.getClose());

                if ((isDownTrend && isCandleStickPattern)
                        || (isPrevDownTrend
                                && isPrevCandleStickPattern
                                && monthlyChngPct < 10.0
                                && monthlyChngPct > 3.0
                                && !candleStickService.isDoji(stockPrice)
                                && !candleStickService.isSpinningTop(stockPrice))) {
                    System.out.println(stock.getNseSymbol() + " Found1");
                    LocalDate sessionDateWeekly = sessionDate.plusWeeks(1);
                    StockPrice stockPriceWeekly =
                            updatePriceService.buildBack(
                                    Timeframe.WEEKLY, stock, sessionDateWeekly);
                    StockTechnicals stockTechnicalsWeekly =
                            updateTechnicalsService.buildBack(
                                    Timeframe.WEEKLY, stock, sessionDateWeekly);
                    double closeWeekly = stockPriceWeekly.getClose();
                    double lowWeekly = stockPriceWeekly.getLow();

                    double weeklyChngPct =
                            formulaService.calculateChangePercentage(
                                    stockPriceWeekly.getPrevClose(), stockPriceWeekly.getClose());

                    boolean isWeeklyBreakout =
                            isPrevCandleStickPattern
                                    ? true
                                    : closeWeekly > close && weeklyChngPct < 6 && weeklyChngPct > 2;

                    if (isWeeklyBreakout) {
                        String pattern = "NA";
                        if (isDoji) {
                            pattern = "Doji";
                        } else if (isSpinningTop) {
                            pattern = "Spining Top";
                        } else if (isHammer) {
                            pattern = "Hammer";
                        } else if (isInvertedHammer) {
                            pattern = "Inverted Hammer";
                        } else if (isBullishHarami) {
                            pattern = "Harami";
                        } else if (isBullishEngulfing) {
                            pattern = "Engulfing";
                        } else if (isPiercingPAttern) {
                            pattern = "Piercing";
                        } else if (isTweezerBottom) {
                            pattern = "Tweezer";
                        } else if (isDoubleBottom) {
                            pattern = "Double Bottom";
                        }
                        System.out.println(stock.getNseSymbol() + " Found2");
                        double entry =
                                isPrevCandleStickPattern
                                        ? stockPrice.getClose()
                                        : stockPriceWeekly.getClose();
                        double prevLowWeekly = stockPriceWeekly.getPrevLow();
                        double prev2LowWeekly = stockPriceWeekly.getPrev2Low();
                        double prev3LowWeekly = stockPriceWeekly.getPrev3Low();
                        double prev4LowWeekly = stockPriceWeekly.getPrev4Low();
                        double stopLoss =
                                Math.min(
                                        Math.min(
                                                Math.min(lowWeekly, prevLowWeekly),
                                                Math.min(prev2LowWeekly, prev3LowWeekly)),
                                        prev4LowWeekly);

                        StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);
                        double currPer =
                                formulaService.calculateChangePercentage(
                                        entry, stockPriceDaily.getClose());
                        double chngPct =
                                formulaService.calculateChangePercentage(
                                        stockPrice.getPrevClose(), stockPrice.getClose());

                        double ema5Weekly = stockTechnicalsWeekly.getEma5();
                        double ema10Weekly = stockTechnicalsWeekly.getEma10();
                        double ema20Weekly = stockTechnicalsWeekly.getEma20();
                        double ema50Weekly = stockTechnicalsWeekly.getEma50();
                        double ema100Weekly =
                                MovingAverageUtil.getMovingAverage100(
                                        stockTechnicalsWeekly.getTimeframe(),
                                        stockTechnicalsWeekly);
                        double ema200Weekly =
                                MovingAverageUtil.getMovingAverage200(
                                        stockTechnicalsWeekly.getTimeframe(),
                                        stockTechnicalsWeekly);

                        System.out.println(
                                sessionDateWeekly
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + pattern
                                        + ", "
                                        + marketCapCategory
                                        + " ,"
                                        + stockPrice.getClose()
                                        + " ,"
                                        + entry
                                        + " ,"
                                        + stopLoss
                                        + " ,"
                                        + currPer);
                        results.add(
                                sessionDateWeekly
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + pattern
                                        + ", "
                                        + marketCapCategory
                                        + " ,"
                                        + stockPrice.getClose()
                                        + " ,"
                                        + entry
                                        + " ,"
                                        + stopLoss
                                        + " ,"
                                        + currPer
                                        + " ,"
                                        + ema5Weekly
                                        + " ,"
                                        + ema10Weekly
                                        + " ,"
                                        + ema20Weekly
                                        + " ,"
                                        + ema50Weekly
                                        + " ,"
                                        + ema100Weekly
                                        + " ,"
                                        + ema200Weekly);
                    }
                }
            }
            // sessionDate = sessionDate.plusWeeks(1);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        results.forEach(System.out::println);
    }

    public void newScanner() {
        //  List<Stock> stocks = stockService.getActiveStocks();
        List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();
        Set<String> symbols = new HashSet<>();

        for (Stock stock : stocks) {
            LocalDate sessionDateMonthly = LocalDate.of(2025, 1, 31);
            LocalDate sessionDateCurrent = LocalDate.of(2025, 2, 28);
            //  LocalDate sessionDateMonthly =LocalDate.of(2025, 6, 30);
            // LocalDate sessionDateCurrent =LocalDate.of(2025, 7, 31);
            //   LocalDate sessionDateMonthly =LocalDate.of(2025, 7, 31);
            // LocalDate sessionDateCurrent =LocalDate.of(2025, 8, 29);
            //  LocalDate sessionDateMonthly =LocalDate.of(2025, 8, 29);
            // LocalDate sessionDateCurrent =LocalDate.of(2025, 9, 30);
            // LocalDate sessionDateMonthly =LocalDate.of(2025, 9, 30);
            // LocalDate sessionDateCurrent =LocalDate.of(2025, 10, 31);
            //  LocalDate sessionDateMonthly =LocalDate.of(2025, 10, 31);
            // LocalDate sessionDateCurrent =LocalDate.of(2025, 11, 28);
            // LocalDate sessionDateMonthly =LocalDate.of(2025, 11, 30);
            // LocalDate sessionDateCurrent =LocalDate.of(2025, 12, 24);
            StockPrice stockPrice =
                    updatePriceService.buildBack(MONTHLY, stock, sessionDateMonthly);
            StockTechnicals stockTechnicals =
                    updateTechnicalsService.buildBack(MONTHLY, stock, sessionDateMonthly);
            if (!this.isInititalValidated(stock)
                    || !this.isInititalValidated(stockPrice)
                    || !this.isInititalValidated(stockTechnicals)) {
                continue;
            }

            if (!(stockTechnicals.getEma20() >= stockTechnicals.getEma50()
                    && stockTechnicals.getEma50()
                            >= MovingAverageUtil.getMovingAverage200(MONTHLY, stockTechnicals))) {
                continue;
            }

            double mcap = fundamentalResearchService.marketCap(stockPrice);

            MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);
            if (marketCapCategory == MarketCapCategory.MICROCAP || mcap < 1000) {
                continue;
            }

            double monthlyChngPct =
                    formulaService.calculateChangePercentage(
                            stockPrice.getPrevClose(), stockPrice.getClose());

            boolean isLowReject =
                    stockPrice.getLow() < stockTechnicals.getEma5()
                            && stockPrice.getClose() > stockTechnicals.getEma5()
                            && stockPrice.getClose() > stockPrice.getPrevHigh();

            if (!(monthlyChngPct < 10 || isLowReject)) {
                continue;
            }

            if (CandleStickUtils.isUpperWickDominant(stockPrice)) {
                continue;
            }

            if (stockTechnicals.getRsi() > 70.0) {
                continue;
            }

            LocalDate sessionDate = calendarService.nextTradingSession(sessionDateMonthly);
            LocalDate sessionDate2 = calendarService.nextTradingSession(sessionDate);
            LocalDate sessionDate3 = calendarService.nextTradingSession(sessionDate2);
            LocalDate sessionDate4 = calendarService.nextTradingSession(sessionDate3);
            LocalDate sessionDate5 = calendarService.nextTradingSession(sessionDate4);
            LocalDate sessionDate6 = calendarService.nextTradingSession(sessionDate5);
            LocalDate sessionDate7 = calendarService.nextTradingSession(sessionDate6);
            LocalDate sessionDate8 = calendarService.nextTradingSession(sessionDate7);
            LocalDate sessionDate9 = calendarService.nextTradingSession(sessionDate8);
            LocalDate sessionDate10 = calendarService.nextTradingSession(sessionDate9);
            sessionDate = sessionDate2;

            StockPrice stockPriceFirstDay =
                    updatePriceService.buildBack(
                            Timeframe.DAILY,
                            stock,
                            calendarService.nextTradingSession(sessionDateMonthly));

            while (!sessionDate.isAfter(sessionDate10)) {

                StockPrice stockPriceDaily =
                        updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
                StockTechnicals stockTechnicalsDaily =
                        updateTechnicalsService.buildBack(Timeframe.DAILY, stock, sessionDate);

                boolean isCloseAbove =
                        stockPriceFirstDay.getClose() > stockPrice.getClose()
                                && CandleStickUtils.isGreen(stockPriceFirstDay);

                boolean isBullishHigh =
                        stockPrice.getClose() > stockPrice.getPrevHigh()
                                && stockTechnicals.getEma5() > stockTechnicals.getPrevEma5()
                                && CandleStickUtils.isGreen(stockPrice);

                if (CandleStickUtils.isGreen(stockPriceDaily)) {

                    if (isCloseAbove || isBullishHigh) {

                        boolean isBreakOutEma5 =
                                stockPriceDaily.getClose() > stockTechnicalsDaily.getEma5()
                                        && stockPriceDaily.getPrevClose()
                                                < stockTechnicalsDaily.getPrevEma5();
                        boolean isLowRejectedEma5 =
                                stockPriceDaily.getPrevClose() > stockTechnicalsDaily.getPrevEma5()
                                        && stockPriceDaily.getPrevLow()
                                                < stockTechnicalsDaily.getPrevEma5()
                                        && stockPriceDaily.getClose()
                                                > stockPriceDaily.getPrevHigh()
                                        && CandleStickUtils.isPrevSessionRed(stockPriceDaily);

                        boolean isBreakOutEma20 =
                                stockPriceDaily.getClose() > stockTechnicalsDaily.getEma20()
                                        && stockPriceDaily.getPrevClose()
                                                < stockTechnicalsDaily.getPrevEma20();
                        boolean isLowRejectedEma20 =
                                stockPriceDaily.getPrevClose() > stockTechnicalsDaily.getPrevEma20()
                                        && stockPriceDaily.getPrevLow()
                                                < stockTechnicalsDaily.getPrevEma20()
                                        && stockPriceDaily.getClose()
                                                > stockPriceDaily.getPrevHigh()
                                        && CandleStickUtils.isPrevSessionRed(stockPriceDaily);

                        boolean isBreakOutEma50 =
                                stockPriceDaily.getClose() > stockTechnicalsDaily.getEma50()
                                        && stockPriceDaily.getPrevClose()
                                                < stockTechnicalsDaily.getPrevEma50();
                        boolean isLowRejectedEma50 =
                                stockPriceDaily.getPrevClose() > stockTechnicalsDaily.getPrevEma50()
                                        && stockPriceDaily.getPrevLow()
                                                < stockTechnicalsDaily.getPrevEma50()
                                        && stockPriceDaily.getClose()
                                                > stockPriceDaily.getPrevHigh()
                                        && CandleStickUtils.isPrevSessionRed(stockPriceDaily);

                        boolean isBreakOutPrevMonthlyClose =
                                stockPriceDaily.getClose() > stockPrice.getClose()
                                        && stockPriceDaily.getPrevClose()
                                                < stockPrice.getPrevClose();
                        boolean isLowRejectedPrevMonthlyClose =
                                stockPriceDaily.getPrevClose() > stockPrice.getPrevClose()
                                        && stockPriceDaily.getPrevLow() < stockPrice.getPrevClose()
                                        && stockPriceDaily.getClose()
                                                > stockPriceDaily.getPrevHigh()
                                        && CandleStickUtils.isPrevSessionRed(stockPriceDaily);

                        if (isBreakOutEma5
                                || isLowRejectedEma5
                                || isBreakOutEma20
                                || isLowRejectedEma20
                                || isBreakOutEma50
                                || isLowRejectedEma50
                                || isBreakOutPrevMonthlyClose
                                || isLowRejectedPrevMonthlyClose) {
                            if (CandleStickUtils.isGreen(stockPriceDaily)) {

                                if (stockPriceDaily.getClose() > 100.0
                                        && stockPriceDaily.getClose() < 1000.0) {
                                    double entry =
                                            (stockPriceDaily.getOpen() + stockPriceDaily.getClose())
                                                    / 2;
                                    entry = formulaService.applyPercentChange(entry, 0.5);
                                    double ema5 = stockTechnicalsDaily.getEma5();
                                    double prevEma5 = stockTechnicalsDaily.getPrevEma5();
                                    double ema20 = stockTechnicalsDaily.getEma20();
                                    double ema50 = stockTechnicalsDaily.getEma50();
                                    double emA100 =
                                            MovingAverageUtil.getMovingAverage100(
                                                    Timeframe.DAILY, stockTechnicalsDaily);
                                    double emm200 =
                                            MovingAverageUtil.getMovingAverage200(
                                                    Timeframe.DAILY, stockTechnicalsDaily);
                                    double prevEma200 =
                                            MovingAverageUtil.getPrevMovingAverage200(
                                                    Timeframe.DAILY, stockTechnicalsDaily);
                                    if (ema5 > ema20 && ema20 > ema50) {
                                        entry =
                                                (stockPriceDaily.getClose()
                                                                + stockPriceDaily.getHigh())
                                                        / 2;
                                        entry = formulaService.applyPercentChange(entry, 0.75);
                                        entry = Math.min(entry, stockPriceDaily.getHigh());
                                    }
                                    if (ema5 < ema20 && ema20 < ema50) {
                                        entry =
                                                (stockPriceDaily.getOpen()
                                                                + stockPriceDaily.getLow())
                                                        / 2;
                                        entry = formulaService.applyPercentChange(entry, 0.75);
                                        entry = Math.min(entry, stockPriceDaily.getClose());
                                    }
                                    if (ema5 > prevEma5 && emm200 > prevEma200) {

                                        int increasingMaCount =
                                                MovingAverageUtil.increasingMaCount(
                                                        stockTechnicalsDaily);
                                        MovingAverageUtil.isAllMaAlignedBullish(
                                                Timeframe.DAILY, stockTechnicalsDaily);

                                        boolean allIncrAndAlign =
                                                increasingMaCount >= 5
                                                        && ema5 >= ema20
                                                        && ema20 >= ema50;

                                        boolean is3IncrAndAlign =
                                                increasingMaCount >= 3
                                                        && ema50 >= emm200
                                                        && emA100 >= emm200;

                                        if (allIncrAndAlign && is3IncrAndAlign) {

                                            boolean volSufficient =
                                                    stockTechnicalsDaily.getVolume() > 5_00_000
                                                            && stockTechnicalsDaily.getVolumeAvg20()
                                                                    > 10_00_000;

                                            if (volSufficient
                                                    && !CandleStickUtils.isUpperWickDominant(
                                                            stockPriceDaily)) {

                                                long vol2Days =
                                                        (stockTechnicalsDaily.getVolume()
                                                                        + stockTechnicalsDaily
                                                                                .getPrevVolume())
                                                                / 2;
                                                long volAvg2Days =
                                                        (stockTechnicalsDaily.getVolumeAvg20()
                                                                        + stockTechnicalsDaily
                                                                                .getPrevVolumeAvg20())
                                                                / 2;

                                                double chngPct =
                                                        formulaService.calculateChangePercentage(
                                                                stockPriceDaily.getPrevClose(),
                                                                stockPriceDaily.getClose());

                                                if (vol2Days >= volAvg2Days * 1.25
                                                        && chngPct < 7.5) {
                                                    if (!symbols.contains(stock.getNseSymbol())) {
                                                        StockPrice stockPriceCurrent =
                                                                updatePriceService.buildBack(
                                                                        Timeframe.DAILY,
                                                                        stock,
                                                                        sessionDateCurrent);
                                                        double gain =
                                                                formulaService
                                                                        .calculateChangePercentage(
                                                                                entry,
                                                                                stockPriceCurrent
                                                                                        .getClose());
                                                        double target =
                                                                formulaService.applyPercentChange(
                                                                        entry, 10.0);
                                                        String json =
                                                                stock.getNseSymbol()
                                                                        + ", "
                                                                        + sessionDate
                                                                        + ", "
                                                                        + entry
                                                                        + ", "
                                                                        + target
                                                                        + ", "
                                                                        + gain;
                                                        String result = "Close above " + json;
                                                        System.out.println(result);
                                                        results.add(json);
                                                        symbols.add(stock.getNseSymbol());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                sessionDate = calendarService.nextTradingSession(sessionDate);
            }
        }
        String headerJson =
                "symbol" + ", " + "sessionDate" + ", " + "entry" + ", " + "target" + ", " + "gain";
        System.out.println(headerJson);
        results.forEach(System.out::println);
    }

    public void newScanner2() {
        //  List<Stock> stocks = stockService.getActiveStocks();
        List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();
        Set<String> symbols = new HashSet<>();

        //  LocalDate sessionDateMonthly =LocalDate.of(2025, 1, 31);
        // LocalDate sessionDateCurrent =LocalDate.of(2025, 2, 28);
        //  LocalDate sessionDateMonthly =LocalDate.of(2025, 6, 30);
        // LocalDate sessionDateCurrent =LocalDate.of(2025, 7, 31);
        LocalDate sessionDateMonthly = LocalDate.of(2025, 7, 31);
        LocalDate sessionDateCurrent = LocalDate.of(2025, 8, 29);
        //   LocalDate sessionDateMonthly =LocalDate.of(2025, 8, 29);
        // LocalDate sessionDateCurrent =LocalDate.of(2025, 9, 30);
        // LocalDate sessionDateMonthly =LocalDate.of(2025, 9, 30);
        // LocalDate sessionDateCurrent =LocalDate.of(2025, 10, 31);
        //   LocalDate sessionDateMonthly =LocalDate.of(2025, 10, 31);
        // LocalDate sessionDateCurrent =LocalDate.of(2025, 11, 28);
        //  LocalDate sessionDateMonthly =LocalDate.of(2025, 11, 30);
        // LocalDate sessionDateCurrent =LocalDate.of(2025, 12, 24);
        for (Stock stock : stocks) {
            StockPrice stockPriceMonthly =
                    updatePriceService.buildBack(MONTHLY, stock, sessionDateMonthly);
            StockTechnicals stockTechnicalsMonthly =
                    updateTechnicalsService.buildBack(MONTHLY, stock, sessionDateMonthly);

            if (!this.isInititalValidated(stock)
                    || !this.isInititalValidated(stockPriceMonthly)
                    || !this.isInititalValidated(stockTechnicalsMonthly)) {
                continue;
            }

            double mcap = fundamentalResearchService.marketCap(stockPriceMonthly);

            MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);
            if (marketCapCategory == MarketCapCategory.MICROCAP || mcap < 5000) {
                continue;
            }

            double monthlyChngPct =
                    formulaService.calculateChangePercentage(
                            stockPriceMonthly.getPrevClose(), stockPriceMonthly.getClose());

            boolean isLowReject =
                    stockPriceMonthly.getLow() < stockTechnicalsMonthly.getEma5()
                            && stockPriceMonthly.getClose() > stockTechnicalsMonthly.getEma5();

            if (!(monthlyChngPct < 10 || (isLowReject && monthlyChngPct < 25))) {
                continue;
            }

            if (CandleStickUtils.isUpperWickDominant(stockPriceMonthly) && !isLowReject) {
                continue;
            }

            if (monthlyChngPct > 10 && stockTechnicalsMonthly.getRsi() > 65.0) {
                continue;
            }

            boolean isAlign =
                    (stockTechnicalsMonthly.getEma20() != 0
                                    && stockTechnicalsMonthly.getEma5()
                                            > stockTechnicalsMonthly.getEma20())
                            || (stockTechnicalsMonthly.getEma50() != 0
                                    && stockTechnicalsMonthly.getEma20()
                                            > stockTechnicalsMonthly.getEma50());

            if (!isAlign) {
                continue;
            }

            boolean isHaningMan =
                    candleStickService.isHangingMan(stockPriceMonthly)
                            && stockPriceMonthly.getLow() > stockTechnicalsMonthly.getEma5();

            if (isHaningMan) {
                continue;
            }

            boolean isHHHLAndRed =
                    CandleStickUtils.isRed(stockPriceMonthly)
                            && CandleStickUtils.isHigherHigh(stockPriceMonthly)
                            && CandleStickUtils.isHigherLow(stockPriceMonthly);

            if (isHHHLAndRed) {
                continue;
            }

            /*
            boolean isOpenHighAndRed = stockPriceMonthly.getOpen() > stockPriceMonthly.getPrevClose() && CandleStickUtils.isRed(stockPriceMonthly) && CandleStickUtils.isPrevSessionGreen(stockPriceMonthly);

            if(isOpenHighAndRed){
                continue;
            }*/

            boolean isAllMAIncreasingMonthly = this.isAllMAIncreasing(stockTechnicalsMonthly);

            // boolean isDoji = candleStickService.isDoji(stockPriceMonthly);
            boolean isPrevDoji =
                    candleStickService.isPrevDoji(stockPriceMonthly)
                            && CandleStickUtils.isRed(stockPriceMonthly);
            boolean isPre2vDoji =
                    candleStickService.isPrev2Doji(stockPriceMonthly)
                            && CandleStickUtils.isPrevSessionRed(stockPriceMonthly)
                            && CandleStickUtils.isRed(stockPriceMonthly);

            if (isPrevDoji || isPre2vDoji) {
                continue;
            }

            LocalDate sessionDate = calendarService.nextTradingSession(sessionDateMonthly);
            LocalDate sessionDate2 = calendarService.nextTradingSession(sessionDate);
            LocalDate sessionDate3 = calendarService.nextTradingSession(sessionDate2);
            LocalDate sessionDate4 = calendarService.nextTradingSession(sessionDate3);
            LocalDate sessionDate5 = calendarService.nextTradingSession(sessionDate4);
            LocalDate sessionDate6 = calendarService.nextTradingSession(sessionDate5);
            LocalDate sessionDate7 = calendarService.nextTradingSession(sessionDate6);
            LocalDate sessionDate8 = calendarService.nextTradingSession(sessionDate7);
            LocalDate sessionDate9 = calendarService.nextTradingSession(sessionDate8);
            LocalDate sessionDate10 = calendarService.nextTradingSession(sessionDate9);
            LocalDate sessionDate11 = calendarService.nextTradingSession(sessionDate10);
            LocalDate sessionDate12 = calendarService.nextTradingSession(sessionDate11);
            LocalDate sessionDate13 = calendarService.nextTradingSession(sessionDate12);
            LocalDate sessionDate14 = calendarService.nextTradingSession(sessionDate13);
            LocalDate sessionDate15 = calendarService.nextTradingSession(sessionDate14);

            while (!sessionDate.isAfter(sessionDate15)) {
                StockPrice stockPriceDaily =
                        updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
                StockTechnicals stockTechnicalsDaily =
                        updateTechnicalsService.buildBack(Timeframe.DAILY, stock, sessionDate);

                boolean isAllMAIncreasingDaily = this.isAllMAIncreasing(stockTechnicalsDaily);

                boolean isAllIncr = isAllMAIncreasingMonthly || isAllMAIncreasingDaily;

                if (isAllIncr
                        && stockPriceDaily.getClose() > 50.0
                        && stockPriceDaily.getClose() < 3000.0) {
                    double monthlyClose = stockPriceMonthly.getClose();
                    double close = stockPriceDaily.getClose();
                    double prevClose = stockPriceDaily.getPrevClose();
                    double low = stockPriceDaily.getLow();
                    double prevLow = stockPriceDaily.getPrevLow();
                    double prev2Low = stockPriceDaily.getPrev2Low();
                    boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPriceDaily);
                    boolean isPrevDownTrend =
                            stockPriceDaily.getPrevClose() < stockPriceDaily.getPrev2Low();
                    boolean isMonthlyCloseBreakout =
                            isPrevRed
                                    ? (close > monthlyClose && prevClose < monthlyClose)
                                    : this.isBodyAboveLevel(monthlyClose, stockPriceDaily);

                    boolean isMonthlyCloseLowRejected =
                            (isPrevRed
                                            ? (close > monthlyClose && low < monthlyClose)
                                            : this.isBodyAboveLevel(monthlyClose, stockPriceDaily))
                                    && isPrevDownTrend
                                    && isPrevRed;

                    double ema5 = stockTechnicalsDaily.getEma5();

                    double prevEma5 = stockTechnicalsDaily.getPrevEma5();

                    boolean isEma5Breakout =
                            isPrevRed
                                    ? (close > ema5 && prevClose < prevEma5)
                                    : this.isBodyAboveLevel(ema5, stockPriceDaily);

                    boolean isEma5LowRejected =
                            (isPrevRed
                                            ? (close > ema5 && low < ema5)
                                            : this.isBodyAboveLevel(ema5, stockPriceDaily))
                                    && isPrevDownTrend
                                    && isPrevRed;

                    double ema20 = stockTechnicalsDaily.getEma20();

                    double prevEma20 = stockTechnicalsDaily.getPrevEma20();

                    boolean isEma20Breakout =
                            isPrevRed
                                    ? (close > ema20 && prevClose < prevEma20)
                                    : this.isBodyAboveLevel(ema20, stockPriceDaily);

                    boolean isEma20LowRejected =
                            (isPrevRed
                                            ? (close > ema20 && low < ema20)
                                            : this.isBodyAboveLevel(ema20, stockPriceDaily))
                                    && isPrevDownTrend
                                    && isPrevRed;

                    double ema50 = stockTechnicalsDaily.getEma50();

                    double prevEma50 = stockTechnicalsDaily.getPrevEma50();

                    boolean isEma50Breakout =
                            isPrevRed
                                    ? (close > ema50 && prevClose < prevEma50)
                                    : this.isBodyAboveLevel(ema50, stockPriceDaily);

                    boolean isEma50LowRejected =
                            (isPrevRed
                                            ? (close > ema50 && low < ema50)
                                            : this.isBodyAboveLevel(ema50, stockPriceDaily))
                                    && isPrevDownTrend
                                    && isPrevRed;

                    double ema100 =
                            MovingAverageUtil.getMovingAverage100(
                                    stockTechnicalsDaily.getTimeframe(), stockTechnicalsDaily);

                    double prevEma100 =
                            MovingAverageUtil.getPrevMovingAverage100(
                                    stockTechnicalsDaily.getTimeframe(), stockTechnicalsDaily);

                    boolean isEma100Breakout =
                            isPrevRed
                                    ? (close > ema100 && prevClose < prevEma100)
                                    : this.isBodyAboveLevel(ema100, stockPriceDaily);

                    boolean isEma100LowRejected =
                            (isPrevRed
                                            ? (close > ema100 && low < ema100)
                                            : this.isBodyAboveLevel(ema100, stockPriceDaily))
                                    && isPrevDownTrend
                                    && isPrevRed;

                    double ema200 =
                            MovingAverageUtil.getMovingAverage200(
                                    stockTechnicalsDaily.getTimeframe(), stockTechnicalsDaily);

                    double prevEma200 =
                            MovingAverageUtil.getPrevMovingAverage200(
                                    stockTechnicalsDaily.getTimeframe(), stockTechnicalsDaily);

                    boolean isEma200Breakout =
                            isPrevRed
                                    ? (close > ema200 && prevClose < prevEma200)
                                    : this.isBodyAboveLevel(ema200, stockPriceDaily);

                    boolean isEma200LowRejected =
                            (isPrevRed
                                            ? (close > ema200 && low < ema200)
                                            : this.isBodyAboveLevel(ema200, stockPriceDaily))
                                    && isPrevDownTrend
                                    && isPrevRed;

                    boolean isMABreakout =
                            isEma5Breakout
                                    || isEma20Breakout
                                    || isEma50Breakout
                                    || isEma100Breakout
                                    || isEma200Breakout;

                    boolean isMALowRejected =
                            isEma5LowRejected
                                    || isEma20LowRejected
                                    || isEma50LowRejected
                                    || isEma100LowRejected
                                    || isEma200LowRejected;

                    double prevMid = (prevClose + stockPriceDaily.getPrevOpen()) / 2;
                    boolean isPrevGreenMidRejected =
                            isPrevRed ? true : (close > prevMid && low < prevMid);

                    if (isPrevGreenMidRejected
                            && (isMonthlyCloseBreakout || isMonthlyCloseLowRejected)
                            && (isMABreakout || isMALowRejected)) {
                        // System.out.println("TR1 " + stock.getNseSymbol());

                        /*
                        double closeWeekly = stockPriceWeekly.getClose();
                        double prevCloseWeekly = stockPriceWeekly.getPrevClose();
                        double lowWeekly = stockPriceWeekly.getLow();

                        boolean isMonthlyCloseBreakoutWeekly = isPrevRed ? (closeWeekly > monthlyClose && prevCloseWeekly < monthlyClose) : this.isBodyAboveLevel(monthlyClose, stockPriceWeekly);

                        boolean isMonthlyCloseLowRejectedWeekly = (isPrevRed ? (closeWeekly > monthlyClose && lowWeekly < monthlyClose) : this.isBodyAboveLevel(monthlyClose, stockPriceWeekly));

                        double ema5Weekly = stockTechnicalsWeekly.getEma5();

                        double prevEma5Weekly = stockTechnicalsWeekly.getPrevEma5();

                        boolean isEma5BreakoutWeekly = isPrevRed ? (closeWeekly > ema5Weekly && prevCloseWeekly < prevEma5Weekly) : this.isBodyAboveLevel(ema5Weekly, stockPriceWeekly);

                        boolean isEma5LowRejectedWeekly = (isPrevRed ? (closeWeekly > ema5Weekly && lowWeekly < ema5Weekly) : this.isBodyAboveLevel(ema5Weekly, stockPriceWeekly));

                        double ema20Weekly = stockTechnicalsWeekly.getEma20();

                        double prevEma20Weekly = stockTechnicalsWeekly.getPrevEma20();

                        boolean isEma20BreakoutWeekly = isPrevRed ? (closeWeekly > ema20Weekly && prevCloseWeekly < prevEma20Weekly) : this.isBodyAboveLevel(ema20Weekly, stockPriceWeekly);

                        boolean isEma20LowRejectedWeekly = (isPrevRed ? (closeWeekly > ema20Weekly && lowWeekly < ema20Weekly) : this.isBodyAboveLevel(ema20Weekly, stockPriceWeekly));

                        boolean isMABreakoutWeekly = isEma5BreakoutWeekly || isEma20BreakoutWeekly;

                        boolean isMALowRejectedWeekly = isEma5LowRejectedWeekly || isEma20LowRejectedWeekly ;
                        */

                        // if((isMonthlyCloseBreakoutWeekly || isMonthlyCloseLowRejectedWeekly) &&
                        // (isMABreakoutWeekly ||isMALowRejectedWeekly )){

                        double chngPct =
                                formulaService.calculateChangePercentage(
                                        stockPriceDaily.getPrevClose(), stockPriceDaily.getClose());
                        boolean isChngPctSufficient = chngPct < 2.50;
                        boolean isVolumeIncrease =
                                (stockTechnicalsDaily.getVolume()
                                                > stockTechnicalsDaily.getPrevVolume())
                                        || (stockTechnicalsDaily.getVolumeAvg20()
                                                > stockTechnicalsDaily.getPrevVolumeAvg20());
                        boolean isSupport = this.isSupport(stockPriceDaily);
                        boolean isGreen = CandleStickUtils.isGreen(stockPriceDaily);
                        if (isChngPctSufficient && isVolumeIncrease && isSupport && isGreen) {
                            //   System.out.println("TR2 " + stock.getNseSymbol());
                            boolean isUpperWickDominant =
                                    CandleStickUtils.isUpperWickDominant(stockPriceDaily);
                            double prevChngPct =
                                    formulaService.calculateChangePercentage(
                                            stockPriceDaily.getPrev2Close(),
                                            stockPriceDaily.getPrevClose());
                            if (!(isUpperWickDominant || prevChngPct > 5.0)) {
                                //    System.out.println("TR3 " + stock.getNseSymbol());
                                boolean isMAAlign =
                                        (ema20 >= ema50 && ema50 >= ema200)
                                                && (ema5 > prevEma5
                                                        && ema20 > prevEma20
                                                        && ema50 > prevEma50);
                                boolean isDisAlign =
                                        (ema20 < ema50 && ema50 < ema200)
                                                && (ema5 > prevEma5 && ema20 > prevEma20);

                                boolean isCloseAbovePrevHigh =
                                        close > stockPriceDaily.getPrevHigh()
                                                || close > stockPriceDaily.getPrev2High();

                                boolean isHaningManDaily =
                                        candleStickService.isHangingMan(stockPriceDaily)
                                                && stockPriceMonthly.getClose() > ema5
                                                && ema5 > ema20;

                                if (!isHaningManDaily) {

                                    // boolean redIndicator = isEma5LowRejected ? true :
                                    // (isPrevRed ||
                                    // CandleStickUtils.isPrev2SessionRed(stockPriceDaily));
                                    if (isCloseAbovePrevHigh) {
                                        if (isMAAlign || isDisAlign) {
                                            //     System.out.println("TR4 " +
                                            // stock.getNseSymbol());
                                            double sl = Math.min(Math.min(low, prevLow), prev2Low);
                                            sl = formulaService.applyPercentChange(sl, -1 * .10);
                                            double entry =
                                                    Math.min(
                                                            stockPriceDaily.getPrevHigh(),
                                                            formulaService.applyPercentChange(
                                                                    close, .10));
                                            double risk = this.calculateRiskPercent(entry, sl);
                                            boolean isRiskWithinLimit =
                                                    (isAllMAIncreasingMonthly
                                                                    && isAllMAIncreasingDaily)
                                                            ? true
                                                            : risk < 2.0;
                                            if (isRiskWithinLimit) {
                                                StockPrice stockPriceWeekly =
                                                        updatePriceService.buildBack(
                                                                Timeframe.WEEKLY,
                                                                stock,
                                                                sessionDate);
                                                StockTechnicals stockTechnicalsWeekly =
                                                        updateTechnicalsService.buildBack(
                                                                Timeframe.WEEKLY,
                                                                stock,
                                                                sessionDate);

                                                int score =
                                                        StockScoreCalculator.calculateScore(
                                                                stockPriceDaily,
                                                                stockTechnicalsDaily,
                                                                stockPriceWeekly,
                                                                stockTechnicalsWeekly,
                                                                stockPriceMonthly,
                                                                stockTechnicalsMonthly);

                                                StockPrice stockPriceCurrent =
                                                        updatePriceService.buildBack(
                                                                Timeframe.DAILY,
                                                                stock,
                                                                sessionDateCurrent);
                                                double gain =
                                                        formulaService.calculateChangePercentage(
                                                                entry,
                                                                stockPriceCurrent.getClose());
                                                StockPrice stockPriceLive =
                                                        stockPriceService.get(
                                                                stock, Timeframe.DAILY);
                                                double gainLive =
                                                        formulaService.calculateChangePercentage(
                                                                entry, stockPriceLive.getClose());
                                                String jsonStr =
                                                        sessionDate
                                                                + ", "
                                                                + stock.getNseSymbol()
                                                                + ", "
                                                                + entry
                                                                + ", "
                                                                + sl
                                                                + ", "
                                                                + risk
                                                                + ", "
                                                                + miscUtil.formatDouble(gain)
                                                                + ", "
                                                                + miscUtil.formatDouble(gainLive)
                                                                + ", "
                                                                + mcap
                                                                + ", "
                                                                + score; // +",["+this.buildOHLCVStr(stockPriceDaily) +"], "+isResistance(stockPriceDaily);
                                                System.out.println("Found Breakout " + jsonStr);
                                                results.add(jsonStr);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // }
                    }
                }
                sessionDate = calendarService.nextTradingSession(sessionDate);
            }
        }
        results.forEach(System.out::println);
    }

    private double calculateRiskPercent(double entry, double stopLoss) {
        if (entry <= 0 || stopLoss <= 0) {
            throw new IllegalArgumentException("Entry and SL must be > 0");
        }

        return Math.abs(entry - stopLoss) / entry * 100.0;
    }

    private boolean isAllMAIncreasing(StockTechnicals stockTechnicals) {

        double ema5Monthly = stockTechnicals.getEma5();
        double prevEma5Monthly = stockTechnicals.getPrevEma5();

        double ema20Monthly = stockTechnicals.getEma20();
        double prevEma20Monthly = stockTechnicals.getPrevEma20();

        double ema50Monthly = stockTechnicals.getEma50();
        double prevEma50Monthly = stockTechnicals.getPrevEma50();

        double ema100Monthly =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma100Monthly =
                MovingAverageUtil.getPrevMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        double ema200Monthly =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma200Monthly =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        return ema5Monthly >= prevEma5Monthly
                && ema20Monthly >= prevEma20Monthly
                && ema50Monthly >= prevEma50Monthly
                && ema100Monthly >= prevEma100Monthly
                && ema200Monthly >= prevEma200Monthly;
    }

    private String buildOHLCVStr(StockPrice stockPrice) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(stockPrice.getPrev11Open());
        sb.append(",");
        sb.append(stockPrice.getPrev11High());
        sb.append(",");
        sb.append(stockPrice.getPrev11Low());
        sb.append(",");
        sb.append(stockPrice.getPrev11Close());
        sb.append("-");
        sb.append(stockPrice.getPrev10Open());
        sb.append(",");
        sb.append(stockPrice.getPrev10High());
        sb.append(",");
        sb.append(stockPrice.getPrev10Low());
        sb.append(",");
        sb.append(stockPrice.getPrev10Close());
        sb.append("-");
        sb.append(stockPrice.getPrev9Open());
        sb.append(",");
        sb.append(stockPrice.getPrev9High());
        sb.append(",");
        sb.append(stockPrice.getPrev9Low());
        sb.append(",");
        sb.append(stockPrice.getPrev9Close());
        sb.append("-");
        sb.append(stockPrice.getPrev8Open());
        sb.append(",");
        sb.append(stockPrice.getPrev8High());
        sb.append(",");
        sb.append(stockPrice.getPrev8Low());
        sb.append(",");
        sb.append(stockPrice.getPrev8Close());
        sb.append("-");
        sb.append(stockPrice.getPrev7Open());
        sb.append(",");
        sb.append(stockPrice.getPrev7High());
        sb.append(",");
        sb.append(stockPrice.getPrev7Low());
        sb.append(",");
        sb.append(stockPrice.getPrev7Close());
        sb.append("-");
        sb.append(stockPrice.getPrev6Open());
        sb.append(",");
        sb.append(stockPrice.getPrev6High());
        sb.append(",");
        sb.append(stockPrice.getPrev6Low());
        sb.append(",");
        sb.append(stockPrice.getPrev6Close());
        sb.append("-");
        sb.append(stockPrice.getPrev5Open());
        sb.append(",");
        sb.append(stockPrice.getPrev5High());
        sb.append(",");
        sb.append(stockPrice.getPrev5Low());
        sb.append(",");
        sb.append(stockPrice.getPrev5Close());
        sb.append("-");
        sb.append(stockPrice.getPrev4Open());
        sb.append(",");
        sb.append(stockPrice.getPrev4High());
        sb.append(",");
        sb.append(stockPrice.getPrev4Low());
        sb.append(",");
        sb.append(stockPrice.getPrev4Close());
        sb.append("-");
        sb.append(stockPrice.getPrev3Open());
        sb.append(",");
        sb.append(stockPrice.getPrev3High());
        sb.append(",");
        sb.append(stockPrice.getPrev3Low());
        sb.append(",");
        sb.append(stockPrice.getPrev3Close());
        sb.append("-");
        sb.append(stockPrice.getPrev2Open());
        sb.append(",");
        sb.append(stockPrice.getPrev2High());
        sb.append(",");
        sb.append(stockPrice.getPrev2Low());
        sb.append(",");
        sb.append(stockPrice.getPrev2Close());
        sb.append("-");
        sb.append(stockPrice.getPrevOpen());
        sb.append(",");
        sb.append(stockPrice.getPrevHigh());
        sb.append(",");
        sb.append(stockPrice.getPrevLow());
        sb.append(",");
        sb.append(stockPrice.getPrevClose());
        sb.append("-");
        sb.append(stockPrice.getOpen());
        sb.append(",");
        sb.append(stockPrice.getHigh());
        sb.append(",");
        sb.append(stockPrice.getLow());
        sb.append(",");
        sb.append(stockPrice.getClose());
        sb.append("]");

        return sb.toString();
    }

    public boolean isSupport(StockPrice sp) {
        if (sp == null) {
            return false;
        }

        // 1️⃣ Collect wick lows (current + last 7)
        List<Double> lows =
                List.of(
                                sp.getLow(),
                                sp.getPrevLow(),
                                sp.getPrev2Low(),
                                sp.getPrev3Low(),
                                sp.getPrev4Low(),
                                sp.getPrev5Low(),
                                sp.getPrev6Low(),
                                sp.getPrev7Low(),
                                sp.getPrev8Low(),
                                sp.getPrev9Low(),
                                sp.getPrev10Low(),
                                sp.getPrev11Low())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(v -> v > 0)
                        .sorted()
                        .toList();

        if (lows.size() < 3) {
            return false;
        }

        // 2️⃣ Check-2 logic
        for (double base : lows) {

            double exactTolerance = Math.max(base * 0.003, 0.5); // ~0.3%
            double nearTolerance = Math.max(base * 0.012, 1.5); // ~1.2% flush

            int exactTouches = 0;
            int nearTouches = 0;

            for (double low : lows) {
                double diff = Math.abs(low - base);

                if (diff <= exactTolerance) {
                    exactTouches++;
                } else if (low < base && diff <= nearTolerance) {
                    nearTouches++; // liquidity sweep
                }
            }

            if (exactTouches >= 2 && (exactTouches + nearTouches) >= 3) {
                return true; // ✅ VALID SUPPORT
            }
        }

        return false;
    }

    public boolean isResistance(StockPrice sp) {
        if (sp == null) {
            return false;
        }

        // 1️⃣ Collect highs (current + last 7 sessions)
        List<Double> highs =
                List.of(
                                sp.getHigh(),
                                sp.getPrevHigh(),
                                sp.getPrev2High(),
                                sp.getPrev3High(),
                                sp.getPrev4High(),
                                sp.getPrev5High(),
                                sp.getPrev6High(),
                                sp.getPrev7Low(),
                                sp.getPrev8Low(),
                                sp.getPrev9Low(),
                                sp.getPrev10Low(),
                                sp.getPrev11Low())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(v -> v > 0)
                        .sorted()
                        .toList();

        if (highs.size() < 3) {
            return false;
        }

        // 2️⃣ Wick clustering logic (Check-2)
        for (double base : highs) {

            double exactTolerance = Math.max(base * 0.003, 0.5); // ~0.3%
            double nearTolerance = Math.max(base * 0.012, 1.5); // ~1.2% flush

            int exactTouches = 0;
            int nearTouches = 0;

            for (double high : highs) {
                double diff = Math.abs(high - base);

                if (diff <= exactTolerance) {
                    exactTouches++;
                } else if (high > base && diff <= nearTolerance) {
                    nearTouches++; // flush above
                }
            }

            if (exactTouches >= 2 && (exactTouches + nearTouches) >= 3) {
                return true; // ✅ VALID RESISTANCE
            }
        }

        return false;
    }

    private boolean isBodyAboveLevel(double ema, StockPrice sp) {
        if (sp == null) {
            return false;
        }

        Double open = sp.getOpen();
        Double close = sp.getClose();

        if (open == null || close == null) {
            return false;
        }

        // Only bullish candles qualify
        if (close <= open) {
            return false;
        }

        double body = close - open;
        if (body <= 0) {
            return false;
        }

        // If entire body is below EMA
        if (close <= ema) {
            return false;
        }

        // Portion of body above EMA
        double aboveEma = Math.max(0, close - Math.max(open, ema));

        double percentAbove = (aboveEma / body) * 100;

        return percentAbove >= 70.0;
    }

    private boolean isCandleStickPattern(StockPrice stockPrice) {
        boolean isBullishEngulfing = candleStickService.isBullishEngulfing(stockPrice);
        boolean isPiercingPattern = candleStickService.isPiercingPattern(stockPrice);
        boolean isTweezerBottom = candleStickService.isTweezerBottom(stockPrice);
        boolean isDoubleBottom = candleStickService.isDoubleBottom(stockPrice);
        boolean isBullishHarami = candleStickService.isBullishHarami(stockPrice);
        boolean isHammer = candleStickService.isHammer(stockPrice);

        // Confirmation Needed
        boolean isPrevBullishHarami = candleStickService.isPrevBullishHarami(stockPrice);
        boolean isPrevHammer = candleStickService.isPrevHammer(stockPrice);
        boolean isPrevInvertedHammer = candleStickService.isPrevInvertedHammer(stockPrice);

        boolean isPrevDoji = candleStickService.isPrevDoji(stockPrice);
        boolean isPrevSpinningTop = candleStickService.isPrevSpinningTop(stockPrice);

        boolean isCandleStickPattern =
                isBullishEngulfing
                        || isPiercingPattern
                        || isBullishHarami
                        || isTweezerBottom
                        || isDoubleBottom
                        || isHammer;

        boolean isPrevCandleStickPattern =
                stockPrice.getClose() > stockPrice.getPrevClose()
                        && (isPrevBullishHarami
                                || isPrevHammer
                                || isPrevInvertedHammer
                                || isPrevDoji
                                || isPrevSpinningTop);

        boolean isHigherLowHigherHigh =
                CandleStickUtils.isHigherHigh(stockPrice)
                        && CandleStickUtils.isHigherLow(stockPrice);

        return isCandleStickPattern || isPrevCandleStickPattern || isHigherLowHigherHigh;
    }

    /**
     * Monthly resistance, or high if resistance 0, breakout by weekly Weekly chngPct should not be
     * > 6% Weekly should not have resistance SL lowest low of recent 2 months
     */
    public void dynamicScannerEnhanced() {
        List<Stock> stocks = stockService.getActiveStocks();
        //   List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        int year = 2025;

        sessionDates.add(LocalDate.of(year, 1, 31));
        //  sessionDates.add(LocalDate.of(year, 2, 28));
        //  sessionDates.add(LocalDate.of(year, 3, 31));
        /*   sessionDates.add(LocalDate.of(year, 4, 30));
        sessionDates.add(LocalDate.of(year, 5, 31));
        sessionDates.add(LocalDate.of(year, 6, 30));
         sessionDates.add(LocalDate.of(year, 7, 31));
         sessionDates.add(LocalDate.of(year, 8, 31));
         sessionDates.add(LocalDate.of(year, 9, 30));
         sessionDates.add(LocalDate.of(year, 10, 31));
         sessionDates.add(LocalDate.of(year, 11, 30));
         sessionDates.add(LocalDate.of(year, 12, 31));*/

        // List<String> yearLowSupport = new ArrayList<>();

        String Header =
                "sessionDate"
                        + ", "
                        + "Symbol"
                        + ", "
                        + "isMaSupport"
                        + ", "
                        + "isLevelSupport"
                        + ", "
                        + "entryPrice"
                        + ", "
                        + "nextMonthGain"
                        + ", "
                        + "gainCurrent"
                        + ", "
                        + "mcap"
                        + ", "
                        + "Ema5"
                        + ", "
                        + "Ema20"
                        + ", "
                        + "Ema50"
                        + ", "
                        + "Rsi"
                        + ", "
                        + "Volume"
                        + ", "
                        + "PrevVolume"
                        + ", "
                        + "VolumeAvg20"
                        + ", "
                        + "yearLow"
                        + ", "
                        + "yearHigh"
                        + ", "
                        + "support"
                        + ", "
                        + "scoreMode"
                        + ", "
                        + "score"
                        + ", "
                        + "ohlcoldesttolatest";
        results.add(Header);

        Map<String, String> hh = new HashMap<>();

        for (Stock stock : stocks) {
            //  System.out.println(stock.getNseSymbol() +" h1 ");
            if (!this.isInititalValidated(stock)) {
                continue;
            }

            for (LocalDate sessionDate : sessionDates) {

                LocalDate sessionDateMonthEnd =
                        calendarService.previousTradingSession(
                                sessionDate.plusMonths(2).withDayOfMonth(1));

                StockPrice stockPriceYearly =
                        updatePriceService.buildBack(
                                Timeframe.YEARLY,
                                stock,
                                sessionDate.minusMonths(1).withDayOfYear(1).minusDays(1));
                StockTechnicals stockTechnicalsYearly =
                        updateTechnicalsService.buildBack(
                                Timeframe.YEARLY,
                                stock,
                                sessionDate.minusMonths(1).withDayOfYear(1).minusDays(1));
                /*
                if (CandleStickUtils.isLowerHigh(stockPriceYearly)
                        && CandleStickUtils.isLowerLow(stockPriceYearly)
                        && CandleStickUtils.isPrevLowerHigh(stockPriceYearly)
                        && CandleStickUtils.isPrevLowerLow(stockPriceYearly)) {
                    continue;
                }*/

                StockPrice stockPrice = updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);
                //  System.out.println(stock.getNseSymbol() +" h2 ");
                /*
                if (isAtBottom(stockPrice, stockTechnicals)) {
                    continue;
                }*/

                double mcap = fundamentalResearchService.marketCap(stockPrice);

                if (mcap < 750 || mcap > 50_000) {
                    continue;
                }

                /*
                if (stockPrice.getClose() > stockTechnicals.getEma20()
                        && CandleStickUtils.isUpperWickLongerThanLowerWick(stockPrice)) {
                    continue;
                }*/

                if (!(this.isInititalValidated(stockPrice)
                        && this.isInititalValidated(stockTechnicals))) {
                    continue;
                }

                boolean isPRevCloseBelowEma20 =
                        CandleStickUtils.isGreen(stockPrice)
                                && stockPrice.getPrevClose() < stockTechnicals.getPrevEma20();

                boolean isCloseBelowEma20 =
                        CandleStickUtils.isRed(stockPrice)
                                && stockPrice.getClose() < stockTechnicals.getEma20();

                /*
                if(!(isPRevCloseBelowEma20 || isCloseBelowEma20)){
                    continue;
                }*/

                double close = stockPrice.getClose();
                double low = stockPrice.getLow();
                double ema20 = stockTechnicals.getEma20();
                double ema50 = formulaService.applyPercentChange(stockTechnicals.getEma50(), 5.0);

                boolean isEma20LowRejected = low <= ema20 && close > ema20;
                boolean isEma50LowRejected = low <= ema50 && close > ema50;

                boolean isLowRejected =
                        isPRevCloseBelowEma20 && (isEma20LowRejected || isEma50LowRejected);

                // if(!isLowRejected){
                //  continue;
                // }

                Optional<MASupportChecker.MAInteraction> maInteractionOptional =
                        maSupportChecker.findSingleMASupport(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

                boolean isSupport = false;
                double support = 0.0;
                if (MASupportChecker.isPreviousDowntrend(stockPrice)) {
                    support = SupportFinder.findSupport(stockPrice, 11);
                    isSupport =
                            stockPrice.getLow() <= support
                                    && Math.min(stockPrice.getOpen(), stockPrice.getClose())
                                            > support;

                    if (isSupport) {

                        System.out.println(
                                sessionDate + " " + stock.getNseSymbol() + " 000 " + support);
                    }
                }

                boolean isMASupport = (maInteractionOptional.isPresent());

                double chngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPrice.getPrevClose(), stockPrice.getClose());

                double prevChngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPrice.getPrev2Close(), stockPrice.getPrevClose());

                double prev2ChngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPrice.getPrev3Close(), stockPrice.getPrev2Close());

                boolean isSmallBodyOrGreen =
                        (chngPct < 10.0 || CandleStickUtils.isGreen(stockPrice));

                boolean lowerWickGreaterThanUpperAndSmallBody =
                        (CandleStickUtils.lowerWickSize(stockPrice)
                                        > CandleStickUtils.upperWickSize(stockPrice)
                                || (chngPct < 10 && prevChngPct > chngPct));
                boolean smallRelativeBody = (prevChngPct > chngPct * 2);

                boolean isCloseBelowEma5 = stockPrice.getClose() < stockTechnicals.getEma5();

                boolean eitherGreenOrVolumeIncreasing =
                        CandleStickUtils.isGreen(stockPrice)
                                || ((stockTechnicals.getVolume()
                                                        > stockTechnicals.getPrevVolume() * 1.5
                                                || stockTechnicals.getVolume()
                                                        > stockTechnicals.getVolumeAvg20() * 1.5)
                                        //  && (stockTechnicals.getVolume() >
                                        // stockTechnicals.getVolumeAvg20() ||
                                        // stockTechnicals.getVolumeAvg20() >
                                        // stockTechnicals.getPrevVolumeAvg20())
                                        && isCloseBelowEma5
                                        && lowerWickGreaterThanUpperAndSmallBody);

                /*
                boolean isLevelSupport =
                        (isSupport
                                && (isSmallBodyOrGreen || smallRelativeBody)
                                && eitherGreenOrVolumeIncreasing);*/
                boolean isLevelSupport = isSupport;

                // System.out.println(lowerWickGreaterThanUpperAndsmallGreenBody +"
                // "+smallRelativeBody);

                boolean isVolume =
                        stockTechnicals.getVolume()
                                > (stockTechnicals.getVolume()
                                                + stockTechnicals.getPrevVolume()
                                                + stockTechnicals.getPrev2Volume())
                                        / 3;

                isVolume =
                        isVolume || (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume());

                // double close = stockPrice.getClose();
                // double low = stockPrice.getLow();
                double ema5 = stockTechnicals.getEma5();
                // double ema20 = stockTechnicals.getEma20();
                // double ema50 = stockTechnicals.getEma50();

                // boolean isLowRejected = (low < ema5 && close > ema5) || (low < ema20 && close >
                // ema20) || (low < ema50 && close > ema50);

                if (isMASupport || isLevelSupport) {

                    // System.out.println(stockTechnicals.getVolume());
                    // System.out.println(stockTechnicals.getVolumeAvg20());
                    if (CandleStickUtils.isGreen(stockPrice)
                            || stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20()) {

                        Map<StockScanner.ScoreMode, Double> scannerResult =
                                StockScanner.evaluateStock(
                                        stockPrice,
                                        stockTechnicals,
                                        stockPriceYearly.getLow(),
                                        stockPriceYearly.getHigh());
                        StockScanner.ScoreMode scoreMode = StockScanner.ScoreMode.None;
                        Double score = 0.0;
                        if (scannerResult.get(StockScanner.ScoreMode.Both) != null) {
                            scoreMode = StockScanner.ScoreMode.Both;
                            score = scannerResult.get(StockScanner.ScoreMode.Both);
                        } else if (scannerResult.get(StockScanner.ScoreMode.Mean_Reversion)
                                != null) {
                            scoreMode = StockScanner.ScoreMode.Mean_Reversion;
                            score = scannerResult.get(StockScanner.ScoreMode.Mean_Reversion);
                        } else if (scannerResult.get(StockScanner.ScoreMode.Trend_Continuation)
                                != null) {
                            scoreMode = StockScanner.ScoreMode.Trend_Continuation;
                            score = scannerResult.get(StockScanner.ScoreMode.Trend_Continuation);
                        } else if (scannerResult.get(StockScanner.ScoreMode.None) != null) {
                            scoreMode = StockScanner.ScoreMode.None;
                            score = scannerResult.get(StockScanner.ScoreMode.None);
                        }

                        double entryPrice = stockPrice.getClose();

                        boolean isGreen = CandleStickUtils.isGreen(stockPrice);
                        double open = stockPrice.getOpen();

                        if (isLevelSupport && isMASupport) {
                            if (isGreen) {
                                entryPrice = (open + close) / 2;
                            } else {
                                entryPrice = (support + open) / 2;
                            }
                        }

                        if (isLevelSupport) {
                            if (isGreen) {
                                entryPrice = (support + close) / 2;
                            } else {
                                entryPrice = Math.min(support, low);
                            }
                            entryPrice = Math.min(entryPrice, stockPrice.getPrev2Low());
                        }

                        if (isMASupport) {
                            if (isGreen) {
                                entryPrice = close;
                            } else {
                                entryPrice = (open + close) / 2;
                            }
                        }

                        if (isGreen) {
                            entryPrice = formulaService.applyPercentChange(entryPrice, 0.5);
                        }

                        StockPrice stockPriceMonthEnd =
                                updatePriceService.buildBack(DAILY, stock, sessionDateMonthEnd);

                        double gain =
                                formulaService.calculateChangePercentage(
                                        entryPrice, stockPriceMonthEnd.getClose());

                        StockPrice stockPriceCurrent = stockPriceService.get(stock, DAILY);
                        double gainCurrent =
                                formulaService.calculateChangePercentage(
                                        entryPrice, stockPriceCurrent.getClose());

                        String ohlc = this.buildOHLCVStr(stockPrice);
                        double chngPctFromYearHigh =
                                formulaService.calculateAbsChangePercentage(
                                        stockPriceYearly.getHigh(), stockPrice.getLow());

                        boolean isPRev2SessionRed =
                                CandleStickUtils.isPrev2SessionRed(stockPrice)
                                        && CandleStickUtils.isPrevSessionRed(stockPrice);

                        boolean isGReen = CandleStickUtils.isGreen(stockPrice);
                        boolean isPrevGReen = CandleStickUtils.isPrevSessionGreen(stockPrice);
                        boolean isSufficientBody =
                                formulaService.applyPercentChange(chngPct, 50) < prevChngPct
                                        && isGReen;

                        boolean isPrevSufficientBody =
                                formulaService.applyPercentChange(prevChngPct, 50) < prev2ChngPct
                                        && chngPct < prev2ChngPct
                                        && (isGReen || isPrevGReen);

                        if (score >= 6.0 && (isSufficientBody || isPrevSufficientBody)) {
                            String json =
                                    sessionDate
                                            + ", "
                                            + stock.getNseSymbol()
                                            + ", "
                                            + isMASupport
                                            + ", "
                                            + isLevelSupport
                                            + ", "
                                            + entryPrice
                                            + ", "
                                            + gain
                                            + ", "
                                            + gainCurrent
                                            + ", "
                                            + mcap
                                            + ", "
                                            + stockTechnicals.getEma5()
                                            + ", "
                                            + stockTechnicals.getEma20()
                                            + ", "
                                            + stockTechnicals.getEma50()
                                            + ", "
                                            + stockTechnicals.getRsi()
                                            + ", "
                                            + stockTechnicals.getVolume()
                                            + ", "
                                            + stockTechnicals.getPrevVolume()
                                            + ", "
                                            + stockTechnicals.getVolumeAvg20()
                                            + ", "
                                            + stockPriceYearly.getLow()
                                            + ", "
                                            + stockPriceYearly.getHigh()
                                            + ", "
                                            + support
                                            + ", "
                                            + scoreMode
                                            + ", "
                                            + score
                                            + ", "
                                            + chngPctFromYearHigh;
                            ;

                            System.out.println("Found strong monthly ");
                            System.out.println(Header);
                            System.out.println(scoreMode + " " + json);
                            results.add(json);
                            hh.put(stock.getNseSymbol(), json);
                        }
                    }
                }

                /*
                if(hh.containsKey(stock.getNseSymbol())) {
                    //LocalDate nextMonthSessionDate = sessionDate;
                    LocalDate monthFirstSession = calendarService.nextTradingSession(sessionDate.minusMonths(1));
                    LocalDate nextSaturday = monthFirstSession.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
                    LocalDate weeklySessionStart = calendarService.previousTradingSession(nextSaturday);

                    while (!weeklySessionStart.isAfter(sessionDate)) {
                        StockPrice stockPriceWeekly =
                                updatePriceService.buildBack(
                                        WEEKLY, stock, weeklySessionStart);

                        if (stockPriceWeekly.getClose() < stockPrice.getLow() && CandleStickUtils.isRed(stockPriceWeekly) && !(CandleStickUtils.lowerWickSize(stockPriceWeekly) > CandleStickUtils.upperWickSize(stockPriceWeekly))) {
                            hh.remove(stock.getNseSymbol());
                            System.out.println("Removed1 " + stock.getNseSymbol());
                        }

                        weeklySessionStart = weeklySessionStart.plusWeeks(1);

                    }

                    StockPrice stockPriceNextMonth =
                            updatePriceService.buildBack(
                                    MONTHLY, stock, sessionDate);

                    StockTechnicals stockTechnicalsNextMonth = updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                    double chngPctNextMonth = formulaService.calculateAbsChangePercentage(stockPriceNextMonth.getPrevClose(), stockPriceNextMonth.getClose());

                    boolean isRedAndCloseAboveLow = chngPctNextMonth < 10.0 && CandleStickUtils.isRed(stockPriceNextMonth) && stockPriceNextMonth.getClose() > stockPriceNextMonth.getPrevLow() && ((CandleStickUtils.lowerWickSize(stockPriceNextMonth) > CandleStickUtils.upperWickSize(stockPriceNextMonth)) || CandleStickUtils.isVerySmallBody(stockPriceNextMonth));
                    boolean isHigherHigh = CandleStickUtils.isGreen(stockPriceNextMonth) && CandleStickUtils.isHigherHigh(stockPriceNextMonth) && (CandleStickUtils.isUpperWickWithinLimit(stockPriceNextMonth) || (CandleStickUtils.lowerWickSize(stockPriceNextMonth) > CandleStickUtils.upperWickSize(stockPriceNextMonth)) || chngPctNextMonth < 10.0);
                    boolean isNextMonthMomentum =   (isRedAndCloseAboveLow || isHigherHigh)  && chngPct < prevChngPct;

                    //System.out.println(prevChngPct);
                    //System.out.println(chngPct);
                    //System.out.println(chngPctNextMonth);
                   // System.out.println(this.buildOHLCVStr(stockPrice));
                  //  System.out.println(this.buildOHLCVStr(stockPriceNextMonth));

                    if(!isNextMonthMomentum){
                        hh.remove(stock.getNseSymbol());
                        System.out.println("Removed2 " + stock.getNseSymbol());
                    }
                    if(stockPriceNextMonth.getClose() < stockPrice.getLow()){
                        hh.remove(stock.getNseSymbol());
                        System.out.println("Removed3 " + stock.getNseSymbol());
                    }
                    if(MovingAverageUtil.isAllMAsDecreasing(stockTechnicalsNextMonth)){
                        hh.remove(stock.getNseSymbol());
                        System.out.println("Removed4 " + stock.getNseSymbol());
                    }

                }*/
            }
        }
        System.out.println("-----results-----");
        // results.forEach(System.out::println);
        System.out.println(Header);
        hh.forEach(
                (k, v) -> {
                    System.out.println(v);
                });
    }

    public void dynamicScannerEnhanced1Weekly() {
        List<Stock> stocks = stockService.getActiveStocks();
        // List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<Double> levels = new ArrayList<>();
        int year = 2025;
        List<LocalDate> sessionDates = new ArrayList<>();
        sessionDates.add(LocalDate.of(year, 1, 31));
        sessionDates.add(LocalDate.of(year, 2, 28));
        sessionDates.add(LocalDate.of(year, 3, 31));
        sessionDates.add(LocalDate.of(year, 4, 30));
        sessionDates.add(LocalDate.of(year, 5, 31));
        sessionDates.add(LocalDate.of(year, 6, 30));
        sessionDates.add(LocalDate.of(year, 7, 31));
        sessionDates.add(LocalDate.of(year, 8, 31));
        sessionDates.add(LocalDate.of(year, 9, 30));
        sessionDates.add(LocalDate.of(year, 10, 31));
        sessionDates.add(LocalDate.of(year, 11, 30));
        sessionDates.add(LocalDate.of(year, 12, 31));

        Map<String, String> hh = new HashMap<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }
            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(YEARLY, stock, LocalDate.of(year - 1, 12, 31));
            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(
                            YEARLY, stock, LocalDate.of(year - 1, 12, 31));

            double range =
                    formulaService.calculateChangePercentage(
                            stockPriceYearly.getLow(), stockPriceYearly.getHigh());
            double prevRange =
                    formulaService.calculateChangePercentage(
                            stockPriceYearly.getPrevLow(), stockPriceYearly.getPrevHigh());

            if (range < 38.2 || prevRange < 38.2) {
                continue;
            }

            this.addLevels(stockPriceYearly, levels);

            for (LocalDate sessionDate : sessionDates) {

                StockPrice stockPriceMonthly =
                        updatePriceService.buildBack(MONTHLY, stock, sessionDate);

                StockTechnicals stockTechnicalsMonthly =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                double mcap = fundamentalResearchService.marketCap(stockPriceMonthly);

                if (mcap < 750 || mcap > 50_000) {
                    continue;
                }

                boolean isDownTrend =
                        (CandleStickUtils.isLowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isLowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getClose() < stockPriceMonthly.getPrevClose()
                                && (CandleStickUtils.isPrevLowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrevLowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrevClose()
                                        < stockPriceMonthly.getPrev2Close();

                boolean isPrevDownTrend =
                        (CandleStickUtils.isPrevLowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrevLowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrevClose()
                                        < stockPriceMonthly.getPrev2Close()
                                && (CandleStickUtils.isPrev2LowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrev2LowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrev2Close()
                                        < stockPriceMonthly.getPrev3Close();

                boolean isPrev2DownTrend =
                        (CandleStickUtils.isPrev2LowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrev2LowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrev2Close()
                                        < stockPriceMonthly.getPrev3Close()
                                && (CandleStickUtils.isPrev3LowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrev3LowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrev3Close()
                                        < stockPriceMonthly.getPrev4Close();

                if (!(isDownTrend || isPrevDownTrend || isPrev2DownTrend)) {
                    continue;
                }
                double correctionFromSwingHigh =
                        this.correctionFromSwingHigh(stockPriceMonthly, stockPriceYearly.getHigh());

                if (correctionFromSwingHigh > -1 * 22.50) {
                    continue;
                }

                if (!this.isValidSetup(stockPriceMonthly, stockTechnicalsMonthly)) {
                    continue;
                }

                // Get first day of next month
                LocalDate firstDayOfNextMonth =
                        sessionDate.with(TemporalAdjusters.firstDayOfNextMonth());

                // Find the first Monday in that month
                LocalDate sessionDateStart =
                        firstDayOfNextMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

                // Get last day of next month
                LocalDate lastDayOfNextMonth =
                        firstDayOfNextMonth.with(TemporalAdjusters.lastDayOfMonth());

                // Find the last Friday in that month
                LocalDate sessionDateEnd =
                        lastDayOfNextMonth.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));

                for (LocalDate sessionDateMonday = sessionDateStart;
                        !sessionDateMonday.isAfter(sessionDateEnd);
                        sessionDateMonday = sessionDateMonday.plusWeeks(1)) {
                    // Get last session date of previous week
                    LocalDate sessionDateWeekly =
                            calendarService.previousTradingSession(sessionDateMonday);

                    // Get last session date of next week
                    LocalDate sessionDateNextWeekly =
                            calendarService.previousTradingSession(sessionDateMonday.plusWeeks(1));

                    StockPrice stockPriceWeekly =
                            updatePriceService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    StockTechnicals stockTechnicalsWeekly =
                            updateTechnicalsService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    double closeWeekly = stockPriceWeekly.getClose();
                    double lowWeekly = stockPriceWeekly.getLow();

                    StockPrice stockPriceNextWeek =
                            updatePriceService.buildBack(DAILY, stock, sessionDateNextWeekly);

                    double gain =
                            formulaService.calculateChangePercentage(
                                    stockPriceWeekly.getClose(), stockPriceNextWeek.getClose());

                    if (CandleStickUtils.isGreen(stockPriceWeekly)) {
                        String result =
                                sessionDate
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + sessionDateWeekly
                                        + ", "
                                        + sessionDateNextWeekly
                                        + ", "
                                        + miscUtil.formatDouble(gain)
                                        + ", "
                                        + correctionFromSwingHigh;
                        results.add(result);
                        System.out.println("Found it1 " + result);
                    }
                    /*
                    if(this.isLowestBreakout(stockPriceWeekly, stockTechnicalsWeekly)) {
                        if(this.isVolumeAboveAverage(stockPriceWeekly, stockTechnicalsWeekly)) {
                            String result = stock.getNseSymbol() + ", " + sessionDateWeekly + ", " + sessionDateNextWeekly + ", " + miscUtil.formatDouble(gain) +", " +"R1";
                            results.add(result);
                            System.out.println("Found it1 " + result);
                        }
                    }
                        Optional<MASupportChecker.MAInteraction> maInteractionOptional =
                                maSupportChecker.findSingleMASupport(
                                        stockPriceWeekly.getTimeframe(), stockPriceWeekly, stockTechnicalsWeekly, true);

                    if(maInteractionOptional.isPresent()){
                        if(this.isVolumeAboveAverage(stockPriceWeekly, stockTechnicalsWeekly)) {
                            String result = stock.getNseSymbol() + ", " + sessionDateWeekly + ", " + sessionDateNextWeekly + ", " + miscUtil.formatDouble(gain) +", " +"R2";
                            results.add(result);
                            System.out.println("Found it2 " + result);
                        }
                    }

                        if (MASupportChecker.isPreviousDowntrend(stockPriceWeekly)) {
                            double support = SupportFinder.findSupport(stockPriceWeekly, 11);
                            boolean isSupport =
                                    stockPriceWeekly.getLow() <= support
                                            && Math.min(stockPriceWeekly.getOpen(), stockPriceWeekly.getClose())
                                            > support;

                            if(isSupport){
                                if(this.isVolumeAboveAverage(stockPriceWeekly, stockTechnicalsWeekly)) {
                                    String result = stock.getNseSymbol() + ", " + sessionDateWeekly + ", " + sessionDateNextWeekly + ", " + miscUtil.formatDouble(gain) +", " +"R3";
                                    results.add(result);
                                    System.out.println("Found it3 " + result);
                                }
                            }
                        }

                        List<Double> levelsToCheck = this.removeLevelsGreaterThanClose(levels, closeWeekly);

                        levelsToCheck = this.removeLevelsLessThanClose(levelsToCheck, lowWeekly);

                        for(Double level: levelsToCheck){
                            boolean isLevelRespected =  this.isLevelRespected(level, stockPriceWeekly);
                            boolean isSupportAtLows = this.isSupportAtLows(stockPriceWeekly);

                            if( isLevelRespected && isSupportAtLows){
                                if(this.isVolumeAboveAverage(stockPriceWeekly, stockTechnicalsWeekly)) {
                                    String result = stock.getNseSymbol() + ", " + sessionDateWeekly + ", " + sessionDateNextWeekly + ", " + miscUtil.formatDouble(gain) +", " +"R4";
                                    results.add(result);
                                    System.out.println("Found it4 " + result);
                                }
                                break;
                            }
                        }
                    */
                }
            }
        }
        System.out.println("-----results-----");
        results.forEach(System.out::println);
    }

    public void dynamicScannerEnhanced1Monthly() {
        //  List<Stock> stocks = stockService.getActiveStocks();
        List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<Double> levels = new ArrayList<>();
        int year = 2025;
        List<LocalDate> sessionDates = new ArrayList<>();
        sessionDates.add(YearMonth.of(year, Month.JANUARY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.FEBRUARY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.MARCH).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.APRIL).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.MAY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.JUNE).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.JULY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.AUGUST).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.SEPTEMBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.OCTOBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.NOVEMBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.DECEMBER).atEndOfMonth());

        Map<String, String> hh = new HashMap<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }
            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(
                            YEARLY, stock, YearMonth.of(year - 1, Month.DECEMBER).atEndOfMonth());
            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(
                            YEARLY, stock, YearMonth.of(year - 1, Month.DECEMBER).atEndOfMonth());

            double range =
                    formulaService.calculateChangePercentage(
                            stockPriceYearly.getLow(), stockPriceYearly.getHigh());
            double prevRange =
                    formulaService.calculateChangePercentage(
                            stockPriceYearly.getPrevLow(), stockPriceYearly.getPrevHigh());

            if (range < 38.2 || prevRange < 38.2) {
                //   continue;
            }

            this.addLevels(stockPriceYearly, levels);

            for (LocalDate sessionDate : sessionDates) {

                if (sessionDate.isAfter(LocalDate.now())) {
                    continue;
                }

                StockPrice stockPriceMonthly =
                        updatePriceService.buildBack(MONTHLY, stock, sessionDate);

                StockTechnicals stockTechnicalsMonthly =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                double mcap = fundamentalResearchService.marketCap(stockPriceMonthly);

                if (mcap < 750 || mcap > 50_000) {
                    continue;
                }

                boolean isDownTrend =
                        (CandleStickUtils.isLowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isLowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getClose() < stockPriceMonthly.getPrevClose()
                                && (CandleStickUtils.isPrevLowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrevLowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrevClose()
                                        < stockPriceMonthly.getPrev2Close();

                boolean isPrevDownTrend =
                        (CandleStickUtils.isPrevLowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrevLowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrevClose()
                                        < stockPriceMonthly.getPrev2Close()
                                && (CandleStickUtils.isPrev2LowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrev2LowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrev2Close()
                                        < stockPriceMonthly.getPrev3Close();

                boolean isPrev2DownTrend =
                        (CandleStickUtils.isPrev2LowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrev2LowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrev2Close()
                                        < stockPriceMonthly.getPrev3Close()
                                && (CandleStickUtils.isPrev3LowerHigh(stockPriceMonthly)
                                        || CandleStickUtils.isPrev3LowerLow(stockPriceMonthly))
                                && stockPriceMonthly.getPrev3Close()
                                        < stockPriceMonthly.getPrev4Close();

                if (!(isDownTrend || isPrevDownTrend || isPrev2DownTrend)) {
                    continue;
                }

                double correctionFromSwingHigh =
                        this.correctionFromSwingHigh(stockPriceMonthly, stockPriceYearly.getHigh());

                if (correctionFromSwingHigh > -1 * 22.50) {
                    continue;
                }

                if (!this.isValidSetup(stockPriceMonthly, stockTechnicalsMonthly)) {
                    continue;
                }

                double support = SupportFinder.findSupport(stockPriceMonthly, 11);

                boolean isSupport =
                        (CandleStickUtils.isGreen(stockPriceMonthly)
                                        && stockPriceMonthly.getOpen()
                                                < stockPriceMonthly.getPrevClose())
                                || stockPriceMonthly.getLow() <= support
                                        && Math.min(
                                                        stockPriceMonthly.getOpen(),
                                                        stockPriceMonthly.getClose())
                                                > support;

                double chngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPriceMonthly.getPrevClose(), stockPriceMonthly.getClose());
                double prevChngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPriceMonthly.getPrev2Close(),
                                stockPriceMonthly.getPrevClose());

                if (isSupport
                        && (CandleStickUtils.isGreen(stockPriceMonthly)
                                || (chngPct < 6.0
                                        && prevChngPct < 6.0
                                        && stockTechnicalsMonthly.getVolume()
                                                > stockTechnicalsMonthly.getVolumeAvg20()))) {

                    if (chngPct < 10.0) {

                        if (stockTechnicalsMonthly.getVolume()
                                        > stockTechnicalsMonthly.getPrevVolume()
                                || stockTechnicalsMonthly.getPrevVolume()
                                        > stockTechnicalsMonthly.getPrev2Volume()) {
                            boolean isStructureSatisfied =
                                    (CandleStickUtils.isPrevSessionRed(stockPriceMonthly)
                                                    && (stockPriceMonthly.getHigh()
                                                                    < stockPriceMonthly
                                                                            .getPrevOpen()
                                                            || (stockPriceMonthly.getClose()
                                                                            < stockPriceMonthly
                                                                                    .getPrevOpen()
                                                                    && stockPriceMonthly.getHigh()
                                                                            < stockPriceMonthly
                                                                                    .getPrevHigh())))
                                            || (CandleStickUtils.isPrevSessionGreen(
                                                            stockPriceMonthly)
                                                    && (stockPriceMonthly.getClose()
                                                                    > stockPriceMonthly
                                                                            .getPrev2Close()
                                                            || CandleStickUtils
                                                                    .isHigherHighAndHigherLow(
                                                                            stockPriceMonthly)));
                            if (isStructureSatisfied || prevChngPct < 6.0) {

                                double score =
                                        this.score(
                                                stockPriceMonthly,
                                                stockTechnicalsMonthly,
                                                stockPriceYearly.getLow(),
                                                stockPriceYearly.getHigh());

                                if (score >= 70.0) {
                                    double entry =
                                            Math.max(
                                                    stockPriceMonthly.getOpen(),
                                                    formulaService.calculateChangePercentage(
                                                            (stockPriceMonthly.getOpen()
                                                                            + stockPriceMonthly
                                                                                    .getClose())
                                                                    / 2,
                                                            -1 * 2.0));

                                    if (stockTechnicalsMonthly.getVolume()
                                            > stockTechnicalsMonthly.getVolumeAvg20()) {
                                        entry =
                                                (stockPriceMonthly.getOpen()
                                                                + stockPriceMonthly.getClose())
                                                        / 2;
                                    }

                                    boolean isVolumeIncrease =
                                            stockTechnicalsMonthly.getVolume()
                                                    > stockTechnicalsMonthly.getPrevVolume();
                                    if (score >= 80.0 && isVolumeIncrease) {
                                        entry =
                                                formulaService.applyPercentChange(
                                                        stockPriceMonthly.getClose(), 2.0);
                                    } else if (score >= 75.0 && isVolumeIncrease) {
                                        entry =
                                                formulaService.applyPercentChange(
                                                        stockPriceMonthly.getClose(), 0.5);
                                    } else if (score >= 70.0 && isVolumeIncrease) {
                                        if (prevChngPct < 5.0 && chngPct < 5.0) {
                                            entry =
                                                    formulaService.applyPercentChange(
                                                            stockPriceMonthly.getClose(), 0.5);
                                        }
                                    }

                                    double stopLoss =
                                            Math.min(
                                                    stockPriceMonthly.getLow(),
                                                    stockPriceMonthly.getPrevLow());

                                    stopLoss =
                                            formulaService.applyPercentChange(stopLoss, -1 * 1.2);

                                    String result =
                                            sessionDate
                                                    + ", "
                                                    + stock.getNseSymbol()
                                                    + ", "
                                                    + mcap
                                                    + ", "
                                                    + score
                                                    + ", "
                                                    + miscUtil.formatDouble(
                                                            formulaService.ceilToNearestHalf(entry))
                                                    + ", "
                                                    + miscUtil.formatDouble(
                                                            formulaService.floorToNearestHalf(
                                                                    stopLoss));
                                    results.add(result);
                                    System.out.println("Found it1 " + result);
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("-----results-----");
        results.forEach(System.out::println);
    }

    public void dynamicScannerEnhanced2Monthly() {
        //  List<Stock> stocks = stockService.getActiveStocks();
        List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<Double> levels = new ArrayList<>();
        int year = 2025;
        List<LocalDate> sessionDates = new ArrayList<>();
        sessionDates.add(YearMonth.of(year, Month.JANUARY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.FEBRUARY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.MARCH).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.APRIL).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.MAY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.JUNE).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.JULY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.AUGUST).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.SEPTEMBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.OCTOBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.NOVEMBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.DECEMBER).atEndOfMonth());

        Map<String, String> hh = new HashMap<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }
            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(
                            YEARLY, stock, YearMonth.of(year - 1, Month.DECEMBER).atEndOfMonth());

            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(
                            YEARLY, stock, YearMonth.of(year - 1, Month.DECEMBER).atEndOfMonth());

            for (LocalDate sessionDate : sessionDates) {

                if (sessionDate.isAfter(LocalDate.now())) {
                    continue;
                }

                StockPrice stockPriceMonthly =
                        updatePriceService.buildBack(MONTHLY, stock, sessionDate);

                StockTechnicals stockTechnicalsMonthly =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                double range =
                        formulaService.calculateChangePercentage(
                                stockPriceMonthly.getLow(), stockPriceMonthly.getHigh());
                double body =
                        formulaService.calculateChangePercentage(
                                Math.min(stockPriceMonthly.getOpen(), stockPriceMonthly.getClose()),
                                Math.max(
                                        stockPriceMonthly.getOpen(), stockPriceMonthly.getClose()));

                if (range < 10.0 || body > 10.0) {
                    continue;
                }

                double mcap = fundamentalResearchService.marketCap(stockPriceMonthly);

                if (mcap < 750 || mcap > 50_000) {
                    continue;
                }

                if (stockTechnicalsMonthly.getVolume() > stockTechnicalsMonthly.getVolumeAvg20()) {
                    //   continue;
                }

                boolean isLowRejectedEma5 =
                        stockPriceMonthly.getLow() <= stockTechnicalsMonthly.getEma5()
                                && stockPriceMonthly.getClose() > stockTechnicalsMonthly.getEma5();

                if (!isLowRejectedEma5) {
                    continue;
                }

                boolean isEitherGreenOrHhHL =
                        CandleStickUtils.isGreen(stockPriceMonthly)
                                || CandleStickUtils.isHigherHighAndHigherLow(stockPriceMonthly);

                if (!(isEitherGreenOrHhHL)) {
                    continue;
                }

                if (!this.isValidSetup(stockPriceMonthly, stockTechnicalsMonthly)) {
                    continue;
                }

                boolean isMAAlignBullish =
                        (stockTechnicalsMonthly.getEma50() != 0
                                        && stockTechnicalsMonthly.getEma20()
                                                > stockTechnicalsMonthly.getEma50())
                                || (stockTechnicalsMonthly.getEma20() != 0
                                        && stockTechnicalsMonthly.getEma5()
                                                > stockTechnicalsMonthly.getEma20());

                if (!isMAAlignBullish) {
                    continue;
                }

                this.addLevels(stockPriceMonthly, levels);
                // Get first day of next month
                LocalDate firstDayOfNextMonth =
                        sessionDate.with(TemporalAdjusters.firstDayOfNextMonth());

                // Find the first Monday in that month
                LocalDate sessionDateStart =
                        firstDayOfNextMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

                // Get last day of next month
                LocalDate lastDayOfNextMonth =
                        firstDayOfNextMonth.with(TemporalAdjusters.lastDayOfMonth());

                // Find the last Friday in that month
                LocalDate sessionDateEnd =
                        lastDayOfNextMonth.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));

                for (LocalDate sessionDateMonday = sessionDateStart;
                        !sessionDateMonday.isAfter(sessionDateEnd);
                        sessionDateMonday = sessionDateMonday.plusWeeks(1)) {
                    // Get last session date of previous week
                    LocalDate sessionDateWeekly =
                            calendarService.previousTradingSession(sessionDateMonday);

                    // Get last session date of next week
                    LocalDate sessionDateNextWeekly =
                            calendarService.previousTradingSession(sessionDateMonday.plusWeeks(1));

                    StockPrice stockPriceWeekly =
                            updatePriceService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    StockTechnicals stockTechnicalsWeekly =
                            updateTechnicalsService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    double closeWeekly = stockPriceWeekly.getClose();
                    double lowWeekly = stockPriceWeekly.getLow();

                    List<Double> levelsToCheck =
                            this.removeLevelsGreaterThanClose(levels, stockPriceWeekly.getClose());

                    levelsToCheck =
                            this.removeLevelsLessThanClose(
                                    levelsToCheck,
                                    Math.min(
                                            stockPriceWeekly.getPrevLow(),
                                            stockPriceWeekly.getLow()));

                    for (double level : levelsToCheck) {

                        boolean isLevelRejected =
                                stockPriceWeekly.getLow() <= level
                                        && stockPriceWeekly.getClose() > level;

                        if (isLevelRejected) {
                            StockPrice stockPriceNextWeek =
                                    updatePriceService.buildBack(
                                            DAILY, stock, sessionDateNextWeekly);

                            double gain =
                                    formulaService.calculateChangePercentage(
                                            stockPriceWeekly.getClose(),
                                            stockPriceNextWeek.getClose());

                            if (CandleStickUtils.isGreen(stockPriceWeekly)) {
                                String result =
                                        sessionDate
                                                + ", "
                                                + stock.getNseSymbol()
                                                + ", "
                                                + sessionDateWeekly
                                                + ", "
                                                + sessionDateNextWeekly
                                                + ", "
                                                + level
                                                + ", "
                                                + miscUtil.formatDouble(gain);
                                results.add(result);
                                System.out.println("Found it1 " + result);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("-----results-----");
        results.forEach(System.out::println);
    }

    public void dynamicScannerEnhanced1Quarterly() {
        //  List<Stock> stocks = stockService.getActiveStocks();
        List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<Double> levels = new ArrayList<>();
        int year = 2025;
        List<LocalDate> sessionDates = new ArrayList<>();

        sessionDates.add(YearMonth.of(year, Month.MARCH).atEndOfMonth());

        sessionDates.add(YearMonth.of(year, Month.JUNE).atEndOfMonth());

        sessionDates.add(YearMonth.of(year, Month.SEPTEMBER).atEndOfMonth());

        sessionDates.add(YearMonth.of(year, Month.DECEMBER).atEndOfMonth());

        Map<String, String> hh = new HashMap<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }
            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(
                            YEARLY, stock, YearMonth.of(year - 1, Month.DECEMBER).atEndOfMonth());
            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(
                            YEARLY, stock, YearMonth.of(year - 1, Month.DECEMBER).atEndOfMonth());

            for (LocalDate sessionDate : sessionDates) {

                if (sessionDate.isAfter(LocalDate.now())) {
                    continue;
                }

                StockPrice stockPriceQuarterly =
                        updatePriceService.buildBack(QUARTERLY, stock, sessionDate);

                StockTechnicals stockTechnicalsQuarterly =
                        updateTechnicalsService.buildBack(QUARTERLY, stock, sessionDate);

                double mcap = fundamentalResearchService.marketCap(stockPriceQuarterly);

                if (mcap < 750 || mcap > 50_000) {
                    continue;
                }

                if (!this.isValidSetup(stockPriceQuarterly, stockTechnicalsQuarterly)) {
                    continue;
                }

                this.addLevels(stockPriceQuarterly, levels);

                // Get first day of next month
                LocalDate firstDayOfNextMonth =
                        sessionDate.with(TemporalAdjusters.firstDayOfNextMonth());

                // Find the first Monday in that month
                LocalDate sessionDateStart =
                        firstDayOfNextMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

                // Get last day of quarter
                LocalDate lastDayOfNextQuarter =
                        firstDayOfNextMonth
                                .with(firstDayOfNextMonth.getMonth().firstMonthOfQuarter())
                                .plusMonths(2)
                                .with(TemporalAdjusters.lastDayOfMonth());

                // Find the last Friday in that month
                LocalDate sessionDateEnd =
                        lastDayOfNextQuarter.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));

                for (LocalDate sessionDateMonday = sessionDateStart;
                        !sessionDateMonday.isAfter(sessionDateEnd)
                                && !sessionDateMonday.isAfter(LocalDate.now());
                        sessionDateMonday = sessionDateMonday.plusWeeks(1)) {
                    // Get last session date of previous week
                    LocalDate sessionDateWeekly =
                            calendarService.previousTradingSession(sessionDateMonday);

                    // Get last session date of next week
                    LocalDate sessionDateNextWeekly =
                            calendarService.previousTradingSession(sessionDateMonday.plusWeeks(1));

                    StockPrice stockPriceWeekly =
                            updatePriceService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    StockTechnicals stockTechnicalsWeekly =
                            updateTechnicalsService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    List<Double> levelsToCheck =
                            this.removeLevelsGreaterThanClose(levels, stockPriceWeekly.getClose());

                    levelsToCheck =
                            this.removeLevelsLessThanClose(
                                    levelsToCheck,
                                    Math.min(
                                            stockPriceWeekly.getPrevLow(),
                                            stockPriceWeekly.getLow()));

                    for (Double level : levels) {

                        // Rule 1 level Breakout

                        boolean isLevelBreakout =
                                stockPriceWeekly.getPrevClose() <= level
                                        && stockPriceWeekly.getClose() > level;

                        // Rule 2 level Rejected
                        boolean isLevelRejected =
                                stockPriceWeekly.getLow() <= level
                                        && stockPriceWeekly.getClose() > level;

                        if (isLevelBreakout || isLevelRejected) {
                            long volumeWeekly = stockTechnicalsWeekly.getVolume();
                            if (volumeWeekly > stockTechnicalsWeekly.getPrevVolume()
                                    && volumeWeekly > stockTechnicalsWeekly.getVolumeAvg20()) {
                                StockPrice stockPriceNextWeek =
                                        updatePriceService.buildBack(
                                                DAILY, stock, sessionDateNextWeekly);
                                double gain =
                                        formulaService.calculateChangePercentage(
                                                stockPriceWeekly.getClose(),
                                                stockPriceNextWeek.getClose());
                                String result =
                                        sessionDate
                                                + ", "
                                                + sessionDateWeekly
                                                + ", "
                                                + stock.getNseSymbol()
                                                + ", "
                                                + level
                                                + ", "
                                                + gain;
                                results.add(result);
                                System.out.println("Found it " + result);
                                break;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("-----results-----");
        results.forEach(System.out::println);
    }

    public void dynamicScannerEnhanced1FindWeekly() {
        //   List<Stock> stocks = stockService.getActiveStocks();
        //  List<Stock> stocks = stockService.getForActivity();

        List<Stock> stocks = new ArrayList<>();

        Stock testStock = stockService.getStockByNseSymbol("CUPID");
        stocks.add(testStock);
        testStock = stockService.getStockByNseSymbol("KRISHANA");
        stocks.add(testStock);

        List<String> results = new ArrayList<>();

        List<Double> levels = new ArrayList<>();
        int year = 2025;
        List<LocalDate> sessionDates = new ArrayList<>();
        sessionDates.add(YearMonth.of(year, Month.JANUARY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.FEBRUARY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.MARCH).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.APRIL).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.MAY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.JUNE).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.JULY).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.AUGUST).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.SEPTEMBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.OCTOBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.NOVEMBER).atEndOfMonth());
        sessionDates.add(YearMonth.of(year, Month.DECEMBER).atEndOfMonth());

        Map<String, String> hh = new HashMap<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }

            for (LocalDate sessionDate : sessionDates) {
                // Get first day of next month
                LocalDate firstDayOfNextMonth =
                        sessionDate.with(TemporalAdjusters.firstDayOfNextMonth());

                // Find the first Monday in that month
                LocalDate sessionDateStart =
                        firstDayOfNextMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

                // Get last day of next month
                LocalDate lastDayOfNextMonth =
                        firstDayOfNextMonth.with(TemporalAdjusters.lastDayOfMonth());

                // Find the last Friday in that month
                LocalDate sessionDateEnd =
                        lastDayOfNextMonth.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));

                for (LocalDate sessionDateMonday = sessionDateStart;
                        !sessionDateMonday.isAfter(sessionDateEnd)
                                && !sessionDateMonday.isAfter(LocalDate.now());
                        sessionDateMonday = sessionDateMonday.plusWeeks(1)) {
                    // Get last session date of previous week
                    LocalDate sessionDateWeekly =
                            calendarService.previousTradingSession(sessionDateMonday);

                    // Get last session date of next week
                    LocalDate sessionDateNextWeekly =
                            calendarService.previousTradingSession(sessionDateMonday.plusWeeks(1));

                    StockPrice stockPriceWeekly =
                            updatePriceService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    StockTechnicals stockTechnicalWeekly =
                            updateTechnicalsService.buildBack(WEEKLY, stock, sessionDateWeekly);

                    StockPrice stockPriceMonthly =
                            updatePriceService.buildBack(
                                    MONTHLY,
                                    stock,
                                    sessionDateWeekly
                                            .with(TemporalAdjusters.firstDayOfMonth())
                                            .minusDays(1));

                    StockTechnicals stockTechnicalMonthly =
                            updateTechnicalsService.buildBack(
                                    MONTHLY,
                                    stock,
                                    sessionDateWeekly
                                            .with(TemporalAdjusters.firstDayOfMonth())
                                            .minusDays(1));

                    StockPrice stockPriceQuarterly =
                            updatePriceService.buildBack(
                                    MONTHLY,
                                    stock,
                                    sessionDateWeekly
                                            .with(
                                                    sessionDateWeekly
                                                            .getMonth()
                                                            .firstMonthOfQuarter())
                                            .with(TemporalAdjusters.firstDayOfMonth())
                                            .minusDays(1));

                    StockTechnicals stockTechnicalQuarterly =
                            updateTechnicalsService.buildBack(
                                    MONTHLY,
                                    stock,
                                    sessionDateWeekly
                                            .with(
                                                    sessionDateWeekly
                                                            .getMonth()
                                                            .firstMonthOfQuarter())
                                            .with(TemporalAdjusters.firstDayOfMonth())
                                            .minusDays(1));

                    StockPrice stockPriceYearly =
                            updatePriceService.buildBack(
                                    MONTHLY,
                                    stock,
                                    sessionDateWeekly
                                            .with(TemporalAdjusters.firstDayOfYear())
                                            .minusDays(1));

                    StockTechnicals stockTechnicalYearly =
                            updateTechnicalsService.buildBack(
                                    MONTHLY,
                                    stock,
                                    sessionDateWeekly
                                            .with(TemporalAdjusters.firstDayOfYear())
                                            .minusDays(1));

                    double mcap = fundamentalResearchService.marketCap(stockPriceWeekly);

                    if (mcap < 750 || mcap > 50_000) {
                        continue;
                    }

                    if (!this.isValidSetup(stockPriceWeekly, stockTechnicalWeekly)) {
                        continue;
                    }

                    double support = SupportFinder.findSupport(stockPriceWeekly, 11);

                    if (support > 0.0) {

                        //  if (CandleStickUtils.isGreen(stockPriceWeekly) ||
                        // candleStickService.isHammer(stockPriceWeekly)  ) {

                        double lowWeekly = stockPriceWeekly.getLow();
                        double closeWeekly = stockPriceWeekly.getClose();
                        double supportMonthly = SupportFinder.findSupport(stockPriceMonthly, 11);
                        boolean isSupportMonthly =
                                lowWeekly <= supportMonthly && closeWeekly > supportMonthly;
                        double supportQuarterly = SupportFinder.findSupport(stockPriceQuarterly, 8);
                        boolean isSupportQuarterly =
                                lowWeekly <= supportQuarterly && closeWeekly > supportQuarterly;
                        double supportYearly = SupportFinder.findSupport(stockPriceQuarterly, 3);
                        boolean isSupportYearly =
                                lowWeekly <= supportYearly && closeWeekly > supportYearly;
                        String result =
                                sessionDate
                                        + ", "
                                        + sessionDateWeekly
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + support
                                        + ", "
                                        + (supportMonthly > 0.0)
                                        + ", "
                                        + isSupportMonthly
                                        + ", "
                                        + (supportQuarterly > 0.0)
                                        + ", "
                                        + isSupportQuarterly
                                        + ", "
                                        + (supportYearly > 0.0)
                                        + ", "
                                        + isSupportYearly;
                        System.out.println("Found it " + result);
                        results.add(result);
                        // }
                    }
                }
            }
        }
        System.out.println("-----results-----");
        results.forEach(System.out::println);
    }

    private void printAnalysisResult(WeeklyAnalysisResult result) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("WEEKLY ANALYSIS REPORT");
        System.out.println("=".repeat(60));

        StockPrice weekly = result.getWeeklyCandle();
        System.out.printf("Symbol: %s\n", weekly.getStock().getNseSymbol());
        System.out.printf("Date: %s\n", weekly.getSessionDate());
        System.out.printf(
                "Close: %.2f | High: %.2f | Low: %.2f\n",
                weekly.getClose(), weekly.getHigh(), weekly.getLow());

        System.out.println("\n" + "-".repeat(60));
        System.out.println(result.getSummary());

        System.out.println("\n" + "-".repeat(60));
        System.out.println("STRONG INTERACTIONS:");
        for (LevelInteraction interaction : result.getStrongInteractions()) {
            System.out.printf(
                    "  %s @ %.2f (Strength: %.1f/5)\n",
                    interaction.getLevel().getLevelType(),
                    interaction.getLevel().getPrice(),
                    interaction.getStrength());
        }

        System.out.println("\n" + "-".repeat(60));
        System.out.println("TOP SUPPORT LEVELS:");
        result.getSupportLevels().stream()
                .limit(3)
                .forEach(
                        level ->
                                System.out.printf(
                                        "  %.2f (%s)\n", level.getPrice(), level.getLevelType()));

        System.out.println("\nTOP RESISTANCE LEVELS:");
        result.getResistanceLevels().stream()
                .limit(3)
                .forEach(
                        level ->
                                System.out.printf(
                                        "  %.2f (%s)\n", level.getPrice(), level.getLevelType()));

        System.out.println("=".repeat(60));
    }

    public double correctionFromSwingHigh(StockPrice stockPrice, double yearHigh) {
        List<Double> highs = new ArrayList<>();
        highs.add(yearHigh);
        for (int i = 0; i < 12; i++) {
            highs.add(stockPrice.getHigh(i));
        }

        Collections.sort(highs, Collections.reverseOrder());
        double swingHigh = highs.get(0);
        return formulaService.calculateChangePercentage(swingHigh, stockPrice.getClose());
    }

    public boolean isValidSetup(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double mcap = fundamentalResearchService.marketCap(stockPrice);

        if (!(mcap >= 750 && mcap <= 50_000)) {
            return false;
        }

        if (!(this.isInititalValidated(stockPrice) && this.isInititalValidated(stockPrice))) {
            return false;
        }

        return true;
    }

    public boolean isVolumeAboveAverage(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        int counter = 0;
        int heavyCounter = 0;
        for (int i = 0; i < 12; i++) {
            if (stockTechnicals.getVolume(i) > stockTechnicals.getVolumeAvg20(i)) {
                counter++;
            }
            if (stockTechnicals.getVolume(i) > stockTechnicals.getVolumeAvg20(i) * 1.5) {
                heavyCounter++;
            }
        }

        return counter >= 2 && heavyCounter >= 1;
    }

    public boolean isLowestBreakout(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();

        // Get all EMA values
        double[] emaValues = {
            stockTechnicals.getEma5(),
            stockTechnicals.getEma20(),
            stockTechnicals.getEma50(),
            MovingAverageUtil.getMovingAverage100(stockPrice.getTimeframe(), stockTechnicals),
            MovingAverageUtil.getMovingAverage200(stockPrice.getTimeframe(), stockTechnicals)
        };

        double[] prevEmaValues = {
            stockTechnicals.getPrevEma5(),
            stockTechnicals.getPrevEma20(),
            stockTechnicals.getPrevEma50(),
            MovingAverageUtil.getPrevMovingAverage100(stockPrice.getTimeframe(), stockTechnicals),
            MovingAverageUtil.getPrevMovingAverage200(stockPrice.getTimeframe(), stockTechnicals)
        };

        // Find index of lowest EMA
        int lowestIndex = 0;
        for (int i = 1; i < emaValues.length; i++) {
            if (emaValues[i] < emaValues[lowestIndex]) {
                lowestIndex = i;
            }
        }

        // Get the lowest EMA and its previous value
        double lowestEMA = emaValues[lowestIndex];
        double prevLowestEMA = prevEmaValues[lowestIndex];

        // Check breakout
        return prevClose < prevLowestEMA && close > lowestEMA;
    }

    public boolean isLevelRespected(double level, StockPrice stockPrice) {
        int totalCount = 0;
        int consecutiveStreak = 0;
        boolean foundTwoConsecutive = false;

        // Check current and previous 11 sessions (total 12 sessions)
        for (int i = 0; i < 12; i++) {
            double low = stockPrice.getLow(i);
            double close = Math.min(stockPrice.getClose(i), stockPrice.getOpen(i));
            double prevClose = Math.max(stockPrice.getClose(i), stockPrice.getOpen(i));

            boolean isSupport = low <= level && close >= level;
            boolean isBreakout = prevClose < level && close > level;

            int threshold = 2;
            if (stockPrice.getTimeframe() == MONTHLY) {
                threshold = 1;
            }
            if (isSupport || isBreakout) {
                totalCount++;
                consecutiveStreak++;

                // If we find 2 in a row, mark it
                if (consecutiveStreak >= threshold) {
                    foundTwoConsecutive = true;
                }

                // Early exit: if we found 3 total and we have consecutive, return true
                if (foundTwoConsecutive && totalCount >= threshold + 1) {
                    return true;
                }
            } else {
                consecutiveStreak = 0;
            }
        }

        // After checking all sessions:
        // If we found consecutive rejections, need total >= 3
        // If no consecutive rejections, need total >= 2
        if (foundTwoConsecutive) {
            return totalCount >= 3;
        } else {
            return totalCount >= 2;
        }
    }

    public OptionalDouble findLatestSwingHigh(StockPrice sp, int maxLookback) {
        int max = Math.min(maxLookback, 11);

        for (int i = 1; i < max; i++) {

            double leftHigh = sp.getHigh(i + 1);
            double midHigh = sp.getHigh(i);
            double rightHigh = sp.getHigh(i - 1);

            if (midHigh > leftHigh && midHigh > rightHigh) {
                return OptionalDouble.of(midHigh);
            }
        }

        return OptionalDouble.empty();
    }

    private boolean isAtBottom(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        return (stockTechnicals.getEma5() <= stockTechnicals.getEma20()
                        || stockTechnicals.getEma20() == 0.0)
                && (stockTechnicals.getEma20() <= stockTechnicals.getEma50()
                        || stockTechnicals.getEma50() == 0.0)
                && stockPrice.getClose() <= stockTechnicals.getEma5();
    }

    public void monthlyScanner() {
        List<Stock> stocks = stockService.getActiveStocks();
        //    List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        sessionDates.add(LocalDate.of(2025, 1, 31));
        sessionDates.add(LocalDate.of(2025, 2, 28));
        sessionDates.add(LocalDate.of(2025, 3, 31));
        sessionDates.add(LocalDate.of(2025, 4, 30));
        sessionDates.add(LocalDate.of(2025, 5, 31));
        sessionDates.add(LocalDate.of(2025, 6, 30));
        sessionDates.add(LocalDate.of(2025, 7, 31));
        sessionDates.add(LocalDate.of(2025, 8, 31));
        sessionDates.add(LocalDate.of(2025, 9, 30));
        sessionDates.add(LocalDate.of(2025, 10, 31));
        sessionDates.add(LocalDate.of(2025, 11, 30));

        List<String> yearLowSupport = new ArrayList<>();
        List<String> prevYearHighSupport = new ArrayList<>();
        List<String> yearLowSupportConfirmed = new ArrayList<>();
        List<String> prevYearHighSupportConfirmed = new ArrayList<>();

        List<Double> levels = new ArrayList<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }

            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(
                            Timeframe.YEARLY,
                            stock,
                            LocalDate.now().minusYears(1).withDayOfYear(1).minusDays(1));
            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(
                            Timeframe.YEARLY,
                            stock,
                            LocalDate.now().minusYears(1).withDayOfYear(1).minusDays(1));

            double range =
                    formulaService.calculateChangePercentage(
                            stockPriceYearly.getLow(), stockPriceYearly.getHigh());
            double prevRange =
                    formulaService.calculateChangePercentage(
                            stockPriceYearly.getPrevLow(), stockPriceYearly.getPrevHigh());

            if (range < 38.2 || prevRange < 38.2) {
                continue;
            }

            if (range > 100.0 || prevRange > 100.0) {
                continue;
            }

            // System.out.println("logger2 " + stock.getNseSymbol());
            boolean isRedYearly = CandleStickUtils.isRed(stockPriceYearly);

            boolean isPrevRedYearly = CandleStickUtils.isPrevSessionRed(stockPriceYearly);

            if (CandleStickUtils.isLowerLow(stockPriceYearly)
                    && CandleStickUtils.isLowerHigh(stockPriceYearly)) {
                continue;
            }

            /*
            if(CandleStickUtils.upperWickSize(stockPriceYearly) > CandleStickUtils.bodySize(stockPriceYearly) * 3){
                continue;
            }*/

            // System.out.println("logger21 " + this.buildOHLCVStr(stockPriceYearly));
            /*
            if(isRedYearly && isPrevRedYearly){
                continue;
            }
             */
            //  System.out.println("logger3 " + stock.getNseSymbol());
            this.addLevels(stockPriceYearly, levels);

            for (LocalDate sessionDate : sessionDates) {

                StockPrice stockPriceMonthly =
                        updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicalsMonthly =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                double mcap = fundamentalResearchService.marketCap(stockPriceMonthly);

                MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);

                if (mcap < 750 || mcap > 50000) {
                    continue;
                }

                if (!(this.isInititalValidated(stockPriceMonthly)
                        && this.isInititalValidated(stockTechnicalsMonthly))) {
                    continue;
                }

                if (stockPriceMonthly.getOpen() > stockTechnicalsMonthly.getEma5()
                        && stockPriceMonthly.getClose() < stockTechnicalsMonthly.getEma5()
                        && stockTechnicalsMonthly.getEma5() > stockTechnicalsMonthly.getEma20()) {
                    continue;
                }

                if (CandleStickUtils.isRed(stockPriceMonthly)
                        && formulaService.calculateAbsChangePercentage(
                                        stockPriceMonthly.getPrevClose(),
                                        stockPriceMonthly.getClose())
                                > 10.0) {
                    continue;
                }

                if (stockPriceMonthly.getHigh() > stockTechnicalsMonthly.getEma5()
                        && stockPriceMonthly.getClose() < stockTechnicalsMonthly.getEma5()
                        && CandleStickUtils.upperWickSize(stockPriceMonthly)
                                > CandleStickUtils.lowerWickSize(stockPriceMonthly)
                        && stockTechnicalsMonthly.getEma5() > stockTechnicalsMonthly.getEma20()) {
                    continue;
                }

                if (this.isMAAlign(stockTechnicalsMonthly)
                        && stockPriceMonthly.getClose() > stockTechnicalsMonthly.getEma5()) {
                    continue;
                }

                double closeMonthly = stockPriceMonthly.getClose();
                double lowMonthly = stockPriceMonthly.getLow();

                List<Double> levelsToCheck =
                        this.removeLevelsGreaterThanClose(levels, closeMonthly);

                levelsToCheck = this.removeLevelsLessThanClose(levelsToCheck, lowMonthly);

                System.out.println("logger1 " + stock.getNseSymbol() + " " + levelsToCheck);

                boolean isGreen = CandleStickUtils.isGreen(stockPriceMonthly);
                boolean isHammer = candleStickService.isHammer(stockPriceMonthly);
                boolean isShootingStar = candleStickService.isShootingStar(stockPriceMonthly);

                for (Double level : levelsToCheck) {
                    // (isGreen || isHammer || isShootingStar) &&

                    boolean isLevelRespected = this.isLevelRespected(level, stockPriceMonthly);
                    boolean isSupportAtLows = this.isSupportAtLows(stockPriceMonthly);
                    System.out.println(
                            "logger2 "
                                    + stock.getNseSymbol()
                                    + " "
                                    + isLevelRespected
                                    + " "
                                    + isSupportAtLows);
                    double score =
                            this.score(
                                    stockPriceMonthly,
                                    stockTechnicalsMonthly,
                                    stockPriceYearly.getLow(),
                                    stockPriceYearly.getHigh());

                    if (isLevelRespected && isSupportAtLows && score > 65.0) {
                        System.out.println("Respected " + level + " " + stock.getNseSymbol());
                        double entryPrice =
                                formulaService.applyPercentChange(
                                        this.entryPrice(stockPriceMonthly), 2.0);

                        // volume Boost
                        double close = stockPriceMonthly.getClose();

                        if (close < stockTechnicalsMonthly.getEma5()
                                && close < stockTechnicalsMonthly.getEma20()
                                && (stockTechnicalsMonthly.getEma50() == 0.0
                                        || close < stockTechnicalsMonthly.getEma50())) {

                            if (stockTechnicalsMonthly.getVolume()
                                    > stockTechnicalsMonthly.getVolumeAvg20()) {
                                entryPrice = formulaService.applyPercentChange(entryPrice, 2.0);
                            }
                        }

                        // entryPrice = (level + entryPrice)/2;

                        StockPrice stockPriceLive = stockPriceService.get(stock, DAILY);
                        double gain =
                                formulaService.calculateChangePercentage(
                                        entryPrice, stockPriceLive.getClose());

                        String str =
                                sessionDate
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + score
                                        + ", "
                                        + level
                                        + ", "
                                        + formulaService.ceilToNearestHalf(entryPrice)
                                        + ", "
                                        + gain
                                        + ", "
                                        + levelsToCheck;
                        yearLowSupport.add(str);
                        System.out.println("Found it " + str);
                        break;
                    }
                }
            }
        }
        System.out.println("-----yearLowSupport-----");
        yearLowSupport.forEach(System.out::println);
        System.out.println("-----yearLowSupportConfirmed-----");
        yearLowSupportConfirmed.forEach(System.out::println);
        System.out.println("-----prevYearHighSupport-----");
        prevYearHighSupport.forEach(System.out::println);
        System.out.println("-----prevYearHighSupportConfirmed-----");
        prevYearHighSupportConfirmed.forEach(System.out::println);
    }

    private double entryPrice(StockPrice stockPrice) {
        if (CandleStickUtils.isRed(stockPrice)) {
            if (CandleStickUtils.lowerWickSize(stockPrice)
                            > CandleStickUtils.upperWickSize(stockPrice) * 2
                    && CandleStickUtils.lowerWickSize(stockPrice)
                            > CandleStickUtils.bodySize(stockPrice)) {
                return (stockPrice.getClose() + stockPrice.getLow()) / 2;
            }
            return stockPrice.getLow();
        }

        if (formulaService.calculateChangePercentage(
                        stockPrice.getPrevClose(), stockPrice.getClose())
                > 10.0) {
            return (stockPrice.getOpen() + stockPrice.getClose()) / 2;
        }

        return stockPrice.getOpen();
    }

    public void monthlyScanner2() {
        //  List<Stock> stocks = stockService.getActiveStocks();
        List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        LocalDate yearEndDate = LocalDate.of(2024, 12, 31);

        sessionDates.add(yearEndDate.with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(3).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(4).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(5).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(6).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(7).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(8).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(9).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(10).with(TemporalAdjusters.lastDayOfMonth()));

        sessionDates.add(yearEndDate.plusMonths(11).with(TemporalAdjusters.lastDayOfMonth()));
        sessionDates.add(yearEndDate.plusMonths(12).with(TemporalAdjusters.lastDayOfMonth()));

        List<String> yearLowSupport = new ArrayList<>();

        List<Double> yearlyLevels = new ArrayList<>();
        List<Double> monthlyLevels = new ArrayList<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }

            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(
                            Timeframe.YEARLY, stock, LocalDate.now().withDayOfYear(1).minusDays(1));
            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(
                            Timeframe.YEARLY, stock, LocalDate.now().withDayOfYear(1).minusDays(1));

            //  System.out.println(stock.getNseSymbol()+ ", Low: " + stockPriceYearly.getLow()+",
            // High: " + stockPriceYearly.getHigh() +" [" +
            // this.buildOHLCVStr(stockPriceYearly)+"]");

            for (LocalDate sessionDate : sessionDates) {

                LocalDate sessionDateCurrent =
                        calendarService.previousTradingSession(
                                sessionDate.plusMonths(2).withDayOfMonth(1));

                StockPrice stockPriceMonthly =
                        updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicalsMonthly =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                double mcap = fundamentalResearchService.marketCap(stockPriceMonthly);

                double monthlyChng =
                        formulaService.calculateChangePercentage(
                                stockPriceMonthly.getClose(), stockPriceMonthly.getPrevClose());

                double prevMonthlyChng =
                        formulaService.calculateChangePercentage(
                                stockPriceMonthly.getPrevClose(),
                                stockPriceMonthly.getPrev2Close());

                boolean isEitherMonthBelow10 = monthlyChng < 10 || prevMonthlyChng < 10;

                if (!isEitherMonthBelow10) {
                    continue;
                }

                MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);

                if (!(this.isInititalValidated(stockPriceMonthly)
                        && this.isInititalValidated(stockTechnicalsMonthly))) {
                    continue;
                }

                if (mcap < 1000) {
                    continue;
                }
                if (!this.isMAAlign(stockTechnicalsMonthly)) {
                    continue;
                }

                if (stockTechnicalsMonthly.getEma50() != 0
                        && stockTechnicalsMonthly.getEma20() < stockTechnicalsMonthly.getEma50()) {
                    continue;
                }

                double ema5Monthly = stockTechnicalsMonthly.getEma5();
                double ema20Monthly = stockTechnicalsMonthly.getEma20();
                double ema50Monthly = stockTechnicalsMonthly.getEma50();
                double lowMonthly = stockPriceMonthly.getLow();
                double closeMonthly = stockPriceMonthly.getClose();

                boolean isLowRejectedEma5 = lowMonthly < ema5Monthly && closeMonthly > ema5Monthly;
                boolean isLowRejectedEma20 =
                        lowMonthly < ema20Monthly && closeMonthly > ema20Monthly;
                boolean isLowRejectedEma50 =
                        lowMonthly < ema50Monthly && closeMonthly > ema50Monthly;

                boolean isLowRejected =
                        isLowRejectedEma5 || isLowRejectedEma20 || isLowRejectedEma50;
                boolean isGreen = CandleStickUtils.isGreen(stockPriceMonthly);
                boolean isPrevSessionGreen = CandleStickUtils.isPrevSessionGreen(stockPriceMonthly);
                boolean isPrev2SessionGreen =
                        CandleStickUtils.isPrev2SessionGreen(stockPriceMonthly);

                boolean isValidGreenSetup =
                        this.atLeastNTrue(1, isGreen, isPrevSessionGreen, isPrev2SessionGreen);

                if (!isValidGreenSetup) {
                    continue;
                }

                boolean isBothGreen = isGreen && isPrevSessionGreen;
                if (ema20Monthly != 0) {
                    double diff =
                            formulaService.calculateChangePercentage(ema20Monthly, closeMonthly);

                    if (diff > 30.0 && !isBothGreen) {
                        continue;
                    }
                }

                if ((isGreen || isPrevSessionGreen)
                        && candleStickService.isShootingStar(stockPriceMonthly)
                        && closeMonthly > ema5Monthly) {
                    continue;
                }

                if (!isLowRejected) {
                    continue;
                }

                if (isBothGreen) {
                    if (stockTechnicalsMonthly.getVolume()
                            > stockTechnicalsMonthly.getVolumeAvg20() * 2.25) {
                        continue;
                    }
                    if (stockTechnicalsMonthly.getVolume()
                            > stockTechnicalsMonthly.getPrevVolume() * 2.25) {
                        continue;
                    }
                    if (candleStickService.isPrevShootingStar(stockPriceMonthly)
                            && stockPriceMonthly.getPrevClose()
                                    > stockTechnicalsMonthly.getPrevEma5()) {
                        continue;
                    }
                }

                if (isBothGreen) {
                    if (!CandleStickUtils.isUpperWickWithinLimit(stockPriceMonthly, 20.0)) {
                        continue;
                    }
                }

                this.addLevels(stockPriceYearly, yearlyLevels);
                this.addLevels(stockPriceMonthly, monthlyLevels);

                LocalDate dailySession = calendarService.nextTradingSession(sessionDate);

                double upperWickSize = CandleStickUtils.upperWickSize(stockPriceMonthly);

                if (upperWickSize > CandleStickUtils.bodySize(stockPriceMonthly)
                        && upperWickSize > CandleStickUtils.lowerWickSize(stockPriceMonthly)
                        && !candleStickService.isDoji(stockPriceMonthly)) {
                    continue;
                }

                if (ema5Monthly < stockTechnicalsMonthly.getPrevEma5()) {
                    continue;
                }

                if (stockTechnicalsMonthly.getEma20() == 0.0) {
                    continue;
                }

                double resistance = ResistanceFinder.findResistance(stockPriceMonthly, 11);

                if (stockPriceMonthly.getClose() < resistance
                        && stockPriceMonthly.getHigh() >= resistance) {
                    if (CandleStickUtils.isGreen(stockPriceMonthly)) {
                        continue;
                    }

                    double absChngPct =
                            formulaService.calculateAbsChangePercentage(
                                    stockPriceMonthly.getPrevClose(), stockPriceMonthly.getClose());

                    if (CandleStickUtils.isRed(stockPriceMonthly) && absChngPct > 10) {
                        continue;
                    }

                    if (isPrevSessionGreen
                            && CandleStickUtils.isRed(stockPriceMonthly)
                            && stockPriceMonthly.getHigh() > stockPriceMonthly.getPrevHigh()) {
                        continue;
                    }

                    if (!CandleStickUtils.isUpperWickWithinLimit(stockPriceMonthly, 30.0)
                            && !CandleStickUtils.isPrevUpperWickWithinLimit(stockPriceMonthly, 30.0)
                            && !CandleStickUtils.isPrev2UpperWickWithinLimit(
                                    stockPriceMonthly, 30.0)) {
                        continue;
                    }
                }

                LocalDate dailySessionEnd = this.prev5thSession(sessionDateCurrent);

                while (!dailySession.isAfter(dailySessionEnd)) {

                    StockPrice stockPriceDaily =
                            updatePriceService.buildBack(Timeframe.DAILY, stock, dailySession);
                    StockTechnicals stockTechnicalsDaily =
                            updateTechnicalsService.buildBack(Timeframe.DAILY, stock, dailySession);

                    // if(this.isBearishMovingAverageAligned(stockTechnicalsDaily)) {

                    // if(this.isBounceAtMovingAverage(stockPriceDaily, stockTechnicalsDaily)) {

                    boolean isBounceAtLevelMonthly =
                            this.isBounceAtLevel(
                                    stockPriceDaily, stockTechnicalsDaily, monthlyLevels);
                    boolean isBounceAtLevelYearly =
                            this.isBounceAtLevel(
                                            stockPriceMonthly, stockTechnicalsMonthly, yearlyLevels)
                                    && this.isBounceAtLevel(
                                            stockPriceDaily, stockTechnicalsDaily, yearlyLevels);
                    if (isBounceAtLevelMonthly || isBounceAtLevelYearly) {

                        //   StockPrice stockPriceCurrent =
                        // updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDateCurrent);

                        //   StockPrice stockPriceLive = stockPriceService.get(stock, DAILY);
                        //   double gain =
                        // formulaService.calculateChangePercentage(stockPriceDaily.getClose(),
                        // stockPriceCurrent.getClose());
                        //  double liveGain =
                        // formulaService.calculateChangePercentage(stockPriceDaily.getClose(),
                        // stockPriceLive.getClose());
                        if (CandleStickUtils.isGreen(stockPriceDaily)
                                && CandleStickUtils.isPrevSessionRed(stockPriceDaily)) {
                            boolean isAllMaIncr =
                                    MovingAverageUtil.isAllMAsIncreasing(stockTechnicalsDaily);

                            boolean isHHHL =
                                    isBearishMovingAverageAligned(stockTechnicalsDaily)
                                            && CandleStickUtils.isHigherHigh(stockPriceDaily)
                                            && CandleStickUtils.isHigherLow(stockPriceDaily);

                            boolean isCloseAboveMonthlyEma20 =
                                    (stockTechnicalsMonthly.getEma20() != 0
                                                    && stockPriceDaily.getClose()
                                                            > stockTechnicalsMonthly.getEma20())
                                            || (stockTechnicalsMonthly.getEma5() != 0
                                                    && stockPriceDaily.getClose()
                                                            > stockTechnicalsMonthly.getEma5());

                            boolean isValidSetup =
                                    atLeastNTrue(2, isAllMaIncr, isHHHL, isCloseAboveMonthlyEma20);

                            boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPriceDaily);
                            boolean isPrev2Red =
                                    CandleStickUtils.isPrev2SessionRed(stockPriceDaily);
                            boolean isPrev3Red =
                                    CandleStickUtils.isPrev3SessionRed(stockPriceDaily);
                            boolean isPrev4Red =
                                    CandleStickUtils.isPrev4SessionRed(stockPriceDaily);
                            boolean isPrev5Red =
                                    CandleStickUtils.isPrev5SessionRed(stockPriceDaily);

                            boolean isValidTrend =
                                    atLeastNTrue(
                                            3,
                                            isPrevRed,
                                            isPrev2Red,
                                            isPrev3Red,
                                            isPrev4Red,
                                            isPrev5Red);

                            boolean isRecentEma5Breakout =
                                    isRecentEma5Breakout(stockPriceDaily, stockTechnicalsDaily);

                            if (isValidSetup && isValidTrend && isRecentEma5Breakout) {
                                StockPrice stockPriceMonthEnd =
                                        updatePriceService.buildBack(
                                                DAILY, stock, sessionDateCurrent);

                                double gain =
                                        formulaService.calculateChangePercentage(
                                                stockPriceDaily.getClose(),
                                                stockPriceMonthEnd.getClose());

                                StockPrice stockPriceCurrent = stockPriceService.get(stock, DAILY);
                                double gainCurrent =
                                        formulaService.calculateChangePercentage(
                                                stockPriceDaily.getClose(),
                                                stockPriceCurrent.getClose());

                                Map<StockScanner.ScoreMode, Double> scannerResult =
                                        StockScanner.evaluateStock(
                                                stockPriceMonthly,
                                                stockTechnicalsMonthly,
                                                stockPriceYearly.getLow(),
                                                stockPriceYearly.getHigh());
                                StockScanner.ScoreMode scoreMode = StockScanner.ScoreMode.None;
                                Double score = 0.0;
                                if (scannerResult.get(StockScanner.ScoreMode.Both) != null) {
                                    scoreMode = StockScanner.ScoreMode.Both;
                                    score = scannerResult.get(StockScanner.ScoreMode.Both);
                                } else if (scannerResult.get(StockScanner.ScoreMode.Mean_Reversion)
                                        != null) {
                                    scoreMode = StockScanner.ScoreMode.Mean_Reversion;
                                    score =
                                            scannerResult.get(
                                                    StockScanner.ScoreMode.Mean_Reversion);
                                } else if (scannerResult.get(
                                                StockScanner.ScoreMode.Trend_Continuation)
                                        != null) {
                                    scoreMode = StockScanner.ScoreMode.Trend_Continuation;
                                    score =
                                            scannerResult.get(
                                                    StockScanner.ScoreMode.Trend_Continuation);
                                }

                                String json =
                                        sessionDate
                                                + ", "
                                                + stock.getNseSymbol()
                                                + ", "
                                                + gain
                                                + ", "
                                                + gainCurrent
                                                + ", "
                                                + mcap
                                                + ", "
                                                + stockTechnicalsMonthly.getEma5()
                                                + ", "
                                                + stockTechnicalsMonthly.getPrevEma5()
                                                + ", "
                                                + stockTechnicalsMonthly.getPrev2Ema5()
                                                + ", "
                                                + stockTechnicalsMonthly.getEma20()
                                                + ", "
                                                + stockTechnicalsMonthly.getPrevEma20()
                                                + ", "
                                                + stockTechnicalsMonthly.getPrev2Ema20()
                                                + ", "
                                                + stockTechnicalsMonthly.getEma50()
                                                + ", "
                                                + stockTechnicalsMonthly.getPrevEma50()
                                                + ", "
                                                + stockTechnicalsMonthly.getPrev2Ema50()
                                                + ", "
                                                + MovingAverageUtil.getMovingAverage100(
                                                        MONTHLY, stockTechnicalsMonthly)
                                                + ", "
                                                + MovingAverageUtil.getPrevMovingAverage100(
                                                        MONTHLY, stockTechnicalsMonthly)
                                                + ", "
                                                + MovingAverageUtil.getPrev2MovingAverage100(
                                                        MONTHLY, stockTechnicalsMonthly)
                                                + ", "
                                                + MovingAverageUtil.getMovingAverage200(
                                                        MONTHLY, stockTechnicalsMonthly)
                                                + ", "
                                                + MovingAverageUtil.getPrevMovingAverage200(
                                                        MONTHLY, stockTechnicalsMonthly)
                                                + ", "
                                                + MovingAverageUtil.getPrev2MovingAverage200(
                                                        MONTHLY, stockTechnicalsMonthly)
                                                + ", "
                                                + stockTechnicalsMonthly.getRsi()
                                                + ", "
                                                + stockTechnicalsMonthly.getVolume()
                                                + ", "
                                                + stockTechnicalsMonthly.getPrevVolume()
                                                + ", "
                                                + stockTechnicalsMonthly.getVolumeAvg20()
                                                + ", "
                                                + stockPriceYearly.getLow()
                                                + ", "
                                                + stockPriceYearly.getHigh()
                                                + ", "
                                                + scannerResult;
                                String Header =
                                        "sessionDate"
                                                + ", "
                                                + "Symbol"
                                                + ", "
                                                + "nextMonthGain"
                                                + ", "
                                                + "gainCurrent"
                                                + ", "
                                                + "mcap"
                                                + ", "
                                                + "Ema5"
                                                + ", "
                                                + "PrevEma5"
                                                + ", "
                                                + "Prev2Ema5"
                                                + ", "
                                                + "Ema20()"
                                                + ", "
                                                + "PrevEma20"
                                                + ", "
                                                + "Prev2Ema20"
                                                + ", "
                                                + "Ema50"
                                                + ", "
                                                + "PrevEma50"
                                                + "Prev2Ema50"
                                                + ", "
                                                + ", "
                                                + "Ma100"
                                                + ", "
                                                + "PrevMa100"
                                                + "Prev2Ma100"
                                                + ", "
                                                + ", "
                                                + "Ma200"
                                                + ", "
                                                + "PrevMa200"
                                                + "Prev2Ma200"
                                                + ", "
                                                + ", "
                                                + "Rsi"
                                                + ", "
                                                + "Volume"
                                                + ", "
                                                + "PrevVolume"
                                                + ", "
                                                + "VolumeAvg20"
                                                + ", "
                                                + "yearLow"
                                                + ", "
                                                + "yearHigh"
                                                + ", "
                                                + "scannerResult";

                                System.out.println("Found strong monthly ");
                                System.out.println(Header);
                                System.out.println(json);
                                yearLowSupport.add(json);
                            }
                        }
                    }
                    // }
                    // }
                    dailySession = calendarService.nextTradingSession(dailySession);
                }
            }
        }

        System.out.println("-----yearLowSupport-----");
        yearLowSupport.forEach(System.out::println);
    }

    private boolean isRecentEma5Breakout(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice.getPrevClose() < stockTechnicals.getPrevEma5()
                && stockPrice.getClose() > stockTechnicals.getEma5()) {
            return true;
        } else if (stockPrice.getPrev2Close() < stockTechnicals.getPrev2Ema5()
                && stockPrice.getPrevClose() > stockTechnicals.getPrevEma5()) {
            return true;
        } else if (stockPrice.getPrev3Close() < stockTechnicals.getPrev3Ema5()
                && stockPrice.getPrev2Close() > stockTechnicals.getPrev2Ema5()) {
            return true;
        } /*else if(stockPrice.getPrev4Close() < stockTechnicals.getPrev4Ema5() && stockPrice.getPrev3Close() > stockTechnicals.getPrev3Ema5()){
              return true;
          }*/

        return false;
    }

    private boolean atLeastNTrue(int n, boolean... conditions) {
        int count = 0;
        for (boolean c : conditions) {
            if (c && ++count >= n) return true;
        }
        return false;
    }

    public LocalDate prev5thSession(LocalDate sessionDateCurrent) {

        LocalDate prev5thSession = calendarService.previousTradingSession(sessionDateCurrent);
        prev5thSession = calendarService.previousTradingSession(prev5thSession);
        prev5thSession = calendarService.previousTradingSession(prev5thSession);
        prev5thSession = calendarService.previousTradingSession(prev5thSession);
        prev5thSession = calendarService.previousTradingSession(prev5thSession);
        return prev5thSession;
    }

    public LocalDate tenthSession(LocalDate firstSession) {

        if (firstSession.getDayOfMonth() == 1) {
            return calendarService.previousTradingSession(firstSession.plusDays(20));
        } else if (firstSession.getDayOfMonth() == 2) {
            return calendarService.previousTradingSession(firstSession.plusDays(19));
        } else {
            return calendarService.previousTradingSession(firstSession.plusDays(18));
        }
    }

    private boolean isBearishMovingAverageAligned(StockTechnicals stockTechnicals) {

        double ema5 = stockTechnicals.getEma20();

        double ema20 = stockTechnicals.getEma20();

        double ema50 = stockTechnicals.getEma50();

        double ema100 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        double ema200 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        return ema5 <= ema20 && ema20 <= ema50; // && ema50 <= ema100 && ema100 <= ema200;
    }

    private boolean isMAAlign(StockTechnicals stockTechnicals) {

        double ema5 = stockTechnicals.getEma5();

        double ema20 = stockTechnicals.getEma20();

        double ema50 = stockTechnicals.getEma50();

        double ema100 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        double ema200 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        return ema5 >= ema20 && ema20 >= ema50 && ema50 >= ema100 && ema100 >= ema200;
    }

    private boolean isMAIncreasing(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double ema5 = stockTechnicals.getEma5();
        double prevEma5 = stockTechnicals.getPrevEma5();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double ema100 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma100 =
                MovingAverageUtil.getPrevMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        double ema200 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        // if(this.isMAAlign(stockTechnicals)){
        return ema5 >= prevEma5
                && ema20 >= prevEma20
                && ema50 >= prevEma50
                && ema100 >= prevEma100
                && ema200 >= prevEma200;
        // }

        // return false;
    }

    private boolean isBounceAtLevel(
            StockPrice stockPrice, StockTechnicals stockTechnicals, List<Double> levels) {

        Collections.sort(levels);

        for (double level : levels) {

            if (this.isLevelRejected(level, stockPrice)) {
                if (this.isBounceAtMovingAverage(stockPrice, stockTechnicals)) {
                    System.out.println(
                            stockPrice.getStock().getNseSymbol() + " bounced at " + level);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isBounceAtMovingAverage(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        int counter = 0;
        for (int i = 0; i <= 10; i++) {
            if (isMovingAverageRejected(
                    stockTechnicals.getEma5(i),
                    stockTechnicals.getEma5(i + 1),
                    stockPrice.getOpen(i),
                    stockPrice.getHigh(i),
                    stockPrice.getLow(i),
                    stockPrice.getClose(i),
                    stockPrice.getOpen(i + 1),
                    stockPrice.getHigh(i + 1),
                    stockPrice.getLow(i + 1),
                    stockPrice.getClose(i + 1))) {
                counter++;
            }
        }

        return counter > 1 && stockTechnicals.getEma5() > stockTechnicals.getPrevEma5();
    }

    private boolean isMovingAverageRejected(double prevLevel, double level, StockPrice stockPrice) {
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        return (close > level) && (low < level || prevClose < prevLevel);
    }

    private boolean isMovingAverageRejected(
            double prevLevel,
            double level,
            double open,
            double high,
            double low,
            double close,
            double prevOpen,
            double prevHigh,
            double prevLow,
            double prevClose) {
        return (close > level) && (low < level || prevClose < prevLevel);
    }

    private boolean isLevelRejected(double level, StockPrice stockPrice) {
        int counter = 0;
        for (int i = 0; i <= 10; i++) {
            if (isLevelRejected(
                    level,
                    stockPrice.getOpen(i),
                    stockPrice.getHigh(i),
                    stockPrice.getLow(i),
                    stockPrice.getClose(i),
                    stockPrice.getOpen(i + 1),
                    stockPrice.getHigh(i + 1),
                    stockPrice.getLow(i + 1),
                    stockPrice.getClose(i + 1))) {
                counter++;
            }
        }

        return counter > ((stockPrice.getTimeframe() == MONTHLY) ? 1 : 2);
    }

    private boolean isLevelRejected(
            double level,
            double open,
            double high,
            double low,
            double close,
            double prevOpen,
            double prevHigh,
            double prevLow,
            double prevClose) {

        return (close > level) && (low < level || prevClose < level);
    }

    private boolean isSupportAtLows(StockPrice stockPrice) {
        double support = SupportFinder.findSupport(stockPrice, 11);
        return stockPrice.getLow() <= support
                && Math.min(stockPrice.getOpen(), stockPrice.getClose()) > support;
    }

    private double score(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double yearLow,
            double yearHigh) {
        double score = 0.0;

        if (formulaService.calculateAbsChangePercentage(
                        stockPrice.getPrevClose(), stockPrice.getClose())
                < 5.0) {
            score = score + 5.0;
        }

        // If RSI increasing
        if (stockTechnicals.getRsi() > stockTechnicals.getPrevRsi()) {
            score = score + 10.0;
        }
        // IF EMA 20 > EMA 50
        if (stockTechnicals.getEma20() > stockTechnicals.getEma50()) {
            score = score + 15.0;
        }

        // if ema rejected or breakout
        double close = stockPrice.getClose();
        double low = stockPrice.getLow();
        double prevClose = stockPrice.getPrevClose();
        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double ema100 = stockTechnicals.getEma100();
        double prevEma100 = stockTechnicals.getPrevEma100();

        boolean isLowRejected =
                (low <= ema20 && close > ema20)
                        || (low <= ema50 && close > ema50)
                        || (low <= ema100 && close > ema100);

        boolean isBreakout =
                (prevClose <= prevEma20 && close > ema20)
                        || (prevClose <= prevEma50 && close > ema50)
                        || (prevClose <= prevEma100 && close > ema100);

        if (isLowRejected || isBreakout) {
            score = score + 20.0;
        }

        boolean isLowerLowStructure =
                (prevClose < stockPrice.getPrev2Low()
                        && stockPrice.getPrev2Close() < stockPrice.getPrev3Low());

        boolean isPrevLowerLowStructure =
                (stockPrice.getPrev2Close() < stockPrice.getPrev3Low()
                        && stockPrice.getPrev3Close() < stockPrice.getPrev4Low());

        if (isLowerLowStructure) {
            score = score + 15.0;
        }
        if (isPrevLowerLowStructure) {
            score = score + 10.0;
        }

        Map<StockScanner.ScoreMode, Double> scannerResult =
                StockScanner.evaluateStock(stockPrice, stockTechnicals, yearLow, yearHigh);
        StockScanner.ScoreMode scoreMode = StockScanner.ScoreMode.None;
        Double score2 = 0.0;
        if (scannerResult.get(StockScanner.ScoreMode.Both) != null) {
            scoreMode = StockScanner.ScoreMode.Both;
            score2 = scannerResult.get(StockScanner.ScoreMode.Both);
        } else if (scannerResult.get(StockScanner.ScoreMode.Mean_Reversion) != null) {
            scoreMode = StockScanner.ScoreMode.Mean_Reversion;
            score2 = scannerResult.get(StockScanner.ScoreMode.Mean_Reversion);
        } else if (scannerResult.get(StockScanner.ScoreMode.Trend_Continuation) != null) {
            scoreMode = StockScanner.ScoreMode.Trend_Continuation;
            score2 = scannerResult.get(StockScanner.ScoreMode.Trend_Continuation);
        } else if (scannerResult.get(StockScanner.ScoreMode.None) != null) {
            scoreMode = StockScanner.ScoreMode.None;
            score2 = scannerResult.get(StockScanner.ScoreMode.None);
        }

        if (score2 > 9.0) {
            score = score + 15.0;
        } else if (score2 > 8.0) {
            score = score + 10.0;
        } else if (score2 > 7.0) {
            score = score + 5.0;
        }

        if (close < ema20 && close < ema50 && close < ema100) {
            score = score + 20.0;
        } else if (close < ema20 && close < ema50) {
            score = score + 15.0;
        } else if (close < ema20) {
            score = score + 10.0;
        }

        // microcap Penalty
        double mcap = fundamentalResearchService.marketCap(stockPrice);

        if (mcap < 1000) {
            score = score - 10.0;
        }

        return Math.min(score, 100.0);
    }

    public static List<Double> removeLevelsGreaterThanClose(List<Double> levels, double close) {
        return levels.stream().filter(level -> level <= close).collect(Collectors.toList());
    }

    public static List<Double> removeLevelsLessThanClose(List<Double> levels, double low) {
        return levels.stream().filter(level -> level >= low).collect(Collectors.toList());
    }

    private void addLevels(StockPrice stockPrice, List<Double> levels) {
        levels.clear();

        boolean isGreen = CandleStickUtils.isGreen(stockPrice);

        double close = stockPrice.getClose();
        levels.add(close);

        if (isGreen) {
            double mid = (stockPrice.getOpen() + stockPrice.getClose()) / 2;
            levels.add(mid);
        }

        Collections.sort(levels);
    }

    public void dynamicScanner1Enhanced() {
        List<Stock> stocks = stockService.getActiveStocks();
        //   List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        sessionDates.add(LocalDate.of(2024, 12, 31));
        sessionDates.add(LocalDate.of(2025, 1, 31));
        sessionDates.add(LocalDate.of(2025, 2, 28));
        sessionDates.add(LocalDate.of(2025, 3, 31));
        sessionDates.add(LocalDate.of(2025, 4, 30));
        sessionDates.add(LocalDate.of(2025, 5, 31));
        sessionDates.add(LocalDate.of(2025, 6, 30));
        sessionDates.add(LocalDate.of(2025, 7, 31));
        sessionDates.add(LocalDate.of(2025, 8, 31));
        sessionDates.add(LocalDate.of(2025, 9, 30));
        sessionDates.add(LocalDate.of(2025, 10, 31));
        sessionDates.add(LocalDate.of(2025, 11, 30));
        sessionDates.add(LocalDate.of(2025, 12, 31));

        List<String> yearLowSupport = new ArrayList<>();

        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }

            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(
                            Timeframe.YEARLY, stock, LocalDate.now().withDayOfYear(1).minusDays(1));
            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(
                            Timeframe.YEARLY, stock, LocalDate.now().withDayOfYear(1).minusDays(1));

            boolean isGreen = CandleStickUtils.isGreen(stockPriceYearly);
            boolean isRed = CandleStickUtils.isRed(stockPriceYearly);
            boolean isPrevGreen = CandleStickUtils.isPrevSessionGreen(stockPriceYearly);

            boolean isValidYearlyScan =
                    isPrevGreen
                            && (CandleStickUtils.isPrevHigherHigh(stockPriceYearly)
                                    && CandleStickUtils.isPrevHigherLow(stockPriceYearly));

            /*
            if(!isValidYearlyScan){
                continue;
            }*/

            for (LocalDate sessionDate : sessionDates) {

                StockPrice stockPrice = updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);
                LocalDate sessionDateMonthEnd =
                        calendarService.previousTradingSession(
                                sessionDate.plusMonths(2).withDayOfMonth(1));
                double mcap = fundamentalResearchService.marketCap(stockPrice);
                MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);

                if (mcap < 1500) {
                    continue;
                }

                if (!(this.isInititalValidated(stockPrice)
                        && this.isInititalValidated(stockTechnicals))) {
                    continue;
                }

                double currentChngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPrice.getPrevClose(), stockPrice.getClose());
                double prevChngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPrice.getPrev2Close(), stockPrice.getPrevClose());
                double prev2ChngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPrice.getPrev3Close(), stockPrice.getPrev2Close());
                double prev3ChngPct =
                        formulaService.calculateAbsChangePercentage(
                                stockPrice.getPrev4Close(), stockPrice.getPrev3Close());

                boolean currentAndPrevSmallChng = currentChngPct <= 7.5 && prevChngPct <= 7.5;
                boolean currentAndPrev2SmallChng =
                        currentChngPct <= 7.5 && prevChngPct <= 7.5 && prev2ChngPct <= 7.5;

                boolean isCurrentGreen = CandleStickUtils.isGreen(stockPrice);
                boolean isPrev2Green = CandleStickUtils.isPrev2SessionGreen(stockPrice);
                boolean isPrev3Green = CandleStickUtils.isPrev3SessionGreen(stockPrice);

                boolean pattern1 = (isPrev2Green && prev2ChngPct > 10.0 && currentAndPrevSmallChng);

                boolean pattern2 =
                        (!isPrev2Green
                                        && prev2ChngPct > 10.0
                                        && currentChngPct > 10.0
                                        && prevChngPct <= 7.5
                                        && isCurrentGreen)
                                && stockPrice.getPrevClose() < stockTechnicals.getPrevEma20();

                boolean pattern3 =
                        (isPrev3Green && prev3ChngPct > 10.0 && currentAndPrev2SmallChng);

                if (pattern1 || pattern2 || pattern3) {
                    StockPrice stockPriceMonthEnd =
                            updatePriceService.buildBack(DAILY, stock, sessionDateMonthEnd);
                    double monthEndGain =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getClose(), stockPriceMonthEnd.getClose());
                    StockPrice stockPriceCurrent = stockPriceService.get(stock, DAILY);
                    double currentGain =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getClose(), stockPriceCurrent.getClose());
                    String json =
                            sessionDate
                                    + ", "
                                    + stock.getNseSymbol()
                                    + ", "
                                    + monthEndGain
                                    + ", "
                                    + currentGain
                                    + ", "
                                    + pattern1
                                    + ", "
                                    + pattern2
                                    + ", "
                                    + pattern3;
                    System.out.println("Found pattern " + json);
                    System.out.println(
                            StockScanner.evaluateStock(
                                    stockPrice,
                                    stockTechnicals,
                                    stockPriceYearly.getLow(),
                                    stockPriceYearly.getHigh()));
                    yearLowSupport.add(json);
                }
            }
        }
        System.out.println("-----yearLowSupport-----");
        yearLowSupport.forEach(System.out::println);
        System.out.println("-----yearLowSupportConfirmed-----");
    }

    boolean isStrongBullishMonthly(StockPrice mp, StockTechnicals mt) {

        boolean trendAlignment =
                mp.getClose(0) > mt.getEma20()
                        && mt.getEma20() > mt.getEma50()
                        && mt.getEma5() > mt.getEma20();

        boolean momentum =
                mt.getRsi() >= 55 && mt.getAdx() >= 25 && mt.getPlusDi() > mt.getMinusDi();

        boolean emaSlope =
                mt.getEma20() > mt.getPrevEma20() && mt.getPrevEma20() > mt.getPrev2Ema20();

        boolean candleQuality = upperWickPercent(mp) <= 40 && mp.getClose(0) >= mp.getPrevClose();

        boolean location = distance(mp.getClose(0), mt.getEma20()) <= 0.02; // ≤ 2%

        return trendAlignment
                // && momentum
                && emaSlope
                && candleQuality
                && location;
    }

    double upperWickPercent(StockPrice p) {
        double bodyHigh = Math.max(p.getOpen(0), p.getClose(0));
        return (p.getHigh(0) - bodyHigh) / (p.getHigh(0) - p.getLow(0));
    }

    double distance(double price, double level) {
        return Math.abs(price - level) / level;
    }

    public void dynamicScanner2Enhanced() {
        List<Stock> stocks = stockService.getActiveStocks();
        //    List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        // sessionDates.add(LocalDate.of(2024, 12, 31));
        sessionDates.add(LocalDate.of(2025, 1, 31));
        /*sessionDates.add(LocalDate.of(2025, 2, 28));
        sessionDates.add(LocalDate.of(2025, 3, 31));
        sessionDates.add(LocalDate.of(2025, 4, 30));
        sessionDates.add(LocalDate.of(2025, 5, 31));
        sessionDates.add(LocalDate.of(2025, 6, 30));
        sessionDates.add(LocalDate.of(2025, 7, 31));
        sessionDates.add(LocalDate.of(2025, 8, 31));
        sessionDates.add(LocalDate.of(2025, 9, 30));
        sessionDates.add(LocalDate.of(2025, 10, 31));
        sessionDates.add(LocalDate.of(2025, 11, 30));
        sessionDates.add(LocalDate.of(2025, 12, 31));*/

        List<String> yearLowSupport = new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (Stock stock : stocks) {

            if (!this.isInititalValidated(stock)) {
                continue;
            }

            StockPrice stockPriceYearly =
                    updatePriceService.buildBack(YEARLY, stock, LocalDate.of(2024, 12, 31));
            StockTechnicals stockTechnicalsYearly =
                    updateTechnicalsService.buildBack(YEARLY, stock, LocalDate.of(2024, 12, 31));

            for (LocalDate sessionDate : sessionDates) {

                StockPrice stockPrice = updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);
                LocalDate sessionDateMonthEnd =
                        calendarService.previousTradingSession(
                                sessionDate.plusMonths(2).withDayOfMonth(1));
                double mcap = fundamentalResearchService.marketCap(stockPrice);
                MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);

                if (mcap < 1000) {
                    continue;
                }

                if (!(this.isInititalValidated(stockPrice)
                        && this.isInititalValidated(stockTechnicals))) {
                    continue;
                }

                StockPrice stockPriceMonthEnd =
                        updatePriceService.buildBack(DAILY, stock, sessionDateMonthEnd);

                double gain =
                        formulaService.calculateChangePercentage(
                                stockPrice.getClose(), stockPriceMonthEnd.getClose());

                StockPrice stockPriceCurrent = stockPriceService.get(stock, DAILY);
                double gainCurrent =
                        formulaService.calculateChangePercentage(
                                stockPrice.getClose(), stockPriceCurrent.getClose());

                Map<StockScanner.ScoreMode, Double> scannerResult =
                        StockScanner.evaluateStock(
                                stockPrice,
                                stockTechnicals,
                                stockPriceYearly.getLow(),
                                stockPriceYearly.getHigh());
                StockScanner.ScoreMode scoreMode = StockScanner.ScoreMode.None;
                Double score = 0.0;
                if (scannerResult.get(StockScanner.ScoreMode.Both) != null) {
                    scoreMode = StockScanner.ScoreMode.Both;
                    score = scannerResult.get(StockScanner.ScoreMode.Both);
                } else if (scannerResult.get(StockScanner.ScoreMode.Mean_Reversion) != null) {
                    scoreMode = StockScanner.ScoreMode.Mean_Reversion;
                    score = scannerResult.get(StockScanner.ScoreMode.Mean_Reversion);
                } else if (scannerResult.get(StockScanner.ScoreMode.Trend_Continuation) != null) {
                    scoreMode = StockScanner.ScoreMode.Trend_Continuation;
                    score = scannerResult.get(StockScanner.ScoreMode.Trend_Continuation);
                }

                String json =
                        sessionDate
                                + ", "
                                + stock.getNseSymbol()
                                + ", "
                                + gain
                                + ", "
                                + gainCurrent
                                + ", "
                                + mcap
                                + ", "
                                + stockTechnicals.getEma5()
                                + ", "
                                + stockTechnicals.getPrevEma5()
                                + ", "
                                + stockTechnicals.getPrev2Ema5()
                                + ", "
                                + stockTechnicals.getEma20()
                                + ", "
                                + stockTechnicals.getPrevEma20()
                                + ", "
                                + stockTechnicals.getPrev2Ema20()
                                + ", "
                                + stockTechnicals.getEma50()
                                + ", "
                                + stockTechnicals.getPrevEma50()
                                + ", "
                                + stockTechnicals.getPrev2Ema50()
                                + ", "
                                + MovingAverageUtil.getMovingAverage100(MONTHLY, stockTechnicals)
                                + ", "
                                + MovingAverageUtil.getPrevMovingAverage100(
                                        MONTHLY, stockTechnicals)
                                + ", "
                                + MovingAverageUtil.getPrev2MovingAverage100(
                                        MONTHLY, stockTechnicals)
                                + ", "
                                + MovingAverageUtil.getMovingAverage200(MONTHLY, stockTechnicals)
                                + ", "
                                + MovingAverageUtil.getPrevMovingAverage200(
                                        MONTHLY, stockTechnicals)
                                + ", "
                                + MovingAverageUtil.getPrev2MovingAverage200(
                                        MONTHLY, stockTechnicals)
                                + ", "
                                + stockTechnicals.getRsi()
                                + ", "
                                + stockTechnicals.getVolume()
                                + ", "
                                + stockTechnicals.getPrevVolume()
                                + ", "
                                + stockTechnicals.getVolumeAvg20()
                                + ", "
                                + stockPriceYearly.getLow()
                                + ", "
                                + stockPriceYearly.getHigh()
                                + ", "
                                + scannerResult;
                String Header =
                        "sessionDate"
                                + ", "
                                + "Symbol"
                                + ", "
                                + "nextMonthGain"
                                + ", "
                                + "gainCurrent"
                                + ", "
                                + "mcap"
                                + ", "
                                + "Ema5"
                                + ", "
                                + "PrevEma5"
                                + ", "
                                + "Prev2Ema5"
                                + ", "
                                + "Ema20()"
                                + ", "
                                + "PrevEma20"
                                + ", "
                                + "Prev2Ema20"
                                + ", "
                                + "Ema50"
                                + ", "
                                + "PrevEma50"
                                + "Prev2Ema50"
                                + ", "
                                + ", "
                                + "Ma100"
                                + ", "
                                + "PrevMa100"
                                + "Prev2Ma100"
                                + ", "
                                + ", "
                                + "Ma200"
                                + ", "
                                + "PrevMa200"
                                + "Prev2Ma200"
                                + ", "
                                + ", "
                                + "Rsi"
                                + ", "
                                + "Volume"
                                + ", "
                                + "PrevVolume"
                                + ", "
                                + "VolumeAvg20"
                                + ", "
                                + "yearLow"
                                + ", "
                                + "yearHigh"
                                + ", "
                                + "scannerResult";

                System.out.println("Found strong monthly ");
                System.out.println(Header);
                System.out.println(json);
                yearLowSupport.add(json);
            }
        }

        String Header =
                "sessionDate"
                        + ", "
                        + "Symbol"
                        + ", "
                        + "nextMonthGain"
                        + ", "
                        + "gainCurrent"
                        + ", "
                        + "mcap"
                        + ", "
                        + "Ema5"
                        + ", "
                        + "PrevEma5"
                        + ", "
                        + "Prev2Ema5"
                        + ", "
                        + "Ema20()"
                        + ", "
                        + "PrevEma20"
                        + ", "
                        + "Prev2Ema20"
                        + ", "
                        + "Ema50"
                        + ", "
                        + "PrevEma50"
                        + "Prev2Ema50"
                        + ", "
                        + ", "
                        + "Ma100"
                        + ", "
                        + "PrevMa100"
                        + "Prev2Ma100"
                        + ", "
                        + ", "
                        + "Ma200"
                        + ", "
                        + "PrevMa200"
                        + "Prev2Ma200"
                        + ", "
                        + ", "
                        + "Rsi"
                        + ", "
                        + "Volume"
                        + ", "
                        + "PrevVolume"
                        + ", "
                        + "VolumeAvg20"
                        + ", "
                        + "yearLow"
                        + ", "
                        + "yearHigh"
                        + ", "
                        + "scannerResult";

        System.out.println("-----yearLowSupport-----");
        System.out.println(Header);
        yearLowSupport.forEach(System.out::println);
        System.out.println("-----yearLowSupportConfirmed-----");
        result.forEach(System.out::println);
    }

    public void dynamicScanner3() {
        List<Stock> stocks = stockService.getActiveStocks();
        // List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        sessionDates.add(LocalDate.of(2025, 1, 31));
        sessionDates.add(LocalDate.of(2025, 2, 28));
        sessionDates.add(LocalDate.of(2025, 3, 31));
        sessionDates.add(LocalDate.of(2025, 4, 30));
        sessionDates.add(LocalDate.of(2025, 5, 31));
        sessionDates.add(LocalDate.of(2025, 6, 30));
        sessionDates.add(LocalDate.of(2025, 7, 31));
        sessionDates.add(LocalDate.of(2025, 8, 31));
        sessionDates.add(LocalDate.of(2025, 9, 30));
        sessionDates.add(LocalDate.of(2025, 10, 31));
        sessionDates.add(LocalDate.of(2025, 11, 30));
        List<String> support = new ArrayList<>();
        List<String> breakout = new ArrayList<>();
        support.add(
                "sessionDate"
                        + ", "
                        + "symbol"
                        + ", "
                        + "length"
                        + ", "
                        + "pattern"
                        + ", "
                        + "close"
                        + ", "
                        + "high"
                        + ", "
                        + "resistance"
                        + ", "
                        + "gain"
                        + ", "
                        + "isCandleStickPattern"
                        + ", "
                        + "isPrevCandleStickPattern"
                        + ","
                        + "rsi"
                        + ","
                        + "volumeIncr"
                        + ","
                        + "chngPct"
                        + ","
                        + "mcap"
                        + ", ema5"
                        + ", ema10"
                        + ", ema20"
                        + ", ema50"
                        + ", ema100"
                        + ", ohlcvs");

        for (LocalDate sessionDate : sessionDates) {

            for (Stock stock : stocks) {

                if (!this.isInititalValidated(stock)) {
                    continue;
                }

                StockPrice stockPrice = updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                MarketCapCategory marketCapCategory =
                        MarketCapCategory.classify(
                                fundamentalResearchService.marketCap(stockPrice));

                if (!this.isInititalValidated(stockPrice)) {
                    continue;
                }

                if (!this.isInititalValidated(stockTechnicals)) {
                    continue;
                }

                if (stockTechnicals.getEma20() == 0.0) {
                    continue;
                }

                double ema5 = stockTechnicals.getEma5();
                double low = stockPrice.getLow();
                double close = stockPrice.getClose();
                boolean isEma5Rejected = low < ema5 && close > ema5;

                if (isEma5Rejected
                        && !candleStickService.isShootingStar(stockPrice)
                        && !candleStickService.isHangingMan(stockPrice)) {

                    StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);
                    double gain =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getClose(), stockPriceDaily.getClose());
                    boolean isBullishEngulfing = candleStickService.isBullishEngulfing(stockPrice);
                    boolean isPiercingPattern = candleStickService.isPiercingPattern(stockPrice);
                    boolean isTweezerBottom = candleStickService.isTweezerBottom(stockPrice);
                    boolean isDoubleBottom = candleStickService.isDoubleBottom(stockPrice);
                    boolean isBullishHarami = candleStickService.isBullishHarami(stockPrice);

                    // Confirmation Needed
                    boolean isHammer = candleStickService.isPrevHammer(stockPrice);
                    boolean isInvertedHammer = candleStickService.isPrevInvertedHammer(stockPrice);

                    boolean isDoji = candleStickService.isPrevDoji(stockPrice);
                    boolean isSpinningTop = candleStickService.isPrevSpinningTop(stockPrice);

                    boolean isCandleStickPattern =
                            isBullishEngulfing
                                    || isPiercingPattern
                                    || isBullishHarami
                                    || isTweezerBottom
                                    || isDoubleBottom;

                    boolean isPrevCandleStickPattern =
                            isHammer || isInvertedHammer || isDoji || isSpinningTop;

                    double chngPct =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getPrevClose(), stockPrice.getClose());

                    boolean isSmallBody = CandleStickUtils.isSmallBody(stockPrice, stockTechnicals);
                    boolean isPrevSmallBody =
                            CandleStickUtils.isPrevSmallBody(stockPrice, stockTechnicals);
                    if (this.isEmaAlign(stockTechnicals, MASupportChecker.MovingAverageLength.EMA5)
                            && marketCapCategory != MarketCapCategory.MICROCAP
                            && Math.floor(chngPct) < 10.0
                            && Math.floor(chngPct) > -10.0
                            && isSmallBody
                            && isPrevSmallBody) {

                        boolean isLowerLow = CandleStickUtils.isLowerLow(stockPrice);
                        boolean isLowRejected = stockPrice.getOpen() > stockPrice.getPrevLow();

                        //  if(isLowerLow && isLowRejected){
                        System.out.println(
                                sessionDate
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + MASupportChecker.MovingAverageLength.EMA5
                                        + ", "
                                        + stockPrice.getClose()
                                        + ", "
                                        + gain
                                        + ", "
                                        + isCandleStickPattern
                                        + ", "
                                        + isPrevCandleStickPattern
                                        + ","
                                        + chngPct);
                        // maInteractionList.forEach(System.out::println);
                        // double ema5 = stockTechnicals.getEma5();
                        double ema10 = stockTechnicals.getEma10();
                        double ema20 = stockTechnicals.getEma20();
                        double ema50 = stockTechnicals.getEma50();
                        double ema100 = stockTechnicals.getEma100();

                        String pattern = "NA";
                        if (isDoji) {
                            pattern = "Doji";
                        } else if (isSpinningTop) {
                            pattern = "Spining Top";
                        } else if (isHammer) {
                            pattern = "Hammer";
                        } else if (isInvertedHammer) {
                            pattern = "Inverted Hammer";
                        } else if (isBullishHarami) {
                            pattern = "Harami";
                        } else if (isBullishEngulfing) {
                            pattern = "Engulfing";
                        } else if (isPiercingPattern) {
                            pattern = "Piercing";
                        } else if (isTweezerBottom) {
                            pattern = "Tweezer";
                        } else if (isDoubleBottom) {
                            pattern = "Double Bottom";
                        }

                        if (MovingAverageUtil.isAllMAsIncreasing(stockTechnicals)) {

                            StringBuilder ohlcvas = new StringBuilder();
                            ohlcvas.append("[");
                            ohlcvas.append(stockPrice.getPrev6Open());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev6High());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev6Low());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev6Close());
                            ohlcvas.append("~");
                            ohlcvas.append(stockPrice.getPrev5Open());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev5High());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev5Low());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev5Close());
                            ohlcvas.append("~");
                            ohlcvas.append(stockPrice.getPrev4Open());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev4High());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev4Low());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev4Close());
                            ohlcvas.append("~");
                            ohlcvas.append(stockPrice.getPrev3Open());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev3High());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev3Low());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev3Close());
                            ohlcvas.append("~");
                            ohlcvas.append(stockPrice.getPrev2Open());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev2High());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev2Low());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrev2Close());
                            ohlcvas.append("~");
                            ohlcvas.append(stockPrice.getPrevOpen());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrevHigh());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrevLow());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getPrevClose());
                            ohlcvas.append("~");
                            ohlcvas.append(stockPrice.getOpen());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getHigh());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getLow());
                            ohlcvas.append("-");
                            ohlcvas.append(stockPrice.getClose());
                            ohlcvas.append("]");

                            double resistance = resistance(stockPrice);
                            support.add(
                                    sessionDate
                                            + ", "
                                            + stock.getNseSymbol()
                                            + ", "
                                            + MASupportChecker.MovingAverageLength.EMA5
                                            + ", "
                                            + pattern
                                            + ", "
                                            + stockPrice.getClose()
                                            + ", "
                                            + stockPrice.getHigh()
                                            + ", "
                                            + resistance
                                            + ", "
                                            + gain
                                            + ", "
                                            + isCandleStickPattern
                                            + ", "
                                            + isPrevCandleStickPattern
                                            + ","
                                            + stockTechnicals.getRsi()
                                            + ","
                                            + (stockTechnicals.getVolume()
                                                    > stockTechnicals.getPrevVolume())
                                            + ","
                                            + chngPct
                                            + ","
                                            + marketCapCategory
                                            + ","
                                            + ema5
                                            + ","
                                            + ema10
                                            + ","
                                            + ema20
                                            + ","
                                            + ema50
                                            + ","
                                            + ema100
                                            + ","
                                            + ohlcvas);
                        }
                    }
                }
            }
        }
        support.forEach(System.out::println);
        System.out.println("----------");
        breakout.forEach(System.out::println);
    }

    public void dynamicScanner4() {
        List<Stock> stocks = stockService.getActiveStocks();
        // List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        List<String> support = new ArrayList<>();
        List<String> breakout = new ArrayList<>();
        support.add(
                "sessionDate"
                        + ", "
                        + "symbol"
                        + ", "
                        + "length"
                        + ", "
                        + "pattern"
                        + ", "
                        + "close"
                        + ", "
                        + "high"
                        + ", "
                        + "resistance"
                        + ", "
                        + "gain"
                        + ", "
                        + "isCandleStickPattern"
                        + ", "
                        + "isPrevCandleStickPattern"
                        + ","
                        + "rsi"
                        + ","
                        + "volumeIncr"
                        + ","
                        + "chngPct"
                        + ","
                        + "mcap"
                        + ", ema5"
                        + ", ema10"
                        + ", ema20"
                        + ", ema50"
                        + ", ema100"
                        + ", ohlcvs");
        LocalDate sessionDate = LocalDate.of(2025, 11, 1);
        LocalDate sessionDateEnd = LocalDate.of(2025, 11, 30);
        while (!sessionDate.isAfter(sessionDateEnd)) {

            for (Stock stock : stocks) {

                if (!this.isInititalValidated(stock)) {
                    continue;
                }

                StockPrice stockPrice =
                        updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(Timeframe.DAILY, stock, sessionDate);

                MarketCapCategory marketCapCategory =
                        MarketCapCategory.classify(
                                fundamentalResearchService.marketCap(stockPrice));

                if (stockTechnicals.getEma20() == 0.0) {
                    continue;
                }

                Optional<MASupportChecker.MAInteraction> maInteractionOptional =
                        maSupportChecker.findSingleMASupport(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

                if (maInteractionOptional.isPresent()) {
                    StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);
                    double gain =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getClose(), stockPriceDaily.getClose());
                    boolean isBullishEngulfing = candleStickService.isBullishEngulfing(stockPrice);
                    boolean isPiercingPAttern = candleStickService.isPiercingPattern(stockPrice);
                    boolean isTweezerBottom = candleStickService.isTweezerBottom(stockPrice);
                    boolean isDoubleBottom = candleStickService.isDoubleBottom(stockPrice);
                    boolean isBullishHarami = candleStickService.isBullishHarami(stockPrice);

                    // Confirmation Needed
                    boolean isHammer = candleStickService.isPrevHammer(stockPrice);
                    boolean isInvertedHammer = candleStickService.isPrevInvertedHammer(stockPrice);

                    boolean isDoji = candleStickService.isPrevDoji(stockPrice);
                    boolean isSpinningTop = candleStickService.isPrevSpinningTop(stockPrice);

                    boolean isCandleStickPattern =
                            isBullishEngulfing
                                    || isPiercingPAttern
                                    || isBullishHarami
                                    || isTweezerBottom
                                    || isDoubleBottom;

                    boolean isPrevCandleStickPattern =
                            isHammer || isInvertedHammer || isDoji || isSpinningTop;

                    double chngPct =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getPrevClose(), stockPrice.getClose());

                    if (this.isEmaAlign(stockTechnicals, maInteractionOptional.get().getLength())
                            && marketCapCategory != MarketCapCategory.MICROCAP
                            && stockTechnicals.getRsi() <= 65.0) {
                        System.out.println(
                                sessionDate
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + maInteractionOptional.get().getLength()
                                        + ", "
                                        + stockPrice.getClose()
                                        + ", "
                                        + gain
                                        + ", "
                                        + isCandleStickPattern
                                        + ", "
                                        + isPrevCandleStickPattern
                                        + ","
                                        + chngPct);
                        // maInteractionList.forEach(System.out::println);
                        double ema5 = stockTechnicals.getEma5();
                        double ema10 = stockTechnicals.getEma10();
                        double ema20 = stockTechnicals.getEma20();
                        double ema50 = stockTechnicals.getEma50();
                        double ema100 = stockTechnicals.getEma100();

                        String pattern = "NA";
                        if (isDoji) {
                            pattern = "Doji";
                        } else if (isSpinningTop) {
                            pattern = "Spining Top";
                        } else if (isHammer) {
                            pattern = "Hammer";
                        } else if (isInvertedHammer) {
                            pattern = "Inverted Hammer";
                        } else if (isBullishHarami) {
                            pattern = "Harami";
                        } else if (isBullishEngulfing) {
                            pattern = "Engulfing";
                        } else if (isPiercingPAttern) {
                            pattern = "Piercing";
                        } else if (isTweezerBottom) {
                            pattern = "Tweezer";
                        } else if (isDoubleBottom) {
                            pattern = "Double Bottom";
                        }

                        //  if (MovingAverageUtil.isAllMAsIncreasing(stockTechnicals)) {

                        support.add(
                                sessionDate
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + maInteractionOptional.get().getLength()
                                        + ", "
                                        + pattern
                                        + ", "
                                        + stockPrice.getClose()
                                        + ", "
                                        + gain
                                        + ", "
                                        + isCandleStickPattern
                                        + ", "
                                        + isPrevCandleStickPattern
                                        + ","
                                        + stockTechnicals.getRsi()
                                        + ","
                                        + (stockTechnicals.getVolume()
                                                > stockTechnicals.getPrevVolume())
                                        + ","
                                        + chngPct
                                        + ","
                                        + marketCapCategory
                                        + ","
                                        + ema5
                                        + ","
                                        + ema10
                                        + ","
                                        + ema20
                                        + ","
                                        + ema50
                                        + ","
                                        + ema100);
                        // }
                    }
                }
            }
            sessionDate = calendarService.nextTradingSession(sessionDate);
        }
        support.forEach(System.out::println);
        System.out.println("----------");
        breakout.forEach(System.out::println);
    }

    public void dynamicScanner5() {
        List<Stock> stocks = stockService.getActiveStocks();
        //  List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();

        sessionDates.add(LocalDate.of(2025, 1, 31));
        sessionDates.add(LocalDate.of(2025, 2, 28));
        sessionDates.add(LocalDate.of(2025, 3, 31));
        sessionDates.add(LocalDate.of(2025, 4, 30));
        sessionDates.add(LocalDate.of(2025, 5, 31));
        sessionDates.add(LocalDate.of(2025, 6, 30));
        sessionDates.add(LocalDate.of(2025, 7, 31));
        sessionDates.add(LocalDate.of(2025, 8, 31));
        sessionDates.add(LocalDate.of(2025, 9, 30));
        sessionDates.add(LocalDate.of(2025, 10, 31));
        sessionDates.add(LocalDate.of(2025, 11, 30));
        for (LocalDate sessionDate : sessionDates) {

            for (Stock stock : stocks) {

                if (!this.isInititalValidated(stock)) {
                    continue;
                }

                StockPrice stockPrice = updatePriceService.buildBack(MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(MONTHLY, stock, sessionDate);

                if (!this.isInititalValidated(stockPrice)) {
                    continue;
                }

                if (!this.isInititalValidated(stockTechnicals)) {
                    continue;
                }

                double mcap = fundamentalResearchService.marketCap(stockPrice);
                MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);

                if (marketCapCategory == MarketCapCategory.MICROCAP || mcap < 1000) {
                    continue;
                }

                if (stockPrice.getClose() > 50 && stockPrice.getClose() < 1000) {
                    if (CandleStickUtils.isGreen(stockPrice)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)) {
                        // System.out.println("TRRRRRRRR1 " + stock.getNseSymbol());

                        //   System.out.println("TRRRRRRRR2 " + stock.getNseSymbol());
                        double ema5 = stockTechnicals.getEma5();
                        double prevEma5 = stockTechnicals.getPrevEma5();
                        double ema20 = stockTechnicals.getEma20();
                        double prevEma20 = stockTechnicals.getPrevEma20();
                        double ema50 = stockTechnicals.getEma50();
                        double prevEma50 = stockTechnicals.getPrevEma50();
                        double ema100 =
                                MovingAverageUtil.getMovingAverage100(MONTHLY, stockTechnicals);
                        double prevEma100 =
                                MovingAverageUtil.getPrevMovingAverage100(MONTHLY, stockTechnicals);
                        double close = stockPrice.getClose();
                        double prevClose = stockPrice.getPrevClose();
                        boolean isEma5Breakout = close > ema5 && prevClose < prevEma5;
                        boolean isEma20Breakout = close > ema20 && prevClose < prevEma20;
                        boolean isEma50Breakout = close > ema50 && prevClose < prevEma50;
                        boolean isCloseAbovePRevHigh = close > stockPrice.getPrevHigh();
                        boolean isEMAAlign = ema20 >= ema50 && ema50 >= ema100;
                        if (isEMAAlign) {
                            if (stockPrice.getPrevClose() < stockPrice.getPrev2Low()) {
                                if ((isEma5Breakout || isEma20Breakout || isEma50Breakout)) {
                                    String result = sessionDate + ", " + stock.getNseSymbol();
                                    System.out.println("TRRRRRRRR " + result);
                                    results.add(result);
                                }
                            }
                        }
                    }
                }
            }
        }

        results.forEach(System.out::println);
    }

    public boolean isEmaAlign(
            StockTechnicals stockTechnicals, MASupportChecker.MovingAverageLength length) {

        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma100();
        double ema100 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        switch (length) {
            case EMA5:
                return ema5 >= ema20 && ema20 >= ema50 && ema50 >= ema100 && ema100 >= ema200;
            case EMA20:
                return ema20 >= ema50 && ema50 >= ema100 && ema100 >= ema200;
            case EMA50:
                return ema50 >= ema100 && ema100 >= ema200;
            case EMA100:
                return ema100 >= ema200;
            default:
                return true;
        }
    }

    public boolean isEmaAlign(StockTechnicals stockTechnicals, MovingAverageLength length) {

        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma100();
        double ema100 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        switch (length) {
            case HIGHEST:
                return ema5 >= ema20 && ema20 >= ema50 && ema50 >= ema100 && ema100 >= ema200;
                // case HIGH: return ema10 >=ema20 && ema20 >= ema50 && ema50 >=ema100 && ema100 >=
                // ema200;
            case HIGH:
                return ema20 >= ema50 && ema50 >= ema100 && ema100 >= ema200;
            case MEDIUM:
                return ema50 >= ema100 && ema100 >= ema200;
            case LOW:
                return ema100 >= ema200;
            default:
                return false;
        }
    }

    private boolean isInititalValidated(Stock stock) {
        if (stock == null) {
            return false;
        }

        boolean isEqOrBE = stock.getSeries().equalsIgnoreCase("EQ");

        if (!isEqOrBE) {
            return false;
        }

        if (stock.getSector() == null || stock.getSector().getType() == Sector.Type.ETF) {
            return false;
        }

        return true;
    }

    private boolean isInititalValidated(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        if (!fundamentalResearchService.isPriceInRange(stockPrice)) {
            return false;
        }

        return true;
    }

    private boolean isInititalValidated(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null) {
            return false;
        }

        if (stockTechnicals.getTimeframe() == MONTHLY
                && (stockTechnicals.getVolumeAvg20() > 10_00_000
                        || stockTechnicals.getVolume() > 10_00_000)) {

            return true;
        }

        if (stockTechnicals.getTimeframe() == Timeframe.WEEKLY
                && (stockTechnicals.getVolumeAvg20() > 2_50_000
                        || stockTechnicals.getVolume() > 2_50_000)) {
            return true;
        }

        if (stockTechnicals.getTimeframe() == Timeframe.DAILY
                && (stockTechnicals.getVolumeAvg20() > 1_00_000
                        || stockTechnicals.getVolume() > 1_00_000)) {
            return true;
        }

        return false;
    }

    public Double resistance(StockPrice stockPrice) {
        List<Candle> candles = new ArrayList<>();
        candles.add(
                new Candle(
                        stockPrice.getOpen(),
                        stockPrice.getHigh(),
                        stockPrice.getLow(),
                        stockPrice.getClose())); // Current (0)

        candles.add(
                new Candle(
                        stockPrice.getPrevOpen(),
                        stockPrice.getPrevHigh(),
                        stockPrice.getPrevLow(),
                        stockPrice.getPrevClose())); // Prev1 (1)

        candles.add(
                new Candle(
                        stockPrice.getPrev2Open(),
                        stockPrice.getPrev2High(),
                        stockPrice.getPrev2Low(),
                        stockPrice.getPrev2Close())); // Prev2 (2)

        candles.add(
                new Candle(
                        stockPrice.getPrev3Open(),
                        stockPrice.getPrev3High(),
                        stockPrice.getPrev3Low(),
                        stockPrice.getPrev3Close())); // Prev3 (3)

        candles.add(
                new Candle(
                        stockPrice.getPrev4Open(),
                        stockPrice.getPrev4High(),
                        stockPrice.getPrev4Low(),
                        stockPrice.getPrev4Close())); // Prev4 (4)

        candles.add(
                new Candle(
                        stockPrice.getPrev5Open(),
                        stockPrice.getPrev5High(),
                        stockPrice.getPrev5Low(),
                        stockPrice.getPrev5Close())); // Prev5 (5)

        candles.add(
                new Candle(
                        stockPrice.getPrev6Open(),
                        stockPrice.getPrev6High(),
                        stockPrice.getPrev6Low(),
                        stockPrice.getPrev6Close())); // Prev6 (6)

        Candle current = candles.get(0);

        // Check previous candles in order (most recent first)
        for (int prevIdx = 1; prevIdx < candles.size(); prevIdx++) {
            Candle prevCandle = candles.get(prevIdx);

            // Skip if there's a higher bullish body between current and this prev candle
            if (hasHigherBullishBodyBetween(candles, 0, prevIdx)) {
                continue;
            }

            if (wickIntersect(current, prevCandle)) {
                return Math.min(current.high, prevCandle.high);
            }
        }

        return 0.0; // No resistance found
    }

    private boolean hasHigherBullishBodyBetween(List<Candle> candles, int idx1, int idx2) {
        int start = Math.min(idx1, idx2);
        int end = Math.max(idx1, idx2);

        if (end - start <= 1) {
            return false; // Adjacent, no candle between
        }

        Candle candle1 = candles.get(idx1);
        Candle candle2 = candles.get(idx2);
        double minBodyTop =
                Math.min(
                        Math.max(candle1.open, candle1.close),
                        Math.max(candle2.open, candle2.close));

        for (int i = start + 1; i < end; i++) {
            Candle between = candles.get(i);
            if (between.close > between.open) { // Bullish candle
                double betweenBodyTop = between.close; // For bullish, close is body top
                if (betweenBodyTop > minBodyTop) {
                    return true; // Higher bullish body found → blocks
                }
            }
        }

        return false;
    }

    private boolean wickIntersect(Candle c1, Candle c2) {
        double c1BodyTop = Math.max(c1.open, c1.close);
        double c2BodyTop = Math.max(c2.open, c2.close);

        double c1WickStart = c1BodyTop;
        double c1WickEnd = c1.high;
        double c2WickStart = c2BodyTop;
        double c2WickEnd = c2.high;

        // Check if wicks overlap
        return Math.max(c1WickStart, c2WickStart) <= Math.min(c1WickEnd, c2WickEnd);
    }

    // Helper class for candle data
    private static class Candle {
        double open;
        double high;
        double low;
        double close;

        Candle(double open, double high, double low, double close) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }
    }

    public SupportZone detectSupport(StockPrice p) {

        List<Candle> candles =
                List.of(
                        new Candle(
                                p.getPrev11Open(),
                                p.getPrev11High(),
                                p.getPrev11Low(),
                                p.getPrev11Close()),
                        new Candle(
                                p.getPrev10Open(),
                                p.getPrev10High(),
                                p.getPrev10Low(),
                                p.getPrev10Close()),
                        new Candle(
                                p.getPrev9Open(),
                                p.getPrev9High(),
                                p.getPrev9Low(),
                                p.getPrev9Close()),
                        new Candle(
                                p.getPrev8Open(),
                                p.getPrev8High(),
                                p.getPrev8Low(),
                                p.getPrev8Close()),
                        new Candle(
                                p.getPrev7Open(),
                                p.getPrev7High(),
                                p.getPrev7Low(),
                                p.getPrev7Close()),
                        new Candle(
                                p.getPrev6Open(),
                                p.getPrev6High(),
                                p.getPrev6Low(),
                                p.getPrev6Close()),
                        new Candle(
                                p.getPrev5Open(),
                                p.getPrev5High(),
                                p.getPrev5Low(),
                                p.getPrev5Close()),
                        new Candle(
                                p.getPrev4Open(),
                                p.getPrev4High(),
                                p.getPrev4Low(),
                                p.getPrev4Close()),
                        new Candle(
                                p.getPrev3Open(),
                                p.getPrev3High(),
                                p.getPrev3Low(),
                                p.getPrev3Close()),
                        new Candle(
                                p.getPrev2Open(),
                                p.getPrev2High(),
                                p.getPrev2Low(),
                                p.getPrev2Close()),
                        new Candle(
                                p.getPrevOpen(), p.getPrevHigh(), p.getPrevLow(), p.getPrevClose()),
                        new Candle(p.getOpen(), p.getHigh(), p.getLow(), p.getClose()));

        List<Double> lows = candles.stream().map(c -> c.low).collect(Collectors.toList());

        List<SupportZone> zones = new ArrayList<>();

        // Cluster tolerance: 3%
        double clusterTolPct = 0.03;

        for (int i = 0; i < lows.size(); i++) {

            double base = lows.get(i);

            List<Double> cluster = new ArrayList<>();

            for (double low : lows) {
                if (Math.abs(low - base) <= base * clusterTolPct) {
                    cluster.add(low);
                }
            }

            if (cluster.size() < 2) continue;

            // Compute core cluster min/max
            double minLow = cluster.stream().min(Double::compare).get();
            double maxLow = cluster.stream().max(Double::compare).get();

            // Allow ONE deeper wick up to 8%
            double overshootLimit = minLow * 0.92; // 8% lower allowed
            long overshoots = lows.stream().filter(l -> l < overshootLimit).count();
            if (overshoots > 1) continue;

            // Ignore higher lows above cluster + 4%
            double maxAllowed = maxLow * 1.04;
            long tooHigh = lows.stream().filter(l -> l > maxAllowed).count();
            // if (tooHigh > 0) continue;

            double level = (minLow + maxLow) / 2;

            SupportZone z = new SupportZone();
            z.setMin(minLow);
            z.setMax(maxLow);
            z.setLevel(level);
            z.setTouches(cluster.size());
            z.setStrength(cluster.size());

            zones.add(z);
        }

        if (zones.isEmpty()) return null;

        // strongest cluster
        zones.sort((a, b) -> b.getStrength() - a.getStrength());

        return zones.get(0);
    }
}
