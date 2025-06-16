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

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe.getHigher().getHigher());
        StockTechnicals htStockTechnicals =
                stockTechnicalsService.get(stock, timeframe.getHigher().getHigher());
        double researchPrice = 0.0;
        if (htStockPrice != null && htStockTechnicals != null) {
            Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

            MovingAverageResult movingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            MovingAverageLength.HIGH,
                            timeframe.getHigher().getHigher(),
                            htStockTechnicals,
                            false);

            if (breakoutService.isBreakOut(
                    stockPrice, htStockPrice.getResistance1(), htStockPrice.getResistance1())) {
                if (htStockPrice.getClose() > movingAverageResult.getValue()) {
                    subStrategyRef =
                            confirmBreakout(
                                    timeframe,
                                    stock,
                                    stockPrice,
                                    stockTechnicals,
                                    timeframe.getHigher().getHigher().name());
                    researchPrice =
                            signalEvaluatorHelperService.calculateEntryPrice(
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

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            String subStrategyName) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isCurrentBreakoutConfirmation =
                signalEvaluatorHelperService.currentBreakoutConfirmation(
                        stockPrice, stockTechnicals);

        if (isCurrentBreakoutConfirmation) {

            return SubStrategyHelper.resolveByName(subStrategyName);
        }

        return Optional.empty();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakdown(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            String subStrategyName) {

        log.debug(
                "Confirming breakdown for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (CandleStickUtils.isLowerWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isCurrentBreakoutConfirmation =
                signalEvaluatorHelperService.currentBreakdownConfirmation(
                        stockPrice, stockTechnicals);

        if (isCurrentBreakoutConfirmation) {

            return SubStrategyHelper.resolveByName(subStrategyName);
        }

        return Optional.empty();
    }
}
