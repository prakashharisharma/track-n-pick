package com.example.service.calc;

import com.example.storage.model.AverageDirectionalIndex;
import com.example.storage.model.OHLCV;

import java.util.List;

public interface AverageDirectionalIndexCalculatorService {
    public List<AverageDirectionalIndex> calculate(List<OHLCV> ohlcvList);
    public AverageDirectionalIndex calculate(List<OHLCV> ohlcvList, AverageDirectionalIndex prevAverageDirectionalIndex);
}
