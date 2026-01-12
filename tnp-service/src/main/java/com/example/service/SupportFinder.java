package com.example.service;

import com.example.data.transactional.entities.StockPrice;
import java.util.ArrayList;
import java.util.List;

public class SupportFinder {

    public static double findSupport(StockPrice sp, int lookback) {

        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i <= lookback; i++) {
            double low = sp.getLow(i);
            double open = sp.getOpen(i);
            double close = sp.getClose(i);
            if (low > 0) {
                candles.add(new Candle(i, low, open, close));
            }
        }

        int n = candles.size();
        if (n < 2) return 0.0;

        Candle mostRecent = candles.get(0);

        for (int i = 1; i < n; i++) {
            Candle candidate = candles.get(i);

            // Candidate must be below or near recent price
            if (candidate.low > mostRecent.close) continue;

            double supportLevel = Math.max(candidate.low, mostRecent.low);

            if (Math.min(candidate.close, candidate.open) < supportLevel) continue;

            boolean valid = true;

            // No candle should CLOSE below support
            for (int k = i - 1; k >= 0; k--) {
                Candle between = candles.get(k);
                if (Math.min(between.close, between.open) < supportLevel) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                System.out.println("Support found:");
                System.out.println(
                        "Most recent candle -> index: "
                                + mostRecent.index
                                + ", low: "
                                + mostRecent.low
                                + ", close: "
                                + mostRecent.close);
                System.out.println(
                        "Candidate base candle -> index: "
                                + candidate.index
                                + ", low: "
                                + candidate.low
                                + ", close: "
                                + candidate.close);
                System.out.println("Support level: " + supportLevel);

                return supportLevel;
            }
        }

        return 0.0;
    }

    private static final class Candle {
        final int index;
        final double low;
        final double open;
        final double close;

        Candle(int index, double low, double open, double close) {
            this.index = index;
            this.low = low;
            this.open = open;
            this.close = close;
        }
    }
}
