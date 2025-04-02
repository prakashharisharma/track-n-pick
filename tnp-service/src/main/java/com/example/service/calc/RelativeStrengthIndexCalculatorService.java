package com.example.service.calc;

import com.example.data.storage.documents.RelativeStrengthIndex;
import com.example.dto.common.OHLCV;
import java.util.List;

public interface RelativeStrengthIndexCalculatorService {

    public List<RelativeStrengthIndex> calculate(List<OHLCV> ohlcvList);

    /**
     * pass prev day and current day OHLVC ohlcvList[0] - prevDay ohlcvList[1] - current
     *
     * @param ohlcvList
     * @param prevRelativeStrengthIndex
     * @return
     */
    public RelativeStrengthIndex calculate(
            List<OHLCV> ohlcvList, RelativeStrengthIndex prevRelativeStrengthIndex);
}
