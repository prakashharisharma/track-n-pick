package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeframeSupportResistanceServiceImpl implements TimeframeSupportResistanceService {

    private final SupportLevelDetector supportLevelDetector;
    private final ResistanceLevelDetector resistanceLevelDetector;
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final YearlySupportResistanceService yearlySupportResistanceService;
    private final QuarterlySupportResistanceService quarterlySupportResistanceService;
    private final MonthlySupportResistanceService monthlySupportResistanceService;
    private final WeeklySupportResistanceService weeklySupportResistanceService;

    @Override
    public boolean isBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (resistanceLevelDetector.isBreakout(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Resistance breakout for {} at {} timeframe.",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        if (isMultiTimeFrameBreakout(trend, timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "Multi-timeframe breakout confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> weeklySupportResistanceService.isBreakout(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    && monthlySupportResistanceService.isBreakout(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> monthlySupportResistanceService.isBreakout(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals)
                    && quarterlySupportResistanceService.isBreakout(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            case MONTHLY -> quarterlySupportResistanceService.isBreakout(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    && yearlySupportResistanceService.isBreakout(
                            Timeframe.YEARLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }

    @Override
    public boolean isNearSupport(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (supportLevelDetector.isNearSupport(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Support identified for {} at {} timeframe.",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        if (isMultiTimeFrameSupport(trend, timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "Multi-timeframe support confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameSupport(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> weeklySupportResistanceService.isNearSupport(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    || monthlySupportResistanceService.isNearSupport(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> monthlySupportResistanceService.isNearSupport(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals)
                    || quarterlySupportResistanceService.isNearSupport(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            case MONTHLY -> quarterlySupportResistanceService.isNearSupport(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    || yearlySupportResistanceService.isNearSupport(
                            Timeframe.YEARLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }

    @Override
    public boolean isBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (supportLevelDetector.isBreakDown(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Breakdown identified for {} at {} timeframe.",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        if (isMultiTimeFrameBreakdown(trend, timeframe, stockPrice, stockTechnicals)) {
            log.info(
                    "Multi-timeframe breakdown confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> weeklySupportResistanceService.isBreakdown(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    && monthlySupportResistanceService.isBreakdown(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> monthlySupportResistanceService.isBreakdown(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals)
                    && quarterlySupportResistanceService.isBreakdown(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            case MONTHLY -> quarterlySupportResistanceService.isBreakdown(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    && yearlySupportResistanceService.isBreakdown(
                            Timeframe.YEARLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }

    @Override
    public boolean isNearResistance(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (resistanceLevelDetector.isNearResistance(stockPrice.getStock(), timeframe)) {
            log.info(
                    "Resistance identified for {} at {} timeframe with trend {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe,
                    trend);
            return true;
        }

        if (isMultiTimeFrameResistance(trend, timeframe, stockPrice, stockTechnicals)) {
            log.debug(
                    "Multi-timeframe resistance confluence found for {} at {}",
                    stockPrice.getStock().getNseSymbol(),
                    timeframe);
            return true;
        }

        return false;
    }

    private boolean isMultiTimeFrameResistance(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return switch (timeframe) {
            case DAILY -> weeklySupportResistanceService.isNearResistance(
                            Timeframe.WEEKLY, stockPrice, stockTechnicals)
                    && monthlySupportResistanceService.isNearResistance(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case WEEKLY -> quarterlySupportResistanceService.isNearResistance(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals)
                    && monthlySupportResistanceService.isNearResistance(
                            Timeframe.MONTHLY, stockPrice, stockTechnicals);
            case MONTHLY -> yearlySupportResistanceService.isNearResistance(
                            Timeframe.YEARLY, stockPrice, stockTechnicals)
                    && quarterlySupportResistanceService.isNearResistance(
                            Timeframe.QUARTERLY, stockPrice, stockTechnicals);
            default -> false;
        };
    }
}
