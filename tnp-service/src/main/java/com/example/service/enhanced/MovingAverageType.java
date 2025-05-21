package com.example.service.enhanced;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.utils.MovingAverageUtil;

public enum MovingAverageType {
    MA5(MovingAverageUtil::getMovingAverage5, MovingAverageUtil::getPrevMovingAverage5),
    MA20(MovingAverageUtil::getMovingAverage20, MovingAverageUtil::getPrevMovingAverage20),
    MA50(MovingAverageUtil::getMovingAverage50, MovingAverageUtil::getPrevMovingAverage50),
    MA100(MovingAverageUtil::getMovingAverage100, MovingAverageUtil::getPrevMovingAverage100),
    MA200(MovingAverageUtil::getMovingAverage200, MovingAverageUtil::getPrevMovingAverage200);

    private final MovingAverageResolver resolver;
    private final MovingAverageResolver prevResolver;

    MovingAverageType(MovingAverageResolver resolver, MovingAverageResolver prevResolver) {
        this.resolver = resolver;
        this.prevResolver = prevResolver;
    }

    public double resolve(Timeframe timeframe, StockTechnicals technicals) {
        return resolver.getMovingAverage(timeframe, technicals);
    }

    public double resolvePrev(Timeframe timeframe, StockTechnicals technicals) {
        return prevResolver.getMovingAverage(timeframe, technicals);
    }
}
