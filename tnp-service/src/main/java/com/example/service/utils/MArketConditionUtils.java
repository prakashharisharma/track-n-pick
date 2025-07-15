package com.example.service.utils;

import com.example.data.transactional.entities.StockPrice;
import java.util.ArrayList;
import java.util.List;

public class MArketConditionUtils {

    public static class OHLC {
        public final double open, high, low, close;

        public OHLC(double open, double high, double low, double close) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }
    }

    public enum MarketCondition {
        UPTREND,
        DOWNTREND,
        RANGING,
        NEUTRAL
    }

    public static MarketCondition detectMarketCondition(StockPrice stockPrice) {

        List<Double> closes =
                List.of(
                        stockPrice.getPrev5Close(),
                        stockPrice.getPrev4Close(),
                        stockPrice.getPrev3Close(),
                        stockPrice.getPrev2Close(),
                        stockPrice.getPrevClose(),
                        stockPrice.getClose());

        return detectMarketCondition(closes);
    }

    public static MarketCondition detectMarketConditionFromOHLC(StockPrice stockPrice) {

        List<OHLC> ohlcList = new ArrayList<>();

        ohlcList.add(
                new OHLC(
                        stockPrice.getPrev5Close(),
                        stockPrice.getPrev5High(),
                        stockPrice.getPrev5Low(),
                        stockPrice.getPrev5Close()));
        ohlcList.add(
                new OHLC(
                        stockPrice.getPrev4Close(),
                        stockPrice.getPrev4High(),
                        stockPrice.getPrev4Low(),
                        stockPrice.getPrev4Close()));
        ohlcList.add(
                new OHLC(
                        stockPrice.getPrev3Close(),
                        stockPrice.getPrev3High(),
                        stockPrice.getPrev3Low(),
                        stockPrice.getPrev3Close()));
        ohlcList.add(
                new OHLC(
                        stockPrice.getPrev2Close(),
                        stockPrice.getPrev2High(),
                        stockPrice.getPrev2Low(),
                        stockPrice.getPrev2Close()));
        ohlcList.add(
                new OHLC(
                        stockPrice.getPrevClose(),
                        stockPrice.getPrevHigh(),
                        stockPrice.getPrevLow(),
                        stockPrice.getPrevClose()));
        ohlcList.add(
                new OHLC(
                        stockPrice.getClose(),
                        stockPrice.getHigh(),
                        stockPrice.getLow(),
                        stockPrice.getClose()));

        return detectMarketConditionFromOHLC(ohlcList);
    }

    public static MarketCondition detectMarketCondition(List<Double> closes) {
        if (closes == null || closes.size() < 5) return MarketCondition.NEUTRAL;

        double max = closes.stream().max(Double::compareTo).orElse(0.0);
        double min = closes.stream().min(Double::compareTo).orElse(0.0);

        double priceRange = max - min;
        double directionalMove = closes.get(closes.size() - 1) - closes.get(0); // signed

        if (priceRange == 0) return MarketCondition.NEUTRAL;

        double trendRatio = Math.abs(directionalMove) / priceRange;

        if (trendRatio >= 0.6) {
            return directionalMove > 0 ? MarketCondition.UPTREND : MarketCondition.DOWNTREND;
        } else if (trendRatio <= 0.4) {
            return MarketCondition.RANGING;
        } else {
            return MarketCondition.NEUTRAL;
        }
    }

    public static MarketCondition detectMarketConditionFromOHLC(List<OHLC> candles) {
        if (candles == null || candles.size() < 5) return MarketCondition.NEUTRAL;

        int upMoves = 0, downMoves = 0, overlappingCandles = 0;

        double totalRange = 0;
        double totalBodySize = 0;

        for (int i = 0; i < candles.size(); i++) {
            OHLC c = candles.get(i);
            totalRange += c.high - c.low;
            totalBodySize += Math.abs(c.close - c.open);

            if (i > 0) {
                OHLC prev = candles.get(i - 1);
                if (c.high > prev.high && c.low > prev.low) upMoves++;
                else if (c.high < prev.high && c.low < prev.low) downMoves++;

                if (c.low <= prev.high && c.high >= prev.low) overlappingCandles++;
            }
        }

        double avgRange = totalRange / candles.size();
        double avgBodySize = totalBodySize / candles.size();

        boolean trending = upMoves >= 3 || downMoves >= 3;
        boolean ranging = overlappingCandles >= (candles.size() - 2);
        boolean narrowRange = avgBodySize / avgRange < 0.4;

        if (trending && !ranging) {
            return upMoves > downMoves ? MarketCondition.UPTREND : MarketCondition.DOWNTREND;
        }

        if (ranging && narrowRange) return MarketCondition.RANGING;

        return MarketCondition.NEUTRAL;
    }

    public static MarketCondition detectCombinedMarketCondition(StockPrice stockPrice) {
        MarketCondition closeBased = detectMarketCondition(stockPrice);
        MarketCondition ohlcBased = detectMarketConditionFromOHLC(stockPrice);

        // If both agree
        if (closeBased == ohlcBased) return closeBased;

        // If either shows a clear directional trend, prefer it
        if (closeBased == MarketCondition.UPTREND || ohlcBased == MarketCondition.UPTREND)
            return MarketCondition.UPTREND;
        if (closeBased == MarketCondition.DOWNTREND || ohlcBased == MarketCondition.DOWNTREND)
            return MarketCondition.DOWNTREND;

        // Prefer RANGING if one is ranging and other neutral
        if (closeBased == MarketCondition.RANGING || ohlcBased == MarketCondition.RANGING)
            return MarketCondition.RANGING;

        return MarketCondition.NEUTRAL;
    }
}
