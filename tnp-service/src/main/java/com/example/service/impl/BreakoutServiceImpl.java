package com.example.service.impl;

import com.example.data.transactional.entities.StockPrice;
import com.example.service.BreakoutService;
import com.example.service.CrossOverUtil;
import com.example.service.utils.CandleStickUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BreakoutServiceImpl implements BreakoutService {

    @Override
    public boolean isBreakOut(StockPrice stockPrice, double average, double prevAverage) {
        return stockPrice.getPrevClose() < stockPrice.getClose()
                        && this.isRecoveryBreakout(stockPrice, average)
                || CrossOverUtil.isFastCrossesAboveSlow(
                        stockPrice.getPrevClose(), prevAverage, stockPrice.getClose(), average);
    }

    @Override
    public boolean isRecoveryBreakout(StockPrice stockPrice, double ma) {

        boolean isGapDown = CandleStickUtils.isGapDown(stockPrice);

        boolean prevSessionRed = CandleStickUtils.isPrevSessionRed(stockPrice);

        boolean condition1 = stockPrice.getOpen() < ma && stockPrice.getClose() > ma;

        boolean condition2 =
                stockPrice.getOpen() > ma
                        && stockPrice.getClose() > ma
                        && (stockPrice.getLow() < ma
                                || Math.abs(stockPrice.getLow() - ma) / ma <= 0.015)
                        && CandleStickUtils.isStrongLowerWick(stockPrice);

        boolean sessionConfirmation = isGapDown || prevSessionRed;

        return sessionConfirmation && (condition1 || condition2);
    }

    @Override
    public boolean isBreakDown(StockPrice stockPrice, double average, double prevAverage) {
        return stockPrice.getPrevClose() > stockPrice.getClose()
                        && this.isExhaustionBreakdown(stockPrice, average)
                || CrossOverUtil.isSlowCrossesBelowFast(
                        stockPrice.getPrevClose(), prevAverage, stockPrice.getClose(), average);
    }

    @Override
    public boolean isExhaustionBreakdown(StockPrice stockPrice, double ma) {
        boolean isGapUp = CandleStickUtils.isGapUp(stockPrice);
        boolean prevSessionGreen = CandleStickUtils.isPrevSessionGreen(stockPrice);

        boolean condition1 = stockPrice.getOpen() > ma && stockPrice.getClose() < ma;

        boolean condition2 =
                stockPrice.getOpen() < ma
                        && stockPrice.getClose() < ma
                        && (stockPrice.getHigh() > ma
                                || Math.abs(stockPrice.getHigh() - ma) / ma <= 0.015)
                        && CandleStickUtils.isStrongUpperWick(stockPrice);

        boolean sessionConfirmation = isGapUp || prevSessionGreen;

        return sessionConfirmation && (condition1 || condition2);
    }
}
