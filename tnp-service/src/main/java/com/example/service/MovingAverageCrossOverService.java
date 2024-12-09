package com.example.service;

import com.example.model.master.Stock;

public interface MovingAverageCrossOverService {

    public boolean isGoldenCross(Stock stock);
    public boolean isBullishCrossOver50(Stock stock);
    public boolean isBullishCrossOver20(Stock stock);
    public boolean isBullishCrossOver10(Stock stock);
    public boolean isDeathCrossOver(Stock stock);
    public boolean isBearishCrossOver50(Stock stock);
    public boolean isBearishCrossOver20(Stock stock);
    public boolean isBearishCrossOver10(Stock stock);
}
