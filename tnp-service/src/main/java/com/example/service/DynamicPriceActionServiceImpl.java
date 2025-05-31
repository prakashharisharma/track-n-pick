package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DynamicPriceActionServiceImpl implements DynamicPriceActionService {

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private final StockPriceService<StockPrice> stockPriceService;
    private final MacdIndicatorService macdIndicatorService;

    private final BreakoutService breakoutService;

    private final FormulaService formulaService;

    @Override
    public TradeSetup breakOut(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        if (formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue())
                >= 10.0) {

            double average = lowestMovingAverageResult.getValue();

            double prevAverage = lowestMovingAverageResult.getPrevValue();

            boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
            boolean isStrongBody =
                    CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
            boolean isStrongLowerWick =
                    CandleStickUtils.isStrongLowerWick(stockPrice)
                            || CandleStickUtils.isPrevStrongLowerWick(stockPrice);

            boolean isBreakout =
                    (isGapUp || isStrongBody || isStrongLowerWick)
                            && breakoutService.isBreakOut(stockPrice, average, prevAverage);

            if ((isBreakout) && this.isMacdConfirmingBreakout(stockTechnicals)) {

                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.PRICE)
                        .subStrategy(ResearchTechnical.SubStrategy.BOTTOM_BREAKOUT)
                        .build();
            }
        }
        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private boolean isMacdConfirmingBreakout(StockTechnicals st) {

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

    @Override
    public TradeSetup breakDown(Stock stock, Timeframe timeframe) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        MovingAverageResult lowestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        if (formulaService.calculateChangePercentage(
                        lowestMovingAverageResult.getPrevValue(),
                        highestMovingAverageResult.getPrevValue())
                >= 10.0) {

            double average = highestMovingAverageResult.getValue();
            double prevAverage = highestMovingAverageResult.getPrevValue();
            boolean isGapDown = CandleStickUtils.isGapUp(stockPrice);
            boolean isStrongBody =
                    CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);
            boolean isStrongUpperWick =
                    CandleStickUtils.isStrongUpperWick(stockPrice)
                            || CandleStickUtils.isPrevStrongUpperWick(stockPrice);
            boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPrice);

            boolean isBreakdown =
                    (isGapDown || isStrongBody || isStrongUpperWick || isPrevRed)
                            && breakoutService.isBreakDown(stockPrice, average, prevAverage);
            if (isBreakdown) {
                return TradeSetup.builder()
                        .active(Boolean.TRUE)
                        .strategy(ResearchTechnical.Strategy.PRICE)
                        .subStrategy(ResearchTechnical.SubStrategy.TOP_BREAKDOWN)
                        .build();
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }
}
