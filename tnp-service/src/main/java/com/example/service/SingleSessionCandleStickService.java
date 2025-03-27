package com.example.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.util.io.model.type.Timeframe;

public interface SingleSessionCandleStickService {

    // Bullish Patterns
    boolean isBullishMarubozu(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isOpenLow(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isHammer(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isBullishPinBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isInvertedHammer(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    // Indecision Patterns
    boolean isDoji(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isGravestoneDoji(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isDragonflyDoji(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isSpinningTop(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    // Bearish Patterns
    boolean isBearishMarubozu(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isOpenHigh(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isBearishPinBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isShootingStar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    boolean isHangingMan(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
