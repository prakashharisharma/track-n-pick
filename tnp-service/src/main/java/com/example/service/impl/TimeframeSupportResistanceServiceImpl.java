package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeframeSupportResistanceServiceImpl implements TimeframeSupportResistanceService {

    private final SupportLevelDetector supportLevelDetector;
    private final ResistanceLevelDetector resistanceLevelDetector;

    private final SupportResistanceService supportResistanceService;

    @Override
    public boolean isBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (resistanceLevelDetector.isBreakout(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Resistance breakout for {} at {} timeframe.",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        if (isMultiTimeFrameBreakout(timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "Multi-timeframe breakout confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> supportResistanceService.isBreakout(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isBreakout(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> supportResistanceService.isBreakout(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isBreakout(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            case MONTHLY -> supportResistanceService.isBreakout(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isBreakout(
                            Timeframe.YEARLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }

    @Override
    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (supportLevelDetector.isNearSupport(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Support identified for {} at {} timeframe.",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        if (isMultiTimeFrameSupport(timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "Multi-timeframe support confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> supportResistanceService.isNearSupport(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    || supportResistanceService.isNearSupport(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> supportResistanceService.isNearSupport(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals)
                    || supportResistanceService.isNearSupport(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            case MONTHLY -> supportResistanceService.isNearSupport(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    || supportResistanceService.isNearSupport(
                            Timeframe.YEARLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }

    @Override
    public boolean isBreakdown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (supportLevelDetector.isBreakDown(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Breakdown identified for {} at {} timeframe.",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        if (isMultiTimeFrameBreakdown(timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "Multi-timeframe breakdown confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameBreakdown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> supportResistanceService.isBreakdown(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isBreakdown(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> supportResistanceService.isBreakdown(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isBreakdown(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            case MONTHLY -> supportResistanceService.isBreakdown(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isBreakdown(
                            Timeframe.YEARLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }

    @Override
    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (resistanceLevelDetector.isNearResistance(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Resistance identified for {} at {} timeframe with trend {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        if (isMultiTimeFrameResistance(timeframe, stockPrice, stockTechnicals)) {
            log.debug(
                    "Multi-timeframe resistance confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> supportResistanceService.isNearResistance(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isNearResistance(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> supportResistanceService.isNearResistance(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isNearResistance(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case MONTHLY -> supportResistanceService.isNearResistance(
                            Timeframe.YEARLY, stockPrice, stockTechnicals)
                    && supportResistanceService.isNearResistance(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }
}
