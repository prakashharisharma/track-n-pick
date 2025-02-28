package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SwingActionServiceImpl implements SwingActionService {

    private static final double SWING_ACTION_FACTOR = 2.0;

    private static final double RSI_MACD_RISK_REWARD = 2.0;

    private static final double ADX_TMA_RISK_REWARD = 5.0;

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
    private RelevanceService relevanceService;
    @Autowired
    private BreakoutService breakoutService;
    @Autowired
    private AdxIndicatorService adxService;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private VolumeService volumeService;
    @Autowired
    private TrendService trendService;


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
        if(trend!=Trend.INVALID) {
            if (volumeService.isVolumeAboveWeeklyAndMonthlyAverage(stock, 3.0, 1.5) || volumeService.isVolumeAboveDailyAndWeeklyAverage(stock, 3.0, 1.5)) {
                if (candleStickService.isGreen(stock) && candleStickHelperService.isUpperWickSizeConfirmed(stock)) {

                            ResearchLedgerTechnical.SubStrategy subStrategy = ResearchLedgerTechnical.SubStrategy.RSI_MACD;
                            boolean isTmaConvergenceAndDivergence = this.isTmaConvergenceAndDivergence(stock, trend);
                            boolean isRsiMacdBreakout = this.isRsiMacdBreakout(stock, trend);
                            StockPrice stockPrice = stock.getStockPrice();

                            double entryPrice = stockPrice.getHigh();
                            double stopLossPrice = stockPrice.getLow();

                            double targetPrice = 0.0;
                            double risk = formulaService.calculateChangePercentage(stopLossPrice, entryPrice);
                            double correction = stockPriceService.correction(stock);
                            boolean isSwingAction = Boolean.FALSE;

                            if (isTmaConvergenceAndDivergence) {
                                subStrategy = ResearchLedgerTechnical.SubStrategy.ADX_TEMA;
                                targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, ADX_TMA_RISK_REWARD);
                                isSwingAction = Boolean.TRUE;
                            }
                            else if(isRsiMacdBreakout){
                                subStrategy = ResearchLedgerTechnical.SubStrategy.RSI_MACD;
                                targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, RSI_MACD_RISK_REWARD);
                                isSwingAction = Boolean.TRUE;
                            }

                            if(isSwingAction){

                                log.info("{} Swing Action active for {}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}", trend
                                        , stock.getNseSymbol(), entryPrice, targetPrice, stopLossPrice);

                                return TradeSetup.builder()
                                        .strategy(ResearchLedgerTechnical.Strategy.SWING_ACTION)
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
                }
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

                if (rsiTrendService.isBullish(stock)) {
                    if (macdIndicatorService.isHistogramGreen(stock) && macdIndicatorService.isHistogramIncreased(stock)) {
                        if (macdIndicatorService.isSignalNearHistogram(stock)) {
                            if ( trend == Trend.SHORT &&  stockPriceService.isCloseAboveEma20(stock)) {
                                log.info("RSI MACD Breakout active for {}", stock.getNseSymbol());
                                return Boolean.TRUE;
                            }else if ( trend == Trend.MEDIUM &&  stockPriceService.isCloseAboveEma50(stock)) {
                                log.info("RSI MACD Breakout active for {}", stock.getNseSymbol());
                                return Boolean.TRUE;
                            }
                            else if ( trend == Trend.LONG &&  stockPriceService.isCloseAboveEma200(stock)) {
                                log.info("RSI MACD Breakout active for {}", stock.getNseSymbol());
                                return Boolean.TRUE;
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

            if (adxService.isAdxIncreasing(stock)) {
                if (stockPriceService.isTmaConvergenceAndDivergence(stock, trend)) {
                    log.info("ADX TMA in average range breakout active for {}", stock.getNseSymbol());
                    return Boolean.TRUE;
                }else if(stockPriceService.isTmaInPriceRange(stock, trend)){
                    log.info("ADX TMA in price range breakout active for {}", stock.getNseSymbol());
                    return Boolean.TRUE;
                }
            }

        return Boolean.FALSE;
    }


}
