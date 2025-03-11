package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.service.CandleStickService.MIN_RANGE;

@Slf4j
@Service
public class VolumeActionServiceImpl implements VolumeActionService {


    @Autowired
    private CandleStickHelperService candleStickHelperService;
    @Autowired
    private CandleStickService candleStickService;
    @Autowired
    private StockPriceService stockPriceService;
    @Autowired
    private MacdIndicatorService macdActionService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;

    @Autowired
    private AdxIndicatorService adxIndicatorService;

    @Autowired
    private ObvIndicatorService obvIndicatorService;

    @Autowired
    private VolumeIndicatorService volumeIndicatorService;

    @Autowired
    private VolumeService volumeActionService;
    @Autowired
    private BreakoutService breakoutService;
    @Autowired
    private FormulaService formulaService;

    @Autowired
    private TrendService trendService;

    @Override
    public TradeSetup breakOut(Stock stock) {

        Trend trend = trendService.isUpTrend(stock);

    if(trend.getMomentum() == Momentum.RECOVERY || trend.getMomentum() == Momentum.ADVANCE){
        if (volumeIndicatorService.isBullish(stock, 4.0)) {
            if (candleStickService.range(stock) >= MIN_RANGE) {
                if (candleStickService.isGreen(stock) && candleStickHelperService.isUpperWickSizeConfirmed(stock)) {
                    if (candleStickService.body(stock) >= CandleStickService.MIN_BODY_SIZE) {
                        if (candleStickService.isGapUp(stock)
                                || candleStickService.isRisingWindow(stock)
                                || candleStickService.isBullishhMarubozu(stock)
                                || candleStickService.isOpenAndLowEqual(stock)) {

                            if (rsiIndicatorService.isBullish(stock) || rsiIndicatorService.isOverSold(stock)) {
                                if (stockPriceService.isYearHigh(stock) ||
                                        macdActionService.isMacdCrossedSignal(stock) ||
                                        adxIndicatorService.isBullish(stock)
                                ) {

                                    StockPrice stockPrice = stock.getStockPrice();

                                    double entryPrice = stockPrice.getHigh();
                                    double stopLossPrice = stockPrice.getLow();
                                    double targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, VOLUME_ACTION_RISK_REWARD);
                                    double risk = formulaService.calculateChangePercentage(stopLossPrice, entryPrice);
                                    double correction = stockPriceService.correction(stock);
                                    StockTechnicals stockTechnicals = stock.getTechnicals();

                                    if (Math.abs(formulaService.calculateChangePercentage(stockTechnicals.getEma20(),stockPrice.getClose())) < 10.0) {

                                        log.info("{} bullish volume action confirmed using {}:{}", stock.getNseSymbol(), ResearchLedgerTechnical.Strategy.PRICE, ResearchLedgerTechnical.SubStrategy.HV);
                                    log.info("{} bullish volume action active for trend:{}, momentum:{}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}, risk {}, correction {}"
                                            , stock.getNseSymbol(), trend.getStrength(), trend.getMomentum(), entryPrice, targetPrice, stopLossPrice, risk, correction);

                                    return TradeSetup.builder()
                                            .active(Boolean.TRUE)
                                            .strategy(ResearchLedgerTechnical.Strategy.VOLUME)
                                            .subStrategy(ResearchLedgerTechnical.SubStrategy.HV)
                                            .entryPrice(entryPrice)
                                            .stopLossPrice(stopLossPrice)
                                            .targetPrice(targetPrice)
                                            .risk(risk)
                                            .correction(correction)
                                            .build();
                                }
                                    log.info("{} bullish volume action rejected as price is away from ema 20 using {}:{}", stock.getNseSymbol(), ResearchLedgerTechnical.Strategy.VOLUME, ResearchLedgerTechnical.SubStrategy.HV);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }
}
