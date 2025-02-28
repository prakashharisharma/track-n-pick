package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.*;
import com.example.service.util.StockPriceUtil;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandleStickExecutorServiceImpl implements CandleStickExecutorService {

    @Autowired
    private SupportResistanceUtilService supportResistanceService;

    @Autowired
    private CandleStickHelperService candleStickHelperService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;
    @Autowired
    private CandleStickService candleStickService;
    @Autowired
    private StockPriceService stockPriceService;
    @Autowired
    private RelevanceService relevanceService;
    @Autowired
    private BreakoutService breakoutService;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private VolumeService volumeService;

    @Autowired
    private TrendService trendService;

    @Override
    public void executeBullish(Stock stock) {
        if (!candleStickService.isDead(stock)) {
            Trend trend = trendService.isDownTrend(stock);
            Momentum momentum = trendService.scanBullish(stock);
            if (trend != Trend.INVALID) {
                if (candleStickHelperService.isBullishConfirmed(stock, Boolean.TRUE)){
                    if (relevanceService.isBullish(stock, trend,momentum, 1.5)) {
                        log.info("Candlestick confirmed {}", stock.getNseSymbol());
                    }
                }
            }
        }
    }

    @Override
    public void executeBullishInDecisionConfirmation(Stock stock) {

    }

    @Override
    public void executeBearish(Stock stock) {
        if (!candleStickService.isDead(stock)) {
            Trend trend = trendService.isUpTrend(stock);
            Momentum momentum = trendService.scanBearish(stock);
            if(trend != Trend.INVALID) {
                if (candleStickHelperService.isBearishConfirmed(stock, Boolean.TRUE)){
                    if (relevanceService.isBearish(stock, trend,momentum, 1.5)) {
                        log.info("Candlestick confirmed {}", stock.getNseSymbol());
                    }
                }
            }
        }
    }

    @Override
    public void executeBearishInDecisionConfirmation(Stock stock) {

    }

}
