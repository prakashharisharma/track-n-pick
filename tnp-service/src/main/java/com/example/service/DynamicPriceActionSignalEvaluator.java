package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SignalEvaluatorHelperService;
import com.example.service.utils.SubStrategyHelper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("dynamicPriceActionSignalEvaluator")
public class DynamicPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final ResistanceValidationService resistanceValidationService;
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final EvaluationLogService evaluationLogService;
    private final RsiIndicatorService rsiIndicatorService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private final AdxIndicatorService adxIndicatorService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, true);
        double researchPrice = 0.0;
        if (evaluationResultOptional.isPresent()) {

            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakout()) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.POSITIVE,
                        timeframe.name()
                                + "-"
                                + ResearchTechnical.Strategy.DYNAMIC.name()
                                + " breakout found"
                                + " on "
                                + evaluationResult.getLength());

                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);

                researchPrice =
                        signalEvaluatorHelperService.calculateEntryPrice(
                                timeframe,
                                stockPrice,
                                stockTechnicals,
                                evaluationResult.getValue());
            }

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.DYNAMIC)
                        .subStrategy(subStrategyRef.get())
                        .researchPrice(researchPrice)
                        .build();
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, true);

        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakdown()) {

                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEGATIVE,
                        timeframe.name()
                                + "-"
                                + ResearchTechnical.Strategy.DYNAMIC.name()
                                + " breakdown found"
                                + " on "
                                + evaluationResult.getLength());

                subStrategyRef =
                        confirmBreakdown(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
            }

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.DYNAMIC)
                        .subStrategy(subStrategyRef.get())
                        .build();
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (MovingAverageUtil.getMovingAverage200(Timeframe.DAILY, stockTechnicals) == 0.0) {
            return Optional.empty();
        }

        boolean isHighestAndHighMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        if (!isHighestAndHighMovingAverageDiffValid) {
            return Optional.empty();
        }

        /*
        if (CandleStickUtils.isUpperWickLongerThanLowerWick(stockPrice)) {
            return Optional.empty();
        }*/

        /*
        if(CandleStickUtils.upperWickSize(stockPrice) >= 2 * CandleStickUtils.lowerWickSize(stockPrice)
                && CandleStickUtils.prevUpperWickSize(stockPrice) >= 2 * CandleStickUtils.prevLowerWickSize(stockPrice)

        ){
            return Optional.empty();
        }*/

        if (rsiIndicatorService.isOverBought(stockTechnicals)
                || (CandleStickUtils.isUpperWickDominant(stockPrice)
                        && (CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals)
                                || (CandleStickUtils.upperWickSize(stockPrice)
                                        >= 2 * CandleStickUtils.bodySize(stockPrice))))) {
            return Optional.empty();
        }

        if (timeframe.getPriority() > 1) {
            StockPrice dailyStockPrice = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals dailyStockTechnicals =
                    stockTechnicalsService.get(stock, Timeframe.DAILY);
            if (rsiIndicatorService.isOverBought(dailyStockTechnicals)
                    || (CandleStickUtils.isUpperWickDominant(dailyStockPrice)
                            && (CandleStickUtils.isStrongRange(
                                            Timeframe.DAILY, dailyStockPrice, dailyStockTechnicals)
                                    || (CandleStickUtils.upperWickSize(dailyStockPrice)
                                            >= 2 * CandleStickUtils.bodySize(dailyStockPrice))))) {
                return Optional.empty();
            }
        }

        if (!CandleStickUtils.isHigherHigh(stockPrice)) {
            return Optional.empty();
        }

        boolean isRangeHigherThanPrevSessionRange =
                CandleStickUtils.prevSessionRange(stockPrice) < CandleStickUtils.range(stockPrice);
        boolean isBodyHigherThanPrevSessionBody =
                CandleStickUtils.prevSessionBodySize(stockPrice)
                        < CandleStickUtils.bodySize(stockPrice);

        boolean isRangeOrBodyConfirmed =
                isRangeHigherThanPrevSessionRange || isBodyHigherThanPrevSessionBody;

        if (!isRangeOrBodyConfirmed) {
            return Optional.empty();
        }

        if (evaluationResult.getLength() == MovingAverageLength.HIGHEST) {
            if (!MovingAverageUtil.validatedMa200WrtMa100(stockTechnicals)) {
                return Optional.empty();
            }
        }

        int incrMACount = MovingAverageUtil.increasingMaCount(stockTechnicals);

        if (incrMACount == 5) {
            if (rsiIndicatorService.rsi(stockTechnicals) > 60) {
                return Optional.empty();
            }
        }

        if (signalEvaluatorHelperService.isHighestAlsoBreached(
                timeframe,
                stockPrice,
                stockTechnicals,
                evaluationResult.getLength(),
                evaluationResult.getValue())) {
            if (!adxIndicatorService.isBullishIncr(stockTechnicals)
                    || !resistanceValidationService.isOutsideResistanceZone(stockPrice)) {
                return Optional.empty();
            }
        }

        if (evaluationResult.getLength() == MovingAverageLength.HIGHEST
                && evaluationResult.getLength().getMaDays() == 5) {
            // boolean isAllMAsIncreasing = MovingAverageUtil.isAllMAsIncreasing(stockTechnicals);
            if (!adxIndicatorService.isBullishIncr(stockTechnicals)) {
                return Optional.empty();
            }
        }

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        boolean isNearestMovingAverageDiffValidForBreakout =
                signalEvaluatorHelperService.isNearestMovingAverageDiffValidForBreakout(
                        timeframe, stockTechnicals, evaluationResult, true);

        if (isLowestAndHighestMovingAverageDiffValid
                && isNearestMovingAverageDiffValidForBreakout) {

            boolean isCurrentBreakoutConfirmation =
                    signalEvaluatorHelperService.currentBreakoutConfirmation(
                            stockPrice, stockTechnicals);

            if (isCurrentBreakoutConfirmation) {

                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_breakout");
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

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighestAndLowestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKDOWN, true);

        boolean isNearestMovingAverageDiffValidForBreakdown =
                signalEvaluatorHelperService.isNearestMovingAverageDiffValidForBreakdown(
                        timeframe, stockTechnicals, evaluationResult, true);
        boolean isAllMAsDecreasing = MovingAverageUtil.isAllMAsDecreasing(stockTechnicals);
        if (isLowestAndHighestMovingAverageDiffValid
                && isNearestMovingAverageDiffValidForBreakdown) {

            boolean isCurrentBreakdownConfirmation =
                    signalEvaluatorHelperService.currentBreakdownConfirmation(
                            stockPrice, stockTechnicals);

            if (isCurrentBreakdownConfirmation) {
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_breakdown");
            }
        }

        return Optional.empty();
    }
}
