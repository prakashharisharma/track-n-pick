package com.example.service.utils;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.EvaluationLog;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignalEvaluatorHelperService {

    private final FormulaService formulaService;

    private final RsiIndicatorService rsiIndicatorService;

    private final MacdIndicatorService macdIndicatorService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final TimeframeSupportResistanceService timeframeSupportResistanceService;

    private final EvaluationLogService evaluationLogService;

    public boolean isHigherMovingAverageDiffValid(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        MovingAverageLength currentLength = evaluationResult.getLength();
        double nextHighThreshold = 2.5;
        double nextNextHighThreshold = 5.0;

        boolean isHigherMADiffValid = false;

        // Get next higher MA and % difference
        MovingAverageLength nextHighLength = currentLength.getHigher();
        MovingAverageResult nextHighMA =
                MovingAverageUtil.getMovingAverage(
                        nextHighLength, timeframe, stockTechnicals, true);
        double nextHighMaDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), nextHighMA.getPrevValue());
        nextHighMaDiff = formulaService.ceilToNearestQuarter(nextHighMaDiff);

        // Handle HIGHEST case
        if (currentLength == MovingAverageLength.HIGHEST) {
            MovingAverageLength nextLowLength = currentLength.getLower();
            MovingAverageResult nextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextLowLength, timeframe, stockTechnicals, true);

            double nextLowMaDiff =
                    formulaService.calculateChangePercentage(
                            nextLowMA.getPrevValue(), evaluationResult.getPrevValue());
            nextLowMaDiff = formulaService.ceilToNearestQuarter(nextLowMaDiff);

            boolean isLowerMovingAverageIncreasing =
                    MovingAverageUtil.isLowerMovingAverageIncreasing(
                            currentLength, stockTechnicals);

            isHigherMADiffValid =
                    nextLowMaDiff >= nextHighThreshold || isLowerMovingAverageIncreasing;

            evaluationLogService.add(
                    stockTechnicals,
                    isHigherMADiffValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "HIGHEST MA — nextLowMA:{} current:{} diff:{} isLowerIncreasing:{} →"
                                    + " {}",
                            nextLowMA.getPrevValue(),
                            evaluationResult.getPrevValue(),
                            nextLowMaDiff,
                            isLowerMovingAverageIncreasing,
                            isHigherMADiffValid));

        }

        // Handle HIGH case
        else if (currentLength == MovingAverageLength.HIGH) {
            MovingAverageLength nextLowLength = currentLength.getLower();
            MovingAverageResult nextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextLowLength, timeframe, stockTechnicals, true);

            double nextLowMaDiff =
                    formulaService.calculateChangePercentage(
                            nextLowMA.getPrevValue(), evaluationResult.getPrevValue());
            nextLowMaDiff = formulaService.ceilToNearestQuarter(nextLowMaDiff);

            boolean isLowerMovingAverageIncreasing =
                    MovingAverageUtil.isLowerMovingAverageIncreasing(
                            currentLength, stockTechnicals);

            isHigherMADiffValid =
                    nextHighMaDiff >= nextHighThreshold
                            && (nextLowMaDiff >= nextHighThreshold
                                    || isLowerMovingAverageIncreasing);

            evaluationLogService.add(
                    stockTechnicals,
                    isHigherMADiffValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "HIGH MA — nextHighDiff:{} nextLowDiff:{} isLowerIncreasing:{} → {}",
                            nextHighMaDiff,
                            nextLowMaDiff,
                            isLowerMovingAverageIncreasing,
                            isHigherMADiffValid));
        }

        // All other cases
        else {
            if (nextHighMaDiff >= nextHighThreshold) {
                isHigherMADiffValid = true;
                evaluationLogService.add(
                        stockTechnicals,
                        EvaluationLog.Type.POSITIVE,
                        StringUtils.format(
                                "MA nextHighDiff:{} ≥ {} → true",
                                nextHighMaDiff,
                                nextHighThreshold));
            } else {
                // Check next-next higher
                MovingAverageLength nextNextHighLength = nextHighLength.getHigher();
                MovingAverageResult nextNextHighMA =
                        MovingAverageUtil.getMovingAverage(
                                nextNextHighLength, timeframe, stockTechnicals, true);
                double nextNextHighMaDiff =
                        formulaService.calculateChangePercentage(
                                evaluationResult.getPrevValue(), nextNextHighMA.getPrevValue());
                nextNextHighMaDiff = formulaService.ceilToNearestQuarter(nextNextHighMaDiff);

                if (nextNextHighMaDiff >= (nextHighThreshold + nextHighMaDiff)) {

                    isHigherMADiffValid = true;
                    evaluationLogService.add(
                            stockTechnicals,
                            EvaluationLog.Type.POSITIVE,
                            StringUtils.format(
                                    "nextHighDiff:{} <= 0.5 && nextNextHighDiff:{} ≥ {} → true",
                                    nextHighMaDiff,
                                    nextNextHighMaDiff,
                                    nextHighThreshold + nextHighMaDiff));
                } /*else if (nextNextHighMaDiff > nextNextHighThreshold) {
                      isHigherMADiffValid = true;
                      evaluationLogService.add(stockTechnicals, EvaluationLog.Type.POSITIVE,
                              StringUtils.format("nextNextHighDiff:{} > {} → true",
                                      nextNextHighMaDiff, nextNextHighThreshold));
                  } */ else {
                    evaluationLogService.add(
                            stockTechnicals,
                            EvaluationLog.Type.NEUTRAL,
                            StringUtils.format(
                                    "MA nextHighDiff:{} and nextNextHighDiff:{} did not meet"
                                            + " thresholds",
                                    nextHighMaDiff,
                                    nextNextHighMaDiff));
                }
            }
        }

        return isHigherMADiffValid;
    }

    public boolean isLowerMovingAverageDiffValid(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        MovingAverageLength currentLength = evaluationResult.getLength();

        double nextLowThreshold = 2.0;
        double nextNextLowThreshold = 4.0;

        boolean isLowerMADiffValid = false;

        // Get next lower MA and its % difference
        MovingAverageLength nextLowLength = currentLength.getLower();
        MovingAverageResult nextLowMA =
                MovingAverageUtil.getMovingAverage(nextLowLength, timeframe, stockTechnicals, true);

        double nextLowMaDiff =
                formulaService.calculateChangePercentage(
                        nextLowMA.getPrevValue(), evaluationResult.getPrevValue());
        nextLowMaDiff = formulaService.ceilToNearestQuarter(nextLowMaDiff);

        if (currentLength == MovingAverageLength.LOWEST) {
            MovingAverageLength nextHighLength = currentLength.getHigher();
            MovingAverageResult nextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextHighLength, timeframe, stockTechnicals, true);

            double nextHighMaDiff =
                    formulaService.calculateChangePercentage(
                            nextHighMA.getPrevValue(), evaluationResult.getPrevValue());
            nextHighMaDiff = formulaService.ceilToNearestQuarter(nextHighMaDiff);

            boolean isHigherMovingAverageDecreasing =
                    MovingAverageUtil.isHigherMovingAverageDecreasing(
                            currentLength, stockTechnicals);

            isLowerMADiffValid =
                    nextHighMaDiff >= nextLowThreshold || isHigherMovingAverageDecreasing;

            evaluationLogService.add(
                    stockTechnicals,
                    isLowerMADiffValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "LOWEST MA — nextHighMA:{} current:{} diff:{} isHigherDecreasing:{} →"
                                    + " {}",
                            nextHighMA.getPrevValue(),
                            evaluationResult.getPrevValue(),
                            nextHighMaDiff,
                            isHigherMovingAverageDecreasing,
                            isLowerMADiffValid));

        } else if (currentLength == MovingAverageLength.LOW) {
            MovingAverageLength nextHighLength = currentLength.getHigher();
            MovingAverageResult nextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextHighLength, timeframe, stockTechnicals, true);

            double nextHighMaDiff =
                    formulaService.calculateChangePercentage(
                            nextHighMA.getPrevValue(), evaluationResult.getPrevValue());
            nextHighMaDiff = formulaService.ceilToNearestQuarter(nextHighMaDiff);

            boolean isHigherMovingAverageDecreasing =
                    MovingAverageUtil.isHigherMovingAverageDecreasing(
                            currentLength, stockTechnicals);

            isLowerMADiffValid =
                    nextLowMaDiff >= nextLowThreshold
                            && (nextHighMaDiff >= nextLowThreshold
                                    || isHigherMovingAverageDecreasing);

            evaluationLogService.add(
                    stockTechnicals,
                    isLowerMADiffValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "LOW MA — nextLowDiff:{} nextHighDiff:{} isHigherDecreasing:{} → {}",
                            nextLowMaDiff,
                            nextHighMaDiff,
                            isHigherMovingAverageDecreasing,
                            isLowerMADiffValid));

        } else {
            if (nextLowMaDiff >= nextLowThreshold) {
                isLowerMADiffValid = true;
                evaluationLogService.add(
                        stockTechnicals,
                        EvaluationLog.Type.POSITIVE,
                        StringUtils.format(
                                "MA nextLowDiff:{} ≥ {} → true", nextLowMaDiff, nextLowThreshold));
            } else {
                // Check next-next lower MA
                MovingAverageLength nextNextLowLength = nextLowLength.getLower();
                MovingAverageResult nextNextLowMA =
                        MovingAverageUtil.getMovingAverage(
                                nextNextLowLength, timeframe, stockTechnicals, true);

                double nextNextLowMaDiff =
                        formulaService.calculateChangePercentage(
                                evaluationResult.getPrevValue(), nextNextLowMA.getPrevValue());
                nextNextLowMaDiff = formulaService.ceilToNearestQuarter(nextNextLowMaDiff);

                if (nextNextLowMaDiff >= (nextLowThreshold + nextLowMaDiff)) {
                    isLowerMADiffValid = true;
                    evaluationLogService.add(
                            stockTechnicals,
                            EvaluationLog.Type.POSITIVE,
                            StringUtils.format(
                                    "nextLowDiff:{} <= 0.5 && nextNextLowDiff:{} ≥ {} → true",
                                    nextLowMaDiff,
                                    nextNextLowMaDiff,
                                    nextLowThreshold + nextLowMaDiff));
                } /*else if (nextNextLowMaDiff > nextNextLowThreshold) {
                      isLowerMADiffValid = true;
                      evaluationLogService.add(stockTechnicals, EvaluationLog.Type.POSITIVE,
                              StringUtils.format("nextNextLowDiff:{} > {} → true",
                                      nextNextLowMaDiff, nextNextLowThreshold));
                  } */ else {
                    evaluationLogService.add(
                            stockTechnicals,
                            EvaluationLog.Type.NEUTRAL,
                            StringUtils.format(
                                    "MA nextLowDiff:{} and nextNextLowDiff:{} did not meet"
                                            + " thresholds",
                                    nextLowMaDiff,
                                    nextNextLowMaDiff));
                }
            }
        }

        return isLowerMADiffValid;
    }

    public boolean isHighestMovingAverageDiffValid(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult,
            MAInteractionType maInteractionType) {
        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);
        /*
        double maToHighestPercentageDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), highestMovingAverageResult.getPrevValue());

         */

        double maToHighestPercentageDiff =
                formulaService.ceilToNearestHalf(
                        formulaService.calculateChangePercentage(
                                Math.min(stockPrice.getOpen(), stockPrice.getPrevClose()),
                                highestMovingAverageResult.getPrevValue()));

        boolean result =
                MAThresholdsConfig.getThreshold(maInteractionType, evaluationResult.getLength())
                        .map(threshold -> maToHighestPercentageDiff >= threshold)
                        .orElse(true);

        if (result) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Valid HighestMovingAverageDiff {} found for {} HIGHEST {} and"
                                    + " difference is {} ",
                            maInteractionType,
                            Math.min(stockPrice.getOpen(), stockPrice.getPrevClose()),
                            highestMovingAverageResult.getPrevValue(),
                            maToHighestPercentageDiff));
        }

        return result;
    }

    public boolean isLowestMovingAverageDiffValid(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);
        /*
        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(), evaluationResult.getPrevValue());
         */

        double maPercentageDiff =
                formulaService.ceilToNearestHalf(
                        formulaService.calculateChangePercentage(
                                lowestMovingAverageResult.getPrevValue(),
                                Math.max(stockPrice.getOpen(), stockPrice.getPrevClose())));

        boolean result =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.BREAKDOWN, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        if (result) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Valid LowestMovingAverageDiff found for {} HIGHEST {} and difference"
                                    + " is {} ",
                            Math.min(stockPrice.getOpen(), stockPrice.getPrevClose()),
                            lowestMovingAverageResult.getPrevValue(),
                            maPercentageDiff));
        }

        return result;
    }

    public boolean isLowestAndHighestMovingAverageDiffInNarrowRange(
            Timeframe timeframe, StockTechnicals stockTechnicals) {

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double lowestAndHighestPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue());

        return formulaService.ceilToNearestHalf(lowestAndHighestPercentageDiff) <= 2.5;
    }

    public boolean isLowestAndHighestMovingAverageDiffInWideRange(
            Timeframe timeframe, StockTechnicals stockTechnicals) {

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double lowestAndHighestPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue());

        return formulaService.ceilToNearestHalf(lowestAndHighestPercentageDiff) >= 10.0;
    }

    public boolean isBullishCandle(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Timeframe timeframe = stockPrice.getTimeframe();

        boolean isGapUp =
                CandleStickUtils.isGapUp(stockPrice) || CandleStickUtils.isPrevGapUp(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals)
                        || CandleStickUtils.isPrevSessionStrongBody(
                                timeframe, stockPrice, stockTechnicals);
        boolean isStrongLowerWick =
                CandleStickUtils.isStrongLowerWick(stockPrice)
                        || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

        boolean isBullishConfirmed =
                candleStickConfirmationService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        boolean result = isStrongBody || isStrongLowerWick || isBullishConfirmed || isGapUp;

        if (result) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Bullish Candle found for isStrongBody: {}, isStrongLowerWick: {},"
                                    + " isBullishConfirmed: {}, isGapUp: {} ",
                            isStrongBody,
                            isStrongLowerWick,
                            isBullishConfirmed,
                            isGapUp));
        }

        return result;
    }

    public boolean isBearishCandle(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockPrice.getTimeframe();

        boolean isGapDown =
                CandleStickUtils.isGapDown(stockPrice)
                        || CandleStickUtils.isPrevGapDown(stockPrice);
        ;

        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals)
                        || CandleStickUtils.isPrevSessionStrongBody(
                                timeframe, stockPrice, stockTechnicals);

        boolean isStrongUpperWick =
                CandleStickUtils.isStrongUpperWick(stockPrice)
                        || CandleStickUtils.isPrevStrongUpperWick(stockPrice);

        boolean isBearishConfirmed =
                candleStickConfirmationService.isBearishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        boolean result = isStrongBody || isStrongUpperWick || isBearishConfirmed || isGapDown;

        if (result) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEGATIVE,
                    StringUtils.format(
                            "Bearish Candle found for isStrongBody: {}, isStrongUpperWick: {},"
                                    + " isBearishConfirmed: {}, isGapDown: {} ",
                            isStrongBody,
                            isStrongUpperWick,
                            isBearishConfirmed,
                            isGapDown));
        }

        return result;
    }

    private boolean isMacdNearTurningUp(StockTechnicals stockTechnicals) {

        if (stockTechnicals == null) {
            return false;
        }

        double macd = stockTechnicals.getMacd();
        double signal = stockTechnicals.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd < signal && macdIndicatorService.isHistogramBelowZero(stockTechnicals)) {

            return macdIndicatorService.isMacdIncreased(stockTechnicals)
                    && macdIndicatorService.isSignalDecreased(stockTechnicals)
                    && macdIndicatorService.isHistogramIncreased(stockTechnicals);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(stockTechnicals);
    }

    private boolean isMacdTurningUp(StockTechnicals stockTechnicals) {

        return macdIndicatorService.isMacdIncreased(stockTechnicals)
                && macdIndicatorService.isSignalIncreased(stockTechnicals)
                && macdIndicatorService.isHistogramIncreased(stockTechnicals)
                && macdIndicatorService.isMacdBelowZero(stockTechnicals);
    }

    public boolean isMacdConfirmingBreakout(StockTechnicals stockTechnicals) {
        return this.isMacdNearTurningUp(stockTechnicals) || this.isMacdTurningUp(stockTechnicals);
    }

    public boolean currentBreakoutConfirmation(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        boolean isBullishCandle = this.isBullishCandle(stockPrice, stockTechnicals);

        boolean isMacdConfirmingBreakout = this.isMacdConfirmingBreakout(stockTechnicals);

        return isBullishCandle
                && isMacdConfirmingBreakout
                && rsiIndicatorService.isBullish(stockTechnicals)
                && volumeIndicatorService.isVolumeSurge(stockTechnicals);
    }

    public boolean higherTimeframeBreakoutConfirmation(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            StockTechnicals htStockTechnicals) {

        boolean isHigherTimeframeRsiAndMacdBullish =
                (this.isMacdConfirmingBreakout(htStockTechnicals))
                        && rsiIndicatorService.isBullish(htStockTechnicals);

        return isHigherTimeframeRsiAndMacdBullish;
    }

    public boolean currentBreakdownConfirmation(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        boolean isBearishCandle = this.isBearishCandle(stockPrice, stockTechnicals);

        boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPrice);

        boolean isPrevStrongUpperWick = CandleStickUtils.isPrevStrongUpperWick(stockPrice);

        return isBearishCandle && (isPrevRed || isPrevStrongUpperWick);
    }

    public double calculateEntryPrice(
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
}
