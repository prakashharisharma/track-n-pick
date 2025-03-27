package com.example.service.impl;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.service.StockPriceService;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPriceOld;
import com.example.service.CandleStickService;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandleStickServiceImpl implements CandleStickService {
    private static final double TOLERANCE = 0.0001;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private StockPriceService<StockPrice> stockPriceService;

    @Override
    public boolean isDead(StockPrice stockPrice) {
        return stockPrice.getOpen().equals(stockPrice.getHigh()) &&
                stockPrice.getOpen().equals(stockPrice.getLow()) &&
                stockPrice.getOpen().equals(stockPrice.getClose());
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

        return formulaService.calculateChangePercentage(Math.min(open, close), Math.max(open, close));
    }

    @Override
    public double prevBodySize(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        double open = stockPrice.getPrevOpen();
        double close = stockPrice.getPrevClose();

        return formulaService.calculateChangePercentage(Math.min(open, close), Math.max(open, close));
    }

    public double prev2BodySize(StockPrice stockPrice) {
        if (stockPrice == null) {
            throw new IllegalArgumentException("StockPrice cannot be null");
        }

        double open = stockPrice.getPrev2Open();
        double close = stockPrice.getPrev2Close();

        return formulaService.calculateChangePercentage(Math.min(open, close), Math.max(open, close));
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
        return stockPrice.getOpen() > stockPrice.getPrevLow() && stockPrice.getOpen() < stockPrice.getPrevHigh();
    }

    public boolean isPrevOpenInsidePrev2Body(StockPrice stockPrice) {
        return stockPrice.getPrevOpen() > stockPrice.getPrev2Low() && stockPrice.getPrevOpen() < stockPrice.getPrev2High();
    }

    @Override
    public boolean isCloseInsidePrevBody(StockPrice stockPrice) {
        return stockPrice.getClose() > stockPrice.getPrevLow() && stockPrice.getClose() < stockPrice.getPrevHigh();
    }

    public boolean isPrevCloseInsidePrev2Body(StockPrice stockPrice) {
        return stockPrice.getPrevClose() > stockPrice.getPrev2Low() && stockPrice.getPrevClose() < stockPrice.getPrev2High();
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
            log.warn("StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false", low, prevLow);
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
            log.warn("StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false", low, prevLow);
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
            log.warn("StockPrice high or prevHigh is null (high: {}, prevHigh: {}), returning false", high, prevHigh);
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
            log.warn("StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false", low, prevLow);
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
            log.warn("StockPrice high or prevHigh is null (high: {}, prevHigh: {}), returning false", high, prevHigh);
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
            log.warn("StockPrice low or prevLow is null (low: {}, prevLow: {}), returning false", low, prevLow);
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
            log.warn("StockPrice contains null values (high: {}, open: {}, close: {}), returning false", high, open, close);
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
            log.warn("StockPrice contains null values (low: {}, open: {}, close: {}), returning false", low, open, close);
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
            log.warn("StockPrice is null, returning false");
            return false;
        }

        boolean isOpenCloseEqual = formulaService.isEpsilonEqual(
                stockPrice.getOpen(),
                stockPrice.getClose(),
                FibonacciRatio.RATIO_261_8
        );

        if (!isOpenCloseEqual) return false;

        double upperWick = this.upperWickSize(stockPrice);
        double lowerWick = this.lowerWickSize(stockPrice);
        double body = this.currentBodySize(stockPrice);

        boolean isUpperWickReasonable = upperWick <= lowerWick * 3;
        boolean isLowerWickReasonable = lowerWick <= upperWick * 3;
        boolean isUpperWickSignificant = upperWick >= body * 2;
        boolean isLowerWickSignificant = lowerWick >= body * 2;

        if (isUpperWickReasonable && isLowerWickReasonable &&
                isUpperWickSignificant && isLowerWickSignificant) {
            log.info("Doji candle detected.");
            return true;
        }

        return false;
    }

    @Override
    public boolean isPrevDoji(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        boolean isPrevOpenCloseEqual = formulaService.isEpsilonEqual(
                stockPrice.getPrevOpen(),
                stockPrice.getPrevClose(),
                FibonacciRatio.RATIO_261_8
        );

        if (!isPrevOpenCloseEqual) return false;

        double prevUpperWick = Math.abs(formulaService.calculateChangePercentage(stockPrice.getPrevClose(), stockPrice.getPrevHigh()));
        double prevLowerWick = Math.abs(formulaService.calculateChangePercentage(stockPrice.getPrevOpen(), stockPrice.getPrevLow()));
        double prevBody = Math.abs(formulaService.calculateChangePercentage(stockPrice.getPrevOpen(), stockPrice.getPrevClose()));

        boolean isPrevUpperWickReasonable = prevUpperWick <= prevLowerWick * 3;
        boolean isPrevLowerWickReasonable = prevLowerWick <= prevUpperWick * 3;
        boolean isPrevUpperWickSignificant = prevUpperWick >= prevBody * 2;
        boolean isPrevLowerWickSignificant = prevLowerWick >= prevBody * 2;

        if (isPrevUpperWickReasonable && isPrevLowerWickReasonable &&
                isPrevUpperWickSignificant && isPrevLowerWickSignificant) {
            log.info("Previous session is a Doji candle.");
            return true;
        }

        return false;
    }

    @Override
    public boolean isGravestoneDoji(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        boolean isOpenCloseEqual = formulaService.isEpsilonEqual(
                stockPrice.getOpen(),
                stockPrice.getClose(),
                FibonacciRatio.RATIO_261_8
        );

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

        boolean isOpenCloseEqual = formulaService.isEpsilonEqual(
                stockPrice.getOpen(),
                stockPrice.getClose(),
                FibonacciRatio.RATIO_261_8
        );

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

        boolean isOpenCloseEqual = formulaService.isEpsilonEqual(
                stockPrice.getOpen(),
                stockPrice.getClose(),
                FibonacciRatio.RATIO_261_8
        );

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

        boolean isOpenCloseEqual = formulaService.isEpsilonEqual(
                stockPrice.getOpen(),
                stockPrice.getClose(),
                FibonacciRatio.RATIO_261_8
        );

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
            log.warn("StockPrice is null, returning false");
            return false;
        }

        double bodySize = this.currentBodySize(stockPrice);
        if (bodySize <= FibonacciRatio.RATIO_261_8 || bodySize > MIN_BODY_SIZE) {
            return false;
        }

        double upperWick = this.upperWickSize(stockPrice);
        double lowerWick = this.lowerWickSize(stockPrice);

        boolean isWickBalanced = (upperWick <= lowerWick * 3) && (lowerWick <= upperWick * 3);
        boolean isWickSizeSignificant = (upperWick >= bodySize * 2) && (lowerWick >= bodySize * 2);

        if (isWickBalanced && isWickSizeSignificant) {
            log.info("Spinning Top detected.");
            return true;
        }

        return false;
    }

    @Override
    public boolean isPrevSpinningTop(StockPrice stockPrice) {
        if (stockPrice == null) {
            log.warn("StockPrice is null, returning false");
            return false;
        }

        double bodySize = Math.abs(stockPrice.getPrevOpen() - stockPrice.getPrevClose());
        if (bodySize <= FibonacciRatio.RATIO_261_8 || bodySize > MIN_BODY_SIZE) {
            return false;
        }

        double upperWick = stockPrice.getPrevHigh() - Math.max(stockPrice.getPrevOpen(), stockPrice.getPrevClose());
        double lowerWick = Math.min(stockPrice.getPrevOpen(), stockPrice.getPrevClose()) - stockPrice.getPrevLow();

        boolean isWickBalanced = (upperWick <= lowerWick * 3) && (lowerWick <= upperWick * 3);
        boolean isWickSizeSignificant = (upperWick >= bodySize * 2) && (lowerWick >= bodySize * 2);

        if (isWickBalanced && isWickSizeSignificant) {
            log.info("Previous session was a Spinning Top.");
            return true;
        }

        return false;
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

        if (!this.isGreen(stockPrice) || !this.isPrevInDecision(stockPrice) || this.isInDecision(stockPrice)) {
            return false;
        }

        if (this.isHigherHigh(stockPrice)) {
            if ((this.isPreviousSessionRed(stockPrice) && this.isCloseAbovePrevOpen(stockPrice)) ||
                    (this.isPreviousSessionGreen(stockPrice) && this.isCloseAbovePrevClose(stockPrice))) {
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

        if (!this.isRed(stockPrice) || !this.isPrevInDecision(stockPrice) || this.isInDecision(stockPrice)) {
            return false;
        }

        if (this.isLowerLow(stockPrice)) {
            if ((this.isPreviousSessionRed(stockPrice) && this.isCloseBelowPrevClose(stockPrice)) ||
                    (this.isPreviousSessionGreen(stockPrice) && this.isCloseBelowPrevOpen(stockPrice))) {
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
        return this.isHangingMan(stockPrice);
    }

    @Override
    public boolean isShootingStar(StockPrice stockPrice) {

        double bodySize = this.currentBodySize(stockPrice);
        double upperWick = this.upperWickSize(stockPrice);
        double lowerWick = this.lowerWickSize(stockPrice);

        if (upperWick > bodySize * 3 && lowerWick < bodySize && bodySize > FibonacciRatio.RATIO_261_8) {
            if (this.isRed(stockPrice) || (this.isGreen(stockPrice) && bodySize <= FibonacciRatio.RATIO_100_0 * 10)) {
                log.info("Shooting Star / Inverted Hammer candle active");
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isInvertedHammer(StockPrice stockPrice) {

        double bodySize = this.currentBodySize(stockPrice);
        double upperWick = this.upperWickSize(stockPrice);
        double lowerWick = this.lowerWickSize(stockPrice);

        if (upperWick > bodySize * 3 && lowerWick < bodySize && bodySize > FibonacciRatio.RATIO_261_8) {
            if (this.isGreen(stockPrice) || (this.isRed(stockPrice) && bodySize <= FibonacciRatio.RATIO_100_0 * 10)) {
                log.info("Inverted Hammer candle active");
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
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
                currentBody > prevBody &&
                        currentBody >= MIN_BODY_SIZE &&
                        isOpenAbovePrevClose(stockPrice) &&
                        isCloseBelowPrevOpen(stockPrice);

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
                currentBody > prevBody &&
                        currentBody >= MIN_BODY_SIZE &&
                        isOpenAbovePrevClose(stockPrice) &&
                        isCloseBelowPrevOpen(stockPrice);

        if (isBearishPattern) {
            log.info("Bearish engulfing candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBullishEngulfing(StockPrice stockPrice) {
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
            return false;
        }

        double currentBody = currentBodySize(stockPrice);
        double prevBody = prevBodySize(stockPrice);

        boolean isBullishPattern =
                currentBody > prevBody &&
                        currentBody >= MIN_BODY_SIZE &&
                        isOpenBelowPrevClose(stockPrice) &&
                        isCloseAbovePrevOpen(stockPrice);

        if (isBullishPattern) {
            log.info("Bullish engulfing candle detected");
            return true;
        }

        return false;
    }

    public boolean isPrevBullishEngulfing(StockPrice stockPrice) {
        if (!isPreviousSessionGreen(stockPrice) || !isPrevious2SessionRed(stockPrice)) {
            return false;
        }

        double currentBody = prevBodySize(stockPrice);
        double prevBody = prev2BodySize(stockPrice);

        boolean isBullishPattern =
                currentBody > prevBody &&
                        currentBody >= MIN_BODY_SIZE &&
                        isOpenBelowPrevClose(stockPrice) &&
                        isCloseAbovePrevOpen(stockPrice);

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
                range > prevRange &&
                        range >= MIN_RANGE &&
                        currentBody >= prevBody &&
                        lowerWick > upperWick &&
                        isLowerLow(stockPrice) &&
                        isHigherHigh(stockPrice);

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
                range > prevRange &&
                        range >= MIN_RANGE &&
                        currentBody >= prevBody &&
                        lowerWick < upperWick &&
                        isHigherHigh(stockPrice) &&
                        isLowerLow(stockPrice);

        if (isBearishPattern) {
            log.info("Bearish Outside Bar candle detected");
            return true;
        }

        return false;
    }

    @Override
    public boolean isBearishMarubozu(StockPrice stockPrice) {
        if (!isRed(stockPrice) || !isOpenAndHighEqual(stockPrice) || !isCloseAndLowEqual(stockPrice)) {
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
        if (!isGreen(stockPrice) || !isOpenAndLowEqual(stockPrice) || !isCloseAndHighEqual(stockPrice)) {
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

        if (currentBodySize(stockPrice) < MIN_BODY_SIZE || prevBodySize(stockPrice) < MIN_BODY_SIZE) {
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

        if (!formulaService.isEpsilonEqual(stockPrice.getHigh(), stockPrice.getPrevHigh(), FibonacciRatio.RATIO_161_8)) {
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
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
            return false; // Ensure second candle is green and first is red
        }

        if (!isOpenAtPrevClose(stockPrice) && !isOpenAtPrevOpen(stockPrice)) {
            return false; // Second candle should open at previous close or open
        }

        if (currentBodySize(stockPrice) < MIN_BODY_SIZE || prevBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false; // Ensure both candles have significant bodies
        }

        log.info("Tweezer Bottom candle detected");
        return true;
    }


    @Override
    public boolean isDoubleBottom(StockPrice stockPrice) {
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
            return false; // Ensure second candle is green and first is red
        }

        // Check if the second low is nearly equal to the first low (support level)
        if (!formulaService.isEpsilonEqual(stockPrice.getLow(), stockPrice.getPrevLow(), FibonacciRatio.RATIO_161_8)) {
            return false;
        }

        // Ensure a significant range (avoid weak candles)
        if (range(stockPrice) < MIN_RANGE || prevRange(stockPrice) < MIN_RANGE) {
            return false;
        }

        // Confirmation: The close should be above the midpoint of the range
        double neckline = (stockPrice.getPrevHigh() + stockPrice.getPrevLow()) / 2;
        if (stockPrice.getClose() < neckline) {
            return false; // No strong breakout yet
        }

        log.info("Double Low (Double Bottom) pattern detected");
        return true;
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
        if (prevBodySize(stockPrice) < MIN_BODY_SIZE || currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Dark Cloud Cover pattern detected");
        return true;
    }

    @Override
    public boolean isPiercingPattern(StockPrice stockPrice) {
        // Ensure the first candle is bearish (red) and the second is bullish (green)
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
            return false;
        }

        // Calculate the midpoint of the previous candle’s body
        double prevMid = (stockPrice.getPrevOpen() + stockPrice.getPrevClose()) / 2;

        // Ensure the second candle opens below the previous close (gap down)
        if (stockPrice.getOpen() >= stockPrice.getPrevClose()) {
            return false;
        }

        // The second candle should close above the midpoint of the first candle
        if (stockPrice.getClose() < prevMid) {
            return false;
        }

        // Ensure both candles have a significant body size
        if (prevBodySize(stockPrice) < MIN_BODY_SIZE || currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        // Ensure the close is below the previous open but not necessarily lower than the previous high
        if (stockPrice.getClose() >= stockPrice.getPrevOpen()) {
            return false;
        }

        log.info("Piercing Pattern detected");
        return true;
    }



    @Override
    public boolean isBullishKicker(StockPrice stockPrice) {
        // First candle must be red (bearish), second must be green (bullish)
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
            return false;
        }

        // Ensure a strong gap up with no overlap
        if (!isGapUp(stockPrice) || stockPrice.getOpen() <= stockPrice.getPrevClose()) {
            return false;
        }

        // Ensure both candles have a strong body size
        if (prevBodySize(stockPrice) < MIN_BODY_SIZE || currentBodySize(stockPrice) < MIN_BODY_SIZE) {
            return false;
        }

        log.info("Bullish Kicker candle active");
        return true;
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
        if (prevBodySize(stockPrice) < MIN_BODY_SIZE || currentBodySize(stockPrice) < MIN_BODY_SIZE) {
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
        if (stockPrice.getOpen() >= stockPrice.getPrevClose() || stockPrice.getOpen() <= stockPrice.getPrevLow()) {
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
        if (stockPrice.getOpen() <= stockPrice.getPrevClose() || stockPrice.getOpen() >= stockPrice.getPrevHigh()) {
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
        // First candle must be red (bearish), second must be green (bullish)
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
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

        log.info("Bullish Separating Line candle active");
        return true;
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
        // First candle must be red (bearish), second must be green (bullish)
        if (!isGreen(stockPrice) || !isPreviousSessionRed(stockPrice)) {
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

        log.info("Bullish Harami candle active");
        return true;
    }

    public boolean isPrevBullishHarami(StockPrice stockPrice) {
        // First candle must be red (bearish), second must be green (bullish)
        if (!isPreviousSessionGreen(stockPrice) || !isPrevious2SessionRed(stockPrice)) {
            return false;
        }

        // First candle must have a larger body size than the second
        if (prev2BodySize(stockPrice) <= prevBodySize(stockPrice)) {
            return false;
        }

        // Second candle's open and close must be inside the previous candle's body
        if (!isPrevOpenInsidePrev2Body(stockPrice) || !isPrevCloseInsidePrev2Body(stockPrice)) {
            return false;
        }

        log.info("Bullish Harami candle active");
        return true;
    }


    @Override
    public boolean isBullishInsideBar(StockPrice stockPrice) {
        if (this.isLowerHigh(stockPrice) && this.isHigherLow(stockPrice)) { // Inside bar condition
            if (this.prevRange(stockPrice) >= FibonacciRatio.RATIO_38_2 * 100) { // Ensures previous bar is significant
                if (this.isGreen(stockPrice) || this.hasLongLowerWick(stockPrice)) { // Accepts red bars with long lower wicks
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
            if (this.range(stockPrice) <= this.prevRange(stockPrice) && this.prevRange(stockPrice) >= MIN_RANGE) {
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
        if (this.isGreen(stockPrice) && this.isPreviousSessionGreen(stockPrice) && this.isSecondPreviousSessionGreen(stockPrice)) {
            if (this.isOpenInsidePrevBody(stockPrice) && this.isPrevOpenInsideSecondPrevBody(stockPrice)) {
                if (this.isHigherClose(stockPrice) && this.isPrevHigherClose(stockPrice)) {
                    if (!this.hasLongUpperWick(stockPrice) && !this.hasPrevLongUpperWick(stockPrice) && !this.hasPrev2LongUpperWick(stockPrice)) {
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


        if (stockPrice == null ) return false;

        double prevOpen = stockPrice.getPrevOpen();
        double secondPrevOpen = stockPrice.getPrev2Open();
        double secondPrevClose = stockPrice.getPrev2Close();

        return prevOpen >= Math.min(secondPrevOpen, secondPrevClose) && prevOpen <= Math.max(secondPrevOpen, secondPrevClose);
    }


    public boolean isHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getClose() > stockPrice.getPrevClose();
    }

    public boolean isPrevHigherClose(StockPrice stockPrice) {
        return stockPrice != null && stockPrice.getPrevClose() > stockPrice.getPrev2Close();
    }

    public boolean hasLongUpperWick(StockPrice stockPrice) {
        double upperWick = stockPrice.getHigh() - Math.max(stockPrice.getOpen(), stockPrice.getClose());
        double bodySize = Math.abs(stockPrice.getClose() - stockPrice.getOpen());
        return upperWick > (bodySize * 0.5);  // Wick should be more than 50% of the body size
    }

    public boolean hasPrevLongUpperWick(StockPrice stockPrice) {
        double upperWick = stockPrice.getPrevHigh() - Math.max(stockPrice.getPrevOpen(), stockPrice.getPrevClose());
        double bodySize = Math.abs(stockPrice.getPrevClose() - stockPrice.getPrevOpen());
        return upperWick > (bodySize * 0.5);  // Wick should be more than 50% of the body size
    }

    public boolean hasPrev2LongUpperWick(StockPrice stockPrice) {
        double upperWick = stockPrice.getPrev2High() - Math.max(stockPrice.getPrev2Open(), stockPrice.getPrev2Close());
        double bodySize = Math.abs(stockPrice.getPrev2Close() - stockPrice.getPrev2Open());
        return upperWick > (bodySize * 0.5);  // Wick should be more than 50% of the body size
    }

    @Override
    public boolean isThreeBlackCrows(StockPrice stockPrice) {
        if (stockPrice == null) return false;

        // Ensure the last three candles are red (bearish)
        if (this.isRed(stockPrice) &&
                this.isPreviousSessionRed(stockPrice) &&
                this.isSecondPreviousSessionRed(stockPrice)) {

            // Each candle should open inside the previous candle’s body
            if (this.isOpenInsidePrevBody(stockPrice) &&
                    this.isPrevOpenInsideSecondPrevBody(stockPrice)) {

                // Each close should be lower than the previous close
                if (this.isLowerClose(stockPrice) &&
                        this.isPrevLowerClose(stockPrice)) {

                    // Each candle should have a relatively small upper wick
                    if (!this.hasLongUpperWick(stockPrice) &&
                            !this.hasPrevLongUpperWick(stockPrice) &&
                            !this.hasPrev2LongUpperWick(stockPrice)) {

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
            if (this.isGreen(stockPrice) && this.isHigherClose(stockPrice)) { // Third candle is green and closes higher
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

        return stockPrice.getPrevHigh() <= stockPrice.getPrev2High() && stockPrice.getPrevLow() >= stockPrice.getPrev2Low();
    }

    public boolean isCloseBelowSecondPrevOpen(StockPrice stockPrice) {
        if (stockPrice == null || stockPrice.getClose() == null || stockPrice.getPrev2Open() == null) {
            return false;
        }

        return stockPrice.getClose() < stockPrice.getPrev2Open();
    }

    @Override
    public boolean isThreeOutsideUp(StockPrice stockPrice) {
        if (this.isPrevBullishEngulfing(stockPrice)) { // First two candles form a Bullish Engulfing
            if (this.isGreen(stockPrice) && this.isHigherClose(stockPrice)) { // Third candle is green and closes higher
                log.info("Three Outside Up pattern active");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeOutsideDown(StockPrice stockPrice) {
        if (this.isPrevBearishEngulfing(stockPrice)) { // First two candles form a Bearish Engulfing
            if (this.isRed(stockPrice) && this.isLowerClose(stockPrice)) { // Third candle is red and closes lower
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
