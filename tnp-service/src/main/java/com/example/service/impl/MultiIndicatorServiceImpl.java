package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.AdxIndicatorService;
import com.example.service.MacdIndicatorService;
import com.example.service.MultiIndicatorService;
import com.example.service.RsiIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MultiIndicatorServiceImpl implements MultiIndicatorService {

    @Autowired
    private MacdIndicatorService macdIndicatorService;

    @Autowired
    private AdxIndicatorService adxIndicatorService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;

    @Override
    public boolean isBullish(Stock stock) {
        if(rsiIndicatorService.isIncreasing(stock)){
            if(macdIndicatorService.isHistogramIncreased(stock)){
                if(adxIndicatorService.isBullish(stock)){
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBearish(Stock stock) {
        if(rsiIndicatorService.isDecreasing(stock)){
            if(macdIndicatorService.isHistogramDecreased(stock)){
                if(adxIndicatorService.isBearish(stock)){
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
}
