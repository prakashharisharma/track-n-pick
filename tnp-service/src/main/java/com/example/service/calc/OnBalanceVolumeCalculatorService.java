package com.example.service.calc;

import com.example.dto.OHLCV;
import com.example.storage.model.OnBalanceVolume;

import java.util.List;

public interface OnBalanceVolumeCalculatorService {

    public List<OnBalanceVolume> calculate(List<OHLCV> ohlcvList);

    public OnBalanceVolume calculate(List<OHLCV> ohlcvList, OnBalanceVolume prevOnBalanceVolume);
}
