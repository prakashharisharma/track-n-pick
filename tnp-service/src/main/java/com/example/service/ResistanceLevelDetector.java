package com.example.service;

import com.example.transactional.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

public interface ResistanceLevelDetector {
    public boolean isBreakout(Stock stock, Timeframe timeframe);
    public boolean isNearResistance(Stock stock, Timeframe timeframe);
}
