package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;

public interface BreakoutBreakdownConfirmationService {

    public boolean isBreakoutConfirmed(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double average);

    public boolean isBreakdownConfirmed(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double average);
}
