package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.MovingAverageUtil;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class DynamicMovingAverageSupportResolverServiceImpl
        implements DynamicMovingAverageSupportResolverService {

    // Inject or initialize your services as needed
    private final FiveDaysMovingAverageSupportResistanceService
            fiveDaysMovingAverageSupportResistanceService;
    private final TwentyDaysMovingAverageSupportResistanceService
            twentyDaysMovingAverageSupportResistanceService;
    private final FiftyDaysMovingAverageSupportResistanceService
            fiftyDaysMovingAverageSupportResistanceService;
    private final OneHundredDaysMovingAverageSupportResistanceService
            oneHundredDaysMovingAverageSupportResistanceService;
    private final TwoHundredDaysMovingAverageSupportResistanceService
            twoHundredDaysMovingAverageSupportResistanceService;

    @Override
    public boolean isNearSupport(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        MovingAverageLength length = resolveLength(trend.getMomentum());
        MovingAverageSupportResistanceService service = resolve(length, timeframe, stockTechnicals);
        return service.isNearSupport(timeframe, stockPrice, stockTechnicals);
    }

    @Override
    public boolean isBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        MovingAverageLength length = resolveLength(trend.getMomentum());
        MovingAverageSupportResistanceService service = resolve(length, timeframe, stockTechnicals);
        return service.isBreakout(timeframe, stockPrice, stockTechnicals);
    }

    @Override
    public boolean isBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        MovingAverageLength length = resolveLength(trend.getMomentum());
        MovingAverageSupportResistanceService service = resolve(length, timeframe, stockTechnicals);
        return service.isBreakdown(timeframe, stockPrice, stockTechnicals);
    }

    @Override
    public boolean isNearResistance(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        MovingAverageLength length = resolveLength(trend.getMomentum());
        MovingAverageSupportResistanceService service = resolve(length, timeframe, stockTechnicals);
        return service.isNearResistance(timeframe, stockPrice, stockTechnicals);
    }

    public static MovingAverageLength resolveLength(Trend.Phase phase) {
        return switch (phase) {
            case DEEP_CORRECTION, BOTTOM -> MovingAverageLength.LONGEST;
            case CORRECTION -> MovingAverageLength.LONG;
            case PULLBACK -> MovingAverageLength.MEDIUM;
            case DIP -> MovingAverageLength.SHORT;
            default -> MovingAverageLength.SHORTEST;
        };
    }

    public MovingAverageSupportResistanceService resolve(
            MovingAverageLength length, Timeframe timeframe, StockTechnicals stockTechnicals) {
        List<MAServiceEntry> entries =
                List.of(
                        new MAServiceEntry(
                                5,
                                MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals),
                                fiveDaysMovingAverageSupportResistanceService),
                        new MAServiceEntry(
                                20,
                                MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals),
                                twentyDaysMovingAverageSupportResistanceService),
                        new MAServiceEntry(
                                50,
                                MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals),
                                fiftyDaysMovingAverageSupportResistanceService),
                        new MAServiceEntry(
                                100,
                                MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals),
                                oneHundredDaysMovingAverageSupportResistanceService),
                        new MAServiceEntry(
                                200,
                                MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals),
                                twoHundredDaysMovingAverageSupportResistanceService));

        entries =
                entries.stream()
                        .sorted(
                                Comparator.comparingDouble((MAServiceEntry e) -> e.value)
                                        .reversed())
                        .toList();

        int index =
                switch (length) {
                    case SHORTEST -> 0;
                    case SHORT -> 1;
                    case MEDIUM -> 2;
                    case LONG -> 3;
                    case LONGEST -> 4;
                };

        return entries.get(index).service;
    }

    private static class MAServiceEntry {
        int period;
        double value;
        MovingAverageSupportResistanceService service;

        MAServiceEntry(int period, double value, MovingAverageSupportResistanceService service) {
            this.period = period;
            this.value = value;
            this.service = service;
        }
    }
}
