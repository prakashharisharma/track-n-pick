package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.*;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.service.utils.CandleStickUtils;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PriceActionServiceImpl implements PriceActionService {

    private final CandleStickConfirmationService candleStickHelperService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final CandleStickService candleStickService;

    private final BreakoutLedgerService breakoutLedgerService;

    private final StockPriceHelperService stockPriceHelperService;
    private final RelevanceService relevanceService;
    private final DynamicRelevanceService dynamicRelevanceService;
    private final TrendService trendService;

    private final DynamicTrendService dynamicTrendService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private final CandlestickPatternService candlestickPatternService;

    @Override
    public TradeSetup breakOut(Stock stock, Timeframe timeframe) {

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (candleStickService.isDead(stockPrice)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;
        Trend trend = trendService.detect(stock, timeframe);
        Trend dynamicTrend = dynamicTrendService.detect(stock, timeframe);

        AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef = new AtomicReference<>();

        if (trend.getMomentum() == dynamicTrend.getMomentum()) {
            log.info(
                    "{} [{}]: Scanning price action breakout | Direction: {}, Momentum: {}",
                    stock.getNseSymbol(),
                    timeframe,
                    trend.getDirection(),
                    trend.getMomentum());
            isCandleActive =
                    this.isBullishAction(
                            trend, timeframe, stockPrice, stockTechnicals, subStrategyRef);
        } else {
            log.info(
                    "{} [{}]: Scanning price action breakout | Direction: {}, Momentum: {}",
                    stock.getNseSymbol(),
                    timeframe,
                    dynamicTrend.getDirection(),
                    dynamicTrend.getMomentum());
            isCandleActive =
                    this.isDynamicBullishAction(
                            trend, timeframe, stockPrice, stockTechnicals, subStrategyRef);
        }

        if (isCandleActive) {
            log.info(
                    "{} bullish candlestick confirmed using {}:{}",
                    stock.getNseSymbol(),
                    ResearchTechnical.Strategy.PRICE,
                    subStrategyRef.get());

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.PRICE)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private boolean isBullishAction(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef) {

        boolean isUpperWickSizeConfirmed =
                candleStickHelperService.isUpperWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        if (!isUpperWickSizeConfirmed) {
            return false;
        }

        Trend.Phase phase = trend.getMomentum();

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);
        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (relevanceService.isNearSupport(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBullishCandleStick
                    || ((EnumSet.of(
                                            Trend.Phase.DIP,
                                            Trend.Phase.PULLBACK,
                                            Trend.Phase.CORRECTION,
                                            Trend.Phase.DEEP_CORRECTION,
                                            Trend.Phase.BOTTOM)
                                    .contains(phase))
                            && !isBearishCandleStick
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongLowerWick(stockPrice)))) {

                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_SUPPORT
                                : ResearchTechnical.SubStrategy.WEAK_SUPPORT);
                return true;
            }
        }

        boolean isHigherTimeFrameHighBreakout =
                stockPriceHelperService.isHigherTimeFrameHighBreakout(timeframe, stockPrice);
        boolean isHigher2TimeFrameHighBreakout =
                stockPriceHelperService.isHigher2TimeFrameHighBreakout(timeframe, stockPrice);

        if (relevanceService.isBreakout(trend, timeframe, stockPrice, stockTechnicals)
                && isHigherTimeFrameHighBreakout) {
            if (isBullishCandleStick
                    || ((EnumSet.of(
                                            Trend.Phase.TOP,
                                            Trend.Phase.RECOVERY,
                                            Trend.Phase.ADVANCE,
                                            Trend.Phase.STRONG_ADVANCE,
                                            Trend.Phase.EARLY_RECOVERY)
                                    .contains(phase))
                            && !isBearishCandleStick
                            && isVolumeSurge
                            && isHigher2TimeFrameHighBreakout)) {

                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_BREAKOUT
                                : ResearchTechnical.SubStrategy.WEAK_BREAKOUT);
                return true;
            }
        }
        boolean isHigherHighHigherLow = CandleStickUtils.isHigherHighAndHigherLow(stockPrice);
        if (isHigherHighHigherLow
                && relevanceService.isBullishIndicator(
                        trend, timeframe, stockPrice, stockTechnicals)) {
            subStrategyRef.set(ResearchTechnical.SubStrategy.BULLISH_INDICATORS);
            return true;
        }

        return false;
    }

    private boolean isDynamicBullishAction(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef) {

        boolean isUpperWickSizeConfirmed =
                candleStickHelperService.isUpperWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        if (!isUpperWickSizeConfirmed) {
            return false;
        }

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);
        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (dynamicRelevanceService.isNearSupport(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBullishCandleStick
                    || (!isBearishCandleStick
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongLowerWick(stockPrice)))) {

                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.DYNAMIC_STRONG_SUPPORT
                                : ResearchTechnical.SubStrategy.DYNAMIC_WEAK_SUPPORT);
                return true;
            }
        }

        if (trend.getMomentum() == Trend.Phase.BOTTOM) {
            if (candlestickPatternService.hasAtLeastTwoPatternsWithSentiment(
                    stockPrice, CandlestickPattern.Sentiment.BULLISH)) {
                subStrategyRef.set(ResearchTechnical.SubStrategy.BOTTOM_REVERSAL);
                return true;
            }
        }

        boolean isHigherTimeFrameHighBreakout =
                stockPriceHelperService.isHigherTimeFrameHighBreakout(timeframe, stockPrice);
        boolean isHigher2TimeFrameHighBreakout =
                stockPriceHelperService.isHigher2TimeFrameHighBreakout(timeframe, stockPrice);

        if (dynamicRelevanceService.isBreakout(trend, timeframe, stockPrice, stockTechnicals)
                && isHigherTimeFrameHighBreakout) {
            if (isBullishCandleStick
                    || (!isBearishCandleStick && isVolumeSurge && isHigher2TimeFrameHighBreakout)) {

                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.DYNAMIC_STRONG_BREAKOUT
                                : ResearchTechnical.SubStrategy.DYNAMIC_WEAK_BREAKOUT);
                return true;
            }
        }

        boolean isHigherHighHigherLow = CandleStickUtils.isHigherHighAndHigherLow(stockPrice);
        boolean isHtHigherHighHigherLow =
                CandleStickUtils.isHigherHighAndHigherLow(
                        stockPriceService.get(stockPrice.getStock(), timeframe.getHigher()));

        if ((isHigherHighHigherLow || isHtHigherHighHigherLow)
                && dynamicRelevanceService.isBullishIndicator(
                        trend, timeframe, stockPrice, stockTechnicals)) {
            subStrategyRef.set(ResearchTechnical.SubStrategy.DYNAMIC_BULLISH_INDICATORS);
            return true;
        }

        return false;
    }

    @Override
    public TradeSetup breakDown(Stock stock, Timeframe timeframe) {

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (candleStickService.isDead(stockPrice)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;

        Trend trend = trendService.detect(stock, timeframe);
        Trend dynamicTrend = dynamicTrendService.detect(stock, timeframe);

        AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef = new AtomicReference<>();

        if (trend.getMomentum() == dynamicTrend.getMomentum()) {
            log.info(
                    "{} [{}]: Scanning price action breakdown | Direction: {}, Momentum: {}",
                    stock.getNseSymbol(),
                    timeframe,
                    trend.getDirection(),
                    trend.getMomentum());
            isCandleActive =
                    this.isBearishAction(
                            trend, timeframe, stockPrice, stockTechnicals, subStrategyRef);
        } else {
            log.info(
                    "{} [{}]: Scanning price action breakdown | Direction: {}, Momentum: {}",
                    stock.getNseSymbol(),
                    timeframe,
                    dynamicTrend.getDirection(),
                    dynamicTrend.getMomentum());
            isCandleActive =
                    this.isDynamicBearishAction(
                            trend, timeframe, stockPrice, stockTechnicals, subStrategyRef);
        }

        if (isCandleActive) {
            log.info(
                    "{} bearish candlestick confirmed using {}:{}",
                    stock.getNseSymbol(),
                    ResearchTechnical.Strategy.PRICE,
                    subStrategyRef.get());

            breakoutLedgerService.addNegative(
                    stock, timeframe, BreakoutLedger.BreakoutCategory.BREAKDOWN_CANDLESTICK);
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.PRICE)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private boolean isBearishAction(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef) {

        boolean isLowerWickSizeConfirmed =
                candleStickHelperService.isLowerWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        if (!isLowerWickSizeConfirmed) {
            return false;
        }

        Trend.Phase phase = trend.getMomentum();

        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (relevanceService.isNearResistance(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBearishCandleStick
                    || ((EnumSet.of(
                                            Trend.Phase.RECOVERY,
                                            Trend.Phase.ADVANCE,
                                            Trend.Phase.STRONG_ADVANCE,
                                            Trend.Phase.TOP,
                                            Trend.Phase.EARLY_RECOVERY)
                                    .contains(phase))
                            && !isBullishCandleStick
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongUpperWick(stockPrice)))) {

                subStrategyRef.set(
                        isBearishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_RESISTANCE
                                : ResearchTechnical.SubStrategy.WEAK_RESISTANCE);
                return true;
            }
        }

        boolean isHigherTimeFrameHighBreakdown =
                stockPriceHelperService.isHigherTimeFrameHighBreakdown(timeframe, stockPrice);
        boolean isHigher2TimeFrameHighBreakdown =
                stockPriceHelperService.isHigher2TimeFrameHighBreakdown(timeframe, stockPrice);
        if (relevanceService.isBreakdown(trend, timeframe, stockPrice, stockTechnicals)
                && isHigherTimeFrameHighBreakdown) {
            if (isBearishCandleStick
                    || ((EnumSet.of(
                                            Trend.Phase.DIP,
                                            Trend.Phase.PULLBACK,
                                            Trend.Phase.CORRECTION,
                                            Trend.Phase.DEEP_CORRECTION,
                                            Trend.Phase.TOP)
                                    .contains(phase))
                            && !isBullishCandleStick
                            && isVolumeSurge
                            && isHigher2TimeFrameHighBreakdown)) {

                subStrategyRef.set(
                        isBearishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_BREAKDOWN
                                : ResearchTechnical.SubStrategy.WEAK_BREAKDOWN);
                return true;
            }
        }
        boolean isLowerHighAndLowerLow = CandleStickUtils.isLowerHighAndLowerLow(stockPrice);

        if (isLowerHighAndLowerLow
                && relevanceService.isBearishIndicator(
                        trend, timeframe, stockPrice, stockTechnicals)) {
            subStrategyRef.set(ResearchTechnical.SubStrategy.BEARISH_INDICATORS);
            return true;
        }

        return false;
    }

    private boolean isDynamicBearishAction(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef) {

        boolean isLowerWickSizeConfirmed =
                candleStickHelperService.isLowerWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        if (!isLowerWickSizeConfirmed) {
            return false;
        }

        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (dynamicRelevanceService.isNearResistance(
                trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBearishCandleStick
                    || (!isBullishCandleStick
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongUpperWick(stockPrice)))) {

                subStrategyRef.set(
                        isBearishCandleStick
                                ? ResearchTechnical.SubStrategy.DYNAMIC_STRONG_RESISTANCE
                                : ResearchTechnical.SubStrategy.DYNAMIC_WEAK_RESISTANCE);
                return true;
            }
        }

        if (trend.getMomentum() == Trend.Phase.TOP) {
            if (candlestickPatternService.hasAtLeastTwoPatternsWithSentiment(
                    stockPrice, CandlestickPattern.Sentiment.BEARISH)) {
                subStrategyRef.set(ResearchTechnical.SubStrategy.TOP_REVERSAL);
                return true;
            }
        }

        boolean isHigherTimeFrameHighBreakdown =
                stockPriceHelperService.isHigherTimeFrameHighBreakdown(timeframe, stockPrice);
        boolean isHigher2TimeFrameHighBreakdown =
                stockPriceHelperService.isHigher2TimeFrameHighBreakdown(timeframe, stockPrice);
        if (dynamicRelevanceService.isBreakdown(trend, timeframe, stockPrice, stockTechnicals)
                && isHigherTimeFrameHighBreakdown) {
            if (isBearishCandleStick
                    || (!isBullishCandleStick
                            && isVolumeSurge
                            && isHigher2TimeFrameHighBreakdown)) {

                subStrategyRef.set(
                        isBearishCandleStick
                                ? ResearchTechnical.SubStrategy.DYNAMIC_STRONG_BREAKDOWN
                                : ResearchTechnical.SubStrategy.DYNAMIC_WEAK_BREAKDOWN);
                return true;
            }
        }
        boolean isLowerHighAndLowerLow = CandleStickUtils.isLowerHighAndLowerLow(stockPrice);

        if (isLowerHighAndLowerLow
                && dynamicRelevanceService.isBearishIndicator(
                        trend, timeframe, stockPrice, stockTechnicals)) {
            subStrategyRef.set(ResearchTechnical.SubStrategy.DYNAMIC_BEARISH_INDICATORS);
            return true;
        }

        return false;
    }
}
