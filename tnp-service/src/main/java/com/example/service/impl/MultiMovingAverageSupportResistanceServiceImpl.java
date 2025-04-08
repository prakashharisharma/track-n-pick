package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.util.FormulaService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MultiMovingAverageSupportResistanceServiceImpl
        implements MultiMovingAverageSupportResistanceService {

    private final FiveDaysMovingAverageSupportResistanceService
            fiveDaysMovingAverageSupportResistanceService;

    private final TenDaysMovingAverageSupportResistanceService
            tenDaysMovingAverageSupportResistanceService;

    private final TwentyDaysMovingAverageSupportResistanceService
            twentyDaysMovingAverageSupportResistanceService;

    private final FiftyDaysMovingAverageSupportResistanceService
            fiftyDaysMovingAverageSupportResistanceService;

    private final OneHundredDaysMovingAverageSupportResistanceService
            oneHundredDaysMovingAverageSupportResistanceService;

    private final TwoHundredDaysMovingAverageSupportResistanceService
            twoHundredDaysMovingAverageSupportResistanceService;

    private final SupportResistanceConfirmationService supportResistanceConfirmationService;

    private final BreakoutBreakdownConfirmationService breakoutBreakdownConfirmationService;

    private final SupportResistanceUtilService supportResistanceService;

    private final CandleStickService candleStickService;

    private final BreakoutService breakoutService;

    private final FormulaService formulaService;

    @Override
    public boolean isBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Phase, List<MovingAverageSupportResistanceService>>>
                breakoutLevels = new HashMap<>();

        // Daily Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> dailyBreakout =
                new HashMap<>();

        dailyBreakout.put(
                Trend.Phase.DIP, List.of(twentyDaysMovingAverageSupportResistanceService));
        dailyBreakout.put(
                Trend.Phase.PULLBACK,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        dailyBreakout.put(
                Trend.Phase.CORRECTION,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        dailyBreakout.put(
                Trend.Phase.BOTTOM,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Weekly Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> weeklyBreakout =
                new HashMap<>();
        weeklyBreakout.put(
                Trend.Phase.DIP, List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklyBreakout.put(
                Trend.Phase.PULLBACK,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        weeklyBreakout.put(
                Trend.Phase.CORRECTION,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        weeklyBreakout.put(
                Trend.Phase.BOTTOM,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Monthly Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> monthlyBreakout =
                new HashMap<>();

        monthlyBreakout.put(Trend.Phase.DIP, List.of(tenDaysMovingAverageSupportResistanceService));
        monthlyBreakout.put(
                Trend.Phase.PULLBACK,
                List.of(
                        tenDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        monthlyBreakout.put(
                Trend.Phase.CORRECTION,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        monthlyBreakout.put(
                Trend.Phase.BOTTOM,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        breakoutLevels.put(Timeframe.DAILY, dailyBreakout);
        breakoutLevels.put(Timeframe.WEEKLY, weeklyBreakout);
        breakoutLevels.put(Timeframe.MONTHLY, monthlyBreakout);

        // Check for breakout
        return breakoutLevels
                .getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(service -> service.isBreakout(timeframe, stockPrice, stockTechnicals));
    }

    @Override
    public boolean isNearSupport(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Phase, List<MovingAverageSupportResistanceService>>>
                supportLevels = new HashMap<>();

        // Manually populating the maps to avoid type inference issues
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> dailySupport =
                new HashMap<>();

        dailySupport.put(Trend.Phase.DIP, List.of(twentyDaysMovingAverageSupportResistanceService));
        dailySupport.put(
                Trend.Phase.PULLBACK,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        dailySupport.put(
                Trend.Phase.CORRECTION,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        dailySupport.put(
                Trend.Phase.BOTTOM,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> weeklySupport =
                new HashMap<>();
        weeklySupport.put(
                Trend.Phase.DIP, List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklySupport.put(
                Trend.Phase.PULLBACK,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        weeklySupport.put(
                Trend.Phase.CORRECTION,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        weeklySupport.put(
                Trend.Phase.BOTTOM,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> monthlySupport =
                new HashMap<>();
        monthlySupport.put(
                Trend.Phase.DIP, List.of(twentyDaysMovingAverageSupportResistanceService));
        monthlySupport.put(
                Trend.Phase.PULLBACK,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        monthlySupport.put(
                Trend.Phase.CORRECTION,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        monthlySupport.put(
                Trend.Phase.BOTTOM,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        supportLevels.put(Timeframe.DAILY, dailySupport);
        supportLevels.put(Timeframe.WEEKLY, weeklySupport);
        supportLevels.put(Timeframe.MONTHLY, monthlySupport);

        // Check if near support
        return supportLevels
                .getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(service -> service.isNearSupport(timeframe, stockPrice, stockTechnicals));
    }

    @Override
    public boolean isBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Phase, List<MovingAverageSupportResistanceService>>>
                breakdownLevels = new HashMap<>();

        // Daily Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> dailyBreakdown =
                new HashMap<>();
        dailyBreakdown.put(
                Trend.Phase.EARLY_RECOVERY,
                List.of(twentyDaysMovingAverageSupportResistanceService));
        dailyBreakdown.put(
                Trend.Phase.RECOVERY,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        dailyBreakdown.put(
                Trend.Phase.ADVANCE,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        dailyBreakdown.put(
                Trend.Phase.TOP,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Weekly Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> weeklyBreakdown =
                new HashMap<>();
        weeklyBreakdown.put(
                Trend.Phase.EARLY_RECOVERY,
                List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklyBreakdown.put(
                Trend.Phase.RECOVERY,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        weeklyBreakdown.put(
                Trend.Phase.ADVANCE,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        weeklyBreakdown.put(
                Trend.Phase.TOP,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Monthly Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> monthlyBreakdown =
                new HashMap<>();
        monthlyBreakdown.put(
                Trend.Phase.EARLY_RECOVERY,
                List.of(twentyDaysMovingAverageSupportResistanceService));
        monthlyBreakdown.put(
                Trend.Phase.RECOVERY,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        monthlyBreakdown.put(
                Trend.Phase.ADVANCE,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        monthlyBreakdown.put(
                Trend.Phase.TOP,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        breakdownLevels.put(Timeframe.DAILY, dailyBreakdown);
        breakdownLevels.put(Timeframe.WEEKLY, weeklyBreakdown);
        breakdownLevels.put(Timeframe.MONTHLY, monthlyBreakdown);

        // Check for breakdown
        return breakdownLevels
                .getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(service -> service.isBreakdown(timeframe, stockPrice, stockTechnicals));
    }

    @Override
    public boolean isNearResistance(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Phase, List<MovingAverageSupportResistanceService>>>
                resistanceLevels = new HashMap<>();

        // Daily Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> dailyResistance =
                new HashMap<>();
        dailyResistance.put(
                Trend.Phase.EARLY_RECOVERY,
                List.of(twentyDaysMovingAverageSupportResistanceService));
        dailyResistance.put(
                Trend.Phase.RECOVERY,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        dailyResistance.put(
                Trend.Phase.ADVANCE,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        dailyResistance.put(
                Trend.Phase.TOP,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Weekly Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> weeklyResistance =
                new HashMap<>();
        weeklyResistance.put(
                Trend.Phase.EARLY_RECOVERY,
                List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklyResistance.put(
                Trend.Phase.RECOVERY,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        weeklyResistance.put(
                Trend.Phase.ADVANCE,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        weeklyResistance.put(
                Trend.Phase.TOP,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Monthly Timeframe
        Map<Trend.Phase, List<MovingAverageSupportResistanceService>> monthlyResistance =
                new HashMap<>();
        monthlyResistance.put(
                Trend.Phase.EARLY_RECOVERY,
                List.of(twentyDaysMovingAverageSupportResistanceService));
        monthlyResistance.put(
                Trend.Phase.RECOVERY,
                List.of(
                        twentyDaysMovingAverageSupportResistanceService,
                        fiftyDaysMovingAverageSupportResistanceService));
        monthlyResistance.put(
                Trend.Phase.ADVANCE,
                List.of(
                        fiftyDaysMovingAverageSupportResistanceService,
                        oneHundredDaysMovingAverageSupportResistanceService));
        monthlyResistance.put(
                Trend.Phase.TOP,
                List.of(
                        oneHundredDaysMovingAverageSupportResistanceService,
                        twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        resistanceLevels.put(Timeframe.DAILY, dailyResistance);
        resistanceLevels.put(Timeframe.WEEKLY, weeklyResistance);
        resistanceLevels.put(Timeframe.MONTHLY, monthlyResistance);

        // Check for near resistance
        return resistanceLevels
                .getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(
                        service ->
                                service.isNearResistance(timeframe, stockPrice, stockTechnicals));
    }
}
