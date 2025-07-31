package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SignalEvaluatorHelperService;
import com.example.service.utils.SubStrategyHelper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("hybridPriceActionSignalEvaluator")
public class HybridPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final EvaluationLogService evaluationLogService;
    private final RsiIndicatorService rsiIndicatorService;

    private final AdxIndicatorService adxIndicatorService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, true);
        double researchPrice = 0.0;
        if (evaluationResultOptional.isPresent()) {

            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakout()) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.POSITIVE,
                        timeframe.name()
                                + "-"
                                + ResearchTechnical.Strategy.HYBRID.name()
                                + " breakout found"
                                + " on "
                                + evaluationResult.getLength());

                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);

                researchPrice =
                        signalEvaluatorHelperService.calculateEntryPrice(
                                timeframe,
                                stockPrice,
                                stockTechnicals,
                                evaluationResult.getValue());
            }

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.HYBRID)
                        .subStrategy(subStrategyRef.get())
                        .researchPrice(researchPrice)
                        .build();
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, true);

        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakdown()) {

                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEGATIVE,
                        timeframe.name()
                                + "-"
                                + ResearchTechnical.Strategy.HYBRID.name()
                                + " breakdown found"
                                + " on "
                                + evaluationResult.getLength());

                subStrategyRef =
                        confirmBreakdown(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
            }

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.HYBRID)
                        .subStrategy(subStrategyRef.get())
                        .build();
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

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

        if (!CandleStickUtils.isHigherHigh(stockPrice)) {
            return Optional.empty();
        }

        if (signalEvaluatorHelperService.isHighestAlsoBreached(
                timeframe,
                stockPrice,
                stockTechnicals,
                evaluationResult.getLength(),
                evaluationResult.getValue(),
                true)) {
            return Optional.empty();
        }

        if (evaluationResult.getLength() == MovingAverageLength.HIGHEST
                && evaluationResult.getLength().getMaDays() == 5) {
            boolean isAllMAsIncreasing = MovingAverageUtil.isAllMAsIncreasing(stockTechnicals);

            if (!isAllMAsIncreasing) {
                return Optional.empty();
            }
        }

        StockPrice htStockPrice =
                stockPriceService.get(stock, stockPrice.getTimeframe().getHigher());

        StockTechnicals htStockTechnicals =
                stockTechnicalsService.get(stock, stockPrice.getTimeframe().getHigher());

        boolean isHtLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe.getHigher(),
                        htStockPrice,
                        htStockTechnicals,
                        MAInteractionType.BREAKOUT,
                        true);

        boolean isNearestMovingAverageDiffValidForBreakout =
                signalEvaluatorHelperService.isNearestMovingAverageDiffValidForBreakout(
                        timeframe, stockTechnicals, evaluationResult, true);

        boolean isLongerMAsAlignBullish =
                MovingAverageUtil.isLongerMaAlignedBullish(
                        evaluationResult.getLength(), timeframe, stockTechnicals);

        if ((isHtLowestAndHighestMovingAverageDiffValid || isLongerMAsAlignBullish)
                && isNearestMovingAverageDiffValidForBreakout) {
            if (stockPrice.getClose() > htStockPrice.getHigh()) {
                boolean currentConfirmation =
                        signalEvaluatorHelperService.currentBreakoutConfirmation(
                                stockPrice, stockTechnicals);
                if (currentConfirmation && adxIndicatorService.isBullish(stockTechnicals)) {

                    return SubStrategyHelper.resolveByName(
                            timeframe.getHigher().name() + "_breakout");
                }
            }
        }

        return Optional.empty();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakdown(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
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

        StockPrice htStockPrice =
                stockPriceService.get(stock, stockPrice.getTimeframe().getHigher());

        StockTechnicals htStockTechnicals =
                stockTechnicalsService.get(stock, stockPrice.getTimeframe().getHigher());
        boolean isLowestMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighestAndLowestMovingAverageDiffValid(
                        timeframe,
                        htStockPrice,
                        htStockTechnicals,
                        MAInteractionType.BREAKDOWN,
                        true);

        boolean isNearestMovingAverageDiffValidForBreakdown =
                signalEvaluatorHelperService.isNearestMovingAverageDiffValidForBreakdown(
                        timeframe, stockTechnicals, evaluationResult, true);
        boolean isLongerMAsAlignBearish =
                MovingAverageUtil.isLongerMaAlignedBearish(
                        evaluationResult.getLength(), timeframe, stockTechnicals);

        if ((isLowestMovingAverageDiffValid || isLongerMAsAlignBearish)
                && isNearestMovingAverageDiffValidForBreakdown) {
            if (stockPrice.getClose() < htStockPrice.getLow()) {
                boolean isCurrentBreakdownConfirmation =
                        signalEvaluatorHelperService.currentBreakdownConfirmation(
                                stockPrice, stockTechnicals);

                if (isCurrentBreakdownConfirmation) {
                    return SubStrategyHelper.resolveByName(
                            evaluationResult.getLength().name() + "_breakdown");
                }
            }
        }

        return Optional.empty();
    }
}
