package com.example.service.impl;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;

public interface FundamentalResearchService {
    public boolean isPriceInRange(Stock stock);

    public boolean isPriceInRange(StockPrice stockPrice);

    public boolean isMcapInRange(Stock stock);

    public double marketCap(Stock stock);

    public double marketCap(StockPrice stockPrice);

    public double calculateValuation(Stock stock);

    public boolean isGoodValuation(Stock stock);

    public boolean isUndervalued(Stock stock);

    public boolean isOvervalued(Stock stock);
}
