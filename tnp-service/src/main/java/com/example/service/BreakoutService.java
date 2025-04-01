package com.example.service;

public interface BreakoutService {

    public boolean isBreakOut(double prevClose, double prevAverage, double close, double average);

    public boolean isBreakDown(double prevClose, double prevAverage, double close, double average);
}
