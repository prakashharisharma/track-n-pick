package com.example.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.util.io.model.type.Timeframe;

public interface ThreeSessionCandleStickService {

    //Bullish
    public boolean isThreeWhiteSoldiers(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isThreeInsideUp(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isThreeOutsideUp(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isMorningStar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isThreeCandleTweezerBottom(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    //Bearish
    public boolean isThreeBlackCrows(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isThreeInsideDown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isThreeOutsideDown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isEveningStar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isThreeCandleTweezerTop(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

}
