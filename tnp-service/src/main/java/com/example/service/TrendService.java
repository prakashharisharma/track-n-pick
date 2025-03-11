package com.example.service;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;

public interface TrendService {

    public Trend isUpTrend(Stock stock);

    public Trend isDownTrend(Stock stock);

    public Momentum detect(Stock stock);

    public Momentum scanBullish(Stock stock);

    public Momentum scanBearish(Stock stock);


}
