package com.example.service.impl;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.transactional.service.StockPriceService;
import com.example.transactional.service.StockTechnicalsService;
import com.example.service.AdxIndicatorService;
import com.example.service.BreakoutConfirmationService;
import com.example.service.CandleStickService;
import com.example.service.utils.CandleStickUtils;
import com.example.util.io.model.type.Timeframe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BreakoutConfirmationServiceImpl implements BreakoutConfirmationService {

    @Autowired
    private CandleStickService candleStickService;
    @Autowired
    private AdxIndicatorService adxIndicatorService;

    @Autowired
    private StockPriceService<StockPrice> stockPriceService;

    @Autowired
    private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Override
    public boolean isBullishConfirmation(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double breakoutLevel) {
        boolean gapUp = candleStickService.isGapUp(stockPrice);
        boolean openAbovePrevHigh = stockPrice.getOpen() > stockPrice.getPrevHigh();
        boolean prevHighBelowBreakout = stockPrice.getPrevHigh() < breakoutLevel;

        boolean adxStrong = stockTechnicals.getAdx() > 25;

        boolean dmiPositive = stockTechnicals.getPlusDi() > stockTechnicals.getMinusDi();

        boolean bodyMidAboveResistance = isBodyMidHigherThanAverage(stockPrice,  breakoutLevel);

        double atr = stockTechnicals.getAtr();
        boolean atrBreakoutConfirmed = (stockPrice.getClose() - stockPrice.getPrevHigh()) > (1.5 * atr);

        if (gapUp && (openAbovePrevHigh || prevHighBelowBreakout)) {
            log.info("[{}] Breakout confirmed: Gap Up with Open > Prev High or Prev High < Breakout Level ({})", stockPrice.getStock().getNseSymbol(), breakoutLevel);
            return true;
        }

        if (adxStrong && dmiPositive) {
            log.info("[{}] Breakout confirmed: ADX strong ({}) & DMI+ leading DMI-", stockPrice.getStock().getNseSymbol(), stockTechnicals.getAdx());
            return true;
        }


        if (bodyMidAboveResistance) {
            log.info("[{}] Breakout confirmed: Candle body midpoint above resistance ({})", stockPrice.getStock().getNseSymbol(), breakoutLevel);
            return true;
        }

        if (atrBreakoutConfirmed) {
            log.info("[{}] Breakout confirmed: Close above Prev High by 1.5x ATR ({})", stockPrice.getStock().getNseSymbol(), atr);
            return true;
        }

        log.info("[{}] No breakout confirmation", stockPrice.getStock().getNseSymbol());
        return false;
    }

    private boolean isBodyMidHigherThanAverage(StockPrice stockPrice, double average){
        double mid = this.calculateMid(stockPrice);
        if(mid > average){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    @Override
    public boolean isBearishConfirmation(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double breakdownLevel) {

        boolean gapDown = candleStickService.isGapDown(stockPrice);
        boolean openBelowPrevLow = stockPrice.getOpen() < stockPrice.getPrevLow();
        boolean prevLowAboveBreakdown = stockPrice.getPrevLow() > breakdownLevel;

        boolean adxStrong = stockTechnicals.getAdx() > 25;
        boolean dmiNegative = stockTechnicals.getPlusDi() < stockTechnicals.getMinusDi();

        boolean bodyMidBelowSupport = isBodyMidLowerThanAverage(stockPrice, breakdownLevel);

        double atr = stockTechnicals.getAtr();
        boolean atrBreakdownConfirmed = (stockPrice.getPrevLow() - stockPrice.getClose()) > (1.5 * atr);

        if (gapDown && (openBelowPrevLow || prevLowAboveBreakdown)) {
            log.info("[{}]  Breakdown confirmed: Gap Down with Open < Prev Low or Prev Low > Breakdown Level ({})", stockPrice.getStock().getNseSymbol(), breakdownLevel);
            return true;
        }

        if (adxStrong && dmiNegative) {
            log.info("[{}]  Breakdown confirmed: ADX strong ({}) & DMI- leading DMI+", stockPrice.getStock().getNseSymbol(), stockTechnicals.getAdx());
            return true;
        }

        if (bodyMidBelowSupport) {
            log.info("[{}]  Breakdown confirmed: Candle body midpoint below support ({})", stockPrice.getStock().getNseSymbol(), breakdownLevel);
            return true;
        }

        if (atrBreakdownConfirmed) {
            log.info("[{}]  Breakdown confirmed: Close below Prev Low by 1.5x ATR ({})", stockPrice.getStock().getNseSymbol(), atr);
            return true;
        }

        log.info("[{}] No breakdown confirmation", stockPrice.getStock().getNseSymbol());
        return false;
    }

    private boolean isBodyMidLowerThanAverage(StockPrice stockPrice, double average){

        double mid = this.calculateMid(stockPrice);

        if(mid < average){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private double calculateMid(StockPrice stockPrice){
        if(CandleStickUtils.upperWickSize(stockPrice) > CandleStickUtils.lowerWickSize(stockPrice)){
            return (stockPrice.getOpen() + stockPrice.getClose() + stockPrice.getLow())/3;
        }
        return (stockPrice.getOpen() + stockPrice.getClose() + stockPrice.getHigh())/3;
    }
}
