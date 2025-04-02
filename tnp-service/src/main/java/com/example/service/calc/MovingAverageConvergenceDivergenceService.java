package com.example.service.calc;

import com.example.data.storage.documents.MovingAverageConvergenceDivergence;
import com.example.dto.common.OHLCV;
import java.util.List;

public interface MovingAverageConvergenceDivergenceService {

    public List<MovingAverageConvergenceDivergence> calculate(List<OHLCV> ohlcvList);
}
