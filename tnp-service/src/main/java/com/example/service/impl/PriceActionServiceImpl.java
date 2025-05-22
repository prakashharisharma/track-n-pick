package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.BreakoutLedger;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PriceActionServiceImpl implements PriceActionService {

    @Autowired private SupportResistanceUtilService supportResistanceService;
    @Autowired private CandleStickConfirmationService candleStickHelperService;

    @Autowired private VolumeIndicatorService volumeIndicatorService;

    @Autowired private CandleStickService candleStickService;

    @Autowired private BreakoutLedgerService breakoutLedgerService;

    @Autowired private StockPriceHelperService stockPriceHelperService;
    @Autowired private RelevanceService relevanceService;
    @Autowired private DynamicRelevanceService dynamicRelevanceService;
    @Autowired private FormulaService formulaService;
    @Autowired private TrendService trendService;

    @Autowired private DynamicTrendService dynamicTrendService;

    @Autowired private RsiIndicatorService rsiIndicatorService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

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
                candleStickHelperService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (relevanceService.isNearSupport(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBullishCandleStick
                    || ((EnumSet.of(
                                            Trend.Phase.DIP,
                                            Trend.Phase.PULLBACK,
                                            Trend.Phase.CORRECTION,
                                            Trend.Phase.DEEP_CORRECTION)
                                    .contains(phase))
                            && !isBearishCandleStick
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongLowerWick(stockPrice)
                                    || CandleStickUtils.isGreen(stockPrice)))) {

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
                                            Trend.Phase.STRONG_ADVANCE)
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
                candleStickHelperService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (dynamicRelevanceService.isNearSupport(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBullishCandleStick
                    || (!isBearishCandleStick
                            && isVolumeSurge
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongLowerWick(stockPrice)
                                    || CandleStickUtils.isGreen(stockPrice)))) {

                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.DYNAMIC_STRONG_SUPPORT
                                : ResearchTechnical.SubStrategy.DYNAMIC_WEAK_SUPPORT);
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

        if (isHigherHighHigherLow
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
                candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals);

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (relevanceService.isNearResistance(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBearishCandleStick
                    || ((EnumSet.of(
                                            Trend.Phase.RECOVERY,
                                            Trend.Phase.ADVANCE,
                                            Trend.Phase.STRONG_ADVANCE,
                                            Trend.Phase.TOP)
                                    .contains(phase))
                            && !isBullishCandleStick
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongUpperWick(stockPrice)
                                    || CandleStickUtils.isRed(stockPrice)))) {

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
                                            Trend.Phase.DEEP_CORRECTION)
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
                candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals);

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (dynamicRelevanceService.isNearResistance(
                trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBearishCandleStick
                    || (!isBullishCandleStick
                            && isVolumeSurge
                            && (CandleStickUtils.isStrongUpperWick(stockPrice)
                                    || CandleStickUtils.isRed(stockPrice)))) {

                subStrategyRef.set(
                        isBearishCandleStick
                                ? ResearchTechnical.SubStrategy.DYNAMIC_STRONG_RESISTANCE
                                : ResearchTechnical.SubStrategy.DYNAMIC_WEAK_RESISTANCE);
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
