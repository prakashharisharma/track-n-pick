package com.example.service;

import com.example.storage.model.AverageDirectionalIndex;
import com.example.storage.model.OHLCV;
import com.example.storage.model.RelativeStrengthIndex;

import java.util.List;

public interface AverageDirectionalIndexService {
    public List<AverageDirectionalIndex> calculate(List<OHLCV> ohlcvList);
    public AverageDirectionalIndex calculate(List<OHLCV> ohlcvList, AverageDirectionalIndex prevAverageDirectionalIndex);
}
