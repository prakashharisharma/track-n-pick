package com.example.service;

import com.example.dto.TradeSetup;
import com.example.model.master.Stock;

public interface VolumeActionService {
    public static final double VOLUME_ACTION_RISK_REWARD = 3.0;
    public static final double VOLUME_ACTION_FACTOR = 3.0;
    public TradeSetup breakOut(Stock stock);
}
