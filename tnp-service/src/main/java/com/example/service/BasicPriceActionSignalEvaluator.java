package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("basicPriceActionSignalEvaluator")
public class BasicPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final ResistanceValidationService resistanceValidationService;
    private final TimeframeSupportResistanceService timeframeSupportResistanceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final RsiIndicatorService rsiIndicatorService;

    private final AdxIndicatorService adxIndicatorService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        double researchPrice = 0.0;
        MAEvaluationResult higherHighereValuationResult =
                timeframeSupportResistanceService.isBreakout(
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
                    signalEvaluatorHelperService.calculateEntryPrice(
                            timeframe,
                            stockPrice,
                            stockTechnicals,
                            higherHighereValuationResult.getValue());
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

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

        Trend.Direction direction = TrendDirectionUtil.findDirection(stockPrice);

        if (direction == Trend.Direction.DOWN) {

            if (timeframeSupportResistanceService.isBreakdown(
                    timeframe.getHigher().getHigher(), stockPrice, stockTechnicals)) {
                subStrategyRef =
                        confirmBreakdown(
                                timeframe,
                                stock,
                                stockPrice,
                                stockTechnicals,
                                timeframe.getHigher().getHigher().name() + "_breakdown");
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

        if (rsiIndicatorService.isOverBought(stockTechnicals)
                || (CandleStickUtils.isUpperWickDominant(stockPrice)
                                && CandleStickUtils.isStrongRange(
                                        timeframe, stockPrice, stockTechnicals)
                        || (CandleStickUtils.upperWickSize(stockPrice)
                                >= CandleStickUtils.bodySize(stockPrice) * 2))) {
            return Optional.empty();
        }

        if (timeframe.getPriority() > 1) {
            StockPrice dailyStockPrice = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals dailyStockTechnicals =
                    stockTechnicalsService.get(stock, Timeframe.DAILY);
            if (rsiIndicatorService.isOverBought(dailyStockTechnicals)
                    || (CandleStickUtils.isUpperWickDominant(dailyStockPrice)
                            && (CandleStickUtils.isStrongRange(
                                            Timeframe.DAILY, dailyStockPrice, dailyStockTechnicals)
                                    || (CandleStickUtils.upperWickSize(dailyStockPrice)
                                            >= 2 * CandleStickUtils.bodySize(dailyStockPrice))))) {
                return Optional.empty();
            }
        }

        if (CandleStickUtils.isRed(stockPrice)) {
            return Optional.empty();
        }

        boolean isRangeHigherThanPrevSessionRange =
                CandleStickUtils.prevSessionRange(stockPrice) < CandleStickUtils.range(stockPrice);
        boolean isBodyHigherThanPrevSessionBody =
                CandleStickUtils.prevSessionBodySize(stockPrice)
                        < CandleStickUtils.bodySize(stockPrice);

        boolean isRangeOrBodyConfirmed =
                isRangeHigherThanPrevSessionRange || isBodyHigherThanPrevSessionBody;

        if (!isRangeOrBodyConfirmed) {
            return Optional.empty();
        }

        int incrMACount = MovingAverageUtil.increasingMaCount(stockTechnicals);

        if (incrMACount == 5) {
            return Optional.empty();
        }

        MovingAverageResult movingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);
        MovingAverageResult fiveMamovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, false);

        // boolean isAllMAsIncreasing = MovingAverageUtil.isAllMAsIncreasing(stockTechnicals);

        if (movingAverageResult.getValue() == fiveMamovingAverageResult.getValue()) {
            if (!adxIndicatorService.isBullishIncr(stockTechnicals)
                    || !resistanceValidationService.isOutsideResistanceZone(stockPrice)) {
                return Optional.empty();
            }
        }

        boolean currentConfirmation =
                signalEvaluatorHelperService.currentBreakoutConfirmation(
                        stockPrice, stockTechnicals);

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        int increasingMaCount = MovingAverageUtil.increasingMaCount(stockTechnicals);

        if (isLowestAndHighestMovingAverageDiffValid
                && currentConfirmation
                && increasingMaCount >= 3) {

            StockPrice htStockPrice =
                    stockPriceService.get(stockPrice.getStock(), timeframe.getHigher().getHigher());

            StockTechnicals htStockTechnicals =
                    stockTechnicalsService.get(
                            stockPrice.getStock(), timeframe.getHigher().getHigher());

            boolean isHigherTimeframeConfirmation =
                    signalEvaluatorHelperService.higherTimeframeBreakoutConfirmation(
                            htStockPrice, htStockTechnicals);

            if (isHigherTimeframeConfirmation) {

                return SubStrategyHelper.resolveByName(subStrategyName);
            }
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

        if (rsiIndicatorService.isOverSold(stockTechnicals)
                || (CandleStickUtils.isLowerWickDominant(stockPrice)
                                && (CandleStickUtils.isStrongRange(
                                        timeframe, stockPrice, stockTechnicals))
                        || CandleStickUtils.lowerWickSize(stockPrice)
                                >= 2 * CandleStickUtils.bodySize(stockPrice))) {
            return Optional.empty();
        }

        boolean currentConfirmation =
                signalEvaluatorHelperService.currentBreakdownConfirmation(
                        stockPrice, stockTechnicals);

        boolean isHigherMovingAverageIncreasing =
                MovingAverageUtil.isHigherMovingAverageDecreasing(
                        MovingAverageLength.LOWEST, stockTechnicals, true);

        boolean isHighestAndLowestMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighestAndLowestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        if (isHighestAndLowestMovingAverageDiffValid
                && isHigherMovingAverageIncreasing
                && currentConfirmation) {

            StockTechnicals htStockTechnicals =
                    stockTechnicalsService.get(stockPrice.getStock(), timeframe.getHigher());

            boolean isHigherTimeframeConfirmation =
                    signalEvaluatorHelperService.higherTimeframeBreakdownConfirmation(
                            stockPrice, stockTechnicals, htStockTechnicals);

            if (isHigherTimeframeConfirmation) {
                return SubStrategyHelper.resolveByName(subStrategyName);
            }
        }

        return Optional.empty();
    }
}
