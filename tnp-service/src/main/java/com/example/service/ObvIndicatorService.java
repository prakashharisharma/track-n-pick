package com.example.service;


import com.example.data.transactional.entities.StockTechnicals;

public interface ObvIndicatorService {

    public boolean isBullish(StockTechnicals stockTechnicals);

    public boolean isBearish(StockTechnicals stockTechnicals);

    public boolean isObvIncreasing(StockTechnicals stockTechnicals);
    public boolean isObvDecreasing(StockTechnicals stockTechnicals);

    public boolean isObvAvgIncreasing(StockTechnicals stockTechnicals);
    public boolean isObvAvgDecreasing(StockTechnicals stockTechnicals);
}
