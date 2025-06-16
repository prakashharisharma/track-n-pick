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
import com.example.util.FormulaService;
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
    private final CandleStickConfirmationService candleStickConfirmationService;

    private final VolumeIndicatorService volumeIndicatorService;
    private final MacdIndicatorService macdIndicatorService;

    private final RsiIndicatorService rsiIndicatorService;
    private final FormulaService formulaService;

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
                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);

                researchPrice =
                        signalEvaluatorHelperService.calculateEntryPrice(
                                timeframe,
                                stockPrice,
                                stockTechnicals,
                                evaluationResult.getValue());

            } else if (evaluationResult.isNearSupport()) {
                subStrategyRef =
                        confirmSupportBounce(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
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
                        timeframe, stockPrice, stockTechnicals, true);

        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakdown()) {
                subStrategyRef =
                        confirmBreakdown(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
            } else if (evaluationResult.isNearResistance()) {
                subStrategyRef =
                        confirmResistanceRejection(
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

        if (evaluationResult.getLength() == MovingAverageLength.HIGHEST
                || rsiIndicatorService.isOverBought(stockTechnicals)
                || CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isHigherMovingAverageDiffValid =
                signalEvaluatorHelperService.isHigherMovingAverageDiffValid(
                        timeframe, stockTechnicals, evaluationResult);

        boolean isLowestAndHighestMovingAverageDiffInNarrowRange =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffInNarrowRange(
                        timeframe, stockTechnicals);

        if ((isHigherMovingAverageDiffValid
                        && MovingAverageUtil.isAtLeastTwoMovingAverageIncreasing(
                                evaluationResult.getLength(), stockTechnicals))
                || (isLowestAndHighestMovingAverageDiffInNarrowRange
                        && MovingAverageUtil.isAllMAsIncreasing(stockTechnicals))) {

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

    private String getMANameByLength(MovingAverageLength movingAverageLength) {

        if (movingAverageLength == MovingAverageLength.LOWEST) {
            return "ma200";
        } else if (movingAverageLength == MovingAverageLength.LOW) {
            return "ma100";
        } else if (movingAverageLength == MovingAverageLength.MEDIUM) {
            return "ma50";
        } else if (movingAverageLength == MovingAverageLength.HIGH) {
            return "MA20";
        }

        return "MA5";
    }

    private Optional<ResearchTechnical.SubStrategy> confirmSupportBounce(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
        log.debug(
                "Confirming support bounce for stock={} timeframe={}",
                stock.getNseSymbol(),
                timeframe);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), highestMovingAverageResult.getPrevValue());

        boolean isValid =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.SUPPORT, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        if (isValid) {
            boolean isUpperWickSizeConfirmed =
                    candleStickConfirmationService.isUpperWickSizeConfirmed(
                            timeframe, stockPrice, stockTechnicals);

            boolean isBullishCandleStick =
                    candleStickConfirmationService.isBullishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            boolean isVolumeSurge =
                    volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

            // TODO: prevBullishCandleStick

            if (isUpperWickSizeConfirmed && isBullishCandleStick && isVolumeSurge) {
                // return Optional.of(ResearchTechnical.SubStrategy.SUPPORT);
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_support");
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

        if (evaluationResult.getLength() == MovingAverageLength.LOWEST
                || rsiIndicatorService.isOverSold(stockTechnicals)
                || CandleStickUtils.isLowerWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isLowestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestMovingAverageDiffValid(
                        timeframe, stockTechnicals, evaluationResult);

        boolean isLowestAndHighestMovingAverageDiffInWideRange =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffInWideRange(
                        timeframe, stockTechnicals);

        if ((isLowestMovingAverageDiffValid
                        && MovingAverageUtil.isAtLeastTwoMovingAverageDecreasing(
                                evaluationResult.getLength(), stockTechnicals))
                || (isLowestAndHighestMovingAverageDiffInWideRange
                        && MovingAverageUtil.isAllMAsDecreasing(stockTechnicals))) {

            boolean isCurrentBreakdownConfirmation =
                    signalEvaluatorHelperService.currentBreakdownConfirmation(
                            stockPrice, stockTechnicals);

            if (isCurrentBreakdownConfirmation) {
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_breakout");
            }
        }

        return Optional.empty();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmResistanceRejection(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
        log.debug(
                "Confirming resistance rejection for stock={} timeframe={}",
                stock.getNseSymbol(),
                timeframe);

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(), evaluationResult.getPrevValue());

        boolean isValid =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.RESISTANCE, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        if (isValid) {
            boolean isLowerWickSizeConfirmed =
                    candleStickConfirmationService.isLowerWickSizeConfirmed(
                            timeframe, stockPrice, stockTechnicals);

            boolean isBearishCandleStick =
                    candleStickConfirmationService.isBearishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            boolean isVolumeSurge =
                    volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

            // TODO: prevBearishCandleStick
            if (isLowerWickSizeConfirmed && isBearishCandleStick && isVolumeSurge) {
                // return Optional.of(ResearchTechnical.SubStrategy.RESISTANCE);
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_resistance");
            }
        }

        return Optional.empty();
    }
}
