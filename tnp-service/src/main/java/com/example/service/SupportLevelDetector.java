package com.example.service;

import com.example.transactional.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

public interface SupportLevelDetector {

    public boolean isNearSupport(Stock stock, Timeframe timeframe);

    public boolean isBreakDown(Stock stock, Timeframe timeframe);
}
