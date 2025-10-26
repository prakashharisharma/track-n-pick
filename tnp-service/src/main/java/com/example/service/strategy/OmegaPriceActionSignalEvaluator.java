package com.example.service.strategy;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.dto.common.OHLCV;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SignalEvaluatorHelperService;
import com.example.service.utils.SubStrategyHelper;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("omegaPriceActionSignalEvaluator")
public class OmegaPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final MonthlySupportResistanceService monthlySupportResistanceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final StockPriceService<StockPrice> stockPriceService;

    private final CalendarService calendarService;
    private final MiscUtil miscUtil;
    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;
    private final CandleStickConfirmationService candleStickConfirmationService;
    /**
     * 1. 4 Incr 2. Avg ++ vol ++ 3. Low Rejected and lowerWick > upperWick 4. Avg++ Reject 1. All
     * incr and PRevGreen
     *
     * @param timeframe the timeframe to evaluate (e.g., DAILY, WEEKLY)
     * @param stock the stock to evaluate
     * @param stockPrice the current stock price information
     * @param stockTechnicals the derived technical indicators for the stock
     * @return
     */
    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        // StockPrice monthlyStockPrice = stockPriceService.get(stock, Timeframe.MONTHLY);
        // StockTechnicals monthlyStockTechnicals = stockTechnicalsService.get(stock,
        // Timeframe.MONTHLY);

        StockPrice monthlyStockPrice =
                updatePriceService.buildBack(
                        Timeframe.MONTHLY,
                        stock,
                        calendarService.previousTradingSession(miscUtil.currentDate()));
        StockTechnicals monthlyStockTechnicals =
                updateTechnicalsService.buildBack(
                        Timeframe.MONTHLY,
                        stock,
                        calendarService.previousTradingSession(miscUtil.currentDate()));

        double researchPrice = 0.0;

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

        if (monthlyStockPrice != null && monthlyStockTechnicals != null) {

            LocalDate sessionDate = stockPrice.getSessionDate();

            System.out.println("sessionDate1 " + sessionDate);

            LocalDate firstOfMonth =
                    calendarService.nextTradingSession(miscUtil.previousMonthLastDay());
            LocalDate currentMonthThirdSession =
                    calendarService.nextTradingSession(
                            calendarService.nextTradingSession(firstOfMonth));

            if (sessionDate != null
                    && (!sessionDate.isBefore(firstOfMonth)
                            && !sessionDate.isAfter(currentMonthThirdSession))) {
                //  if (sessionDate != null && sessionDate.equals(firstOfMonth)) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe,
                                Timeframe.MONTHLY,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                monthlyStockPrice,
                                monthlyStockTechnicals);
            }
        }
        /*
                if (subStrategyRef.isEmpty()) {

                     monthlyStockPrice = stockPriceService.get(stock, Timeframe.WEEKLY);
                     monthlyStockTechnicals =
                            stockTechnicalsService.get(stock, Timeframe.WEEKLY);

                    LocalDate sessionDate = stockPrice.getSessionDate();

                    LocalDate firstOfWeek =
                            calendarService.nextTradingSession(miscUtil.previousWeekLastDay());
                    LocalDate currentWeekSecondSession = calendarService.nextTradingSession(firstOfWeek);

                    if (sessionDate != null
                            && (!sessionDate.isBefore(firstOfWeek)
                                    && !sessionDate.isAfter(currentWeekSecondSession))) {
                        // if (sessionDate != null && sessionDate.equals(firstOfMonth)) {
                        subStrategyRef =
                                confirmBreakout(
                                        timeframe,
                                        Timeframe.WEEKLY,
                                        stock,
                                        stockPrice,
                                        stockTechnicals,
                                        monthlyStockPrice,
                                        monthlyStockTechnicals);
                    }
                }
        */
        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.OMEGA)
                    .subStrategy(subStrategyRef.get())
                    .researchPrice(researchPrice)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Timeframe higherTimeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            StockPrice higherTimeframeStockPrice,
            StockTechnicals higherTimeframeStockTechnicals) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (higherTimeframeStockPrice == null || higherTimeframeStockTechnicals == null) {
            return Optional.empty();
        }

        if (timeframe != Timeframe.DAILY) {
            return Optional.empty();
        }

        if (MovingAverageUtil.getMovingAverage50(higherTimeframe, higherTimeframeStockTechnicals)
                        <= 0.0
                && MovingAverageUtil.getMovingAverage100(
                                higherTimeframe, higherTimeframeStockTechnicals)
                        <= 0.0
                && MovingAverageUtil.getMovingAverage200(
                                higherTimeframe, higherTimeframeStockTechnicals)
                        <= 0.0) {
            return Optional.empty();
        }

        boolean isLowRejected =
                higherTimeframeStockPrice.getLow() < higherTimeframeStockTechnicals.getEma5()
                        || higherTimeframeStockPrice.getLow()
                                < higherTimeframeStockTechnicals.getEma20();

        if (MovingAverageUtil.increasingMaCount(higherTimeframeStockTechnicals) >= 5
                && CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                && !isLowRejected) {
            return Optional.empty();
        }

        double htEma5 =
                MovingAverageUtil.getMovingAverage5(
                        higherTimeframeStockPrice.getTimeframe(), higherTimeframeStockTechnicals);

        boolean isHtHigherHighAndHigherLow =
                CandleStickUtils.isHigherHigh(higherTimeframeStockPrice)
                        && CandleStickUtils.isHigherLow(higherTimeframeStockPrice);

        boolean isHtLongUpperWick =
                CandleStickUtils.upperWickSize(higherTimeframeStockPrice)
                        > CandleStickUtils.lowerWickSize(higherTimeframeStockPrice);

        if (CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                && (!isHtHigherHighAndHigherLow
                        || higherTimeframeStockPrice.getClose() < htEma5
                        || isHtLongUpperWick)) {
            return Optional.empty();
        }

        double htEma20 =
                MovingAverageUtil.getMovingAverage20(
                        higherTimeframeStockTechnicals.getTimeframe(),
                        higherTimeframeStockTechnicals);

        if (higherTimeframeStockPrice.getClose() < htEma20) {
            return Optional.empty();
        }

        // Monthly Align Bullish
        if (MovingAverageUtil.isAllMaAlignedBullish(
                higherTimeframeStockTechnicals.getTimeframe(), higherTimeframeStockTechnicals)) {

            Optional<MAEvaluationResult> evaluationResultOptional =
                    dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                            timeframe,
                            higherTimeframeStockPrice,
                            higherTimeframeStockTechnicals,
                            false);

            boolean isNearSupport =
                    evaluationResultOptional.isPresent()
                            && (evaluationResultOptional.get().isNearSupport());

            // Monthly closed above ema5
            if (higherTimeframeStockPrice.getClose()
                            > MovingAverageUtil.getMovingAverage5(
                                    higherTimeframeStockTechnicals.getTimeframe(),
                                    higherTimeframeStockTechnicals)
                    || isLowRejected
                    || isNearSupport) {

                // Monthly REd and prev Green
                if ((CandleStickUtils.isRed(higherTimeframeStockPrice)
                                && (CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                                        || CandleStickUtils.isPrev2SessionGreen(
                                                higherTimeframeStockPrice)
                                        || CandleStickUtils.isPrev3SessionGreen(
                                                higherTimeframeStockPrice)
                                        || isNearSupport))
                        || (CandleStickUtils.isGreen(higherTimeframeStockPrice) && isLowRejected)) {
                    // Above Green or condition added later
                    if (!(CandleStickUtils.isRed(higherTimeframeStockPrice)
                            && CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                            && higherTimeframeStockPrice.getOpen()
                                    > higherTimeframeStockPrice.getPrevClose())) {

                        // monthly HL or low rejected
                        if (CandleStickUtils.isHigherLow(higherTimeframeStockPrice)
                                || isLowRejected) {

                            if (!CandleStickUtils.isUpperWickDominant(higherTimeframeStockPrice)) {
                                if (higherTimeframeStockTechnicals.getEma5()
                                        > higherTimeframeStockTechnicals.getEma20()) {
                                    if (!CandleStickUtils.isPrevUpperWickDominant(
                                            higherTimeframeStockPrice)) {
                                        if (MovingAverageUtil.increasingMaCount(
                                                        higherTimeframeStockTechnicals)
                                                >= 2) {

                                            LocalDate firstOfMonth =
                                                    miscUtil.currentDate().withDayOfMonth(1);

                                            OHLCV ohlcv =
                                                    monthlySupportResistanceService
                                                            .supportAndResistance(
                                                                    stock.getNseSymbol(),
                                                                    firstOfMonth,
                                                                    stockPrice.getSessionDate());

                                            double htClose = higherTimeframeStockPrice.getClose();
                                            double htLow = higherTimeframeStockPrice.getLow();
                                            double open = ohlcv.getOpen();
                                            double low = ohlcv.getLow();

                                            // Monthly close >= open or Monthly close >= low

                                            boolean interactsWithHigherTimeframe =
                                                    htClose >= open
                                                            || htClose >= low
                                                            || (open >= htClose && low >= htLow)
                                                            || (open >= htClose && low <= htClose);

                                            if (interactsWithHigherTimeframe) {
                                                // Is Daily MA align Bullish
                                                if (MovingAverageUtil.isAllMaAlignedBullish(
                                                                stockTechnicals.getTimeframe(),
                                                                stockTechnicals)
                                                        || (MovingAverageUtil.isAllMaAlignedBearish(
                                                                stockTechnicals.getTimeframe(),
                                                                stockTechnicals))) {
                                                    // Daily close > monthly close
                                                    if (stockPrice.getClose()
                                                            > higherTimeframeStockPrice
                                                                    .getClose()) {

                                                        // Prev close <= monthly close
                                                        if (stockPrice.getPrevClose()
                                                                <= higherTimeframeStockPrice
                                                                        .getClose()) {
                                                            if (CandleStickUtils.isGreen(
                                                                    stockPrice)) {

                                                                if (CandleStickUtils
                                                                                .isPrevSessionRed(
                                                                                        stockPrice)
                                                                        || CandleStickUtils
                                                                                .isPrevLowerWickDominant(
                                                                                        stockPrice)
                                                                        || (CandleStickUtils
                                                                                                .prevSessionBodySize(
                                                                                                        stockPrice)
                                                                                        < CandleStickUtils
                                                                                                .bodySize(
                                                                                                        stockPrice)
                                                                                && !CandleStickUtils
                                                                                        .isPrevUpperWickDominant(
                                                                                                stockPrice))) {

                                                                    if (!CandleStickUtils
                                                                            .isUpperWickDominant(
                                                                                    stockPrice)) {

                                                                        if ((stockPrice.getClose()
                                                                                        > MovingAverageUtil
                                                                                                .getMovingAverage200(
                                                                                                        timeframe,
                                                                                                        stockTechnicals))
                                                                                || (CandleStickUtils
                                                                                                .isHigherHigh(
                                                                                                        stockPrice)
                                                                                        && (this
                                                                                                .isVolumeSurge(
                                                                                                        stockTechnicals)))) {

                                                                            if (this
                                                                                    .isEma5Respected(
                                                                                            stockPrice,
                                                                                            stockTechnicals)) {
                                                                                double ema5 =
                                                                                        MovingAverageUtil
                                                                                                .getMovingAverage5(
                                                                                                        timeframe,
                                                                                                        stockTechnicals);
                                                                                if (stockPrice
                                                                                                        .getClose()
                                                                                                > ema5
                                                                                        && stockPrice
                                                                                                        .getLow()
                                                                                                < ema5) {

                                                                                    if (!(CandleStickUtils
                                                                                                    .isLowerLow(
                                                                                                            stockPrice)
                                                                                            && CandleStickUtils
                                                                                                    .isLowerHigh(
                                                                                                            stockPrice))) {

                                                                                        if (MovingAverageUtil
                                                                                                        .increasingMaCount(
                                                                                                                stockTechnicals)
                                                                                                >= 3) {
                                                                                            if (candleStickConfirmationService
                                                                                                    .isUpperWickSizeConfirmed(
                                                                                                            timeframe,
                                                                                                            stockPrice,
                                                                                                            stockTechnicals)) {
                                                                                                if (higherTimeframe
                                                                                                        == Timeframe
                                                                                                                .WEEKLY) {
                                                                                                    return SubStrategyHelper
                                                                                                            .resolveByName(
                                                                                                                    "weekly_breakout");
                                                                                                }

                                                                                                System
                                                                                                        .out
                                                                                                        .println(
                                                                                                                "FoundNi"
                                                                                                                    + " "
                                                                                                                        + stock
                                                                                                                                .getNseSymbol());
                                                                                                return SubStrategyHelper
                                                                                                        .resolveByName(
                                                                                                                "monthly_breakout");
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
                            }
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean isEma5Respected(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double close = stockPrice.getClose();
        double open = stockPrice.getOpen();
        double prevClose = stockPrice.getPrevClose();
        double prevOpen = stockPrice.getPrevOpen();
        double ema5 =
                MovingAverageUtil.getMovingAverage5(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        if (close > ema5) {
            return true;
        }

        boolean isHigherHighAndHigherLow =
                CandleStickUtils.isHigherHigh(stockPrice)
                        && CandleStickUtils.isHigherLow(stockPrice);

        boolean isVolumeAvgIncreasing =
                stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20();
        boolean isVolumeIncreasing = stockTechnicals.getVolume() > stockTechnicals.getPrevVolume();
        boolean isVolAboveAvg = stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20();

        if (isHigherHighAndHigherLow
                && isVolumeAvgIncreasing
                && isVolumeIncreasing
                && isVolAboveAvg) {
            return true;
        }

        // Engulfing
        if (CandleStickUtils.isGreen(stockPrice) && CandleStickUtils.isPrevSessionRed(stockPrice)) {
            if (open < prevClose && close > prevOpen) {
                return true;
            }
        }

        return false;
    }

    private boolean isVolumeSurge(StockTechnicals stockTechnicals) {

        long avg = stockTechnicals.getVolumeAvg20();
        long prevAvg = stockTechnicals.getPrevVolumeAvg20();
        long volume = stockTechnicals.getVolume();
        long prevVolume = stockTechnicals.getPrevVolume();

        if (avg > prevAvg) {
            if (volume > avg) {
                return true;
            }
        }

        if (prevVolume > volume * 2) {
            if (prevVolume > prevAvg) {
                return true;
            }
        }

        return false;
    }
}
