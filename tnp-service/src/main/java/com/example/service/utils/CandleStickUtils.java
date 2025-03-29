package com.example.service.utils;


import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.util.io.model.type.Timeframe;

public class CandleStickUtils {

    private static final double MIN_BODY_PERCENTAGE = 0.75; // 75% of total range
    private static final double ATR_MULTIPLIER = 1.2; // 1.2x ATR
    private static final double TOLERANCE = 0.0001;

    public static boolean isStrongBody(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) return false;

        double bodySize = bodySize(stockPrice);
        double totalRange = range(stockPrice);
        double atr = (stockTechnicals.getAtr() != null) ? stockTechnicals.getAtr() : 0.0;

        if (totalRange == 0) return false; // Avoid division issues

        // Adjust thresholds based on timeframe
        double minBodyPercentage, atrMultiplier;
        switch (timeframe) {
            case DAILY:   minBodyPercentage = 0.65; atrMultiplier = 1.1; break;
            case WEEKLY:  minBodyPercentage = 0.70; atrMultiplier = 1.3; break;
            case MONTHLY: minBodyPercentage = 0.75; atrMultiplier = 1.5; break;
            default: return false;
        }

        return (bodySize >= minBodyPercentage * totalRange) || (bodySize >= atrMultiplier * atr);
    }

    public static boolean isPrevSessionStrongBody(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) return false;

        double bodySize = prevSessionBodySize(stockPrice);
        double totalRange = prevSessionRange(stockPrice);
        double atr = (stockTechnicals.getPrevAtr() != null) ? stockTechnicals.getPrevAtr() : 0.0;

        if (totalRange == 0) return false; // Avoid division issues

        // Adjust thresholds based on timeframe
        double minBodyPercentage, atrMultiplier;
        switch (timeframe) {
            case DAILY:   minBodyPercentage = 0.65; atrMultiplier = 1.1; break;
            case WEEKLY:  minBodyPercentage = 0.70; atrMultiplier = 1.3; break;
            case MONTHLY: minBodyPercentage = 0.75; atrMultiplier = 1.5; break;
            default: return false;
        }

        return (bodySize >= minBodyPercentage * totalRange) || (bodySize >= atrMultiplier * atr);
    }

    public static boolean isPrev2SessionStrongBody(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) return false;

        double bodySize = prev2SessionBodySize(stockPrice);
        double totalRange = prev2SessionRange(stockPrice);
        double atr = (stockTechnicals.getPrev2Atr() != null) ? stockTechnicals.getPrev2Atr() : 0.0;

        if (totalRange == 0) return false; // Avoid division issues

        // Adjust thresholds based on timeframe
        double minBodyPercentage, atrMultiplier;
        switch (timeframe) {
            case DAILY:   minBodyPercentage = 0.65; atrMultiplier = 1.1; break;
            case WEEKLY:  minBodyPercentage = 0.70; atrMultiplier = 1.3; break;
            case MONTHLY: minBodyPercentage = 0.75; atrMultiplier = 1.5; break;
            default: return false;
        }

        return (bodySize >= minBodyPercentage * totalRange) || (bodySize >= atrMultiplier * atr);
    }

    public static boolean isStrongRange(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) return false;

        double range = range(stockPrice);
        double prevRange = prevSessionRange(stockPrice);
        double atr = (stockTechnicals.getAtr() != null) ? stockTechnicals.getAtr() : 0.0;

        if (range == 0) return false; // Avoid division issues

        // Adjust thresholds based on timeframe
        double minRangeMultiplier, atrMultiplier;
        switch (timeframe) {
            case DAILY:   minRangeMultiplier = 1.2; atrMultiplier = 1.1; break;
            case WEEKLY:  minRangeMultiplier = 1.3; atrMultiplier = 1.3; break;
            case MONTHLY: minRangeMultiplier = 1.5; atrMultiplier = 1.5; break;
            default: return false;
        }

        return (range >= minRangeMultiplier * prevRange) || (range >= atrMultiplier * atr);
    }

    public static boolean isGreen(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() > stockPrice.getOpen();
    }

    public static boolean isPrevSessionGreen(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() > stockPrice.getPrevOpen();
    }

    public static boolean isPrev2SessionGreen(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrev2Close() > stockPrice.getPrev2Open();
    }

    public static boolean isRed(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() < stockPrice.getOpen();
    }

    public static boolean isPrevSessionRed(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() < stockPrice.getPrevOpen();
    }

    public static boolean isPrev2SessionRed(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrev2Close() < stockPrice.getPrev2Open();
    }

    public static double bodySize(StockPrice stockPrice){
        return Math.abs(stockPrice.getClose() - stockPrice.getOpen());
    }

    public static double prevSessionBodySize(StockPrice stockPrice){
        return Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
    }

    public static double prev2SessionBodySize(StockPrice stockPrice){
        return Math.abs(stockPrice.getPrev2Close() - stockPrice.getPrev2Open());
    }

    public static double range(StockPrice stockPrice){
        return stockPrice.getHigh() - stockPrice.getLow();
    }

    public static double prevSessionRange(StockPrice stockPrice){
        return stockPrice.getPrevHigh() - stockPrice.getPrevLow();
    }

    public static double prev2SessionRange(StockPrice stockPrice){
        return stockPrice.getPrev2High() - stockPrice.getPrev2Low();
    }

    public static double upperWickSize(StockPrice stockPrice) {
        return stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
    }

    public static double lowerWickSize(StockPrice stockPrice) {
        return Math.min(stockPrice.getOpen(), stockPrice.getClose()) - stockPrice.getLow();
    }

    public static double prevLowerWickSize(StockPrice stockPrice) {
        return Math.min(stockPrice.getPrevOpen(), stockPrice.getPrevClose()) - stockPrice.getPrevLow();
    }

    public static double prev2LowerWickSize(StockPrice stockPrice) {
        return Math.min(stockPrice.getPrev2Open(), stockPrice.getPrev2Close()) - stockPrice.getPrev2Low();
    }

    public static boolean isOpenAndLowEqual(StockPrice stockPrice) {
        if (stockPrice == null) return false;
        return areAlmostEqual(stockPrice.getOpen(), stockPrice.getLow(), TOLERANCE);
    }

    public static boolean areAlmostEqual(double value1, double value2, double tolerance) {
        return Math.abs(value1 - value2) < tolerance;
    }

    public static boolean isWickDominantCandle(StockPrice stockPrice, boolean checkUpperWick) {
        if (stockPrice == null) return false;

        double bodySize = bodySize(stockPrice);
        double totalRange = range(stockPrice);
        double lowerWick = lowerWickSize(stockPrice);
        double upperWick = upperWickSize(stockPrice);

        if (totalRange == 0) return false; // Avoid division errors

        boolean smallBody = bodySize <= 0.35 * totalRange;
        boolean longWick = checkUpperWick ? (upperWick >= 2 * bodySize) : (lowerWick >= 2 * bodySize);
        boolean smallOppositeWick = checkUpperWick ? (lowerWick <= 0.1 * totalRange) : (upperWick <= 0.1 * totalRange);

        return smallBody && longWick && smallOppositeWick;
    }

    public static boolean isOpenBelowPrevClose(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double open = stockPrice.getOpen();
        Double prevClose = stockPrice.getPrevClose();

        if (open == null || prevClose == null) {
            return false; // Or log a warning
        }

        return open < prevClose;
    }

    public static boolean isPrevOpenBelowPrevClose(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double open = stockPrice.getPrevOpen();
        Double prevClose = stockPrice.getPrev2Close();

        if (open == null || prevClose == null) {
            return false; // Or log a warning
        }

        return open < prevClose;
    }

    public static boolean isOpenAbovePrevClose(StockPrice stockPrice) {
        if (stockPrice == null || stockPrice.getPrevClose() == null || stockPrice.getOpen() == null) {
            return false;
        }
        return stockPrice.getOpen() > stockPrice.getPrevClose();
    }

    public static boolean isPrevOpenAbovePrevClose(StockPrice stockPrice) {
        if (stockPrice == null || stockPrice.getPrev2Close() == null || stockPrice.getPrevOpen() == null) {
            return false;
        }
        return stockPrice.getPrevOpen() > stockPrice.getPrev2Close();
    }

    public static boolean isCloseAbovePrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        Double close = stockPrice.getClose();
        Double prevOpen = stockPrice.getPrevOpen();

        return (close != null && prevOpen != null) && close > prevOpen;
    }

    public static boolean isPrevCloseAbovePrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        Double close = stockPrice.getPrevClose();
        Double prevOpen = stockPrice.getPrev2Open();

        return (close != null && prevOpen != null) && close > prevOpen;
    }

    public static boolean isCloseBelowPrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        Double close = stockPrice.getClose();
        Double prevOpen = stockPrice.getPrevOpen();

        return (close != null && prevOpen != null) && close < prevOpen;
    }

    public static boolean isPrevCloseBelowPrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        Double close = stockPrice.getPrevClose();
        Double prevOpen = stockPrice.getPrev2Open();

        return (close != null && prevOpen != null) && close < prevOpen;
    }

    public static boolean isLowerLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double low = stockPrice.getLow();
        Double prevLow = stockPrice.getPrevLow();

        if (low == null || prevLow == null) {
            return false;
        }

        return low < prevLow;
    }

    public static boolean isHigherHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double high = stockPrice.getHigh();
        Double prevHigh = stockPrice.getPrevHigh();

        return (high != null && prevHigh != null) && high > prevHigh;
    }

    public static boolean isGapUp(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getOpen() > stockPrice.getPrevHigh();
    }

    public static boolean isPrevGapUp(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getPrevOpen() > stockPrice.getPrev2High();
    }

    public static boolean isGapDown(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getOpen() < stockPrice.getPrevLow();
    }

    public static boolean isPrevGapDown(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getPrevOpen() < stockPrice.getPrev2Low();
    }

    public static boolean isOpenInsidePrevBody(StockPrice stockPrice) {
        return stockPrice.getOpen() > stockPrice.getPrevLow() && stockPrice.getOpen() < stockPrice.getPrevHigh();
    }

    public static boolean isPrevOpenInsideSecondPrevBody(StockPrice stockPrice) {


        if (stockPrice == null ) return false;

        double prevOpen = stockPrice.getPrevOpen();
        double secondPrevOpen = stockPrice.getPrev2Open();
        double secondPrevClose = stockPrice.getPrev2Close();

        return prevOpen >= Math.min(secondPrevOpen, secondPrevClose) && prevOpen <= Math.max(secondPrevOpen, secondPrevClose);
    }

    public static boolean isHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() > stockPrice.getPrevClose();
    }

    public static boolean isPrevHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() > stockPrice.getPrev2Close();
    }

    public static boolean hasLongUpperWick(StockPrice stockPrice) {
        double upperWick = stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
        double bodySize = Math.abs(stockPrice.getClose() - stockPrice.getOpen());
        return upperWick > (bodySize * 0.5);  // Wick should be more than 50% of the body size
    }

    public static boolean hasPrevLongUpperWick(StockPrice stockPrice) {
        double upperWick = stockPrice.getPrevHigh() - Math.max(stockPrice.getPrevOpen(), stockPrice.getPrevClose());
        double bodySize = Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
        return upperWick > (bodySize * 0.5);  // Wick should be more than 50% of the body size
    }

    public static boolean hasPrev2LongUpperWick(StockPrice stockPrice) {
        double upperWick = stockPrice.getPrev2High() - Math.max(stockPrice.getPrev2Open(), stockPrice.getPrev2Close());
        double bodySize = Math.abs(stockPrice.getPrev2Close() - stockPrice.getPrev2Open());
        return upperWick > (bodySize * 0.5);  // Wick should be more than 50% of the body size
    }

    public static boolean isLowerClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() < stockPrice.getPrevClose();
    }

    public static boolean isPrevLowerClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() < stockPrice.getPrev2Close();
    }

    public static boolean hasLongLowerWick(StockPrice stockPrice) {
        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        return lowerWick >= (bodySize * 1.5); // Lower wick should be at least 1.5x the body size
    }
    public static boolean hasPrevLongLowerWick(StockPrice stockPrice) {
        double bodySize = CandleStickUtils.prevSessionBodySize(stockPrice);
        double lowerWick = CandleStickUtils.prevLowerWickSize(stockPrice);

        return lowerWick >= (bodySize * 1.5); // Lower wick should be at least 1.5x the body size
    }

    public static boolean hasPrev2LongLowerWick(StockPrice stockPrice) {
        double bodySize = CandleStickUtils.prev2SessionBodySize(stockPrice);
        double lowerWick = CandleStickUtils.prev2LowerWickSize(stockPrice);

        return lowerWick >= (bodySize * 1.5); // Lower wick should be at least 1.5x the body size
    }

    public static boolean isSmallBody(StockPrice stockPrice) {
        double bodySize = Math.abs(stockPrice.getClose() - stockPrice.getOpen());
        double candleRange = stockPrice.getHigh() - stockPrice.getLow();

        return bodySize <= (0.3 * candleRange); // Small body if <= 30% of the total range
    }

    public static boolean isPrevSmallBody(StockPrice stockPrice) {
        double bodySize = Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
        double candleRange = stockPrice.getPrevHigh() - stockPrice.getPrevLow();

        return bodySize <= (0.3 * candleRange); // Small body if <= 30% of the total range
    }
}
