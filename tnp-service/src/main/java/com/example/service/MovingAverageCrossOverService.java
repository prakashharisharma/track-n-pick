package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.master.Stock;

public interface MovingAverageCrossOverService {

    public boolean isGoldenCross(Stock stock, Timeframe timeframe);
    public boolean isBullishCrossOver50(Stock stock, Timeframe timeframe);
    public boolean isBullishCrossOver20(Stock stock, Timeframe timeframe);
    public boolean isBullishCrossOver10(Stock stock, Timeframe timeframe);
    public boolean isDeathCrossOver(Stock stock, Timeframe timeframe);
    public boolean isBearishCrossOver50(Stock stock, Timeframe timeframe);
    public boolean isBearishCrossOver20(Stock stock, Timeframe timeframe);
    public boolean isBearishCrossOver10(Stock stock, Timeframe timeframe);
}
