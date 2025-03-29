package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;

public interface CandleStickConfirmationService {

    public boolean isBullishConfirmed(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishConfirmed(Timeframe timeframe, StockPrice stockPrice,StockTechnicals stockTechnicals);

}
