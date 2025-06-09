package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.CrossOverUtil;
import com.example.service.RsiIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RsiIndicatorServiceImpl implements RsiIndicatorService {

    private static double DEFAULT_OVERSOLD = 40.0;

    private static double DEFAULT_OVERBOUGHT = 70.0;

    @Override
    public boolean isBullish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        if (stockTechnicals.getRsi() > stockTechnicals.getPrevRsi()) {
            boolean isRsiEnteredBullishZone =
                    CrossOverUtil.isFastCrossesAboveSlow(
                            stockTechnicals.getPrevRsi(), DEFAULT_OVERSOLD,
                            stockTechnicals.getRsi(), DEFAULT_OVERSOLD);

            return isRsiEnteredBullishZone
                    || Math.ceil(stockTechnicals.getPrevRsi()) > DEFAULT_OVERSOLD;
        }

        return false;
    }

    @Override
    public boolean isOverSold(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        return stockTechnicals.getPrevRsi() <= DEFAULT_OVERSOLD
                || stockTechnicals.getRsi() <= DEFAULT_OVERSOLD;
    }

    @Override
    public boolean isBearish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        double overBoughtFactor = this.getOverBoughtFactor(stockTechnicals);
        if (stockTechnicals.getRsi() < stockTechnicals.getPrevRsi()) {
            boolean isRsiEnteredBearishZone =
                    CrossOverUtil.isSlowCrossesBelowFast(
                            stockTechnicals.getPrevRsi(), overBoughtFactor,
                            stockTechnicals.getRsi(), overBoughtFactor);

            return isRsiEnteredBearishZone
                    || Math.ceil(stockTechnicals.getPrevRsi()) < overBoughtFactor;
        }

        return false;
    }

    @Override
    public boolean isOverBought(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        double overBoughtFactor = this.getOverBoughtFactor(stockTechnicals);

        return stockTechnicals.getPrevRsi() >= overBoughtFactor
                || stockTechnicals.getRsi() >= overBoughtFactor;
    }

    private double getOverBoughtFactor(StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockTechnicals.getTimeframe();

        return switch (timeframe) {
            case WEEKLY -> 75.0;
            case MONTHLY -> 80.0;
            default -> 70.0; // fallback value if needed
        };
    }

    @Override
    public double rsi(StockTechnicals stockTechnicals) {
        return stockTechnicals.getRsi();
    }

    @Override
    public boolean isIncreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        return stockTechnicals.getRsi() > stockTechnicals.getPrevRsi();
    }

    @Override
    public boolean isDecreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        return stockTechnicals.getRsi() < stockTechnicals.getPrevRsi();
    }
}
