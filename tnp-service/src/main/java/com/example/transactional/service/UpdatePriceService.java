package com.example.transactional.service;

import com.example.transactional.model.master.Stock;
import com.example.storage.model.StockPrice;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;

public interface UpdatePriceService {

    public void updatePrice(Timeframe timeframe, Stock stock, StockPrice  stockPrice);
    public StockPrice build(StockPriceIO stockPriceIO);
    public StockPrice build(Timeframe timeframe, Stock stock, LocalDate sessionDate);
}
