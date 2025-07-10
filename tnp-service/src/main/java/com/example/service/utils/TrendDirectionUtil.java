package com.example.service.utils;

import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;

public class TrendDirectionUtil {

    public static Trend.Direction findDirection(StockPrice stockPrice) {
        // Step 1: Check for gap up/down first
        if (CandleStickUtils.isFallingWindow(stockPrice)) return Trend.Direction.DOWN;
        if (CandleStickUtils.isRisingWindow(stockPrice)) return Trend.Direction.UP;

        // Step 2: Score previous 5 sessions
        double[] highs = {
            stockPrice.getPrev5High(),
            stockPrice.getPrev4High(),
            stockPrice.getPrev3High(),
            stockPrice.getPrev2High(),
            stockPrice.getPrevHigh()
        };
        double[] lows = {
            stockPrice.getPrev5Low(),
            stockPrice.getPrev4Low(),
            stockPrice.getPrev3Low(),
            stockPrice.getPrev2Low(),
            stockPrice.getPrevLow()
        };
        double[] closes = {
            stockPrice.getPrev5Close(),
            stockPrice.getPrev4Close(),
            stockPrice.getPrev3Close(),
            stockPrice.getPrev2Close(),
            stockPrice.getPrevClose()
        };

        int upScore = 0, downScore = 0;

        for (int i = 1; i < 5; i++) {
            if (highs[i] > highs[i - 1]) upScore++;
            else if (highs[i] < highs[i - 1]) downScore++;
            if (lows[i] > lows[i - 1]) upScore++;
            else if (lows[i] < lows[i - 1]) downScore++;
            if (closes[i] > closes[i - 1]) upScore++;
            else if (closes[i] < closes[i - 1]) downScore++;
        }

        boolean prevTrendDown = downScore >= 6 && downScore > upScore;
        boolean prevTrendUp = upScore >= 6 && upScore > downScore;

        double currentHigh = stockPrice.getHigh();
        double currentLow = stockPrice.getLow();
        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();

        boolean currentHHHL = currentHigh > prevHigh && currentLow > prevLow;
        boolean currentLHLL = currentHigh < prevHigh && currentLow < prevLow;

        // Step 3 & 4: Check for reversal from 5-session trend based on current candle
        if (prevTrendDown && currentHHHL) return Trend.Direction.UP;
        if (prevTrendUp && currentLHLL) return Trend.Direction.DOWN;

        // Step 5: Fallback to previous 5-session direction
        if (prevTrendDown) return Trend.Direction.DOWN;
        if (prevTrendUp) return Trend.Direction.UP;

        // Final tie-breaker using last two sessions
        boolean lowerHigh = highs[4] < highs[3];
        boolean lowerLow = lows[4] < lows[3];
        boolean higherHigh = highs[4] > highs[3];
        boolean higherLow = lows[4] > lows[3];

        if (lowerHigh && lowerLow) return Trend.Direction.DOWN;
        if (higherHigh && higherLow) return Trend.Direction.UP;

        return Trend.Direction.INVALID;
    }
}
