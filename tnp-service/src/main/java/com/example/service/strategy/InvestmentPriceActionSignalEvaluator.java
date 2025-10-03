package com.example.service.strategy;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.utils.*;
import com.example.util.FormulaService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("investmentPriceActionSignalEvaluator")
public class InvestmentPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final FormulaService formulaService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

        if (TrendDirectionUtil.findDirection(stockPrice) == Trend.Direction.DOWN
                || TrendDirectionUtil.findDirection(
                                stockPriceService.buildPrevSessionStockPrice(stockPrice))
                        == Trend.Direction.DOWN) {
            // System.out.println("Log1 " + stock.getNseSymbol());
            if (stockPrice.getClose()
                    < MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals)) {
                if (stockPrice.getClose()
                        < MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals)) {
                    // System.out.println("Log2 " + stock.getNseSymbol());
                    subStrategyRef = confirmBreakout(timeframe, stock, stockPrice, stockTechnicals);
                }
            }
        }

        if (subStrategyRef.isPresent()) {

            double researchPrice = stockPrice.getHigh() + stockPrice.getLow() / 2;

            researchPrice = Math.min(researchPrice, stockPrice.getClose());

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.INVESTMENT)
                    .subStrategy(subStrategyRef.get())
                    .researchPrice(researchPrice)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (timeframe != Timeframe.MONTHLY) {
            if (!signalEvaluatorHelperService.isHigherTimeframeConfirmed(stockTechnicals, true)) {
                return Optional.empty();
            }
        }

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        boolean isStrongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isHighestAndHighMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        boolean isLowestAndLowMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndLowMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        boolean isCloseAndLowestMovingAverageDiffValid =
                (formulaService.calculateChangePercentage(
                                lowestMovingAverageResult.getValue(),
                                Math.min(stockPrice.getClose(), stockPrice.getOpen()))
                        >= 2.0); // && stockPrice.getHigh() < lowestMovingAverageResult.getValue();

        if (!isLowestAndLowMovingAverageDiffValid && !isCloseAndLowestMovingAverageDiffValid) {
            return Optional.empty();
        }

        if (!isHighestAndHighMovingAverageDiffValid) {
            return Optional.empty();
        }

        // System.out.println("Log3 " + stock.getNseSymbol());

        if (!volumeIndicatorService.isMinVolume(stockTechnicals, 2.0)) {
            return Optional.empty();
        }

        // System.out.println("Log4 " + stock.getNseSymbol());

        if (!volumeIndicatorService.isMinVolumeAvg(stockTechnicals, 1.0)) {
            return Optional.empty();
        }

        StockPrice prevStockPrice = stockPriceService.buildPrevSessionStockPrice(stockPrice);

        boolean isPrevLowerHighAndLowerLow =
                CandleStickUtils.isLowerHigh(prevStockPrice)
                        && CandleStickUtils.isLowerLow(prevStockPrice);

        if (!isPrevLowerHighAndLowerLow) {
            return Optional.empty();
        }

        // System.out.println("Log5 " + stock.getNseSymbol());
        if (isLowestAndHighestMovingAverageDiffValid && (isStrongRange || isStrongBody)) {
            // System.out.println("Log6 " + stock.getNseSymbol());
            boolean isBullishConfirmed =
                    candleStickConfirmationService.isBullishConfirmed(
                            timeframe, stockPrice, stockTechnicals, false);

            boolean isOverSold =
                    stockTechnicals.getRsi() < 25.0 || stockTechnicals.getPrevRsi() < 25.0;

            boolean isHigherHighAndHigherLow =
                    CandleStickUtils.isHigherHigh(stockPrice)
                            && CandleStickUtils.isHigherLow(stockPrice);

            if (isHigherHighAndHigherLow
                    || CandleStickUtils.isStrongLowerWick(stockPrice)
                    || CandleStickUtils.isLowerWickDominant(stockPrice)
                    || isBullishConfirmed
                    || isOverSold) {
                // System.out.println("Log7 " + stock.getNseSymbol());
                if (volumeIndicatorService.isVolumeSurge(stockTechnicals)) {
                    if (stockTechnicals.getVolume() > 1.5 * stockTechnicals.getVolumeAvg20()) {
                        if ((stockTechnicals.getVolume() > 1.5 * stockTechnicals.getPrevVolume())
                                || (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume()
                                        && stockTechnicals.getPrevVolume()
                                                > 1.5 * stockTechnicals.getPrevVolumeAvg20())) {
                            System.out.println("Log8 " + stock.getNseSymbol());
                            return SubStrategyHelper.resolveByName("support");
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return null;
    }
}
