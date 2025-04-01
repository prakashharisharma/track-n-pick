package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;

public interface CandleStickConfirmationService {

    public boolean isBullishConfirmed(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isBearishConfirmed(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
