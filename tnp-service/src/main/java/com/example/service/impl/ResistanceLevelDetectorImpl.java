package com.example.service.impl;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.service.StockPriceService;
import com.example.transactional.model.master.Stock;
import com.example.service.ResistanceLevelDetector;
import com.example.util.io.model.type.Timeframe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.util.io.model.type.Timeframe.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResistanceLevelDetectorImpl implements ResistanceLevelDetector {
    private static final double THRESHOLD = 0.0168; // 1.68% tolerance
    private final StockPriceService<StockPrice> stockPriceService;

    @Override
    public boolean isBreakout(Stock stock, Timeframe timeframe) {
        StockPrice currentStockPrice = stockPriceService.get(stock, timeframe);
        if (currentStockPrice == null) return false;

        boolean isResistanceBreak = this.isNearResistance(stock, timeframe);
        boolean isMultiTimeFrameBreakout = this.isMultiTimeFrameBreakout(stock, timeframe);

        boolean breakoutConfirmed = isResistanceBreak || isMultiTimeFrameBreakout;

        if (breakoutConfirmed) {
            System.out.println("Breakout detected for " + stock.getNseSymbol() + " at " + timeframe);
        }

        return breakoutConfirmed;
    }


    private boolean isMultiTimeFrameBreakout(Stock stock, Timeframe timeframe) {
        if(timeframe == DAILY) {
            return this.isNearResistance(stock, Timeframe.WEEKLY) && this.isNearResistance(stock, Timeframe.MONTHLY);
        }
        else if(timeframe == WEEKLY) {
            return this.isNearResistance(stock, Timeframe.MONTHLY) && this.isNearResistance(stock, Timeframe.QUARTERLY);
        }
        else if(timeframe == MONTHLY) {
            return this.isNearResistance(stock, Timeframe.QUARTERLY) && this.isNearResistance(stock, Timeframe.YEARLY);
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
        return checkResistance(currentStockPrice, resistanceLevel);
    }

    private List<Double> getRelevantResistanceLevels(Stock stock, Timeframe timeframe) {
        switch (timeframe) {
            case MONTHLY:
                return getResistanceLevels(stock, Timeframe.QUARTERLY, Timeframe.YEARLY, 2, 2);
            case WEEKLY:
                return getResistanceLevels(stock, Timeframe.MONTHLY, Timeframe.QUARTERLY, 3, 2);
            case DAILY:
                return getResistanceLevels(stock, Timeframe.WEEKLY, Timeframe.MONTHLY, 5, 3);
            default:
                return List.of();
        }
    }

    private List<Double> getResistanceLevels(Stock stock, Timeframe t1, Timeframe t2, int limit1, int limit2) {
        StockPrice sp1 = stockPriceService.get(stock, t1);
        StockPrice sp2 = stockPriceService.get(stock, t2);
        if (sp1 == null || sp2 == null) return List.of();

        return Stream.concat(
                getRecentHighs(sp1, limit1).stream(),
                getRecentHighs(sp2, limit2).stream()
        ).collect(Collectors.toList());
    }

    private List<Double> getRecentHighs(StockPrice sp, int limit) {
        return Stream.of(sp.getHigh(), sp.getPrevHigh(), sp.getPrev2High(), sp.getPrev3High(), sp.getPrev4High())
                .limit(limit)
                .filter(high -> high != null)
                .collect(Collectors.toList());
    }

    private double findConfluenceResistance(List<Double> resistanceLevels) {
        double maxResistance = resistanceLevels.stream().mapToDouble(v -> v).max().orElse(Double.NaN);
        double avgResistance = resistanceLevels.stream().mapToDouble(v -> v).average().orElse(Double.NaN);

        return isConfluence(maxResistance, avgResistance, THRESHOLD) ? avgResistance : maxResistance;
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
