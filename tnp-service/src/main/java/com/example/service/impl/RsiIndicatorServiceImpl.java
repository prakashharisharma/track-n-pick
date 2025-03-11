package com.example.service.impl;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.BreakoutLedgerService;
import com.example.service.CrossOverUtil;
import com.example.service.RsiIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RsiIndicatorServiceImpl implements RsiIndicatorService {

    private static double RSI_OVERSOLD = 40.0;

    private static double RSI_OVERBOUGHT = 65.0;

    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Override
    public boolean isBullish(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getRsi() > stockTechnicals.getPrevRsi()){
                boolean isRsiEnteredBullishZone = CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevRsi(), RSI_OVERSOLD, stockTechnicals.getRsi(), RSI_OVERSOLD);
                if (isRsiEnteredBullishZone) {
                    breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.RSI_ENTERED_BULLISH);
                    return Boolean.TRUE;
                } else if (Math.ceil(stockTechnicals.getPrevRsi()) > RSI_OVERSOLD) {
                    breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.RSI_CONTINUED_BULLISH);
                    return Boolean.TRUE;
                }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isOverSold(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        return stockTechnicals.getPrevRsi() <= RSI_OVERSOLD || stockTechnicals.getRsi() <=  RSI_OVERSOLD;
    }

    @Override
    public boolean isBearish(Stock stock) {
        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getRsi() < stockTechnicals.getPrevRsi()){
            boolean isRsiEnteredBearishZone = CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevRsi(), RSI_OVERBOUGHT, stockTechnicals.getRsi(), RSI_OVERBOUGHT);
            if (isRsiEnteredBearishZone) {
                breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.RSI_ENTERED_BEARISH);
                return Boolean.TRUE;
            } else if (Math.ceil(stockTechnicals.getPrevRsi()) < RSI_OVERBOUGHT) {
                breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.RSI_CONTINUED_BEARISH);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isOverBaught(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        return stockTechnicals.getPrevRsi() >= RSI_OVERBOUGHT || stockTechnicals.getRsi() >=  RSI_OVERBOUGHT;
    }



    @Override
    public double rsi(Stock stock) {
        return stock.getTechnicals().getRsi();
    }

    @Override
    public double rsiPreviousSession(Stock stock) {
        return stock.getTechnicals().getPrevRsi();
    }

    @Override
    public boolean isIncreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals.getRsi() > stockTechnicals.getPrevRsi()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDecreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals.getRsi() < stockTechnicals.getPrevRsi()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
