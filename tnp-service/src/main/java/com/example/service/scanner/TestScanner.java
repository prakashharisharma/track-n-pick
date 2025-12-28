package com.example.service.scanner;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
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

                StockPrice stockPrice =
                        updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

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
                    updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDateMonthly);
            StockTechnicals stockTechnicals =
                    updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDateMonthly);
            if (!this.isInititalValidated(stock)
                    || !this.isInititalValidated(stockPrice)
                    || !this.isInititalValidated(stockTechnicals)) {
                continue;
            }

            if (!(stockTechnicals.getEma20() >= stockTechnicals.getEma50()
                    && stockTechnicals.getEma50()
                            >= MovingAverageUtil.getMovingAverage200(
                                    Timeframe.MONTHLY, stockTechnicals))) {
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
                    updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDateMonthly);
            StockTechnicals stockTechnicalsMonthly =
                    updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDateMonthly);

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
    public void dynamicScanner() {
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
                        + "supportZone"
                        + ", "
                        + "gain"
                        + ", "
                        + "mcap"
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

                StockPrice stockPrice =
                        updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

                double mcap = fundamentalResearchService.marketCap(stockPrice);
                MarketCapCategory marketCapCategory = MarketCapCategory.classify(mcap);

                if (!this.isInititalValidated(stockPrice)) {
                    continue;
                }

                if (!this.isInititalValidated(stockTechnicals)) {
                    continue;
                }

                if (stockTechnicals.getEma20() == 0.0) {
                    continue;
                }

                Optional<MASupportChecker.MAInteraction> maInteractionOptional =
                        maSupportChecker.findSingleMASupport(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

                if (maInteractionOptional.isPresent() && CandleStickUtils.isGreen(stockPrice)) {

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

                    if (this.isEmaAlign(stockTechnicals, maInteractionOptional.get().getLength())
                            && marketCapCategory != MarketCapCategory.MICROCAP
                            && stockTechnicals.getRsi() <= 65.0
                            && Math.floor(chngPct) <= 5.0) {

                        boolean isLowerLow = CandleStickUtils.isLowerLow(stockPrice);
                        boolean isLowRejected = stockPrice.getOpen() > stockPrice.getPrevLow();

                        //  if(isLowerLow && isLowRejected){
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
                        } else if (isPiercingPattern) {
                            pattern = "Piercing";
                        } else if (isTweezerBottom) {
                            pattern = "Tweezer";
                        } else if (isDoubleBottom) {
                            pattern = "Double Bottom";
                        }

                        //  if (MovingAverageUtil.isAllMAsIncreasing(stockTechnicals)) {

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
                        SupportZone supportZone = detectSupport(stockPrice);
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
                                        + stockPrice.getHigh()
                                        + ", "
                                        + resistance
                                        + ", "
                                        + supportZone
                                        + ", "
                                        + gain
                                        + ", "
                                        + mcap
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
                        // }
                    }
                }
            }
        }
        support.forEach(System.out::println);
        System.out.println("----------");
        breakout.forEach(System.out::println);
    }

    public void dynamicScanner1() {
        List<Stock> stocks = stockService.getActiveStocks();
        // List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();

        List<LocalDate> sessionDates = new ArrayList<>();
        /*
          sessionDates.add(LocalDate.of(2025, 1, 31));
          sessionDates.add(LocalDate.of(2025, 2, 28));
          sessionDates.add(LocalDate.of(2025, 3, 31));
          sessionDates.add(LocalDate.of(2025, 4, 30));
        sessionDates.add(LocalDate.of(2025, 5, 31));
          sessionDates.add(LocalDate.of(2025, 6, 30));
          sessionDates.add(LocalDate.of(2025, 7, 31));
          sessionDates.add(LocalDate.of(2025, 8, 31));
          sessionDates.add(LocalDate.of(2025, 9, 30));
          sessionDates.add(LocalDate.of(2025, 10, 31));*/
        sessionDates.add(LocalDate.of(2025, 11, 30));
        List<String> support = new ArrayList<>();
        List<String> breakout = new ArrayList<>();
        support.add(
                "sessionDate"
                        + ", "
                        + "symbol"
                        + ", "
                        + "pattern"
                        + ", "
                        + "close"
                        + ", "
                        + "gain"
                        + ","
                        + "rsi"
                        + ","
                        + "volumeIncr"
                        + ","
                        + "chngPct"
                        + ","
                        + "mcap"
                        + ","
                        + "supportLevel"
                        + ", ohlcvas");
        for (LocalDate sessionDate : sessionDates) {

            for (Stock stock : stocks) {

                if (!this.isInititalValidated(stock)) {
                    continue;
                }

                StockPrice stockPrice =
                        updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

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

                if (marketCapCategory != MarketCapCategory.MICROCAP
                        && stockTechnicals.getRsi() <= 65.0
                        && (CandleStickUtils.isRed(stockPrice)
                                || CandleStickUtils.isPrevSessionRed(stockPrice)
                                || CandleStickUtils.isPrev2SessionRed(stockPrice))) {

                    StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);
                    double gain =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getClose(), stockPriceDaily.getClose());

                    double chngPct =
                            formulaService.calculateChangePercentage(
                                    stockPrice.getPrevClose(), stockPrice.getClose());

                    System.out.println(
                            sessionDate
                                    + ", "
                                    + stock.getNseSymbol()
                                    + ", "
                                    + stockPrice.getClose()
                                    + ", "
                                    + gain
                                    + ","
                                    + chngPct);

                    String pattern = "NA";

                    //  if (MovingAverageUtil.isAllMAsIncreasing(stockTechnicals)) {

                    StringBuilder ohlcvas = new StringBuilder();
                    ohlcvas.append("[");
                    ohlcvas.append(stockPrice.getPrev11Open());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev11High());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev11Low());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev11Close());
                    ohlcvas.append("~");
                    ohlcvas.append(stockPrice.getPrev10Open());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev10High());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev10Low());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev10Close());
                    ohlcvas.append("~");
                    ohlcvas.append(stockPrice.getPrev9Open());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev9High());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev9Low());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev9Close());
                    ohlcvas.append("~");
                    ohlcvas.append(stockPrice.getPrev8Open());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev8High());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev8Low());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev8Close());
                    ohlcvas.append("~");
                    ohlcvas.append(stockPrice.getPrev7Open());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev7High());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev7Low());
                    ohlcvas.append("-");
                    ohlcvas.append(stockPrice.getPrev7Close());
                    ohlcvas.append("~");
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

                    SupportZone supportLevel = this.detectSupport(stockPrice);

                    // supportLevel = supportLevel != null ? supportLevel :0.0;

                    if (supportLevel != null
                            && stockPrice.getLow() < supportLevel.getLevel()
                            && stockPrice.getClose() > supportLevel.getLevel()
                            && chngPct < 10.0) {

                        support.add(
                                sessionDate
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + pattern
                                        + ", "
                                        + stockPrice.getClose()
                                        + ", "
                                        + gain
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
                                        + supportLevel
                                        + ","
                                        + ohlcvas);
                    }
                }
                // }
            }
        }
        support.forEach(System.out::println);
        System.out.println("----------");
        breakout.forEach(System.out::println);
    }

    public void dynamicScanner2() {
        List<Stock> stocks = stockService.getActiveStocks();
        //  List<Stock> stocks = stockService.getForActivity();
        List<String> results = new ArrayList<>();
        // sessionDateWeekly + ", " + stock.getNseSymbol() + ", " + marketCapCategory + " ," +
        // stockPrice.getClose() + " ," + entry + " ," + stopLoss + " ," + currPer
        // results.add("sessionDate" + ", " + "symbol"+", " +"pattern" + ", "+ "mcap"+ ", "  +
        // "Close" + ", "+ "entry" +", "+ "stoploss" + ", " + "currPr"+ " ," + "ema5Weekly"+ " ," +
        // "ema10Weekly"+ " ," + "ema20Weekly"+ " ," + "ema50Weekl"+ " ," + "ema100Weekly"+ " ," +
        // "ema200Weekly");
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
                        + "close"
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
                        + "volumeAvgIncr"
                        + ","
                        + "chngPct"
                        + ","
                        + "mcap"
                        + ", ema5"
                        + ", ema10"
                        + ", ema20"
                        + ", ema50"
                        + ", ema100");
        for (LocalDate sessionDate : sessionDates) {

            for (Stock stock : stocks) {

                if (!this.isInititalValidated(stock)) {
                    continue;
                }

                StockPrice stockPrice =
                        updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

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
                double prevEma5 = stockTechnicals.getPrevEma5();
                double close = stockPrice.getClose();
                double prevClose = stockPrice.getPrevClose();
                double low = stockPrice.getLow();
                double prevLow = stockPrice.getPrevLow();

                boolean isPrevGreen = CandleStickUtils.isPrevSessionGreen(stockPrice);
                boolean isCurrentGreen = CandleStickUtils.isGreen(stockPrice);

                boolean isCurrentLowRejected =
                        low < ema5 && close > formulaService.applyPercentChange(ema5, 2.0);
                boolean isPrevLowRejected =
                        prevLow < prevEma5
                                && prevClose > formulaService.applyPercentChange(prevEma5, 2.0);
                ;

                boolean isBasicFilter =
                        isCurrentGreen
                                && isCurrentLowRejected
                                && (isPrevGreen || isPrevLowRejected);

                if (isBasicFilter && !CandleStickUtils.isUpperWickDominant(stockPrice)) {
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

                    if (marketCapCategory != MarketCapCategory.MICROCAP
                            && stockTechnicals.getRsi() <= 65.0
                            && Math.floor(chngPct) <= 25.0) {

                        boolean adxIndicator =
                                stockTechnicals.getAdx() > stockTechnicals.getPrevAdx()
                                        && stockTechnicals.getAdx() > 20.0;
                        // maInteractionList.forEach(System.out::println);

                        double ema10 = stockTechnicals.getEma10();
                        double ema20 = stockTechnicals.getEma20();
                        double ema50 = stockTechnicals.getEma50();
                        double ema100 = stockTechnicals.getEma100();

                        if (MovingAverageUtil.isAllMAsIncreasing(stockTechnicals) && adxIndicator) {

                            System.out.println(
                                    sessionDate
                                            + ", "
                                            + stock.getNseSymbol()
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

                            support.add(
                                    sessionDate
                                            + ", "
                                            + stock.getNseSymbol()
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
                                            + (stockTechnicals.getVolumeAvg10()
                                                    > stockTechnicals.getPrevVolumeAvg10())
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
                        }
                    }
                }
            }
        }
        support.forEach(System.out::println);
        System.out.println("----------");
        breakout.forEach(System.out::println);
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

                StockPrice stockPrice =
                        updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

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

                StockPrice stockPrice =
                        updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);
                StockTechnicals stockTechnicals =
                        updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

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
                                MovingAverageUtil.getMovingAverage100(
                                        Timeframe.MONTHLY, stockTechnicals);
                        double prevEma100 =
                                MovingAverageUtil.getPrevMovingAverage100(
                                        Timeframe.MONTHLY, stockTechnicals);
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

        if (stockTechnicals.getTimeframe() == Timeframe.MONTHLY
                && stockTechnicals.getVolumeAvg20() < 10_00_000) {
            return false;
        }

        if (stockTechnicals.getTimeframe() == Timeframe.WEEKLY
                && stockTechnicals.getVolumeAvg20() < 5_00_000) {
            return false;
        }

        if (stockTechnicals.getTimeframe() == Timeframe.DAILY
                && stockTechnicals.getVolumeAvg20() < 1_00_000) {
            return false;
        }

        return true;
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
