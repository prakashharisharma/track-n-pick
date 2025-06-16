package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SignalEvaluatorHelperService;
import com.example.service.utils.SubStrategyHelper;
import com.example.util.FormulaService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("dynamicPriceActionSignalEvaluator")
public class DynamicPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final TimeframeSupportResistanceService timeframeSupportResistanceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockPriceHelperService stockPriceHelperService;

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

    private final CandleStickConfirmationService candleStickConfirmationService;

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

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, true);
        double researchPrice = 0.0;
        if (evaluationResultOptional.isPresent()) {

            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            if (evaluationResult.isBreakout()) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);

                researchPrice =
                        signalEvaluatorHelperService.calculateEntryPrice(
                                timeframe,
                                stockPrice,
                                stockTechnicals,
                                evaluationResult.getValue());
            } /* else if (evaluationResult.isNearSupport()) {
                  subStrategyRef =
                          confirmSupportBounce(
                                  timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
              }*/

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.DYNAMIC)
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
                subStrategyRef =
                        confirmBreakdown(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
            } /*else if (evaluationResult.isNearResistance()) {
                  subStrategyRef =
                          confirmResistanceRejection(
                                  timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
              }*/

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.DYNAMIC)
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

        if (evaluationResult.getLength() == MovingAverageLength.HIGHEST
                || rsiIndicatorService.isOverBought(stockTechnicals)
                || CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighestMovingAverageDiffValid(
                        timeframe, stockTechnicals, evaluationResult);

        boolean isHigherMovingAverageDiffValid =
                signalEvaluatorHelperService.isHigherMovingAverageDiffValid(
                        timeframe, stockTechnicals, evaluationResult);

        // We will not consider breakout for HIGHEST MA for DAILY
        if (isHighestMovingAverageDiffValid && isHigherMovingAverageDiffValid) {

            boolean isCurrentBreakoutConfirmation =
                    signalEvaluatorHelperService.currentBreakoutConfirmation(
                            stockPrice, stockTechnicals);

            if (isCurrentBreakoutConfirmation) {
                StockTechnicals htStockTechnicals =
                        stockTechnicalsService.get(stockPrice.getStock(), timeframe.getHigher());

                boolean isHigherTimeframeConfirmation =
                        signalEvaluatorHelperService.higherTimeframeBreakoutConfirmation(
                                stockPrice, stockTechnicals, htStockTechnicals);

                if (isHigherTimeframeConfirmation) {
                    return SubStrategyHelper.resolveByName(
                            evaluationResult.getLength().name() + "_breakout");
                }
            }
        }

        return Optional.empty();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmSupportBounce(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
        log.debug(
                "Confirming support bounce for stock={} timeframe={}",
                stock.getNseSymbol(),
                timeframe);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        evaluationResult.getPrevValue(), highestMovingAverageResult.getPrevValue());

        boolean isValid =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.SUPPORT, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        if (isValid) {
            boolean isUpperWickSizeConfirmed =
                    candleStickConfirmationService.isUpperWickSizeConfirmed(
                            timeframe, stockPrice, stockTechnicals);

            boolean isBullishCandleStick =
                    candleStickConfirmationService.isBullishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            boolean isVolumeSurge =
                    volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

            // TODO: prevBullishCandleStick

            if (isUpperWickSizeConfirmed && isBullishCandleStick && isVolumeSurge) {
                // return Optional.of(ResearchTechnical.SubStrategy.SUPPORT);
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_support");
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

        if (evaluationResult.getLength() == MovingAverageLength.LOWEST
                || rsiIndicatorService.isOverSold(stockTechnicals)
                || CandleStickUtils.isLowerWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isLowestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestMovingAverageDiffValid(
                        timeframe, stockTechnicals, evaluationResult);

        boolean isLowerMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowerMovingAverageDiffValid(
                        timeframe, stockTechnicals, evaluationResult);

        if (isLowestMovingAverageDiffValid && isLowerMovingAverageDiffValid) {

            boolean isCurrentBreakdownConfirmation =
                    signalEvaluatorHelperService.currentBreakdownConfirmation(
                            stockPrice, stockTechnicals);

            if (isCurrentBreakdownConfirmation) {
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_breakdown");
            }
        }

        return Optional.empty();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmResistanceRejection(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
        log.debug(
                "Confirming resistance rejection for stock={} timeframe={}",
                stock.getNseSymbol(),
                timeframe);

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        double maPercentageDiff =
                formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(), evaluationResult.getPrevValue());

        boolean isValid =
                MAThresholdsConfig.getThreshold(
                                MAInteractionType.RESISTANCE, evaluationResult.getLength())
                        .map(threshold -> maPercentageDiff >= threshold)
                        .orElse(true);

        if (isValid) {
            boolean isLowerWickSizeConfirmed =
                    candleStickConfirmationService.isLowerWickSizeConfirmed(
                            timeframe, stockPrice, stockTechnicals);

            boolean isBearishCandleStick =
                    candleStickConfirmationService.isBearishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            boolean isVolumeSurge =
                    volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

            // TODO: prevBearishCandleStick
            if (isLowerWickSizeConfirmed && isBearishCandleStick && isVolumeSurge) {
                // return Optional.of(ResearchTechnical.SubStrategy.RESISTANCE);
                return SubStrategyHelper.resolveByName(
                        evaluationResult.getLength().name() + "_resistance");
            }
        }

        return Optional.empty();
    }
}
