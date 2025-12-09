package com.example.service.scanner;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.OHLCV;
import com.example.dto.io.StockAnalysis;
import com.example.service.*;
import com.example.service.scanner.validator.DailyEntryValidator;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
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
public class GamaScanner {

    private final MiscUtil miscUtil;
    private final CalendarService calendarService;

    private final FormulaService formulaService;

    private final DailyEntryValidator dailyEntryValidator;

    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;

    private final MonthlySupportResistanceService monthlySupportResistanceService;

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;

    public boolean isValid(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double close = stockPrice.getClose();

        double ema5 = stockTechnicals.getEma5();
        double ema10 = stockTechnicals.getEma10();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();

        double ema100 =
                MovingAverageUtil.getMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevEma10 = stockTechnicals.getPrevEma10();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevEma50 = stockTechnicals.getPrevEma50();

        double prevEma100 =
                MovingAverageUtil.getPrevMovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        double prev2Ema10 = stockTechnicals.getPrev2Ema10();
        double prev2Ema20 = stockTechnicals.getPrev2Ema20();
        double prev2Ema50 = stockTechnicals.getPrev2Ema50();

        double prev2Ema100 =
                MovingAverageUtil.getPrev2MovingAverage100(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prev2Ema200 =
                MovingAverageUtil.getPrev2MovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        double prevOpen = stockPrice.getPrevOpen();
        double prevLow = stockPrice.getPrevLow();
        double prevClose = stockPrice.getPrevClose();

        boolean isPrevLowRejectedEma5 =
                prevLow <= prevEma5
                        && prevClose > prevEma5
                        && prevEma5 > prevEma10
                        && prevEma10 <= ema10
                        && prevEma20 <= ema20;

        boolean isPrevLowRejectedEma10 =
                prevLow <= prevEma10
                        && prevClose > prevEma10
                        && prevEma10 > prevEma20
                        && prevEma20 <= ema20
                        && prevEma50 <= ema50;

        boolean isPrevLowRejectedEma20 =
                prevLow <= prevEma20
                        && prevClose > prevEma20
                        && prevEma20 > prevEma50
                        && prevEma50 <= ema50
                        && prevEma100 <= ema100;

        boolean isPrevLowRejectedEma50 =
                prevLow <= prevEma50
                        && prevClose > prevEma50
                        && prevEma50 > prevEma100
                        && prevEma100 <= ema100
                        && prevEma200 <= ema200;

        boolean isPrevLowRejectedEma100 =
                prevLow <= prevEma100
                        && prevClose > prevEma100
                        && prevEma100 > prevEma200
                        && ema100 > prevEma100
                        && prev2Ema200 >= prevEma200;
        ;

        boolean isPrevLowRejected =
                isPrevLowRejectedEma5
                        || isPrevLowRejectedEma10
                        || isPrevLowRejectedEma20
                        || isPrevLowRejectedEma50
                        || isPrevLowRejectedEma100;

        boolean isEma5Breakout =
                prevClose < prevEma5
                        && close > ema5
                        && close > formulaService.applyPercentChange(ema5, 1.5);
        boolean isEma20Breakout =
                prevClose < prevEma20
                        && close > ema20
                        && close > formulaService.applyPercentChange(ema20, 1.5);
        boolean isEma50Breakout =
                prevClose < prevEma50
                        && close > ema50
                        && close > formulaService.applyPercentChange(ema50, 1.5);
        boolean isEma200Breakout =
                prevClose < prevEma200
                        && close > ema200
                        && close > formulaService.applyPercentChange(ema200, 1.5);

        boolean isBreakout =
                isEma5Breakout || isEma20Breakout || isEma50Breakout || isEma200Breakout;

        if (isPrevLowRejected) {
            long volume = stockTechnicals.getVolume();
            long prevVolume = stockTechnicals.getPrevVolume();
            long volume3MonthAvg = (stockTechnicals.getPrev2Volume() + prevVolume + volume) / 3;
            boolean isVolumeSatisfied = (volume > prevVolume) || volume > volume3MonthAvg;
            if (isVolumeSatisfied) {
                boolean isRsiInRange =
                        stockTechnicals.getRsi() > 40 && stockTechnicals.getRsi() < 65;
                if (isRsiInRange) {
                    // if(isHammer(stockPrice, stockTechnicals) || isEngulfing(stockPrice,
                    // stockTechnicals) || isTweezerBottom(stockPrice)) {

                    StockPrice stockPriceDaily =
                            updatePriceService.buildBack(
                                    Timeframe.WEEKLY,
                                    stockPrice.getStock(),
                                    stockPrice.getSessionDate().plusWeeks(1));
                    if (stockPriceDaily.getClose() > stockPriceDaily.getPrevClose()
                            || CandleStickUtils.isGreen(stockPriceDaily)) {
                        return true;
                    } else if (stockPriceDaily.getPrevClose() > stockPriceDaily.getPrev2Close()
                            || CandleStickUtils.isPrevSessionGreen(stockPriceDaily)) {
                        return true;
                    }
                    // }
                }
            }
        }

        return false;
    }

    public boolean isBreakout(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (CandleStickUtils.isGreen(stockPrice)) {
            double close = stockPrice.getClose();
            double prevClose = stockPrice.getPrevClose();

            double ema5 = stockTechnicals.getEma5();
            double prevEma5 = stockTechnicals.getPrevEma5();

            double ema20 = stockTechnicals.getEma20();
            double prevEma20 = stockTechnicals.getPrevEma20();

            double ema50 = stockTechnicals.getEma50();
            double prevEma50 = stockTechnicals.getPrevEma50();

            double ema200 =
                    MovingAverageUtil.getMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma200 =
                    MovingAverageUtil.getPrevMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);

            boolean isBreakoutEma5 =
                    close > ema5 * 1.015 && prevClose < prevEma5 && close > ema5 && (close > ema20);
            boolean isBreakoutEma20 =
                    close > ema20 * 1.015
                            && prevClose < prevEma20
                            && close > ema20
                            && (close > ema50);
            ;
            boolean isBreakoutEma50 =
                    close > ema50 * 1.015
                            && prevClose < prevEma50
                            && close > ema50
                            && (close > ema200);
            ;

            boolean isBreakout = isBreakoutEma50 || isBreakoutEma20 || isBreakoutEma5;

            if (isBreakout) {
                long volume = stockTechnicals.getVolume();
                long prevVolume = stockTechnicals.getPrevVolume();
                long volume3MonthAvg = (stockTechnicals.getPrev2Volume() + prevVolume + volume) / 3;
                boolean isVolumeSatisfied = (volume > prevVolume) || volume > volume3MonthAvg;
                if (isVolumeSatisfied) {
                    boolean isRsiInRange =
                            stockTechnicals.getRsi() > 45 && stockTechnicals.getRsi() < 65;
                    if (isRsiInRange) {
                        if (isHammer(stockPrice, stockTechnicals)
                                || isEngulfing(stockPrice, stockTechnicals)
                                || isTweezerBottom(stockPrice)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean isSupport(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double low = stockPrice.getLow();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();

        double ema5 = stockTechnicals.getEma5();
        double prevEma5 = stockTechnicals.getPrevEma5();

        double ema20 = stockTechnicals.getEma20();
        double prevEma20 = stockTechnicals.getPrevEma20();

        double ema50 = stockTechnicals.getEma50();
        double prevEma50 = stockTechnicals.getPrevEma50();

        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        boolean isEma20Support = (low <= ema20 * 1.01) && close > ema20 && (ema50 > prevEma50);
        boolean isEma50Support = (low <= ema50 * 1.01) && close > ema50 && (ema200 > prevEma200);
        ;

        boolean isEmaSupport = isEma20Support || isEma50Support;

        if (isEmaSupport) {
            long volume = stockTechnicals.getVolume();
            long prevVolume = stockTechnicals.getPrevVolume();
            long volume2MonthAvg = (prevVolume + volume) / 2;
            boolean isVolumeSatisfied =
                    (volume > stockTechnicals.getVolumeAvg10()) || volume > volume2MonthAvg;
            if (isVolumeSatisfied) {
                boolean isRsiInRange =
                        stockTechnicals.getRsi() > 45 && stockTechnicals.getRsi() < 65;
                if (isRsiInRange) {
                    if (isHammer(stockPrice, stockTechnicals)
                            || isEngulfing(stockPrice, stockTechnicals)
                            || isTweezerBottom(stockPrice)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isHammer(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (CandleStickUtils.isLowerWickDominant(stockPrice)) {
            if (CandleStickUtils.isSmallBody(stockPrice, stockTechnicals)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEngulfing(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (CandleStickUtils.isGreen(stockPrice)) {
            if (stockPrice.getOpen() < stockPrice.getPrevClose()
                    && stockPrice.getClose() > stockPrice.getPrevOpen()) {
                return true;
            }
        }

        return false;
    }

    public boolean isTweezerBottom(StockPrice stockPrice) {
        if (CandleStickUtils.isGreen(stockPrice)) {
            if (isEqualWithinTolerance(stockPrice.getPrevClose(), stockPrice.getOpen(), 0.05)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEqualWithinTolerance(double a, double b, double percentTolerance) {
        double diffPercent = Math.abs(a - b) / a * 100.0;
        return diffPercent <= percentTolerance;
    }

    private boolean isMonthlySatisfied(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        boolean isMAAligned =
                (ema5 > ema20 && ema20 > 0.00)
                        || (ema20 > ema50 && ema5 > ema50 && ema50 > 0.0)
                        || (ema50 > ema200 && ema20 > ema200 && ema200 > 0.0);

        if (isMAAligned) {

            boolean isUpperWickDominant =
                    CandleStickUtils.isUpperWickDominant(stockPrice)
                            && stockPrice.getOpen() > ema5
                            && stockPrice.getClose() > ema5;
            if (!isUpperWickDominant) {
                double prevEma5 =
                        stockTechnicals.getPrevEma5() != null ? stockTechnicals.getPrevEma5() : 0.0;
                boolean isPrevUpperWickDominant =
                        CandleStickUtils.isPrevUpperWickDominant(stockPrice)
                                && stockPrice.getPrevOpen() > prevEma5
                                && stockPrice.getPrevClose() > prevEma5;

                if (!isPrevUpperWickDominant) {

                    return true;
                }
            }
        }
        return false;
    }

    public List<StockAnalysis> scan(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        boolean isHigherLow = CandleStickUtils.isHigherLow(stockPrice);
        boolean isLowerHigh = CandleStickUtils.isLowerHigh(stockPrice);
        boolean isPrevLowerHigh = CandleStickUtils.isPrevLowerHigh(stockPrice);
        boolean isPrev2LowerHigh = CandleStickUtils.isPrev2LowerHigh(stockPrice);
        double close = stockPrice.getClose();
        if (CandleStickUtils.isGreen(stockPrice)
                && close < stockPrice.getPrevOpen()
                && isHigherLow
                && isLowerHigh
                && isPrevLowerHigh
                && isPrev2LowerHigh) {

            //  System.out.println(" Here1 " + stock.getNseSymbol());

            double ema5 = stockTechnicals.getEma5();
            double ema20 = stockTechnicals.getEma20();
            double ema50 = stockTechnicals.getEma50();
            double ema100 =
                    MovingAverageUtil.getMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double ema200 =
                    MovingAverageUtil.getMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma5 = stockTechnicals.getPrevEma5();
            double prevEma20 = stockTechnicals.getPrevEma20();
            double prevEma50 = stockTechnicals.getPrevEma50();
            double prevEma100 =
                    MovingAverageUtil.getPrevMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma200 =
                    MovingAverageUtil.getPrevMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);

            double prevEma5Weighted = formulaService.applyPercentChange(prevEma5, 2.0);
            double prevEma20Weighted = formulaService.applyPercentChange(prevEma20, 2.0);
            double prevEma50Weighted = formulaService.applyPercentChange(prevEma50, 2.0);
            double prevEma100Weighted = formulaService.applyPercentChange(prevEma100, 2.0);
            double prevEma200Weighted = formulaService.applyPercentChange(prevEma200, 2.0);

            double prevEma5WeightedNegative = formulaService.applyPercentChange(prevEma5, -2.0);
            double prevEma20WeightedNegative = formulaService.applyPercentChange(prevEma20, -2.0);
            double prevEma50WeightedNegative = formulaService.applyPercentChange(prevEma50, -2.0);
            double prevEma100WeightedNegative = formulaService.applyPercentChange(prevEma100, -2.0);
            double prevEma200WeightedNegative = formulaService.applyPercentChange(prevEma200, -2.0);

            double prevOpen = stockPrice.getPrevOpen();
            double prevLow = stockPrice.getPrevLow();
            double prevClose = stockPrice.getPrevClose();

            boolean isLowRejectedEma5 =
                    (prevOpen > prevEma5
                                    && prevLow <= prevEma5Weighted
                                    && prevClose > prevEma5WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema5;

            boolean isLowRejectedEma20 =
                    (prevOpen > prevEma20
                                    && prevLow <= prevEma20Weighted
                                    && prevClose > prevEma20WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema20;

            boolean isLowRejectedEma50 =
                    (prevOpen > prevEma50
                                    && prevLow <= prevEma50Weighted
                                    && prevClose > prevEma50WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema50;

            boolean isLowRejectedEma100 =
                    (prevOpen > prevEma100
                                    && prevLow <= prevEma100Weighted
                                    && prevClose > prevEma100WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema100;

            boolean isLowRejectedEma200 =
                    (prevOpen > prevEma200
                                    && prevLow <= prevEma200Weighted
                                    && prevClose > prevEma200WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema200;

            if (((isLowRejectedEma5 || isLowRejectedEma20 || isLowRejectedEma50)
                            && (ema20 > prevEma20 && ema50 > prevEma50))
                    || ((isLowRejectedEma100 || isLowRejectedEma200)
                            && (ema50 > prevEma50 && ema200 > prevEma200))) {

                boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice);
                boolean isPrevDoji = CandleStickUtils.isPrevVerySmallBody(stockPrice);

                // boolean isUpperWickDominant = CandleStickUtils.isUpperWickDominant(stockPrice);
                // boolean isUpperWickDominant = false;
                if (!isDoji && !isPrevDoji) {
                    // System.out.println(" Here2 " + stock.getNseSymbol());

                    // if(ema20 > prevEma20 && ema50 > prevEma50) {

                    // if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    //  if (close > ema20 && close > ema50) {
                    long volumeAvg = stockTechnicals.getVolumeAvg10();
                    long volume = stockTechnicals.getVolume();
                    //  if (volume > volumeAvg * 1.5) {

                    // Daily check
                    LocalDate currentMonthFirstSession =
                            calendarService.nextTradingSession(miscUtil.previousMonthLastDay());

                    LocalDate currentMonthSecondSession =
                            calendarService.nextTradingSession(currentMonthFirstSession);

                    LocalDate currentMonthThirdSession =
                            calendarService.nextTradingSession(currentMonthSecondSession);
                    LocalDate sessionDate = currentMonthFirstSession;
                    LocalDate sessionDateTill = currentMonthThirdSession;
                    LocalDate ohlcvFrom = currentMonthFirstSession;

                    while (sessionDate.isBefore(sessionDateTill)) {

                        OHLCV curentMonthOlcv =
                                monthlySupportResistanceService.supportAndResistance(
                                        stock.getNseSymbol(), ohlcvFrom, sessionDate);

                        boolean interactsWithHigherTimeframe =
                                (stockPrice.getClose() >= curentMonthOlcv.getOpen())
                                        || (stockPrice.getClose() >= curentMonthOlcv.getLow())
                                        || (curentMonthOlcv.getLow() >= stockPrice.getLow());

                        if (interactsWithHigherTimeframe) {

                            StockPrice stockPriceDaily =
                                    updatePriceService.buildBack(
                                            Timeframe.DAILY, stock, sessionDate);
                            // getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                            if (stockPriceDaily.getClose() > stockPrice.getClose()) {

                                StockTechnicals stockTechnicalsDaily =
                                        updateTechnicalsService.buildBack(
                                                Timeframe.DAILY, stock, sessionDate);
                                // getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);

                                Optional<StockAnalysis> stockAnalysisOptional =
                                        dailyEntryValidator.isValid(
                                                stockPrice,
                                                stockPriceDaily,
                                                stockTechnicals,
                                                stockTechnicalsDaily,
                                                sessionDate,
                                                ResearchTechnical.Strategy.GAMA);

                                if (stockAnalysisOptional.isPresent()) {
                                    stockAnalysed.add(stockAnalysisOptional.get());
                                    break;
                                }
                            }
                        }

                        sessionDate = calendarService.nextTradingSession(sessionDate);
                    }
                }
                // }
            }
        }

        return stockAnalysed;
    }

    public List<StockAnalysis> scanLastDay(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        LocalDate sessionDate =
                calendarService.previousTradingSession(miscUtil.currentDate().withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = miscUtil.currentDate();
        }

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        boolean isHigherLow = CandleStickUtils.isHigherLow(stockPrice);

        boolean isLowerHigh = CandleStickUtils.isLowerHigh(stockPrice);
        double close = stockPrice.getClose();
        if (CandleStickUtils.isGreen(stockPrice)
                && close < stockPrice.getPrevOpen()
                && isHigherLow
                && isLowerHigh) {

            // System.out.println(" Here1 " + stock.getNseSymbol());

            double ema5 = stockTechnicals.getEma5();
            double ema20 = stockTechnicals.getEma20();
            double ema50 = stockTechnicals.getEma50();
            double ema100 =
                    MovingAverageUtil.getMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double ema200 =
                    MovingAverageUtil.getMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma5 = stockTechnicals.getPrevEma5();
            double prevEma20 = stockTechnicals.getPrevEma20();
            double prevEma50 = stockTechnicals.getPrevEma50();
            double prevEma100 =
                    MovingAverageUtil.getPrevMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma200 =
                    MovingAverageUtil.getPrevMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);

            double prevEma5Weighted = formulaService.applyPercentChange(prevEma5, 2.0);
            double prevEma20Weighted = formulaService.applyPercentChange(prevEma20, 2.0);
            double prevEma50Weighted = formulaService.applyPercentChange(prevEma50, 2.0);
            double prevEma100Weighted = formulaService.applyPercentChange(prevEma100, 2.0);
            double prevEma200Weighted = formulaService.applyPercentChange(prevEma200, 2.0);

            double prevEma5WeightedNegative = formulaService.applyPercentChange(prevEma5, -2.0);
            double prevEma20WeightedNegative = formulaService.applyPercentChange(prevEma20, -2.0);
            double prevEma50WeightedNegative = formulaService.applyPercentChange(prevEma50, -2.0);
            double prevEma100WeightedNegative = formulaService.applyPercentChange(prevEma100, -2.0);
            double prevEma200WeightedNegative = formulaService.applyPercentChange(prevEma200, -2.0);

            double prevOpen = stockPrice.getPrevOpen();
            double prevLow = stockPrice.getPrevLow();
            double prevClose = stockPrice.getPrevClose();

            boolean isLowRejectedEma5 =
                    (prevOpen > prevEma5
                                    && prevLow <= prevEma5Weighted
                                    && prevClose > prevEma5WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema5;

            boolean isLowRejectedEma20 =
                    (prevOpen > prevEma20
                                    && prevLow <= prevEma20Weighted
                                    && prevClose > prevEma20WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema20;

            boolean isLowRejectedEma50 =
                    (prevOpen > prevEma50
                                    && prevLow <= prevEma50Weighted
                                    && prevClose > prevEma50WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema50;

            boolean isLowRejectedEma100 =
                    (prevOpen > prevEma100
                                    && prevLow <= prevEma100Weighted
                                    && prevClose > prevEma100WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema100;

            boolean isLowRejectedEma200 =
                    (prevOpen > prevEma200
                                    && prevLow <= prevEma200Weighted
                                    && prevClose > prevEma200WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema200;

            if (((isLowRejectedEma5 || isLowRejectedEma20 || isLowRejectedEma50)
                            && (ema20 > prevEma20 && ema50 > prevEma50))
                    || ((isLowRejectedEma100 || isLowRejectedEma200)
                            && (ema50 > prevEma50 && ema200 > prevEma200))) {

                boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice);
                boolean isPrevDoji = CandleStickUtils.isPrevVerySmallBody(stockPrice);
                // boolean isUpperWickDominant = CandleStickUtils.isUpperWickDominant(stockPrice);

                if (!isDoji && !isPrevDoji) {
                    // if(ema20 > prevEma20 && ema50 > prevEma50){

                    // System.out.println(" Here2 " + stock.getNseSymbol());
                    // if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    //  if (close > ema20 && close > ema50) {
                    // long volumeAvg = stockTechnicals.getVolumeAvg10();
                    // long volume = stockTechnicals.getVolume();
                    //  if (volume > volumeAvg * 1.5) {

                    // Daily check
                    LocalDate currentMonthFirstSession =
                            calendarService.nextTradingSession(miscUtil.previousMonthLastDay());
                    OHLCV curentMonthOlcv =
                            monthlySupportResistanceService.supportAndResistance(
                                    stock.getNseSymbol(), currentMonthFirstSession, sessionDate);

                    boolean interactsWithHigherTimeframe =
                            (stockPrice.getClose() >= curentMonthOlcv.getOpen())
                                    || (stockPrice.getClose() >= curentMonthOlcv.getLow())
                                    || (curentMonthOlcv.getLow() >= stockPrice.getLow());

                    if (interactsWithHigherTimeframe) {

                        StockPrice stockPriceDaily =
                                updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
                        // getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                        if (stockPriceDaily.getClose() > stockPrice.getClose()) {

                            StockTechnicals stockTechnicalsDaily =
                                    updateTechnicalsService.buildBack(
                                            Timeframe.DAILY, stock, sessionDate);
                            // getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);

                            Optional<StockAnalysis> stockAnalysisOptional =
                                    dailyEntryValidator.isValid(
                                            stockPrice,
                                            stockPriceDaily,
                                            stockTechnicals,
                                            stockTechnicalsDaily,
                                            sessionDate,
                                            ResearchTechnical.Strategy.GAMA);
                            if (stockAnalysisOptional.isPresent()) {
                                stockAnalysed.add(stockAnalysisOptional.get());
                            }
                        }
                    }
                }
                //  }
                // }
                // }
                // }
            }
        }

        return stockAnalysed;
    }
}
