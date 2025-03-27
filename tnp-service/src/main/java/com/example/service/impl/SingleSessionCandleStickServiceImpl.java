package com.example.service.impl;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.service.SingleSessionCandleStickService;
import com.example.service.utils.CandleStickUtils;
import com.example.util.io.model.type.Timeframe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleSessionCandleStickServiceImpl implements SingleSessionCandleStickService {


    @Override
    public boolean isBullishMarubozu(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
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
            log.info("{}: Bullish Marubozu detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isOpenLow(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean strongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean openEqualsLow = CandleStickUtils.isOpenAndLowEqual(stockPrice);

        if (strongBody && openEqualsLow) {
            log.info("{}: Open Low candle detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isHammer(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean result = CandleStickUtils.isWickDominantCandle(stockPrice, false); // Checks lower wick
        if (result) {
            log.info("{}: Hammer detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }
        return result;
    }

    @Override
    public boolean isBullishPinBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);

        // Conditions for Bullish Pin Bar
        boolean smallBody = bodySize <= 0.35 * totalRange;  // Body ≤ 35% of range
        boolean longLowerWick = lowerWick >= 2 * bodySize;  // Lower wick ≥ 2x body
        boolean smallUpperWick = upperWick <= 0.1 * totalRange;  // Upper wick ≤ 10% of range
        boolean isGreen = CandleStickUtils.isGreen(stockPrice);  // Preferably a green candle

        if (smallBody && longLowerWick && smallUpperWick && isGreen) {
            log.info("{}: Bullish Pin Bar detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isInvertedHammer(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean result = CandleStickUtils.isWickDominantCandle(stockPrice, true); // Checks upper wick
        if (result) {
            log.info("{}: Inverted Hammer detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }
        return result;
    }

    @Override
    public boolean isDoji(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);

        return bodySize <= 0.1 * totalRange; // Body is ≤ 10% of range
    }

    @Override
    public boolean isGravestoneDoji(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        // Conditions for Gravestone Doji
        boolean verySmallBody = bodySize <= 0.05 * totalRange; // Body is ≤ 5% of range
        boolean longUpperWick = upperWick >= 0.6 * totalRange; // Upper wick is ≥ 60% of range
        boolean tinyOrNoLowerWick = lowerWick <= 0.05 * totalRange; // Lower wick is ≤ 5% of range

        if (verySmallBody && longUpperWick && tinyOrNoLowerWick) {
            log.info("{}: Gravestone Doji detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isDragonflyDoji(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        // Conditions for Dragonfly Doji
        boolean verySmallBody = bodySize <= 0.05 * totalRange; // Body is ≤ 5% of range
        boolean longLowerWick = lowerWick >= 0.6 * totalRange; // Lower wick is ≥ 60% of range
        boolean tinyOrNoUpperWick = upperWick <= 0.05 * totalRange; // Upper wick is ≤ 5% of range

        if (verySmallBody && longLowerWick && tinyOrNoUpperWick) {
            log.info("{}: Dragonfly Doji detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isSpinningTop(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double totalRange = CandleStickUtils.range(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        // Conditions for Spinning Top
        boolean smallBody = bodySize <= 0.3 * totalRange; // Body ≤ 30% of range
        boolean longUpperWick = upperWick >= 0.3 * totalRange; // Upper wick ≥ 30% of range
        boolean longLowerWick = lowerWick >= 0.3 * totalRange; // Lower wick ≥ 30% of range

        if (smallBody && longUpperWick && longLowerWick) {
            log.info("{}: Spinning Top detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }



    @Override
    public boolean isBearishMarubozu(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
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
            log.info("{}: Bearish Marubozu detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isOpenHigh(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean strongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isOpenHigh = stockPrice.getOpen().equals(stockPrice.getHigh());

        if (isOpenHigh && strongBody) {
            log.info("{}: Open High candle detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isBearishPinBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
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

        // Configurable threshold values
        double bodyThreshold = 0.35;  // Body should be <= 35% of total range
        double lowerWickThreshold = 0.1; // Lower wick should be <= 10% of total range
        double upperWickMultiplier = 2.0; // Upper wick should be >= 2x body

        // Conditions for Bearish Pin Bar
        boolean smallBody = bodySize <= bodyThreshold * totalRange;
        boolean longUpperWick = upperWick >= upperWickMultiplier * bodySize && upperWick >= 0.6 * totalRange;
        boolean smallLowerWick = lowerWick <= lowerWickThreshold * totalRange;

        if (smallBody && longUpperWick && smallLowerWick && isRedCandle) {
            log.info("{}: Bearish Pin Bar detected on {} [Open: {}, High: {}, Low: {}, Close: {}]",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate(),
                    stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose());
            return true;
        }

        return false;
    }

    @Override
    public boolean isShootingStar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean result = CandleStickUtils.isWickDominantCandle(stockPrice, true); // Checks upper wick
        if (result) {
            log.info("{}: Shooting Star detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }
        return result;
    }

    @Override
    public boolean isHangingMan(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean result = CandleStickUtils.isWickDominantCandle(stockPrice, false); // Checks upper wick
        if (result) {
            log.info("{}: Hanging Man detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }
        return result;
    }
}
