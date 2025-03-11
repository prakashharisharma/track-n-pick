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
    public double adx(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals!=null) {
            return stockTechnicals.getAdx();
        }

        return 0;
    }

    @Override
    public boolean isAdxIncreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals.getAdx() > stockTechnicals.getPrevAdx()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isAdxDecreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals.getAdx() < stockTechnicals.getPrevAdx()){
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

    @Override
    public boolean isBullish(Stock stock) {

        if(this.isPlusDiIncreasing(stock) && this.isMinusDiDecreasing(stock)){
            if(this.isAdxIncreasing(stock) && this.adx(stock) > ADX_BULLISH_MIN && this.adx(stock) < ADX_BULLISH_MAX){
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearish(Stock stock) {
        if(this.isPlusDiDecreasing(stock) && this.isMinusDiIncreasing(stock)){
            if(this.isAdxDecreasing(stock) && this.adx(stock) > ADX_BEARISH_MAX){

            }else if(this.isAdxIncreasing(stock) && this.adx(stock) > ADX_BEARISH_MIN){
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
