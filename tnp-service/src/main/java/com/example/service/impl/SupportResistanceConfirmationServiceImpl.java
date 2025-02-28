package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.SupportResistanceConfirmationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupportResistanceConfirmationServiceImpl implements SupportResistanceConfirmationService {

    @Override
    public boolean isSupportConfirmed(Stock stock, double average) {

        if(stock.getStockPrice().getClose() >= average){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isResistanceConfirmed(Stock stock, double average) {

        if(stock.getStockPrice().getClose() <= average){
            log.info("close {} is less than average {}", stock.getStockPrice().getClose(), average);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
