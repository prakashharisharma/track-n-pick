package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.CandlestickPattern;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.CandlestickPatternService;
import com.example.service.TwoSessionCandleStickService;
import com.example.service.utils.CandleStickUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TwoSessionCandleStickServiceImpl implements TwoSessionCandleStickService {

    private final CandlestickPatternService candlestickPatternService;

    @Override
    public boolean isBullishEngulfing(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        if (!CandleStickUtils.isGreen(stockPrice)
                || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.bodySize(stockPrice);
        double prevBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBullishPattern =
                currentBody > prevBody
                        && isStrongBody
                        && CandleStickUtils.isOpenBelowPrevClose(stockPrice)
                        && CandleStickUtils.isCloseAbovePrevOpen(stockPrice);

        if (isBullishPattern) {
            log.info(
                    "{}: Bullish Engulfing detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(
                                    CandlestickPattern.SessionCount.TWO) // Engulfing uses 2 candles
                            .name(CandlestickPattern.Name.ENGULFING)
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(isStrongBody)
                            .bodySize(currentBody)
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isBullishPattern;
    }

    @Override
    public boolean isPrevSessionBullishEngulfing(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        if (!CandleStickUtils.isPrevSessionGreen(stockPrice)
                || !CandleStickUtils.isPrev2SessionRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        double prevBody = CandleStickUtils.prev2SessionBodySize(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBullishPattern =
                currentBody > prevBody
                        && isStrongBody
                        && CandleStickUtils.isPrevOpenBelowPrevClose(stockPrice)
                        && CandleStickUtils.isPrevCloseAbovePrevOpen(stockPrice);

        if (isBullishPattern) {
            log.info(
                    "{}: Bullish Engulfing detected on previous session",
                    stockPrice.getStock().getNseSymbol());
        }

        return isBullishPattern;
    }

    @Override
    public boolean isBullishOutsideBar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        if (!CandleStickUtils.isGreen(stockPrice)
                || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        double range = CandleStickUtils.range(stockPrice);
        double prevRange = CandleStickUtils.prevSessionRange(stockPrice);
        double currentBody = CandleStickUtils.bodySize(stockPrice);
        double prevBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);

        if (prevRange == 0) {
            return false; // Avoid division by zero or invalid comparisons
        }

        boolean isStrongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBullishPattern =
                range >= 1.2 * prevRange
                        && // Ensure strong range expansion
                        isStrongRange
                        && isStrongBody
                        && currentBody >= prevBody
                        && lowerWick >= 1.5 * upperWick
                        && // Ensure meaningful lower wick dominance
                        CandleStickUtils.isLowerLow(stockPrice)
                        && CandleStickUtils.isHigherHigh(stockPrice);

        if (isBullishPattern) {
            log.info(
                    "{}: Bullish Outside Bar detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.OUTSIDE_BAR)
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(isStrongBody)
                            .bodySize(currentBody)
                            .rangeSize(range)
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
            return true;
        }

        return false;
    }

    @Override
    public boolean isTweezerBottom(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is red (bearish) and second is green (bullish)
        if (!CandleStickUtils.isPrevSessionRed(stockPrice)
                || !CandleStickUtils.isGreen(stockPrice)) {
            return false;
        }

        // Compare absolute lows (0.5% deviation allowed)
        double prevLow = stockPrice.getPrevLow();
        double currentLow = stockPrice.getLow();
        boolean isEqualLows = Math.abs(prevLow - currentLow) / prevLow <= 0.005;

        // Compare body lows (previous close ≈ current open)
        double prevClose = stockPrice.getPrevClose();
        double currentOpen = stockPrice.getOpen();
        boolean isEqualBodyLows = Math.abs(prevClose - currentOpen) / prevClose <= 0.005;

        // Strong bullish reversal confirmation: Close above previous open
        boolean isBullishReversal = stockPrice.getClose() > stockPrice.getPrevOpen();

        if ((isEqualLows || isEqualBodyLows) && isBullishReversal) {

            // Build the CandlestickPattern entity
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.TWEEZER_BOTTOM) // Add this enum if needed
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(false) // Tweezer bodies aren't necessarily strong
                            .isSmallBody(false) // adjust if your definition differs
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();
            candlestickPatternService.create(pattern); // <-- Call your service here
            log.info(
                    "{}: {} detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    "Tweezer Bottom",
                    stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isPiercingPattern(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        // Ensure we have previous session data
        if (stockPrice == null
                || stockTechnicals == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevLow() == null
                || stockPrice.getPrevHigh() == null) {
            return false;
        }

        // First candle (previous) must be bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);
        double midpointPrev = (stockPrice.getPrevOpen() + stockPrice.getPrevClose()) / 2;

        // Second candle (current) must be bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);
        boolean opensBelowPrevLow = stockPrice.getOpen() < stockPrice.getPrevLow();
        boolean closesAboveMidpoint = stockPrice.getClose() > midpointPrev;

        // Strong-body checks
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isCurrStrong =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Piercing pattern criteria
        boolean isPiercing =
                firstBearish
                        && secondBullish
                        && opensBelowPrevLow
                        && closesAboveMidpoint
                        && isPrevStrong
                        && isCurrStrong;

        if (isPiercing) {
            log.info(
                    "{}: Piercing Pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.PIERCING_LINE)
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(isCurrStrong)
                            .isSmallBody(false) // typically not a small body
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

        return isPiercing;
    }

    @Override
    public boolean isBullishKicker(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null
                || stockTechnicals == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevLow() == null
                || stockPrice.getPrevHigh() == null) {
            return false;
        }

        // First candle (previous) must be bearish and strong
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current) must be bullish and strong
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);
        boolean isCurrStrong =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Gap up beyond previous high and no overlap
        boolean gapsUp = CandleStickUtils.isGapUp(stockPrice);
        boolean noOverlap = stockPrice.getLow() > stockPrice.getPrevHigh();

        // Bullish Kicker criteria
        boolean isBullishKicker =
                firstBearish
                        && secondBullish
                        && isPrevStrong
                        && isCurrStrong
                        && gapsUp
                        && noOverlap;

        if (isBullishKicker) {
            log.info(
                    "{}: Bullish Kicker detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build and save the pattern
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.KICKER) // add to your enum
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(isCurrStrong)
                            .isSmallBody(false)
                            .isGapUp(true)
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

        return isBullishKicker;
    }

    @Override
    public boolean isBullishSash(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        // Ensure we have previous session data
        if (stockPrice == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevLow() == null
                || stockPrice.getPrevHigh() == null) {
            return false;
        }

        // First candle (previous) must be bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);

        // Second candle (current) must be bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);

        // Current open lies within previous body (prevLow < open < prevClose)
        boolean opensWithinPrevBody =
                stockPrice.getOpen() > stockPrice.getPrevLow()
                        && stockPrice.getOpen() < stockPrice.getPrevClose();

        // Current close is above previous open
        boolean closesAbovePrevOpen = stockPrice.getClose() > stockPrice.getPrevOpen();

        boolean isBullishSash =
                firstBearish && secondBullish && opensWithinPrevBody && closesAbovePrevOpen;

        if (isBullishSash) {
            log.info(
                    "{}: Bullish Sash pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.SASH) // add this enum value
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(false) // sash bodies are moderate
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(
                                    stockTechnicals != null && stockTechnicals.getAtr() != null
                                            ? stockTechnicals.getAtr()
                                            : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isBullishSash;
    }

    @Override
    public boolean isBullishSeparatingLine(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null
                || stockTechnicals == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null
                || stockPrice.getPrevLow() == null
                || stockPrice.getPrevHigh() == null) {
            return false;
        }

        // First candle (previous) must be bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);

        // Second candle (current) must be bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);

        // Opens exactly at previous open
        boolean opensAtPrevOpen = stockPrice.getOpen().equals(stockPrice.getPrevOpen());

        // Closes higher than previous close
        boolean closesHigherThanPrevClose = stockPrice.getClose() > stockPrice.getPrevClose();

        boolean isBullishSeparatingLine =
                firstBearish && secondBullish && opensAtPrevOpen && closesHigherThanPrevClose;

        if (isBullishSeparatingLine) {
            log.info(
                    "{}: Bullish Separating Line pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.SEPARATING_LINE) // add this enum
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(false)
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

        return isBullishSeparatingLine;
    }

    @Override
    public boolean isBullishHarami(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        // Ensure we have previous session data
        if (stockPrice == null
                || stockTechnicals == null
                || stockPrice.getPrevOpen() == null
                || stockPrice.getPrevClose() == null) {
            return false;
        }

        // First candle (previous) must be bearish and strong
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current) must be bullish and have a small body
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);
        boolean isCurrSmall =
                !CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle's body must lie entirely within the previous candle's body
        boolean bodyInsidePrev =
                stockPrice.getOpen() > stockPrice.getPrevClose()
                        && stockPrice.getClose() < stockPrice.getPrevOpen();

        boolean isBullishHarami =
                firstBearish && secondBullish && isPrevStrong && isCurrSmall && bodyInsidePrev;

        if (isBullishHarami) {
            log.info(
                    "{}: Bullish Harami pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.HARAMI)
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(false)
                            .isSmallBody(true)
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

        return isBullishHarami;
    }

    @Override
    public boolean isPrevSessionBullishHarami(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null
                || stockTechnicals == null
                || stockPrice.getPrev2Open() == null
                || stockPrice.getPrev2Close() == null) {
            return false;
        }

        // First candle (previous session) - Bearish
        boolean firstBearish = CandleStickUtils.isPrev2SessionRed(stockPrice);
        boolean isPrevCandleStrong =
                CandleStickUtils.isPrev2SessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current session) - Bullish
        boolean secondBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isCurrCandleSmall =
                !CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle's body must be inside the previous candle's body
        boolean bodyInsidePrevBody =
                stockPrice.getPrevOpen() > stockPrice.getPrev2Close()
                        && stockPrice.getPrevClose() < stockPrice.getPrev2Open();

        // Validate Bullish Harami pattern conditions
        return firstBearish
                && secondBullish
                && isPrevCandleStrong
                && isCurrCandleSmall
                && bodyInsidePrevBody;
    }

    @Override
    public boolean isBullishInsideBar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null
                || stockTechnicals == null
                || stockPrice.getPrevHigh() == null
                || stockPrice.getPrevLow() == null) {
            return false;
        }

        // Current must be green and previous red
        if (!CandleStickUtils.isGreen(stockPrice)
                || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        // Previous candle strong body
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);
        // Current candle small body
        boolean isCurrSmall =
                !CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current entirely inside previous range
        boolean insideBar =
                stockPrice.getHigh() < stockPrice.getPrevHigh()
                        && stockPrice.getLow() > stockPrice.getPrevLow();

        boolean isBullishInsideBar = isPrevStrong && isCurrSmall && insideBar;

        if (isBullishInsideBar) {
            log.info(
                    "{}: Bullish Inside Bar detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.INSIDE_BAR) // add INSIDE_BAR to your enum
                            .sentiment(CandlestickPattern.Sentiment.BULLISH)
                            .isStrongBody(false)
                            .isSmallBody(true)
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

        return isBullishInsideBar;
    }

    @Override
    public boolean isBearishEngulfing(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous) must be bullish, current must be red
        if (!CandleStickUtils.isPrevSessionGreen(stockPrice)
                || !CandleStickUtils.isRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.bodySize(stockPrice);
        double prevBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBearishPattern =
                currentBody > prevBody
                        && isStrongBody
                        && CandleStickUtils.isOpenAbovePrevClose(stockPrice)
                        && CandleStickUtils.isCloseBelowPrevOpen(stockPrice);

        if (isBearishPattern) {
            log.info(
                    "{}: Bearish Engulfing detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.ENGULFING)
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(isStrongBody)
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(currentBody)
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isBearishPattern;
    }

    @Override
    public boolean isPrevSessionBearishEngulfing(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) - Bullish
        if (!CandleStickUtils.isPrev2SessionGreen(stockPrice)
                || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        double prevBody = CandleStickUtils.prev2SessionBodySize(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBearishPattern =
                currentBody > prevBody
                        && isStrongBody
                        && CandleStickUtils.isPrevOpenAbovePrevClose(stockPrice)
                        && CandleStickUtils.isPrevCloseBelowPrevOpen(stockPrice);

        if (isBearishPattern) {
            log.info(
                    "{}: Bearish Engulfing detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());
        }

        return isBearishPattern;
    }

    @Override
    public boolean isBearishOutsideBar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle strength
        boolean isPrevCandleStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle bearish and strong
        boolean secondBearish = CandleStickUtils.isRed(stockPrice);
        boolean isCurrCandleStrong =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();

        // Current fully engulfs previous
        boolean lowBreaksPrevLow = stockPrice.getLow() < prevLow;
        boolean highBreaksPrevHigh = stockPrice.getHigh() > prevHigh;

        boolean isBearishOutsideBar =
                secondBearish
                        && isCurrCandleStrong
                        && isPrevCandleStrong
                        && lowBreaksPrevLow
                        && highBreaksPrevHigh;

        if (isBearishOutsideBar) {
            log.info(
                    "{}: Bearish Outside Bar detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.OUTSIDE_BAR) // ensure enum value exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(isCurrCandleStrong)
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

        return isBearishOutsideBar;
    }

    @Override
    public boolean isTweezerTop(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is green (bullish) and second is red (bearish)
        if (!CandleStickUtils.isPrevSessionGreen(stockPrice)
                || !CandleStickUtils.isRed(stockPrice)) {
            return false;
        }

        // Compare absolute highs (0.5% deviation allowed)
        double prevHigh = stockPrice.getPrevHigh();
        double currentHigh = stockPrice.getHigh();
        boolean isEqualHighs = Math.abs(prevHigh - currentHigh) / prevHigh <= 0.005;

        // Compare body highs (previous close ≈ current open)
        double prevClose = stockPrice.getPrevClose();
        double currentOpen = stockPrice.getOpen();
        boolean isEqualBodyHighs = Math.abs(prevClose - currentOpen) / prevClose <= 0.005;

        // Strong bearish reversal confirmation: Close below previous open
        boolean isBearishReversal = stockPrice.getClose() < stockPrice.getPrevOpen();

        if ((isEqualHighs || isEqualBodyHighs) && isBearishReversal) {
            log.info(
                    "{}: Tweezer Top detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.TWEEZER_TOP) // ensure enum exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(false)
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
            return true;
        }

        return false;
    }

    @Override
    public boolean isDarkCloudCover(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) – strong bullish
        boolean firstBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);
        double prevMidpoint = (stockPrice.getPrevOpen() + stockPrice.getPrevClose()) / 2;

        // Second candle (current session) – bearish
        boolean secondBearish = CandleStickUtils.isRed(stockPrice);
        boolean isCurrStrong =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Opens above previous high (gap up)
        boolean opensAbovePrevHigh = stockPrice.getOpen() > stockPrice.getPrevHigh();

        // Closes below previous midpoint but above previous open
        boolean closesBelowPrevMid = stockPrice.getClose() < prevMidpoint;
        boolean closesAbovePrevOpen = stockPrice.getClose() > stockPrice.getPrevOpen();

        boolean isDarkCloudCover =
                firstBullish
                        && isPrevStrong
                        && secondBearish
                        && isCurrStrong
                        && opensAbovePrevHigh
                        && closesBelowPrevMid
                        && closesAbovePrevOpen;

        if (isDarkCloudCover) {
            log.info(
                    "{}: Dark Cloud Cover detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.DARK_CLOUD_COVER) // ensure enum exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(isCurrStrong)
                            .isSmallBody(false)
                            .isGapUp(true)
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

        return isDarkCloudCover;
    }

    @Override
    public boolean isBearishKicker(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) – Bullish and strong
        boolean firstBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current session) – Bearish and strong
        boolean secondBearish = CandleStickUtils.isRed(stockPrice);
        boolean isCurrStrong =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Check for a gap down below previous low
        boolean gapsDown = CandleStickUtils.isGapDown(stockPrice);

        // Ensure no overlap with the previous candle
        boolean noOverlap = stockPrice.getHigh() < stockPrice.getPrevLow();

        // Bearish Kicker criteria
        boolean isBearishKicker =
                firstBullish
                        && secondBearish
                        && isPrevStrong
                        && isCurrStrong
                        && gapsDown
                        && noOverlap;

        if (isBearishKicker) {
            log.info(
                    "{}: Bearish Kicker detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            // Build and persist the pattern
            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.KICKER) // ensure enum exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(isCurrStrong)
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(true)
                            .atr(stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isBearishKicker;
    }

    @Override
    public boolean isBearishSash(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle – bullish (green)
        boolean prevBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        // Current candle – bearish (red)
        boolean currBearish = CandleStickUtils.isRed(stockPrice);

        // Open near or slightly above previous close (within 10% of ATR)
        double atr = stockTechnicals.getAtr() != null ? stockTechnicals.getAtr() : 0.0;
        boolean opensNearPrevClose =
                Math.abs(stockPrice.getOpen() - stockPrice.getPrevClose()) <= (atr * 0.1);

        // Closes below previous open (strong downward move)
        boolean closesBelowPrevOpen = stockPrice.getClose() < stockPrice.getPrevOpen();

        boolean isBearishSash =
                prevBullish && currBearish && opensNearPrevClose && closesBelowPrevOpen;

        if (isBearishSash) {
            log.info(
                    "{}: Bearish Sash pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.SASH) // ensure this exists in your enum
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(true) // typically a strong body in a sash
                            .isSmallBody(false)
                            .isGapUp(false)
                            .isGapDown(false)
                            .atr(atr)
                            .bodySize(CandleStickUtils.bodySize(stockPrice))
                            .rangeSize(CandleStickUtils.range(stockPrice))
                            .lowerWickSize(CandleStickUtils.lowerWickSize(stockPrice))
                            .upperWickSize(CandleStickUtils.upperWickSize(stockPrice))
                            .sessionDate(stockPrice.getSessionDate())
                            .build();

            candlestickPatternService.create(pattern);
        }

        return isBearishSash;
    }

    @Override
    public boolean isBearishSeparatingLine(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle – bullish (green)
        boolean prevBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        // Current candle – bearish (red)
        boolean currBearish = CandleStickUtils.isRed(stockPrice);

        // Opens exactly at previous close
        boolean opensAtPrevClose = stockPrice.getOpen().equals(stockPrice.getPrevClose());
        // Closes below previous low (strong bearish follow-through)
        boolean closesLower = stockPrice.getClose() < stockPrice.getPrevLow();

        boolean isBearishSeparatingLine =
                prevBullish && currBearish && opensAtPrevClose && closesLower;

        if (isBearishSeparatingLine) {
            log.info(
                    "{}: Bearish Separating Line detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(
                                    CandlestickPattern.Name
                                            .SEPARATING_LINE) // ensure this enum exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(true) // typically a strong body
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

        return isBearishSeparatingLine;
    }

    @Override
    public boolean isBearishHarami(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle – bullish (green) and strong
        boolean prevBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle – bearish (red)
        boolean currBearish = CandleStickUtils.isRed(stockPrice);

        // Current body must lie entirely within the previous body
        boolean opensWithinPrevBody =
                stockPrice.getOpen() <= stockPrice.getPrevClose()
                        && stockPrice.getOpen() >= stockPrice.getPrevOpen();
        boolean closesWithinPrevBody =
                stockPrice.getClose() <= stockPrice.getPrevClose()
                        && stockPrice.getClose() >= stockPrice.getPrevOpen();

        boolean isBearishHarami =
                prevBullish
                        && isPrevStrong
                        && currBearish
                        && opensWithinPrevBody
                        && closesWithinPrevBody;

        if (isBearishHarami) {
            log.info(
                    "{}: Bearish Harami detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(CandlestickPattern.Name.HARAMI) // make sure this enum exists
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(false)
                            .isSmallBody(true)
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

        return isBearishHarami;
    }

    @Override
    public boolean isPrevSessionBearishHarami(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle - Bullish (Green) with a strong body
        boolean prevBullish = CandleStickUtils.isPrev2SessionGreen(stockPrice);
        boolean isPrevCandleStrong =
                CandleStickUtils.isPrev2SessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle - Bearish (Red)
        boolean currBearish = CandleStickUtils.isPrevSessionRed(stockPrice);

        // Current candle is completely inside the previous candle's body
        boolean opensWithinPrevBody =
                stockPrice.getPrevOpen() <= stockPrice.getPrev2Close()
                        && stockPrice.getPrevOpen() >= stockPrice.getPrev2Open();
        boolean closesWithinPrevBody =
                stockPrice.getPrevClose() <= stockPrice.getPrev2Close()
                        && stockPrice.getPrevClose() >= stockPrice.getPrev2Open();

        return prevBullish
                && isPrevCandleStrong
                && currBearish
                && opensWithinPrevBody
                && closesWithinPrevBody;
    }

    @Override
    public boolean isBearishInsideBar(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Current must be red and previous must be green
        if (!CandleStickUtils.isRed(stockPrice)
                || !CandleStickUtils.isPrevSessionGreen(stockPrice)) {
            return false;
        }

        // Current entirely inside previous high/low range
        boolean insideBar =
                stockPrice.getHigh() < stockPrice.getPrevHigh()
                        && stockPrice.getLow() > stockPrice.getPrevLow();

        // Previous candle strong body
        boolean isPrevStrong =
                CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBearishInsideBar = insideBar && isPrevStrong;

        if (isBearishInsideBar) {
            log.info(
                    "{}: Bearish Inside Bar pattern detected on {}",
                    stockPrice.getStock().getNseSymbol(),
                    stockPrice.getSessionDate());

            CandlestickPattern pattern =
                    CandlestickPattern.builder()
                            .stockPrice(stockPrice)
                            .sessionCount(CandlestickPattern.SessionCount.TWO)
                            .name(
                                    CandlestickPattern.Name
                                            .INSIDE_BAR) // or BEARISH_INSIDE_BAR if you prefer
                            .sentiment(CandlestickPattern.Sentiment.BEARISH)
                            .isStrongBody(false)
                            .isSmallBody(true)
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

        return isBearishInsideBar;
    }
}
