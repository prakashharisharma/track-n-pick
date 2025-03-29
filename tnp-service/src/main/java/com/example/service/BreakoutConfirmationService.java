package com.example.service;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.util.io.model.type.Timeframe;

public interface BreakoutConfirmationService {

    public boolean isBullishConfirmation(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);

    public boolean isBearishConfirmation(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);
}
