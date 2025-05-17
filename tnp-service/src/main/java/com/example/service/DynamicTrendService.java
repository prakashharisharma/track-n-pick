package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.Stock;

public interface DynamicTrendService {
    public Trend detect(Stock stock, Timeframe timeframe);
}
