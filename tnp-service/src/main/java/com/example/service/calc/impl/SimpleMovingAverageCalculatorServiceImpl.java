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

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private MiscUtil miscUtil;

    @Override
    public List<Double> calculate(List<OHLCV> ohlcvList, int days) {

        List<Double> smaList = new ArrayList<>(ohlcvList.size());

        for(int i=0; i < ohlcvList.size(); i++){

            if(i < days-1){
                smaList.add(i, 0.00);
            }else if(i == days-1){
                double sma = this.calculateSimpleAverage(ohlcvList, days);
                smaList.add(i, miscUtil.formatDouble(sma,"00"));
            }else{
                double sma=  formulaService.calculateSmoothedMovingAverage(smaList.get(i-1),ohlcvList.get(i).getClose(),  days);
                smaList.add(i, miscUtil.formatDouble(sma,"00"));
            }

        }
        return smaList;
    }

    @Override
    public Double calculate(OHLCV ohlcv, double prevSMA, int days) {
        return miscUtil.formatDouble(formulaService.calculateSmoothedMovingAverage(prevSMA, ohlcv.getClose(),  days),"00");
    }

    private double calculateSimpleAverage(List<OHLCV> ohlcvList, int days){

        double sum = 0.00;

        for(int i =0; i < days; i++){
            sum = sum + ohlcvList.get(i).getClose();
        }

        return sum / days;
    }
}
