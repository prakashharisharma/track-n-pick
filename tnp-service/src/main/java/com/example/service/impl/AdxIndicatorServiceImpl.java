package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.AdxIndicatorService;
import com.example.service.CrossOverUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdxIndicatorServiceImpl implements AdxIndicatorService {
    @Override
    public boolean isAdxIncreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals.getAdx() > stockTechnicals.getPrevAdx()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isPlusDiIncreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals.getPlusDi() > stockTechnicals.getPrevPlusDi()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isMinusDiIncreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals.getMinusDi() > stockTechnicals.getPrevMinusDi()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isPlusDiDecreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals.getPlusDi() < stockTechnicals.getPrevPlusDi()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isMinusDiDecreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals.getMinusDi() < stockTechnicals.getPrevMinusDi()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDmiConvergence(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
         if(CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevPlusDi(), stockTechnicals.getPrevMinusDi(),
        stockTechnicals.getPlusDi(), stockTechnicals.getMinusDi())){
             return Boolean.TRUE;
         }else if(this.isMinusDiIncreasing(stock) && this.isPlusDiDecreasing(stock)){
             return Boolean.TRUE;
         }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDmiDivergence(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevPlusDi(), stockTechnicals.getPrevMinusDi(),
                stockTechnicals.getPlusDi(), stockTechnicals.getMinusDi())){
            return Boolean.TRUE;
        }
        else if(this.isMinusDiDecreasing(stock) && this.isPlusDiIncreasing(stock)){
                return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
