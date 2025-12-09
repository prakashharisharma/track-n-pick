package com.example.service.scanner;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestScanner {

    private final MASupportChecker maSupportChecker;
    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;

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

    public void dynamicScanner() {
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
                        + "length"
                        + ", "
                        + "pattern"
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

                // List<MASupportChecker.MAInteraction> maInteractionList =
                // maSupportChecker.findMAInteractions(stockPrice.getTimeframe(), stockPrice,
                // stockTechnicals, true);

                Optional<MASupportChecker.MAInteraction> maInteractionOptional =
                        maSupportChecker.findSingleMASupport(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

                if (maInteractionOptional.isPresent() && CandleStickUtils.isGreen(stockPrice)) {

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
                            && !candleStickService.isDoji(stockPrice)
                            && marketCapCategory != MarketCapCategory.MICROCAP
                            && stockTechnicals.getRsi() <= 65.0
                            && Math.floor(chngPct) <= 30.0) {
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
        }
        support.forEach(System.out::println);
        System.out.println("----------");
        breakout.forEach(System.out::println);
    }

    public void dynamicScanner1() {
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
                        + "length"
                        + ", "
                        + "pattern"
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

                // List<MASupportChecker.MAInteraction> maInteractionList =
                // maSupportChecker.findMAInteractions(stockPrice.getTimeframe(), stockPrice,
                // stockTechnicals, true);

                Optional<MAEvaluationResult> evaluationResultOptional =
                        dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

                if (evaluationResultOptional.isPresent()
                        && evaluationResultOptional.get().isNearSupport()) {
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

                    if (this.isEmaAlign(stockTechnicals, evaluationResultOptional.get().getLength())
                            && !candleStickService.isDoji(stockPrice)
                            && marketCapCategory != MarketCapCategory.MICROCAP
                            && stockTechnicals.getRsi() <= 65.0) {
                        System.out.println(
                                sessionDate
                                        + ", "
                                        + stock.getNseSymbol()
                                        + ", "
                                        + evaluationResultOptional.get().getLength()
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
                                        + evaluationResultOptional.get().getLength()
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
                && stockTechnicals.getVolume() < 5_00_000) {
            return false;
        }

        if (stockTechnicals.getTimeframe() == Timeframe.WEEKLY
                && stockTechnicals.getVolume() < 1_00_000) {
            return false;
        }

        if (stockTechnicals.getTimeframe() == Timeframe.DAILY
                && stockTechnicals.getVolume() < 1_00_000) {
            return false;
        }

        return true;
    }
}
