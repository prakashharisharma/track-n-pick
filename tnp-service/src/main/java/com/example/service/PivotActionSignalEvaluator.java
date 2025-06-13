package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SubStrategyHelper;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


/*

1. daily price close above monthly R1
2. Volume above average on daily
3. Bullish momentum using RSI and MACD
5. Ensure no long upper wick on breakout candle

4. prev month close above monthly ma 20

6. Entry at high of breakout candle
7. Stoploss below 2% of R1
8. Target R2
 */
@Slf4j
@RequiredArgsConstructor
@Service("pivotPriceActionSignalEvaluator")
public class PivotActionSignalEvaluator implements TradeSignalEvaluator {

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private final BreakoutService breakoutService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final RsiIndicatorService rsiIndicatorService;

    private final MacdIndicatorService macdIndicatorService;

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final FormulaService formulaService;

    @Override
    public TradeSetup evaluateEntry(Timeframe timeframe, Stock stock, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe.getHigher().getHigher());
        StockTechnicals htStockTechnicals = stockTechnicalsService.get(stock, timeframe.getHigher().getHigher());
        double researchPrice = 0.0;
        if(htStockPrice!=null && htStockTechnicals!=null){
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            MovingAverageResult movingAverageResult = MovingAverageUtil.getMovingAverage(MovingAverageLength.HIGH, timeframe.getHigher().getHigher(), htStockTechnicals, false);

            if(breakoutService.isBreakOut(stockPrice, htStockPrice.getResistance1(), htStockPrice.getResistance1())){
                  if(htStockPrice.getClose() > movingAverageResult.getValue()){
                      subStrategyRef =
                              confirmBreakout(
                                      timeframe, stock, stockPrice, stockTechnicals, timeframe.getHigher().getHigher().name());
                      researchPrice =
                              this.calculateEntryPriceForBreakout(
                                      timeframe,
                                      stockPrice,
                                      stockTechnicals,
                                      htStockPrice.getResistance1());
                  }
            }

            if (subStrategyRef.isPresent()) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.PIVOT)
                        .subStrategy(subStrategyRef.get())
                        .researchPrice(researchPrice)
                        .build();
            }
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

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals, String subStrategyName) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

            boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
            boolean isStrongBody =
                    CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
            boolean isStrongLowerWick =
                    CandleStickUtils.isStrongLowerWick(stockPrice)
                            || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

            boolean isMacdConfirmingBreakout =
                    this.isMacdConfirmingBreakout(stockTechnicals)
                            || this.isMacdTurningUp(stockTechnicals);

            boolean isVolumeAboveAvg = volumeIndicatorService.isVolumeAverage(stockTechnicals);

            boolean isBullishConfirmed =
                    candleStickConfirmationService.isBullishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            boolean isCandleStickBullish =
                    isBullishConfirmed || isGapUp || isStrongBody || isStrongLowerWick;

            if (isCandleStickBullish
                    && isMacdConfirmingBreakout
                    && rsiIndicatorService.isBullish(stockTechnicals)
                    && (isVolumeAboveAvg || (stockTechnicals.getPrevRsi() < 30.0))) {

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

    private boolean isMacdTurningUp(StockTechnicals stockTechnicals) {

        return macdIndicatorService.isMacdIncreased(stockTechnicals)
                && macdIndicatorService.isSignalIncreased(stockTechnicals)
                && macdIndicatorService.isHistogramIncreased(stockTechnicals)
                && macdIndicatorService.isMacdBelowZero(stockTechnicals);
    }

    @Override
    public TradeSetup evaluateExit(Timeframe timeframe, Stock stock, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return TradeSetup.builder().active(Boolean.FALSE).build();
    }
}
