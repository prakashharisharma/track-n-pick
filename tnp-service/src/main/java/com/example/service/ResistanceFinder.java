package com.example.service;

import com.example.data.transactional.entities.StockPrice;
import java.util.ArrayList;
import java.util.List;

public class ResistanceFinder {

    public static double findResistance(StockPrice sp, int lookback) {
        // Collect candles from newest (0) → oldest
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i <= lookback; i++) {
            double high = sp.getHigh(i);
            double open = sp.getOpen(i);
            double close = sp.getClose(i);
            if (high > 0) {
                candles.add(new Candle(i, high, open, close));
            }
        }

        int n = candles.size();
        if (n < 2) return 0.0;

        Candle mostRecent = candles.get(0);

        // Scan candidates from 1 → n-1 (older candles)
        for (int i = 1; i < n; i++) {
            Candle candidate = candles.get(i);

            // Candidate must be higher than most recent candle to be considered
            if (candidate.high < mostRecent.close) continue;

            boolean valid = true;

            // Check all closes between candidate and most recent (exclusive candidate)
            for (int k = i - 1; k >= 0; k--) {
                Candle between = candles.get(k);
                if (between.close > Math.min(candidate.high, mostRecent.high)) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                double resistance = Math.min(candidate.high, mostRecent.high);
                System.out.println("Resistance found:");
                System.out.println(
                        "Most recent candle -> index: "
                                + mostRecent.index
                                + ", high: "
                                + mostRecent.high
                                + ", close: "
                                + mostRecent.close);
                System.out.println(
                        "Candidate lower high candle -> index: "
                                + candidate.index
                                + ", high: "
                                + candidate.high
                                + ", close: "
                                + candidate.close);
                return resistance;
            }
        }

        // No valid resistance found
        return 0.0;
    }

    private static final class Candle {
        final int index;
        final double high;
        final double open;
        final double close;

        Candle(int index, double high, double open, double close) {
            this.index = index;
            this.high = high;
            this.open = open;
            this.close = close;
        }
    }
}
