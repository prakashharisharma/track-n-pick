package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SignalEvaluatorHelperService;
import com.example.service.utils.SubStrategyHelper;
import com.example.util.StringUtils;
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
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final EvaluationLogService evaluationLogService;
    private final RsiIndicatorService rsiIndicatorService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final ResistanceValidationService resistanceValidationService;
    private final AdxIndicatorService adxIndicatorService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, false);
        double researchPrice = 0.0;
        if (evaluationResultOptional.isPresent()) {
            log.info("Simple breakout found1");
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakout()) {
                log.info("Simple breakout found");
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.POSITIVE,
                        timeframe.name()
                                + "-"
                                + ResearchTechnical.Strategy.SIMPLE.name()
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
                        .strategy(ResearchTechnical.Strategy.SIMPLE)
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
                        timeframe, stockPrice, stockTechnicals, false);

        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakdown()) {

                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEGATIVE,
                        timeframe.name()
                                + "-"
                                + ResearchTechnical.Strategy.SIMPLE.name()
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
                        .strategy(ResearchTechnical.Strategy.SIMPLE)
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

        // (timeframe == Timeframe.DAILY && isMa5Highest)
        if (rsiIndicatorService.isOverBought(stockTechnicals)
                || (CandleStickUtils.isUpperWickDominant(stockPrice)
                                && CandleStickUtils.isStrongRange(
                                        timeframe, stockPrice, stockTechnicals)
                        || (CandleStickUtils.upperWickSize(stockPrice)
                                >= CandleStickUtils.bodySize(stockPrice) * 2))) {
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

        boolean isLongerMaAlignedBullish =
                MovingAverageUtil.isLongerMaAlignedBullish(
                        evaluationResult.getLength(), timeframe, stockTechnicals);

        if (!isLongerMaAlignedBullish) {
            return Optional.empty();
        }
        boolean isAllMAsIncreasing = MovingAverageUtil.isAllMAsIncreasing(stockTechnicals);

        if (evaluationResult.getLength().getMaDays() == 5) {
            if (!adxIndicatorService.isBullishIncr(stockTechnicals)) {
                return Optional.empty();
            }
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

        int incrMACount = MovingAverageUtil.increasingMaCount(stockTechnicals);

        if (incrMACount == 5) {
            return Optional.empty();
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

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, false);

        boolean isNearestMovingAverageDiffValidForBreakout =
                signalEvaluatorHelperService.isNearestMovingAverageDiffValidForBreakout(
                        timeframe, stockTechnicals, evaluationResult, false);

        boolean isMaAlignBullish =
                MovingAverageUtil.isAllMaAlignedBullish(timeframe, stockTechnicals);

        // We will not consider breakout for HIGHEST MA for DAILY
        if (isLowestAndHighestMovingAverageDiffValid
                && isNearestMovingAverageDiffValidForBreakout) {

            evaluationLogService.add(
                    stockTechnicals,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Entry condition passed: (isAllMAsIncreasing:{} && isMaAlignBullish:{})"
                                    + " || (isLowestAndHighestMovingAverageDiffValid:{} &&"
                                    + " isNearestMovingAverageDiffValidForBreakout:{}) → true",
                            isAllMAsIncreasing,
                            isMaAlignBullish,
                            isLowestAndHighestMovingAverageDiffValid,
                            isNearestMovingAverageDiffValidForBreakout));

            boolean isCurrentBreakoutConfirmation =
                    signalEvaluatorHelperService.currentBreakoutConfirmation(
                            stockPrice, stockTechnicals);

            if (isCurrentBreakoutConfirmation) {
                return SubStrategyHelper.resolveByName(
                        "ma" + evaluationResult.getLength().getMaDays() + "_breakout");
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
