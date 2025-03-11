package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SwingActionServiceImpl implements SwingActionService {


    @Autowired
    private SupportResistanceUtilService supportResistanceService;
    @Autowired
    private CandleStickHelperService candleStickHelperService;
    @Autowired
    private MacdIndicatorService macdIndicatorService;
    @Autowired
    private CandleStickService candleStickService;
    @Autowired
    private RsiIndicatorService rsiTrendService;
    @Autowired
    private StockPriceService stockPriceService;

    @Autowired
    private BreakoutService breakoutService;
    @Autowired
    private AdxIndicatorService adxService;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private VolumeService volumeService;

    @Autowired
    private VolumeIndicatorService volumeIndicatorService;
    @Autowired
    private TrendService trendService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;

    @Autowired
    private ObvIndicatorService obvIndicatorService;

    @Autowired
    private AdxIndicatorService adxIndicatorService;

    /**
     * Volume > Weekly && Volume > Monthly
     * if RSISwing
     * Or AdxSwing
     * @param stock
     * @return
     */
    @Override
    public TradeSetup breakOut(Stock stock) {
        Trend trend = trendService.isUpTrend(stock);
        if(trend.getMomentum() == Momentum.RECOVERY || trend.getMomentum() == Momentum.ADVANCE) {
            //if (volumeService.isVolumeAboveWeeklyAndMonthlyAverage(stock, 3.0, 1.5) || volumeService.isVolumeAboveDailyAndWeeklyAverage(stock, 3.0, 1.5)) {
                if (candleStickService.isGreen(stock) && candleStickHelperService.isUpperWickSizeConfirmed(stock)) {

                            ResearchLedgerTechnical.SubStrategy subStrategy = ResearchLedgerTechnical.SubStrategy.RM;
                            boolean isTmaConvergenceAndDivergence = this.isTmaConvergenceAndDivergence(stock, trend);
                            boolean isRsiMacdBreakout = this.isRsiMacdBreakout(stock, trend);
                            StockPrice stockPrice = stock.getStockPrice();

                            double entryPrice = stockPrice.getHigh();
                            double stopLossPrice = stockPrice.getLow();

                            double targetPrice = 0.0;
                            double risk = Math.abs(formulaService.calculateChangePercentage(entryPrice, stopLossPrice));
                            double correction = stockPriceService.correction(stock);
                            boolean isSwingAction = Boolean.FALSE;

                            if (isTmaConvergenceAndDivergence) {
                                subStrategy = ResearchLedgerTechnical.SubStrategy.TEMA;
                                targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, RiskReward.SWING_TEMA);
                                isSwingAction = Boolean.TRUE;
                            }
                            else if(isRsiMacdBreakout){
                                subStrategy = ResearchLedgerTechnical.SubStrategy.RM;
                                targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, RiskReward.SWING_RM);
                                isSwingAction = Boolean.TRUE;
                            }

                            if(isSwingAction){
                                log.info("{} bullish swing action confirmed using {}:{}", stock.getNseSymbol(), ResearchLedgerTechnical.Strategy.PRICE, subStrategy);
                                log.info("{} bullish swing action active for trend:{}, momentum:{}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}, risk {}, correction {}"
                                        , stock.getNseSymbol(), trend.getStrength(), trend.getMomentum(), entryPrice, targetPrice, stopLossPrice, risk, correction);

                                return TradeSetup.builder()
                                        .strategy(ResearchLedgerTechnical.Strategy.SWING)
                                        .subStrategy(subStrategy)
                                        .active(Boolean.TRUE)
                                        .entryPrice(entryPrice)
                                        .stopLossPrice(stopLossPrice)
                                        .targetPrice(targetPrice)
                                        .risk(risk)
                                        .correction(correction)
                                        .build();
                            }
                        }
                //}
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }



    /**
     * RSI >= 60
     * MACD histogram is green
     * Close Above EMA20
     * @param stock
     * @return
     */
    private boolean isRsiMacdBreakout(Stock stock, Trend trend){

        if(obvIndicatorService.isBullish(stock) || volumeIndicatorService.isBullish(stock)) {
            if (rsiTrendService.isBullish(stock)) {
                if (macdIndicatorService.isHistogramGreen(stock) && macdIndicatorService.isHistogramIncreased(stock)) {
                    if (macdIndicatorService.isSignalNearHistogram(stock)) {
                        if (stockPriceService.isCloseAboveEma20(stock)) {
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }
        return Boolean.FALSE;
    }



    /**
     * uptrend
     * TMA (EMA10, EMA20, EMA50) Convergence & Divergence
     * ADX Increasing
     * @param stock
     * @return
     */
    private boolean isTmaConvergenceAndDivergence(Stock stock, Trend trend){

        if(obvIndicatorService.isBullish(stock) || volumeIndicatorService.isBullish(stock)) {
            if (adxService.isBullish(stock)) {
                //if (adxService.isPlusDiIncreasing(stock) && adxService.isMinusDiDecreasing(stock)) {
                    if (stockPriceService.isTmaConvergenceAndDivergence(stock, trend)) {
                        return Boolean.TRUE;
                    }
                //}
            }
        }

        return Boolean.FALSE;
    }

}
