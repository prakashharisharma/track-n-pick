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
        return CrossOverUtil.isFastCrossesAboveSlow(
                        stockPrice.getPrevClose(), prevAverage, stockPrice.getClose(), average);
    }

    @Override
    public boolean isBreakDown(StockPrice stockPrice, double average, double prevAverage) {
        return CrossOverUtil.isSlowCrossesBelowFast(
                        stockPrice.getPrevClose(), prevAverage, stockPrice.getClose(), average);
    }

}
