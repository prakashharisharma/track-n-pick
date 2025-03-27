package com.example.enhanced.service;

import com.example.model.master.Stock;
import com.example.storage.model.StockPrice;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;

public interface UpdatePriceService {

    public void updatePrice(Timeframe timeframe, Stock stock, StockPrice  stockPrice);
    public StockPrice build(StockPriceIO stockPriceIO);
    public StockPrice build(Timeframe timeframe, Stock stock, LocalDate sessionDate);
}
