package com.example.service.calc;

import com.example.dto.OHLCV;

import java.util.List;

public interface ExponentialMovingAverageCalculatorService {

    public List<Double> calculate(List<OHLCV> ohlcvList, int days);

    public Double calculate(OHLCV ohlcv, double prevEMA, int days);
}
