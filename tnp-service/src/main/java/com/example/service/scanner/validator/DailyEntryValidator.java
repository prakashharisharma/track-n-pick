package com.example.service.scanner.validator;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.OHLCV;
import com.example.dto.io.StockAnalysis;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyEntryValidator {
    private final MiscUtil miscUtil;
    private final CalendarService calendarService;
    private final FormulaService formulaService;
    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;
    private final StockPriceService stockPriceService;
    private final CandleStickConfirmationService candleStickConfirmationService;

    private final FundamentalResearchService fundamentalResearchService;

    private final MonthlySupportResistanceService monthlySupportResistanceService;

    public Optional<StockAnalysis> isValid(
            StockPrice stockPriceMonthly,
            StockPrice stockPriceDaily,
            StockTechnicals stockTechnicalsMonthly,
            StockTechnicals stockTechnicalsDaily,
            LocalDate sessionDate,
            ResearchTechnical.Strategy strategy) {
        Stock stock = stockPriceDaily.getStock();
        double monthlyClose = stockPriceMonthly.getClose();
        double dailyClose = stockPriceDaily.getClose();
        double dailyLow = stockPriceDaily.getLow();
        double dailyHigh = stockPriceDaily.getHigh();
        double entryPrice = (dailyClose + dailyHigh) / 2;

        // 1. Daily confirmation must be above monthly breakout close
        if (dailyClose <= monthlyClose) {
            return Optional.empty();
        }

        // 2. Daily trend check → EMA5 > EMA20
        double dailyEma5 = stockTechnicalsDaily.getEma5();
        double dailyEma20 = stockTechnicalsDaily.getEma20();

        if (!(dailyEma5 > dailyEma20)) {
            return Optional.empty();
        }

        // 3. Avoid weak or bearish candles
        if (CandleStickUtils.isRed(stockPriceDaily)
                && CandleStickUtils.bodySize(stockPriceDaily)
                        > CandleStickUtils.upperWickSize(stockPriceDaily)) {
            return Optional.empty();
        }

        // avoid extremely weak doji or high upper wick
        if (CandleStickUtils.isVerySmallBody(stockPriceDaily)
                || CandleStickUtils.isUpperWickWithinLimit(stockPriceDaily, 40)) {
            return Optional.empty();
        }

        // 4. EMA5 decreasing → avoid weakening momentum
        Double prevDailyEma5 = stockTechnicalsDaily.getPrevEma5();
        if (prevDailyEma5 != null && dailyEma5 < prevDailyEma5) {
            return Optional.empty();
        }

        // 5. Volume confirmation (optional but good)
        long dailyVolume = stockTechnicalsDaily.getVolume();
        long dailyVolumeAvg = stockTechnicalsDaily.getVolumeAvg10();
        if (dailyVolumeAvg > 0 && dailyVolume < dailyVolumeAvg) {
            // allow but not mandatory → do nothing
        }

        // 6. Stop-loss sanity → SL must not be too large
        double stopLoss = stockPriceDaily.getLow();
        double riskPercent = ((entryPrice - stopLoss) / entryPrice) * 100;

        if (riskPercent > 4.5) { // cap risk to avoid huge candles
            return Optional.empty();
        }

        LocalDate entryMonthCloseDate = miscUtil.currentMonthLastDay();

        if (calendarService.isLastTradingSessionOfMonth(sessionDate)) {
            entryMonthCloseDate =
                    calendarService.previousTradingSession(
                            YearMonth.from(sessionDate.plusMonths(1)).atEndOfMonth());
        }

        StockPrice stockPriceEntryMonthClose =
                updatePriceService.buildBack(
                        Timeframe.DAILY,
                        stock,
                        miscUtil.isBackTest() ? entryMonthCloseDate : miscUtil.currentDate());

        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(fundamentalResearchService.marketCap(stockPriceDaily));
        // ----------------------------
        // Passed all checks → create StockAnalysis
        // ----------------------------

        // double entryPrice = stockPriceDaily.getHigh();

        double per =
                formulaService.calculateChangePercentage(
                        entryPrice, Math.max(stopLoss, stockPriceEntryMonthClose.getClose()));
        /*
                StockPrice stockPriceNextDay =
                        getStockPriceFromMap(
                                Timeframe.DAILY,
                                stock,
                                calendarService
                                        .nextTradingSession(
                                                sessionDate));
        */
        StockPrice stockPriceNextDay =
                updatePriceService.buildBack(
                        Timeframe.DAILY, stock, calendarService.nextTradingSession(sessionDate));
        if (!miscUtil.isBackTest() || entryPrice > stockPriceNextDay.getLow()) {

            StockPrice stockPriceCurrent = stockPriceService.get(stock, Timeframe.DAILY);

            double currentPEr =
                    formulaService.calculateChangePercentage(
                            entryPrice, stockPriceCurrent.getClose());

            StockAnalysis result = new StockAnalysis();

            result.setScanDate(sessionDate);
            //  result.setEntryDate(stockPriceNextDay.getSessionDate());
            //   result.setEntryMonthCloseDate(stockPriceEntryMonthClose.getSessionDate());
            result.setCurrentCloseDate(stockPriceCurrent.getSessionDate());
            result.setStock(stockPriceDaily.getStock());
            result.setStrategy(strategy);
            result.setMarketCap(marketCapCategory);

            result.setEntryPrice(entryPrice);
            //  result.setEntryMonthClose(stockPriceEntryMonthClose.getClose());
            result.setCurrentClose(stockPriceCurrent.getClose());

            result.setStopLoss(Math.min(stockPriceDaily.getLow(), stockPriceDaily.getPrevLow()));

            result.setRisk(riskPercent);
            result.setChangePercent(per);
            // result.setCurrentChangePercent(currentPEr);

            return Optional.of(result);
        }
        return Optional.empty();
    }

    public Optional<StockAnalysis> isValid2(
            StockPrice stockPriceMonthly,
            StockPrice stockPriceDaily,
            StockTechnicals stockTechnicalsMonthly,
            StockTechnicals stockTechnicalsDaily,
            LocalDate sessionDate,
            ResearchTechnical.Strategy strategy) {

        Stock stock = stockPriceDaily.getStock();

        boolean isGreen = CandleStickUtils.isGreen(stockPriceDaily);

        double closeDaily = stockPriceDaily.getClose();
        double lowDaily = stockPriceDaily.getLow();
        boolean isEma5LowRejected =
                stockTechnicalsDaily.getEma5() > lowDaily
                        && stockTechnicalsDaily.getEma5() < closeDaily;
        boolean isEma20LowRejected =
                stockTechnicalsDaily.getEma20() > lowDaily
                        && stockTechnicalsDaily.getEma20() < closeDaily;
        boolean isEma50LowRejected =
                stockTechnicalsDaily.getEma50() > lowDaily
                        && stockTechnicalsDaily.getEma50() < closeDaily;
        boolean isEma100LowRejected =
                stockTechnicalsDaily.getEma100() > lowDaily
                        && stockTechnicalsDaily.getEma100() < closeDaily;
        boolean isEma200LowRejected =
                stockTechnicalsDaily.getEma200() > lowDaily
                        && stockTechnicalsDaily.getEma200() < closeDaily;
        boolean isLowRejected =
                isEma5LowRejected
                        || isEma20LowRejected
                        || isEma50LowRejected
                        || isEma100LowRejected
                        || isEma200LowRejected;

        double dailyVariation =
                formulaService.calculateAbsChangePercentage(
                        stockPriceDaily.getOpen(), stockPriceDaily.getHigh());

        boolean isOpenAndLowEqual = CandleStickUtils.isOpenAndLowEqual(stockPriceDaily);

        boolean isUpperWickLongerThanLowerWick =
                !isOpenAndLowEqual
                        && CandleStickUtils.isUpperWickLongerThanLowerWick(stockPriceDaily);

        double ema5 = stockTechnicalsDaily.getEma5();
        double ema20 = stockTechnicalsDaily.getEma20();
        double ema50 = stockTechnicalsDaily.getEma50();

        double prevEma5 = stockTechnicalsDaily.getPrevEma5();
        double prevEma20 = stockTechnicalsDaily.getPrevEma20();
        double prevEma50 = stockTechnicalsDaily.getPrevEma50();

        double prev2Ema5 = stockTechnicalsDaily.getPrev2Ema5();

        boolean isOpenAndCloseAboveEma5 =
                stockPriceDaily.getOpen() > ema5
                        && stockPriceDaily.getClose() > ema5
                        && MovingAverageUtil.isAllMaAlignedBullish(
                                stockTechnicalsDaily.getTimeframe(), stockTechnicalsDaily);

        double bodyVariation =
                formulaService.calculateAbsChangePercentage(
                        stockPriceDaily.getOpen(), stockPriceDaily.getClose());

        double prevBodyVariation =
                formulaService.calculateAbsChangePercentage(
                        stockPriceDaily.getPrevOpen(), stockPriceDaily.getPrevClose());

        boolean isDailyBodyExtended = false;

        boolean isEma5OnTop = (ema5 > ema20 && ema20 > ema50);

        if (bodyVariation < 5.0 && prevBodyVariation < 7.5) {
            if (!this.isDead(stockPriceDaily) && CandleStickUtils.isGreen(stockPriceDaily)) {
                boolean isDailyExtendedVariation =
                        isOpenAndCloseAboveEma5
                                && isUpperWickLongerThanLowerWick
                                && dailyVariation >= 10.0;
                boolean isExtendedGap =
                        isEma5OnTop && CandleStickUtils.isRisingWindow(stockPriceDaily);
                boolean isExtendedFromEma5 =
                        isEma5OnTop
                                && formulaService.calculateChangePercentage(
                                                ema5, stockPriceDaily.getClose())
                                        > 5.0;
                boolean isPrevExtendedFromEma5 =
                        isEma5OnTop
                                && formulaService.calculateChangePercentage(
                                                prevEma5, stockPriceDaily.getPrevClose())
                                        > 5.0;

                if (!isDailyExtendedVariation
                        && !isExtendedFromEma5
                        && !isExtendedGap
                        && !isPrevExtendedFromEma5) {

                    long avgVol = stockTechnicalsDaily.getVolumeAvg20();

                    boolean isAvgVolumeSufficient =
                            avgVol > 20_000
                                    || avgVol * stockTechnicalsDaily.getEma20() > 200_00_000;

                    if (isAvgVolumeSufficient) {

                        if (isGreen || isLowRejected) {
                            // System.out.println(" Here3 " + stock.getNseSymbol());
                            boolean isPRevREdORLowerHighLowerLow =
                                    CandleStickUtils.isPrevSessionRed(stockPriceDaily)
                                            || CandleStickUtils.isPrevLowerHigh(stockPriceDaily)
                                                    && CandleStickUtils.isPrevLowerLow(
                                                            stockPriceDaily);

                            boolean isPRevGreenAndHigherLow =
                                    CandleStickUtils.isPrevSessionGreen(stockPriceDaily)
                                            && CandleStickUtils.isPrev2SessionRed(stockPriceDaily)
                                            && CandleStickUtils.isPrevHigherLow(stockPriceDaily)
                                            && stockPriceDaily.getPrevClose()
                                                    > stockPriceDaily.getPrev2Open();

                            if (isPRevREdORLowerHighLowerLow
                                    || isPRevGreenAndHigherLow
                                    || strategy == ResearchTechnical.Strategy.DOJI) {

                                boolean isVolOrAvgIncr =
                                        (stockTechnicalsDaily.getVolume()
                                                                > stockTechnicalsDaily
                                                                        .getPrevVolume()
                                                        || stockTechnicalsDaily.getVolumeAvg20()
                                                                > stockTechnicalsDaily
                                                                        .getPrevVolumeAvg20())
                                                || (stockTechnicalsMonthly.getVolume()
                                                                > stockTechnicalsMonthly
                                                                        .getPrevVolume()
                                                        || stockTechnicalsMonthly.getVolumeAvg20()
                                                                > stockTechnicalsMonthly
                                                                        .getPrevVolumeAvg20());
                                //     System.out.println(" Here4 " + stock.getNseSymbol());
                                MarketCapCategory marketCapCategory =
                                        MarketCapCategory.classify(
                                                fundamentalResearchService.marketCap(
                                                        stockPriceDaily));

                                if (isVolOrAvgIncr) {

                                    double volThreshold = 1.0;

                                    if (marketCapCategory == MarketCapCategory.LARGECAP) {
                                        volThreshold = 1.0;
                                    }
                                    if (marketCapCategory == MarketCapCategory.MIDCAP) {
                                        volThreshold = 1.5;
                                    }
                                    if (marketCapCategory == MarketCapCategory.SMALLCAP) {
                                        volThreshold = 2.0;
                                    }
                                    if (marketCapCategory == MarketCapCategory.MICROCAP) {
                                        volThreshold = 2.5;
                                    }

                                    boolean isVolumeAboveAverage =
                                            ((marketCapCategory == MarketCapCategory.MEGACAP)
                                                            ? stockTechnicalsDaily.getVolume()
                                                                    > stockTechnicalsDaily
                                                                            .getPrevVolume()
                                                            : stockTechnicalsDaily.getVolume()
                                                                    > stockTechnicalsDaily
                                                                                    .getVolumeAvg20()
                                                                            * volThreshold)
                                                    || ((marketCapCategory
                                                                    == MarketCapCategory.MEGACAP)
                                                            ? stockTechnicalsMonthly.getVolume()
                                                                    > stockTechnicalsMonthly
                                                                            .getPrevVolume()
                                                            : stockTechnicalsMonthly.getVolume()
                                                                    > stockTechnicalsMonthly
                                                                                    .getVolumeAvg20()
                                                                            * volThreshold);

                                    boolean isVolumeIncr =
                                            (stockTechnicalsDaily.getVolume()
                                                                    > stockTechnicalsDaily
                                                                            .getVolumeAvg20()
                                                            && stockTechnicalsDaily.getVolume()
                                                                    > 1.40
                                                                            * stockTechnicalsDaily
                                                                                    .getPrevVolume())
                                                    || (stockTechnicalsMonthly.getVolume()
                                                                    > stockTechnicalsMonthly
                                                                            .getVolumeAvg20()
                                                            && stockTechnicalsMonthly.getVolume()
                                                                    > 1.40
                                                                            * stockTechnicalsMonthly
                                                                                    .getPrevVolume());

                                    if (isVolumeAboveAverage || isVolumeIncr) {

                                        boolean isEma5Lowest = ema5 < ema20 && ema20 < ema50;
                                        boolean isPrevEma5Lowest =
                                                prevEma5 < prevEma20 && prevEma20 < prevEma50;

                                        boolean isUpperWickDominant =
                                                (stockPriceDaily.getClose() > ema5 && !isEma5Lowest)
                                                        && CandleStickUtils.isStrongRange(
                                                                stockPriceDaily.getTimeframe(),
                                                                stockPriceDaily,
                                                                stockTechnicalsDaily)
                                                        && CandleStickUtils.isUpperWickDominant(
                                                                stockPriceDaily)
                                                        && ((closeDaily
                                                                                > stockTechnicalsDaily
                                                                                        .getEma5()
                                                                        && closeDaily
                                                                                > stockTechnicalsDaily
                                                                                        .getEma20())
                                                                || (stockPriceDaily.getPrevClose()
                                                                                        > prevEma5
                                                                                && !isPrevEma5Lowest)
                                                                        && CandleStickUtils
                                                                                .isPrevStrongRange(
                                                                                        stockPriceDaily
                                                                                                .getTimeframe(),
                                                                                        stockPriceDaily,
                                                                                        stockTechnicalsDaily)
                                                                        && CandleStickUtils
                                                                                .isPrevUpperWickDominant(
                                                                                        stockPriceDaily));
                                        if (!isUpperWickDominant) {

                                            boolean isLowerHighAndLowerLow =
                                                    CandleStickUtils.isRed(stockPriceDaily)
                                                            && CandleStickUtils.isLowerHigh(
                                                                    stockPriceDaily)
                                                            && CandleStickUtils.isLowerLow(
                                                                    stockPriceDaily);
                                            if (!isLowerHighAndLowerLow) {

                                                boolean isHarami =
                                                        CandleStickUtils.isRed(stockPriceDaily)
                                                                && CandleStickUtils
                                                                        .isPrevSessionGreen(
                                                                                stockPriceDaily)
                                                                && CandleStickUtils.isLowerHigh(
                                                                        stockPriceDaily)
                                                                && CandleStickUtils.isHigherLow(
                                                                        stockPriceDaily);
                                                boolean isDoji =
                                                        CandleStickUtils.isVerySmallBody(
                                                                stockPriceDaily);

                                                boolean isHaningMan =
                                                        MovingAverageUtil.isAllMaAlignedBullish(
                                                                        stockPriceDaily
                                                                                .getTimeframe(),
                                                                        stockTechnicalsDaily)
                                                                && CandleStickUtils
                                                                        .isLowerWickLongerThanUpperWick(
                                                                                stockPriceDaily)
                                                                && stockPriceDaily.getOpen() > ema5
                                                                && stockPriceDaily.getClose()
                                                                        > ema5;
                                                if (!isHarami && !isDoji && !isHaningMan) {

                                                    boolean isRsiInRange =
                                                            stockTechnicalsDaily.getRsi() < 65.0;

                                                    if (isRsiInRange) {
                                                        boolean isBreakOutEma5 =
                                                                stockPriceDaily.getClose() > ema5
                                                                        && stockPriceDaily
                                                                                        .getPrevClose()
                                                                                < prevEma5;
                                                        boolean isPrevBreakOutEma5 =
                                                                stockPriceDaily.getPrevClose()
                                                                                > prevEma5
                                                                        && stockPriceDaily
                                                                                        .getPrev2Close()
                                                                                < prev2Ema5;

                                                        if (isBreakOutEma5
                                                                || isPrevBreakOutEma5
                                                                || strategy
                                                                        == ResearchTechnical
                                                                                .Strategy.DOJI) {
                                                            double entryPrice =
                                                                    Math.min(
                                                                            formulaService
                                                                                    .applyPercentChange(
                                                                                            Math
                                                                                                    .min(
                                                                                                            stockPriceMonthly
                                                                                                                    .getClose(),
                                                                                                            stockPriceDaily
                                                                                                                    .getPrevClose()),
                                                                                            strategy
                                                                                                            == ResearchTechnical
                                                                                                                    .Strategy
                                                                                                                    .OMEGA
                                                                                                    ? 2.0
                                                                                                    : 1.5),
                                                                            stockPriceDaily
                                                                                    .getHigh());

                                                            entryPrice =
                                                                    Math.max(
                                                                            entryPrice,
                                                                            stockPriceDaily
                                                                                    .getClose());

                                                            double target =
                                                                    this.calculateTarget(
                                                                            entryPrice,
                                                                            Timeframe.MONTHLY);

                                                            LocalDate entryMonthCloseDate =
                                                                    calendarService
                                                                            .previousTradingSession(
                                                                                    sessionDate
                                                                                            .plusMonths(
                                                                                                    1)
                                                                                            .withDayOfMonth(
                                                                                                    1));

                                                            if (calendarService
                                                                    .isLastTradingSessionOfMonth(
                                                                            sessionDate)) {
                                                                entryMonthCloseDate =
                                                                        calendarService
                                                                                .previousTradingSession(
                                                                                        YearMonth
                                                                                                .from(
                                                                                                        sessionDate
                                                                                                                .plusMonths(
                                                                                                                        1))
                                                                                                .atEndOfMonth());
                                                            }

                                                            StockPrice stockPriceEntryMonthClose =
                                                                    updatePriceService.buildBack(
                                                                            Timeframe.DAILY,
                                                                            stock,
                                                                            miscUtil.isBackTest()
                                                                                    ? entryMonthCloseDate
                                                                                    : miscUtil
                                                                                            .currentDate());
                                                            StockTechnicals stockTechnicalsCurrent =
                                                                    updateTechnicalsService
                                                                            .buildBack(
                                                                                    Timeframe.DAILY,
                                                                                    stock,
                                                                                    miscUtil
                                                                                                    .isBackTest()
                                                                                            ? entryMonthCloseDate
                                                                                            : miscUtil
                                                                                                    .currentDate());

                                                            double stopLoss =
                                                                    stockPriceDaily.getLow();

                                                            double currentClose =
                                                                    stockPriceEntryMonthClose
                                                                            .getClose();

                                                            double risk =
                                                                    ((entryPrice - stopLoss)
                                                                                    / entryPrice)
                                                                            * 100.0;
                                                            // System.out.println("risk " + risk);

                                                            double riskThreshold = 1.0;

                                                            if (marketCapCategory
                                                                    == MarketCapCategory.MEGACAP) {
                                                                riskThreshold = 2.5;
                                                            } else if (marketCapCategory
                                                                    == MarketCapCategory.LARGECAP) {
                                                                riskThreshold = 2.0;
                                                            } else if (marketCapCategory
                                                                    == MarketCapCategory.MIDCAP) {
                                                                riskThreshold = 3.5;
                                                            } else if (marketCapCategory
                                                                    == MarketCapCategory.SMALLCAP) {
                                                                riskThreshold = 2.5;
                                                            } else {
                                                                riskThreshold = 2.0;
                                                            }

                                                            boolean isUpperWickSizeConfirmed =
                                                                    candleStickConfirmationService
                                                                            .isUpperWickSizeConfirmed(
                                                                                    Timeframe.DAILY,
                                                                                    stockPriceDaily,
                                                                                    stockTechnicalsDaily);
                                                            boolean isInvertedHammer =
                                                                    CandleStickUtils
                                                                                    .isUpperWickDominant(
                                                                                            stockPriceDaily)
                                                                            && closeDaily < ema50
                                                                            && closeDaily
                                                                                    < MovingAverageUtil
                                                                                            .getMovingAverage200(
                                                                                                    Timeframe
                                                                                                            .DAILY,
                                                                                                    stockTechnicalsDaily);
                                                            if ((CandleStickUtils.isStrongRange(
                                                                                    Timeframe.DAILY,
                                                                                    stockPriceDaily,
                                                                                    stockTechnicalsDaily)
                                                                            && isUpperWickSizeConfirmed)
                                                                    || isInvertedHammer) {
                                                                double candleRisk =
                                                                        ((stockPriceDaily.getHigh()
                                                                                                - stockPriceDaily
                                                                                                        .getLow())
                                                                                        / stockPriceDaily
                                                                                                .getHigh())
                                                                                * 100.0;

                                                                if (candleRisk < riskThreshold * 2
                                                                        && candleRisk <= 5.0) {
                                                                    riskThreshold = candleRisk;
                                                                }
                                                            }

                                                            if (risk > riskThreshold
                                                                    && risk < riskThreshold * 3
                                                                    && Math.floor(risk)
                                                                            <= riskThreshold
                                                                                    + 1.0) {

                                                                entryPrice =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        entryPrice,
                                                                                        -1
                                                                                                * (risk
                                                                                                        - riskThreshold));

                                                                if (marketCapCategory
                                                                                == MarketCapCategory
                                                                                        .MEGACAP
                                                                        || marketCapCategory
                                                                                == MarketCapCategory
                                                                                        .LARGECAP) {
                                                                    entryPrice =
                                                                            formulaService
                                                                                    .applyPercentChange(
                                                                                            entryPrice,
                                                                                            1.0);
                                                                } else if (marketCapCategory
                                                                        == MarketCapCategory
                                                                                .MIDCAP) {
                                                                    entryPrice =
                                                                            formulaService
                                                                                    .applyPercentChange(
                                                                                            entryPrice,
                                                                                            0.8);
                                                                } else {
                                                                    entryPrice =
                                                                            formulaService
                                                                                    .applyPercentChange(
                                                                                            entryPrice,
                                                                                            0.50);
                                                                }
                                                            }

                                                            if (entryPrice
                                                                    < stockPriceDaily.getClose()) {
                                                                entryPrice =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        stockPriceDaily
                                                                                                .getClose(),
                                                                                        1.0);
                                                            }

                                                            entryPrice =
                                                                    Math.min(
                                                                            entryPrice,
                                                                            stockPriceDaily
                                                                                    .getHigh());

                                                            double sellPrice =
                                                                    formulaService
                                                                            .applyPercentChange(
                                                                                    currentClose,
                                                                                    -1 * 0.5);

                                                            double realizedStopLoss =
                                                                    formulaService
                                                                            .applyPercentChange(
                                                                                    stopLoss,
                                                                                    -1 * 0.5);
                                                            double per =
                                                                    formulaService
                                                                            .calculateChangePercentage(
                                                                                    entryPrice,
                                                                                    Math.max(
                                                                                            realizedStopLoss,
                                                                                            sellPrice));
                                                            risk =
                                                                    ((entryPrice - stopLoss)
                                                                                    / entryPrice)
                                                                            * 100.0;

                                                            if (Math.floor(risk)
                                                                    <= riskThreshold + 2.0) {

                                                                if (risk <= riskThreshold + 0.5) {

                                                                    riskThreshold =
                                                                            riskThreshold + 0.5;
                                                                }

                                                                if (risk > riskThreshold) {

                                                                    entryPrice =
                                                                            formulaService
                                                                                    .applyPercentChange(
                                                                                            entryPrice,
                                                                                            -1
                                                                                                    * (risk
                                                                                                            - riskThreshold));
                                                                    risk =
                                                                            ((entryPrice - stopLoss)
                                                                                            / entryPrice)
                                                                                    * 100.0;
                                                                    per =
                                                                            formulaService
                                                                                    .calculateChangePercentage(
                                                                                            entryPrice,
                                                                                            sellPrice);
                                                                }

                                                                StockPrice stockPriceNextDay =
                                                                        updatePriceService
                                                                                .buildBack(
                                                                                        Timeframe
                                                                                                .DAILY,
                                                                                        stock,
                                                                                        calendarService
                                                                                                .nextTradingSession(
                                                                                                        sessionDate));

                                                                if (Math.floor(risk)
                                                                        <= Math.floor(
                                                                                riskThreshold)) {

                                                                    entryPrice =
                                                                            formulaService
                                                                                    .applyPercentChange(
                                                                                            entryPrice,
                                                                                            1.50);
                                                                    entryPrice =
                                                                            Math.min(
                                                                                    entryPrice,
                                                                                    stockPriceDaily
                                                                                            .getHigh());

                                                                    double entryPriceClose =
                                                                            formulaService
                                                                                    .applyPercentChange(
                                                                                            stockPriceDaily
                                                                                                    .getClose(),
                                                                                            0.75);
                                                                    entryPrice =
                                                                            Math.max(
                                                                                    entryPriceClose,
                                                                                    entryPrice);
                                                                    entryPrice =
                                                                            Math.min(
                                                                                    stockPriceDaily
                                                                                            .getHigh(),
                                                                                    entryPrice);

                                                                    if (!miscUtil.isBackTest()
                                                                            || entryPrice
                                                                                    > stockPriceNextDay
                                                                                            .getLow()) {
                                                                        stopLoss =
                                                                                stockPriceDaily
                                                                                        .getLow();
                                                                        double breakdownLevel =
                                                                                Math.min(
                                                                                        stockPriceDaily
                                                                                                .getOpen(),
                                                                                        stockPriceDaily
                                                                                                .getClose());
                                                                        double hardStopLoss =
                                                                                formulaService
                                                                                        .applyPercentChange(
                                                                                                stopLoss,
                                                                                                -1
                                                                                                        * 0.25);

                                                                        OHLCV ohlcvCurrent =
                                                                                monthlySupportResistanceService
                                                                                        .supportAndResistance(
                                                                                                stock
                                                                                                        .getNseSymbol(),
                                                                                                calendarService
                                                                                                        .nextTradingSession(
                                                                                                                sessionDate),
                                                                                                entryMonthCloseDate);

                                                                        StockPrice
                                                                                stockPriceCurrent =
                                                                                        stockPriceService
                                                                                                .get(
                                                                                                        stock,
                                                                                                        Timeframe
                                                                                                                .DAILY);
                                                                        double currentPEr =
                                                                                formulaService
                                                                                        .calculateAbsChangePercentage(
                                                                                                entryPrice,
                                                                                                stockPriceCurrent
                                                                                                        .getClose());
                                                                        StockAnalysis
                                                                                stockAnalysis =
                                                                                        new StockAnalysis();
                                                                        /*
                                                                        StockAnalysis
                                                                                stockAnalysis =
                                                                                        new StockAnalysis(
                                                                                                sessionDate,
                                                                                                calendarService
                                                                                                        .nextTradingSession(
                                                                                                                sessionDate),
                                                                                                entryMonthCloseDate,
                                                                                                stockPriceCurrent
                                                                                                        .getSessionDate(),
                                                                                                strategy,
                                                                                                stock,
                                                                                                marketCapCategory,
                                                                                                formulaService
                                                                                                        .ceilToNearestTen(
                                                                                                                entryPrice),
                                                                                                stockPriceEntryMonthClose
                                                                                                        .getClose(),
                                                                                                currentClose,
                                                                                                Math
                                                                                                        .min(
                                                                                                                stockPriceMonthly
                                                                                                                        .getLow(),
                                                                                                                stockPriceMonthly
                                                                                                                        .getPrevClose()),
                                                                                                risk,
                                                                                                per,
                                                                                                currentPEr,
                                                                                                false);
                                                                        */
                                                                        return Optional.of(
                                                                                stockAnalysis);
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
                            }
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean isDead(StockPrice stockPrice) {

        if (stockPrice.getOpen().equals(stockPrice.getHigh())
                && stockPrice.getOpen().equals(stockPrice.getLow())
                && stockPrice.getOpen().equals(stockPrice.getClose())) {
            return true;
        } else if (stockPrice.getPrevOpen().equals(stockPrice.getPrevHigh())
                && stockPrice.getPrevOpen().equals(stockPrice.getPrevLow())
                && stockPrice.getPrevOpen().equals(stockPrice.getPrevClose())) {
            return true;
        } else if (stockPrice.getPrev2Open().equals(stockPrice.getPrev2High())
                && stockPrice.getPrev2Open().equals(stockPrice.getPrev2Low())
                && stockPrice.getPrev2Open().equals(stockPrice.getPrev2Close())) {
            return true;
        } else if (stockPrice.getPrev3Open().equals(stockPrice.getPrev3High())
                && stockPrice.getPrev3Open().equals(stockPrice.getPrev3Low())
                && stockPrice.getPrev3Open().equals(stockPrice.getPrev3Close())) {
            return true;
        } else if (stockPrice.getPrev4Open().equals(stockPrice.getPrev4High())
                && stockPrice.getPrev4Open().equals(stockPrice.getPrev4Low())
                && stockPrice.getPrev4Open().equals(stockPrice.getPrev4Close())) {
            return true;
        } else if (stockPrice.getPrev5Open().equals(stockPrice.getPrev5High())
                && stockPrice.getPrev5Open().equals(stockPrice.getPrev5Low())
                && stockPrice.getPrev5Open().equals(stockPrice.getPrev5Close())) {
            return true;
        }

        return false;
    }

    private double calculateTarget(double entryPrice, Timeframe timeframe) {
        return formulaService.applyPercentChange(
                entryPrice, timeframe == Timeframe.MONTHLY ? 24.0 : 6.0);
    }

    private double calculateEntryPrice(double open, double close, Timeframe timeframe) {
        double entryPrice = (open + close) / 2;

        entryPrice =
                formulaService.applyPercentChange(
                        entryPrice, timeframe == Timeframe.MONTHLY ? 1.25 : 0.625);

        double closePRic =
                formulaService.applyPercentChange(
                        close, timeframe == Timeframe.MONTHLY ? 0.50 : 0.25);

        entryPrice = Math.max(entryPrice, closePRic);

        return entryPrice;
    }
}
