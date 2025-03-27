package com.example.storage.model.assembler;


import com.example.dto.OHLCV;
import com.example.storage.model.WeeklyStockPrice;
import com.example.storage.model.StockPrice;
import com.example.util.CustomBeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockPriceOHLCVAssembler {

    public OHLCV toModel(StockPrice stockPrice) {
        OHLCV ohlcv = new OHLCV();
        CustomBeanUtils.copyNonNullProperties(stockPrice, ohlcv);

        return ohlcv;
    }

    public OHLCV toModel(WeeklyStockPrice stockPrice) {
        OHLCV ohlcv = new OHLCV();
        CustomBeanUtils.copyNonNullProperties(stockPrice, ohlcv);

        return ohlcv;
    }

    public StockPrice toDomain(OHLCV ohlcv) {
        StockPrice stockPrice = new StockPrice();
        CustomBeanUtils.copyNonNullProperties(ohlcv, stockPrice);
        return stockPrice;
    }

    public List<OHLCV> toModel(List<StockPrice> stockPriceList) {
        List<OHLCV> ohlcvList = new ArrayList<>();
        stockPriceList.forEach(stockPrice -> ohlcvList.add(this.toModel(stockPrice)));
        return ohlcvList;
    }

    public List<OHLCV> toWeeklyModel(List<WeeklyStockPrice> stockPriceList) {
        List<OHLCV> ohlcvList = new ArrayList<>();
        stockPriceList.forEach(stockPrice -> ohlcvList.add(this.toModel(stockPrice)));
        return ohlcvList;
    }

    public List<StockPrice> toDomain(List<OHLCV> ohlcvList) {
        List<StockPrice> stockPriceList = new ArrayList<>();
        ohlcvList.forEach(ohlcv -> stockPriceList.add(this.toDomain(ohlcv)));
        return stockPriceList;
    }
}
