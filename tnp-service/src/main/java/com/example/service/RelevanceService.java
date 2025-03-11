package com.example.service;

import com.example.model.master.Stock;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;

public interface RelevanceService {
    public boolean isBullishTimeFrame(Stock stock, Trend trend,  double minimumScore);
    public boolean isBullishMovingAverage(Stock stock, Trend trend,  double minimumScore);
    public boolean isBullishIndicator(Stock stock, Trend trend);
    public boolean isBearishTimeFrame(Stock stock, Trend trend,  double minimumScore);
    public boolean isBearishMovingAverage(Stock stock, Trend trend, double minimumScore);
    public boolean isBearishIndicator(Stock stock, Trend trend);
}
