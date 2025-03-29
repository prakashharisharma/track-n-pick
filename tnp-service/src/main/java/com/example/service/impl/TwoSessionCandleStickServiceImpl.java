package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.service.TwoSessionCandleStickService;
import com.example.service.utils.CandleStickUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TwoSessionCandleStickServiceImpl implements TwoSessionCandleStickService {

    @Override
    public boolean isBullishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        if (!CandleStickUtils.isGreen(stockPrice) || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.bodySize(stockPrice);
        double prevBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        boolean isStrongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBullishPattern =
                currentBody > prevBody &&
                        isStrongBody &&
                        CandleStickUtils.isOpenBelowPrevClose(stockPrice) &&
                        CandleStickUtils.isCloseAbovePrevOpen(stockPrice);

        if (isBullishPattern) {
            log.info("{}: Bullish Engulfing detected on {}",
                    stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBullishPattern;
    }

    @Override
    public boolean isPrevSessionBullishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        if (!CandleStickUtils.isPrevSessionGreen(stockPrice) || !CandleStickUtils.isPrev2SessionRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        double prevBody = CandleStickUtils.prev2SessionBodySize(stockPrice);
        boolean isStrongBody = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBullishPattern =
                currentBody > prevBody &&
                        isStrongBody &&
                        CandleStickUtils.isPrevOpenBelowPrevClose(stockPrice) &&
                        CandleStickUtils.isPrevCloseAbovePrevOpen(stockPrice);

        if (isBullishPattern) {
            log.info("{}: Bullish Engulfing detected on previous session",
                    stockPrice.getStock().getNseSymbol());
        }

        return isBullishPattern;
    }

    @Override
    public boolean isBullishOutsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (!CandleStickUtils.isGreen(stockPrice) || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        double range = CandleStickUtils.range(stockPrice);
        double prevRange = CandleStickUtils.prevSessionRange(stockPrice);
        double currentBody = CandleStickUtils.bodySize(stockPrice);
        double prevBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        double lowerWick = CandleStickUtils.lowerWickSize(stockPrice);
        double upperWick = CandleStickUtils.upperWickSize(stockPrice);

        boolean isStrongRange = CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean isStrongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBullishPattern =
                range >= 1.2 * prevRange &&   // Ensure strong range expansion
                        isStrongRange &&
                        isStrongBody &&
                        currentBody >= prevBody &&
                        lowerWick >= 1.5 * upperWick && // Ensure meaningful lower wick dominance
                        CandleStickUtils.isLowerLow(stockPrice) &&
                        CandleStickUtils.isHigherHigh(stockPrice);

        if (isBullishPattern) {
            log.info("{}: Bullish Outside Bar detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }


    @Override
    public boolean isTweezerBottom(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is red (bearish) and second is green (bullish)
        if (!CandleStickUtils.isPrevSessionRed(stockPrice) || !CandleStickUtils.isGreen(stockPrice)) {
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
            log.info("{}: Tweezer Bottom detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }

    @Override
    public boolean isPiercingPattern(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null || stockTechnicals == null ||
                stockPrice.getPrevOpen() == null || stockPrice.getPrevClose() == null ||
                stockPrice.getPrevLow() == null || stockPrice.getPrevHigh() == null) {
            return false;
        }


        // First candle (previous candle) - Bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);
        double midpointPrev = (stockPrice.getPrevOpen() + stockPrice.getPrevClose()) / 2;

        // Second candle (current candle) - Bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);
        boolean opensBelowPrevLow = stockPrice.getOpen() < stockPrice.getPrevLow();
        boolean closesAboveMidpoint = stockPrice.getClose() > midpointPrev;

        // Validate strong body using ATR and percentage-based thresholds
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isCurrCandleStrong = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Validate piercing pattern conditions
        boolean isPiercing = firstBearish && secondBullish && opensBelowPrevLow && closesAboveMidpoint
                && isPrevCandleStrong && isCurrCandleStrong;

        if(isPiercing) {
            log.info("{} Piercing Pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isPiercing;
    }

    @Override
    public boolean isBullishKicker(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null || stockTechnicals == null ||
                stockPrice.getPrevOpen() == null || stockPrice.getPrevClose() == null ||
                stockPrice.getPrevLow() == null || stockPrice.getPrevHigh() == null) {
            return false;
        }

        // First candle (previous candle) - Bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current candle) - Bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);
        boolean isCurrCandleStrong = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Check for a gap up
        boolean gapsUpAbovePrevHigh = CandleStickUtils.isGapUp(stockPrice);

        boolean noOverlapWithPrevCandle = stockPrice.getLow() > stockPrice.getPrevHigh();

        // Validate bullish kicker pattern conditions
        boolean isBullishKicker = firstBearish && secondBullish && gapsUpAbovePrevHigh && noOverlapWithPrevCandle
                && isPrevCandleStrong && isCurrCandleStrong;

        if (isBullishKicker) {
            log.info("{} Bullish Kicker detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBullishKicker;
    }

    @Override
    public boolean isBullishSash(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null ||
                stockPrice.getPrevOpen() == null || stockPrice.getPrevClose() == null ||
                stockPrice.getPrevLow() == null || stockPrice.getPrevHigh() == null) {
            return false;
        }

        // First candle (previous candle) - Bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);

        // Second candle (current candle) - Bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);

        // Open within previous candle’s body
        boolean opensWithinPrevBody = stockPrice.getOpen() > stockPrice.getPrevLow()
                && stockPrice.getOpen() < stockPrice.getPrevClose();

        // Closes above previous open
        boolean closesAbovePrevOpen = stockPrice.getClose() > stockPrice.getPrevOpen();

        // Validate Bullish Sash pattern conditions
        boolean isBullishSash = firstBearish && secondBullish && opensWithinPrevBody && closesAbovePrevOpen;

        if (isBullishSash) {
            log.info("{} Bullish Sash pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBullishSash;
    }

    @Override
    public boolean isBullishSeparatingLine(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null || stockTechnicals == null ||
                stockPrice.getPrevOpen() == null || stockPrice.getPrevClose() == null ||
                stockPrice.getPrevLow() == null || stockPrice.getPrevHigh() == null) {
            return false;
        }

        // First candle (previous session) - Bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);

        // Second candle (current session) - Bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);

        // Opens exactly at previous open
        boolean opensAtPrevOpen = stockPrice.getOpen().equals(stockPrice.getPrevOpen());

        // Closes higher than previous close
        boolean closesHigherThanPrevClose = stockPrice.getClose() > stockPrice.getPrevClose();


        // Validate Bullish Separating Line pattern conditions
        boolean isBullishSeparatingLine = firstBearish && secondBullish && opensAtPrevOpen && closesHigherThanPrevClose;

        if (isBullishSeparatingLine) {
            log.info("{} Bullish Separating Line pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBullishSeparatingLine;
    }

    @Override
    public boolean isBullishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null || stockTechnicals == null ||
                stockPrice.getPrevOpen() == null || stockPrice.getPrevClose() == null) {
            return false;
        }

        // First candle (previous session) - Bearish
        boolean firstBearish = CandleStickUtils.isPrevSessionRed(stockPrice);
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current session) - Bullish
        boolean secondBullish = CandleStickUtils.isGreen(stockPrice);
        boolean isCurrCandleSmall = !CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle's body must be inside the previous candle's body
        boolean bodyInsidePrevBody = stockPrice.getOpen() > stockPrice.getPrevClose() &&
                stockPrice.getClose() < stockPrice.getPrevOpen();

        // Validate Bullish Harami pattern conditions
        boolean isBullishHarami = firstBearish && secondBullish && isPrevCandleStrong && isCurrCandleSmall && bodyInsidePrevBody;

        if (isBullishHarami) {
            log.info("{} Bullish Harami pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBullishHarami;
    }

    @Override
    public boolean isPrevSessionBullishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null || stockTechnicals == null ||
                stockPrice.getPrev2Open() == null || stockPrice.getPrev2Close() == null) {
            return false;
        }

        // First candle (previous session) - Bearish
        boolean firstBearish = CandleStickUtils.isPrev2SessionRed(stockPrice);
        boolean isPrevCandleStrong = CandleStickUtils.isPrev2SessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current session) - Bullish
        boolean secondBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isCurrCandleSmall = !CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle's body must be inside the previous candle's body
        boolean bodyInsidePrevBody = stockPrice.getPrevOpen() > stockPrice.getPrev2Close() &&
                stockPrice.getPrevClose() < stockPrice.getPrev2Open();


        // Validate Bullish Harami pattern conditions
        return firstBearish && secondBullish && isPrevCandleStrong && isCurrCandleSmall && bodyInsidePrevBody ;
    }

    @Override
    public boolean isBullishInsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        // Ensure we have previous session data
        if (stockPrice == null || stockTechnicals == null ||
                stockPrice.getPrevHigh() == null || stockPrice.getPrevLow() == null) {
            return false;
        }

        if (!CandleStickUtils.isGreen(stockPrice) || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        // First candle (previous session) - Can be bullish or bearish
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current session) - Can be bullish or bearish
        boolean isCurrCandleSmall = !CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle must be fully inside the previous candle’s range
        boolean insideBar = stockPrice.getHigh() < stockPrice.getPrevHigh() &&
                stockPrice.getLow() > stockPrice.getPrevLow();

        // Validate Bullish Inside Bar pattern conditions
        boolean isBullishInsideBar = isPrevCandleStrong && isCurrCandleSmall && insideBar;

        if (isBullishInsideBar) {
            log.info("{} Bullish Inside Bar detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBullishInsideBar;
    }

    @Override
    public boolean isBearishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) - Bullish
        if (!CandleStickUtils.isPrevSessionGreen(stockPrice) || !CandleStickUtils.isRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.bodySize(stockPrice);
        double prevBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        boolean isStrongBody = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBearishPattern =
                currentBody > prevBody &&
                        isStrongBody &&
                        CandleStickUtils.isOpenAbovePrevClose(stockPrice) &&
                        CandleStickUtils.isCloseBelowPrevOpen(stockPrice);

        if (isBearishPattern) {
            log.info("{}: Bearish Engulfing detected on {}",
                    stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishPattern;
    }

    @Override
    public boolean isPrevSessionBearishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) - Bullish
        if (!CandleStickUtils.isPrev2SessionGreen(stockPrice) || !CandleStickUtils.isPrevSessionRed(stockPrice)) {
            return false;
        }

        double currentBody = CandleStickUtils.prevSessionBodySize(stockPrice);
        double prevBody = CandleStickUtils.prev2SessionBodySize(stockPrice);
        boolean isStrongBody = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBearishPattern =
                currentBody > prevBody &&
                        isStrongBody &&
                        CandleStickUtils.isPrevOpenAbovePrevClose(stockPrice) &&
                        CandleStickUtils.isPrevCloseBelowPrevOpen(stockPrice);

        if (isBearishPattern) {
            log.info("{}: Bearish Engulfing detected on {}",
                    stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishPattern;
    }

    @Override
    public boolean isBearishOutsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) - Bullish or Bearish
        double prevHigh = stockPrice.getPrevHigh();
        double prevLow = stockPrice.getPrevLow();
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current session) - Bearish
        boolean secondBearish = CandleStickUtils.isRed(stockPrice);
        boolean isCurrCandleStrong = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle fully engulfs previous candle
        boolean lowBreaksPrevLow = stockPrice.getLow() < prevLow;
        boolean highBreaksPrevHigh = stockPrice.getHigh() > prevHigh;

        boolean isBearishOutsideBar = secondBearish && isCurrCandleStrong && isPrevCandleStrong &&
                lowBreaksPrevLow && highBreaksPrevHigh;

        if (isBearishOutsideBar) {
            log.info("{}: Bearish Outside Bar detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishOutsideBar;
    }

    @Override
    public boolean isTweezerTop(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Ensure first candle is green (bullish) and second is red (bearish)
        if (!CandleStickUtils.isPrevSessionGreen(stockPrice) || !CandleStickUtils.isRed(stockPrice)) {
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
            log.info("{}: Tweezer Top detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
            return true;
        }

        return false;
    }


    @Override
    public boolean isDarkCloudCover(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) - Strong Bullish
        boolean firstBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);
        double prevMidpoint = (stockPrice.getPrevOpen() + stockPrice.getPrevClose()) / 2;

        // Second candle (current session) - Bearish
        boolean secondBearish = CandleStickUtils.isRed(stockPrice);
        boolean isCurrCandleStrong = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle opens above the previous high (gap up)
        boolean opensAbovePrevHigh = stockPrice.getOpen() > stockPrice.getPrevHigh();

        // Current candle closes below previous midpoint but above previous open
        boolean closesBelowPrevMid = stockPrice.getClose() < prevMidpoint;
        boolean closesAbovePrevOpen = stockPrice.getClose() > stockPrice.getPrevOpen();

        boolean isDarkCloudCover = firstBullish && isPrevCandleStrong && secondBearish && isCurrCandleStrong &&
                opensAbovePrevHigh && closesBelowPrevMid && closesAbovePrevOpen;

        if (isDarkCloudCover) {
            log.info("{}: Dark Cloud Cover detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isDarkCloudCover;
    }

    @Override
    public boolean isBearishKicker(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // First candle (previous session) - Bullish
        boolean firstBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Second candle (current session) - Strong Bearish
        boolean secondBearish = CandleStickUtils.isRed(stockPrice);
        boolean isCurrCandleStrong = CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        // Check for a gap down
        boolean gapsDownBelowPrevLow = CandleStickUtils.isGapDown(stockPrice);

        // No overlap with the previous candle
        boolean noOverlapWithPrevCandle = stockPrice.getHigh() < stockPrice.getPrevLow();

        // Validate Bearish Kicker pattern conditions
        boolean isBearishKicker = firstBullish && secondBearish && gapsDownBelowPrevLow && noOverlapWithPrevCandle
                && isPrevCandleStrong && isCurrCandleStrong;

        if (isBearishKicker) {
            log.info("{}: Bearish Kicker detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishKicker;
    }

    @Override
    public boolean isBearishSash(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle - Bullish (Green)
        boolean prevBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);

        // Current candle - Bearish (Red)
        boolean currBearish = CandleStickUtils.isRed(stockPrice);

        // Open near or slightly above previous close
        boolean opensNearPrevClose = Math.abs(stockPrice.getOpen() - stockPrice.getPrevClose()) <= stockTechnicals.getAtr() * 0.1;

        // Closes below previous open (strong downward move)
        boolean closesBelowPrevOpen = stockPrice.getClose() < stockPrice.getPrevOpen();

        boolean isBearishSash = prevBullish && currBearish && opensNearPrevClose && closesBelowPrevOpen;

        if (isBearishSash) {
            log.info("{} Bearish Sash pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishSash;
    }

    @Override
    public boolean isBearishSeparatingLine(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle - Bullish (Green)
        boolean prevBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);

        // Current candle - Bearish (Red)
        boolean currBearish = CandleStickUtils.isRed(stockPrice);

        // Opens at the exact same price as the previous close
        boolean opensAtPrevClose = stockPrice.getOpen().equals(stockPrice.getPrevClose());

        // Closes significantly lower (strong bearish move)
        boolean closesLower = stockPrice.getClose() < stockPrice.getPrevLow();


        boolean isBearishSeparatingLine = prevBullish && currBearish && opensAtPrevClose && closesLower;

        if (isBearishSeparatingLine) {
            log.info("{} Bearish Separating Line pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishSeparatingLine;
    }


    @Override
    public boolean isBearishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle - Bullish (Green) with a strong body
        boolean prevBullish = CandleStickUtils.isPrevSessionGreen(stockPrice);
        boolean isPrevCandleStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle - Bearish (Red)
        boolean currBearish = CandleStickUtils.isRed(stockPrice);

        // Current candle is completely inside the previous candle's body
        boolean opensWithinPrevBody = stockPrice.getOpen() <= stockPrice.getPrevClose() && stockPrice.getOpen() >= stockPrice.getPrevOpen();
        boolean closesWithinPrevBody = stockPrice.getClose() <= stockPrice.getPrevClose() && stockPrice.getClose() >= stockPrice.getPrevOpen();


        boolean isBearishHarami = prevBullish && isPrevCandleStrong && currBearish && opensWithinPrevBody && closesWithinPrevBody;

        if (isBearishHarami) {
            log.info("{} Bearish Harami pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishHarami;
    }

    @Override
    public boolean isPrevSessionBearishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        // Previous candle - Bullish (Green) with a strong body
        boolean prevBullish = CandleStickUtils.isPrev2SessionGreen(stockPrice);
        boolean isPrevCandleStrong = CandleStickUtils.isPrev2SessionStrongBody(timeframe, stockPrice, stockTechnicals);

        // Current candle - Bearish (Red)
        boolean currBearish = CandleStickUtils.isPrevSessionRed(stockPrice);

        // Current candle is completely inside the previous candle's body
        boolean opensWithinPrevBody = stockPrice.getPrevOpen() <= stockPrice.getPrev2Close() && stockPrice.getPrevOpen() >= stockPrice.getPrev2Open();
        boolean closesWithinPrevBody = stockPrice.getPrevClose() <= stockPrice.getPrev2Close() && stockPrice.getPrevClose() >= stockPrice.getPrev2Open();


        return prevBullish && isPrevCandleStrong && currBearish && opensWithinPrevBody && closesWithinPrevBody;
    }

    @Override
    public boolean isBearishInsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) return false;

        if (!CandleStickUtils.isRed(stockPrice) || !CandleStickUtils.isPrevSessionGreen(stockPrice)) {
            return false;
        }

        boolean insideBar = stockPrice.getHigh() < stockPrice.getPrevHigh() &&
                stockPrice.getLow() > stockPrice.getPrevLow();

        boolean isRedCandle = CandleStickUtils.isRed(stockPrice);

        boolean isPrevStrong = CandleStickUtils.isPrevSessionStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isBearishInsideBar = insideBar && isRedCandle && isPrevStrong;

        if (isBearishInsideBar) {
            log.info("{} Bearish Inside Bar pattern detected on {}", stockPrice.getStock().getNseSymbol(), stockPrice.getSessionDate());
        }

        return isBearishInsideBar;
    }
}
