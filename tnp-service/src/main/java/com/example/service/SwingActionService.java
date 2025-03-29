package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.dto.TradeSetup;
import com.example.transactional.model.master.Stock;

public interface SwingActionService {

    public TradeSetup breakOut(Stock stock, Timeframe timeframe);
    public TradeSetup breakDown(Stock stock, Timeframe timeframe);
}
