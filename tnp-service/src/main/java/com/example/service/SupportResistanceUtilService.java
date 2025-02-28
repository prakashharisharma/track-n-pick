package com.example.service;

import com.example.model.master.Stock;

public interface SupportResistanceUtilService {


    public boolean isNearSupport(double open,double high, double low, double close, double average);

    public boolean isNearResistance(Stock stock);

    public boolean isNearResistance(double open,double high, double low, double close, double average);

}
