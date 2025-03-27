package com.example.service.impl;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Timeframe;
import com.example.util.io.model.type.Trend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class MultiMovingAverageSupportResistanceServiceImpl implements MultiMovingAverageSupportResistanceService {

    private final FiveDaysMovingAverageSupportResistanceService fiveDaysMovingAverageSupportResistanceService;

    private final TenDaysMovingAverageSupportResistanceService tenDaysMovingAverageSupportResistanceService;

    private final TwentyDaysMovingAverageSupportResistanceService twentyDaysMovingAverageSupportResistanceService;

    private final FiftyDaysMovingAverageSupportResistanceService fiftyDaysMovingAverageSupportResistanceService;

    private final OneHundredDaysMovingAverageSupportResistanceService oneHundredDaysMovingAverageSupportResistanceService;

    private final TwoHundredDaysMovingAverageSupportResistanceService twoHundredDaysMovingAverageSupportResistanceService;


    private  final SupportResistanceConfirmationService supportResistanceConfirmationService;

    private final BreakoutConfirmationService breakoutConfirmationService;


    private final SupportResistanceUtilService supportResistanceService;


    private final CandleStickService candleStickService;


    private final BreakoutService breakoutService;


    private final FormulaService formulaService;



    @Override
    public boolean isBreakout(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Momentum, List<MovingAverageSupportResistanceService>>> breakoutLevels = new HashMap<>();

        // Daily Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> dailyBreakout = new HashMap<>();
        dailyBreakout.put(Trend.Momentum.PULLBACK, List.of(twentyDaysMovingAverageSupportResistanceService));
        dailyBreakout.put(Trend.Momentum.CORRECTION, List.of(fiftyDaysMovingAverageSupportResistanceService));
        dailyBreakout.put(Trend.Momentum.BOTTOM, List.of(oneHundredDaysMovingAverageSupportResistanceService, twoHundredDaysMovingAverageSupportResistanceService));

        // Weekly Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> weeklyBreakout = new HashMap<>();
        weeklyBreakout.put(Trend.Momentum.PULLBACK, List.of(tenDaysMovingAverageSupportResistanceService));
        weeklyBreakout.put(Trend.Momentum.CORRECTION, List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklyBreakout.put(Trend.Momentum.BOTTOM, List.of(fiftyDaysMovingAverageSupportResistanceService, oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        // Monthly Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> monthlyBreakout = new HashMap<>();
        monthlyBreakout.put(Trend.Momentum.PULLBACK, List.of(fiveDaysMovingAverageSupportResistanceService));
        monthlyBreakout.put(Trend.Momentum.CORRECTION, List.of(tenDaysMovingAverageSupportResistanceService));
        monthlyBreakout.put(Trend.Momentum.BOTTOM, List.of(twentyDaysMovingAverageSupportResistanceService, fiftyDaysMovingAverageSupportResistanceService,oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        breakoutLevels.put(Timeframe.DAILY, dailyBreakout);
        breakoutLevels.put(Timeframe.WEEKLY, weeklyBreakout);
        breakoutLevels.put(Timeframe.MONTHLY, monthlyBreakout);

        // Check for breakout
        return breakoutLevels.getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(service -> service.isBreakout(timeframe, stockPrice, stockTechnicals));
    }

    @Override
    public boolean isNearSupport(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Momentum, List<MovingAverageSupportResistanceService>>> supportLevels = new HashMap<>();

        // Manually populating the maps to avoid type inference issues
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> dailySupport = new HashMap<>();
        dailySupport.put(Trend.Momentum.PULLBACK, List.of(twentyDaysMovingAverageSupportResistanceService));
        dailySupport.put(Trend.Momentum.CORRECTION, List.of(fiftyDaysMovingAverageSupportResistanceService));
        dailySupport.put(Trend.Momentum.BOTTOM, List.of(oneHundredDaysMovingAverageSupportResistanceService, twoHundredDaysMovingAverageSupportResistanceService));

        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> weeklySupport = new HashMap<>();
        weeklySupport.put(Trend.Momentum.PULLBACK, List.of(tenDaysMovingAverageSupportResistanceService));
        weeklySupport.put(Trend.Momentum.CORRECTION, List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklySupport.put(Trend.Momentum.BOTTOM, List.of(fiftyDaysMovingAverageSupportResistanceService, oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> monthlySupport = new HashMap<>();
        monthlySupport.put(Trend.Momentum.PULLBACK, List.of(fiveDaysMovingAverageSupportResistanceService));
        monthlySupport.put(Trend.Momentum.CORRECTION, List.of(tenDaysMovingAverageSupportResistanceService));
        monthlySupport.put(Trend.Momentum.BOTTOM, List.of(twentyDaysMovingAverageSupportResistanceService, fiftyDaysMovingAverageSupportResistanceService,oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        supportLevels.put(Timeframe.DAILY, dailySupport);
        supportLevels.put(Timeframe.WEEKLY, weeklySupport);
        supportLevels.put(Timeframe.MONTHLY, monthlySupport);

        // Check if near support
        return supportLevels.getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(service -> service.isNearSupport(timeframe, stockPrice, stockTechnicals));
    }


    @Override
    public boolean isBreakdown(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Momentum, List<MovingAverageSupportResistanceService>>> breakdownLevels = new HashMap<>();

        // Daily Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> dailyBreakdown = new HashMap<>();
        dailyBreakdown.put(Trend.Momentum.RECOVERY, List.of(twentyDaysMovingAverageSupportResistanceService));
        dailyBreakdown.put(Trend.Momentum.ADVANCE, List.of(fiftyDaysMovingAverageSupportResistanceService));
        dailyBreakdown.put(Trend.Momentum.TOP, List.of(oneHundredDaysMovingAverageSupportResistanceService, twoHundredDaysMovingAverageSupportResistanceService));

        // Weekly Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> weeklyBreakdown = new HashMap<>();
        weeklyBreakdown.put(Trend.Momentum.RECOVERY, List.of(tenDaysMovingAverageSupportResistanceService));
        weeklyBreakdown.put(Trend.Momentum.ADVANCE, List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklyBreakdown.put(Trend.Momentum.TOP, List.of(fiftyDaysMovingAverageSupportResistanceService, oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        // Monthly Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> monthlyBreakdown = new HashMap<>();
        monthlyBreakdown.put(Trend.Momentum.RECOVERY, List.of(fiveDaysMovingAverageSupportResistanceService));
        monthlyBreakdown.put(Trend.Momentum.ADVANCE, List.of(tenDaysMovingAverageSupportResistanceService));
        monthlyBreakdown.put(Trend.Momentum.TOP, List.of(twentyDaysMovingAverageSupportResistanceService, fiftyDaysMovingAverageSupportResistanceService,oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        breakdownLevels.put(Timeframe.DAILY, dailyBreakdown);
        breakdownLevels.put(Timeframe.WEEKLY, weeklyBreakdown);
        breakdownLevels.put(Timeframe.MONTHLY, monthlyBreakdown);

        // Check for breakdown
        return breakdownLevels.getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(service -> service.isBreakdown(timeframe,stockPrice, stockTechnicals ));
    }

    @Override
    public boolean isNearResistance(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Map<Timeframe, Map<Trend.Momentum, List<MovingAverageSupportResistanceService>>> resistanceLevels = new HashMap<>();

        // Daily Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> dailyResistance = new HashMap<>();
        dailyResistance.put(Trend.Momentum.RECOVERY, List.of(twentyDaysMovingAverageSupportResistanceService));
        dailyResistance.put(Trend.Momentum.ADVANCE, List.of(fiftyDaysMovingAverageSupportResistanceService));
        dailyResistance.put(Trend.Momentum.TOP, List.of(oneHundredDaysMovingAverageSupportResistanceService, twoHundredDaysMovingAverageSupportResistanceService));

        // Weekly Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> weeklyResistance = new HashMap<>();
        weeklyResistance.put(Trend.Momentum.RECOVERY, List.of(tenDaysMovingAverageSupportResistanceService));
        weeklyResistance.put(Trend.Momentum.ADVANCE, List.of(twentyDaysMovingAverageSupportResistanceService));
        weeklyResistance.put(Trend.Momentum.TOP, List.of(fiftyDaysMovingAverageSupportResistanceService, oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        // Monthly Timeframe
        Map<Trend.Momentum, List<MovingAverageSupportResistanceService>> monthlyResistance = new HashMap<>();
        monthlyResistance.put(Trend.Momentum.RECOVERY, List.of(fiveDaysMovingAverageSupportResistanceService));
        monthlyResistance.put(Trend.Momentum.ADVANCE, List.of(tenDaysMovingAverageSupportResistanceService));
        monthlyResistance.put(Trend.Momentum.TOP, List.of(twentyDaysMovingAverageSupportResistanceService, fiftyDaysMovingAverageSupportResistanceService,oneHundredDaysMovingAverageSupportResistanceService,twoHundredDaysMovingAverageSupportResistanceService));

        // Populate the main map
        resistanceLevels.put(Timeframe.DAILY, dailyResistance);
        resistanceLevels.put(Timeframe.WEEKLY, weeklyResistance);
        resistanceLevels.put(Timeframe.MONTHLY, monthlyResistance);

        // Check for near resistance
        return resistanceLevels.getOrDefault(timeframe, Collections.emptyMap())
                .getOrDefault(trend.getMomentum(), Collections.emptyList())
                .stream()
                .anyMatch(service -> service.isNearResistance(timeframe, stockPrice, stockTechnicals));
    }
}
