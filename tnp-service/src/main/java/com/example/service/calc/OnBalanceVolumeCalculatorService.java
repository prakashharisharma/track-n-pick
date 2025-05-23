package com.example.service.calc;

import com.example.data.storage.documents.OnBalanceVolume;
import com.example.dto.common.OHLCV;
import java.util.List;

public interface OnBalanceVolumeCalculatorService {

    public List<OnBalanceVolume> calculate(List<OHLCV> ohlcvList);

    public OnBalanceVolume calculate(List<OHLCV> ohlcvList, OnBalanceVolume prevOnBalanceVolume);
}
