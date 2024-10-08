package com.example.service;

import com.example.model.um.UserProfile;

public interface UserBrokerageService {

    public double calculate(UserProfile user, double price, long quantity);

    public double getDpCharge(UserProfile user);
}
