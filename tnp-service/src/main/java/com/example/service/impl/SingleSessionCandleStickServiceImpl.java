package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.CandlestickPattern;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.CandlestickPatternService;
import com.example.service.SingleSessionCandleStickService;
import com.example.service.utils.CandleStickUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleSessionCandleStickServiceImpl implements SingleSessionCandleStickService {

    private final CandlestickPatternService candlestickPatternService;

    @Override
    public boolean isBullishMarubozu(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Check for a strong bullish body
        boolean strongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isGreen = CandleStickUtils.isGreen(stockPrice);

        // Check for small wicks
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);

        boolean smallWicks = upperWick <= 0.05 * totalRange && lowerWick <= 0.05 * totalRange;

        if (isGreen && strongBody && smallWicks) {
            log.info(
                    "{}: Bullish Marubozu detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build CandlestickPattern entity
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.MARUBOZU)
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(strongBody)
                            .isSmallBody(false) // Set accordingly, or create another util if needed
                            .isGapUp(false) // Set logic if you want to detect gap ups
                            .isGapDown(false) // Set logic if you want to detect gap downs
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(totalRange)
                            .lowerWickSize(lowerWick)
                            .upperWickSize(upperWick)
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isOpenLow(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean strongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean openEqualsLow = CandleStickUtils.isOpenAndLowEqual(stockPrice);

        if (strongBody && openEqualsLow) {
            log.info(
                    "{}: Open Low candle detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(
                                    CandlestickPattern.Name
                                            .OPEN_LOW) // Add this enum if it doesn't exist
                            .sentiment(
                                    CandlestickPattern.Sentiment
                                            .BEARISH) // or BULLISH depending on your logic
                            .isStrongBody(strongBody)
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isHammer(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, false, false); // Checks lower wick

        if (result) {
            log.info(
                    "{}: Hammer detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build CandlestickPattern entity
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.HAMMER)
                            .sentiment(CandlestickPattern.Sentiment.BULLISH) // Usually bullish
                            .isStrongBody(false) // Hammer usually has small body
                            .isSmallBody(CandleStickUtils.isSmallBody(stockPrice, stockTechnicals))
                            .isGapUp(false) // Set your logic if needed
                            .isGapDown(false) // Set your logic if needed
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return result;
    }

    @Override
    public boolean isBullishPinBar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, false, true); // Checks lower wick

        if (result) {
            log.info(
                    "{}: Bullish Pin Bar detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build and save CandlestickPattern entity
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(
                                    CandlestickPattern.Name
                                            .PIN_BAR) // You need to add this in your enum Name
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(
                                    false) // set according to your logic or utils if available
                            .isSmallBody(false)
                            .isGapUp(false) // Set your logic here if you want to detect gap ups
                            .isGapDown(false) // Set your logic here if you want to detect gap downs
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isInvertedHammer(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, true, false); // Checks upper wick

        if (result) {
            log.info(
                    "{}: Inverted Hammer detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.INVERTED_HAMMER)
                            .sentiment(
                                    CandlestickPattern.Sentiment.BULLISH) // usually bullish pattern
                            .isStrongBody(false) // set based on your logic if needed
                            .isSmallBody(false) // set accordingly or use utility if available
                            .isGapUp(false) // set if you want to detect gap ups
                            .isGapDown(false) // set if you want to detect gap downs
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }
        return result;
    }

    @Override
    public boolean isDoji(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);

        boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice);

        if (isDoji) {
            log.info(
                    "{}: Doji detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.DOJI)
                            .sentiment(CandlestickPattern.Sentiment.NEUTRAL)
                            .isStrongBody(false)
                            .isSmallBody(true) // small body by definition
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(
                                    stockTechnicals != null && stockTechnicals.getAtr() != null
                                            ? stockTechnicals.getAtr()
                                            : 0.0)
                            .bodySize(bodySize)
                            .rangeSize(totalRange)
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isDoji;
    }

    @Override
    public boolean isGravestoneDoji(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        // Conditions for Gravestone Doji
        boolean verySmallBody = CandleStickUtils.isVerySmallBody(stockPrice); // ≤ 5% of range
        boolean longUpperWick = upperWick >= 0.85 * totalRange; // ≥ 60% of range
        boolean tinyOrNoLowerWick = lowerWick <= 0.05 * totalRange; // ≤ 5% of range

        if (verySmallBody && longUpperWick && tinyOrNoLowerWick) {
            log.info(
                    "{}: Gravestone Doji detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build and save CandlestickPattern entity
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.DOJI_GRAVESTONE) // Or create new enum for
                            // GRAVESTONE_DOJI if you want
                            // specific
                            .sentiment(
                                    CandlestickPattern.Sentiment
                                            .BEARISH) // Gravestone Doji is typically bearish
                            .isStrongBody(false)
                            .isSmallBody(true)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(bodySize)
                            .rangeSize(totalRange)
                            .lowerWickSize(lowerWick)
                            .upperWickSize(upperWick)
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isDragonflyDoji(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        // Conditions for Dragonfly Doji
        boolean verySmallBody = CandleStickUtils.isVerySmallBody(stockPrice); // Body ≤ 1% of range
        boolean longLowerWick = lowerWick >= 0.85 * totalRange; // Lower wick ≥ 60% of range
        boolean tinyOrNoUpperWick = upperWick <= 0.05 * totalRange; // Upper wick ≤ 5% of range

        if (verySmallBody && longLowerWick && tinyOrNoUpperWick) {
            log.info(
                    "{}: Dragonfly Doji detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(
                                    CandlestickPattern.Name
                                            .DOJI) // Or add DRAGONFLY_DOJI enum if preferred
                            .sentiment(
                                    CandlestickPattern.Sentiment
                                            .BULLISH) // Dragonfly Doji is bullish
                            .isStrongBody(false)
                            .isSmallBody(true)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(bodySize)
                            .rangeSize(totalRange)
                            .lowerWickSize(lowerWick)
                            .upperWickSize(upperWick)
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isSpinningTop(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        // Conditions for Spinning Top
        boolean smallBody =
                CandleStickUtils.isSmallBody(
                        stockPrice, stockTechnicals); // e.g. body ≤ 30% of range
        boolean longUpperWick = upperWick >= 0.3 * totalRange; // Upper wick ≥ 30% of range
        boolean longLowerWick = lowerWick >= 0.3 * totalRange; // Lower wick ≥ 30% of range

        if (smallBody && longUpperWick && longLowerWick) {
            log.info(
                    "{}: Spinning Top detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.SPINNING_TOP)
                            .sentiment(CandlestickPattern.Sentiment.NEUTRAL) // Typically neutral
                            .isStrongBody(false)
                            .isSmallBody(true)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(bodySize)
                            .rangeSize(totalRange)
                            .lowerWickSize(lowerWick)
                            .upperWickSize(upperWick)
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isBearishMarubozu(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Check for a strong bearish body
        boolean strongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isRed = CandleStickUtils.isRed(stockPrice);

        // Check for small wicks
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);

        boolean smallWicks = upperWick <= 0.05 * totalRange && lowerWick <= 0.05 * totalRange;

        if (isRed && strongBody && smallWicks) {
            log.info(
                    "{}: Bearish Marubozu detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.MARUBOZU)
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(strongBody)
                            .isSmallBody(false) // adjust if needed
                            .isGapUp(false) // logic can be added if desired
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(totalRange)
                            .lowerWickSize(lowerWick)
                            .upperWickSize(upperWick)
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isOpenHigh(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean strongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isOpenHigh = stockPrice.getOpen().equals(stockPrice.getHigh());

        if (isOpenHigh && strongBody) {
            log.info(
                    "{}: Open High candle detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.OPEN_HIGH) // define this enum value
                            .sentiment(
                                    CandlestickPattern.Sentiment.NEUTRAL) // or whichever fits best
                            .isStrongBody(strongBody)
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isBearishPinBar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        boolean isRedCandle = CandleStickUtils.isRed(stockPrice);

        // Avoid division issues for very small range candles
        if (totalRange == 0) {
            return false;
        }

        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, true, true); // Checks upper wick

        if (result) {
            log.info(
                    "{}: Bearish Pin Bar detected on {} [Open: {}, High: {}, Low: {}, Close: {}]",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate(),
                    stockPrice.getOpen(),
                    stockPrice.getHigh(),
                    stockPrice.getLow(),
                    stockPrice.getClose());

            // Build CandlestickPattern entity
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.PIN_BAR)
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(false) // Typically Pin Bars have small body
                            .isSmallBody(true) // Set small body true
                            .isGapUp(false) // Implement gap logic if desired
                            .isGapDown(false) // Implement gap logic if desired
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(bodySize)
                            .rangeSize(totalRange)
                            .lowerWickSize(lowerWick)
                            .upperWickSize(upperWick)
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isShootingStar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, true, false); // Checks upper wick
        if (result) {
            log.info(
                    "{}: Shooting Star detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build CandlestickPattern entity
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(
                                    CandlestickPattern.Name
                                            .SHOOTING_STAR) // assuming enum value exists
                            .sentiment(
                                    CandlestickPattern.Sentiment
                                            .BEARISH) // Shooting star is bearish reversal
                            .isStrongBody(false) // Typically small body
                            .isSmallBody(true)
                            .isGapUp(false) // Set if gap logic is implemented
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }
        return result;
    }

    @Override
    public boolean isHangingMan(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Hanging Man usually has a long lower wick, so wickDominantCandle with false means
        // checking lower wick
        boolean result =
                CandleStickUtils.isWickDominantCandle(stockPrice, stockTechnicals, false, false);
        if (result) {
            log.info(
                    "{}: Hanging Man detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.SINGLE)
                            .name(CandlestickPattern.Name.HANGING_MAN) // Ensure this enum exists
                            .sentiment(
                                    CandlestickPattern.Sentiment
                                            .BEARISH) // Hanging Man is a bearish reversal pattern
                            .isStrongBody(false) // Typically small or moderate body
                            .isSmallBody(true)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }
        return result;
    }
}
