package com.example.service.impl;

import com.example.transactional.model.stocks.StockTechnicals;
import com.example.service.CrossOverUtil;
import com.example.service.ObvIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ObvIndicatorServiceImpl implements ObvIndicatorService {


    @Override
    public boolean isBullish(StockTechnicals stockTechnicals) {

        return this.isBullishCurrentSession(stockTechnicals);
    }

    private boolean isBullishCurrentSession(StockTechnicals stockTechnicals){
            if (stockTechnicals.getObv() < stockTechnicals.getObvAvg()) {
                if (this.isObvIncreasing(stockTechnicals) && this.isObvAvgDecreasing(stockTechnicals)) {
                    return Boolean.TRUE;
                } else if (this.isObvIncreasing(stockTechnicals) && this.isObvAvgIncreasing(stockTechnicals)) {
                    return Boolean.TRUE;
                }
            } else {
                if (this.isObvIncreasing(stockTechnicals) && this.isObvAvgIncreasing(stockTechnicals)) {
                    return Boolean.TRUE;
                }
                return CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevObv(), stockTechnicals.getPrevObvAvg(), stockTechnicals.getObv(), stockTechnicals.getObvAvg());
            }

        return Boolean.FALSE;
    }


    @Override
    public boolean isBearish(StockTechnicals stockTechnicals) {
        return this.isBearishCurrentSession(stockTechnicals);
    }


    private boolean isBearishCurrentSession(StockTechnicals stockTechnicals){

            if (stockTechnicals.getObv() > stockTechnicals.getObvAvg()) {
                if (this.isObvDecreasing(stockTechnicals) && this.isObvAvgIncreasing(stockTechnicals)) {
                    return Boolean.TRUE;
                } else if (this.isObvDecreasing(stockTechnicals) && this.isObvAvgDecreasing(stockTechnicals)) {
                    return Boolean.TRUE;
                }
            } else {
                if (this.isObvDecreasing(stockTechnicals) && this.isObvAvgDecreasing(stockTechnicals)) {
                    return Boolean.TRUE;
                }

                return CrossOverUtil.isSlowCrossesBelowFast(stockTechnicals.getPrevObv(), stockTechnicals.getPrevObvAvg(), stockTechnicals.getObv(), stockTechnicals.getObvAvg());
            }

        return Boolean.FALSE;
    }


    @Override
    public boolean isObvIncreasing(StockTechnicals stockTechnicals) {

        if(stockTechnicals.getObv() > stockTechnicals.getPrevObv()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isObvDecreasing(StockTechnicals stockTechnicals) {

        if(stockTechnicals.getObv() < stockTechnicals.getPrevObv()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isObvAvgIncreasing(StockTechnicals stockTechnicals) {

        if(stockTechnicals.getObvAvg() > stockTechnicals.getPrevObvAvg()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isObvAvgDecreasing(StockTechnicals stockTechnicals) {

        if(stockTechnicals.getObvAvg() < stockTechnicals.getPrevObvAvg()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
