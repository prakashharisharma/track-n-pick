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

    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Override
    public boolean isBullish(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getRsi() > stockTechnicals.getPrevRsi()){
                boolean isRsiEnteredBullishZone = CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevRsi(), 50.0, Math.ceil(stockTechnicals.getRsi()), 49.0);
                if (isRsiEnteredBullishZone) {
                    breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.RSI_ENTERED_BULLISH);
                    return Boolean.TRUE;
                } else if (Math.ceil(stockTechnicals.getPrevRsi()) >= 55.0) {
                    breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.RSI_CONTINUED_BULLISH);
                    return Boolean.TRUE;
                }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isOverBaught(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        return stockTechnicals.getPrevRsi() >= 70.0 || stockTechnicals.getRsi() >=  70.0;
    }

    @Override
    public boolean isOverSold(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        return stockTechnicals.getPrevRsi() <= 40.0 || stockTechnicals.getRsi() <=  40.0;
    }

    @Override
    public double rsi(Stock stock) {
        return stock.getTechnicals().getRsi();
    }

    @Override
    public double rsiPreviousSession(Stock stock) {
        return stock.getTechnicals().getPrevRsi();
    }
}
