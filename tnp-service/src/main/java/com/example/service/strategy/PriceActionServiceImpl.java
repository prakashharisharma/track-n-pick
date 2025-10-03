package com.example.service.strategy;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.*;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.SignalEvaluatorHelperService;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("priceActionService")
public class PriceActionServiceImpl implements TradeSignalEvaluator {
    private final CandleStickConfirmationService candleStickHelperService;
    private final CandleStickConfirmationService candleStickConfirmationService;
    private final AdxIndicatorService adxIndicatorService;

    private final ResistanceValidationService resistanceValidationService;
    private final VolumeIndicatorService volumeIndicatorService;

    private final CandleStickService candleStickService;

    private final StockPriceHelperService stockPriceHelperService;
    private final RelevanceService relevanceService;

    private final TrendService trendService;

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        if (candleStickService.isDead(stockPrice)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;
        Trend trend = trendService.detect(stock, timeframe);

        AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef = new AtomicReference<>();

        log.info(
                "{} [{}]: Scanning price action breakout | Direction: {}, Momentum: {}",
                stock.getNseSymbol(),
                timeframe,
                trend.getDirection(),
                trend.getMomentum());
        isCandleActive =
                this.isBullishAction(trend, timeframe, stockPrice, stockTechnicals, subStrategyRef);

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

        if (timeframe != Timeframe.MONTHLY) {
            if (!signalEvaluatorHelperService.isHigherTimeframeConfirmed(stockTechnicals, false)) {
                return false;
            }
        }

        boolean isBullishConfirmed =
                candleStickConfirmationService.isBullishConfirmed(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, false);

        boolean checkHigherTimeFrameResistance =
                (isBullishConfirmed)
                                && (CandleStickUtils.isProGapUp(stockPrice)
                                        || adxIndicatorService.isBullishIncr(stockTechnicals))
                        ? false
                        : true;

        boolean isHigherTimeframeResistanceCheckPassed =
                (checkHigherTimeFrameResistance
                        ? resistanceValidationService.isOutsideResistanceZone(stockPrice)
                        : true);
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
            if (isHigherTimeframeResistanceCheckPassed && isBullishCandleStick
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
                                : ResearchTechnical.SubStrategy.SUPPORT);
                return true;
            }
        }

        if (isHigherTimeframeResistanceCheckPassed
                && relevanceService.isBreakout(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBullishCandleStick
                    || ((EnumSet.of(
                                            Trend.Phase.TOP,
                                            Trend.Phase.RECOVERY,
                                            Trend.Phase.ADVANCE,
                                            Trend.Phase.STRONG_ADVANCE,
                                            Trend.Phase.EARLY_RECOVERY)
                                    .contains(phase))
                            && !isBearishCandleStick
                            && isVolumeSurge)) {

                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_BREAKOUT
                                : ResearchTechnical.SubStrategy.BREAKOUT);
                return true;
            }
        }

        return false;
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return TradeSetup.builder().active(Boolean.FALSE).build();
    }
}
