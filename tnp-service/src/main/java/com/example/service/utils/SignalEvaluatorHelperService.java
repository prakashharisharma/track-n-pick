package com.example.service.utils;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.util.FormulaService;
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

    public boolean isHigherMovingAverageDiffValid(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        MovingAverageLength currentLength = evaluationResult.getLength();

        double nextHighThreshold = 2.5;
        double nextNextHighThreshold = 5.0;

        boolean isHigherMADiffValid = false;

        // Get next higher MA and its % difference
        MovingAverageLength nextHighLength = currentLength.getHigher();
        MovingAverageResult nextHighMA =
                MovingAverageUtil.getMovingAverage(
                        nextHighLength, timeframe, stockTechnicals, true);

        double nextHighMaDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), nextHighMA.getPrevValue());

        // Special handling if current length is HIGH — check both higher and lower MAs
        if (currentLength == MovingAverageLength.HIGH) {
            MovingAverageLength nextLowLength = currentLength.getLower();
            MovingAverageResult nextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextLowLength, timeframe, stockTechnicals, true);

            double nextLowMaDiff =
                    formulaService.calculateChangePercentage(
                            nextLowMA.getPrevValue(), evaluationResult.getPrevValue());

            isHigherMADiffValid = nextHighMaDiff >= 2.5 && nextLowMaDiff >= 2.5;

        } else {
            if (nextHighMaDiff >= nextHighThreshold) {
                isHigherMADiffValid = true;
            } else {
                // Check next-next higher MA
                MovingAverageLength nextNextHighLength = nextHighLength.getHigher();

                MovingAverageResult nextNextHighMA =
                        MovingAverageUtil.getMovingAverage(
                                nextNextHighLength, timeframe, stockTechnicals, true);

                double nextNextHighMaDiff =
                        formulaService.calculateChangePercentage(
                                evaluationResult.getPrevValue(), nextNextHighMA.getPrevValue());

                if (nextHighMaDiff <= 0.5
                        && nextNextHighMaDiff >= (nextHighThreshold + nextHighMaDiff)) {
                    isHigherMADiffValid = true;
                } else if (nextNextHighMaDiff > nextNextHighThreshold) {
                    isHigherMADiffValid = true;
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

        double nextLowThreshold = 2.5;
        double nextNextLowThreshold = 5.0;

        boolean isLowerMADiffValid = false;

        // Get next lower MA and its % difference
        MovingAverageLength nextLowLength = currentLength.getLower();
        MovingAverageResult nextLowMA =
                MovingAverageUtil.getMovingAverage(nextLowLength, timeframe, stockTechnicals, true);

        double nextLowMaDiff =
                formulaService.calculateChangePercentage(
                        nextLowMA.getPrevValue(), evaluationResult.getPrevValue());

        // Special handling if current length is LOW — check both higher and lower MAs
        if (currentLength == MovingAverageLength.LOW) {
            MovingAverageLength nextHighLength = currentLength.getHigher();
            MovingAverageResult nextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextHighLength, timeframe, stockTechnicals, true);

            double nextHighMaDiff =
                    formulaService.calculateChangePercentage(
                            nextHighMA.getPrevValue(), evaluationResult.getPrevValue());

            isLowerMADiffValid = nextLowMaDiff >= 2.5 && nextHighMaDiff >= 2.5;

        } else {
            if (nextLowMaDiff >= nextLowThreshold) {
                isLowerMADiffValid = true;
            } else {
                // Check next-next lower MA
                MovingAverageLength nextNextLowLength = nextLowLength.getLower();

                MovingAverageResult nextNextLowMA =
                        MovingAverageUtil.getMovingAverage(
                                nextNextLowLength, timeframe, stockTechnicals, true);

                double nextNextLowMaDiff =
                        formulaService.calculateChangePercentage(
                                evaluationResult.getPrevValue(), nextNextLowMA.getPrevValue());

                if (nextLowMaDiff <= 0.5
                        && nextNextLowMaDiff >= (nextLowThreshold + nextLowMaDiff)) {
                    isLowerMADiffValid = true;
                } else if (nextNextLowMaDiff > nextNextLowThreshold) {
                    isLowerMADiffValid = true;
                }
            }
        }

        return isLowerMADiffValid;
    }

    public boolean isHighestMovingAverageDiffValid(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double maToHighestPercentageDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), highestMovingAverageResult.getPrevValue());

        return MAThresholdsConfig.getThreshold(
                        MAInteractionType.BREAKOUT, evaluationResult.getLength())
                .map(threshold -> maToHighestPercentageDiff >= threshold)
                .orElse(true);
    }

    public boolean isLowestMovingAverageDiffValid(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(), evaluationResult.getPrevValue());

        return MAThresholdsConfig.getThreshold(
                        MAInteractionType.BREAKDOWN, evaluationResult.getLength())
                .map(threshold -> maPercentageDiff >= threshold)
                .orElse(true);
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

        return lowestAndHighestPercentageDiff <= 2.5;
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

        return lowestAndHighestPercentageDiff > 10.0;
    }

    public boolean isBullishCandle(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Timeframe timeframe = stockPrice.getTimeframe();

        boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isStrongLowerWick =
                CandleStickUtils.isStrongLowerWick(stockPrice)
                        || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

        boolean isBullishConfirmed =
                candleStickConfirmationService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        return isGapUp || isStrongBody || isStrongLowerWick || isBullishConfirmed;
    }

    public boolean isBearishCandle(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockPrice.getTimeframe();

        boolean isGapDown = CandleStickUtils.isGapUp(stockPrice);

        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isStrongUpperWick =
                CandleStickUtils.isStrongUpperWick(stockPrice)
                        || CandleStickUtils.isPrevStrongUpperWick(stockPrice);

        boolean isBearishConfirmed =
                candleStickConfirmationService.isBearishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        return isGapDown || isStrongBody || isStrongUpperWick || isBearishConfirmed;
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
                && volumeIndicatorService.isVolumeAverage(stockTechnicals);
    }

    public boolean higherTimeframeBreakoutConfirmation(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            StockTechnicals htStockTechnicals) {

        Timeframe htTimeframe = htStockTechnicals.getTimeframe();
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
