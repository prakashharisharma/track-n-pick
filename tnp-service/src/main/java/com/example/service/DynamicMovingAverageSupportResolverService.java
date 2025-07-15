package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import java.util.List;
import java.util.Optional;

public interface DynamicMovingAverageSupportResolverService {
    public boolean isNearSupport(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals);

    public boolean isBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals);

    public boolean isBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals);

    public boolean isNearResistance(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals);

    public MovingAverageSupportResistanceService resolve(
            MovingAverageLength length,
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            boolean sortByValue);

    public List<MAInteraction> findMAInteractions(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean sortByValue);

    public List<MAEvaluationResult> evaluateInteractions(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean sortByValue);

    public Optional<MAEvaluationResult> evaluateSingleInteractionSmart(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean sortByValue);
}
