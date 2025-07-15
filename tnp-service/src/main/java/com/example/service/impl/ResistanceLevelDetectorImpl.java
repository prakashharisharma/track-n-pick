package com.example.service.impl;

import static com.example.data.common.type.Timeframe.*;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResistanceLevelDetectorImpl implements ResistanceLevelDetector {
    private static final double THRESHOLD = 0.0168; // 1.68% tolerance
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final SupportResistanceConfirmationService supportResistanceConfirmationService;
    private final BreakoutBreakdownConfirmationService breakoutBreakdownConfirmationService;

    @Override
    public boolean isBreakout(Stock stock, Timeframe timeframe) {
        StockPrice current = stockPriceService.get(stock, timeframe);
        if (current == null) return false;

        List<Double> resistanceLevels = getRelevantResistanceLevels(stock, timeframe);
        if (resistanceLevels.isEmpty()) return false;

        double resistanceLevel = findConfluenceResistance(resistanceLevels);
        double currentHigh = current.getHigh();

        // Breakout = high is significantly above resistance (beyond threshold)
        double deviation = (currentHigh - resistanceLevel) / resistanceLevel;
        boolean brokeAboveResistance = deviation > THRESHOLD;

        boolean isMultiTimeFrameBreakout = this.isMultiTimeFrameBreakout(stock, timeframe);
        boolean potentialBreakout = brokeAboveResistance || isMultiTimeFrameBreakout;

        return potentialBreakout;
    }

    private boolean isMultiTimeFrameBreakout(Stock stock, Timeframe timeframe) {
        if (timeframe == DAILY) {
            return this.isBreakout(stock, WEEKLY) && this.isBreakout(stock, MONTHLY);
        } else if (timeframe == WEEKLY) {
            return this.isBreakout(stock, MONTHLY) && this.isBreakout(stock, Timeframe.QUARTERLY);
        } else if (timeframe == MONTHLY) {
            return this.isBreakout(stock, Timeframe.QUARTERLY)
                    && this.isBreakout(stock, Timeframe.YEARLY);
        }
        return false;
    }

    @Override
    public boolean isNearResistance(Stock stock, Timeframe timeframe) {
        StockPrice currentStockPrice = stockPriceService.get(stock, timeframe);
        if (currentStockPrice == null) return false;

        List<Double> resistanceLevels = getRelevantResistanceLevels(stock, timeframe);

        if (resistanceLevels.isEmpty()) return false;

        double resistanceLevel = findConfluenceResistance(resistanceLevels);

        boolean isNear = checkResistance(currentStockPrice, resistanceLevel);

        boolean isMultiTimeFrameResistance = this.isMultiTimeFrameResistance(stock, timeframe);
        boolean potentialResistance = isNear || isMultiTimeFrameResistance;

        return potentialResistance;
    }

    private boolean isMultiTimeFrameResistance(Stock stock, Timeframe timeframe) {
        if (timeframe == DAILY) {
            return this.isBreakout(stock, WEEKLY) && this.isBreakout(stock, MONTHLY);
        } else if (timeframe == WEEKLY) {
            return this.isBreakout(stock, MONTHLY) && this.isBreakout(stock, Timeframe.QUARTERLY);
        } else if (timeframe == MONTHLY) {
            return this.isBreakout(stock, Timeframe.QUARTERLY)
                    && this.isBreakout(stock, Timeframe.YEARLY);
        }
        return false;
    }

    private List<Double> getRelevantResistanceLevels(Stock stock, Timeframe timeframe) {
        switch (timeframe) {
            case MONTHLY:
                return getResistanceLevels(stock, Timeframe.QUARTERLY, Timeframe.YEARLY, 2, 2);
            case WEEKLY:
                return getResistanceLevels(stock, MONTHLY, Timeframe.QUARTERLY, 3, 2);
            case DAILY:
                return getResistanceLevels(stock, WEEKLY, MONTHLY, 5, 3);
            default:
                return List.of();
        }
    }

    private List<Double> getResistanceLevels(
            Stock stock, Timeframe t1, Timeframe t2, int limit1, int limit2) {
        StockPrice sp1 = stockPriceService.get(stock, t1);
        StockPrice sp2 = stockPriceService.get(stock, t2);
        if (sp1 == null || sp2 == null) return List.of();

        return Stream.concat(
                        getRecentHighs(sp1, limit1).stream(), getRecentHighs(sp2, limit2).stream())
                .collect(Collectors.toList());
    }

    private List<Double> getRecentHighs(StockPrice sp, int limit) {
        return Stream.of(
                        sp.getHigh(),
                        sp.getPrevHigh(),
                        sp.getPrev2High(),
                        sp.getPrev3High(),
                        sp.getPrev4High())
                .limit(limit)
                .filter(high -> high != null)
                .collect(Collectors.toList());
    }

    private double findConfluenceResistance(List<Double> resistanceLevels) {
        double maxResistance =
                resistanceLevels.stream().mapToDouble(v -> v).max().orElse(Double.NaN);
        double avgResistance =
                resistanceLevels.stream().mapToDouble(v -> v).average().orElse(Double.NaN);

        return isConfluence(maxResistance, avgResistance, THRESHOLD)
                ? avgResistance
                : maxResistance;
    }

    private boolean checkResistance(StockPrice currentStockPrice, double resistanceLevel) {
        double currentHigh = currentStockPrice.getHigh();
        double deviation = Math.abs(currentHigh - resistanceLevel) / resistanceLevel;

        if (deviation > THRESHOLD) return false;

        if (currentHigh < resistanceLevel) {
            log.info("Resistance is holding. Possible rejection.");
            return true;
        }

        log.info("Price broke above resistance. Possible breakout.");
        return false;
    }

    private boolean isConfluence(double level1, double level2, double threshold) {
        return Math.abs(level1 - level2) / level2 <= threshold;
    }
}
