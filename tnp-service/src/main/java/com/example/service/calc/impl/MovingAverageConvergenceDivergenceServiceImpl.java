package com.example.service.calc.impl;

import com.example.dto.OHLCV;
import com.example.service.calc.ExponentialMovingAverageCalculatorService;
import com.example.service.calc.MovingAverageConvergenceDivergenceService;
import com.example.storage.model.MovingAverageConvergenceDivergence;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MovingAverageConvergenceDivergenceServiceImpl implements MovingAverageConvergenceDivergenceService {

    private static final int longPeriod = 26;
    private static final int shortPeriod = 12;
    private static final int signalPeriod = 9;

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private MiscUtil miscUtil;

    @Autowired
    private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

    @Override
    public List<MovingAverageConvergenceDivergence> calculate(List<OHLCV> ohlcvList) {

        //long startTime = System.currentTimeMillis();
        List<Double> ema12List = new ArrayList<>(ohlcvList.size());
        List<Double> ema26List = new ArrayList<>(ohlcvList.size());
        List<Double> macdList = new ArrayList<>(ohlcvList.size());
        List<Double> signalList = new ArrayList<>(ohlcvList.size());

        ema12List = exponentialMovingAverageService.calculate(ohlcvList, shortPeriod);
        ema26List = exponentialMovingAverageService.calculate(ohlcvList, longPeriod);

        for(int i=0; i < ohlcvList.size(); i++){

            if(i < longPeriod - 1){
                macdList.add(0.00);

            }
            else {
                macdList.add(ema12List.get(i) - ema26List.get(i));
            }

            signalList.add(0.00);
            if(i == longPeriod + signalPeriod - 2 ){
                double sma = this.calculateSimpleAverage(macdList, longPeriod, signalPeriod);
                signalList.add(i, sma);
            }

            if (i > longPeriod + signalPeriod - 2 ){
                double ema=  formulaService.calculateEMA(macdList.get(i), signalList.get(i-1), signalPeriod);
                signalList.add(i, ema);
            }
        }
        //long endTime = System.currentTimeMillis();

        //log.info("Time took to calculate MACD {}ms",(endTime - startTime));
        return this.mapResult(ema12List, ema26List,macdList, signalList);
    }

    @Override
    public MovingAverageConvergenceDivergence calculate(OHLCV ohlcv, MovingAverageConvergenceDivergence prevMovingAverageConvergenceDivergence) {

        double ema12 = exponentialMovingAverageService.calculate(ohlcv, prevMovingAverageConvergenceDivergence.getEma12(), shortPeriod);
        double ema26 = exponentialMovingAverageService.calculate(ohlcv, prevMovingAverageConvergenceDivergence.getEma26(), longPeriod);
        double macd = ema12 - ema26;
        double signal = formulaService.calculateEMA(macd, prevMovingAverageConvergenceDivergence.getSignal(), signalPeriod);

        return new MovingAverageConvergenceDivergence(ema12, ema26, miscUtil.formatDouble(macd,"00"), miscUtil.formatDouble(signal,"00"));
    }

    private List<MovingAverageConvergenceDivergence> mapResult(List<Double> ema12List, List<Double> ema26List, List<Double> macdList, List<Double> signalList){
        List<MovingAverageConvergenceDivergence> movingAverageConvergenceDivergenceList = new ArrayList<>();
        for(int i = 0; i < macdList.size(); i++){
            movingAverageConvergenceDivergenceList.add(new MovingAverageConvergenceDivergence(ema12List.get(i), ema26List.get(i), miscUtil.formatDouble(macdList.get(i),"00"), miscUtil.formatDouble(signalList.get(i),"00")));
        }
        return movingAverageConvergenceDivergenceList;
    }

    private double calculateSimpleAverage(List<Double> macdList, int longPeriod,  int signalPeriod){

        double sum = 0.00;

        for(int i = longPeriod-1; i < longPeriod+signalPeriod-1; i++){
            sum = sum + macdList.get(i);
        }

        return sum / signalPeriod;
    }
}
