package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SubStrategyHelper;
import com.example.service.utils.TrendDirectionUtil;
import java.util.Optional;

import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("basicPriceActionSignalEvaluator")
public class BasicPriceActionSignalEvaluator implements TradeSignalEvaluator {
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final MultiTimeframeSupportResistanceService multiTimeframeSupportResistanceService;

    private final TimeframeSupportResistanceService timeframeSupportResistanceService;
    private final CandleStickConfirmationService candleStickConfirmationService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final StockPriceService<StockPrice> stockPriceService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final MacdIndicatorService macdIndicatorService;

    private final RsiIndicatorService rsiIndicatorService;

    private final FormulaService formulaService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        Trend.Direction direction = TrendDirectionUtil.findDirection(stockPrice);
        double researchPrice = 0.0;
        if (direction == Trend.Direction.UP) {
            MAEvaluationResult higherHighereValuationResult = timeframeSupportResistanceService.isBreakout(
                    timeframe.getHigher().getHigher(), stockPrice, stockTechnicals);
            MAEvaluationResult higherValuationResult = timeframeSupportResistanceService.isBreakout(
                    timeframe.getHigher().getHigher(), stockPrice, stockTechnicals);
            if (higherHighereValuationResult.isBreakout()) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().getHigher().name() + "_breakout");


                researchPrice =
                        this.calculateEntryPriceForBreakout(
                                timeframe,
                                stockPrice,
                                stockTechnicals,
                                higherHighereValuationResult.getValue());



            } else if (higherValuationResult.isBreakout()) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().name() + "_breakout");

                researchPrice =
                        this.calculateEntryPriceForBreakout(
                                timeframe,
                                stockPrice,
                                stockTechnicals,
                                higherValuationResult.getValue());


            }

        } else if (direction == Trend.Direction.DOWN) {
            if (timeframeSupportResistanceService.isNearSupport(
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
                    .researchPrice(researchPrice)
                    .strategy(ResearchTechnical.Strategy.BASIC)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    public double calculateEntryPriceForBreakout(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double breakoutValue) {

        boolean isUpperWickClean =
                candleStickConfirmationService.isUpperWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);
        boolean isHistogramAboveZero = macdIndicatorService.isHistogramAboveZero(stockTechnicals);

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();

        double entryPrice = (open + high + low + close) / 4.0;

        entryPrice = entryPrice * 1.00382;

        // 1. Clean upper wick and RSI above 60
        if (isUpperWickClean && Math.ceil(stockTechnicals.getRsi()) >= 60.0) {
            entryPrice = high;
        }

        // 2. Clean upper wick and Body above breakout level
        else if (open > breakoutValue && close > breakoutValue && isUpperWickClean) {
            entryPrice = (high + close) / 2.0;
        }

        // 3. Body above breakout level
        else if (open > breakoutValue && close > breakoutValue) {
            entryPrice = (open + close) / 2.0;
            entryPrice = entryPrice * 1.00382;
        }

        // 4. Clean upper wick and bullish momentum
        else if (isUpperWickClean && isHistogramAboveZero) {
            entryPrice = (high + close) / 2.0;
        }

        // 5. Default to average price
        return formulaService.ceilToNearestHalf(entryPrice);
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
                    .strategy(ResearchTechnical.Strategy.BASIC)
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

        log.debug(
                "Confirming breakout for stock={} timeframe={}",
                stock.getNseSymbol(),
                stockPrice.getTimeframe());

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        if (stockPrice.getClose() > highestMovingAverageResult.getValue()
                || rsiIndicatorService.isOverBought(stockTechnicals)
                || CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);

        boolean isStrongBody =
                CandleStickUtils.isStrongBody(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        boolean isStrongLowerWick =
                CandleStickUtils.isStrongLowerWick(stockPrice)
                        || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

        boolean isVolumeAboveAvg = volumeIndicatorService.isVolumeAverage(stockTechnicals);

        boolean isMovingAverageConfirmingBreakout =
                isMovingAverageConfirmingBreakout(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        boolean isBullishCandleStick =
                candleStickConfirmationService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, false);

        boolean isMacdConfirmingBreakout =
                this.isMacdConfirmingBreakout(stockTechnicals)
                        || this.isMacdTurningUp(stockTechnicals);

        boolean currentConfirmation =
                (isVolumeAboveAvg || (stockTechnicals.getPrevRsi() < 30.0))
                        && (isBullishCandleStick
                                || isGapUp
                                || isStrongBody
                                || isStrongLowerWick
                                || isMovingAverageConfirmingBreakout)
                        && rsiIndicatorService.isBullish(stockTechnicals)
                && isMacdConfirmingBreakout;

            if (currentConfirmation ) {
                if (this.higherTimeframeConfirmation(stockPrice, stockTechnicals)) {
                    return SubStrategyHelper.resolveByName(subStrategyName);
                }
            }

        return Optional.empty();
    }

    public boolean higherTimeframeConfirmation(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();
        Timeframe htTimeframe = stockPrice.getTimeframe().getHigher();
        StockTechnicals htStockTechnicals = stockTechnicalsService.get(stock, htTimeframe);

        boolean isHigherTimeframeRsiAndMacdBullish =
                (this.isMacdConfirmingBreakout(htStockTechnicals)
                        || this.isMacdTurningUp(htStockTechnicals))
                        && rsiIndicatorService.isBullish(htStockTechnicals);

        return isHigherTimeframeRsiAndMacdBullish;
    }

    private void shiftMacd(StockTechnicals stockTechnicals) {
        stockTechnicals.setMacd(stockTechnicals.getPrevMacd());
        stockTechnicals.setPrevMacd(stockTechnicals.getPrev2Macd());
        stockTechnicals.setSignal(stockTechnicals.getPrevSignal());
        stockTechnicals.setPrevSignal(stockTechnicals.getPrev2Signal());

        stockTechnicals.setRsi(stockTechnicals.getPrevRsi());
        stockTechnicals.setPrevRsi(stockTechnicals.getPrev2Rsi());
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

    private boolean isMacdTurningUp(StockTechnicals stockTechnicals) {

        return macdIndicatorService.isMacdIncreased(stockTechnicals)
                && macdIndicatorService.isSignalIncreased(stockTechnicals)
                && macdIndicatorService.isHistogramIncreased(stockTechnicals)
                && macdIndicatorService.isMacdBelowZero(stockTechnicals);
    }

    private boolean isMacdAndRsiConfirmingBreakout(StockTechnicals st) {

        if (st == null) {
            return false;
        }

        double macd = st.getMacd();
        double signal = st.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd < signal && macdIndicatorService.isHistogramBelowZero(st)) {
            return rsiIndicatorService.isBullish(st)
                    && macdIndicatorService.isSignalDecreased(st)
                    && macdIndicatorService.isHistogramIncreased(st);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(st);
    }

    private boolean isMovingAverageConfirmingBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, true);
        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            return ((evaluationResult.getLength() != MovingAverageLength.HIGHEST))
                    && evaluationResult.isBreakout();
        }
        return false;
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

        boolean isBullishCandleStick =
                candleStickConfirmationService.isBullishConfirmed(
                        timeframe, stockPrice, stockTechnicals, true);

        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        // TODO: prevBullishCandleStick

        StockTechnicals htStockTechnicals = stockTechnicalsService.get(stock, timeframe);
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);
        boolean isNewerSession =
                htStockPrice.getSessionDate().isBefore(stockPrice.getSessionDate());

        // Shift MACD history if older HTF candle is being used
        if (!isNewerSession) {
            shiftMacd(htStockTechnicals);
        }
        boolean htConfirmation =
                rsiIndicatorService.isBullish(htStockTechnicals)
                        && isMacdTurningUp(htStockTechnicals);

        if (isBullishCandleStick && isVolumeSurge && htConfirmation) {

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
