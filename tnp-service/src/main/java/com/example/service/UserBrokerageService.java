package com.example.service;

import com.example.model.um.User;

import java.math.BigDecimal;

public interface UserBrokerageService {

    public double calculate(User user, double price, long quantity);
    public BigDecimal calculate(Long userId, double price, long quantity);
    public double getDpCharge(User user);

    public BigDecimal getDpCharge(Long userId);
}
