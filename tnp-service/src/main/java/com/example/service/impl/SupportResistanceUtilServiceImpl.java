package com.example.service.impl;

import com.example.transactional.model.master.Stock;
import com.example.service.CandleStickService;
import com.example.service.SupportResistanceUtilService;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupportResistanceUtilServiceImpl implements SupportResistanceUtilService {

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private FormulaService formulaService;


    @Override
    public boolean isNearSupport(double open,double high, double low, double close, double average) {
        if (close > average) {
            if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(low, average, FibonacciRatio.RATIO_161_8 * 10)) {
                log.info("Support low: {} close {} above avg: {}",low,  close, average);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }



    private boolean isLowNearAverage(Stock stock, double open, double high, double low, double close, double average, double immediateLowAverage, boolean isLongEma ){

        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * -100) >= average) {
            if (close >= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(low, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                } else if (isLongEma && formulaService.calculateChangePercentage(average, low) <= -1 * FibonacciRatio.RATIO_161_8 * 100) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isLowNearAverage(double currClose, double open,double high, double low, double close, double average,double immediateLowAverage, boolean isLongEma ){

        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * -100) >= average) {
            if (currClose >= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(low, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                } else if (isLongEma && formulaService.calculateChangePercentage(average, low) <= -1 * FibonacciRatio.RATIO_161_8 * 100) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }


    @Override
    public boolean isNearResistance(double open, double high, double low, double close, double average) {
        if (close < average) {
            if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(high, average, FibonacciRatio.RATIO_161_8 * 10)) {
                log.info("Resistance high: {} close: {} below avg: {}", high, close, average);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isHighNearAverage(Stock stock, double open,double high, double low, double close, double average,double immediateLowAverage, boolean isLongEma ){
        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * 100) <= average) {
            if (close <= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(high, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                }
            } else if (!isLongEma && formulaService.calculateChangePercentage(average, high) >= FibonacciRatio.RATIO_161_8 * 100) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isHighNearAverage(double currClose, double open,double high, double low, double close, double average,double immediateLowAverage, boolean isLongEma ){
        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * 100) <= average) {
            if (currClose <= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(high, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                }
            } else if (!isLongEma && formulaService.calculateChangePercentage(average, high) >= FibonacciRatio.RATIO_161_8 * 100) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
