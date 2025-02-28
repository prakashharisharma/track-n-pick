package com.example.service;

import com.example.model.master.Stock;

public interface SupportResistanceConfirmationService {

    public boolean isSupportConfirmed(Stock stock, double average);

    public boolean isResistanceConfirmed(Stock stock, double average);
}
