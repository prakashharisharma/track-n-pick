package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.dto.OHLCV;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.transactional.model.master.Stock;

import java.time.LocalDate;

public interface WeeklySupportResistanceService {


    public boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearSupport(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearResistance(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);


    public OHLCV supportAndResistance(Stock stock);

    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to);
}
