package com.example.service.calc.impl;

import com.example.dto.OHLCV;
import com.example.service.calc.ExponentialMovingAverageCalculatorService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExponentialMovingAverageCalculatorServiceImpl implements ExponentialMovingAverageCalculatorService {

    @Override
    public List<Double> calculate(List<OHLCV> ohlcvList, int period) {
        if (ohlcvList == null || ohlcvList.isEmpty() || period <= 0) {
            throw new IllegalArgumentException("Invalid input data or period.");
        }

        List<Double> emaList = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        double ema = 0.0;

        // If the list size is smaller than the period, return a list of zeros
        if (ohlcvList.size() < period) {
            for (int i = 0; i < ohlcvList.size(); i++) {
                emaList.add(0.0);
            }
            return emaList;
        }

        // Initialize first (period - 1) elements as 0.0
        for (int i = 0; i < period - 1; i++) {
            emaList.add(0.0);
        }

        // Compute initial SMA for the first EMA value
        double sum = 0.0;
        for (int i = 0; i < period; i++) {
            sum += ohlcvList.get(i).getClose();
        }
        ema = sum / period;
        emaList.add(ema);

        // Calculate EMA for the remaining data points
        for (int i = period; i < ohlcvList.size(); i++) {
            double closePrice = ohlcvList.get(i).getClose();
            ema = ((closePrice - ema) * multiplier) + ema;
            emaList.add(ema);
        }

        return emaList;
    }
}