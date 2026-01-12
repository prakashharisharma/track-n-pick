package com.example.service.impl;

import com.example.data.transactional.entities.StockPrice;
import com.example.service.CandleStickService;
import com.example.service.StockPriceService;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandleStickServiceImpl implements CandleStickService {
    private static final double TOLERANCE = 0.0001;
    @Autowired private FormulaService formulaService;
    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Override
    public boolean isDead(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getOpen() == null
                || stockPrice.getHigh() == null
                || stockPrice.getLow() == null
                || stockPrice.getClose() == null) {
            return false; // or throw an exception based on requirements
        }
        return stockPrice.getOpen().equals(stockPrice.getHigh())
                && stockPrice.getOpen().equals(stockPrice.getLow())
                && stockPrice.getOpen().equals(stockPrice.getClose());
    }

    @Override
    public double upperWickSize(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }
        /*
        double high = stockPrice.getHigh();
        double referencePrice = this.isGreen(stockPrice) ? stockPrice.getClose() : stockPrice.getOpen();

        return formulaService.calculateChangePercentage(referencePrice, high);
        */
        return stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
    }

    @Override
    public double lowerWickSize(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        /*
        double low = stockPrice.getLow();
        double referencePrice = this.isGreen(stockPrice) ? stockPrice.getOpen() : stockPrice.getClose();

        return formulaService.calculateChangePercentage(referencePrice, low);

        return stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
         */
        return Math.min(stockPrice.getOpen(), stockPrice.getClose()) - stockPrice.getLow();
    }

    @Override
    public double currentBodySize(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();

        return formulaService.calculateChangePercentage(
                Math.min(open, close), Math.max(open, close));
    }

    @Override
    public double prevBodySize(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        double open = stockPrice.getPrevOpen();
        double close = stockPrice.getPrevClose();

        return formulaService.calculateChangePercentage(
                Math.min(open, close), Math.max(open, close));
    }

    public double prev2BodySize(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        double open = stockPrice.getPrev2Open();
        double close = stockPrice.getPrev2Close();

        return formulaService.calculateChangePercentage(
                Math.min(open, close), Math.max(open, close));
    }

    @Override
    public double range(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double low = stockPrice.getLow();
        Double high = stockPrice.getHigh();

        return formulaService.calculateChangePercentage(low, high);
    }

    @Override
    public double prevRange(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double low = stockPrice.getPrevLow();
        Double high = stockPrice.getPrevHigh();

        return formulaService.calculateChangePercentage(low, high);
    }

    @Override
    public boolean hasLongLowerWick(StockPrice stockPrice) {
        double bodySize = this.currentBodySize(stockPrice);
        double lowerWick = this.lowerWickSize(stockPrice);

        return lowerWick >= (bodySize * 1.5); // Lower wick should be at least 1.5x the body size
    }

    @Override
    public boolean isOpenInsidePrevBody(StockPrice stockPrice) {
        return stockPrice.getOpen() > stockPrice.getPrevLow()
                && stockPrice.getOpen() < stockPrice.getPrevHigh();
    }

    public boolean isPrevOpenInsidePrev2Body(StockPrice stockPrice) {
        return stockPrice.getPrevOpen() > stockPrice.getPrev2Low()
                && stockPrice.getPrevOpen() < stockPrice.getPrev2High();
    }

    @Override
    public boolean isCloseInsidePrevBody(StockPrice stockPrice) {
        return stockPrice.getClose() > stockPrice.getPrevLow()
                && stockPrice.getClose() < stockPrice.getPrevHigh();
    }

    public boolean isPrevCloseInsidePrev2Body(StockPrice stockPrice) {
        return stockPrice.getPrevClose() > stockPrice.getPrev2Low()
                && stockPrice.getPrevClose() < stockPrice.getPrev2High();
    }

    @Override
    public boolean isCloseAbovePrevClose(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double close = stockPrice.getClose();
        Double prevClose = stockPrice.getPrevClose();

        if (close == null || prevClose == null) {
            return false; // Or consider throwing an exception
        }

        return close > prevClose;
    }

    @Override
    public boolean isCloseBelowPrevClose(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double close = stockPrice.getClose();
        Double prevClose = stockPrice.getPrevClose();

        if (close == null || prevClose == null) {
            return false; // Or log a warning instead
        }

        return close < prevClose;
    }

    @Override
    public boolean isOpenAbovePrevClose(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double open = stockPrice.getOpen();
        Double prevClose = stockPrice.getPrevClose();

        if (open == null || prevClose == null) {
            return false; // Or log a warning
        }

        return open > prevClose;
    }

    public boolean isPrevOpenAbovePrev2Close(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double open = stockPrice.getPrevOpen();
        Double prevClose = stockPrice.getPrev2Close();

        if (open == null || prevClose == null) {
            return false; // Or log a warning
        }

        return open > prevClose;
    }

    @Override
    public boolean isOpenBelowPrevClose(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double open = stockPrice.getOpen();
        Double prevClose = stockPrice.getPrevClose();

        if (open == null || prevClose == null) {
            return false; // Or log a warning
        }

        return open < prevClose;
    }

    public boolean isPrevOpenBelowPrev2Close(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double open = stockPrice.getPrevOpen();
        Double prevClose = stockPrice.getPrev2Close();

        if (open == null || prevClose == null) {
            return false; // Or log a warning
        }

        return open < prevClose;
    }

    @Override
    public boolean isOpenAtPrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double open = stockPrice.getOpen();
        Double prevOpen = stockPrice.getPrevOpen();

        if (open == null || prevOpen == null) {
            return false; // Or log a warning
        }

        return open.equals(prevOpen);
    }

    @Override
    public boolean isOpenAtPrevClose(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double open = stockPrice.getOpen();
        Double prevClose = stockPrice.getPrevClose();

        if (open == null || prevClose == null) {
            return false; // Or log a warning
        }

        // Handling floating-point precision issues
        final double TOLERANCE = 0.0001;
        return Math.abs(open - prevClose) < TOLERANCE;
    }

    @Override
    public boolean isOpenAbovePrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        Double open = stockPrice.getOpen();
        Double prevOpen = stockPrice.getPrevOpen();

        if (open == null || prevOpen == null) {
            return false; // Or log a warning
        }

        return open > prevOpen;
    }

    @Override
    public boolean isOpenBelowPrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double open = stockPrice.getOpen();
        Double prevOpen = stockPrice.getPrevOpen();

        return (open != null && prevOpen != null) && open < prevOpen;
    }

    @Override
    public boolean isCloseAbovePrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double close = stockPrice.getClose();
        Double prevOpen = stockPrice.getPrevOpen();

        return (close != null && prevOpen != null) && close > prevOpen;
    }

    @Override
    public boolean isCloseBelowPrevOpen(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double close = stockPrice.getClose();
        Double prevOpen = stockPrice.getPrevOpen();

        return (close != null && prevOpen != null) && close < prevOpen;
    }

    @Override
    public boolean isOpenAndLowEqual(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double open = stockPrice.getOpen();
        Double low = stockPrice.getLow();

        return (open != null && low != null) && Math.abs(open - low) < TOLERANCE;
    }

    @Override
    public boolean isOpenAndHighEqual(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double open = stockPrice.getOpen();
        Double high = stockPrice.getHigh();

        return (open != null && high != null) && Math.abs(open - high) < TOLERANCE;
    }

    @Override
    public boolean isCloseAndLowEqual(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double close = stockPrice.getClose();
        Double low = stockPrice.getLow();

        return (close != null && low != null) && Math.abs(close - low) < TOLERANCE;
    }

    @Override
    public boolean isCloseAndHighEqual(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double close = stockPrice.getClose();
        Double high = stockPrice.getHigh();

        return (close != null && high != null) && Math.abs(close - high) < TOLERANCE;
    }

    @Override
    public boolean isCloseBelowPrevLow(StockPrice stockPrice) {
        if (stockPrice == null) throw new IllegalArgumentException("StockPrice cannot be null");

        Double close = stockPrice.getClose();
        Double prevLow = stockPrice.getPrevLow();

        return (close != null && prevLow != null) && close < prevLow;
    }

    @Override
    public boolean isHigherHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double high = stockPrice.getHigh();
        Double prevHigh = stockPrice.getPrevHigh();

        return (high != null && prevHigh != null) && high > prevHigh;
    }

    @Override
    public boolean isHigherLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double low = stockPrice.getLow();
        Double prevLow = stockPrice.getPrevLow();

        if (low == null || prevLow == null) {
            log.warn(
                    "StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false",
                    low,
                    prevLow);
            return false;
        }

        return low > prevLow;
    }

    @Override
    public boolean isPrevHigherHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double high = stockPrice.getPrevHigh();
        Double prevHigh = stockPrice.getPrev2High();

        return (high != null && prevHigh != null) && high > prevHigh;
    }

    @Override
    public boolean isPrevHigherLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double low = stockPrice.getPrevLow();
        Double prevLow = stockPrice.getPrev2Low();

        if (low == null || prevLow == null) {
            log.warn(
                    "StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false",
                    low,
                    prevLow);
            return false;
        }

        return low > prevLow;
    }

    @Override
    public boolean isLowerHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double high = stockPrice.getHigh();
        Double prevHigh = stockPrice.getPrevHigh();

        if (high == null || prevHigh == null) {
            log.warn(
                    "StockPrice high or prevHigh is null (high: {}, prevHigh: {}), returning false",
                    high,
                    prevHigh);
            return false;
        }

        return high < prevHigh;
    }

    @Override
    public boolean isLowerLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double low = stockPrice.getLow();
        Double prevLow = stockPrice.getPrevLow();

        if (low == null || prevLow == null) {
            log.warn(
                    "StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false",
                    low,
                    prevLow);
            return false;
        }

        return low < prevLow;
    }

    @Override
    public boolean isPrevLowerHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double high = stockPrice.getPrevHigh();
        Double prevHigh = stockPrice.getPrev2High();

        if (high == null || prevHigh == null) {
            log.warn(
                    "StockPrice high or prevHigh is null (high: {}, prevHigh: {}), returning false",
                    high,
                    prevHigh);
            return false;
        }

        return high < prevHigh;
    }

    @Override
    public boolean isPrevLowerLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double low = stockPrice.getPrevLow();
        Double prevLow = stockPrice.getPrev2Low();

        if (low == null || prevLow == null) {
            log.warn(
                    "StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false",
                    low,
                    prevLow);
            return false;
        }

        return low < prevLow;
    }

    @Override
    public boolean isSellingWickPresent(StockPrice stockPrice) {

        return this.isSellingWickPresent(stockPrice, DEFAULT_SELLING_WICK_PER);
    }

    @Override
    public boolean isSellingWickPresent(StockPrice stockPrice, double benchmark) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double high = stockPrice.getHigh();
        Double open = stockPrice.getOpen();
        Double close = stockPrice.getClose();

        if (high == null || open == null || close == null) {
            log.warn(
                    "StockPrice contains null values (high: {}, open: {}, close: {}), returning"
                            + " false",
                    high,
                    open,
                    close);
            return false;
        }

        if (high.equals(open)) {
            return false;
        }

        double bodySize = high - open;
        double upperWickSize = high - close;

        if (this.isRed(stockPrice)) {
            bodySize = high - close;
            upperWickSize = high - open;
        }

        double highWickPerOfBody = formulaService.calculatePercentage(bodySize, upperWickSize);

        return highWickPerOfBody >= benchmark;
    }

    @Override
    public boolean isBuyingWickPresent(StockPrice stockPrice) {

        return this.isBuyingWickPresent(stockPrice, BUYING_WICK_PER);
    }

    @Override
    public boolean isBuyingWickPresent(StockPrice stockPrice, double benchmark) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        Double low = stockPrice.getLow();
        Double open = stockPrice.getOpen();
        Double close = stockPrice.getClose();

        if (low == null || open == null || close == null) {
            log.warn(
                    "StockPrice contains null values (low: {}, open: {}, close: {}), returning"
                            + " false",
                    low,
                    open,
                    close);
            return false;
        }

        if (low.equals(close)) {
            return false;
        }

        double bodySize = close - low;
        double lowerWickSize = open - low;

        if (this.isRed(stockPrice)) {
            bodySize = open - low;
            lowerWickSize = close - low;
        }

        double lowerWickPerOfBody = formulaService.calculatePercentage(bodySize, lowerWickSize);

        return lowerWickPerOfBody >= benchmark;
    }

    @Override
    public boolean isGreen(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getOpen() < stockPrice.getClose();
    }

    @Override
    public boolean isGapUp(StockPrice stockPrice) {
        if (this.isOpenAbovePrevClose(stockPrice)) {
            log.info("Gap up active");
            return true;
        }
        return false;
    }

    public boolean isPrevGapUp(StockPrice stockPrice) {
        if (this.isPrevOpenAbovePrev2Close(stockPrice)) {
            log.info("Gap up active");
            return true;
        }
        return false;
    }

    @Override
    public boolean isGapDown(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        if (this.isOpenBelowPrevClose(stockPrice)) {
            log.info("Gap down active");
            return true;
        }

        return false;
    }

    public boolean isPrevGapDown(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        if (this.isPrevOpenBelowPrev2Close(stockPrice)) {
            log.info("Gap down active");
            return true;
        }

        return false;
    }

    @Override
    public boolean isRisingWindow(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        if (this.isGreen(stockPrice) && stockPrice.getLow() > stockPrice.getPrevHigh()) {
            log.info("Rising window active");
            return true;
        }

        return false;
    }

    @Override
    public boolean isFallingWindow(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        if (this.isRed(stockPrice) && stockPrice.getHigh() < stockPrice.getPrevLow()) {
            log.info("Falling window active");
            return true;
        }

        return false;
    }

    @Override
    public boolean isPreviousSessionGreen(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        return stockPrice.getPrevOpen() < stockPrice.getPrevClose();
    }

    public boolean isPrevious2SessionGreen(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        return stockPrice.getPrev2Open() < stockPrice.getPrev2Close();
    }

    @Override
    public boolean isRed(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        return stockPrice.getOpen() > stockPrice.getClose();
    }

    @Override
    public boolean isPreviousSessionRed(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        return stockPrice.getPrevOpen() > stockPrice.getPrevClose();
    }

    public boolean isPrevious2SessionRed(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        return stockPrice.getPrev2Open() > stockPrice.getPrev2Close();
    }

    @Override
    public boolean isDoji(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getOpen();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();

        double realBody = Math.abs(close - open);
        double totalRange = high - low;

        // Avoid division by zero
        if (totalRange == 0) {
            return false;
        }

        // Doji criteria: Very small real body (typically less than 5-10% of total range)
        boolean hasVerySmallBody = realBody < totalRange * 0.1; // Body less than 10% of total range

        return hasVerySmallBody;
    }

    @Override
    public boolean isPrevDoji(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getPrevOpen();
        double high = stockPrice.getPrevHigh();
        double low = stockPrice.getPrevLow();
        double close = stockPrice.getPrevClose();

        double realBody = Math.abs(close - open);
        double totalRange = high - low;

        // Avoid division by zero
        if (totalRange == 0) {
            return false;
        }

        // Doji criteria: Very small real body (typically less than 5-10% of total range)
        boolean hasVerySmallBody = realBody < totalRange * 0.1; // Body less than 10% of total range

        return hasVerySmallBody;
    }

    @Override
    public boolean isPrev2Doji(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getPrev2Open();
        double high = stockPrice.getPrev2High();
        double low = stockPrice.getPrev2Low();
        double close = stockPrice.getPrev2Close();

        double realBody = Math.abs(close - open);
        double totalRange = high - low;

        // Avoid division by zero
        if (totalRange == 0) {
            return false;
        }

        // Doji criteria: Very small real body (typically less than 5-10% of total range)
        boolean hasVerySmallBody = realBody < totalRange * 0.1; // Body less than 10% of total range

        return hasVerySmallBody;
    }

    @Override
    public boolean isGravestoneDoji(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        boolean isOpenCloseEqual =
                formulaService.isEpsilonEqual(
                        stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8);

        if (!isOpenCloseEqual) return false;

        boolean isOpenAtLow = stockPrice.getOpen().equals(stockPrice.getLow());

        if (isOpenAtLow) {
            log.info("Gravestone Doji candle detected.");
            return true;
        }

        return false;
    }

    @Override
    public boolean isDragonflyDoji(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        boolean isOpenCloseEqual =
                formulaService.isEpsilonEqual(
                        stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8);

        if (!isOpenCloseEqual) return false;

        boolean isOpenAtHigh = stockPrice.getOpen().equals(stockPrice.getHigh());

        if (isOpenAtHigh) {
            log.info("Dragonfly Doji candle detected.");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBullishPinBar(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        boolean isOpenCloseEqual =
                formulaService.isEpsilonEqual(
                        stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8);

        if (!isOpenCloseEqual) return false;

        double lowerWick = lowerWickSize(stockPrice);
        double upperWick = upperWickSize(stockPrice);

        if (lowerWick > upperWick * 3) {
            log.info("Bullish Pin Bar detected.");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBearishPinBar(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        boolean isOpenCloseEqual =
                formulaService.isEpsilonEqual(
                        stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8);

        if (!isOpenCloseEqual) return false;

        double upperWick = upperWickSize(stockPrice);
        double lowerWick = lowerWickSize(stockPrice);

        if (upperWick > lowerWick * 3) {
            log.info("Bearish Pin Bar detected.");
            return true;
        }

        return false;
    }

    @Override
    public boolean isSpinningTop(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getOpen();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();

        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;
        double totalRange = high - low;

        if (totalRange == 0) {
            return false;
        }

        // Spinning Top criteria:
        // 1. Small real body (larger than Doji but still small - typically 10-30% of total range)
        boolean hasSmallBody = realBody > totalRange * 0.1 && realBody < totalRange * 0.3;

        // 2. Relatively equal shadows on both sides (neither shadow dominates)
        double shadowRatio =
                Math.min(upperShadow, lowerShadow) / Math.max(upperShadow, lowerShadow);
        boolean hasBalancedShadows = shadowRatio >= 0.5; // Shadows within 50% of each other

        // 3. Both shadows should be significant (at least 20% of total range each)
        boolean hasSignificantUpperShadow = upperShadow >= totalRange * 0.2;
        boolean hasSignificantLowerShadow = lowerShadow >= totalRange * 0.2;

        return hasSmallBody
                && hasBalancedShadows
                && hasSignificantUpperShadow
                && hasSignificantLowerShadow;
    }

    @Override
    public boolean isPrevSpinningTop(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getPrevOpen();
        double high = stockPrice.getPrevHigh();
        double low = stockPrice.getPrevLow();
        double close = stockPrice.getPrevClose();

        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;
        double totalRange = high - low;

        if (totalRange == 0) {
            return false;
        }

        // Spinning Top criteria:
        // 1. Small real body (larger than Doji but still small - typically 10-30% of total range)
        boolean hasSmallBody = realBody > totalRange * 0.1 && realBody < totalRange * 0.3;

        // 2. Relatively equal shadows on both sides (neither shadow dominates)
        double shadowRatio =
                Math.min(upperShadow, lowerShadow) / Math.max(upperShadow, lowerShadow);
        boolean hasBalancedShadows = shadowRatio >= 0.5; // Shadows within 50% of each other

        // 3. Both shadows should be significant (at least 20% of total range each)
        boolean hasSignificantUpperShadow = upperShadow >= totalRange * 0.2;
        boolean hasSignificantLowerShadow = lowerShadow >= totalRange * 0.2;

        return hasSmallBody
                && hasBalancedShadows
                && hasSignificantUpperShadow
                && hasSignificantLowerShadow;
    }

    @Override
    public boolean isInDecision(StockPrice stockPrice) {
        return this.isDoji(stockPrice) || this.isSpinningTop(stockPrice);
    }

    @Override
    public boolean isPrevInDecision(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        return this.isPrevDoji(stockPrice) || this.isPrevSpinningTop(stockPrice);
    }

    @Override
    public boolean isPrevInDecisionConfirmationBullish(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        if (!this.isGreen(stockPrice)
                || !this.isPrevInDecision(stockPrice)
                || this.isInDecision(stockPrice)) {
            return false;
        }

        if (this.isHigherHigh(stockPrice)) {
            if ((this.isPreviousSessionRed(stockPrice) && this.isCloseAbovePrevOpen(stockPrice))
                    || (this.isPreviousSessionGreen(stockPrice)
                            && this.isCloseAbovePrevClose(stockPrice))) {
                return true;
            }
        }

        return this.isHammer(stockPrice);
    }

    @Override
    public boolean isPrevInDecisionConfirmationBearish(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        if (!this.isRed(stockPrice)
                || !this.isPrevInDecision(stockPrice)
                || this.isInDecision(stockPrice)) {
            return false;
        }

        if (this.isLowerLow(stockPrice)) {
            if ((this.isPreviousSessionRed(stockPrice) && this.isCloseBelowPrevClose(stockPrice))
                    || (this.isPreviousSessionGreen(stockPrice)
                            && this.isCloseBelowPrevOpen(stockPrice))) {
                return true;
            }
        }

        return this.isShootingStar(stockPrice);
    }

    @Override
    public boolean isHangingMan(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        double bodySize = this.currentBodySize(stockPrice);
        double lowerWickSize = this.lowerWickSize(stockPrice);
        double upperWickSize = this.upperWickSize(stockPrice);

        // Corrected condition for Hanging Man
        if (lowerWickSize < bodySize * 2 || upperWickSize > bodySize) {
            return false;
        }

        boolean isValid = this.isGreen(stockPrice) || this.isRed(stockPrice);

        if (isValid) {
            log.info("Hanging Man candle active");
        }

        return isValid;
    }

    @Override
    public boolean isHammer(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getOpen();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();

        // Calculate real body and shadows
        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        // Hammer criteria:
        // 1. Small real body (body should be relatively small compared to the total range)
        double totalRange = high - low;
        boolean hasSmallBody = realBody < totalRange * 0.3; // Body less than 30% of total range

        // 2. Long lower shadow (at least twice the height of real body)
        boolean hasLongLowerShadow = lowerShadow >= realBody * 3;

        // 3. Little or no upper shadow (less than 20% of real body)
        boolean hasSmallUpperShadow = (upperShadow / realBody) < 0.6;

        // 4. Additional validation: lower shadow should be significant (at least 1/3 of total
        // range)
        boolean hasSignificantLowerShadow = lowerShadow >= totalRange * 0.33;

        return hasSmallBody
                && hasLongLowerShadow
                && hasSmallUpperShadow
                && hasSignificantLowerShadow;
    }

    @Override
    public boolean isPrevHammer(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getPrevOpen();
        double high = stockPrice.getPrevHigh();
        double low = stockPrice.getPrevLow();
        double close = stockPrice.getPrevClose();

        // Calculate real body and shadows
        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        // Hammer criteria:
        // 1. Small real body (body should be relatively small compared to the total range)
        double totalRange = high - low;
        boolean hasSmallBody = realBody < totalRange * 0.3; // Body less than 30% of total range

        // 2. Long lower shadow (at least twice the height of real body)
        boolean hasLongLowerShadow = lowerShadow >= realBody * 3;

        // 3. Little or no upper shadow (less than 20% of real body)
        boolean hasSmallUpperShadow = (upperShadow / realBody) < 0.6;

        // 4. Additional validation: lower shadow should be significant (at least 1/3 of total
        // range)
        boolean hasSignificantLowerShadow = lowerShadow >= totalRange * 0.33;

        return hasSmallBody
                && hasLongLowerShadow
                && hasSmallUpperShadow
                && hasSignificantLowerShadow;
    }

    @Override
    public boolean isShootingStar(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getOpen();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();

        // Calculate real body and shadows
        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;
        double totalRange = high - low;

        // Avoid division by zero
        if (realBody == 0) {
            return false;
        }

        // Shooting Star criteria (mirror image of Hammer):
        // 1. Small real body (body should be relatively small compared to the total range)
        boolean hasSmallBody = realBody < totalRange * 0.3; // Body less than 30% of total range

        // 2. Long upper shadow (at least three times the height of real body)
        boolean hasLongUpperShadow = upperShadow >= realBody * 3;

        // 3. Little or no lower shadow (less than 60% of real body)
        boolean hasSmallLowerShadow = (lowerShadow / realBody) < 0.6;

        // 4. Additional validation: upper shadow should be significant (at least 1/3 of total
        // range)
        boolean hasSignificantUpperShadow = upperShadow >= totalRange * 0.33;

        return hasSmallBody
                && hasLongUpperShadow
                && hasSmallLowerShadow
                && hasSignificantUpperShadow;
    }

    @Override
    public boolean isPrevShootingStar(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getPrevOpen();
        double high = stockPrice.getPrevHigh();
        double low = stockPrice.getPrevLow();
        double close = stockPrice.getPrevClose();

        // Calculate real body and shadows
        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;
        double totalRange = high - low;

        // Avoid division by zero
        if (realBody == 0) {
            return false;
        }

        // Shooting Star criteria (mirror image of Hammer):
        // 1. Small real body (body should be relatively small compared to the total range)
        boolean hasSmallBody = realBody < totalRange * 0.3; // Body less than 30% of total range

        // 2. Long upper shadow (at least three times the height of real body)
        boolean hasLongUpperShadow = upperShadow >= realBody * 3;

        // 3. Little or no lower shadow (less than 60% of real body)
        boolean hasSmallLowerShadow = (lowerShadow / realBody) < 0.6;

        // 4. Additional validation: upper shadow should be significant (at least 1/3 of total
        // range)
        boolean hasSignificantUpperShadow = upperShadow >= totalRange * 0.33;

        return hasSmallBody
                && hasLongUpperShadow
                && hasSmallLowerShadow
                && hasSignificantUpperShadow;
    }

    @Override
    public boolean isInvertedHammer(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getOpen();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();

        // Calculate real body and shadows
        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        // Inverted Hammer criteria:
        // 1. Small real body (body should be relatively small compared to the total range)
        double totalRange = high - low;
        boolean hasSmallBody = realBody < totalRange * 0.3; // Body less than 30% of total range

        // 2. Long upper shadow (at least twice the height of real body)
        boolean hasLongUpperShadow = upperShadow >= realBody * 3;

        // 3. Little or no lower shadow (less than 20% of real body)
        boolean hasSmallLowerShadow = (lowerShadow / realBody) < 0.6;

        // 4. Additional validation: upper shadow should be significant (at least 1/3 of total
        // range)
        boolean hasSignificantUpperShadow = upperShadow >= totalRange * 0.33;

        return hasSmallBody
                && hasLongUpperShadow
                && hasSmallLowerShadow
                && hasSignificantUpperShadow;
    }

    @Override
    public boolean isPrevInvertedHammer(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate candlestick components
        double open = stockPrice.getPrevOpen();
        double high = stockPrice.getPrevHigh();
        double low = stockPrice.getPrevLow();
        double close = stockPrice.getPrevClose();

        // Calculate real body and shadows
        double realBody = Math.abs(close - open);
        double upperShadow = high - Math.max(open, close);
        double lowerShadow = Math.min(open, close) - low;

        // Inverted Hammer criteria:
        // 1. Small real body (body should be relatively small compared to the total range)
        double totalRange = high - low;
        boolean hasSmallBody = realBody < totalRange * 0.3; // Body less than 30% of total range

        // 2. Long upper shadow (at least twice the height of real body)
        boolean hasLongUpperShadow = upperShadow >= realBody * 3;

        // 3. Little or no lower shadow (less than 20% of real body)
        boolean hasSmallLowerShadow = (lowerShadow / realBody) < 0.6;

        // 4. Additional validation: upper shadow should be significant (at least 1/3 of total
        // range)
        boolean hasSignificantUpperShadow = upperShadow >= totalRange * 0.33;

        return hasSmallBody
                && hasLongUpperShadow
                && hasSmallLowerShadow
                && hasSignificantUpperShadow;
    }

    @Override
    public boolean isOpenHigh(StockPrice stockPrice) {

        double bodySize = this.currentBodySize(stockPrice);

        if (bodySize > FibonacciRatio.RATIO_261_8 && this.isOpenAndHighEqual(stockPrice)) {
            log.info("open high candle active");
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isOpenLow(StockPrice stockPrice) {
        double bodySize = this.currentBodySize(stockPrice);

        if (bodySize > FibonacciRatio.RATIO_261_8 && this.isOpenAndLowEqual(stockPrice)) {
            log.info("open low candle active");
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishEngulfing(StockPrice stockPrice) {
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        double currentBody = currentBodySize(stockPrice);
        double prevBody = prevBodySize(stockPrice);

        boolean isBearishPattern =
                currentBody > prevBody
                        && currentBody >= MIN_BODY_SIZE
                        && isOpenAbovePrevClose(stockPrice)
                        && isCloseBelowPrevOpen(stockPrice);

        if (isBearishPattern) {
            log.info("Bearish engulfing candle detected");
            return true;
        }

        return false;
    }

    public boolean isPrevBearishEngulfing(StockPrice stockPrice) {
        if (!isPreviousSessionRed(stockPrice) || !isPrevious2SessionGreen(stockPrice)) {
            return false;
        }

        double currentBody = prevBodySize(stockPrice);
        double prevBody = prev2BodySize(stockPrice);

        boolean isBearishPattern =
                currentBody > prevBody
                        && currentBody >= MIN_BODY_SIZE
                        && isOpenAbovePrevClose(stockPrice)
                        && isCloseBelowPrevOpen(stockPrice);

        if (isBearishPattern) {
            log.info("Bearish engulfing candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBullishEngulfing(StockPrice stockPrice) {

        if (stockPrice == null) return false;

        double prevOpen = stockPrice.getPrevOpen();
        double prevClose = stockPrice.getPrevClose();
        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();

        double currOpen = stockPrice.getOpen();
        double currClose = stockPrice.getClose();
        double currHigh = stockPrice.getHigh();
        double currLow = stockPrice.getLow();

        // 1. Previous candle bearish
        boolean firstBearish = prevClose < prevOpen;

        // 2. Current candle bullish
        boolean secondBullish = currClose > currOpen;

        // 3. Current real body engulfs previous real body
        boolean bodyEngulf = currOpen <= prevClose && currClose >= prevOpen;

        // 4. Avoid dojis
        double prevBody = Math.abs(prevOpen - prevClose);
        double currBody = Math.abs(currOpen - currClose);

        double prevRange = prevHigh - prevLow;
        double currRange = currHigh - currLow;

        boolean prevBodyOk = prevBody >= prevRange * 0.2; // min 20% of range
        boolean currBodyOk = currBody >= currRange * 0.4; // strong bullish body

        return firstBearish && secondBullish && bodyEngulf && prevBodyOk && currBodyOk;
    }

    public boolean isPrevBullishEngulfing(StockPrice stockPrice) {
        if (!isPreviousSessionGreen(stockPrice) || !isPrevious2SessionRed(stockPrice)) {
            return false;
        }

        double currentBody = prevBodySize(stockPrice);
        double prevBody = prev2BodySize(stockPrice);

        boolean isBullishPattern =
                currentBody > prevBody
                        && currentBody >= MIN_BODY_SIZE
                        && isOpenBelowPrevClose(stockPrice)
                        && isCloseAbovePrevOpen(stockPrice);

        if (isBullishPattern) {
            log.info("Bullish engulfing candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBullishOutsideBar(StockPrice stockPrice) {
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
            return false;
        }

        double range = range(stockPrice);
        double prevRange = prevRange(stockPrice);
        double currentBody = currentBodySize(stockPrice);
        double prevBody = prevBodySize(stockPrice);
        double lowerWick = lowerWickSize(stockPrice);
        double upperWick = upperWickSize(stockPrice);

        boolean isBullishPattern =
                range > prevRange
                        && range >= MIN_RANGE
                        && currentBody >= prevBody
                        && lowerWick > upperWick
                        && isLowerLow(stockPrice)
                        && isHigherHigh(stockPrice);

        if (isBullishPattern) {
            log.info("Bullish Outside Bar candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBearishOutsideBar(StockPrice stockPrice) {
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        double range = range(stockPrice);
        double prevRange = prevRange(stockPrice);
        double currentBody = currentBodySize(stockPrice);
        double prevBody = prevBodySize(stockPrice);
        double lowerWick = lowerWickSize(stockPrice);
        double upperWick = upperWickSize(stockPrice);

        boolean isBearishPattern =
                range > prevRange
                        && range >= MIN_RANGE
                        && currentBody >= prevBody
                        && lowerWick < upperWick
                        && isHigherHigh(stockPrice)
                        && isLowerLow(stockPrice);

        if (isBearishPattern) {
            log.info("Bearish Outside Bar candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBearishMarubozu(StockPrice stockPrice) {
        if (!isRed(stockPrice)
                || !isOpenAndHighEqual(stockPrice)
                || !isCloseAndLowEqual(stockPrice)) {
            return false;
        }

        if (currentBodySize(stockPrice) >= MIN_BODY_SIZE) {
            log.info("Bearish Marubozu candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBullishMarubozu(StockPrice stockPrice) {
        if (!isGreen(stockPrice)
                || !isOpenAndLowEqual(stockPrice)
                || !isCloseAndHighEqual(stockPrice)) {
            return false;
        }

        if (currentBodySize(stockPrice) >= MIN_BODY_SIZE) {
            log.info("Bullish Marubozu candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isTweezerTop(StockPrice stockPrice) {
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        if (!isOpenAtPrevClose(stockPrice) && !isOpenAtPrevOpen(stockPrice)) {
            return false;
        }

        if (currentBodySize(stockPrice) < MIN_BODY_SIZE
                || prevBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Tweezer Top candle detected");
        return true;
    }

    @Override
    public boolean isDoubleTop(StockPrice stockPrice) {
        if (stockPrice.getPrevHigh() == null) {
            return false; // Ensure previous session data exists
        }

        if (!isRed(stockPrice)) {
            return false; // Second candle must be red
        }

        if (!formulaService.isEpsilonEqual(
                stockPrice.getHigh(), stockPrice.getPrevHigh(), FibonacciRatio.RATIO_161_8)) {
            return false; // Highs must be nearly equal
        }

        if (range(stockPrice) < MIN_RANGE) {
            return false; // Second candle should have a significant range
        }

        log.info("Double High candle detected");
        return true;
    }

    @Override
    public boolean isTweezerBottom(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Tweezer Bottom criteria:
        // 1. Both candles should have similar lows (within 0.05%)
        double currentLow = stockPrice.getLow();
        double previousLow = stockPrice.getPrevLow();
        double lowDifference = Math.abs(currentLow - previousLow) / previousLow;
        boolean haveSimilarLows = lowDifference <= 0.001; // 0.1%

        // 2. First candle is typically bearish, second candle shows reversal
        boolean firstCandleBearish = stockPrice.getPrevClose() < stockPrice.getPrevOpen();
        boolean secondCandleBullish = stockPrice.getClose() > stockPrice.getOpen();

        // 3. The lows form a support level (almost equal lows)
        boolean formSupportLevel = haveSimilarLows;

        // 4. Additional: Current session should show buying pressure
        boolean showsBuyingPressure =
                stockPrice.getClose() > stockPrice.getOpen()
                        || (stockPrice.getClose() > stockPrice.getPrevClose());

        return formSupportLevel && showsBuyingPressure && firstCandleBearish && secondCandleBullish;
    }

    @Override
    public boolean isDoubleBottom(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Tweezer Bottom criteria:
        // 1. Both candles should have similar lows (within 0.05%)
        double currentOpen = stockPrice.getOpen();
        double previousClose = stockPrice.getPrevClose();
        double bottomDifference = Math.abs(currentOpen - previousClose) / previousClose;
        boolean haveSimilarBottom = bottomDifference <= 0.001; // 0.1%

        // 2. First candle is typically bearish, second candle shows reversal
        boolean firstCandleBearish = stockPrice.getPrevClose() < stockPrice.getPrevOpen();
        boolean secondCandleBullish = stockPrice.getClose() > stockPrice.getOpen();

        // 3. The lows form a support level (almost equal lows)
        boolean formSupportLevel = haveSimilarBottom;

        // 4. Additional: Current session should show buying pressure
        boolean showsBuyingPressure =
                stockPrice.getClose() > stockPrice.getOpen()
                        || (stockPrice.getClose() > stockPrice.getPrevClose());

        // 6. NEW: First candle should have a substantial body (not a doji or small body)
        double firstCandleBody = Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
        double firstCandleRange = stockPrice.getPrevHigh() - stockPrice.getPrevLow();
        boolean firstCandleSubstantialBody =
                firstCandleBody > firstCandleRange * 0.4; // At least 40% of range

        // 6. NEW: First candle should have a substantial body (not a doji or small body)
        double secondCandleBody = Math.abs(stockPrice.getClose() - stockPrice.getOpen());
        double secondCandleRange = stockPrice.getHigh() - stockPrice.getLow();
        boolean secondCandleSubstantialBody =
                secondCandleBody > secondCandleRange * 0.3; // At least 50% of range

        // boolean isHigherLow = CandleStickUtils.isHigherLow(stockPrice);

        return formSupportLevel && showsBuyingPressure && firstCandleBearish && secondCandleBullish;
    }

    @Override
    public boolean isDarkCloudCover(StockPrice stockPrice) {
        // Ensure the first candle is bullish (green) and the second is bearish (red)
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        // Calculate the midpoint of the previous candle’s body
        double prevMid = (stockPrice.getPrevOpen() + stockPrice.getPrevClose()) / 2;

        // Ensure the second candle opens above the previous close (gap up)
        if (stockPrice.getOpen() <= stockPrice.getPrevClose()) {
            return false;
        }

        // The second candle should close below the midpoint of the first candle
        if (stockPrice.getClose() > prevMid) {
            return false;
        }

        // Ensure both candles have a significant body size
        if (prevBodySize(stockPrice) < MIN_BODY_SIZE
                || currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Dark Cloud Cover pattern detected");
        return true;
    }

    @Override
    public boolean isPiercingPattern(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate components for both candles
        double prevOpen = stockPrice.getPrevOpen();
        double prevClose = stockPrice.getPrevClose();
        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();

        double currOpen = stockPrice.getOpen();
        double currClose = stockPrice.getClose();
        double currHigh = stockPrice.getHigh();
        double currLow = stockPrice.getLow();

        // Piercing Pattern criteria:

        // 1. First candle must be bearish (close < open)
        boolean firstCandleBearish = prevClose < prevOpen;

        // 2. Second candle must be bullish (close > open)
        boolean secondCandleBullish = currClose > currOpen;

        // 3. Second candle opens below first candle's close
        boolean opensBelowPreviousClose = currOpen < prevClose;

        // 4. Second candle closes above the midpoint of first candle's real body
        double firstCandleMidpoint = prevOpen + (prevClose - prevOpen) / 2;
        boolean closesAboveMidpoint = currClose > firstCandleMidpoint;

        // 5. Second candle closes below first candle's open (doesn't completely engulf)
        boolean closesBelowPreviousOpen = currClose < prevOpen;

        // 6. NEW: First candle should have a substantial body (not a doji or small body)
        double firstCandleBody = Math.abs(prevClose - prevOpen);
        double firstCandleRange = prevHigh - prevLow;
        boolean firstCandleSubstantialBody =
                firstCandleBody > firstCandleRange * 0.6; // At least 50% of range

        return firstCandleBearish
                && secondCandleBullish
                && opensBelowPreviousClose
                && closesAboveMidpoint
                && closesBelowPreviousOpen
                && firstCandleSubstantialBody;
    }

    // Helper method to check downtrend context

    @Override
    public boolean isBullishKicker(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate components for both candles
        double prevOpen = stockPrice.getPrevOpen();
        double prevClose = stockPrice.getPrevClose();
        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();

        double currOpen = stockPrice.getOpen();
        double currClose = stockPrice.getClose();
        double currHigh = stockPrice.getHigh();
        double currLow = stockPrice.getLow();

        // Bullish Kicker Pattern criteria:

        // 1. First candle can be any type (but often bearish in context)
        // 2. Second candle opens with a gap UP from first candle's body
        boolean hasGapUp = currOpen > prevHigh; // Opens above previous high

        // 3. Second candle is strongly bullish (large body)
        double currBody = Math.abs(currClose - currOpen);
        double currRange = currHigh - currLow;
        boolean isStrongBullishCandle = currBody > currRange * 0.6; // At least 60% body

        // 4. Second candle closes near its high (small or no upper shadow)
        double upperShadow = currHigh - currClose;
        boolean smallUpperShadow = upperShadow < currBody * 0.1; // Less than 10% of body

        // 5. The gap remains unfilled (current low > previous high)
        boolean gapRemainsUnfilled = currLow > prevHigh;

        // 6. Significant price move (avoid very small kicks)
        double gapSize = currOpen - prevHigh;
        double relativeGapSize = gapSize / prevHigh;
        boolean significantGap = relativeGapSize > 0.005; // At least 0.5% gap

        return hasGapUp
                && isStrongBullishCandle
                && smallUpperShadow
                && gapRemainsUnfilled
                && significantGap;
    }

    @Override
    public boolean isBearishKicker(StockPrice stockPrice) {
        // First candle must be green (bullish), second must be red (bearish)
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        // Ensure a strong gap down with no overlap
        if (!isGapDown(stockPrice) || stockPrice.getOpen() >= stockPrice.getPrevClose()) {
            return false;
        }

        // Ensure both candles have a strong body size
        if (prevBodySize(stockPrice) < MIN_BODY_SIZE
                || currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Bearish Kicker candle active");
        return true;
    }

    @Override
    public boolean isBullishSash(StockPrice stockPrice) {
        // First candle must be red (bearish), second must be green (bullish)
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
            return false;
        }

        // The second candle must open inside the previous candle’s body (not a gap up)
        if (stockPrice.getOpen() >= stockPrice.getPrevClose()
                || stockPrice.getOpen() <= stockPrice.getPrevLow()) {
            return false;
        }

        // The second candle must close above the first candle's open
        if (!isCloseAbovePrevOpen(stockPrice)) {
            return false;
        }

        // Ensure the second candle has a strong body size
        if (currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Bullish Sash candle active");
        return true;
    }

    @Override
    public boolean isBearishSash(StockPrice stockPrice) {
        // First candle must be green (bullish), second must be red (bearish)
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        // The second candle must open inside the previous candle’s body (not a gap down)
        if (stockPrice.getOpen() <= stockPrice.getPrevClose()
                || stockPrice.getOpen() >= stockPrice.getPrevHigh()) {
            return false;
        }

        // The second candle must close below the first candle's open
        if (!isCloseBelowPrevOpen(stockPrice)) {
            return false;
        }

        // Ensure the second candle has a strong body size
        if (currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Bearish Sash candle active");
        return true;
    }

    @Override
    public boolean isBullishSeparatingLine(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate components for both candles
        double prevOpen = stockPrice.getPrevOpen();
        double prevClose = stockPrice.getPrevClose();
        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();

        double currOpen = stockPrice.getOpen();
        double currClose = stockPrice.getClose();
        double currHigh = stockPrice.getHigh();
        double currLow = stockPrice.getLow();

        // Bullish Separating Line Pattern criteria:

        // 1. First candle is bearish (close < open)
        boolean firstCandleBearish = prevClose < prevOpen;

        // 2. Second candle is bullish (close > open)
        boolean secondCandleBullish = currClose > currOpen;

        // 3. Both candles have approximately the SAME OPENING price
        double openDifference = Math.abs(currOpen - prevOpen);
        double openTolerance = prevOpen * 0.001; // 0.1% tolerance
        boolean sameOpeningPrice = openDifference <= openTolerance;

        // 4. Second candle closes above first candle's close
        boolean closesAbovePreviousClose = currClose > prevClose;

        // 5. Both candles should have substantial bodies (not dojis)
        double prevBody = Math.abs(prevClose - prevOpen);
        double prevRange = prevHigh - prevLow;
        boolean prevSubstantialBody = prevBody > prevRange * 0.3;

        double currBody = Math.abs(currClose - currOpen);
        double currRange = currHigh - currLow;
        boolean currSubstantialBody = currBody > currRange * 0.3;

        return firstCandleBearish
                && secondCandleBullish
                && sameOpeningPrice
                && closesAbovePreviousClose
                && prevSubstantialBody
                && currSubstantialBody;
    }

    @Override
    public boolean isBearishSeparatingLine(StockPrice stockPrice) {
        // First candle must be green (bullish), second must be red (bearish)
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        // Second candle must open exactly at the previous open price (no gap)
        if (!isOpenAtPrevOpen(stockPrice)) {
            return false;
        }

        // The second candle must have a strong body size
        if (currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Bearish Separating Line candle active");
        return true;
    }

    @Override
    public boolean isBearishHarami(StockPrice stockPrice) {
        // First candle must be green (bullish), second must be red (bearish)
        if (!isRed(stockPrice) || !isPreviousSessionGreen(stockPrice)) {
            return false;
        }

        // First candle must have a larger body size than the second
        if (prevBodySize(stockPrice) <= currentBodySize(stockPrice)) {
            return false;
        }

        // Second candle's open and close must be inside the previous candle's body
        if (!isOpenInsidePrevBody(stockPrice) || !isCloseInsidePrevBody(stockPrice)) {
            return false;
        }

        log.info("Bearish Harami candle active");
        return true;
    }

    @Override
    public boolean isBullishHarami(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate components for both candles
        double prevOpen = stockPrice.getPrevOpen();
        double prevClose = stockPrice.getPrevClose();
        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();

        double currOpen = stockPrice.getOpen();
        double currClose = stockPrice.getClose();
        double currHigh = stockPrice.getHigh();
        double currLow = stockPrice.getLow();

        // Bullish Harami Pattern criteria:

        // 1. First candle is bearish (close < open) and has large body
        boolean firstCandleBearish = prevClose < prevOpen;
        double prevBody = Math.abs(prevClose - prevOpen);
        double prevRange = prevHigh - prevLow;
        boolean firstCandleLargeBody = prevBody > prevRange * 0.4; // At least 50% of range

        // 2. Second candle is bullish (close > open) and has small body
        boolean secondCandleBullish = currClose > currOpen;
        double currBody = Math.abs(currClose - currOpen);
        double currRange = currHigh - currLow;
        boolean secondCandleSmallBody =
                currBody > currRange * 0.1 && currBody < currRange * 0.4; // Less than 40% of range

        // 3. Second candle is completely inside first candle's range (high and low)
        boolean insideHigh = currHigh < prevHigh;
        boolean insideLow = currLow > prevLow;
        boolean completelyInside = insideHigh && insideLow;

        // 4. Second candle's body is inside first candle's body
        boolean bodyInside = currOpen > prevClose && currClose < prevOpen;

        return firstCandleBearish
                && secondCandleBullish
                && firstCandleLargeBody
                && secondCandleSmallBody
                && insideHigh
                && bodyInside;
    }

    public boolean isPrevBullishHarami(StockPrice stockPrice) {
        if (stockPrice == null) {
            return false;
        }

        // Calculate components for both candles
        double prevOpen = stockPrice.getPrev2Open();
        double prevClose = stockPrice.getPrev2Close();
        double prevHigh = stockPrice.getPrev2High();
        double prevLow = stockPrice.getPrev2Low();

        double currOpen = stockPrice.getPrevOpen();
        double currClose = stockPrice.getPrevClose();
        double currHigh = stockPrice.getPrevHigh();
        double currLow = stockPrice.getPrevLow();

        // Bullish Harami Pattern criteria:

        // 1. First candle is bearish (close < open) and has large body
        boolean firstCandleBearish = prevClose < prevOpen;
        double prevBody = Math.abs(prevClose - prevOpen);
        double prevRange = prevHigh - prevLow;
        boolean firstCandleLargeBody = prevBody > prevRange * 0.5; // At least 50% of range

        // 2. Second candle is bullish (close > open) and has small body
        boolean secondCandleBullish = currClose > currOpen;
        double currBody = Math.abs(currClose - currOpen);
        double currRange = currHigh - currLow;
        boolean secondCandleSmallBody =
                currBody > currRange * 0.1 && currBody < currRange * 0.4; // Less than 40% of range

        // 3. Second candle is completely inside first candle's range (high and low)
        boolean insideHigh = currHigh < prevHigh;
        boolean insideLow = currLow > prevLow;
        boolean completelyInside = insideHigh && insideLow;

        // 4. Second candle's body is inside first candle's body
        boolean bodyInside = currOpen > prevClose && currClose < prevOpen;

        return firstCandleBearish
                && secondCandleBullish
                && firstCandleLargeBody
                && secondCandleSmallBody
                && completelyInside
                && bodyInside;
    }

    @Override
    public boolean isBullishInsideBar(StockPrice stockPrice) {
        if (this.isLowerHigh(stockPrice) && this.isHigherLow(stockPrice)) { // Inside bar condition
            if (this.prevRange(stockPrice)
                    >= FibonacciRatio.RATIO_38_2 * 100) { // Ensures previous bar is significant
                if (this.isGreen(stockPrice)
                        || this.hasLongLowerWick(
                                stockPrice)) { // Accepts red bars with long lower wicks
                    log.info("Bullish Inside Bar candle active");
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishInsideBar(StockPrice stockPrice) {
        if (this.isRed(stockPrice)) {
            if (this.range(stockPrice) <= this.prevRange(stockPrice)
                    && this.prevRange(stockPrice) >= MIN_RANGE) {
                if (this.isLowerHigh(stockPrice) && this.isHigherLow(stockPrice)) {
                    log.info("Bearish Inside Bar candle active");
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isThreeWhiteSoldiers(StockPrice stockPrice) {
        if (this.isGreen(stockPrice)
                && this.isPreviousSessionGreen(stockPrice)
                && this.isSecondPreviousSessionGreen(stockPrice)) {
            if (this.isOpenInsidePrevBody(stockPrice)
                    && this.isPrevOpenInsideSecondPrevBody(stockPrice)) {
                if (this.isHigherClose(stockPrice) && this.isPrevHigherClose(stockPrice)) {
                    if (!this.hasLongUpperWick(stockPrice)
                            && !this.hasPrevLongUpperWick(stockPrice)
                            && !this.hasPrev2LongUpperWick(stockPrice)) {
                        log.info("Three White Soldiers pattern detected.");
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    public boolean isSecondPreviousSessionGreen(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrev2Close() > stockPrice.getPrev2Open();
    }

    public boolean isPrevOpenInsideSecondPrevBody(StockPrice stockPrice) {

        if (stockPrice == null) return false;

        double prevOpen = stockPrice.getPrevOpen();
        double secondPrevOpen = stockPrice.getPrev2Open();
        double secondPrevClose = stockPrice.getPrev2Close();

        return prevOpen >= Math.min(secondPrevOpen, secondPrevClose)
                && prevOpen <= Math.max(secondPrevOpen, secondPrevClose);
    }

    public boolean isHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() > stockPrice.getPrevClose();
    }

    public boolean isPrevHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() > stockPrice.getPrev2Close();
    }

    public boolean hasLongUpperWick(StockPrice stockPrice) {
        double upperWick =
                stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
        double bodySize = Math.abs(stockPrice.getClose() - stockPrice.getOpen());
        return upperWick > (bodySize * 0.5); // Wick should be more than 50% of the body size
    }

    public boolean hasPrevLongUpperWick(StockPrice stockPrice) {
        double upperWick =
                stockPrice.getPrevHigh()
                        - Math.max(stockPrice.getPrevOpen(), stockPrice.getPrevClose());
        double bodySize = Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
        return upperWick > (bodySize * 0.5); // Wick should be more than 50% of the body size
    }

    public boolean hasPrev2LongUpperWick(StockPrice stockPrice) {
        double upperWick =
                stockPrice.getPrev2High()
                        - Math.max(stockPrice.getPrev2Open(), stockPrice.getPrev2Close());
        double bodySize = Math.abs(stockPrice.getPrev2Close() - stockPrice.getPrev2Open());
        return upperWick > (bodySize * 0.5); // Wick should be more than 50% of the body size
    }

    @Override
    public boolean isThreeBlackCrows(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        // Ensure the last three candles are red (bearish)
        if (this.isRed(stockPrice)
                && this.isPreviousSessionRed(stockPrice)
                && this.isSecondPreviousSessionRed(stockPrice)) {

            // Each candle should open inside the previous candle’s body
            if (this.isOpenInsidePrevBody(stockPrice)
                    && this.isPrevOpenInsideSecondPrevBody(stockPrice)) {

                // Each close should be lower than the previous close
                if (this.isLowerClose(stockPrice) && this.isPrevLowerClose(stockPrice)) {

                    // Each candle should have a relatively small upper wick
                    if (!this.hasLongUpperWick(stockPrice)
                            && !this.hasPrevLongUpperWick(stockPrice)
                            && !this.hasPrev2LongUpperWick(stockPrice)) {

                        log.info("Three Black Crows pattern detected.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isSecondPreviousSessionRed(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrev2Close() < stockPrice.getPrev2Open();
    }

    public boolean isLowerClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() < stockPrice.getPrevClose();
    }

    public boolean isPrevLowerClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() < stockPrice.getPrev2Close();
    }

    @Override
    public boolean isThreeInsideUp(StockPrice stockPrice) {
        if (this.isPrevBullishHarami(stockPrice)) { // First two candles form a Bullish Harami
            if (this.isGreen(stockPrice)
                    && this.isHigherClose(stockPrice)) { // Third candle is green and closes higher
                log.info("Three Inside Up pattern active");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeInsideDown(StockPrice stockPrice) {
        if (!isSecondPreviousSessionGreen(stockPrice)) {
            return false;
        }
        if (!isPreviousSessionRed(stockPrice) || !isInsidePreviousCandle(stockPrice)) {
            return false;
        }
        if (!isRed(stockPrice) || !isCloseBelowSecondPrevOpen(stockPrice)) {
            return false;
        }

        log.info("Three Inside Down pattern active");
        return true;
    }

    public boolean isInsidePreviousCandle(StockPrice stockPrice) {

        if (stockPrice == null) {
            return false;
        }

        return stockPrice.getPrevHigh() <= stockPrice.getPrev2High()
                && stockPrice.getPrevLow() >= stockPrice.getPrev2Low();
    }

    public boolean isCloseBelowSecondPrevOpen(StockPrice stockPrice) {
        if (stockPrice == null
                || stockPrice.getClose() == null
                || stockPrice.getPrev2Open() == null) {
            return false;
        }

        return stockPrice.getClose() < stockPrice.getPrev2Open();
    }

    @Override
    public boolean isThreeOutsideUp(StockPrice stockPrice) {
        if (this.isPrevBullishEngulfing(stockPrice)) { // First two candles form a Bullish Engulfing
            if (this.isGreen(stockPrice)
                    && this.isHigherClose(stockPrice)) { // Third candle is green and closes higher
                log.info("Three Outside Up pattern active");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeOutsideDown(StockPrice stockPrice) {
        if (this.isPrevBearishEngulfing(stockPrice)) { // First two candles form a Bearish Engulfing
            if (this.isRed(stockPrice)
                    && this.isLowerClose(stockPrice)) { // Third candle is red and closes lower
                log.info("Three Outside Down pattern active");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMorningStar(StockPrice stockPrice) {
        if (!isPreviousSessionRed(stockPrice) || !isSecondPreviousSessionRed(stockPrice)) {
            return false;
        }
        if (!hasPrevSmallBody(stockPrice) || !isPrevGapDown(stockPrice)) {
            return false;
        }
        if (!isGreen(stockPrice) || !isCloseAbovePrevOpen(stockPrice)) {
            return false;
        }

        log.info("Morning Star pattern active");
        return true;
    }

    public boolean hasSmallBody(StockPrice stockPrice) {
        double bodySize = Math.abs(stockPrice.getClose() - stockPrice.getOpen());
        double range = stockPrice.getHigh() - stockPrice.getLow();

        return bodySize <= (range * 0.3); // A small body is typically ≤ 30% of the total range.
    }

    public boolean hasPrevSmallBody(StockPrice stockPrice) {
        double bodySize = Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
        double range = stockPrice.getPrevHigh() - stockPrice.getPrevLow();

        return bodySize <= (range * 0.3); // A small body is typically ≤ 30% of the total range.
    }

    @Override
    public boolean isEveningStar(StockPrice stockPrice) {
        if (!isPreviousSessionGreen(stockPrice) || !isSecondPreviousSessionGreen(stockPrice)) {
            return false;
        }
        if (!hasPrevSmallBody(stockPrice) || !isPrevGapUp(stockPrice)) {
            return false;
        }
        if (!isRed(stockPrice) || !isCloseBelowPrevOpen(stockPrice)) {
            return false;
        }

        log.info("Evening Star pattern active");
        return true;
    }

    @Override
    public boolean isRisingThreeMethods(StockPrice stockPrice) {
        /*
        if (this.isGreen(stockPrice) && this.isPreviousSessionRed(stockPrice) && this.isSecondPreviousSessionRed(stockPrice) && this.isThirdPreviousSessionRed(stockPrice)) {
            if (this.isOpenInsidePrevBody(stockPrice) && this.isPrevOpenInsideSecondPrevBody(stockPrice) && this.isSecondPrevOpenInsideThirdPrevBody(stockPrice)) {
                if (this.isHigherClose(stockPrice) && this.isPrevHigherClose(stockPrice) && this.isSecondPrevHigherClose(stockPrice)) {
                    if (!this.hasLongUpperWick(stockPrice) && !this.hasPrevLongUpperWick(stockPrice) && !this.hasPrev2LongUpperWick(stockPrice)) {
                        log.info("Rising Three Methods pattern detected.");
                        return Boolean.TRUE;
                    }
                }
            }
        }*/
        return Boolean.FALSE;
    }

    @Override
    public boolean isFallingThreeMethods(StockPrice stockPrice) {
        /*
        if (this.isRed(stockPrice) && this.isPreviousSessionGreen(stockPrice) && this.isSecondPreviousSessionGreen(stockPrice) && this.isThirdPreviousSessionGreen(stockPrice)) {
            if (this.isOpenInsidePrevBody(stockPrice) && this.isPrevOpenInsideSecondPrevBody(stockPrice) && this.isSecondPrevOpenInsideThirdPrevBody(stockPrice)) {
                if (this.isLowerClose(stockPrice) && this.isPrevLowerClose(stockPrice) && this.isSecondPrevLowerClose(stockPrice)) {
                    if (!this.hasLongLowerWick(stockPrice) && !this.hasPrevLongLowerWick(stockPrice) && !this.hasPrev2LongLowerWick(stockPrice)) {
                        log.info("Falling Three Methods pattern detected.");
                        return Boolean.TRUE;
                    }
                }
            }
        }*/
        return Boolean.FALSE;
    }
}
