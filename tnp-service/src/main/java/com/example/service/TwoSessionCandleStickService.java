package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;


public interface TwoSessionCandleStickService {

    //Bullish
    public boolean isBullishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isPrevSessionBullishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBullishOutsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isTweezerBottom(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isPiercingPattern(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBullishKicker(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBullishSash(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBullishSeparatingLine(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBullishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isPrevSessionBullishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isBullishInsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);


    //Bearish
    public boolean isBearishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isPrevSessionBearishEngulfing(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishOutsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isTweezerTop(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isDarkCloudCover(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishKicker(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishSash(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishSeparatingLine(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isPrevSessionBearishHarami(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishInsideBar(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
