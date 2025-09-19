package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
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
@Service("megaPriceActionSignalEvaluator")
public class MegaPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final MonthlySupportResistanceService monthlySupportResistanceService;

    private final WeeklySupportResistanceService weeklySupportResistanceService;
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final EvaluationLogService evaluationLogService;
    private final RsiIndicatorService rsiIndicatorService;
    private final StockPriceService<StockPrice> stockPriceService;

    private final MiscUtil miscUtil;
    private final CalendarService calendarService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final ResistanceValidationService resistanceValidationService;
    private final AdxIndicatorService adxIndicatorService;
    private final CandleStickConfirmationService candleStickConfirmationService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        double researchPrice = 0.0;
        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        if (MovingAverageUtil.isAllMaAlignedBullish(timeframe, stockTechnicals)
                && MovingAverageUtil.isAllMAsIncreasing(stockTechnicals)) {

            LocalDate sessionDate = stockPrice.getSessionDate();

            LocalDate firstOfMonth =
                    calendarService.nextTradingDate(miscUtil.previousMonthLastDay());
            LocalDate firstDayOfWeek =
                    calendarService.nextTradingDate(miscUtil.previousWeekLastDay());

            if (sessionDate != null
                    && (sessionDate.isEqual(firstOfMonth) || sessionDate.isEqual(firstDayOfWeek))) {
                subStrategyRef = confirmBreakout(timeframe, stock, stockPrice, stockTechnicals);
            }
        }

        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.INVESTMENT)
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
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (timeframe == Timeframe.DAILY) {
            return Optional.empty();
        }

        if (timeframe != Timeframe.MONTHLY) {
            if (!signalEvaluatorHelperService.isHigherTimeframeConfirmed(stockTechnicals, false)) {
                return Optional.empty();
            }
        }

        StockTechnicals lowerStockTechnicals =
                stockTechnicalsService.get(stock, timeframe.getLower());
        // lower Align Bullish
        if (MovingAverageUtil.isAllMaAlignedBullish(
                lowerStockTechnicals.getTimeframe(), lowerStockTechnicals)) {

            Optional<MAEvaluationResult> evaluationResultOptional =
                    dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                            timeframe, stockPrice, stockTechnicals, false);

            double ema5 = MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals);

            // close above ema5
            if (stockPrice.getClose() > ema5) {

                boolean isBullishConfirmed =
                        candleStickConfirmationService.isBullishConfirmed(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals, false);

                boolean checkHigherTimeFrameResistance =
                        (!isBullishConfirmed)
                                || (!CandleStickUtils.isProGapUp(stockPrice)
                                        && !adxIndicatorService.isBullishIncr(stockTechnicals));

                boolean isHigherTimeframeResistanceCheckPassed =
                        (checkHigherTimeFrameResistance
                                ? resistanceValidationService.isOutsideResistanceZone(stockPrice)
                                : true);

                boolean isBreakout =
                        evaluationResultOptional.isPresent()
                                && evaluationResultOptional.get().isBreakout();

                boolean isRejectedAtLow =
                        CandleStickUtils.isRed(stockPrice) && stockPrice.getLow() < ema5;

                boolean isLowerHighLowerLow =
                        CandleStickUtils.isLowerHigh(stockPrice)
                                && CandleStickUtils.isLowerLow(stockPrice);

                if (isBreakout || (isRejectedAtLow && !isLowerHighLowerLow)) {

                    if (!CandleStickUtils.isUpperWickDominant(stockPrice)
                            && !CandleStickUtils.isStrongUpperWick(stockPrice)) {

                        // volume avg increasing
                        if (stockTechnicals.getVolumeAvg20()
                                > stockTechnicals.getPrevVolumeAvg20()) {
                            // volume above avg
                            if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20()) {
                                System.out.println(
                                        timeframe + " INV Found " + stock.getNseSymbol());
                                if (isHigherTimeframeResistanceCheckPassed) {
                                    return SubStrategyHelper.resolveByName(
                                            timeframe.name() + "_breakout");
                                }
                            }
                        }
                        // }
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
