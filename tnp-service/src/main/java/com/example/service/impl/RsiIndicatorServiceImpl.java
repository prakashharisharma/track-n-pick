package com.example.service.impl;


import com.example.service.CrossOverUtil;
import com.example.service.RsiIndicatorService;
import com.example.data.transactional.entities.StockTechnicals;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RsiIndicatorServiceImpl implements RsiIndicatorService {

    private static double RSI_OVERSOLD = 45.0;

    private static double RSI_OVERBOUGHT = 65.0;

    @Override
    public boolean isBullish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null || stockTechnicals.getRsi() == null || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        if (stockTechnicals.getRsi() > stockTechnicals.getPrevRsi()) {
            boolean isRsiEnteredBullishZone = CrossOverUtil.isFastCrossesAboveSlow(
                    stockTechnicals.getPrevRsi(), RSI_OVERSOLD,
                    stockTechnicals.getRsi(), RSI_OVERSOLD
            );

            return isRsiEnteredBullishZone || Math.ceil(stockTechnicals.getPrevRsi()) > RSI_OVERSOLD;
        }

        return false;
    }

    @Override
    public boolean isOverSold(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null || stockTechnicals.getRsi() == null || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        return stockTechnicals.getPrevRsi() <= RSI_OVERSOLD || stockTechnicals.getRsi() <= RSI_OVERSOLD;
    }

    @Override
    public boolean isBearish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null || stockTechnicals.getRsi() == null || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        if (stockTechnicals.getRsi() < stockTechnicals.getPrevRsi()) {
            boolean isRsiEnteredBearishZone = CrossOverUtil.isSlowCrossesBelowFast(
                    stockTechnicals.getPrevRsi(), RSI_OVERBOUGHT,
                    stockTechnicals.getRsi(), RSI_OVERBOUGHT
            );

            return isRsiEnteredBearishZone || Math.ceil(stockTechnicals.getPrevRsi()) < RSI_OVERBOUGHT;
        }

        return false;
    }

    @Override
    public boolean isOverBought(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null || stockTechnicals.getRsi() == null || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        return stockTechnicals.getPrevRsi() >= RSI_OVERBOUGHT || stockTechnicals.getRsi() >= RSI_OVERBOUGHT;
    }



    @Override
    public double rsi(StockTechnicals stockTechnicals) {
        return stockTechnicals.getRsi();
    }


    @Override
    public boolean isIncreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null || stockTechnicals.getRsi() == null || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        return stockTechnicals.getRsi() > stockTechnicals.getPrevRsi();
    }

    @Override
    public boolean isDecreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null || stockTechnicals.getRsi() == null || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        return stockTechnicals.getRsi() < stockTechnicals.getPrevRsi();
    }
}
