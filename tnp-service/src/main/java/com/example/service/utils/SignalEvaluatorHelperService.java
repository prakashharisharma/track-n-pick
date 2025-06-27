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

    private final SingleSessionCandleStickService singleSessionCandleStickService;
    private final ResistanceValidationService resistanceValidationService;

    public boolean isNearestMovingAverageDiffValidForBreakout(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult,
            boolean sortByValue) {

        MovingAverageLength currentLength = evaluationResult.getLength();

        double nextHighThreshold = 2.5;
        double nextLowThreshold = 2.5;

        if (MovingAverageUtil.isLowerMovingAverageIncreasing(
                currentLength, stockTechnicals, sortByValue)) {
            nextHighThreshold = nextHighThreshold - 1.0;
            nextLowThreshold = nextLowThreshold - 1.0;
        }

        double nextNextHighThreshold = 2 * nextHighThreshold;
        double nextNextLowThreshold = 2 * nextLowThreshold;

        boolean isValid = false;

        MovingAverageLength nextHighLength = currentLength.getHigher(sortByValue);
        MovingAverageResult nextHighMA =
                MovingAverageUtil.getMovingAverage(
                        nextHighLength, timeframe, stockTechnicals, sortByValue);
        double nextHighDiff =
                formulaService.ceilToNearestQuarter(
                        formulaService.calculateAbsChangePercentage(
                                evaluationResult.getPrevValue(), nextHighMA.getPrevValue()));

        MovingAverageLength nextLowLength = currentLength.getLower(sortByValue);

        MovingAverageResult nextLowMA =
                MovingAverageUtil.getMovingAverage(
                        nextLowLength, timeframe, stockTechnicals, sortByValue);
        double nextLowDiff =
                formulaService.ceilToNearestQuarter(
                        formulaService.calculateAbsChangePercentage(
                                nextLowMA.getPrevValue(), evaluationResult.getPrevValue()));

        /*
        boolean isHighest = (sortByValue ? currentLength == MovingAverageLength.HIGHEST : currentLength == MovingAverageLength.LOWEST);
        boolean isLowest = (sortByValue ? currentLength == MovingAverageLength.LOWEST : currentLength == MovingAverageLength.HIGHEST);
        */
        boolean isHighest = currentLength == MovingAverageLength.HIGHEST;
        boolean isLowest = currentLength == MovingAverageLength.LOWEST;

        if (isLowest) {
            // Check only nextHighDiff or nextNextHighDiff
            MovingAverageLength nextNextHighLength = nextHighLength.getHigher(sortByValue);
            MovingAverageResult nextNextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextHighLength, timeframe, stockTechnicals, sortByValue);
            double nextNextHighDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(),
                                    nextNextHighMA.getPrevValue()));

            isValid =
                    nextHighDiff >= nextHighThreshold
                            || (nextNextHighDiff >= nextNextHighThreshold);

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} → nextHighDiff:{} (≥ {}) OR nextNextHighDiff:{} (≥ {}) → {}",
                            timeframe.name(),
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextHighDiff,
                            nextHighThreshold,
                            nextNextHighDiff,
                            nextNextHighThreshold,
                            isValid));

        } else if (isHighest) {

            MovingAverageLength nextNextLowLength = nextLowLength.getLower(sortByValue);
            MovingAverageResult nextNextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextLowLength, timeframe, stockTechnicals, sortByValue);
            double nextNextLowDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(), nextNextLowMA.getPrevValue()));

            isValid = nextLowDiff >= nextLowThreshold || (nextNextLowDiff >= nextNextLowThreshold);

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} → nextLowDiff:{} (≥ {}) OR nextNextLowDiff:{} (≥ {}) → {}",
                            timeframe.name(),
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextLowDiff,
                            nextLowThreshold,
                            nextNextLowDiff,
                            nextNextLowThreshold,
                            isValid));
        } else {
            // MID MAs — both high and low must satisfy
            MovingAverageLength nextNextHighLength = nextHighLength.getHigher(sortByValue);
            MovingAverageResult nextNextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextHighLength, timeframe, stockTechnicals, sortByValue);
            double nextNextHighDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(),
                                    nextNextHighMA.getPrevValue()));

            MovingAverageLength nextNextLowLength = nextLowLength.getLower(sortByValue);
            MovingAverageResult nextNextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextLowLength, timeframe, stockTechnicals, sortByValue);
            double nextNextLowDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(), nextNextLowMA.getPrevValue()));

            boolean highValid =
                    nextHighDiff >= nextHighThreshold
                            || (nextNextHighDiff >= nextNextHighThreshold);

            boolean lowValid =
                    nextLowDiff >= nextLowThreshold || (nextNextLowDiff >= nextNextLowThreshold);

            isValid = highValid && lowValid;

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} MA → "
                                    + "nextHighDiff:{} (≥ {}) OR nextNextHighDiff:{} (≥ {}) | "
                                    + "nextLowDiff:{} (≥ {}) OR nextNextLowDiff:{} (≥ {}) → "
                                    + "highValid:{} lowValid:{} → {}",
                            timeframe.name(),
                            currentLength,
                            nextHighDiff,
                            nextHighThreshold,
                            nextNextHighDiff,
                            nextNextHighThreshold,
                            nextLowDiff,
                            nextLowThreshold,
                            nextNextLowDiff,
                            nextNextLowThreshold,
                            highValid,
                            lowValid,
                            isValid));
        }

        return isValid;
    }

    public boolean isNearestMovingAverageDiffValidForBreakdown(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult,
            boolean sortByValue) {

        MovingAverageLength currentLength = evaluationResult.getLength();
        double nextHighThreshold = 2.5;
        double nextLowThreshold = 2.5;

        if (MovingAverageUtil.isLowerMovingAverageIncreasing(
                currentLength, stockTechnicals, sortByValue)) {
            nextHighThreshold = nextHighThreshold - 1.0;
            nextLowThreshold = nextLowThreshold - 1.0;
        }

        double nextNextHighThreshold = 2 * nextHighThreshold;
        double nextNextLowThreshold = 2 * nextLowThreshold;

        boolean isValid = false;

        MovingAverageLength nextHighLength = currentLength.getHigher(sortByValue);
        MovingAverageResult nextHighMA =
                MovingAverageUtil.getMovingAverage(
                        nextHighLength, timeframe, stockTechnicals, sortByValue);
        double nextHighDiff =
                formulaService.ceilToNearestQuarter(
                        formulaService.calculateAbsChangePercentage(
                                evaluationResult.getPrevValue(), nextHighMA.getPrevValue()));

        MovingAverageLength nextLowLength = currentLength.getLower(sortByValue);

        MovingAverageResult nextLowMA =
                MovingAverageUtil.getMovingAverage(
                        nextLowLength, timeframe, stockTechnicals, sortByValue);
        double nextLowDiff =
                formulaService.ceilToNearestQuarter(
                        formulaService.calculateAbsChangePercentage(
                                nextLowMA.getPrevValue(), evaluationResult.getPrevValue()));

        /*
        boolean isHighest = (sortByValue ? currentLength == MovingAverageLength.HIGHEST : currentLength == MovingAverageLength.LOWEST);
        boolean isLowest = (sortByValue ? currentLength == MovingAverageLength.LOWEST : currentLength == MovingAverageLength.HIGHEST);
        */
        boolean isHighest = currentLength == MovingAverageLength.HIGHEST;
        boolean isLowest = currentLength == MovingAverageLength.LOWEST;

        if (isLowest) {
            // Check only nextHighDiff or nextNextHighDiff
            MovingAverageLength nextNextHighLength = nextHighLength.getHigher(sortByValue);
            MovingAverageResult nextNextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextHighLength, timeframe, stockTechnicals, sortByValue);
            double nextNextHighDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(),
                                    nextNextHighMA.getPrevValue()));

            isValid =
                    nextHighDiff >= nextHighThreshold
                            || (nextNextHighDiff >= nextNextHighThreshold);

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.NEGATIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} → nextHighDiff:{} (≥ {}) OR nextNextHighDiff:{} (≥ {}) → {}",
                            timeframe.name(),
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextHighDiff,
                            nextHighThreshold,
                            nextNextHighDiff,
                            nextNextHighThreshold,
                            isValid));

        } else if (isHighest) {

            MovingAverageLength nextNextLowLength = nextLowLength.getLower(sortByValue);
            MovingAverageResult nextNextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextLowLength, timeframe, stockTechnicals, sortByValue);
            double nextNextLowDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(), nextNextLowMA.getPrevValue()));

            isValid = nextLowDiff >= nextLowThreshold || (nextNextLowDiff >= nextNextLowThreshold);

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.NEGATIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} → nextLowDiff:{} (≥ {}) OR nextNextLowDiff:{} (≥ {}) → {}",
                            timeframe.name(),
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextLowDiff,
                            nextLowThreshold,
                            nextNextLowDiff,
                            nextNextLowThreshold,
                            isValid));
        } else {
            // MID MAs — both high and low must satisfy
            MovingAverageLength nextNextHighLength = nextHighLength.getHigher(sortByValue);
            MovingAverageResult nextNextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextHighLength, timeframe, stockTechnicals, sortByValue);
            double nextNextHighDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(),
                                    nextNextHighMA.getPrevValue()));

            MovingAverageLength nextNextLowLength = nextLowLength.getLower(sortByValue);
            MovingAverageResult nextNextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextLowLength, timeframe, stockTechnicals, sortByValue);
            double nextNextLowDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    evaluationResult.getPrevValue(), nextNextLowMA.getPrevValue()));

            boolean highValid =
                    nextHighDiff >= nextHighThreshold
                            || (nextNextHighDiff >= nextNextHighThreshold);

            boolean lowValid =
                    nextLowDiff >= nextLowThreshold || (nextNextLowDiff >= nextNextLowThreshold);

            isValid = highValid && lowValid;

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.NEGATIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} MA → "
                                    + "nextHighDiff:{} (≥ {}) OR nextNextHighDiff:{} (≥ {}) | "
                                    + "nextLowDiff:{} (≥ {}) OR nextNextLowDiff:{} (≥ {}) → "
                                    + "highValid:{} lowValid:{} → {}",
                            timeframe.name(),
                            currentLength,
                            nextHighDiff,
                            nextHighThreshold,
                            nextNextHighDiff,
                            nextNextHighThreshold,
                            nextLowDiff,
                            nextLowThreshold,
                            nextNextLowDiff,
                            nextNextLowThreshold,
                            highValid,
                            lowValid,
                            isValid));
        }

        return isValid;
    }

    public boolean isLowestAndHighestMovingAverageDiffValid(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAInteractionType maInteractionType,
            boolean sortByValue) {

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, sortByValue);

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, sortByValue);

        double lowestToHighestPercentageDiff =
                formulaService.calculateAbsChangePercentage(
                        lowestMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue());

        MovingAverageLength thresholdLookupLength = getThresholdLookupLength(maInteractionType);

        boolean result =
                MAThresholdsConfig.getThreshold(maInteractionType, thresholdLookupLength)
                        .map(threshold -> lowestToHighestPercentageDiff >= threshold)
                        .orElse(true);

        evaluationLogService.add(
                stockPrice,
                (maInteractionType == MAInteractionType.BREAKOUT
                                        || maInteractionType == MAInteractionType.SUPPORT)
                                && result
                        ? EvaluationLog.Type.POSITIVE
                        : EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        "{} Valid {} LowestToHighestPercentageDiff {} for LOWEST {} HIGHEST {} and"
                                + " difference is {} ",
                        timeframe.name(),
                        maInteractionType,
                        result,
                        lowestMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue(),
                        lowestToHighestPercentageDiff));

        return result;
    }

    public MovingAverageLength getThresholdLookupLength(MAInteractionType maInteractionType) {
        if (maInteractionType == MAInteractionType.BREAKOUT
                || maInteractionType == MAInteractionType.SUPPORT) {
            return MovingAverageLength.LOWEST;
        }

        return MovingAverageLength.HIGHEST;
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
                            "{} Valid LowestMovingAverageDiff found for {} HIGHEST {} and"
                                    + " difference is {} ",
                            timeframe.name(),
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
                            "{} Bullish Candle found for isStrongBody: {}, isStrongLowerWick: {},"
                                    + " isBullishConfirmed: {}, isGapUp: {} ",
                            timeframe.name(),
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
                            "{} Bearish Candle found for isStrongBody: {}, isStrongUpperWick: {},"
                                    + " isBearishConfirmed: {}, isGapDown: {} ",
                            timeframe.name(),
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

        boolean isNearTurningUp = this.isMacdNearTurningUp(stockTechnicals);
        boolean isTurningUp = this.isMacdTurningUp(stockTechnicals);
        boolean result = isNearTurningUp || isTurningUp;

        evaluationLogService.add(
                stockTechnicals,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format(
                        "{} MACD breakout confirmation: isMacdNearTurningUp: {}, isMacdTurningUp:"
                                + " {} → {}",
                        stockTechnicals.getTimeframe().name(),
                        isNearTurningUp,
                        isTurningUp,
                        result));

        return result;
    }

    public boolean currentBreakoutConfirmation(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        boolean isBullishCandle = this.isBullishCandle(stockPrice, stockTechnicals);

        boolean isMacdConfirmingBreakout = this.isMacdConfirmingBreakout(stockTechnicals);

        return isBullishCandle
                && isMacdConfirmingBreakout
                && rsiIndicatorService.isBullish(stockTechnicals)
                && volumeIndicatorService.isVolumeSurge(stockTechnicals)
                && resistanceValidationService.isOutsideHigherTimeframeResistanceZone(stockPrice);
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

        boolean isLowerHighAndLowerLow =
                CandleStickUtils.isLowerHigh(stockPrice) && CandleStickUtils.isLowerLow(stockPrice);

        return isBearishCandle && (isPrevRed || isPrevStrongUpperWick || isLowerHighAndLowerLow);
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

        boolean isBodyAboveBreakoutLevel = open > breakoutValue && close > breakoutValue;
        // Additional logic for breakout body % and RSI
        double bodySize = Math.abs(close - open);
        double bodyAboveBreakout = close > breakoutValue ? close - breakoutValue : 0;
        boolean isBreakoutCrossedHalfBody = bodySize > 0 && (bodyAboveBreakout / bodySize) > 0.5;

        if (singleSessionCandleStickService.isBullishMarubozu(
                        timeframe, stockPrice, stockTechnicals)
                || CandleStickUtils.isCloseHighEqual(stockPrice)) {
            entryPrice = formulaService.applyPercentChange(close, 1);
        } else if (stockTechnicals.getRsi() > 50
                && CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals)) {
            double percentAboveBreakout =
                    formulaService.calculatePercentage(bodySize, bodyAboveBreakout);
            double highMinusClose = high - close;

            if (percentAboveBreakout > 0 && percentAboveBreakout < 100) {
                double adjustment =
                        formulaService.calculateFraction(highMinusClose, percentAboveBreakout);

                entryPrice = close + adjustment;
            } else {
                entryPrice = (high + close) / 2.0;
            }

        } else if (isUpperWickClean && Math.ceil(stockTechnicals.getRsi()) >= 60.0) {
            entryPrice = high;
        } else if (isUpperWickClean
                && (isBodyAboveBreakoutLevel || Math.ceil(stockTechnicals.getRsi()) >= 50.0)) {
            entryPrice = (high + close) / 2.0;
        } else if (isBodyAboveBreakoutLevel) {
            entryPrice = (open + close) / 2.0;
            entryPrice = entryPrice * 1.00382;
        } else if (isUpperWickClean && isHistogramAboveZero) {
            entryPrice = (high + close) / 2.0;
        } else if (CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals)
                && CandleStickUtils.bodySize(stockPrice)
                        >= CandleStickUtils.prevSessionBodySize(stockPrice)) {
            entryPrice = (high + close) / 2.0;
        } else if (CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals)
                && CandleStickUtils.range(stockPrice)
                        >= CandleStickUtils.prevSessionRange(stockPrice)) {
            entryPrice = (high + close) / 2.0;
        }

        if (macdIndicatorService.isMacdCrossedSignal(stockTechnicals)
                || MovingAverageUtil.isAllMAsIncreasing(stockTechnicals)) {
            entryPrice = formulaService.ceilToNearestHalf(entryPrice);
            entryPrice =
                    Math.max(formulaService.applyPercentChange(entryPrice, 0.05), entryPrice + 0.5);
            return formulaService.ceilToNearestFive(entryPrice);
        } else if (macdIndicatorService.isHistogramGreen(stockTechnicals)) {
            entryPrice = formulaService.ceilToNearestHalf(entryPrice);
            entryPrice =
                    Math.max(
                            formulaService.applyPercentChange(entryPrice, 0.025),
                            entryPrice + 0.25);
            return formulaService.ceilToNearestFive(entryPrice);
        }

        return formulaService.ceilToNearestHalf(entryPrice);
    }
}
