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
@Service("simplePriceActionSignalEvaluator")
public class SimplePriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final MonthlySupportResistanceService monthlySupportResistanceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final RsiIndicatorService rsiIndicatorService;
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

            LocalDate firstOfMonth =
                    calendarService.nextTradingDate(miscUtil.previousMonthLastDay());

            LocalDate fifteenthOfMonth = firstOfMonth.plusDays(3);

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

        // Monthly Align Bullish
        if (MovingAverageUtil.isAllMaAlignedBullish(
                higherTimeframeStockTechnicals.getTimeframe(), higherTimeframeStockTechnicals)) {

            boolean isLowRejected =
                    higherTimeframeStockPrice.getLow() < higherTimeframeStockTechnicals.getEma5();

            Optional<MAEvaluationResult> evaluationResultOptional =
                    dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                            timeframe, stockPrice, stockTechnicals, false);

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
                if (CandleStickUtils.isRed(higherTimeframeStockPrice)
                        && (CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                                || CandleStickUtils.isPrev2SessionGreen(higherTimeframeStockPrice)
                                || CandleStickUtils.isPrev3SessionGreen(higherTimeframeStockPrice)
                                || isNearSupport)) {

                    if (!(CandleStickUtils.isRed(higherTimeframeStockPrice)
                            && CandleStickUtils.isPrevSessionGreen(higherTimeframeStockPrice)
                            && higherTimeframeStockPrice.getOpen()
                                    > higherTimeframeStockPrice.getPrevClose())) {

                        // monthly HL or low rejected
                        if (CandleStickUtils.isHigherLow(higherTimeframeStockPrice)
                                || isLowRejected) {
                            //  System.out.println("STEP5: " +stock.getNseSymbol());
                            /*
                            if (!CandleStickUtils.isUpperWickDominant(higherTimeframeStockPrice)
                                    && !CandleStickUtils.isStrongUpperWick(
                                            higherTimeframeStockPrice)) {*/
                            if (!CandleStickUtils.isUpperWickDominant(higherTimeframeStockPrice)) {
                                StockPrice prevSessionStockPrice =
                                        stockPriceService.buildPrevSessionStockPrice(
                                                higherTimeframeStockPrice);
                                if (higherTimeframeStockTechnicals.getEma5()
                                        > higherTimeframeStockTechnicals.getEma20()) {
                                    if (!CandleStickUtils.isUpperWickDominant(
                                            prevSessionStockPrice)) {
                                        if (MovingAverageUtil.increasingMaCount(
                                                        higherTimeframeStockTechnicals)
                                                >= 2) {

                                            LocalDate firstOfMonth =
                                                    LocalDate.now().withDayOfMonth(1);
                                            OHLCV ohlcv =
                                                    monthlySupportResistanceService
                                                            .supportAndResistance(
                                                                    stock.getNseSymbol(),
                                                                    firstOfMonth,
                                                                    LocalDate.now());

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

                                                                        /*
                                                                        boolean isStrongBody =
                                                                                CandleStickUtils.isStrongBody(
                                                                                        stockPrice
                                                                                                .getTimeframe(),
                                                                                        stockPrice,
                                                                                        stockTechnicals);

                                                                        boolean isStrongRange =
                                                                                CandleStickUtils.isStrongRange(
                                                                                        stockPrice
                                                                                                .getTimeframe(),
                                                                                        stockPrice,
                                                                                        stockTechnicals);
                                                                         */
                                                                        // if (isStrongBody ||
                                                                        // isStrongRange) {

                                                                        System.out.println(
                                                                                stock.getNseSymbol()
                                                                                        + " Found"
                                                                                        + " with"
                                                                                        + " stop"
                                                                                        + " loss "
                                                                                        + stockPrice
                                                                                                .getLow());
                                                                        System.out.print(
                                                                                higherTimeframe
                                                                                        + " open "
                                                                                        + ohlcv
                                                                                                .getOpen()
                                                                                        + " low "
                                                                                        + ohlcv
                                                                                                .getLow());

                                                                        return SubStrategyHelper
                                                                                .resolveByName(
                                                                                        "monthly_breakout");
                                                                        // }
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
