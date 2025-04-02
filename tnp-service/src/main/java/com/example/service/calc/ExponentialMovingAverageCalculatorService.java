package com.example.service.calc;

import com.example.dto.common.OHLCV;
import java.util.List;

public interface ExponentialMovingAverageCalculatorService {

    public List<Double> calculate(List<OHLCV> ohlcvList, int days);
}
