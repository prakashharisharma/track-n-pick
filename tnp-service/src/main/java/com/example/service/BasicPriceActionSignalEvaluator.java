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
    private final MultiTimeframeSupportResistanceService multiTimeframeSupportResistanceService;

    private final TimeframeSupportResistanceService timeframeSupportResistanceService;
    private final CandleStickConfirmationService candleStickConfirmationService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final StockPriceService<StockPrice> stockPriceService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final MacdIndicatorService macdIndicatorService;

    private final RsiIndicatorService rsiIndicatorService;

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

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
            MAEvaluationResult higherHighereValuationResult =
                    timeframeSupportResistanceService.isBreakout(
                            timeframe.getHigher().getHigher(), stockPrice, stockTechnicals);
            MAEvaluationResult higherValuationResult =
                    timeframeSupportResistanceService.isBreakout(
                            timeframe.getHigher(), stockPrice, stockTechnicals);
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
            } /* else if (higherValuationResult.isBreakout()) {
                  subStrategyRef =
                          confirmBreakout(
                                  timeframe,
                                  stock,
                                  stockPrice,
                                  stockTechnicals,
                                  timeframe.getHigher().name() + "_breakout");

                  researchPrice =
                          signalEvaluatorHelperService.calculateEntryPrice(
                                  timeframe,
                                  stockPrice,
                                  stockTechnicals,
                                  higherValuationResult.getValue());
              }*/
        } /*else if (direction == Trend.Direction.DOWN) {
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
          }*/

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
            } /*else if (timeframeSupportResistanceService.isBreakdown(
                      timeframe.getHigher(), stockPrice, stockTechnicals)) {
                  subStrategyRef =
                          confirmBreakdown(
                                  timeframe,
                                  stock,
                                  stockPrice,
                                  stockTechnicals,
                                  timeframe.getHigher().name() + "_breakdown");
              }*/
        } /*else if (direction == Trend.Direction.UP) {
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
          }*/

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

        if ((stockPrice.getClose() > highestMovingAverageResult.getValue()
                        && timeframe == Timeframe.DAILY)
                || rsiIndicatorService.isOverBought(stockTechnicals)
                || CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean currentConfirmation =
                signalEvaluatorHelperService.currentBreakoutConfirmation(
                        stockPrice, stockTechnicals);

        boolean isLowerMovingAverageIncreasing =
                MovingAverageUtil.isLowerMovingAverageIncreasing(
                        MovingAverageLength.HIGHEST, stockTechnicals, true);

        if (currentConfirmation && isLowerMovingAverageIncreasing) {

            StockTechnicals htStockTechnicals =
                    stockTechnicalsService.get(stockPrice.getStock(), timeframe.getHigher());

            boolean isHigherTimeframeConfirmation =
                    signalEvaluatorHelperService.higherTimeframeBreakoutConfirmation(
                            stockPrice, stockTechnicals, htStockTechnicals);

            if (isHigherTimeframeConfirmation) {
                return SubStrategyHelper.resolveByName(subStrategyName);
            }
        }

        return Optional.empty();
    }

    private void shiftMacd(StockTechnicals stockTechnicals) {
        stockTechnicals.setMacd(stockTechnicals.getPrevMacd());
        stockTechnicals.setPrevMacd(stockTechnicals.getPrev2Macd());
        stockTechnicals.setSignal(stockTechnicals.getPrevSignal());
        stockTechnicals.setPrevSignal(stockTechnicals.getPrev2Signal());

        stockTechnicals.setRsi(stockTechnicals.getPrevRsi());
        stockTechnicals.setPrevRsi(stockTechnicals.getPrev2Rsi());
    }

    private boolean isMacdTurningUp(StockTechnicals stockTechnicals) {

        return macdIndicatorService.isMacdIncreased(stockTechnicals)
                && macdIndicatorService.isSignalIncreased(stockTechnicals)
                && macdIndicatorService.isHistogramIncreased(stockTechnicals)
                && macdIndicatorService.isMacdBelowZero(stockTechnicals);
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
                        && signalEvaluatorHelperService.isMacdConfirmingBreakout(htStockTechnicals);

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

        if (rsiIndicatorService.isOverSold(stockTechnicals)
                || CandleStickUtils.isLowerWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean currentConfirmation =
                signalEvaluatorHelperService.currentBreakdownConfirmation(
                        stockPrice, stockTechnicals);

        if (currentConfirmation) {

            StockTechnicals htStockTechnicals =
                    stockTechnicalsService.get(stockPrice.getStock(), timeframe.getHigher());

            boolean isMacdConfirmingBreakout =
                    signalEvaluatorHelperService.isMacdConfirmingBreakout(htStockTechnicals);

            if (!isMacdConfirmingBreakout) {
                return SubStrategyHelper.resolveByName(subStrategyName);
            }
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
