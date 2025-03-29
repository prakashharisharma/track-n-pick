package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.master.Stock;
import com.example.storage.model.StockPrice;
import com.example.dto.io.StockPriceIO;


import java.time.LocalDate;

public interface UpdatePriceService {

    public void updatePrice(Timeframe timeframe, Stock stock, StockPrice  stockPrice);
    public StockPrice build(StockPriceIO stockPriceIO);
    public StockPrice build(Timeframe timeframe, Stock stock, LocalDate sessionDate);
}
