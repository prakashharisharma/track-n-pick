package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.dto.TradeSetup;

public interface PriceActionService {

    public TradeSetup breakOut(Stock stock, Timeframe timeframe);

    public TradeSetup breakDown(Stock stock, Timeframe timeframe);
}
