package com.example.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

public interface SupportResistanceConfirmationService {

    public boolean isSupportConfirmed(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);

    public boolean isResistanceConfirmed(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double average);
}
