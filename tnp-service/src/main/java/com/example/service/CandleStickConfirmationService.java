package com.example.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.util.io.model.type.Timeframe;

public interface CandleStickConfirmationService {

    public boolean isBullishConfirmed(Timeframe timeframe, StockPrice stockPrice,StockTechnicals stockTechnicals);
    public boolean isBearishConfirmed(Timeframe timeframe, StockPrice stockPrice,StockTechnicals stockTechnicals);

}
