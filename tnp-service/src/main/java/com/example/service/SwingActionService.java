package com.example.service;

import com.example.dto.TradeSetup;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

public interface SwingActionService {

    public TradeSetup breakOut(Stock stock, Timeframe timeframe);
    public TradeSetup breakDown(Stock stock, Timeframe timeframe);
}
