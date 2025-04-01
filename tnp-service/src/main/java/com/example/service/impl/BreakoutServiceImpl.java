package com.example.service.impl;

import com.example.service.BreakoutService;
import com.example.service.CrossOverUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BreakoutServiceImpl implements BreakoutService {

    @Override
    public boolean isBreakOut(double prevClose, double prevAverage, double close, double average) {
        return CrossOverUtil.isFastCrossesAboveSlow(prevClose, prevAverage, close, average);
    }

    @Override
    public boolean isBreakDown(double prevClose, double prevAverage, double close, double average) {
        return CrossOverUtil.isSlowCrossesBelowFast(prevClose, prevAverage, close, average);
    }
}
