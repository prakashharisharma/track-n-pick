package com.example.service.scanner;

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
public class AlphaScanner {
    private final MiscUtil miscUtil;
    private final FormulaService formulaService;
    private final CalendarService calendarService;
    private final DailyEntryValidator dailyEntryValidator;
    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;
    private final MonthlySupportResistanceService monthlySupportResistanceService;

    private final StockService stockService;

    private final FundamentalResearchService fundamentalResearchService;

    public boolean test(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        // Stock stock = stockPrice.getStock();

        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma5 =
                stockTechnicals.getPrevEma5() != null ? stockTechnicals.getPrevEma5() : 0.0;
        double prevEma20 =
                stockTechnicals.getPrevEma20() != null ? stockTechnicals.getPrevEma20() : 0.0;
        double prevEma50 =
                stockTechnicals.getPrevEma50() != null ? stockTechnicals.getPrevEma50() : 0.0;
        double rsi = stockTechnicals.getRsi();
        long volume = stockTechnicals.getVolume() != null ? stockTechnicals.getVolume() : 0;
        long prevVolume =
                stockTechnicals.getPrevVolume() != null ? stockTechnicals.getPrevVolume() : 0;

        /** 1. Close above prev High */
        if (close > stockPrice.getPrevHigh() && CandleStickUtils.isGreen(stockPrice)) {
            double changePct =
                    formulaService.calculateChangePercentage(
                            stockPrice.getPrevClose(), stockPrice.getClose());

            boolean isEma5Incr = ema5 > prevEma5;
            boolean isEma20Incr = ema20 > prevEma20;
            boolean isEma50Incr = ema50 > prevEma50;

            boolean isEma5LowRejected = ema5 > 0.0 && low < ema5 && close > ema5;
            boolean isEma20LowRejected = ema20 > 0.0 && low < ema20 && close > ema20;
            boolean isEma50LowRejected = ema50 > 0.0 && low < ema50 && close > ema50;

            boolean isLowRejected = isEma5LowRejected || isEma20LowRejected || isEma50LowRejected;

            boolean isPrevEma5LowRejected =
                    prevEma5 > 0.0 && prevLow < prevEma5 && prevClose > prevEma5;
            boolean isPrevEma20LowRejected =
                    prevEma20 > 0.0 && prevLow < prevEma20 && prevClose > prevEma20;
            boolean isPrevEma50LowRejected =
                    prevEma50 > 0.0 && prevLow < prevEma50 && prevClose > prevEma50;

            boolean isPrevLowRejected =
                    isPrevEma5LowRejected || isPrevEma20LowRejected || isPrevEma50LowRejected;

            boolean isEma5Breakout = prevClose < ema5 && close > ema5;
            boolean isEma20Breakout = prevClose < ema20 && close > ema20;
            boolean isEma50Breakout = prevClose < ema50 && close > ema50;

            boolean isBreakout = isEma5Breakout || isEma20Breakout || isEma50Breakout;

            boolean isPrevEma5Breakout = prev2Close < prevEma5 && prevClose > prevEma5;
            boolean isPrevEma20Breakout = prev2Close < prevEma20 && prevClose > prevEma20;
            boolean isPrevEma50Breakout = prev2Close < prevEma50 && prevClose > prevEma50;

            boolean isPrevBreakout =
                    isPrevEma5Breakout || isPrevEma20Breakout || isPrevEma50Breakout;

            boolean isAllIncr =
                    (isEma5Incr && isEma20Incr && isEma50Incr)
                            || (isEma5Incr
                                    && isEma50Incr
                                    && (isEma20LowRejected || isEma20Breakout))
                            || (isEma20Incr && isEma50Incr && (isEma5LowRejected || isEma5Breakout))
                            || (isEma5Incr
                                    && isEma20Incr
                                    && (isEma50LowRejected || isEma50Breakout));

            boolean isEmaSatisfied = ema20 > 0.0 && ema50 > 0.0;

            boolean rsiFilter = false;

            if (rsi < 60) {
                rsiFilter = changePct < 30;
            } else if (rsi < 65) {
                rsiFilter = changePct < 20;
            } else if (rsi < 75) {
                rsiFilter = changePct < 18;
            } else {
                rsiFilter = false;
            }

            boolean isVolumeNotOverExtended = (volume / prevVolume) < 5.0;

            boolean isWeakVolume =
                    (volume < prevVolume
                            && stockTechnicals.getVolumeAvg10()
                                    < stockTechnicals.getPrevVolumeAvg10());

            if (!isWeakVolume
                    && isVolumeNotOverExtended
                    && rsiFilter
                    && isEmaSatisfied
                    && isAllIncr
                    && (CandleStickUtils.isPrevSessionRed(stockPrice)
                            || isPrevLowRejected
                            || isPrevBreakout
                            || isLowRejected
                            || isBreakout)) {

                return true;
            }
        }
        return false;
    }

    public boolean isValid(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        // Stock stock = stockPrice.getStock();

        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double prevEma5 =
                stockTechnicals.getPrevEma5() != null ? stockTechnicals.getPrevEma5() : 0.0;
        double prevEma20 =
                stockTechnicals.getPrevEma20() != null ? stockTechnicals.getPrevEma20() : 0.0;
        double prevEma50 =
                stockTechnicals.getPrevEma50() != null ? stockTechnicals.getPrevEma50() : 0.0;
        double rsi = stockTechnicals.getRsi();
        long volume = stockTechnicals.getVolume() != null ? stockTechnicals.getVolume() : 0;
        long prevVolume =
                stockTechnicals.getPrevVolume() != null ? stockTechnicals.getPrevVolume() : 0;

        /** 1. Close above prev High */
        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(fundamentalResearchService.marketCap(stockPrice));

        if (close > stockPrice.getPrevHigh() && CandleStickUtils.isGreen(stockPrice)) {
            double changePct =
                    formulaService.calculateChangePercentage(
                            stockPrice.getPrevClose(), stockPrice.getClose());

            boolean isEma5Incr = ema5 > prevEma5;
            boolean isEma20Incr = ema20 > prevEma20;
            boolean isEma50Incr = ema50 > prevEma50;

            boolean isEma5LowRejected = ema5 > 0.0 && low < ema5 && close > ema5;
            boolean isEma20LowRejected = ema20 > 0.0 && low < ema20 && close > ema20;
            boolean isEma50LowRejected = ema50 > 0.0 && low < ema50 && close > ema50;

            boolean isLowRejected = isEma5LowRejected || isEma20LowRejected || isEma50LowRejected;

            boolean isPrevEma5LowRejected =
                    prevEma5 > 0.0 && prevLow < prevEma5 && prevClose > prevEma5;
            boolean isPrevEma20LowRejected =
                    prevEma20 > 0.0 && prevLow < prevEma20 && prevClose > prevEma20;
            boolean isPrevEma50LowRejected =
                    prevEma50 > 0.0 && prevLow < prevEma50 && prevClose > prevEma50;

            boolean isPrevLowRejected =
                    isPrevEma5LowRejected || isPrevEma20LowRejected || isPrevEma50LowRejected;

            boolean isEma5Breakout = prevClose < ema5 && close > ema5;
            boolean isEma20Breakout = prevClose < ema20 && close > ema20;
            boolean isEma50Breakout = prevClose < ema50 && close > ema50;

            boolean isBreakout = isEma20Breakout || isEma50Breakout;

            boolean isPrevEma5Breakout = prev2Close < prevEma5 && prevClose > prevEma5;
            boolean isPrevEma20Breakout = prev2Close < prevEma20 && prevClose > prevEma20;
            boolean isPrevEma50Breakout = prev2Close < prevEma50 && prevClose > prevEma50;

            boolean isPrevBreakout =
                    isPrevEma5Breakout || isPrevEma20Breakout || isPrevEma50Breakout;

            boolean isAllIncr =
                    (isEma5Incr && isEma20Incr && isEma50Incr)
                            || (isEma5Incr
                                    && isEma50Incr
                                    && (isEma20LowRejected || isEma20Breakout))
                            || (isEma20Incr && isEma50Incr && (isEma5LowRejected || isEma5Breakout))
                            || (isEma5Incr
                                    && isEma20Incr
                                    && (isEma50LowRejected || isEma50Breakout));

            boolean isEmaSatisfied = ema20 > 0.0 && ema50 > 0.0;

            boolean rsiFilter = false;

            if (rsi < 60) {
                rsiFilter = changePct < 30;
            } else if (rsi < 65) {
                rsiFilter = changePct < 20;
            } else if (rsi < 75) {
                rsiFilter = changePct < 18;
            } else {
                rsiFilter = false;
            }

            boolean isVolumeNotOverExtended =
                    (volume / prevVolume) < 5.0
                            && ((volume / prevVolume) > 1.0
                                    || stockTechnicals.getVolumeAvg10()
                                            > stockTechnicals.getPrevVolumeAvg10());

            boolean isWeakVolume =
                    (volume < prevVolume
                            && stockTechnicals.getVolumeAvg10()
                                    < stockTechnicals.getPrevVolumeAvg10());

            if (!isWeakVolume
                    && isVolumeNotOverExtended
                    && rsiFilter
                    && isEmaSatisfied
                    && isAllIncr
                    && (CandleStickUtils.isPrevSessionRed(stockPrice)
                            || isPrevLowRejected
                            || isPrevBreakout
                            || isLowRejected
                            || isBreakout)) {
                boolean isPrevDoji =
                        CandleStickUtils.isPrevVerySmallBody(stockPrice) && prevClose > prevEma5;

                boolean isDojiConfirmed =
                        isPrevDoji
                                && !CandleStickUtils.isVerySmallBody(stockPrice)
                                && CandleStickUtils.isRed(stockPrice);

                boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice) && close > ema5;

                if (!isDojiConfirmed && !isDoji) {

                    return true;
                }
            }
        }
        return false;
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
        double close = stockPrice.getClose();
        if (close > stockPrice.getPrevHigh()
                && (CandleStickUtils.isUpperWickWithinLimit(stockPrice)
                        || (CandleStickUtils.isPrevSessionRed(stockPrice)
                                && (CandleStickUtils.isPrevSmallBody(stockPrice, stockTechnicals)
                                        || CandleStickUtils.isUpperWickWithinLimit(
                                                stockPrice, 32.5))))) {
            double ema5 = stockTechnicals.getEma5();

            boolean isCloseBelowEma5OREma5Decreasing =
                    (close < ema5 && CandleStickUtils.isRed(stockPrice))
                            || (stockTechnicals.getPrevEma5() != null
                                    && ema5 < stockTechnicals.getPrevEma5());

            if (!isCloseBelowEma5OREma5Decreasing) {
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();

                if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    if (close > ema20 && close > ema50) {
                        long volumeAvg = stockTechnicals.getVolumeAvg10();
                        long volume = stockTechnicals.getVolume();
                        if (volume > volumeAvg * 1.5) {

                            // Daily check

                            // StockPrice stockPriceDaily = getStockPriceFromMap(Timeframe.DAILY,
                            // stock, sessionDate);

                            StockPrice stockPriceDaily =
                                    updatePriceService.buildBack(
                                            Timeframe.DAILY, stock, sessionDate);
                            StockTechnicals stockTechnicalsDaily =
                                    updateTechnicalsService.buildBack(
                                            Timeframe.DAILY, stock, sessionDate);

                            Optional<StockAnalysis> stockAnalysisOptional =
                                    dailyEntryValidator.isValid2(
                                            stockPrice,
                                            stockPriceDaily,
                                            stockTechnicals,
                                            stockTechnicalsDaily,
                                            sessionDate,
                                            ResearchTechnical.Strategy.ALPHA);
                            if (stockAnalysisOptional.isPresent()) {
                                stockAnalysed.add(stockAnalysisOptional.get());
                            }
                        }
                    }
                }
            }
        }

        return stockAnalysed;
    }

    public List<StockAnalysis> scan(
            StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate currentDate) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        double close = stockPrice.getClose();
        if (close > stockPrice.getPrevHigh()
                && (CandleStickUtils.isUpperWickWithinLimit(stockPrice)
                        || (CandleStickUtils.isPrevSessionRed(stockPrice)
                                && (CandleStickUtils.isPrevSmallBody(stockPrice, stockTechnicals)
                                        || CandleStickUtils.isUpperWickWithinLimit(
                                                stockPrice, 32.5))))) {
            double ema5 = stockTechnicals.getEma5();

            boolean isCloseBelowEma5OREma5Decreasing =
                    (close < ema5 && CandleStickUtils.isRed(stockPrice))
                            || (stockTechnicals.getPrevEma5() != null
                                    && ema5 < stockTechnicals.getPrevEma5());

            if (!isCloseBelowEma5OREma5Decreasing) {
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();

                if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    if (close > ema20 && close > ema50) {
                        long volumeAvg = stockTechnicals.getVolumeAvg10();
                        long volume = stockTechnicals.getVolume();

                        long prevVolumeAvg = stockTechnicals.getPrevVolumeAvg10();

                        long prevVolume = stockTechnicals.getPrevVolume();

                        boolean isVolumeIndicator =
                                (volume > volumeAvg * 1.5)
                                        || (volume > prevVolume * 1.5)
                                        || (volume > prevVolume && volumeAvg > prevVolumeAvg)
                                        || (volume < prevVolume && volumeAvg < prevVolumeAvg);

                        if (isVolumeIndicator) {
                            double low = stockPrice.getLow();
                            double high = stockPrice.getHigh();
                            boolean isOnTop = ema5 > ema20 && close > ema5 && low > ema5;
                            boolean heavyVolumeOnTop =
                                    isOnTop
                                            && ((volume > volumeAvg * 1.5)
                                                    || (volume > prevVolume * 1.5));

                            double ema5DistanceToLow =
                                    formulaService.calculateChangePercentage(ema5, low);
                            double ema5DistanceToHigh =
                                    formulaService.calculateChangePercentage(ema5, high);
                            if (!heavyVolumeOnTop
                                    && ema5DistanceToLow < 2.0
                                    && ema5DistanceToHigh < 20.0) {

                                double ema5And20Diff = ema5 - ema20;
                                double prevEma5And20Diff =
                                        stockTechnicals.getPrevEma5()
                                                - stockTechnicals.getPrevEma20();

                                if (ema5And20Diff > prevEma5And20Diff) {

                                    // Daily check
                                    LocalDate previousMonthLastDay =
                                            calendarService.previousTradingSession(
                                                    currentDate.withDayOfMonth(1));
                                    LocalDate currentMonthFirstSession =
                                            calendarService.nextTradingSession(
                                                    previousMonthLastDay);

                                    LocalDate currentMonthSecondSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthFirstSession);

                                    LocalDate currentMonthThirdSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthSecondSession);

                                    LocalDate currentMonthForthSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthSecondSession);

                                    LocalDate currentMonthFifthSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthSecondSession);

                                    LocalDate currentMonthSixthSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthSecondSession);
                                    LocalDate sessionDate = currentMonthFirstSession;
                                    LocalDate sessionDateTill = currentMonthSixthSession;
                                    LocalDate ohlcvFrom = currentMonthFirstSession;

                                    while (sessionDate.isBefore(sessionDateTill)) {

                                        OHLCV curentMonthOlcv =
                                                monthlySupportResistanceService
                                                        .supportAndResistance(
                                                                stock.getNseSymbol(),
                                                                ohlcvFrom,
                                                                sessionDate);

                                        boolean interactsWithHigherTimeframe =
                                                (stockPrice.getClose() >= curentMonthOlcv.getOpen())
                                                        || (stockPrice.getClose()
                                                                >= curentMonthOlcv.getLow())
                                                        || (curentMonthOlcv.getLow()
                                                                >= stockPrice.getLow());

                                        if (interactsWithHigherTimeframe) {

                                            // StockPrice stockPriceDaily =
                                            // getStockPriceFromMap(Timeframe.DAILY, stock,
                                            // sessionDate);

                                            StockPrice stockPriceDaily =
                                                    updatePriceService.buildBack(
                                                            Timeframe.DAILY, stock, sessionDate);

                                            if (stockPriceDaily.getClose()
                                                    > stockPrice.getClose()) {

                                                // StockTechnicals stockTechnicalsDaily =
                                                // getStockTechnicalsFromMap(Timeframe.DAILY, stock,
                                                // sessionDate);
                                                StockTechnicals stockTechnicalsDaily =
                                                        updateTechnicalsService.buildBack(
                                                                Timeframe.DAILY,
                                                                stock,
                                                                sessionDate);
                                                Optional<StockAnalysis> stockAnalysisOptional =
                                                        dailyEntryValidator.isValid2(
                                                                stockPrice,
                                                                stockPriceDaily,
                                                                stockTechnicals,
                                                                stockTechnicalsDaily,
                                                                sessionDate,
                                                                ResearchTechnical.Strategy.ALPHA);

                                                if (stockAnalysisOptional.isPresent()) {
                                                    stockAnalysed.add(stockAnalysisOptional.get());
                                                    break;
                                                }
                                            }
                                        }

                                        sessionDate =
                                                calendarService.nextTradingSession(sessionDate);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return stockAnalysed;
    }
}
