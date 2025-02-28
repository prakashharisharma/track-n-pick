package com.example.service;

import com.example.dto.TradeSetup;
import com.example.model.master.Stock;

public interface CandleStickExecutorService {
    public void executeBullish(Stock stock);
    public void executeBullishInDecisionConfirmation(Stock stock);
    public void executeBearish(Stock stock);
    public void executeBearishInDecisionConfirmation(Stock stock);

}
