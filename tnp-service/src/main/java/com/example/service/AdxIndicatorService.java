package com.example.service;

import com.example.model.master.Stock;

public interface AdxIndicatorService {
    public boolean isAdxIncreasing(Stock stock);

    public boolean isPlusDiIncreasing(Stock stock);

    public boolean isMinusDiIncreasing(Stock stock);

    public boolean isPlusDiDecreasing(Stock stock);

    public boolean isMinusDiDecreasing(Stock stock);

    public boolean isDmiConvergence(Stock stock);

    public boolean isDmiDivergence(Stock stock);
}
