package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;


public interface SupportResistanceConfirmationService {

    public boolean isSupportConfirmed(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);

    public boolean isResistanceConfirmed(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);
}
