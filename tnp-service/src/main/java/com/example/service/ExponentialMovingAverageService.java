package com.example.service;

import com.example.storage.model.OHLCV;
import com.example.storage.model.RelativeStrengthIndex;

import java.util.List;

public interface ExponentialMovingAverageService {

    public List<Double> calculate(List<OHLCV> ohlcvList,  int days);

    public Double calculate(OHLCV ohlcv, double prevEMA, int days);
}
