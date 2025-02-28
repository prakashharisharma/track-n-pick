package com.example.service;

import com.example.model.master.Stock;

public interface BreakoutConfirmationService {

    public boolean isBullishFollowup(Stock stock, double average);

    public boolean isBullishConfirmation(Stock stock, double average);

    public boolean isBearishFollowup(Stock stock, double average);

    public boolean isBearishConfirmation(Stock stock, double average);
}
