package com.example.service.calc;

import com.example.data.storage.documents.AverageDirectionalIndex;
import com.example.dto.OHLCV;
import java.util.List;

public interface AverageDirectionalIndexCalculatorService {
    public List<AverageDirectionalIndex> calculate(List<OHLCV> ohlcvList);

    public AverageDirectionalIndex calculate(
            List<OHLCV> ohlcvList, AverageDirectionalIndex prevAverageDirectionalIndex);
}
