package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.SubStrategyHelper;
import com.example.service.utils.TrendDirectionUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("basicPriceActionSignalEvaluator")
public class BasicPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final MultiTimeframeSupportResistanceService multiTimeframeSupportResistanceService;

    private final TimeframeSupportResistanceService timeframeSupportResistanceService;
    private final CandleStickConfirmationService candleStickConfirmationService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final MacdIndicatorService macdIndicatorService;

    private final RsiIndicatorService rsiIndicatorService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        Trend.Direction direction = TrendDirectionUtil.findDirection(stockPrice);
        if (direction == Trend.Direction.UP) {
            if (multiTimeframeSupportResistanceService.isBreakout(
                    timeframe, stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                "multi_timeframe_breakout");
            } else if (timeframeSupportResistanceService.isBreakout(
                    timeframe.getHigher().getHigher(), stockPrice, stockTechnicals, true)) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().getHigher().name() + "_breakout");
            } else if (timeframeSupportResistanceService.isBreakout(
                    timeframe.getHigher(), stockPrice, stockTechnicals, true)) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().name() + "_breakout");
            }

        } else if (direction == Trend.Direction.DOWN) {
            if (multiTimeframeSupportResistanceService.isNearSupport(
                    timeframe, stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmSupportBounce(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                "multi_timeframe_support");
            } else if (timeframeSupportResistanceService.isNearSupport(
                    timeframe.getHigher().getHigher(), stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmSupportBounce(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().getHigher().name() + "_support");
            } else if (timeframeSupportResistanceService.isNearSupport(
                    timeframe.getHigher(), stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmSupportBounce(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().name() + "_support");
            }
        }

        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.LEVEL)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

        Trend.Direction direction = TrendDirectionUtil.findDirection(stockPrice);

        if (direction == Trend.Direction.DOWN) {
            if (multiTimeframeSupportResistanceService.isBreakdown(
                    timeframe, stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmBreakdown(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                "multi_timeframe_breakdown");
            } else if (timeframeSupportResistanceService.isBreakdown(
                    timeframe.getHigher().getHigher(), stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmBreakdown(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().getHigher().name() + "_breakdown");
            } else if (timeframeSupportResistanceService.isBreakdown(
                    timeframe.getHigher(), stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmBreakdown(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().name() + "_breakdown");
            }
        } else if (direction == Trend.Direction.UP) {
            if (multiTimeframeSupportResistanceService.isNearResistance(
                    timeframe, stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmResistanceRejection(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                "multi_timeframe_resistance");
            } else if (timeframeSupportResistanceService.isNearResistance(
                    timeframe.getHigher().getHigher(), stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmResistanceRejection(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().getHigher().name() + "_resistance");
            } else if (timeframeSupportResistanceService.isNearResistance(
                    timeframe.getHigher(), stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmResistanceRejection(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().name() + "_resistance");
            }
        }

        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.LEVEL)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            String subStrategyName) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isStrongLowerWick =
                CandleStickUtils.isStrongLowerWick(stockPrice)
                        || CandleStickUtils.isPrevStrongLowerWick(stockPrice);
        boolean isUpperWickSizeConfirmed =
                candleStickConfirmationService.isUpperWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        if ((isGapUp || isStrongBody || isStrongLowerWick) && isUpperWickSizeConfirmed) {

            return SubStrategyHelper.resolveByName(subStrategyName);
        }

        return Optional.empty();
    }

    private boolean isMacdConfirmingBreakout(StockTechnicals st) {

        if (st == null) {
            return false;
        }

        double macd = st.getMacd();
        double signal = st.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd < signal && macdIndicatorService.isHistogramBelowZero(st)) {
            return macdIndicatorService.isMacdIncreased(st)
                    && macdIndicatorService.isSignalDecreased(st)
                    && macdIndicatorService.isHistogramIncreased(st);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(st);
    }

    private Optional<ResearchTechnical.SubStrategy> confirmSupportBounce(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            String subStrategyName) {
        log.debug(
                "Confirming support bounce for stock={} timeframe={}",
                stock.getNseSymbol(),
                timeframe);

        boolean isUpperWickSizeConfirmed =
                candleStickConfirmationService.isUpperWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        boolean isBullishCandleStick =
                candleStickConfirmationService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        // TODO: prevBullishCandleStick

        if (isUpperWickSizeConfirmed
                && isBullishCandleStick
                && isVolumeSurge
                && rsiIndicatorService.isBullish(stockTechnicals)) {

            return SubStrategyHelper.resolveByName(subStrategyName);
        }

        return Optional.empty();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakdown(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            String subStrategyName) {
        log.debug(
                "Confirming breakdown for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        boolean isGapDown = CandleStickUtils.isGapUp(stockPrice);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
        boolean isStrongUpperWick =
                CandleStickUtils.isStrongUpperWick(stockPrice)
                        || CandleStickUtils.isPrevStrongUpperWick(stockPrice);

        boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPrice);

        if ((isGapDown || isStrongBody || isStrongUpperWick || isPrevRed)
                && rsiIndicatorService.isBearish(stockTechnicals)) {
            return SubStrategyHelper.resolveByName(subStrategyName);
        }

        return Optional.empty();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmResistanceRejection(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            String subStrategyName) {
        log.debug(
                "Confirming resistance rejection for stock={} timeframe={}",
                stock.getNseSymbol(),
                timeframe);

        boolean isLowerWickSizeConfirmed =
                candleStickConfirmationService.isLowerWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        boolean isBearishCandleStick =
                candleStickConfirmationService.isBearishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        // TODO: prevBearishCandleStick
        if (isLowerWickSizeConfirmed
                && isBearishCandleStick
                && isVolumeSurge
                && rsiIndicatorService.isOverBought(stockTechnicals)) {

            return SubStrategyHelper.resolveByName(subStrategyName);
        }

        return Optional.empty();
    }
}
