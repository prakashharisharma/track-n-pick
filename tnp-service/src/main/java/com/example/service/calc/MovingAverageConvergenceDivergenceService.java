package com.example.service.calc;

import com.example.dto.OHLCV;
import com.example.data.storage.documents.MovingAverageConvergenceDivergence;

import java.util.List;

public interface MovingAverageConvergenceDivergenceService {

    public List<MovingAverageConvergenceDivergence> calculate(List<OHLCV> ohlcvList);
}
