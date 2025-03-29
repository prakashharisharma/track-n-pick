package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.master.Stock;

public interface SupportLevelDetector {

    public boolean isNearSupport(Stock stock, Timeframe timeframe);

    public boolean isBreakDown(Stock stock, Timeframe timeframe);
}
