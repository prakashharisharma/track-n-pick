package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.TrendDirectionUtil;
import com.example.util.FormulaService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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
        return service.isNearSupport(timeframe, stockPrice, stockTechnicals, true);
    }

    @Override
    public boolean isBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        MovingAverageLength length = resolveLength(trend.getMomentum());
        MovingAverageSupportResistanceService service = resolve(length, timeframe, stockTechnicals);
        return service.isBreakout(timeframe, stockPrice, stockTechnicals, true);
    }

    @Override
    public boolean isBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        MovingAverageLength length = resolveLength(trend.getMomentum());
        MovingAverageSupportResistanceService service = resolve(length, timeframe, stockTechnicals);
        return service.isBreakdown(timeframe, stockPrice, stockTechnicals, true);
    }

    @Override
    public boolean isNearResistance(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        MovingAverageLength length = resolveLength(trend.getMomentum());
        MovingAverageSupportResistanceService service = resolve(length, timeframe, stockTechnicals);
        return service.isNearResistance(timeframe, stockPrice, stockTechnicals, true);
    }

    public static MovingAverageLength resolveLength(Trend.Phase phase) {
        return switch (phase) {
            case DEEP_CORRECTION, BOTTOM -> MovingAverageLength.LOWEST;
            case CORRECTION -> MovingAverageLength.LOW;
            case PULLBACK -> MovingAverageLength.MEDIUM;
            case DIP -> MovingAverageLength.HIGH;
            default -> MovingAverageLength.HIGHEST;
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
                    case HIGHEST -> 0;
                    case HIGH -> 1;
                    case MEDIUM -> 2;
                    case LOW -> 3;
                    case LOWEST -> 4;
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
    public List<MAInteraction> findMAInteractions(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double low = Math.min(open, close);
        double high = Math.max(open, close);

        boolean checkSupport =
                (TrendDirectionUtil.findDirection(stockPrice) == Trend.Direction.DOWN);

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

        List<MAServiceEntry> sorted =
                entries.stream()
                        .sorted(
                                Comparator.comparingDouble((MAServiceEntry e) -> e.value)
                                        .reversed())
                        .toList();

        return IntStream.range(1, sorted.size())
                .filter(
                        i -> {
                            double value = sorted.get(i).value;
                            return value >= low && value <= high;
                        })
                .mapToObj(
                        i -> {
                            double value = sorted.get(i).value;
                            MovingAverageLength length =
                                    switch (i) {
                                        case 1 -> MovingAverageLength.HIGHEST;
                                        case 2 -> MovingAverageLength.HIGH;
                                        case 3 -> MovingAverageLength.MEDIUM;
                                        case 4 -> MovingAverageLength.LOW;
                                        case 5 -> MovingAverageLength.LOWEST;
                                        default -> throw new IllegalStateException(
                                                "Unexpected MA index: " + i);
                                    };
                            return MAInteraction.of(length, value, checkSupport);
                        })
                .toList();
    }

    public List<MAEvaluationResult> evaluateInteractions(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        List<MAInteraction> interactions =
                findMAInteractions(timeframe, stockPrice, stockTechnicals);

        return interactions.stream()
                .map(
                        interaction -> {
                            MovingAverageSupportResistanceService service =
                                    resolve(interaction.getLength(), timeframe, stockTechnicals);

                            boolean nearSupport = false;
                            boolean breakout = false;
                            boolean nearResistance = false;
                            boolean breakdown = false;

                            if (interaction.supportSide()) {
                                // trend DOWN -> check support & breakdown
                                breakdown =
                                        service.isBreakdown(
                                                timeframe, stockPrice, stockTechnicals, false);
                                if (!breakdown) {
                                    nearSupport =
                                            service.isNearSupport(
                                                    timeframe, stockPrice, stockTechnicals, false);
                                }

                            } else {
                                // trend UP -> check resistance & breakout
                                breakout =
                                        service.isBreakout(
                                                timeframe, stockPrice, stockTechnicals, false);
                                if (!breakout) {
                                    nearResistance =
                                            service.isNearResistance(
                                                    timeframe, stockPrice, stockTechnicals, false);
                                }
                            }

                            return new MAEvaluationResult(
                                    interaction.getLength(),
                                    service.getValue(timeframe, stockTechnicals),
                                    service.getPrevValue(timeframe, stockTechnicals),
                                    interaction.supportSide(),
                                    nearSupport,
                                    breakout,
                                    nearResistance,
                                    breakdown);
                        })
                .toList();
    }

    @Override
    public Optional<MAEvaluationResult> evaluateSingleInteractionSmart(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        List<MAEvaluationResult> results =
                evaluateInteractions(timeframe, stockPrice, stockTechnicals);

        List<MAEvaluationResult> breakouts =
                results.stream().filter(MAEvaluationResult::isBreakout).toList();
        List<MAEvaluationResult> breakdowns =
                results.stream().filter(MAEvaluationResult::isBreakdown).toList();
        List<MAEvaluationResult> supports =
                results.stream().filter(MAEvaluationResult::isNearSupport).toList();
        List<MAEvaluationResult> resistances =
                results.stream().filter(MAEvaluationResult::isNearResistance).toList();

        // 1. Breakdown + Support → Support with lower MA
        if (!breakdowns.isEmpty() && !supports.isEmpty()) {
            return supports.stream().min(Comparator.comparingInt(r -> r.getLength().getWeight()));
        }

        // 2. Breakout + Resistance → Resistance with higher MA
        if (!breakouts.isEmpty() && !resistances.isEmpty()) {
            return resistances.stream()
                    .max(Comparator.comparingInt(r -> r.getLength().getWeight()));
        }

        // 3. Breakout + Breakout → Lower MA breakout (higher weight)
        if (breakouts.size() > 1) {
            return breakouts.stream().max(Comparator.comparingInt(r -> r.getLength().getWeight()));
        }

        // 4. Breakdown + Breakdown → Higher MA breakdown (lower weight)
        if (breakdowns.size() > 1) {
            return breakdowns.stream().min(Comparator.comparingInt(r -> r.getLength().getWeight()));
        }

        // 5. Support + Support → Support with higher MA (lower weight)
        if (supports.size() > 1) {
            return supports.stream().min(Comparator.comparingInt(r -> r.getLength().getWeight()));
        }

        // 6. Resistance + Resistance → Resistance with lower MA (higher weight)
        if (resistances.size() > 1) {
            return resistances.stream()
                    .max(Comparator.comparingInt(r -> r.getLength().getWeight()));
        }

        // 7. Fallback: best individual signal by scoring
        return results.stream()
                .filter(
                        r ->
                                r.isBreakout()
                                        || r.isBreakdown()
                                        || r.isNearResistance()
                                        || r.isNearSupport())
                .max(Comparator.comparingInt(this::calculateSignalScore));
    }

    int calculateSignalScore(MAEvaluationResult result) {
        int baseScore;

        if (result.isBreakout()) {
            baseScore = 100;
            // For breakout, a breakout above a lower MA is more significant (short-term momentum)
            // Hence, use normal weight (lower MA has higher weight value)
            return baseScore + result.getLength().getWeight();
        } else if (result.isBreakdown()) {
            baseScore = 90;
            // For breakdown, breaking below a lower MA is more significant (short-term weakness)
            // Hence, use normal weight (lower MA has higher weight value)
            return baseScore + result.getLength().getWeight();
        } else if (result.isNearResistance()) {
            baseScore = 70;
            // For resistance, rejection at a higher MA is more significant (long-term barrier)
            // Hence, use reverse weight (higher MA gets higher score)
            return baseScore + result.getLength().getReverseWeight();
        } else if (result.isNearSupport()) {
            baseScore = 60;
            // For support, holding a higher MA is more significant (long-term floor)
            // Hence, use reverse weight (higher MA gets higher score)
            return baseScore + result.getLength().getReverseWeight();
        }

        return 0;
    }
}
