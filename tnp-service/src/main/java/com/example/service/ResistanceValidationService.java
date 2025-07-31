package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.EvaluationLog;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.service.utils.SupportResistanceZoneUtils;
import com.example.util.FormulaService;
import com.example.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResistanceValidationService {
    private static final double DEFAULT_MARGIN_PERCENT = 0.5;

    private final StockPriceService stockPriceService;
    private final FormulaService formulaService; // For 1% calculation
    private final EvaluationLogService evaluationLogService;

    public boolean isOutsideResistanceZone(StockPrice stockPrice) {
        Timeframe current = stockPrice.getTimeframe();
        Stock stock = stockPrice.getStock();
        double high = stockPrice.getHigh();
        double close = stockPrice.getClose();

        // Check resistance zone in the current timeframe first
        SupportResistanceZones currentZones =
                SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);
        SupportResistanceZoneUtils.Zone currentResistance = currentZones.getResistance();

        double currentResistanceStart = currentResistance.getStart();
        double currentResistanceEnd = currentResistance.getEnd();
        double onePercentBelowStart =
                currentResistanceStart
                        - formulaService.calculateFraction(currentResistanceStart, 1.0);

        boolean isInsideCurrent =
                isInsideResistanceZone(high, close, currentResistance, DEFAULT_MARGIN_PERCENT);

        if (isInsideCurrent) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} Timeframe resistance alert (current) Close={} | Resistance Start={}"
                                    + " End={} | 1% Below Start={}",
                            current.name(),
                            close,
                            currentResistanceStart,
                            currentResistanceEnd,
                            onePercentBelowStart));
            return false;
        }

        evaluationLogService.add(
                stockPrice,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format("Passing {} Timeframe resistance (current)", current.name()));

        // Now check higher timeframes
        Timeframe higher = current.getHigher();
        while (higher != current) {
            StockPrice higherTfPrice = stockPriceService.get(stock, higher);
            if (higherTfPrice == null) break;

            SupportResistanceZones zones =
                    SupportResistanceZoneUtils.calculateSupportResistanceZones(higherTfPrice);
            SupportResistanceZoneUtils.Zone resistance = zones.getResistance();

            double resistanceStart = resistance.getStart();
            double resistanceEnd = resistance.getEnd();
            double onePercentBelow =
                    resistanceStart - formulaService.calculateFraction(resistanceStart, 1.0);

            isInsideCurrent =
                    isInsideResistanceZone(high, close, resistance, DEFAULT_MARGIN_PERCENT);

            if (isInsideCurrent) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        StringUtils.format(
                                "{} Timeframe resistance alert Close={} | Resistance Start={}"
                                        + " End={} | 1% Below Start={}",
                                higher.name(),
                                close,
                                resistanceStart,
                                resistanceEnd,
                                onePercentBelow));
                return false;
            }

            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format("Passing {} Timeframe resistance", higher.name()));

            Timeframe nextHigher = higher.getHigher();
            if (nextHigher == higher) break;
            higher = nextHigher;
        }

        return true;
    }

    public boolean isOutsideSupportZone(StockPrice stockPrice) {
        Timeframe current = stockPrice.getTimeframe();
        Stock stock = stockPrice.getStock();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();

        // Check support zone in current timeframe
        SupportResistanceZones currentZones =
                SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);
        SupportResistanceZoneUtils.Zone currentSupport = currentZones.getSupport();

        double supportStart = currentSupport.getStart();
        double supportEnd = currentSupport.getEnd();
        double onePercentAboveEnd = supportEnd + formulaService.calculateFraction(supportEnd, 1.0);

        boolean isInsideCurrent =
                isInsideSupportZone(low, close, currentSupport, DEFAULT_MARGIN_PERCENT);

        if (isInsideCurrent) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} Timeframe support alert (current) Close={} | Support Start={}"
                                    + " End={} | 1% Above End={}",
                            current.name(), close, supportStart, supportEnd, onePercentAboveEnd));
            return false;
        }

        evaluationLogService.add(
                stockPrice,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format("Passing {} Timeframe support (current)", current.name()));

        // Check higher timeframes
        Timeframe higher = current.getHigher();
        while (higher != current) {
            StockPrice higherTfPrice = stockPriceService.get(stock, higher);
            if (higherTfPrice == null) break;

            SupportResistanceZones zones =
                    SupportResistanceZoneUtils.calculateSupportResistanceZones(higherTfPrice);
            SupportResistanceZoneUtils.Zone support = zones.getSupport();

            double supportStartHigher = support.getStart();
            double supportEndHigher = support.getEnd();
            double onePercentAbove =
                    supportEndHigher + formulaService.calculateFraction(supportEndHigher, 1.0);

            isInsideCurrent = isInsideSupportZone(low, close, support, DEFAULT_MARGIN_PERCENT);

            if (isInsideCurrent) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        StringUtils.format(
                                "{} Timeframe support alert Close={} | Support Start={} End={} | 1%"
                                        + " Above End={}",
                                higher.name(),
                                close,
                                supportStartHigher,
                                supportEndHigher,
                                onePercentAbove));
                return false;
            }

            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format("Passing {} Timeframe support", higher.name()));

            Timeframe nextHigher = higher.getHigher();
            if (nextHigher == higher) break;
            higher = nextHigher;
        }

        return true;
    }

    public boolean isInsideResistanceZone(StockPrice stockPrice) {
        Timeframe current = stockPrice.getTimeframe();
        Stock stock = stockPrice.getStock();
        double high = stockPrice.getHigh();
        double close = stockPrice.getClose();

        // Check resistance zone in the current timeframe first
        SupportResistanceZones currentZones =
                SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);
        SupportResistanceZoneUtils.Zone currentResistance = currentZones.getResistance();

        double start = currentResistance.getStart();
        double end = currentResistance.getEnd();

        boolean isInsideCurrent =
                isInsideResistanceZone(high, close, currentResistance, DEFAULT_MARGIN_PERCENT);

        if (isInsideCurrent) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} Timeframe resistance zone hit: Close={} | Resistance Start={}"
                                    + " End={}",
                            current.name(),
                            close,
                            start,
                            end));
            return true;
        }

        // Check higher timeframes
        Timeframe higher = current.getHigher();
        while (higher != current) {
            StockPrice higherTfPrice = stockPriceService.get(stock, higher);
            if (higherTfPrice == null) break;

            SupportResistanceZones zones =
                    SupportResistanceZoneUtils.calculateSupportResistanceZones(higherTfPrice);
            SupportResistanceZoneUtils.Zone resistance = zones.getResistance();

            double resStart = resistance.getStart();
            double resEnd = resistance.getEnd();
            isInsideCurrent =
                    isInsideResistanceZone(high, close, resistance, DEFAULT_MARGIN_PERCENT);

            if (isInsideCurrent) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        StringUtils.format(
                                "{} Timeframe resistance zone hit: Close={} | Resistance Start={}"
                                        + " End={}",
                                higher.name(),
                                close,
                                resStart,
                                resEnd));
                return true;
            }

            Timeframe nextHigher = higher.getHigher();
            if (nextHigher == higher) break;
            higher = nextHigher;
        }

        return false;
    }

    public boolean isInsideSupportZone(StockPrice stockPrice) {
        Timeframe current = stockPrice.getTimeframe();
        Stock stock = stockPrice.getStock();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();

        // Check current timeframe support
        SupportResistanceZones currentZones =
                SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);
        SupportResistanceZoneUtils.Zone currentSupport = currentZones.getSupport();

        double start = currentSupport.getStart();
        double end = currentSupport.getEnd();
        boolean isInsideCurrent =
                isInsideSupportZone(low, close, currentSupport, DEFAULT_MARGIN_PERCENT);

        if (isInsideCurrent) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "{} Timeframe support zone hit: Close={} | Support Start={} End={}",
                            current.name(),
                            close,
                            start,
                            end));
            return true;
        }

        // Check higher timeframes
        Timeframe higher = current.getHigher();
        while (higher != current) {
            StockPrice higherTfPrice = stockPriceService.get(stock, higher);
            if (higherTfPrice == null) break;

            SupportResistanceZones zones =
                    SupportResistanceZoneUtils.calculateSupportResistanceZones(higherTfPrice);
            SupportResistanceZoneUtils.Zone support = zones.getSupport();

            double supStart = support.getStart();
            double supEnd = support.getEnd();
            isInsideCurrent = isInsideSupportZone(low, close, support, DEFAULT_MARGIN_PERCENT);

            if (isInsideCurrent) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        StringUtils.format(
                                "{} Timeframe support zone hit: Close={} | Support Start={} End={}",
                                higher.name(),
                                close,
                                supStart,
                                supEnd));
                return true;
            }

            Timeframe nextHigher = higher.getHigher();
            if (nextHigher == higher) break;
            higher = nextHigher;
        }

        return false;
    }

    public static boolean isInsideResistanceZone(
            double high,
            double close,
            SupportResistanceZoneUtils.Zone resistanceZone,
            double marginPercent) {
        double start = resistanceZone.getStart();
        double end = resistanceZone.getEnd();

        double margin = (end - start) * (marginPercent / 100.0);
        double bufferedStart = start - margin;
        double bufferedEnd = end + margin;

        return (high >= bufferedStart && high <= bufferedEnd)
                || (close >= bufferedStart && close <= bufferedEnd);
    }

    public static boolean isInsideSupportZone(
            double low,
            double close,
            SupportResistanceZoneUtils.Zone supportZone,
            double marginPercent) {
        double start = supportZone.getStart();
        double end = supportZone.getEnd();

        double margin = (end - start) * (marginPercent / 100.0);
        double bufferedStart = start - margin;
        double bufferedEnd = end + margin;

        return (low >= bufferedStart && low <= bufferedEnd)
                || (close >= bufferedStart && close <= bufferedEnd);
    }
}
