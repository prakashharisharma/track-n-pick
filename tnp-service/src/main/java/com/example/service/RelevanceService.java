package com.example.service;

import com.example.model.master.Stock;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;

public interface RelevanceService {
    public boolean isBullish(Stock stock, Trend trend, Momentum momentum, double minimumScore);
    public boolean isBearish(Stock stock,Trend trend,Momentum momentum, double minimumScore);
}
