package com.example.service;

import com.example.dto.TradeSetup;
import com.example.model.master.Stock;

public interface PriceActionService {

    public TradeSetup breakOut(Stock stock);
    public TradeSetup breakDown(Stock stock);
}
