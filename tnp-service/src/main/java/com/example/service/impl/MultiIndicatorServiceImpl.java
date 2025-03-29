package com.example.service.impl;

import com.example.transactional.model.stocks.StockTechnicals;
import com.example.service.AdxIndicatorService;
import com.example.service.MacdIndicatorService;
import com.example.service.MultiIndicatorService;
import com.example.service.RsiIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MultiIndicatorServiceImpl implements MultiIndicatorService {

    @Autowired
    private MacdIndicatorService macdIndicatorService;

    @Autowired
    private AdxIndicatorService adxIndicatorService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;

    @Override
    public boolean isBullish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null) {
            log.warn("StockTechnicals is null. Cannot determine bullish trend.");
            return false;
        }

        if (!rsiIndicatorService.isIncreasing(stockTechnicals)) {
            log.debug("RSI is not increasing. Bullish condition failed.");
            return false;
        }

        if (!macdIndicatorService.isHistogramIncreased(stockTechnicals)) {
            log.debug("MACD histogram has not increased. Bullish condition failed.");
            return false;
        }

        if (!adxIndicatorService.isBullish(stockTechnicals)) {
            log.debug("ADX is not bullish. Bullish condition failed.");
            return false;
        }

        log.info("Stock is in a bullish trend based on RSI, MACD, and ADX indicators.");
        return true;
    }

    @Override
    public boolean isBearish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null) {
            log.warn("StockTechnicals is null. Cannot determine bearish trend.");
            return false;
        }

        if (!rsiIndicatorService.isDecreasing(stockTechnicals)) {
            log.debug("RSI is not decreasing. Bearish condition failed.");
            return false;
        }

        if (!macdIndicatorService.isHistogramDecreased(stockTechnicals)) {
            log.debug("MACD histogram has not decreased. Bearish condition failed.");
            return false;
        }

        if (!adxIndicatorService.isBearish(stockTechnicals)) {
            log.debug("ADX is not bearish. Bearish condition failed.");
            return false;
        }

        log.info("Stock is in a bearish trend based on RSI, MACD, and ADX indicators.");
        return true;
    }
}
