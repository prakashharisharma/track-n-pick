package com.example.service.calc;

import com.example.dto.OHLCV;

import java.util.List;

public interface SimpleMovingAverageCalculatorService {

    public List<Double> calculate(List<OHLCV> ohlcvList, int period);

}
