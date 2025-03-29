package com.example.service;

public interface SupportResistanceUtilService {


    public boolean isNearSupport(double open,double high, double low, double close, double average);

    public boolean isNearResistance(double open,double high, double low, double close, double average);

}
