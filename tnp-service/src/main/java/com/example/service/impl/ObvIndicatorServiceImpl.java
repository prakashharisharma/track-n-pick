package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.CrossOverUtil;
import com.example.service.ObvIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ObvIndicatorServiceImpl implements ObvIndicatorService {

    @Override
    public boolean isBullish(Stock stock) {
        return this.isBullishCurrentSession(stock.getTechnicals()) || this.isBullishPreviousSession(stock.getTechnicals());
    }

    private boolean isBullishCurrentSession(StockTechnicals stockTechnicals){
        Stock stock = stockTechnicals.getStock();
        if(stockTechnicals.getObv() < stockTechnicals.getObvAvg()){

            if(this.isObvIncreasing(stock) && this.isObvAvgDecreasing(stock)){
                    return Boolean.TRUE;
            }else if(this.isObvIncreasing(stock) && this.isObvAvgIncreasing(stock)){
                return Boolean.TRUE;
            }
        }else{
            if(this.isObvIncreasing(stock) && this.isObvAvgIncreasing(stock)) {
                    return Boolean.TRUE;
            }
            return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevObv(), stockTechnicals.getPrevObvAvg(), stockTechnicals.getObv(),stockTechnicals.getObvAvg());
        }

        return Boolean.FALSE;
    }

    private boolean isBullishPreviousSession(StockTechnicals stockTechnicals){

        if(stockTechnicals.getObv() > stockTechnicals.getPrevObv()){

            stockTechnicals.setObv(stockTechnicals.getPrevObv());
            stockTechnicals.setPrevObv(stockTechnicals.getPrevPrevObv());
            stockTechnicals.setObvAvg(stockTechnicals.getObvAvg());
            stockTechnicals.setPrevObvAvg(stockTechnicals.getPrevPrevObvAvg());

            return this.isBullishCurrentSession(stockTechnicals);
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearish(Stock stock) {
        return this.isBearishPreviousSession(stock.getTechnicals()) || this.isBearishCurrentSession(stock.getTechnicals());
    }



    private boolean isBearishCurrentSession(StockTechnicals stockTechnicals){
        Stock stock = stockTechnicals.getStock();
        if(stockTechnicals.getObv() > stockTechnicals.getObvAvg()){
            if(this.isObvDecreasing(stock) && this.isObvAvgIncreasing(stock)){
                    return Boolean.TRUE;
            }else if(this.isObvDecreasing(stock) && this.isObvAvgDecreasing(stock)) {
                return Boolean.TRUE;
            }
        }else{
            if(this.isObvDecreasing(stock) && this.isObvAvgDecreasing(stock)) {
                    return Boolean.TRUE;
            }

            return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevObv(), stockTechnicals.getPrevObvAvg(), stockTechnicals.getObv(),stockTechnicals.getObvAvg());
        }

        return Boolean.FALSE;
    }

    private boolean isBearishPreviousSession(StockTechnicals stockTechnicals){

        if(stockTechnicals.getObv() < stockTechnicals.getPrevObv()){

            stockTechnicals.setObv(stockTechnicals.getPrevObv());
            stockTechnicals.setPrevObv(stockTechnicals.getPrevPrevObv());
            stockTechnicals.setObvAvg(stockTechnicals.getObvAvg());
            stockTechnicals.setPrevObvAvg(stockTechnicals.getPrevPrevObvAvg());
            return this.isBearishCurrentSession(stockTechnicals);
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isObvIncreasing(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals.getObv() > stockTechnicals.getPrevObv()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isObvDecreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals.getObv() < stockTechnicals.getPrevObv()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isObvAvgIncreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals.getObvAvg() > stockTechnicals.getPrevObvAvg()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isObvAvgDecreasing(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals.getObvAvg() < stockTechnicals.getPrevObvAvg()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
