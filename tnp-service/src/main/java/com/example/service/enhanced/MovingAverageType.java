package com.example.service.enhanced;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.utils.MovingAverageUtil;

public enum MovingAverageType {
    MA5(
            MovingAverageUtil::getMovingAverage5,
            MovingAverageUtil::getPrevMovingAverage5,
            MovingAverageUtil::getPrev2MovingAverage5),
    MA20(
            MovingAverageUtil::getMovingAverage20,
            MovingAverageUtil::getPrevMovingAverage20,
            MovingAverageUtil::getPrev2MovingAverage20),
    MA50(
            MovingAverageUtil::getMovingAverage50,
            MovingAverageUtil::getPrevMovingAverage50,
            MovingAverageUtil::getPrev2MovingAverage50),
    MA100(
            MovingAverageUtil::getMovingAverage100,
            MovingAverageUtil::getPrevMovingAverage100,
            MovingAverageUtil::getPrev2MovingAverage100),
    MA200(
            MovingAverageUtil::getMovingAverage200,
            MovingAverageUtil::getPrevMovingAverage200,
            MovingAverageUtil::getPrev2MovingAverage200);

    private final MovingAverageResolver resolver;
    private final MovingAverageResolver prevResolver;
    private final MovingAverageResolver prev2Resolver;

    MovingAverageType(
            MovingAverageResolver resolver,
            MovingAverageResolver prevResolver,
            MovingAverageResolver prev2Resolver) {
        this.resolver = resolver;
        this.prevResolver = prevResolver;
        this.prev2Resolver = prev2Resolver;
    }

    public double resolve(Timeframe timeframe, StockTechnicals technicals) {
        return resolver.getMovingAverage(timeframe, technicals);
    }

    public double resolvePrev(Timeframe timeframe, StockTechnicals technicals) {
        return prevResolver.getMovingAverage(timeframe, technicals);
    }

    public double resolvePrev2(Timeframe timeframe, StockTechnicals technicals) {
        return prev2Resolver.getMovingAverage(timeframe, technicals);
    }
}
