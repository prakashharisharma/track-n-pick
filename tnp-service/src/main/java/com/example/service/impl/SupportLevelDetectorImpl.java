package com.example.service.impl;

import static com.example.data.common.type.Timeframe.*;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SupportLevelDetectorImpl implements SupportLevelDetector {

    private static final double THRESHOLD = 0.0168; // 1.68% tolerance

    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final SupportResistanceConfirmationService supportResistanceConfirmationService;
    private final BreakoutBreakdownConfirmationService breakoutBreakdownConfirmationService;

    @Override
    public boolean isNearSupport(Stock stock, Timeframe timeframe) {
        StockPrice currentStockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (currentStockPrice == null) return false;

        List<Double> supportLevels = getRelevantSupportLevels(stock, timeframe);
        if (supportLevels.isEmpty()) return false;

        double supportLevel = findConfluenceSupport(supportLevels);

        boolean isNear = checkSupport(currentStockPrice, supportLevel);
        boolean isConfirmed =
                supportResistanceConfirmationService.isSupportConfirmed(
                        timeframe, currentStockPrice, stockTechnicals, supportLevel);

        return isNear && isConfirmed;
    }

    @Override
    public boolean isBreakDown(Stock stock, Timeframe timeframe) {
        StockPrice currentStockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (currentStockPrice == null) return false;

        boolean isSupportBreak = !this.isNearSupport(stock, timeframe);
        boolean isMultiTimeFrameBreakdown = this.isMultiTimeFrameBreakdown(stock, timeframe);
        boolean potentialBreakdown = isSupportBreak || isMultiTimeFrameBreakdown;

        if (!potentialBreakdown) return false;

        boolean isConfirmed =
                breakoutBreakdownConfirmationService.isBreakdownConfirmed(
                        timeframe, currentStockPrice, stockTechnicals, currentStockPrice.getLow());

        if (isConfirmed) {
            log.warn(
                    "Breakdown confirmed for {} at timeframe {} | Current Price: {}",
                    stock.getNseSymbol(),
                    timeframe,
                    currentStockPrice.getLow());
        }

        return isConfirmed;
    }

    private boolean isMultiTimeFrameBreakdown(Stock stock, Timeframe timeframe) {
        if (timeframe == DAILY) {
            return !this.isNearSupport(stock, WEEKLY) && !this.isNearSupport(stock, MONTHLY);
        } else if (timeframe == WEEKLY) {
            return !this.isNearSupport(stock, MONTHLY)
                    && !this.isNearSupport(stock, Timeframe.QUARTERLY);
        } else if (timeframe == MONTHLY) {
            return !this.isNearSupport(stock, Timeframe.QUARTERLY)
                    && !this.isNearSupport(stock, Timeframe.YEARLY);
        }
        return false;
    }

    private List<Double> getRelevantSupportLevels(Stock stock, Timeframe timeframe) {
        switch (timeframe) {
            case MONTHLY:
                return getSupportLevels(stock, Timeframe.QUARTERLY, Timeframe.YEARLY, 2, 2);
            case WEEKLY:
                return getSupportLevels(stock, MONTHLY, Timeframe.QUARTERLY, 3, 2);
            case DAILY:
                return getSupportLevels(stock, WEEKLY, MONTHLY, 5, 3);
            default:
                return List.of();
        }
    }

    private List<Double> getSupportLevels(
            Stock stock, Timeframe t1, Timeframe t2, int limit1, int limit2) {
        StockPrice sp1 = stockPriceService.get(stock, t1);
        StockPrice sp2 = stockPriceService.get(stock, t2);
        if (sp1 == null || sp2 == null) return List.of();

        return Stream.concat(
                        getRecentLows(sp1, limit1).stream(), getRecentLows(sp2, limit2).stream())
                .collect(Collectors.toList());
    }

    private List<Double> getRecentLows(StockPrice sp, int limit) {
        return Stream.of(
                        sp.getLow(),
                        sp.getPrevLow(),
                        sp.getPrev2Low(),
                        sp.getPrev3Low(),
                        sp.getPrev4Low())
                .limit(limit)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private double findConfluenceSupport(List<Double> supportLevels) {
        double minSupport = supportLevels.stream().mapToDouble(v -> v).min().orElse(Double.NaN);
        double avgSupport = supportLevels.stream().mapToDouble(v -> v).average().orElse(Double.NaN);

        return isConfluence(minSupport, avgSupport, THRESHOLD) ? avgSupport : minSupport;
    }

    private boolean checkSupport(StockPrice currentStockPrice, double supportLevel) {
        double currentLow = currentStockPrice.getLow();
        if (Math.abs(currentLow - supportLevel) / supportLevel <= THRESHOLD) {
            if (currentLow > supportLevel) {
                log.info(
                        "Support is holding for {}. Possible bounce at {}.",
                        currentLow,
                        supportLevel);
                return true;
            } else {
                log.warn(
                        "Price closed below support {} at {}. Possible breakdown.",
                        supportLevel,
                        currentLow);
            }
        }
        return false;
    }

    private boolean isConfluence(double level1, double level2, double threshold) {
        return Math.abs(level1 - level2) / level2 <= threshold;
    }
}
