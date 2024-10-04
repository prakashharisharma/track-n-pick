package com.example.service;

import com.example.storage.model.OHLCV;
import com.example.storage.model.RelativeStrengthIndex;

import java.util.List;

public interface RelativeStrengthIndexService {

    public List<RelativeStrengthIndex> calculate(List<OHLCV> ohlcvList);

    /**
     * pass prev day and current day OHLVC
     * ohlcvList[0] - prevDay
     * ohlcvList[1] - current
     * @param ohlcvList
     * @param prevRelativeStrengthIndex
     * @return
     */
    public RelativeStrengthIndex calculate(List<OHLCV> ohlcvList, RelativeStrengthIndex prevRelativeStrengthIndex);
}
