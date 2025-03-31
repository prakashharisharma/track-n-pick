package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.ThreeSessionCandleStickService;
import com.example.service.TwoSessionCandleStickService;
import com.example.service.utils.CandleStickUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ThreeSessionCandleStickServiceImpl implements ThreeSessionCandleStickService {

    private final TwoSessionCandleStickService twoSessionCandleStickService;

    @Override
    public boolean isThreeWhiteSoldiers(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean threeGreenCandles = CandleStickUtils.isGreen(stockPrice) &&
                CandleStickUtils.isPrevSessionGreen(stockPrice) &&
                CandleStickUtils.isPrev2SessionGreen(stockPrice);

        boolean opensWithinPrevBodies = CandleStickUtils.isOpenInsidePrevBody(stockPrice) &&
                CandleStickUtils.isPrevOpenInsideSecondPrevBody(stockPrice);

        boolean closesHigher = CandleStickUtils.isHigherClose(stockPrice) &&
                CandleStickUtils.isPrevHigherClose(stockPrice);

        boolean noLongUpperWicks = !CandleStickUtils.hasLongUpperWick(stockPrice) &&
                !CandleStickUtils.hasPrevLongUpperWick(stockPrice) &&
                !CandleStickUtils.hasPrev2LongUpperWick(stockPrice);

        if (threeGreenCandles && opensWithinPrevBodies && closesHigher && noLongUpperWicks) {
            log.info("Three White Soldiers pattern detected.");
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isThreeInsideUp(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (twoSessionCandleStickService.isPrevSessionBullishHarami(timeframe, stockPrice, stockTechnicals)) { // First two candles form a Bullish Harami
            if (CandleStickUtils.isGreen(stockPrice) && CandleStickUtils.isHigherClose(stockPrice)) { // Third candle is green and closes higher
                log.info("Three Inside Up pattern active");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeOutsideUp(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (twoSessionCandleStickService.isPrevSessionBullishEngulfing(timeframe, stockPrice, stockTechnicals)) { // First two candles form a Bullish Engulfing
            if (CandleStickUtils.isGreen(stockPrice) && CandleStickUtils.isHigherClose(stockPrice)) { // Third candle is green and closes higher
                log.info("Three Outside Up pattern active");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMorningStar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (CandleStickUtils.isPrev2SessionRed(stockPrice) &&  // First candle is a strong red candle (downtrend continuation)
                CandleStickUtils.isPrevSmallBody(stockPrice) &&  // Second candle has a small body (indecision)
                CandleStickUtils.isGreen(stockPrice) && CandleStickUtils.isHigherClose(stockPrice)) { // Third candle is a strong green candle closing above the first candle’s midpoint

            if (CandleStickUtils.isPrevGapDown(stockPrice) &&
                    CandleStickUtils.isGapUp(stockPrice)) { // Gaps between candles
                log.info("Morning Star pattern detected.");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeCandleTweezerBottom(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is bearish, second & third are bullish
        if (!CandleStickUtils.isPrev2SessionRed(stockPrice) ||
                !CandleStickUtils.isPrevSessionGreen(stockPrice) ||
                !CandleStickUtils.isGreen(stockPrice)) {
            return false;
        }

        // Compare lows and body lows
        double prevLow = stockPrice.getPrevLow();
        double prev2Low = stockPrice.getPrev2Low();
        double currentLow = stockPrice.getLow();

        boolean isEqualLows = Math.abs(prevLow - currentLow) / prevLow <= 0.005
                && Math.abs(prev2Low - prevLow) / prev2Low <= 0.005;

        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        double currentOpen = stockPrice.getOpen();

        boolean isEqualBodyLows = Math.abs(prevClose - currentOpen) / prevClose <= 0.005
                && Math.abs(prev2Close - prevClose) / prev2Close <= 0.005;

        // Strong bullish confirmation: Third candle closes above second candle
        boolean isBullishReversal = stockPrice.getClose() > stockPrice.getPrevClose();

        if ((isEqualLows || isEqualBodyLows) && isBullishReversal) {
            log.info("{}: Three-Candle Tweezer Bottom detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }


    @Override
    public boolean isThreeBlackCrows(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean threeRedCandles = CandleStickUtils.isRed(stockPrice) &&
                CandleStickUtils.isPrevSessionRed(stockPrice) &&
                CandleStickUtils.isPrev2SessionRed(stockPrice);

        boolean opensWithinPrevBodies = CandleStickUtils.isOpenInsidePrevBody(stockPrice) &&
                CandleStickUtils.isPrevOpenInsideSecondPrevBody(stockPrice);

        boolean closesLower = CandleStickUtils.isLowerClose(stockPrice) &&
                CandleStickUtils.isPrevLowerClose(stockPrice);

        boolean noLongLowerWicks = !CandleStickUtils.hasLongLowerWick(stockPrice) &&
                !CandleStickUtils.hasPrevLongLowerWick(stockPrice) &&
                !CandleStickUtils.hasPrev2LongLowerWick(stockPrice);

        if (threeRedCandles && opensWithinPrevBodies && closesLower && noLongLowerWicks) {
            log.info("Three Black Crows pattern detected.");
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isThreeInsideDown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (twoSessionCandleStickService.isPrevSessionBearishHarami(timeframe, stockPrice, stockTechnicals)) { // First two candles form a Bearish Harami
            if (CandleStickUtils.isRed(stockPrice) && CandleStickUtils.isLowerClose(stockPrice)) { // Third candle is red and closes lower
                log.info("Three Inside Down pattern detected.");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeOutsideDown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (twoSessionCandleStickService.isPrevSessionBearishEngulfing(timeframe, stockPrice, stockTechnicals)) { // First two candles form a Bearish Engulfing
            if (CandleStickUtils.isRed(stockPrice) && CandleStickUtils.isLowerClose(stockPrice)) { // Third candle is red and closes lower
                log.info("Three Outside Down pattern detected.");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEveningStar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (CandleStickUtils.isPrev2SessionGreen(stockPrice) &&  // First candle is a strong green candle (uptrend continuation)
                CandleStickUtils.isPrevSmallBody(stockPrice) &&  // Second candle has a small body (indecision)
                CandleStickUtils.isRed(stockPrice) && CandleStickUtils.isLowerClose(stockPrice)) { // Third candle is a strong red candle closing below the first candle’s midpoint

            if (CandleStickUtils.isPrevGapUp(stockPrice) &&
                    CandleStickUtils.isGapDown(stockPrice)) { // Gaps between candles
                log.info("Evening Star pattern detected.");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeCandleTweezerTop(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is bullish, second & third are bearish
        if (!CandleStickUtils.isPrev2SessionGreen(stockPrice) ||
                !CandleStickUtils.isPrevSessionRed(stockPrice) ||
                !CandleStickUtils.isRed(stockPrice)) {
            return false;
        }

        // Compare highs and body highs
        double prevHigh = stockPrice.getPrevHigh();
        double prev2High = stockPrice.getPrev2High();
        double currentHigh = stockPrice.getHigh();

        boolean isEqualHighs = Math.abs(prevHigh - currentHigh) / prevHigh <= 0.005
                && Math.abs(prev2High - prevHigh) / prev2High <= 0.005;

        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        double currentOpen = stockPrice.getOpen();

        boolean isEqualBodyHighs = Math.abs(prevClose - currentOpen) / prevClose <= 0.005
                && Math.abs(prev2Close - prevClose) / prev2Close <= 0.005;

        // Strong bearish confirmation: Third candle closes below second candle
        boolean isBearishReversal = stockPrice.getClose() < stockPrice.getPrevClose();

        if ((isEqualHighs || isEqualBodyHighs) && isBearishReversal) {
            log.info("{}: Three-Candle Tweezer Top detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

}
