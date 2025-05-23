package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.CandleStickUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class CandleStickConfirmationServiceImpl implements CandleStickConfirmationService {

    private final SingleSessionCandleStickService singleSessionCandleStickService;
    private final TwoSessionCandleStickService twoSessionCandleStickService;
    private final ThreeSessionCandleStickService threeSessionCandleStickService;
    private final VolumeIndicatorService volumeIndicatorService;

    @Override
    public boolean isUpperWickSizeConfirmed(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        double latestATR = stockTechnicals.getAtr();
        double latestClose = stockPrice.getClose();

        // Prevent division by zero
        double atrPercent = (latestClose > 0) ? (latestATR / latestClose) * 100 : 0;

        // Define thresholds based on volatility
        double upperWickThresholdDailyHigh = 0.10 * bodySize; // 10% for High Volatility
        double upperWickThresholdDailyModerate = 0.15 * bodySize; // 15% for Moderate Volatility
        double upperWickThresholdDailyLow = 0.20 * bodySize; // 20% for Low Volatility

        double upperWickThresholdWeeklyHigh = 0.05 * bodySize; // 5% for High Volatility
        double upperWickThresholdWeeklyModerate = 0.10 * bodySize; // 10% for Moderate Volatility
        double upperWickThresholdWeeklyLow = 0.15 * bodySize; // 15% for Low Volatility

        double upperWickThresholdMonthly = 0.10 * bodySize; // 10% for Monthly timeframe

        // Define lower wick size thresholds
        double lowerWickThresholdHigh = 0.70 * bodySize; // 70% for High Volatility
        double lowerWickThresholdModerate = 0.60 * bodySize; // 60% for Moderate Volatility
        double lowerWickThresholdLow = 0.50 * bodySize; // 50% for Low Volatility

        // Check for lower wick bypass
        boolean isLowerWickSignificant =
                lowerWick
                        > lowerWickThresholdHigh; // Initially assuming High Volatility for default

        // Determine volatility based on ATR percent
        if (atrPercent > 5) {
            // High Volatility
            isLowerWickSignificant = lowerWick > lowerWickThresholdHigh && lowerWick > upperWick;
        } else if (atrPercent > 1.5 && atrPercent <= 5) {
            // Moderate Volatility
            isLowerWickSignificant =
                    lowerWick > lowerWickThresholdModerate && lowerWick > upperWick;
        } else {
            // Low Volatility
            isLowerWickSignificant = lowerWick > lowerWickThresholdLow && lowerWick > upperWick;
        }

        switch (timeframe) {
            case DAILY:
                // Adjust for daily volatility
                if (atrPercent > 5) { // High Volatility
                    return (upperWick <= upperWickThresholdDailyHigh || isLowerWickSignificant);
                } else if (atrPercent > 1.5 && atrPercent <= 5) { // Moderate Volatility
                    return (upperWick <= upperWickThresholdDailyModerate || isLowerWickSignificant);
                } else { // Low Volatility
                    return (upperWick <= upperWickThresholdDailyLow || isLowerWickSignificant);
                }

            case WEEKLY:
                // Adjust for weekly volatility
                if (atrPercent > 5) { // High Volatility
                    return (upperWick <= upperWickThresholdWeeklyHigh || isLowerWickSignificant);
                } else if (atrPercent > 1.5 && atrPercent <= 5) { // Moderate Volatility
                    return (upperWick <= upperWickThresholdWeeklyModerate
                            || isLowerWickSignificant);
                } else { // Low Volatility
                    return (upperWick <= upperWickThresholdWeeklyLow || isLowerWickSignificant);
                }

            case MONTHLY:
                // Only check upper wick size for monthly
                return (upperWick <= upperWickThresholdMonthly || isLowerWickSignificant);

            default:
                return false;
        }
    }

    @Override
    public boolean isLowerWickSizeConfirmed(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double upperWick =
                CandleStickUtils.upperWickSize(stockPrice); // For checking upper wick condition

        double latestATR = stockTechnicals.getAtr();
        double latestClose = stockPrice.getClose();

        // Prevent division by zero
        double atrPercent = (latestClose > 0) ? (latestATR / latestClose) * 100 : 0;

        // Define thresholds based on volatility
        double lowerWickThresholdDailyHigh = 0.20 * bodySize; // 20% for High Volatility
        double lowerWickThresholdDailyModerate = 0.15 * bodySize; // 15% for Moderate Volatility
        double lowerWickThresholdDailyLow = 0.10 * bodySize; // 10% for Low Volatility

        double lowerWickThresholdWeeklyHigh = 0.15 * bodySize; // 15% for High Volatility
        double lowerWickThresholdWeeklyModerate = 0.10 * bodySize; // 10% for Moderate Volatility
        double lowerWickThresholdWeeklyLow = 0.05 * bodySize; // 5% for Low Volatility

        double lowerWickThresholdMonthly = 0.10 * bodySize; // 10% for Monthly timeframe

        // Define upper wick size thresholds (for bypass condition if upper wick is too long)
        double upperWickThresholdHigh = 0.70 * bodySize; // 70% for High Volatility
        double upperWickThresholdModerate = 0.60 * bodySize; // 60% for Moderate Volatility
        double upperWickThresholdLow = 0.50 * bodySize; // 50% for Low Volatility

        // Check for upper wick bypass
        boolean isUpperWickSignificant =
                upperWick
                        > upperWickThresholdHigh; // Initially assuming High Volatility for default

        // Determine volatility based on ATR percent
        if (atrPercent > 5) {
            // High Volatility
            isUpperWickSignificant = upperWick > upperWickThresholdHigh && upperWick > lowerWick;
        } else if (atrPercent > 1.5 && atrPercent <= 5) {
            // Moderate Volatility
            isUpperWickSignificant =
                    upperWick > upperWickThresholdModerate && upperWick > lowerWick;
        } else {
            // Low Volatility
            isUpperWickSignificant = upperWick > upperWickThresholdLow && upperWick > lowerWick;
        }

        // Now applying the rules for each timeframe based on volatility
        switch (timeframe) {
            case DAILY:
                // Adjust for daily volatility
                if (atrPercent > 5) { // High Volatility
                    return (lowerWick <= lowerWickThresholdDailyHigh || isUpperWickSignificant);
                } else if (atrPercent > 1.5 && atrPercent <= 5) { // Moderate Volatility
                    return (lowerWick <= lowerWickThresholdDailyModerate || isUpperWickSignificant);
                } else { // Low Volatility
                    return (lowerWick <= lowerWickThresholdDailyLow || isUpperWickSignificant);
                }

            case WEEKLY:
                // Adjust for weekly volatility
                if (atrPercent > 5) { // High Volatility
                    return (lowerWick <= lowerWickThresholdWeeklyHigh || isUpperWickSignificant);
                } else if (atrPercent > 1.5 && atrPercent <= 5) { // Moderate Volatility
                    return (lowerWick <= lowerWickThresholdWeeklyModerate
                            || isUpperWickSignificant);
                } else { // Low Volatility
                    return (lowerWick <= lowerWickThresholdWeeklyLow || isUpperWickSignificant);
                }

            case MONTHLY:
                // Only check lower wick size for monthly
                return (lowerWick <= lowerWickThresholdMonthly || isUpperWickSignificant);

            default:
                return false;
        }
    }

    @Override
    public boolean isBullishConfirmed(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {

        if (this.isThreeSessionBullish(timeframe, stockPrice, stockTechnicals, skipVolumeCheck)
                || this.isTwoSessionBullish(timeframe, stockPrice, stockTechnicals, skipVolumeCheck)
                || this.isSingleSessionBullish(
                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck)) {
            // if (this.isUpperWickSizeConfirmed(timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "{} bullish candlestick active on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());
            return Boolean.TRUE;
            // }
            /*
            log.info(
                    "{} bullish candlestick found but upper wick size not confirmed {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());*/
        }
        return Boolean.FALSE;
    }

    private boolean isThreeSessionBullish(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {

        boolean isBullishPattern =
                // 1️⃣ Strongest Bullish Reversal Pattern
                threeSessionCandleStickService.isMorningStar(timeframe, stockPrice, stockTechnicals)
                        || threeSessionCandleStickService.isThreeCandleTweezerBottom(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 2️⃣ Strong Bullish Reversal Pattern
                        threeSessionCandleStickService.isThreeWhiteSoldiers(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 3️⃣ Bullish Continuation Pattern
                        threeSessionCandleStickService.isThreeOutsideUp(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 4️⃣ Weaker Bullish Reversal Pattern
                        (threeSessionCandleStickService.isThreeInsideUp(
                                        timeframe, stockPrice, stockTechnicals)
                                && this.isSingleSessionBullish(
                                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck));

        if (!isBullishPattern) {
            return false;
        }

        boolean isVolumeConfirmed =
                skipVolumeCheck
                        || volumeIndicatorService.isBullish(
                                timeframe, stockPrice, stockTechnicals, 3);

        if (isVolumeConfirmed) {
            log.info(
                    "{} Bullish 3 session pattern confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        } else {
            log.info(
                    "{} Bullish 3 session pattern detected but NOT confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        }

        return isVolumeConfirmed;
    }

    private boolean isThreeSessionBearish(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {

        boolean isBearishPattern =
                // 1️⃣ Strongest Bearish Reversal Patterns
                threeSessionCandleStickService.isEveningStar(timeframe, stockPrice, stockTechnicals)
                        ||

                        // 2️⃣ Strong Bearish Reversal Patterns
                        threeSessionCandleStickService.isThreeBlackCrows(
                                timeframe, stockPrice, stockTechnicals)
                        || threeSessionCandleStickService.isThreeCandleTweezerTop(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 3️⃣ Bearish Continuation Patterns
                        threeSessionCandleStickService.isThreeOutsideDown(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 4️⃣ Weaker Bearish Reversal Patterns
                        (threeSessionCandleStickService.isThreeInsideDown(
                                        timeframe, stockPrice, stockTechnicals)
                                && this.isSingleSessionBearish(
                                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck));

        if (!isBearishPattern) {
            return false;
        }

        boolean isVolumeConfirmed =
                skipVolumeCheck
                        || volumeIndicatorService.isBearish(
                                timeframe, stockPrice, stockTechnicals, 3);

        if (isVolumeConfirmed) {
            log.info(
                    "{} Bearish 3 session pattern confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        } else {
            log.info(
                    "{} Bearish 3 session pattern detected but NOT confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        }

        return isVolumeConfirmed;
    }

    private boolean isTwoSessionBullish(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {

        boolean isBullishPattern =
                // 1Strongest Reversal Patterns
                twoSessionCandleStickService.isBullishKicker(timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isBullishEngulfing(
                                timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isPiercingPattern(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // Strong Trend Continuation & Reversal Patterns
                        twoSessionCandleStickService.isTweezerBottom(
                                timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isBullishOutsideBar(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // Medium Strength Reversal/Continuation Patterns
                        twoSessionCandleStickService.isBullishSash(
                                timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isBullishSeparatingLine(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // Weak
                        (twoSessionCandleStickService.isBullishHarami(
                                        timeframe, stockPrice, stockTechnicals)
                                && this.isSingleSessionBullish(
                                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck))
                        || (twoSessionCandleStickService.isBullishInsideBar(
                                        timeframe, stockPrice, stockTechnicals)
                                && this.isSingleSessionBullish(
                                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck));

        if (!isBullishPattern) {
            return false;
        }

        boolean isVolumeConfirmed =
                skipVolumeCheck
                        || volumeIndicatorService.isBullish(
                                timeframe, stockPrice, stockTechnicals, 2);

        if (isVolumeConfirmed) {
            log.info(
                    "{} Bullish 2 session pattern confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        } else {
            log.info(
                    "{} Bullish 2 session pattern detected but NOT confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        }

        return isVolumeConfirmed;
    }

    private boolean isTwoSessionBearish(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {

        boolean isBearishPattern =
                // Strongest Reversal Patterns
                twoSessionCandleStickService.isBearishKicker(timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isBearishEngulfing(
                                timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isDarkCloudCover(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // ️ Strong Trend Continuation & Reversal Patterns
                        twoSessionCandleStickService.isTweezerTop(
                                timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isBearishOutsideBar(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // Medium Strength Reversal/Continuation Patterns
                        twoSessionCandleStickService.isBearishSash(
                                timeframe, stockPrice, stockTechnicals)
                        || twoSessionCandleStickService.isBearishSeparatingLine(
                                timeframe, stockPrice, stockTechnicals)
                        || (twoSessionCandleStickService.isBearishHarami(
                                        timeframe, stockPrice, stockTechnicals)
                                && this.isSingleSessionBearish(
                                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck))
                        || (twoSessionCandleStickService.isBearishInsideBar(
                                        timeframe, stockPrice, stockTechnicals)
                                && this.isSingleSessionBearish(
                                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck));

        if (!isBearishPattern) {
            return false;
        }

        boolean isVolumeConfirmed =
                skipVolumeCheck
                        || volumeIndicatorService.isBearish(
                                timeframe, stockPrice, stockTechnicals, 2);

        if (isVolumeConfirmed) {
            log.info(
                    "{} Bearish 2 session pattern confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        } else {
            log.info(
                    "{} Bearish 2 session pattern detected but NOT confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        }

        return isVolumeConfirmed;
    }

    private boolean isSingleSessionBullish(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {

        boolean isBullishPattern =
                // 1️⃣ Strongest Bullish Reversal Patterns
                singleSessionCandleStickService.isBullishMarubozu(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 2️⃣ Strong Bullish Reversal Patterns
                        singleSessionCandleStickService.isHammer(
                                timeframe, stockPrice, stockTechnicals)
                        || singleSessionCandleStickService.isBullishPinBar(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 3️⃣ Weaker Bullish Reversal Patterns
                        singleSessionCandleStickService.isOpenLow(
                                timeframe, stockPrice, stockTechnicals);

        if (!isBullishPattern) {
            return false;
        }

        boolean isVolumeConfirmed =
                skipVolumeCheck
                        || volumeIndicatorService.isBullish(
                                timeframe, stockPrice, stockTechnicals, 1);

        if (isVolumeConfirmed) {
            log.info(
                    "{} Bullish 1 session pattern confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        } else {
            log.info(
                    "{} Bullish 1 session pattern detected but NOT confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        }

        return isVolumeConfirmed;
    }

    private boolean isSingleSessionBearish(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {

        boolean isBearishPattern =
                // 1️⃣ Strongest Bearish Reversal Patterns
                singleSessionCandleStickService.isBearishMarubozu(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 2️⃣ Strong Bearish Reversal Patterns
                        singleSessionCandleStickService.isShootingStar(
                                timeframe, stockPrice, stockTechnicals)
                        || singleSessionCandleStickService.isBearishPinBar(
                                timeframe, stockPrice, stockTechnicals)
                        ||

                        // 3️⃣ Weaker Bearish Reversal Patterns
                        singleSessionCandleStickService.isOpenHigh(
                                timeframe, stockPrice, stockTechnicals);

        if (!isBearishPattern) {
            return false;
        }

        boolean isVolumeConfirmed =
                skipVolumeCheck
                        || volumeIndicatorService.isBearish(
                                timeframe, stockPrice, stockTechnicals, 1);

        if (isVolumeConfirmed) {
            log.info(
                    "{} Bearish 1 session pattern confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        } else {
            log.info(
                    "{} Bearish 1 session pattern detected but NOT confirmed by volume",
                    stockPrice.getStock().getNseSymbol());
        }

        return isVolumeConfirmed;
    }

    @Override
    public boolean isBearishConfirmed(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean skipVolumeCheck) {
        // isWickCheck = timeframe == Timeframe.DAILY ? true : false;

        if (this.isThreeSessionBearish(timeframe, stockPrice, stockTechnicals, skipVolumeCheck)
                || this.isTwoSessionBearish(timeframe, stockPrice, stockTechnicals, skipVolumeCheck)
                || this.isSingleSessionBearish(
                        timeframe, stockPrice, stockTechnicals, skipVolumeCheck)) {

            // if (this.isLowerWickSizeConfirmed(timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "{} bearish candlestick active on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());
            return Boolean.TRUE;
            // }
            /*
            log.info(
                    "{} bearish candlestick found but lower wick size not confirmed {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());
             */
        }
        return Boolean.FALSE;
    }
}
