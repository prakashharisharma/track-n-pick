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
@Service("dynamicPriceActionSignalEvaluator")
public class DynamicPriceActionSignalEvaluator implements TradeSignalEvaluator {

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
                        timeframe, stockPrice, stockTechnicals);
        double researchPrice = 0.0;
        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakout()) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);

                researchPrice =  this.calculateEntryPriceForBreakout(timeframe, stockPrice, stockTechnicals, evaluationResult.getValue());

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

        boolean isUpperWickClean = candleStickConfirmationService.isUpperWickSizeConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isHistogramAboveZero = macdIndicatorService.isHistogramAboveZero(stockTechnicals);

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();

        // 1. Clean upper wick and Body above breakout level
        if (open > breakoutValue && close > breakoutValue && isUpperWickClean) {
            return (high + close) / 2.0;
        }

        // 2. Body above breakout level
        else if (open > breakoutValue && close > breakoutValue) {
            return (open + close) / 2.0;
        }

        // 2. Clean upper wick and bullish momentum
        else if (isUpperWickClean && isHistogramAboveZero) {
            return (high + close) / 2.0;
        }

        // 3. Default to average price
        return (open + high + low + close) / 4.0;
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

        if(evaluationResult.getLength() == MovingAverageLength.HIGHEST || rsiIndicatorService.isOverBought(stockTechnicals) || CandleStickUtils.isUpperWickDominant(stockPrice)){
            return Optional.empty();
        }

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), highestMovingAverageResult.getPrevValue());

        if(evaluationResult.getLength() == MovingAverageLength.LOWEST) {

            MovingAverageResult nextHigherMovingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            evaluationResult.getLength().getHigher(), timeframe, stockTechnicals, true);

            double nextHigherPercentDiff = formulaService.calculateChangePercentage(
                    evaluationResult.getPrevValue(), nextHigherMovingAverageResult.getPrevValue());

            if(nextHigherPercentDiff < 3.0){
                return Optional.empty();
            }
        }else{
            MovingAverageResult nextLowerMovingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            evaluationResult.getLength().getLower(), timeframe, stockTechnicals, true);

            double nextLowrPercentDiff = formulaService.calculateChangePercentage(
                    nextLowerMovingAverageResult.getPrevValue(), evaluationResult.getPrevValue());

            if(nextLowrPercentDiff < 2.0){
                return Optional.empty();
            }
        }

        boolean isValid =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.BREAKOUT, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        // We will not consider breakout for HIGHEST MA for DAILY
        if (isValid
                && (timeframe == Timeframe.DAILY
                        && evaluationResult.getLength() != MovingAverageLength.HIGHEST)) {
            boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
            boolean isStrongBody =
                    CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
            boolean isStrongLowerWick =
                    CandleStickUtils.isStrongLowerWick(stockPrice)
                            || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

            boolean isMacdConfirmingBreakout = this.isMacdConfirmingBreakout(stockTechnicals) || this.isMacdTurningUp(stockTechnicals);

            boolean isVolumeAboveAvg = volumeIndicatorService.isVolumeAverage(stockTechnicals);

            boolean isBullishConfirmed = candleStickConfirmationService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals, true);

            boolean isCandleStickBullish = isBullishConfirmed || isGapUp || isStrongBody || isStrongLowerWick;

            if (isCandleStickBullish
                    && isMacdConfirmingBreakout && rsiIndicatorService.isBullish(stockTechnicals)
            && (isVolumeAboveAvg || (stockTechnicals.getPrevRsi() < 30.0))
            ) {

                // return Optional.of(ResearchTechnical.SubStrategy.BREAKOUT);
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_breakout");
            }
        }

        return Optional.empty();
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


            /*
            return  macdIndicatorService.isSignalDecreased(st)
                    && macdIndicatorService.isHistogramIncreased(st);

             */
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(st);
    }

    private boolean isMacdTurningUp(StockTechnicals stockTechnicals){
        return stockTechnicals.getMacd() > stockTechnicals.getPrevMacd() && stockTechnicals.getSignal() > stockTechnicals.getPrevSignal();
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
