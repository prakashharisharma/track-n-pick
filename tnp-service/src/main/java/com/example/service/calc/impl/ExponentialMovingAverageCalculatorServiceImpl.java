package com.example.service.calc.impl;

import com.example.service.calc.ExponentialMovingAverageCalculatorService;
import com.example.storage.model.OHLCV;
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

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private MiscUtil miscUtil;

    @Override
    public List<Double> calculate(List<OHLCV> ohlcvList, int days){

        long startTime = System.currentTimeMillis();

        List<Double> emaList = new ArrayList<>(ohlcvList.size());

        for(int i=0; i < ohlcvList.size(); i++){

            if(i < days-1){
                emaList.add(i, 0.00);
            }else if(i == days-1){
                double sma = this.calculateSimpleAverage(ohlcvList, days);
                emaList.add(i, miscUtil.formatDouble(sma,"00"));
            }else{
                double ema=  formulaService.calculateEMA(ohlcvList.get(i).getClose(), emaList.get(i-1), days);
                emaList.add(i, miscUtil.formatDouble(ema,"00"));
            }

        }

        long endTime = System.currentTimeMillis();

        log.info("Time took to calculate EMA {}ms",(endTime - startTime));

        return emaList;
    }

    @Override
    public Double calculate(OHLCV ohlcv, double prevEMA, int days) {
        return miscUtil.formatDouble(formulaService.calculateEMA(ohlcv.getClose(), prevEMA, days),"00");
    }

    private double calculateSimpleAverage(List<OHLCV> ohlcvList, int days){

        double sum = 0.00;

        for(int i =0; i < days; i++){
            sum = sum + ohlcvList.get(i).getClose();
        }

        return sum / days;
    }
}
