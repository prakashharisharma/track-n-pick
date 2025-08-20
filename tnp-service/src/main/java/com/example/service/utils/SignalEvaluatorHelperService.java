package com.example.service.utils;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.EvaluationLog;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
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

    private final AdxIndicatorService adxIndicatorService;

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final TimeframeSupportResistanceService timeframeSupportResistanceService;

    private final EvaluationLogService evaluationLogService;

    private final SingleSessionCandleStickService singleSessionCandleStickService;
    private final ResistanceValidationService resistanceValidationService;

    private final FundamentalResearchService fundamentalResearchService;

    public boolean isHighAndHighestMovingAverageDiffValid(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAInteractionType maInteractionType,
            boolean sortByValue) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double threshold = 2.0;

        if (MovingAverageUtil.validatedMa5MA20AndMa50(stockTechnicals)) {
            threshold = 1.5;
        }

        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(
                        fundamentalResearchService.marketCap(stockTechnicals.getStock()));

        if (MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals)
                > MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals)) {
            if (MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals)
                    > MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals)) {
                // sortByValue = false;
            }
        }

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, sortByValue);

        MovingAverageResult highMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGH, timeframe, stockTechnicals, sortByValue);

        double highToHighestDiff =
                formulaService.calculateAbsChangePercentage(
                        highMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue());

        boolean result = highToHighestDiff >= threshold;

        evaluationLogService.add(
                stockPrice,
                result ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        "{} {} High to Highest MA Diff:{} (> {}) → {}",
                        timeframe.name(),
                        marketCapCategory,
                        highToHighestDiff,
                        threshold,
                        result));

        return result;
    }

    public boolean isNearestMovingAverageDiffValidForBreakout(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult,
            boolean sortByValue) {

        if (stockTechnicals == null) {
            return false;
        }

        MovingAverageLength currentLength = evaluationResult.getLength();

        double nextHighThreshold;
        double nextLowThreshold;

        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(
                        fundamentalResearchService.marketCap(stockTechnicals.getStock()));

        // Set default thresholds based on market cap
        switch (marketCapCategory) {
            case MEGACAP:
                nextHighThreshold = 2.25;
                nextLowThreshold = 2.25;
                break;
            case LARGECAP:
                nextHighThreshold = 2.50;
                nextLowThreshold = 2.50;
                break;
            case MIDCAP:
                nextHighThreshold = 2.75;
                nextLowThreshold = 2.75;
                break;
            default: // SMALLCAP
                nextHighThreshold = 3.0;
                nextLowThreshold = 3.0;
                break;
        }

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

        int increasingMaCount = MovingAverageUtil.increasingMaCount(stockTechnicals);
        boolean allMaIncreasing = MovingAverageUtil.isAllMAsIncreasing(stockTechnicals);

        // Adjust thresholds based on MA conditions
        if (allMaIncreasing) {
            // If all MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    nextHighThreshold = 1.5;
                    nextLowThreshold = 1.5;
                    break;
                case LARGECAP:
                    nextHighThreshold = 1.75;
                    nextLowThreshold = 1.75;
                    break;
                case MIDCAP:
                    nextHighThreshold = 2.00;
                    nextLowThreshold = 2.00;
                    break;
                default: // SMALLCAP
                    nextHighThreshold = 2.25;
                    nextLowThreshold = 2.25;
                    break;
            }
        } else if (increasingMaCount == 4) {
            // If 4 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    nextHighThreshold = 1.75;
                    nextLowThreshold = 1.75;
                    break;
                case LARGECAP:
                    nextHighThreshold = 2.00;
                    nextLowThreshold = 2.00;
                    break;
                case MIDCAP:
                    nextHighThreshold = 2.25;
                    nextLowThreshold = 2.25;
                    break;
                default: // SMALLCAP
                    nextHighThreshold = 2.5;
                    nextLowThreshold = 2.5;
                    break;
            }
        } else if (increasingMaCount == 3) {
            // If 3 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    nextHighThreshold = 2.0;
                    nextLowThreshold = 2.0;
                    break;
                case LARGECAP:
                    nextHighThreshold = 2.25;
                    nextLowThreshold = 2.25;
                    break;
                case MIDCAP:
                    nextHighThreshold = 2.50;
                    nextLowThreshold = 2.50;
                    break;
                default: // SMALLCAP
                    nextHighThreshold = 2.75;
                    nextLowThreshold = 2.75;
                    break;
            }
        }

        nextHighThreshold = formulaService.ceilToNearestTen(nextHighThreshold);
        nextLowThreshold = formulaService.ceilToNearestTen(nextHighThreshold);
        // Default thresholds are already set above

        boolean isHighest = currentLength == MovingAverageLength.HIGHEST;
        boolean isLowest = currentLength == MovingAverageLength.LOWEST;

        if (isLowest
                || currentLength == MovingAverageLength.LOW
                || currentLength == MovingAverageLength.MEDIUM) {
            // For Lowest, Low, and Medium MA - check higher diffs
            MovingAverageLength nextNextHighLength = nextHighLength.getHigher(sortByValue);
            MovingAverageResult nextNextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextHighLength, timeframe, stockTechnicals, sortByValue);
            double nextNextHighDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextHighMA.getPrevValue(), nextNextHighMA.getPrevValue()));

            double diffBetweenNextHighs =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextHighMA.getPrevValue(), nextNextHighMA.getPrevValue()));

            if (nextHighDiff < 1.0) {
                isValid = nextNextHighDiff >= nextHighThreshold && diffBetweenNextHighs > 1.0;
            } else {
                isValid = nextHighDiff >= nextHighThreshold;
            }

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} {} → nextHighDiff:{} {} → {}",
                            timeframe.name(),
                            marketCapCategory,
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextHighDiff,
                            nextHighDiff < 1.0
                                    ? ", nextNextHighDiff:"
                                            + nextNextHighDiff
                                            + " (> "
                                            + nextHighThreshold
                                            + "), diffBetweenNextHighs:"
                                            + diffBetweenNextHighs
                                            + " (> 1.0)"
                                    : " (≥ " + nextHighThreshold + ")",
                            isValid));

        } else if (currentLength == MovingAverageLength.HIGH) {
            // For High MA - check higher first, then lower if needed
            if (nextHighDiff < 1.0) {
                isValid = nextLowDiff >= nextLowThreshold;
            } else {
                isValid = nextHighDiff >= nextHighThreshold;
            }

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} {} → {} → {}",
                            timeframe.name(),
                            marketCapCategory,
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextHighDiff < 1.0
                                    ? "nextHighDiff:"
                                            + nextHighDiff
                                            + " < 1.0, nextLowDiff:"
                                            + nextLowDiff
                                            + " (≥ "
                                            + nextLowThreshold
                                            + ")"
                                    : "nextHighDiff:"
                                            + nextHighDiff
                                            + " (≥ "
                                            + nextHighThreshold
                                            + ")",
                            isValid));
        } else if (isHighest) {
            // For Highest MA - check lower diffs
            MovingAverageLength nextNextLowLength = nextLowLength.getLower(sortByValue);
            MovingAverageResult nextNextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextLowLength, timeframe, stockTechnicals, sortByValue);
            double nextNextLowDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextLowMA.getPrevValue(), nextNextLowMA.getPrevValue()));

            double diffBetweenNextLows =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextLowMA.getPrevValue(), nextNextLowMA.getPrevValue()));

            if (nextLowDiff < 1.0) {
                isValid = nextNextLowDiff >= nextLowThreshold && diffBetweenNextLows > 1.0;
            } else {
                isValid = nextLowDiff >= nextLowThreshold;
            }

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} {} → nextLowDiff:{} {} → {}",
                            timeframe.name(),
                            marketCapCategory,
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextLowDiff,
                            nextLowDiff < 1.0
                                    ? ", nextNextLowDiff:"
                                            + nextNextLowDiff
                                            + " (> "
                                            + nextLowThreshold
                                            + "), diffBetweenNextLows:"
                                            + diffBetweenNextLows
                                            + " (> 1.0)"
                                    : " (≥ " + nextLowThreshold + ")",
                            isValid));
        }

        return isValid;
    }

    public boolean isNearestMovingAverageDiffValidForBreakdown(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult,
            boolean sortByValue) {

        if (stockTechnicals == null) {
            return false;
        }

        MovingAverageLength currentLength = evaluationResult.getLength();

        double nextHighThreshold;
        double nextLowThreshold;

        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(
                        fundamentalResearchService.marketCap(stockTechnicals.getStock()));

        // Set default thresholds based on market cap
        switch (marketCapCategory) {
            case MEGACAP:
                nextHighThreshold = 2.25;
                nextLowThreshold = 2.25;
                break;
            case LARGECAP:
                nextHighThreshold = 2.50;
                nextLowThreshold = 2.50;
                break;
            case MIDCAP:
                nextHighThreshold = 2.75;
                nextLowThreshold = 2.75;
                break;
            default: // SMALLCAP
                nextHighThreshold = 3.0;
                nextLowThreshold = 3.0;
                break;
        }

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

        int decreasingMaCount = MovingAverageUtil.decreasingMaCount(stockTechnicals);
        boolean allMaDecreasing = MovingAverageUtil.isAllMAsDecreasing(stockTechnicals);

        // Adjust thresholds based on MA conditions
        if (allMaDecreasing) {
            // If all MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    nextHighThreshold = 1.5;
                    nextLowThreshold = 1.5;
                    break;
                case LARGECAP:
                    nextHighThreshold = 1.75;
                    nextLowThreshold = 1.75;
                    break;
                case MIDCAP:
                    nextHighThreshold = 2.00;
                    nextLowThreshold = 2.00;
                    break;
                default: // SMALLCAP
                    nextHighThreshold = 2.25;
                    nextLowThreshold = 2.25;
                    break;
            }
        } else if (decreasingMaCount == 4) {
            // If 4 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    nextHighThreshold = 1.75;
                    nextLowThreshold = 1.75;
                    break;
                case LARGECAP:
                    nextHighThreshold = 2.00;
                    nextLowThreshold = 2.00;
                    break;
                case MIDCAP:
                    nextHighThreshold = 2.25;
                    nextLowThreshold = 2.25;
                    break;
                default: // SMALLCAP
                    nextHighThreshold = 2.5;
                    nextLowThreshold = 2.5;
                    break;
            }
        } else if (decreasingMaCount == 3) {
            // If 3 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    nextHighThreshold = 2.0;
                    nextLowThreshold = 2.0;
                    break;
                case LARGECAP:
                    nextHighThreshold = 2.25;
                    nextLowThreshold = 2.25;
                    break;
                case MIDCAP:
                    nextHighThreshold = 2.50;
                    nextLowThreshold = 2.50;
                    break;
                default: // SMALLCAP
                    nextHighThreshold = 2.75;
                    nextLowThreshold = 2.75;
                    break;
            }
        }
        // Default thresholds are already set above
        nextHighThreshold = formulaService.ceilToNearestTen(nextHighThreshold);
        nextLowThreshold = formulaService.ceilToNearestTen(nextHighThreshold);

        boolean isHighest = currentLength == MovingAverageLength.HIGHEST;
        boolean isLowest = currentLength == MovingAverageLength.LOWEST;

        if (isLowest) {
            // For Lowest MA - check higher diffs
            MovingAverageLength nextNextHighLength = nextHighLength.getHigher(sortByValue);
            MovingAverageResult nextNextHighMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextHighLength, timeframe, stockTechnicals, sortByValue);
            double nextNextHighDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextHighMA.getPrevValue(), nextNextHighMA.getPrevValue()));

            double diffBetweenNextHighs =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextHighMA.getPrevValue(), nextNextHighMA.getPrevValue()));

            if (nextHighDiff < 1.0) {
                isValid = nextNextHighDiff >= nextHighThreshold && diffBetweenNextHighs > 1.0;
            } else {
                isValid = nextHighDiff >= nextHighThreshold;
            }

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.NEGATIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} {} → nextHighDiff:{} {} → {}",
                            timeframe.name(),
                            marketCapCategory,
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextHighDiff,
                            nextHighDiff < 1.0
                                    ? ", nextNextHighDiff:"
                                            + nextNextHighDiff
                                            + " (> "
                                            + nextHighThreshold
                                            + "), diffBetweenNextHighs:"
                                            + diffBetweenNextHighs
                                            + " (> 1.0)"
                                    : " (≥ " + nextHighThreshold + ")",
                            isValid));

        } else if (currentLength == MovingAverageLength.LOW) {
            // For Low MA - check higher first, then lower if needed
            if (nextLowDiff < 1.0) {
                isValid = nextHighDiff >= nextHighThreshold;
            } else {
                isValid = nextLowDiff >= nextLowThreshold;
            }

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.NEGATIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} {} → {} → {}",
                            timeframe.name(),
                            marketCapCategory,
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextHighDiff < 1.0
                                    ? "nextHighDiff:"
                                            + nextHighDiff
                                            + " < 1.0, nextLowDiff:"
                                            + nextLowDiff
                                            + " (≥ "
                                            + nextLowThreshold
                                            + ")"
                                    : "nextHighDiff:"
                                            + nextHighDiff
                                            + " (≥ "
                                            + nextHighThreshold
                                            + ")",
                            isValid));
        } else if (isHighest
                || currentLength == MovingAverageLength.HIGH
                || currentLength == MovingAverageLength.MEDIUM) {
            // For Highest, High, and Medium MA - check lower diffs
            MovingAverageLength nextNextLowLength = nextLowLength.getLower(sortByValue);
            MovingAverageResult nextNextLowMA =
                    MovingAverageUtil.getMovingAverage(
                            nextNextLowLength, timeframe, stockTechnicals, sortByValue);
            double nextNextLowDiff =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextLowMA.getPrevValue(), nextNextLowMA.getPrevValue()));

            double diffBetweenNextLows =
                    formulaService.ceilToNearestQuarter(
                            formulaService.calculateAbsChangePercentage(
                                    nextLowMA.getPrevValue(), nextNextLowMA.getPrevValue()));

            if (nextLowDiff < 1.0) {
                isValid = nextNextLowDiff >= nextLowThreshold && diffBetweenNextLows > 1.0;
            } else {
                isValid = nextLowDiff >= nextLowThreshold;
            }

            evaluationLogService.add(
                    stockTechnicals,
                    isValid ? EvaluationLog.Type.NEGATIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} {} → nextLowDiff:{} {} → {}",
                            timeframe.name(),
                            marketCapCategory,
                            (sortByValue)
                                    ? currentLength + " MA"
                                    : "MA" + currentLength.getMaDays(),
                            nextLowDiff,
                            nextLowDiff < 1.0
                                    ? ", nextNextLowDiff:"
                                            + nextNextLowDiff
                                            + " (> "
                                            + nextLowThreshold
                                            + "), diffBetweenNextLows:"
                                            + diffBetweenNextLows
                                            + " (> 1.0)"
                                    : " (≥ " + nextLowThreshold + ")",
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

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double threshold;
        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(
                        fundamentalResearchService.marketCap(stockTechnicals.getStock()));

        // Set default thresholds based on market cap
        switch (marketCapCategory) {
            case MEGACAP:
                threshold = 10.5;
                break;
            case LARGECAP:
                threshold = 13.0;
                break;
            case MIDCAP:
                threshold = 15.0;
                break;
            default: // SMALLCAP
                threshold = 18.0;
                break;
        }

        int increasingMaCount = MovingAverageUtil.increasingMaCount(stockTechnicals);
        boolean allMaIncreasing = MovingAverageUtil.isAllMAsIncreasing(stockTechnicals);

        // Adjust thresholds based on MA conditions
        if (allMaIncreasing) {
            // If all MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    threshold = 7.5;
                    break;
                case LARGECAP:
                    threshold = 10.0;
                    break;
                case MIDCAP:
                    threshold = 12.5;
                    break;
                default: // SMALLCAP
                    threshold = 15.0;
                    break;
            }
        } else if (increasingMaCount == 4) {
            // If 4 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    threshold = 8.5;
                    break;
                case LARGECAP:
                    threshold = 11.0;
                    break;
                case MIDCAP:
                    threshold = 13.5;
                    break;
                default: // SMALLCAP
                    threshold = 16.0;
                    break;
            }
        } else if (increasingMaCount == 3) {
            // If 3 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    threshold = 9.5;
                    break;
                case LARGECAP:
                    threshold = 12.0;
                    break;
                case MIDCAP:
                    threshold = 14.0;
                    break;
                default: // SMALLCAP
                    threshold = 17.0;
                    break;
            }
        }

        threshold = formulaService.ceilToNearestQuarter(threshold);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, sortByValue);

        if (highestMovingAverageResult.getPrevValue() == 0.0) {
            highestMovingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            MovingAverageLength.HIGH, timeframe, stockTechnicals, sortByValue);
        }

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, sortByValue);

        if (lowestMovingAverageResult.getPrevValue() == 0.0) {
            lowestMovingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            MovingAverageLength.LOW, timeframe, stockTechnicals, sortByValue);
        }

        double lowestToHighestDiff =
                formulaService.calculateAbsChangePercentage(
                        lowestMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue());

        boolean result = lowestToHighestDiff >= threshold;

        evaluationLogService.add(
                stockPrice,
                result ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        "{} {} Lowest to Highest MA Diff:{} (> {}) → {} increasing MA:{}",
                        timeframe.name(),
                        marketCapCategory,
                        lowestToHighestDiff,
                        threshold,
                        result,
                        increasingMaCount));

        return result;
    }

    public boolean isHighestAndLowestMovingAverageDiffValid(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAInteractionType maInteractionType,
            boolean sortByValue) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double threshold;
        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(
                        fundamentalResearchService.marketCap(stockTechnicals.getStock()));

        // Set default thresholds based on market cap
        switch (marketCapCategory) {
            case MEGACAP:
                threshold = 10.5;
                break;
            case LARGECAP:
                threshold = 13.0;
                break;
            case MIDCAP:
                threshold = 15.0;
                break;
            default: // SMALLCAP
                threshold = 18.0;
                break;
        }

        int decreasingMaCount = MovingAverageUtil.decreasingMaCount(stockTechnicals);
        boolean allMaDecreasing = MovingAverageUtil.isAllMAsDecreasing(stockTechnicals);

        // Adjust thresholds based on MA conditions
        if (allMaDecreasing) {
            // If all MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    threshold = 7.5;
                    break;
                case LARGECAP:
                    threshold = 10.0;
                    break;
                case MIDCAP:
                    threshold = 12.5;
                    break;
                default: // SMALLCAP
                    threshold = 15.0;
                    break;
            }
        } else if (decreasingMaCount == 4) {
            // If 4 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    threshold = 8.5;
                    break;
                case LARGECAP:
                    threshold = 11.0;
                    break;
                case MIDCAP:
                    threshold = 13.5;
                    break;
                default: // SMALLCAP
                    threshold = 16.0;
                    break;
            }
        } else if (decreasingMaCount == 3) {
            // If 3 MA increasing
            switch (marketCapCategory) {
                case MEGACAP:
                    threshold = 9.5;
                    break;
                case LARGECAP:
                    threshold = 12.0;
                    break;
                case MIDCAP:
                    threshold = 14.5;
                    break;
                default: // SMALLCAP
                    threshold = 17.0;
                    break;
            }
        }
        threshold = formulaService.ceilToNearestQuarter(threshold);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, sortByValue);

        if (highestMovingAverageResult.getPrevValue() == 0.0) {
            highestMovingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            MovingAverageLength.HIGH, timeframe, stockTechnicals, sortByValue);
        }

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, sortByValue);

        if (lowestMovingAverageResult.getPrevValue() == 0.0) {
            lowestMovingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            MovingAverageLength.LOW, timeframe, stockTechnicals, sortByValue);
        }

        double lowestToHighestDiff =
                formulaService.calculateAbsChangePercentage(
                        highestMovingAverageResult.getPrevValue(),
                        lowestMovingAverageResult.getPrevValue());

        boolean result = lowestToHighestDiff >= threshold;

        evaluationLogService.add(
                stockPrice,
                result ? EvaluationLog.Type.NEGATIVE : EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        "{} {} Lowest to Highest MA Diff:{} (> {}) → {} decreasing MA : {}",
                        timeframe.name(),
                        marketCapCategory,
                        lowestToHighestDiff,
                        threshold,
                        result));

        return result;
    }

    public boolean isHighestAlsoBreached(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MovingAverageLength movingAverageLength,
            double value) {

        if (movingAverageLength == MovingAverageLength.HIGHEST) {
            return false;
        }

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double lowestToHighestPercentageDiff =
                formulaService.calculateAbsChangePercentage(
                        highestMovingAverageResult.getValue(), value);

        if (lowestToHighestPercentageDiff < 1.0) {
            return false;
        }

        if (stockPrice.getClose() > highestMovingAverageResult.getValue()) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} {} {} → highestMovingAverage: {} | close :{} is also breached",
                            timeframe.name(),
                            highestMovingAverageResult.getValue(),
                            stockPrice.getClose()));

            return true;
        }

        return false;
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
        if (macd < signal || macd < 0.0) {

            return macdIndicatorService.isHistogramBelowZero(stockTechnicals)
                    && macdIndicatorService.isMacdIncreased(stockTechnicals)
                    && macdIndicatorService.isSignalDecreased(stockTechnicals)
                    && macdIndicatorService.isHistogramIncreased(stockTechnicals);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(stockTechnicals);
    }

    private boolean isMacdTurningUp(StockTechnicals stockTechnicals) {

        if (stockTechnicals == null) {
            return false;
        }

        double macd = stockTechnicals.getMacd();
        double signal = stockTechnicals.getSignal();

        return macdIndicatorService.isMacdIncreased(stockTechnicals)
                && macdIndicatorService.isSignalIncreased(stockTechnicals)
                && macdIndicatorService.isHistogramIncreased(stockTechnicals);
        // && (macdIndicatorService.isMacdBelowZero(stockTechnicals) || macd < signal);
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

    private boolean isMacdNearTurningDown(StockTechnicals stockTechnicals) {

        if (stockTechnicals == null) {
            return false;
        }

        double macd = stockTechnicals.getMacd();
        double signal = stockTechnicals.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd > signal || macd > 0.0) {

            return macdIndicatorService.isHistogramAboveZero(stockTechnicals)
                    && macdIndicatorService.isMacdDecreased(stockTechnicals)
                    && macdIndicatorService.isSignalIncreased(stockTechnicals)
                    && macdIndicatorService.isHistogramDecreased(stockTechnicals);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isSignalCrossedMacd(stockTechnicals);
    }

    private boolean isMacdTurningDown(StockTechnicals stockTechnicals) {

        if (stockTechnicals == null) {
            return false;
        }

        return macdIndicatorService.isMacdDecreased(stockTechnicals)
                && macdIndicatorService.isSignalDecreased(stockTechnicals)
                && macdIndicatorService.isHistogramDecreased(stockTechnicals);
    }

    public boolean isMacdConfirmingBreakdown(StockTechnicals stockTechnicals) {

        boolean isNearTurningDown = this.isMacdNearTurningDown(stockTechnicals);
        boolean isTurningDown = this.isMacdTurningDown(stockTechnicals);
        boolean result = isNearTurningDown || isTurningDown;

        evaluationLogService.add(
                stockTechnicals,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format(
                        "{} MACD breakout confirmation: isMacdNearTurningUp: {}, isMacdTurningUp:"
                                + " {} → {}",
                        stockTechnicals.getTimeframe().name(),
                        isNearTurningDown,
                        isTurningDown,
                        result));

        return result;
    }

    public boolean currentBreakoutConfirmation(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        boolean isBullishCandle = this.isBullishCandle(stockPrice, stockTechnicals);

        boolean isMacdConfirmingBreakout = this.isMacdConfirmingBreakout(stockTechnicals);

        boolean isRsiBullish =
                rsiIndicatorService.isBullish(stockTechnicals)
                        || (stockTechnicals.getPrevRsi() < 30 && stockTechnicals.getRsi() > 38);

        boolean isBullishConfirmed =
                candleStickConfirmationService.isBullishConfirmed(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

        boolean isUpperWickSizeConfirmed =
                candleStickConfirmationService.isUpperWickSizeConfirmed(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        boolean checkHigherTimeFrameResistance =
                (isBullishConfirmed || isBullishCandle)
                                && (CandleStickUtils.isProGapUp(stockPrice)
                                        || adxIndicatorService.isBullishIncr(stockTechnicals))
                        ? false
                        : true;

        evaluationLogService.add(
                stockPrice,
                EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        " isBullishConfirmed {} CandleStickUtils.isProGapUp(stockPrice) {}"
                                + " adxIndicatorService.isBullishIncr(stockTechnicals) {} Timeframe"
                                + " resistance check passed",
                        isBullishConfirmed,
                        CandleStickUtils.isProGapUp(stockPrice),
                        adxIndicatorService.isBullishIncr(stockTechnicals)));

        boolean isHigherTimeframeResistanceCheckPassed =
                (checkHigherTimeFrameResistance
                        ? resistanceValidationService.isOutsideResistanceZone(stockPrice)
                        : true);

        if (isHigherTimeframeResistanceCheckPassed) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format("Timeframe resistance check passed"));
        }

        return isBullishCandle
                && isMacdConfirmingBreakout
                && isRsiBullish
                && (volumeIndicatorService.isVolumeSurge(stockTechnicals))
                && isHigherTimeframeResistanceCheckPassed;
    }

    public boolean higherTimeframeBreakoutConfirmation(
            StockPrice htStockPrice, StockTechnicals htStockTechnicals) {

        boolean isRsiBullish =
                rsiIndicatorService.isBullish(htStockTechnicals)
                        || (htStockTechnicals.getPrevRsi() < 30 && htStockTechnicals.getRsi() > 38);

        boolean isStrongBody =
                CandleStickUtils.isStrongBody(
                        htStockPrice.getTimeframe(), htStockPrice, htStockTechnicals);
        boolean isStrongRange =
                CandleStickUtils.isStrongRange(
                        htStockPrice.getTimeframe(), htStockPrice, htStockTechnicals);

        return this.isMacdConfirmingBreakout(htStockTechnicals)
                && isRsiBullish
                && (isStrongBody || isStrongRange);
    }

    public boolean higherTimeframeBreakdownConfirmation(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            StockTechnicals htStockTechnicals) {

        boolean isHigherTimeframeRsiAndMacdBearish =
                (this.isMacdConfirmingBreakdown(htStockTechnicals))
                        && rsiIndicatorService.isBearish(htStockTechnicals);

        return isHigherTimeframeRsiAndMacdBearish;
    }

    public boolean currentBreakdownConfirmation(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        boolean isBearishCandle = this.isBearishCandle(stockPrice, stockTechnicals);

        boolean isBearishConfirmed =
                candleStickConfirmationService.isBearishConfirmed(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

        boolean isLowerWickSizeConfirmed =
                candleStickConfirmationService.isLowerWickSizeConfirmed(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        boolean checkHigherTimeFrameSupport =
                (isBearishConfirmed && isLowerWickSizeConfirmed) ? false : true;

        boolean isHigherTimeframeSupportCheckPassed =
                (checkHigherTimeFrameSupport
                        ? resistanceValidationService.isOutsideSupportZone(stockPrice)
                        : true);

        if (isHigherTimeframeSupportCheckPassed) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEGATIVE,
                    StringUtils.format("Timeframe support check passed"));
        }

        boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPrice);

        boolean isPrevStrongUpperWick = CandleStickUtils.isPrevStrongUpperWick(stockPrice);

        boolean isLowerHighAndLowerLow =
                CandleStickUtils.isLowerHigh(stockPrice) && CandleStickUtils.isLowerLow(stockPrice);

        return isHigherTimeframeSupportCheckPassed
                && isBearishCandle
                && (isPrevRed || isPrevStrongUpperWick || isLowerHighAndLowerLow);
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

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

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
        if (open >= highestMovingAverageResult.getValue()
                && close >= highestMovingAverageResult.getValue()) {
            entryPrice = (open + high) / 2;
            entryPrice =
                    formulaService.applyPercentChange(
                            Math.max(entryPrice, highestMovingAverageResult.getValue()), 0.2);
        } else if (breakoutValue >= highestMovingAverageResult.getValue()) {

            entryPrice = (highestMovingAverageResult.getValue() + high) / 2;

            if (isBreakoutCrossedHalfBody) {
                entryPrice = (breakoutValue + high) / 2;
            }

            if (macdIndicatorService.isMacdCrossedSignal(stockTechnicals)) {
                entryPrice = (Math.max(open, close) + high) / 2;

                if (isUpperWickClean) {
                    entryPrice = high;
                }
            }

            entryPrice = formulaService.applyPercentChange(entryPrice, 0.5);

            entryPrice = Math.max(entryPrice, highestMovingAverageResult.getValue());

        } else if (singleSessionCandleStickService.isBullishMarubozu(
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

                entryPrice = Math.max((close + adjustment), (high + close) / 2.0);
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
            // entryPrice = formulaService.ceilToNearestHalf(entryPrice);
            entryPrice =
                    Math.max(formulaService.applyPercentChange(entryPrice, 0.2), entryPrice + 0.5);
            return entryPrice;
        } else if (macdIndicatorService.isHistogramGreen(stockTechnicals)) {
            // entryPrice = formulaService.ceilToNearestHalf(entryPrice);
            entryPrice =
                    Math.max(formulaService.applyPercentChange(entryPrice, 0.1), entryPrice + 0.5);
            return entryPrice;
        }

        return entryPrice;
    }

    public boolean isSteapRise(StockPrice stockPrice) {

        if (formulaService.calculateChangePercentage(stockPrice.getPrev6Low(), stockPrice.getHigh())
                > 15.0) {
            return true;
        }

        return false;
    }
}
