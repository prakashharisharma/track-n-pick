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

    private final StockPriceService stockPriceService;
    private final FormulaService formulaService; // For 1% calculation
    private final EvaluationLogService evaluationLogService;
    public boolean isOutsideHigherTimeframeResistanceZone(StockPrice stockPrice) {
        Timeframe current = stockPrice.getTimeframe();
        Stock stock = stockPrice.getStock();
        double close = stockPrice.getClose();

        Timeframe higher = current.getHigher();
        while (higher != current) {
            StockPrice higherTfPrice = stockPriceService.get(stock, higher);
            if (higherTfPrice == null) break;

            SupportResistanceZones zones = SupportResistanceZoneUtils.calculateSupportResistanceZones(higherTfPrice);
            SupportResistanceZoneUtils.Zone resistance = zones.getResistance();

            double resistanceStart = resistance.getStart();
            double resistanceEnd = resistance.getEnd();

            double onePercentBelowStart = resistanceStart - formulaService.calculateFraction(resistanceStart, 1.0);

            boolean isOutside = close < onePercentBelowStart || close > resistanceEnd;


            log.debug(
                    "{}: Close={} | Resistance Start={} End={} | 1% Below Start={} | OutsideZone={}",
                    higher.name(), close, resistanceStart, resistanceEnd, onePercentBelowStart, isOutside
            );

            if (!isOutside){
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        StringUtils.format(
                                "{} Timeframe resistance alert Close={} | Resistance Start={} End={} | 1% Below Start={} | OutsideZone={}",
                                higher.name(), close, resistanceStart, resistanceEnd, onePercentBelowStart, isOutside));
                return false;
            }// still within resistance zone

            Timeframe nextHigher = higher.getHigher();
            if (nextHigher == higher) break; // Reached top
            higher = nextHigher;
        }

        return true;
    }
}

