package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;

public interface BreakoutConfirmationService {

    public boolean isBullishConfirmation(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);

    public boolean isBearishConfirmation(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);
}
