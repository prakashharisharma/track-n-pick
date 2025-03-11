package com.example.service;

import com.example.model.master.Stock;

public interface MultiIndicatorService {

    public boolean isBullish(Stock stock);

    public boolean isBearish(Stock stock);
}
