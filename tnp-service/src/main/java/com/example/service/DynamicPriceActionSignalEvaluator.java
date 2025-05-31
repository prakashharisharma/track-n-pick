package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
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

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final VolumeIndicatorService volumeIndicatorService;
    private final MacdIndicatorService macdIndicatorService;
    private final FormulaService formulaService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals);

        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakout()) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
            } else if (evaluationResult.isNearSupport()) {
                subStrategyRef =
                        confirmSupportBounce(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
            }

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.PRICE)
                        .subStrategy(subStrategyRef.get())
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
                        timeframe, stockPrice, stockTechnicals);

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
                        .strategy(ResearchTechnical.Strategy.PRICE)
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

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), highestMovingAverageResult.getPrevValue());

        boolean isValid =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.BREAKOUT, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        if (isValid) {
            boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
            boolean isStrongBody =
                    CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
            boolean isStrongLowerWick =
                    CandleStickUtils.isStrongLowerWick(stockPrice)
                            || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

            if ((isGapUp || isStrongBody || isStrongLowerWick)
                    && this.isMacdConfirmingBreakout(stockTechnicals)) {
                return Optional.of(ResearchTechnical.SubStrategy.BREAKOUT);
            }
        }

        return Optional.empty();
    }

    private boolean isMacdConfirmingBreakout(StockTechnicals st) {

        double macd = st.getMacd();
        double signal = st.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd < signal && macdIndicatorService.isHistogramBelowZero(st)) {
            return macdIndicatorService.isMacdIncreased(st)
                    && macdIndicatorService.isSignalDecreased(st)
                    && macdIndicatorService.isHistogramIncreased(st);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(st);
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
                return Optional.of(ResearchTechnical.SubStrategy.SUPPORT);
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

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(), evaluationResult.getPrevValue());

        boolean isValid =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.BREAKDOWN, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        if (isValid) {
            boolean isGapDown = CandleStickUtils.isGapUp(stockPrice);
            boolean isStrongBody =
                    CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
            boolean isStrongUpperWick =
                    CandleStickUtils.isStrongUpperWick(stockPrice)
                            || CandleStickUtils.isPrevStrongUpperWick(stockPrice);

            boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPrice);

            if ((isGapDown || isStrongBody || isStrongUpperWick || isPrevRed)) {
                return Optional.of(ResearchTechnical.SubStrategy.BREAKDOWN);
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
                return Optional.of(ResearchTechnical.SubStrategy.RESISTANCE);
            }
        }

        return Optional.empty();
    }
}
