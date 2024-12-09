package com.example.service;

import com.example.model.master.Stock;

public interface CandleStickExecutorService {
    public void executeBullish(Stock stock);
    public void executeBearish(Stock stock);
}
