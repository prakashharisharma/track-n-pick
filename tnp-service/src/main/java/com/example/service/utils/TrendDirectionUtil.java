package com.example.service.utils;

import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;

public class TrendDirectionUtil {

    public static Trend.Direction findDirection(StockPrice stockPrice) {
        double[] opens = {
            stockPrice.getPrev4Open(),
            stockPrice.getPrev3Open(),
            stockPrice.getPrev2Open(),
            stockPrice.getPrevOpen(),
            stockPrice.getOpen()
        };
        double[] highs = {
            stockPrice.getPrev4High(),
            stockPrice.getPrev3High(),
            stockPrice.getPrev2High(),
            stockPrice.getPrevHigh(),
            stockPrice.getHigh()
        };
        double[] lows = {
            stockPrice.getPrev4Low(),
            stockPrice.getPrev3Low(),
            stockPrice.getPrev2Low(),
            stockPrice.getPrevLow(),
            stockPrice.getLow()
        };
        double[] closes = {
            stockPrice.getPrev4Close(),
            stockPrice.getPrev3Close(),
            stockPrice.getPrev2Close(),
            stockPrice.getPrevClose(),
            stockPrice.getClose()
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

        if (downScore >= 6 && downScore > upScore) {
            return Trend.Direction.DOWN;
        } else if (upScore >= 6 && upScore > downScore) {
            return Trend.Direction.UP;
        } else {
            // Tie-breaker logic
            boolean isCurrentRed = CandleStickUtils.isRed(stockPrice);
            if (isCurrentRed) {
                // Compare current day with previous day only
                boolean lowerHigh = highs[4] < highs[3];
                boolean lowerLow = lows[4] < lows[3];
                if (lowerHigh && lowerLow) return Trend.Direction.DOWN;

                boolean higherHigh = highs[4] > highs[3];
                boolean higherLow = lows[4] > lows[3];
                if (higherHigh && higherLow) return Trend.Direction.UP;
            } else {
                // Current day is green - compare current with previous two days
                boolean lowerHigh = (highs[4] < highs[3]) && (highs[3] < highs[2]);
                boolean lowerLow = (lows[4] < lows[3]) && (lows[3] < lows[2]);
                if (lowerHigh && lowerLow) return Trend.Direction.DOWN;

                boolean higherHigh = (highs[4] > highs[3]) && (highs[3] > highs[2]);
                boolean higherLow = (lows[4] > lows[3]) && (lows[3] > lows[2]);
                if (higherHigh && higherLow) return Trend.Direction.UP;
            }
            return Trend.Direction.INVALID;
        }
    }
}
