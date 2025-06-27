package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.SignalEvaluatorHelperService;
import com.example.service.utils.SubStrategyHelper;
import com.example.util.FormulaService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("dynamicPriceActionSignalEvaluator")
public class DynamicPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final CandleStickConfirmationService candleStickConfirmationService;
    private final VolumeIndicatorService volumeIndicatorService;
    private final RsiIndicatorService rsiIndicatorService;
    private final FormulaService formulaService;

    private final EvaluationLogService evaluationLogService;

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

        if (rsiIndicatorService.isOverBought(stockTechnicals)
                || (CandleStickUtils.isUpperWickDominant(stockPrice)
                        && CandleStickUtils.isStrongRange(
                                timeframe, stockPrice, stockTechnicals))) {
            return Optional.empty();
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
                || CandleStickUtils.isLowerWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKDOWN, true);

        boolean isNearestMovingAverageDiffValidForBreakdown =
                signalEvaluatorHelperService.isNearestMovingAverageDiffValidForBreakdown(
                        timeframe, stockTechnicals, evaluationResult, true);

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
