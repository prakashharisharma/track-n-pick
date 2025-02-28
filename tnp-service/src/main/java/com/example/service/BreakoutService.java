package com.example.service;

import com.example.model.master.Stock;

public interface BreakoutService {

    public boolean isBreakOut(double prevClose, double prevAverage, double close, double average);

    public boolean isBreakDown(double prevClose, double prevAverage, double close, double average);

}
