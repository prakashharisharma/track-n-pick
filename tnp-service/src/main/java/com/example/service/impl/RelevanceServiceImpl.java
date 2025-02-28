package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.*;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RelevanceServiceImpl implements RelevanceService {

    @Autowired
    private TimeframeSupportResistanceService timeframeSupportResistanceService;
    @Autowired
    private MovingAverageSupportResistanceService movingAverageSupportResistanceService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;

    @Autowired
    private ObvIndicatorService obvIndicatorService;

    @Autowired
    private VolumeService volumeService;

    @Autowired
    private TrendService trendService;

    @Override
    public boolean isBullish(Stock stock, Trend trend, Momentum momentum, double minimumScore) {

        double score = 0.0;


        if (timeframeSupportResistanceService.isBullish(stock, trend)) {
            log.info("Time frame support / breakout {} momentum {} bullish {}", trend, momentum, stock.getNseSymbol());
            score = score + 1.0;
        }

        if (movingAverageSupportResistanceService.isBullish(stock, trend)) {
            log.info("Moving average support / breakout {} momentum {} bullish {}",trend, momentum, stock.getNseSymbol());
            score = score + 1.0;
        }

        if(rsiIndicatorService.isOverSold(stock)){
            log.info("RSI oversold {}", stock.getNseSymbol());
            score = score + 0.5;
        }
        if(volumeService.isVolumeIncreasedDailyMonthly(stock, this.calculateBullishVolumeFactorUsingRsi(rsiIndicatorService.rsi(stock)), this.calculateBullishVolumeFactorUsingRsi(rsiIndicatorService.rsiPreviousSession(stock)))){
            log.info("High volume {}", stock.getNseSymbol());
            score = score + 0.5;
        }
        else if(volumeService.isVolumeIncreasedWeeklyMonthly(stock, this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsi(stock)), this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsiPreviousSession(stock)))){
            log.info("High volume {}", stock.getNseSymbol());
            score = score + 0.5;
        }else if(obvIndicatorService.isBullish(stock)){
            log.info("OBV above average {}", stock.getNseSymbol());
            score = score + 0.5;
        }

        log.info("Bullish relevance score: {} trend: {} momentum {} symbol: {}", score, trend,momentum, stock.getNseSymbol());

        return score >= minimumScore;
    }

    @Override
    public boolean isBearish(Stock stock,Trend trend,Momentum momentum, double minimumScore) {

        double score = 0.0;


        if (timeframeSupportResistanceService.isBearish(stock, trend)) {
            log.info("Time frame resistance / breakdown {} momentum {} bearish {}", trend, momentum, stock.getNseSymbol());
            score = score + 1.0;
        }


        if (movingAverageSupportResistanceService.isBearish(stock, trend)) {
            log.info("Moving average resistance / breakdown {} momentum {} bullish {}",trend, momentum, stock.getNseSymbol());
            score = score + 1.0;
        }

        if(rsiIndicatorService.isOverBaught(stock)){
            log.info("RSI over bought {}", stock.getNseSymbol());
            score = score + 0.5;
        }

        if(volumeService.isVolumeIncreasedDailyMonthly(stock, this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsi(stock)), this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsiPreviousSession(stock)))){
            log.info("High volume {}", stock.getNseSymbol());
            score = score + 0.5;
        }else if(volumeService.isVolumeIncreasedWeeklyMonthly(stock, this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsi(stock)), this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsiPreviousSession(stock)))){
            score = score + 0.5;
        }else if(obvIndicatorService.isBearish(stock)){
            log.info("OBV below average {}", stock.getNseSymbol());
            score = score + 0.5;
        }
        log.info("Bearish Relevance score: {} trend: {} momentum {} symbol: {}", score, trend,momentum, stock.getNseSymbol());
        return score >= minimumScore;
    }

    public  double calculateBullishVolumeFactorUsingRsi(double rsi) {
        double baseValue = 2.5;
        double overSold = 30.0;
        if (rsi == overSold) return baseValue;
        if (rsi < 20.0) return 1.0;

        if (rsi > overSold) {
            return baseValue + (rsi - overSold) * 0.2;
        } else {
            return baseValue - (overSold - rsi) * 0.1;
        }
    }

    public double calculateBearishVolumeFactorUsingRsi(double rsi) {
        double baseValue = 2.0;
        double overBought = 70.0;
        if (rsi == overBought) return baseValue; // Base value at 70
        if (rsi > 80 ) return 1.0;

        if (rsi > overBought) {
            return baseValue - (rsi - overBought) * 0.1; // Decrease by 0.50 per 5 units above 70
        } else {
            return baseValue + (overBought - rsi) * 0.2; // Increase by 1.0 per 5 units below 70
        }
    }
}
