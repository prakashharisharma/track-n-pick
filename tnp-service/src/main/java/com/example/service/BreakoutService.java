package com.example.service;

import com.example.data.transactional.entities.StockPrice;

public interface BreakoutService {

    public boolean isBreakOut(StockPrice price, double average, double prevAverage);

    public boolean isBreakDown(StockPrice price, double average, double prevAverage);

}
