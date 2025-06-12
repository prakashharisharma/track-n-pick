package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
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
                        this.calculateEntryPriceForBreakout(
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

    public double calculateEntryPriceForBreakout(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double breakoutValue) {

        boolean isUpperWickClean =
                candleStickConfirmationService.isUpperWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);
        boolean isHistogramAboveZero = macdIndicatorService.isHistogramAboveZero(stockTechnicals);

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();

        double entryPrice = (open + high + low + close) / 4.0;

        entryPrice = entryPrice * 1.00382;

        // 1. Clean upper wick and RSI above 60
        if (isUpperWickClean && Math.ceil(stockTechnicals.getRsi()) >= 60.0) {
            entryPrice = high;
        }

        // 2. Clean upper wick and Body above breakout level
        else if (open > breakoutValue && close > breakoutValue && isUpperWickClean) {
            entryPrice = (high + close) / 2.0;
        }

        // 3. Body above breakout level
        else if (open > breakoutValue && close > breakoutValue) {
            entryPrice = (open + close) / 2.0;
            entryPrice = entryPrice * 1.00382;
        }

        // 4. Clean upper wick and bullish momentum
        else if (isUpperWickClean && isHistogramAboveZero) {
            entryPrice = (high + close) / 2.0;
        }

        // 5. Default to average price
        return formulaService.ceilToNearestHalf(entryPrice);
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
        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double lowestAndHighestPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(), highestMovingAverageResult.getPrevValue());

        if (evaluationResult.getLength() == MovingAverageLength.HIGHEST
                || rsiIndicatorService.isOverBought(stockTechnicals)
                || CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

        MovingAverageResult higherMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        evaluationResult.getLength().getHigher(), timeframe, stockTechnicals, true);

        MovingAverageResult lowerMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        evaluationResult.getLength().getHigher(), timeframe, stockTechnicals, true);


        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), higherMovingAverageResult.getPrevValue());

        double lowerMaPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowerMovingAverageResult.getPrevValue(), evaluationResult.getPrevValue());

        boolean isHigherMADiffValid = maPercentageDiff >= 2.0 || (lowerMaPercentageDiff >= 2.0 && maPercentageDiff <= 1.0);

        if (!isHigherMADiffValid && stockPrice.getClose() > higherMovingAverageResult.getValue()) {

            if (evaluationResult.getLength().getWeight() > MovingAverageLength.HIGH.getWeight()) {
                MovingAverageResult nextHigherMovingAverageResult =
                        MovingAverageUtil.getMovingAverage(
                                evaluationResult.getLength().getHigher().getHigher(),
                                timeframe,
                                stockTechnicals,
                                true);
                maPercentageDiff =
                        formulaService.calculateChangePercentage(
                                evaluationResult.getPrevValue(),
                                nextHigherMovingAverageResult.getPrevValue());

                isHigherMADiffValid = maPercentageDiff >= 2.0 || (lowerMaPercentageDiff >= 2.0 && maPercentageDiff <= 1.0);
            }
        }

        if (
                (isHigherMADiffValid
                && MovingAverageUtil.isAtLeastTwoMovingAverageIncreasing(
                        evaluationResult.getLength(), stockTechnicals))
        ||
           ( lowestAndHighestPercentageDiff < 2.0
                   && MovingAverageUtil.isAllMAsIncreasing(stockTechnicals))
        ) {
            boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
            boolean isStrongBody =
                    CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
            boolean isStrongLowerWick =
                    CandleStickUtils.isStrongLowerWick(stockPrice)
                            || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

            boolean isMacdConfirmingBreakout =
                    this.isMacdConfirmingBreakout(stockTechnicals)
                            || this.isMacdTurningUp(stockTechnicals);

            boolean isVolumeAboveAvg = volumeIndicatorService.isVolumeAverage(stockTechnicals);

            boolean isBullishConfirmed =
                    candleStickConfirmationService.isBullishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            boolean isCandleStickBullish =
                    isBullishConfirmed || isGapUp || isStrongBody || isStrongLowerWick;

            if (isCandleStickBullish
                    && isMacdConfirmingBreakout
                    && rsiIndicatorService.isBullish(stockTechnicals)
                    && (isVolumeAboveAvg || (stockTechnicals.getPrevRsi() < 30.0))) {

                // return Optional.of(ResearchTechnical.SubStrategy.BREAKOUT);
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

    private boolean isMacdConfirmingBreakout(StockTechnicals st) {

        if (st == null) {
            return false;
        }

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

    private boolean isMacdTurningUp(StockTechnicals stockTechnicals) {

        return macdIndicatorService.isMacdIncreased(stockTechnicals)
                && macdIndicatorService.isSignalIncreased(stockTechnicals)
                && macdIndicatorService.isHistogramIncreased(stockTechnicals)
                && macdIndicatorService.isMacdBelowZero(stockTechnicals);
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
                // return Optional.of(ResearchTechnical.SubStrategy.BREAKDOWN);
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_breakdown");
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
