package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.CandlestickPattern;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.CandlestickPatternService;
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

    private final CandlestickPatternService candlestickPatternService;

    @Override
    public boolean isThreeWhiteSoldiers(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // 1) Three consecutive bullish candles
        boolean threeGreenCandles =
                CandleStickUtils.isGreen(stockPrice)
                        && CandleStickUtils.isPrevSessionGreen(stockPrice)
                        && CandleStickUtils.isPrev2SessionGreen(stockPrice);

        // 2) Each open within the previous candle’s body
        boolean opensWithinPrevBodies =
                CandleStickUtils.isOpenInsidePrevBody(stockPrice)
                        && CandleStickUtils.isPrevOpenInsideSecondPrevBody(stockPrice);

        // 3) Each close higher than the previous close
        boolean closesHigher =
                CandleStickUtils.isHigherClose(stockPrice)
                        && CandleStickUtils.isPrevHigherClose(stockPrice);

        // 4) No long upper wicks on any of the three candles
        boolean noLongUpperWicks =
                !CandleStickUtils.hasLongUpperWick(stockPrice)
                        && !CandleStickUtils.hasPrevLongUpperWick(stockPrice)
                        && !CandleStickUtils.hasPrev2LongUpperWick(stockPrice);

        boolean isThreeWhiteSoldiers =
                threeGreenCandles && opensWithinPrevBodies && closesHigher && noLongUpperWicks;

        if (isThreeWhiteSoldiers) {
            log.info(
                    "{}: Three White Soldiers pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(CandlestickPattern.Name.THREE_WHITE_SOLDIERS)
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(true)
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isThreeWhiteSoldiers;
    }

    @Override
    public boolean isThreeInsideUp(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // 1) First two candles form a bullish Harami
        boolean harami =
                twoSessionCandleStickService.isPrevSessionBullishHarami(
                        timeframe, stockPrice, stockTechnicals);

        // 2) Third candle is bullish and closes above the previous close
        boolean thirdBullish =
                CandleStickUtils.isGreen(stockPrice) && CandleStickUtils.isHigherClose(stockPrice);

        boolean isThreeInsideUp = harami && thirdBullish;

        if (isThreeInsideUp) {
            log.info(
                    "{}: Three Inside Up pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build and save the pattern
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(CandlestickPattern.Name.THREE_INSIDE_UP) // add to enum
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(true)
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isThreeInsideUp;
    }

    @Override
    public boolean isThreeOutsideUp(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // 1) First two candles form a bullish Engulfing
        boolean engulfing =
                twoSessionCandleStickService.isPrevSessionBullishEngulfing(
                        timeframe, stockPrice, stockTechnicals);

        // 2) Third candle is bullish and closes above the previous close
        boolean thirdBullish =
                CandleStickUtils.isGreen(stockPrice) && CandleStickUtils.isHigherClose(stockPrice);

        boolean isThreeOutsideUp = engulfing && thirdBullish;

        if (isThreeOutsideUp) {
            log.info(
                    "{}: Three Outside Up pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(CandlestickPattern.Name.THREE_OUTSIDE_UP) // add to enum
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(true)
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isThreeOutsideUp;
    }

    @Override
    public boolean isMorningStar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // 1) First candle: strong bearish (red) – downtrend continuation
        boolean firstBearish =
                CandleStickUtils.isPrev2SessionRed(stockPrice)
                        && CandleStickUtils.isPrev2SessionStrongBody(
                                timeframe, stockPrice, stockTechnicals);

        // 2) Second candle: small body (indecision)
        boolean secondSmallBody = CandleStickUtils.isPrevSmallBody(stockPrice, stockTechnicals) || CandleStickUtils.isPrevVerySmallBody(stockPrice);

        // 3) Third candle: strong bullish (green), closing above the midpoint of the first candle
        double firstOpen = stockPrice.getPrev2Open();
        double firstClose = stockPrice.getPrev2Close();
        double firstMid = (firstOpen + firstClose) / 2;
        boolean thirdBullish = CandleStickUtils.isGreen(stockPrice);
        boolean thirdClosesHigh = stockPrice.getClose() > firstMid;
        boolean thirdStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // 4) Gaps: gap down into the second candle, gap up into the third
        boolean gapDown = CandleStickUtils.isPrevGapDown(stockPrice);
        boolean gapUp = CandleStickUtils.isGapUp(stockPrice);

        boolean isMorningStar =
                firstBearish
                        && secondSmallBody
                        && thirdBullish
                        && thirdStrongBody
                        && thirdClosesHigh
                        && gapDown
                        && gapUp;

        if (isMorningStar) {
            log.info(
                    "{}: Morning Star pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(CandlestickPattern.Name.MORNING_STAR) // ensure enum exists
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(thirdStrongBody)
                            .isSmallBody(secondSmallBody)
                            .isGapUp(gapUp)
                            .isGapDown(gapDown)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isMorningStar;
    }

    @Override
    public boolean isThreeCandleTweezerBottom(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is bearish, second & third are bullish
        if (!CandleStickUtils.isPrev2SessionRed(stockPrice)
                || !CandleStickUtils.isPrevSessionGreen(stockPrice)
                || !CandleStickUtils.isGreen(stockPrice)) {
            return false;
        }

        // Compare lows and body lows
        double prevLow = stockPrice.getPrevLow();
        double prev2Low = stockPrice.getPrev2Low();
        double currentLow = stockPrice.getLow();

        boolean isEqualLows =
                Math.abs(prevLow - currentLow) / prevLow <= 0.005
                        && Math.abs(prev2Low - prevLow) / prev2Low <= 0.005;

        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        double currentOpen = stockPrice.getOpen();

        boolean isEqualBodyLows =
                Math.abs(prevClose - currentOpen) / prevClose <= 0.005
                        && Math.abs(prev2Close - prevClose) / prev2Close <= 0.005;

        // Strong bullish confirmation: Third candle closes above second candle
        boolean isBullishReversal = stockPrice.getClose() > stockPrice.getPrevClose();

        if ((isEqualLows || isEqualBodyLows) && isBullishReversal) {
            log.info(
                    "{}: Three-Candle Tweezer Bottom detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(
                                    CandlestickPattern.Name
                                            .THREE_CANDLE_TWEEZER_BOTTOM) // Ensure enum exists
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(
                                    CandleStickUtils.isStrongBody(
                                            timeframe, stockPrice, stockTechnicals))
                            .isSmallBody(CandleStickUtils.isSmallBody(stockPrice, stockTechnicals))
                            .isGapUp(CandleStickUtils.isGapUp(stockPrice))
                            .isGapDown(CandleStickUtils.isPrevGapDown(stockPrice))
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isThreeBlackCrows(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean threeRedCandles =
                CandleStickUtils.isRed(stockPrice)
                        && CandleStickUtils.isPrevSessionRed(stockPrice)
                        && CandleStickUtils.isPrev2SessionRed(stockPrice);

        boolean opensWithinPrevBodies =
                CandleStickUtils.isOpenInsidePrevBody(stockPrice)
                        && CandleStickUtils.isPrevOpenInsideSecondPrevBody(stockPrice);

        boolean closesLower =
                CandleStickUtils.isLowerClose(stockPrice)
                        && CandleStickUtils.isPrevLowerClose(stockPrice);

        boolean noLongLowerWicks =
                !CandleStickUtils.hasLongLowerWick(stockPrice)
                        && !CandleStickUtils.hasPrevLongLowerWick(stockPrice)
                        && !CandleStickUtils.hasPrev2LongLowerWick(stockPrice);

        if (threeRedCandles && opensWithinPrevBodies && closesLower && noLongLowerWicks) {
            log.info("Three Black Crows pattern detected on {}", stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(CandlestickPattern.Name.THREE_BLACK_CROWS) // Ensure enum exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(
                                    CandleStickUtils.isStrongBody(
                                            timeframe, stockPrice, stockTechnicals))
                            .isSmallBody(CandleStickUtils.isSmallBody(stockPrice, stockTechnicals))
                            .isGapUp(CandleStickUtils.isGapUp(stockPrice))
                            .isGapDown(CandleStickUtils.isPrevGapDown(stockPrice))
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }

    @Override
    public boolean isThreeInsideDown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        if (twoSessionCandleStickService.isPrevSessionBearishHarami(
                timeframe,
                stockPrice,
                stockTechnicals)) { // First two candles form a Bearish Harami

            if (CandleStickUtils.isRed(stockPrice)
                    && CandleStickUtils.isLowerClose(
                            stockPrice)) { // Third candle is red and closes lower

                log.info("Three Inside Down pattern detected on {}", stockPrice.getSessionDate());

                CandlestickPattern pattern =
                        CandlestickPattern.builder()
                                .stockPrice(stockPrice)
                                .sessionCount(CandlestickPattern.SessionCount.THREE)
                                .name(
                                        CandlestickPattern.Name
                                                .THREE_INSIDE_DOWN) // Ensure this enum exists
                                .sentiment(CandlestickPattern.Sentiment.BEARISH)
                                .isStrongBody(
                                        CandleStickUtils.isStrongBody(
                                                timeframe, stockPrice, stockTechnicals))
                                .isSmallBody(
                                        CandleStickUtils.isSmallBody(stockPrice, stockTechnicals))
                                .isGapUp(CandleStickUtils.isGapUp(stockPrice))
                                .isGapDown(CandleStickUtils.isPrevGapDown(stockPrice))
                                .atr(
                                        stockTechnicals.getAtr() != null
                                                ? stockTechnicals.getAtr()
                                                : 0.0)
                                .bodySize(CandleStickUtils.bodySize(stockPrice))
                                .rangeSize(CandleStickUtils.range(stockPrice))
                                .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                                .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                                .sessionDate(stockPrice.getSessionDate())
                                .build();

                candlestickPatternService.create(pattern);

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isThreeOutsideDown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        if (twoSessionCandleStickService.isPrevSessionBearishEngulfing(
                timeframe,
                stockPrice,
                stockTechnicals)) { // First two candles form a Bearish Engulfing

            if (CandleStickUtils.isRed(stockPrice)
                    && CandleStickUtils.isLowerClose(
                            stockPrice)) { // Third candle is red and closes lower

                log.info("Three Outside Down pattern detected on {}", stockPrice.getSessionDate());

                CandlestickPattern pattern =
                        CandlestickPattern.builder()
                                .stockPrice(stockPrice)
                                .sessionCount(CandlestickPattern.SessionCount.THREE)
                                .name(
                                        CandlestickPattern.Name
                                                .THREE_OUTSIDE_DOWN) // Ensure this enum exists
                                .sentiment(CandlestickPattern.Sentiment.BEARISH)
                                .isStrongBody(
                                        CandleStickUtils.isStrongBody(
                                                timeframe, stockPrice, stockTechnicals))
                                .isSmallBody(
                                        CandleStickUtils.isSmallBody(stockPrice, stockTechnicals))
                                .isGapUp(CandleStickUtils.isGapUp(stockPrice))
                                .isGapDown(CandleStickUtils.isPrevGapDown(stockPrice))
                                .atr(
                                        stockTechnicals.getAtr() != null
                                                ? stockTechnicals.getAtr()
                                                : 0.0)
                                .bodySize(CandleStickUtils.bodySize(stockPrice))
                                .rangeSize(CandleStickUtils.range(stockPrice))
                                .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                                .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                                .sessionDate(stockPrice.getSessionDate())
                                .build();

                candlestickPatternService.create(pattern);

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEveningStar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        boolean firstGreen = CandleStickUtils.isPrev2SessionGreen(stockPrice);
        boolean secondSmallBody = CandleStickUtils.isPrevSmallBody(stockPrice, stockTechnicals) || CandleStickUtils.isPrevVerySmallBody(stockPrice);
        boolean thirdRed =
                CandleStickUtils.isRed(stockPrice) && CandleStickUtils.isLowerClose(stockPrice);
        boolean gapUp = CandleStickUtils.isPrevGapUp(stockPrice);
        boolean gapDown = CandleStickUtils.isGapDown(stockPrice);

        if (firstGreen && secondSmallBody && thirdRed && gapUp && gapDown) {
            log.info("Evening Star pattern detected on {}", stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(
                                    CandlestickPattern.Name
                                            .EVENING_STAR) // make sure this enum value exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(
                                    CandleStickUtils.isStrongBody(
                                            timeframe, stockPrice, stockTechnicals))
                            .isSmallBody(secondSmallBody)
                            .isGapUp(gapUp)
                            .isGapDown(gapDown)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
            return true;
        }
        return false;
    }

    @Override
    public boolean isThreeCandleTweezerTop(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is bullish, second & third are bearish
        if (!CandleStickUtils.isPrev2SessionGreen(stockPrice)
                || !CandleStickUtils.isPrevSessionRed(stockPrice)
                || !CandleStickUtils.isRed(stockPrice)) {
            return false;
        }

        // Compare highs and body highs
        double prevHigh = stockPrice.getPrevHigh();
        double prev2High = stockPrice.getPrev2High();
        double currentHigh = stockPrice.getHigh();

        boolean isEqualHighs =
                Math.abs(prevHigh - currentHigh) / prevHigh <= 0.005
                        && Math.abs(prev2High - prevHigh) / prev2High <= 0.005;

        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        double currentOpen = stockPrice.getOpen();

        boolean isEqualBodyHighs =
                Math.abs(prevClose - currentOpen) / prevClose <= 0.005
                        && Math.abs(prev2Close - prevClose) / prev2Close <= 0.005;

        // Strong bearish confirmation: Third candle closes below second candle
        boolean isBearishReversal = stockPrice.getClose() < stockPrice.getPrevClose();

        if ((isEqualHighs || isEqualBodyHighs) && isBearishReversal) {
            log.info(
                    "{}: Three-Candle Tweezer Top detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.THREE)
                            .name(
                                    CandlestickPattern.Name
                                            .THREE_CANDLE_TWEEZER_TOP) // add enum value if missing
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(
                                    CandleStickUtils.isStrongBody(
                                            timeframe, stockPrice, stockTechnicals))
                            .isSmallBody(
                                    false) // tweezers generally no small body candle in 3rd, adjust
                            // if needed
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);

            return true;
        }

        return false;
    }
}
