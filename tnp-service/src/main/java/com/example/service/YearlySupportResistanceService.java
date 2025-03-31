package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.dto.OHLCV;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;


import java.time.LocalDate;

public interface YearlySupportResistanceService {

    public boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearSupport(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);


    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearResistance(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public OHLCV supportAndResistance(Stock stock);

    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to);

    public OHLCV supportAndResistance(String nseSymbol, LocalDate onDate);
}
