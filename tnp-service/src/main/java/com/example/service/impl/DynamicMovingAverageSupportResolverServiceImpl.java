package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DynamicMovingAverageSupportResolverServiceImpl
        implements DynamicMovingAverageSupportResolverService {

    private final MovingAverageSupportResistanceService
            fiveDaysMovingAverageSupportResistanceService;

    private final MovingAverageSupportResistanceService
            twentyDaysMovingAverageSupportResistanceService;

    private final MovingAverageSupportResistanceService
            fiftyDaysMovingAverageSupportResistanceService;

    private final MovingAverageSupportResistanceService
            oneHundredDaysMovingAverageSupportResistanceService;

    private final MovingAverageSupportResistanceService
            twoHundredDaysMovingAverageSupportResistanceService;

    private final SupportResistanceConfirmationService supportResistanceConfirmationService;

    private final BreakoutBreakdownConfirmationService breakoutBreakdownConfirmationService;

    private final SupportResistanceUtilService supportResistanceService;

    private final CandleStickService candleStickService;

    private final BreakoutService breakoutService;

    private final FormulaService formulaService;

    public DynamicMovingAverageSupportResolverServiceImpl(
            @Qualifier("fiveDayMovingAverageService")
                    MovingAverageSupportResistanceService
                            fiveDaysMovingAverageSupportResistanceService,
            @Qualifier("twentyDayMovingAverageService")
                    MovingAverageSupportResistanceService
                            twentyDaysMovingAverageSupportResistanceService,
            @Qualifier("fiftyDayMovingAverageService")
                    MovingAverageSupportResistanceService
                            fiftyDaysMovingAverageSupportResistanceService,
            @Qualifier("hundredDayMovingAverageService")
                    MovingAverageSupportResistanceService
                            oneHundredDaysMovingAverageSupportResistanceService,
            @Qualifier("twoHundredDayMovingAverageService")
                    MovingAverageSupportResistanceService
                            twoHundredDaysMovingAverageSupportResistanceService,
            SupportResistanceConfirmationService supportResistanceConfirmationService,
            BreakoutBreakdownConfirmationService breakoutBreakdownConfirmationService,
            SupportResistanceUtilService supportResistanceService,
            CandleStickService candleStickService,
            BreakoutService breakoutService,
            FormulaService formulaService) {
        this.fiveDaysMovingAverageSupportResistanceService =
                fiveDaysMovingAverageSupportResistanceService;
        this.twentyDaysMovingAverageSupportResistanceService =
                twentyDaysMovingAverageSupportResistanceService;
        this.fiftyDaysMovingAverageSupportResistanceService =
                fiftyDaysMovingAverageSupportResistanceService;
        this.oneHundredDaysMovingAverageSupportResistanceService =
                oneHundredDaysMovingAverageSupportResistanceService;
        this.twoHundredDaysMovingAverageSupportResistanceService =
                twoHundredDaysMovingAverageSupportResistanceService;
        this.supportResistanceConfirmationService = supportResistanceConfirmationService;
        this.breakoutBreakdownConfirmationService = breakoutBreakdownConfirmationService;
        this.supportResistanceService = supportResistanceService;
        this.candleStickService = candleStickService;
        this.breakoutService = breakoutService;
        this.formulaService = formulaService;
    }

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

    @Override
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

    @Override
    public boolean isBottomBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        MovingAverageLength length = resolveLength(trend.getMomentum());

        if (length.getPeriod() >= 2) {
            MovingAverageSupportResistanceService service =
                    resolve(length, timeframe, stockTechnicals);
            return service.isBreakout(timeframe, stockPrice, stockTechnicals);
        }

        return false;
    }

    @Override
    public boolean isTopBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        MovingAverageLength length = resolveLength(trend.getMomentum());
        if (length == MovingAverageLength.SHORTEST) {
            MovingAverageSupportResistanceService service =
                    resolve(length, timeframe, stockTechnicals);
            service.isBreakdown(timeframe, stockPrice, stockTechnicals);
        }
        return false;
    }
}
