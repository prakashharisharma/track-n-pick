package com.example.service.impl;

import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.AdxIndicatorService;
import com.example.service.CrossOverUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdxIndicatorServiceImpl implements AdxIndicatorService {

    @Override
    public double adx(StockTechnicals stockTechnicals) {

        if (stockTechnicals != null) {
            return stockTechnicals.getAdx();
        }

        return 0;
    }

    @Override
    public boolean isAdxIncreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getAdx() == null
                || stockTechnicals.getPrevAdx() == null) {
            return false;
        }
        return stockTechnicals.getAdx() > stockTechnicals.getPrevAdx();
    }

    @Override
    public boolean isAdxDecreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getAdx() == null
                || stockTechnicals.getPrevAdx() == null) {
            return false;
        }
        return stockTechnicals.getAdx() < stockTechnicals.getPrevAdx();
    }

    @Override
    public boolean isPlusDiIncreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getPlusDi() == null
                || stockTechnicals.getPrevPlusDi() == null) {
            return false;
        }
        return stockTechnicals.getPlusDi() > stockTechnicals.getPrevPlusDi();
    }

    @Override
    public boolean isMinusDiIncreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getMinusDi() == null
                || stockTechnicals.getPrevMinusDi() == null) {
            return false;
        }
        return stockTechnicals.getMinusDi() > stockTechnicals.getPrevMinusDi();
    }

    @Override
    public boolean isPlusDiDecreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getPlusDi() == null
                || stockTechnicals.getPrevPlusDi() == null) {
            return false;
        }
        return stockTechnicals.getPlusDi() < stockTechnicals.getPrevPlusDi();
    }

    @Override
    public boolean isMinusDiDecreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getMinusDi() == null
                || stockTechnicals.getPrevMinusDi() == null) {
            return false;
        }
        return stockTechnicals.getMinusDi() < stockTechnicals.getPrevMinusDi();
    }

    @Override
    public boolean isDmiConvergence(StockTechnicals stockTechnicals) {

        if (CrossOverUtil.isSlowCrossesBelowFast(
                stockTechnicals.getPrevPlusDi(),
                stockTechnicals.getPrevMinusDi(),
                stockTechnicals.getPlusDi(),
                stockTechnicals.getMinusDi())) {
            return Boolean.TRUE;
        } else if (this.isMinusDiIncreasing(stockTechnicals)
                && this.isPlusDiDecreasing(stockTechnicals)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDmiDivergence(StockTechnicals stockTechnicals) {

        if (stockTechnicals == null) {
            return false;
        }

        if (CrossOverUtil.isFastCrossesAboveSlow(
                stockTechnicals.getPrevPlusDi(),
                stockTechnicals.getPrevMinusDi(),
                stockTechnicals.getPlusDi(),
                stockTechnicals.getMinusDi())) {
            return Boolean.TRUE;
        } else if (this.isMinusDiDecreasing(stockTechnicals)
                && this.isPlusDiIncreasing(stockTechnicals)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullish(StockTechnicals stockTechnicals) {

        if (this.isPlusDiIncreasing(stockTechnicals) && this.isMinusDiDecreasing(stockTechnicals)) {
            if (this.isAdxIncreasing(stockTechnicals)
                    && this.adx(stockTechnicals) > ADX_BULLISH_MIN) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearish(StockTechnicals stockTechnicals) {
        if (this.isPlusDiDecreasing(stockTechnicals) && this.isMinusDiIncreasing(stockTechnicals)) {
            if (this.isAdxDecreasing(stockTechnicals)
                    && this.adx(stockTechnicals) > ADX_BEARISH_MAX) {

            } else if (this.isAdxIncreasing(stockTechnicals)
                    && this.adx(stockTechnicals) > ADX_BEARISH_MIN) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
