package com.example.service.calc;

import com.example.dto.OHLCV;
import java.util.List;

public interface VolumeAverageCalculatorService {

    public List<Long> calculate(List<OHLCV> ohlcvList, int days);

    public Long calculate(OHLCV ohlcv, long prevSmoothedAverageVolume, int days);
}
