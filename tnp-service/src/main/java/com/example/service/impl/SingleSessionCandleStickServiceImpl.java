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
        boolean strongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean isGreen = CandleStickUtils.isGreen(stockPrice);

        // Check for small wicks
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);

        boolean smallWicks = upperWick <= 0.05 * totalRange && lowerWick <= 0.05 * totalRange;

        if (isGreen && strongRange && smallWicks) {
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
                                            .BULLISH) // or BULLISH depending on your logic
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

        boolean strongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);

        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, false, false); // Checks lower wick

        if (result && strongRange) {
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
        boolean strongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, false, true); // Checks lower wick

        if (result && strongRange) {
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
        boolean strongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, true, false); // Checks upper wick

        if (result && strongRange) {
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
        boolean strongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean isRed = CandleStickUtils.isRed(stockPrice);

        // Check for small wicks
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);

        boolean smallWicks = upperWick <= 0.05 * totalRange && lowerWick <= 0.05 * totalRange;

        if (isRed && strongRange && smallWicks) {
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
                                    CandlestickPattern.Sentiment.BEARISH) // or whichever fits best
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

        double totalRange = CandleStickUtils.range(stockPrice);

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
        boolean strongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean result =
                CandleStickUtils.isWickDominantCandle(
                        stockPrice, stockTechnicals, true, false); // Checks upper wick
        if (result && strongRange) {
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
        boolean strongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        // checking lower wick
        boolean result =
                CandleStickUtils.isWickDominantCandle(stockPrice, stockTechnicals, false, false);
        if (result && strongRange) {
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
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }
        return result;
    }
}
