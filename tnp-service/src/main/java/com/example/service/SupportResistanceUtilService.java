package com.example.service;

import com.example.data.transactional.entities.StockPrice;

public interface SupportResistanceUtilService {

    public boolean isNearSupport(
            StockPrice stockPrice, double average, double prevAverage, double prev2Average);

    public boolean isNearSupport(StockPrice stockPrice, double average);

    public boolean isNearResistance(
            StockPrice stockPrice, double average, double prevAverage, double prev2Average);

    public boolean isNearResistance(StockPrice stockPrice, double average);
}
