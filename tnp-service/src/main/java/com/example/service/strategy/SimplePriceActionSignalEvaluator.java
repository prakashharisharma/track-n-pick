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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("simplePriceActionSignalEvaluator")
public class SimplePriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final MonthlySupportResistanceService monthlySupportResistanceService;
    private final WeeklySupportResistanceService weeklySupportResistanceService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final RsiIndicatorService rsiIndicatorService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final AdxIndicatorService adxIndicatorService;

    private final ResistanceValidationService resistanceValidationService;

    private final CalendarService calendarService;
    private final MiscUtil miscUtil;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        StockPrice monthlyStockPrice = stockPriceService.get(stock, Timeframe.MONTHLY);
        StockTechnicals monthlyStockTechnicals =
                stockTechnicalsService.get(stock, Timeframe.MONTHLY);

        double researchPrice = 0.0;
        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        if (monthlyStockPrice != null && monthlyStockTechnicals != null) {

            LocalDate sessionDate = stockPrice.getSessionDate();
            LocalDate now = LocalDate.now();

            LocalDate firstOfMonth =
                    calendarService.nextTradingDate(miscUtil.previousMonthLastDay());

            LocalDate fifteenthOfMonth = firstOfMonth.plusDays(7);

            if (sessionDate != null
                    && (!sessionDate.isBefore(firstOfMonth)
                            && !sessionDate.isAfter(fifteenthOfMonth))) {

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

            if (subStrategyRef.isEmpty()) {
                StockPrice weeklyStockPrice = stockPriceService.get(stock, Timeframe.WEEKLY);
                StockTechnicals weeklyStockTechnicals =
                        stockTechnicalsService.get(stock, Timeframe.WEEKLY);

                LocalDate firstDayOfWeek =
                        calendarService.nextTradingDate(miscUtil.previousWeekLastDay());
                LocalDate secondDayOfWeek = firstDayOfWeek.plusDays(1);

                if (sessionDate != null
                        && (!sessionDate.isBefore(firstDayOfWeek)
                                && !sessionDate.isAfter(secondDayOfWeek))) {
                    if (signalEvaluatorHelperService.isHigherTimeframeConfirmed(
                            weeklyStockTechnicals, false)) {
                        subStrategyRef =
                                confirmBreakout(
                                        timeframe,
                                        Timeframe.WEEKLY,
                                        stock,
                                        stockPrice,
                                        stockTechnicals,
                                        weeklyStockPrice,
                                        weeklyStockTechnicals);
                    }
                }
            }
        }

        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.SIMPLE)
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

        if (timeframe != Timeframe.DAILY) {
            return Optional.empty();
        }

        boolean isBullishConfirmed =
                candleStickConfirmationService.isBullishConfirmed(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, false);

        boolean checkHigherTimeFrameResistance =
                !isBullishConfirmed
                        || (!CandleStickUtils.isProGapUp(stockPrice)
                                && !adxIndicatorService.isBullishIncr(stockTechnicals));

        boolean isHigherTimeframeResistanceCheckPassed =
                (checkHigherTimeFrameResistance
                        ? resistanceValidationService.isOutsideResistanceZone(stockPrice)
                        : true);

        // Monthly Align Bullish
        if (MovingAverageUtil.isAllMaAlignedBullish(
                higherTimeframeStockTechnicals.getTimeframe(), higherTimeframeStockTechnicals)) {

            // Monthly closed above ema5
            if (higherTimeframeStockPrice.getClose()
                    > MovingAverageUtil.getMovingAverage5(
                            higherTimeframeStockTechnicals.getTimeframe(),
                            higherTimeframeStockTechnicals)) {
                boolean isLowRejected =
                        higherTimeframeStockPrice.getLow()
                                < higherTimeframeStockTechnicals.getEma5();

                // Monthly REd and prev Green
                if (CandleStickUtils.isRed(higherTimeframeStockPrice)
                        && (CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                                || CandleStickUtils.isPrev2SessionGreen(higherTimeframeStockPrice)
                                || isLowRejected)) {

                    if (!(CandleStickUtils.isRed(higherTimeframeStockPrice)
                            && CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                            && higherTimeframeStockPrice.getOpen()
                                    > higherTimeframeStockPrice.getPrevClose())) {

                        // monthly HL or low rejected
                        if (CandleStickUtils.isHigherLow(higherTimeframeStockPrice)
                                || isLowRejected) {

                            if (!CandleStickUtils.isUpperWickDominant(higherTimeframeStockPrice)
                                    && !CandleStickUtils.isStrongUpperWick(
                                            higherTimeframeStockPrice)) {

                                StockPrice prevSessionStockPrice =
                                        stockPriceService.buildPrevSessionStockPrice(
                                                higherTimeframeStockPrice);

                                if (!CandleStickUtils.isUpperWickDominant(prevSessionStockPrice)) {
                                    if (MovingAverageUtil.isAllMAsIncreasing(
                                            higherTimeframeStockTechnicals)) {

                                        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
                                        OHLCV ohlcv =
                                                monthlySupportResistanceService
                                                        .supportAndResistance(
                                                                stock.getNseSymbol(),
                                                                firstOfMonth,
                                                                LocalDate.now());

                                        if (higherTimeframe == Timeframe.WEEKLY) {
                                            LocalDate firstDayOfWeek =
                                                    LocalDate.now().with(DayOfWeek.MONDAY);
                                            ohlcv =
                                                    weeklySupportResistanceService
                                                            .supportAndResistance(
                                                                    stock.getNseSymbol(),
                                                                    firstDayOfWeek,
                                                                    LocalDate.now());
                                        }

                                        // Monthly close >= open or Monthly close >= low
                                        if (higherTimeframeStockPrice.getClose() >= ohlcv.getOpen()
                                                || higherTimeframeStockPrice.getClose()
                                                        >= ohlcv.getLow()) {

                                            // Is Daily MA align Bullish
                                            if (MovingAverageUtil.isAllMaAlignedBullish(
                                                    stockTechnicals.getTimeframe(),
                                                    stockTechnicals)) {
                                                // Daily close > monthly close
                                                if (stockPrice.getClose()
                                                        > higherTimeframeStockPrice.getClose()) {

                                                    // Prev close <= monthly close
                                                    if (stockPrice.getPrevClose()
                                                            <= higherTimeframeStockPrice
                                                                    .getClose()) {
                                                        if (CandleStickUtils.isGreen(stockPrice)
                                                                && isHigherTimeframeResistanceCheckPassed) {
                                                            System.out.println(
                                                                    stock.getNseSymbol()
                                                                            + " Found with stop"
                                                                            + " loss "
                                                                            + stockPrice.getLow());
                                                            System.out.print(
                                                                    higherTimeframe
                                                                            + " open "
                                                                            + ohlcv.getOpen()
                                                                            + " low "
                                                                            + ohlcv.getLow());
                                                            if (higherTimeframe
                                                                    == Timeframe.WEEKLY) {
                                                                return SubStrategyHelper
                                                                        .resolveByName(
                                                                                "weekly_breakout");
                                                            }

                                                            return SubStrategyHelper.resolveByName(
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

        return Optional.empty();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakdown(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
        log.debug(
                "Confirming breakdown for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (rsiIndicatorService.isOverSold(stockTechnicals)
                || (CandleStickUtils.isLowerWickDominant(stockPrice)
                                && (CandleStickUtils.isStrongRange(
                                        timeframe, stockPrice, stockTechnicals))
                        || CandleStickUtils.lowerWickSize(stockPrice)
                                >= 2 * CandleStickUtils.bodySize(stockPrice))) {
            return Optional.empty();
        }

        if (!CandleStickUtils.isLowerLow(stockPrice)) {
            return Optional.empty();
        }

        boolean isLongerMaAlignedBearish =
                MovingAverageUtil.isLongerMaAlignedBearish(
                        evaluationResult.getLength(), timeframe, stockTechnicals);

        if (!isLongerMaAlignedBearish) {
            return Optional.empty();
        }

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighestAndLowestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKDOWN, false);

        boolean isNearestMovingAverageDiffValidForBreakdown =
                signalEvaluatorHelperService.isNearestMovingAverageDiffValidForBreakdown(
                        timeframe, stockTechnicals, evaluationResult, false);
        boolean isAllMAsDecreasing = MovingAverageUtil.isAllMAsDecreasing(stockTechnicals);
        boolean isMaAlignBearish =
                MovingAverageUtil.isAllMaAlignedBearish(timeframe, stockTechnicals);

        if (isLowestAndHighestMovingAverageDiffValid
                && isNearestMovingAverageDiffValidForBreakdown) {
            boolean isCurrentBreakdownConfirmation =
                    signalEvaluatorHelperService.currentBreakdownConfirmation(
                            stockPrice, stockTechnicals);

            if (isCurrentBreakdownConfirmation) {
                return SubStrategyHelper.resolveByName(
                        "ma" + evaluationResult.getLength().getMaDays() + "_breakdown");
            }
        }

        return Optional.empty();
    }
}
