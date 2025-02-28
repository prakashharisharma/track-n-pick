package com.example.service;

import com.example.model.master.Stock;

public interface ObvIndicatorService {

    public boolean isBullish(Stock stock);

    public boolean isBearish(Stock stock);
}
