package com.example.service.enhanced;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;

@FunctionalInterface
public interface MovingAverageResolver {

    double getMovingAverage(Timeframe timeframe, StockTechnicals technicals);

    default double getPrevMovingAverage(Timeframe timeframe, StockTechnicals technicals) {
        return 0.0; // Optional override
    }
}
