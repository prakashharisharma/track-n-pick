package com.example.service;

import com.example.storage.model.MovingAverageConvergenceDivergence;
import com.example.storage.model.OHLCV;

import java.util.List;

public interface MovingAverageConvergenceDivergenceService {


    public List<MovingAverageConvergenceDivergence> calculate(List<OHLCV> ohlcvList);

    public MovingAverageConvergenceDivergence calculate(OHLCV ohlcv, MovingAverageConvergenceDivergence prevMovingAverageConvergenceDivergence);

}
