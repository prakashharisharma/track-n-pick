package com.example.service.impl;

import com.example.transactional.model.master.Stock;

public interface FundamentalResearchService {
    public boolean isPriceInRange(Stock stock);
    public boolean isMcapInRange(Stock stock);
    public double calculateValuation(Stock stock);
    public boolean isGoodValuation(Stock stock);
    public boolean isUndervalued(Stock stock);
    public boolean isOvervalued(Stock stock);
}
