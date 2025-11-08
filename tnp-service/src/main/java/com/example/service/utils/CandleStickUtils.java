package com.example.service.utils;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;

public class CandleStickUtils {

    private static final double MIN_BODY_PERCENTAGE = 0.75; // 75% of total range
    private static final double ATR_MULTIPLIER = 1.2; // 1.2x ATR
    private static final double TOLERANCE = 0.0001;

    private static final double WICK_TOLERANCE = 0.50;

    public static boolean isStrongBody(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null) return false;

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();

        double bodySize = Math.abs(close - open);
        double totalRange = high - low;
        if (totalRange == 0) return false;

        // Absolute threshold: body must be ≥ 3.82% (Fib) or 5% of price
        double midPrice = (open + close) / 2.0;
        double minPercentRange = 0.0382; // or 0.05 for stricter filter
        if (bodySize < minPercentRange * midPrice) return false;

        // Wick adjustment
        double upperWick = high - Math.max(open, close);
        double lowerWick = Math.min(open, close) - low;
        double effectiveRange =
                (lowerWick > upperWick)
                        ? (bodySize + upperWick) // ignore long lower wick
                        : totalRange;

        // Relative threshold: at least 60% of effective range
        return bodySize >= 0.60 * effectiveRange;
    }

    public static boolean isPrevSessionStrongBody(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null) return false;

        double open = stockPrice.getPrevOpen();
        double close = stockPrice.getPrevClose();
        double high = stockPrice.getPrevHigh();
        double low = stockPrice.getPrevLow();

        double bodySize = Math.abs(close - open);
        double totalRange = high - low;
        if (totalRange == 0) return false;

        // Absolute threshold: body must be ≥ 3.82% (Fib) or 5% of price
        double midPrice = (open + close) / 2.0;
        double minPercentRange = 0.0382; // or 0.05 for stricter filter
        if (bodySize < minPercentRange * midPrice) return false;

        // Wick adjustment
        double upperWick = high - Math.max(open, close);
        double lowerWick = Math.min(open, close) - low;
        double effectiveRange =
                (lowerWick > upperWick)
                        ? (bodySize + upperWick) // ignore long lower wick
                        : totalRange;

        // Relative threshold: at least 60% of effective range
        return bodySize >= 0.60 * effectiveRange;
    }

    public static boolean isPrev2SessionStrongBody(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null) return false;

        double open = stockPrice.getPrev2Open();
        double close = stockPrice.getPrev2Close();
        double high = stockPrice.getPrev2High();
        double low = stockPrice.getPrev2Low();

        double bodySize = Math.abs(close - open);
        double totalRange = high - low;
        if (totalRange == 0) return false;

        // Absolute threshold: body must be ≥ 3.82% (Fib) or 5% of price
        double midPrice = (open + close) / 2.0;
        double minPercentRange = 0.0382; // or 0.05 for stricter filter
        if (bodySize < minPercentRange * midPrice) return false;

        // Wick adjustment
        double upperWick = high - Math.max(open, close);
        double lowerWick = Math.min(open, close) - low;
        double effectiveRange =
                (lowerWick > upperWick)
                        ? (bodySize + upperWick) // ignore long lower wick
                        : totalRange;

        // Relative threshold: at least 60% of effective range
        return bodySize >= 0.60 * effectiveRange;
    }

    public static boolean isStrongRange(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) return false;

        double range = range(stockPrice);
        double prevRange = prevSessionRange(stockPrice);
        if (range == 0 || prevRange == 0) return false; // avoid div by zero

        // --- absolute threshold check ---
        // double price = stockPrice.getClose(); // could also use midpoint (high+low)/2
        double price =
                (stockPrice.getHigh() + stockPrice.getLow())
                        / 2; // could also use midpoint (high+low)/2
        double minPercentRange = 0.05; // Fibonacci 3.82% or 0.05 (5%)

        boolean isAbsoluteStrong = (range >= minPercentRange * price);

        // --- relative multiplier check ---
        double minRangeMultiplier;
        switch (timeframe) {
            case DAILY:
                minRangeMultiplier = 1.25;
                break;
            case WEEKLY:
                minRangeMultiplier = 1.5;
                break;
            case MONTHLY:
                minRangeMultiplier = 1.75;
                break;
            default:
                minRangeMultiplier = 2.0;
                break;
        }

        boolean isRelativeStrong = (range >= minRangeMultiplier * prevRange);

        // --- hybrid rule ---
        return isAbsoluteStrong || isRelativeStrong;
    }

    public static boolean isPrevStrongRange(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) return false;

        double range = prevSessionRange(stockPrice);
        double prevRange = prev2SessionRange(stockPrice);
        if (range == 0 || prevRange == 0) return false; // avoid div by zero

        // --- absolute threshold check ---
        // double price = stockPrice.getClose(); // could also use midpoint (high+low)/2
        double price =
                (stockPrice.getPrevHigh() + stockPrice.getPrevLow())
                        / 2; // could also use midpoint (high+low)/2
        double minPercentRange = 0.05; // Fibonacci 3.82% or 0.05 (5%)

        boolean isAbsoluteStrong = (range >= minPercentRange * price);

        // --- relative multiplier check ---
        double minRangeMultiplier;
        switch (timeframe) {
            case DAILY:
                minRangeMultiplier = 1.25;
                break;
            case WEEKLY:
                minRangeMultiplier = 1.5;
                break;
            case MONTHLY:
                minRangeMultiplier = 1.75;
                break;
            default:
                minRangeMultiplier = 2.0;
                break;
        }

        boolean isRelativeStrong = (range >= minRangeMultiplier * prevRange);

        // --- hybrid rule ---
        return isAbsoluteStrong || isRelativeStrong;
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

    public static boolean isPrev3SessionGreen(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrev3Close() > stockPrice.getPrev3Open();
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

    public static boolean isPrev3SessionRed(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrev3Close() < stockPrice.getPrev3Open();
    }

    public static double bodySize(StockPrice stockPrice) {
        if (stockPrice == null || stockPrice.getClose() == null || stockPrice.getOpen() == null) {
            return 0.0;
        }
        return Math.abs(stockPrice.getClose() - stockPrice.getOpen());
    }

    public static double prevSessionBodySize(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevOpen() == null) {
            return 0.0;
        }
        return Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
    }

    public static double prev2SessionBodySize(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrev2Close() == null
                || stockPrice.getPrev2Open() == null) {
            return 0.0;
        }
        return Math.abs(stockPrice.getPrev2Close() - stockPrice.getPrev2Open());
    }

    public static double range(StockPrice stockPrice) {
        if (stockPrice == null || stockPrice.getHigh() == null || stockPrice.getLow() == null) {
            return 0.0;
        }
        return stockPrice.getHigh() - stockPrice.getLow();
    }

    public static double prevSessionRange(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrevHigh() == null
                || stockPrice.getPrevLow() == null) {
            return 0.0;
        }
        return stockPrice.getPrevHigh() - stockPrice.getPrevLow();
    }

    public static double prev2SessionRange(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrev2High() == null
                || stockPrice.getPrev2Low() == null) {
            return 0.0;
        }
        return stockPrice.getPrev2High() - stockPrice.getPrev2Low();
    }

    public static double upperWickSize(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getHigh() == null
                || stockPrice.getOpen() == null
                || stockPrice.getClose() == null) {
            return 0.0;
        }
        return stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
    }

    public static double prevUpperWickSize(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getHigh() == null
                || stockPrice.getOpen() == null
                || stockPrice.getClose() == null) {
            return 0.0;
        }
        return stockPrice.getPrevHigh()
                - Math.max(stockPrice.getPrevOpen(), stockPrice.getPrevClose());
    }

    public static double lowerWickSize(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getLow() == null
                || stockPrice.getOpen() == null
                || stockPrice.getClose() == null) {
            return 0.0;
        }
        return Math.min(stockPrice.getOpen(), stockPrice.getClose()) - stockPrice.getLow();
    }

    public static double prevLowerWickSize(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrevLow() == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null) {
            return 0.0;
        }
        return Math.min(stockPrice.getPrevOpen(), stockPrice.getPrevClose())
                - stockPrice.getPrevLow();
    }

    public static double prev2LowerWickSize(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrev2Open() == null
                || stockPrice.getPrev2Close() == null
                || stockPrice.getPrev2Low() == null) {
            return 0.0;
        }
        return Math.min(stockPrice.getPrev2Open(), stockPrice.getPrev2Close())
                - stockPrice.getPrev2Low();
    }

    public static boolean isOpenAndLowEqual(StockPrice stockPrice) {
        if (stockPrice == null) return false;
        return areAlmostEqual(stockPrice.getOpen(), stockPrice.getLow(), TOLERANCE);
    }

    public static boolean areAlmostEqual(double value1, double value2, double tolerance) {
        return Math.abs(value1 - value2) < tolerance;
    }

    public static boolean isWickDominantCandle(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean checkUpperWick,
            boolean checkVerySmallBody) {
        if (stockPrice == null) return false;

        double bodySize = bodySize(stockPrice);
        double totalRange = range(stockPrice);
        double lowerWick = lowerWickSize(stockPrice);
        double upperWick = upperWickSize(stockPrice);

        if (totalRange == 0) return false; // Avoid division errors

        boolean smallBody =
                checkVerySmallBody
                        ? isVerySmallBody(stockPrice)
                        : isSmallBody(stockPrice, stockTechnicals);

        boolean longWick =
                checkUpperWick
                        ? (upperWick + WICK_TOLERANCE >= 2 * bodySize)
                        : (lowerWick + WICK_TOLERANCE >= 2 * bodySize);
        boolean smallOppositeWick =
                checkUpperWick
                        ? (lowerWick <= 0.15 * totalRange + WICK_TOLERANCE)
                        : (upperWick <= 0.15 * totalRange + WICK_TOLERANCE);

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
        if (stockPrice == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getOpen() == null) {
            return false;
        }
        return stockPrice.getOpen() > stockPrice.getPrevClose();
    }

    public static boolean isPrevOpenAbovePrevClose(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrev2Close() == null
                || stockPrice.getPrevOpen() == null) {
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

    public static boolean isLowerHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double high = stockPrice.getHigh();
        Double prevHigh = stockPrice.getPrevHigh();

        if (high == null || prevHigh == null) {
            return false;
        }

        return high < prevHigh;
    }

    public static boolean isPrevLowerHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double high = stockPrice.getPrevHigh();
        Double prevHigh = stockPrice.getPrev2High();

        if (high == null || prevHigh == null) {
            return false;
        }

        return high < prevHigh;
    }

    public static boolean isPrev2LowerHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double high = stockPrice.getPrev2High();
        Double prevHigh = stockPrice.getPrev3High();

        if (high == null || prevHigh == null) {
            return false;
        }

        return high < prevHigh;
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

    public static boolean isPrevLowerLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double low = stockPrice.getPrevLow();
        Double prevLow = stockPrice.getPrev2Low();

        if (low == null || prevLow == null) {
            return false;
        }

        return low < prevLow;
    }

    public static boolean isHigherLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double low = stockPrice.getLow();
        Double prevLow = stockPrice.getPrevLow();

        if (low == null || prevLow == null) {
            return false;
        }

        return low > prevLow;
    }

    public static boolean isPrevHigherLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double low = stockPrice.getPrevLow();
        Double prevLow = stockPrice.getPrev2Low();

        if (low == null || prevLow == null) {
            return false;
        }

        return low > prevLow;
    }

    public static boolean isLowerLow(double low, double prevLow) {
        return low < prevLow;
    }

    public static boolean isHigherLow(double low, double prevLow) {
        return low > prevLow;
    }

    public static boolean isHigherHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double high = stockPrice.getHigh();
        Double prevHigh = stockPrice.getPrevHigh();

        return (high != null && prevHigh != null) && high > prevHigh;
    }

    public static boolean isPrevHigherHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        Double high = stockPrice.getPrevHigh();
        Double prevHigh = stockPrice.getPrev2High();

        return (high != null && prevHigh != null) && high > prevHigh;
    }

    public static boolean isHigherHighAndHigherLow(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        Double high = stockPrice.getHigh();
        Double prevHigh = stockPrice.getPrevHigh();
        Double prev2High = stockPrice.getPrev2High();
        Double prev3High = stockPrice.getPrev3High();
        Double low = stockPrice.getLow();
        Double prevLow = stockPrice.getPrevLow();
        Double prev2Low = stockPrice.getPrev2Low();
        Double prev3Low = stockPrice.getPrev3Low();

        return high != null
                && prevHigh != null
                && prev2High != null
                && prev3High != null
                && low != null
                && prevLow != null
                && prev2Low != null
                && prev3Low != null
                && isHigherHigh(high, prevHigh)
                && isHigherHigh(prevHigh, prev2High)
                && isHigherHigh(prev2High, prev3High)
                && isHigherLow(low, prevLow)
                && isHigherLow(prevLow, prev2Low)
                && isHigherLow(prev2Low, prev3Low);
    }

    public static boolean isLowerHighAndLowerLow(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        Double high = stockPrice.getHigh();
        Double prevHigh = stockPrice.getPrevHigh();
        Double prev2High = stockPrice.getPrev2High();
        Double prev3High = stockPrice.getPrev3High();
        Double low = stockPrice.getLow();
        Double prevLow = stockPrice.getPrevLow();
        Double prev2Low = stockPrice.getPrev2Low();
        Double prev3Low = stockPrice.getPrev3Low();

        return high != null
                && prevHigh != null
                && prev2High != null
                && prev3High != null
                && low != null
                && prevLow != null
                && prev2Low != null
                && prev3Low != null
                && isLowerHigh(high, prevHigh)
                && isLowerHigh(prevHigh, prev2High)
                && isLowerHigh(prev2High, prev3High)
                && isLowerLow(low, prevLow)
                && isLowerLow(prevLow, prev2Low)
                && isLowerLow(prev2Low, prev3Low);
    }

    public static boolean isHigherHigh(double high, double prevHigh) {

        return high > prevHigh;
    }

    public static boolean isLowerHigh(double high, double prevHigh) {

        return high < prevHigh;
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

    public static boolean isProGapUp(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getOpen() > stockPrice.getPrevClose();
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

    public static boolean isProGapDown(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getOpen() < stockPrice.getPrevClose();
    }

    public static boolean isRisingWindow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getLow() > stockPrice.getPrevHigh();
    }

    public static boolean isPrevRisingWindow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getPrevLow() > stockPrice.getPrev2High();
    }

    public static boolean isFallingWindow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }
        return stockPrice.getHigh() < stockPrice.getPrevLow();
    }

    public static boolean isOpenInsidePrevBody(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getOpen() == null
                || stockPrice.getPrevLow() == null
                || stockPrice.getPrevHigh() == null) {
            return false;
        }

        return stockPrice.getOpen() > stockPrice.getPrevLow()
                && stockPrice.getOpen() < stockPrice.getPrevHigh();
    }

    public static boolean isPrevOpenInsideSecondPrevBody(StockPrice stockPrice) {

        if (stockPrice == null) return false;

        double prevOpen = stockPrice.getPrevOpen();
        double secondPrevOpen = stockPrice.getPrev2Open();
        double secondPrevClose = stockPrice.getPrev2Close();

        return prevOpen >= Math.min(secondPrevOpen, secondPrevClose)
                && prevOpen <= Math.max(secondPrevOpen, secondPrevClose);
    }

    public static boolean isHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() > stockPrice.getPrevClose();
    }

    public static boolean isPrevHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() > stockPrice.getPrev2Close();
    }

    public static boolean hasLongUpperWick(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getHigh() == null
                || stockPrice.getOpen() == null
                || stockPrice.getClose() == null) {
            return false;
        }

        double upperWick =
                stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
        double bodySize = Math.abs(stockPrice.getClose() - stockPrice.getOpen());

        return upperWick > (bodySize * 0.5); // Wick should be more than 50% of the body size
    }

    public static boolean hasPrevLongUpperWick(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrevHigh() == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null) {
            return false;
        }

        double upperWick =
                stockPrice.getPrevHigh()
                        - Math.max(stockPrice.getPrevOpen(), stockPrice.getPrevClose());
        double bodySize = Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());

        return upperWick > (bodySize * 0.5); // Wick should be more than 50% of the body size
    }

    public static boolean hasPrev2LongUpperWick(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrev2High() == null
                || stockPrice.getPrev2Open() == null
                || stockPrice.getPrev2Close() == null) {
            return false;
        }

        double upperWick =
                stockPrice.getPrev2High()
                        - Math.max(stockPrice.getPrev2Open(), stockPrice.getPrev2Close());
        double bodySize = Math.abs(stockPrice.getPrev2Close() - stockPrice.getPrev2Open());

        return upperWick > (bodySize * 0.5); // Wick should be more than 50% of the body size
    }

    public static boolean isLowerClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() < stockPrice.getPrevClose();
    }

    public static boolean isPrevLowerClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() < stockPrice.getPrev2Close();
    }

    public static boolean hasLongLowerWick(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getOpen() == null
                || stockPrice.getClose() == null
                || stockPrice.getLow() == null) {
            return false;
        }

        double bodySize = CandleStickUtils.bodySize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);

        return lowerWick >= (bodySize * 1.5);
    }

    public static boolean hasPrevLongLowerWick(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevLow() == null) {
            return false;
        }

        double bodySize = CandleStickUtils.prevSessionBodySize(stockPrice);
        double lowerWick = CandleStickUtils.prevLowerWickSize(stockPrice);

        return lowerWick >= (bodySize * 1.5);
    }

    public static boolean hasPrev2LongLowerWick(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrev2Open() == null
                || stockPrice.getPrev2Close() == null
                || stockPrice.getPrev2Low() == null) {
            return false;
        }

        double bodySize = CandleStickUtils.prev2SessionBodySize(stockPrice);
        double lowerWick = CandleStickUtils.prev2LowerWickSize(stockPrice);

        return lowerWick >= (bodySize * 1.5);
    }

    public static boolean isCloseHighEqual(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevHigh() == null
                || stockPrice.getPrevLow() == null) {
            return false;
        }

        if (stockPrice.getClose().equals(stockPrice.getHigh())) {
            return true;
        }

        return false;
    }

    public static boolean isSmallBody(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null
                || stockTechnicals == null
                || stockPrice.getOpen() == null
                || stockPrice.getClose() == null
                || stockPrice.getHigh() == null
                || stockPrice.getLow() == null
                || stockTechnicals.getAtr() == null) {
            return false;
        }

        double bodySize = Math.abs(stockPrice.getClose() - stockPrice.getOpen());
        double candleRange = stockPrice.getHigh() - stockPrice.getLow();
        // double atr = stockTechnicals.getAtr();

        if (candleRange == 0) return false;

        return bodySize <= (0.30 * candleRange) && bodySize > (0.1 * candleRange);
        // boolean smallByAtr = bodySize <= (0.4 * atr); // You can tweak this threshold

        // return smallByRange || smallByAtr;
    }

    public static boolean isPrevSmallBody(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevHigh() == null
                || stockPrice.getPrevLow() == null) {
            return false;
        }

        double bodySize = prevSessionBodySize(stockPrice);
        double candleRange = prevSessionRange(stockPrice);
        // double atr = stockTechnicals.getPrevAtr();

        return bodySize <= (0.30 * candleRange) && bodySize > (0.1 * candleRange);

        // boolean smallByAtr = bodySize <= (0.35 * atr); // You can tweak this threshold

        // return smallByRange || smallByAtr;
    }

    public static boolean isVerySmallBody(StockPrice stockPrice) {
        if (stockPrice == null) return false;
        double bodySize = bodySize(stockPrice);
        double range = range(stockPrice);
        return range > 0 && bodySize <= 0.1 * range;
    }

    public static boolean isPrevVerySmallBody(StockPrice stockPrice) {
        if (stockPrice == null) return false;
        double bodySize = prevSessionBodySize(stockPrice);
        double range = prevSessionRange(stockPrice);
        return range > 0 && bodySize <= 0.1 * range;
    }

    public static boolean isStrongLowerWick(StockPrice stockPrice) {

        double bodySize = bodySize(stockPrice);
        double lowerWickSize = lowerWickSize(stockPrice);
        double upperWickSize = upperWickSize(stockPrice);

        boolean isLowerWickSignificant =
                lowerWickSize > (0.50 * bodySize) && lowerWickSize > upperWickSize * 2;

        return isLowerWickSignificant;
    }

    public static boolean isPrevStrongLowerWick(StockPrice stockPrice) {

        double bodySize = prevSessionBodySize(stockPrice);
        double lowerWickSize = prevLowerWickSize(stockPrice);
        double upperWickSize = prevUpperWickSize(stockPrice);

        boolean isLowerWickSignificant =
                lowerWickSize > (0.50 * bodySize) && lowerWickSize > upperWickSize * 2;

        return isLowerWickSignificant;
    }

    public static boolean isStrongUpperWick(StockPrice stockPrice) {

        double bodySize = bodySize(stockPrice);
        double lowerWickSize = lowerWickSize(stockPrice);
        double upperWickSize = upperWickSize(stockPrice);

        boolean isUpperWickSignificant =
                upperWickSize > (0.50 * bodySize) && upperWickSize > lowerWickSize * 2;

        return isUpperWickSignificant;
    }

    public static boolean isPrevStrongUpperWick(StockPrice stockPrice) {

        double bodySize = prevSessionBodySize(stockPrice);
        double lowerWickSize = prevLowerWickSize(stockPrice);
        double upperWickSize = prevUpperWickSize(stockPrice);

        boolean isUpperWickSignificant =
                upperWickSize > (0.50 * bodySize) && upperWickSize > lowerWickSize * 2;

        return isUpperWickSignificant;
    }

    public static boolean isUpperWickDominant(StockPrice stockPrice) {

        double bodySize = bodySize(stockPrice);
        double lowerWickSize = lowerWickSize(stockPrice);
        double upperWickSize = upperWickSize(stockPrice);

        return upperWickSize > lowerWickSize && upperWickSize > bodySize;
    }

    public static boolean isPrevUpperWickDominant(StockPrice stockPrice) {

        double bodySize = prevSessionBodySize(stockPrice);
        double lowerWickSize = prevLowerWickSize(stockPrice);
        double upperWickSize = prevUpperWickSize(stockPrice);

        return upperWickSize > lowerWickSize && upperWickSize > bodySize;
    }

    public static boolean isLowerWickDominant(StockPrice stockPrice) {
        double bodySize = bodySize(stockPrice);
        double lowerWickSize = lowerWickSize(stockPrice);
        double upperWickSize = upperWickSize(stockPrice);

        return lowerWickSize > upperWickSize && lowerWickSize > bodySize;
    }

    public static boolean isPrevLowerWickDominant(StockPrice stockPrice) {
        double bodySize = prevSessionBodySize(stockPrice);
        double lowerWickSize = prevLowerWickSize(stockPrice);
        double upperWickSize = prevUpperWickSize(stockPrice);

        return lowerWickSize > upperWickSize && lowerWickSize > bodySize;
    }

    public static boolean isUpperWickLongerThanLowerWick(StockPrice stockPrice) {

        double bodySize = bodySize(stockPrice);
        double lowerWickSize = lowerWickSize(stockPrice);
        double upperWickSize = upperWickSize(stockPrice);

        return upperWickSize > lowerWickSize && upperWickSize >= 0.25 * bodySize;
    }

    public static boolean isLowerWickLongerThanUpperWick(StockPrice stockPrice) {
        double bodySize = bodySize(stockPrice);
        double lowerWickSize = lowerWickSize(stockPrice);
        double upperWickSize = upperWickSize(stockPrice);

        return upperWickSize < lowerWickSize && lowerWickSize >= 0.25 * bodySize;
    }

    public static boolean isUpperWickWithinLimit(StockPrice stockPrice) {

        // Check if within 20%
        return isUpperWickWithinLimit(stockPrice, 24.5);
    }

    public static boolean isUpperWickWithinLimit(StockPrice stockPrice, double threshold) {

        // Check if within 20%
        if (stockPrice == null
                || stockPrice.getOpen() == null
                || stockPrice.getClose() == null
                || stockPrice.getHigh() == null) {
            return false;
        }

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double high = stockPrice.getHigh();

        // Body size
        double body = Math.abs(close - open);
        if (body == 0) return false; // avoid division by zero

        // Determine top of the body
        double topOfBody = Math.max(open, close);

        // Upper wick size
        double upperWick = high - topOfBody;

        // Upper wick % of body
        double upperWickPercent = (upperWick / body) * 100.0;

        // Check if within 20%
        return upperWickPercent < threshold;
    }

    public static boolean isBearishEngulfing(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (isStrongBody(stockPrice.getTimeframe(), stockPrice, stockTechnicals)) {

            if (isRed(stockPrice)) {
                if (stockPrice.getOpen() > stockPrice.getPrevClose()) {
                    if (stockPrice.getClose() < stockPrice.getPrevOpen()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isDarkCloudCover(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (isStrongBody(stockPrice.getTimeframe(), stockPrice, stockTechnicals)) {

            if (isRed(stockPrice)) {
                // For Dark Cloud Cover, the current candle should open above previous close (gap
                // up)
                // and close below the midpoint of the previous bullish candle's body
                if (stockPrice.getOpen() > stockPrice.getPrevClose()) {
                    double prevBodyMidpoint =
                            (stockPrice.getPrevOpen() + stockPrice.getPrevClose()) / 2.0;
                    if (stockPrice.getClose() < prevBodyMidpoint) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isTweezerTop(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (isPrevSessionStrongBody(stockPrice.getTimeframe(), stockPrice, stockTechnicals)) {

            if (isRed(stockPrice)) {
                if (stockPrice.getOpen() == stockPrice.getPrevClose()) {
                    return true;
                }
            }
        }
        return false;
    }
}
