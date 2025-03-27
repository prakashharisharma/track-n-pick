package com.example.service.calc.impl;

import com.example.dto.OHLCV;
import com.example.service.calc.SimpleMovingAverageCalculatorService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SimpleMovingAverageCalculatorServiceImpl implements SimpleMovingAverageCalculatorService {

    @Override
    public List<Double> calculate(List<OHLCV> ohlcvList, int period) {
        if (ohlcvList == null || ohlcvList.isEmpty() || period <= 0) {
            throw new IllegalArgumentException("Invalid input data or period.");
        }

        List<Double> smaList = new ArrayList<>();

        // Initialize first (period - 1) elements as 0.0
        for (int i = 0; i < period - 1; i++) {
            smaList.add(0.0);
        }

        // Compute SMA values
        for (int i = period - 1; i < ohlcvList.size(); i++) {
            double sum = 0.0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += ohlcvList.get(j).getClose();
            }
            double sma = sum / period;
            smaList.add(sma);
        }

        return smaList;
    }
}
