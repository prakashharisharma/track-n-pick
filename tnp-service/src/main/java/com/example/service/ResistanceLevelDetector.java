package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;

public interface ResistanceLevelDetector {
    public boolean isBreakout(Stock stock, Timeframe timeframe);

    public boolean isNearResistance(Stock stock, Timeframe timeframe);
}
