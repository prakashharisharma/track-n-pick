package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.*;
import com.example.util.FormulaService;
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
    private VolumeService volumeActionService;
    @Autowired
    private BreakoutService breakoutService;
    @Autowired
    private FormulaService formulaService;

    @Override
    public TradeSetup breakOut(Stock stock) {

        if(volumeActionService.isVolumeHigherThanMonthlyAverage(stock, 10.0)){
            if(candleStickService.range(stock) >= MIN_RANGE){
                if(candleStickService.isGreen(stock) && candleStickHelperService.isUpperWickSizeConfirmed(stock)) {
                    if (candleStickService.body(stock) >= CandleStickService.MIN_BODY_SIZE) {
                        if (candleStickService.isGapUp(stock)
                            || candleStickService.isRisingWindow(stock)
                            || candleStickService.isBullishhMarubozu(stock)
                            || candleStickService.isOpenAndLowEqual(stock)) {

                            if ( //breakoutService.isBreakOut200(stock)
                               // || breakoutService.isBreakOut50(stock)
                            //    ||
                            stockPriceService.isYearHigh(stock)
                                || macdActionService.isMacdCrossedSignal(stock)) {

                                StockPrice stockPrice = stock.getStockPrice();

                                double entryPrice = stockPrice.getHigh();
                                double stopLossPrice = stockPrice.getLow();
                                double targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, VOLUME_ACTION_RISK_REWARD);
                                double risk = formulaService.calculateChangePercentage(stopLossPrice, entryPrice);
                                double correction = stockPriceService.correction(stock);
                                log.info("Volume Action active for {}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}"
                                    , stock.getNseSymbol(), entryPrice, targetPrice, stopLossPrice);

                                return TradeSetup.builder()
                                    .active(Boolean.TRUE)
                                        .strategy(ResearchLedgerTechnical.Strategy.VOLUME_ACTION)
                                        .subStrategy(ResearchLedgerTechnical.SubStrategy.HIGH_VOLUME)
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
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }
}
