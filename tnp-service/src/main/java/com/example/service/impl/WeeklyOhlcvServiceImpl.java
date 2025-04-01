package com.example.service.impl;

import com.example.dto.OHLCV;
import com.example.service.WeeklyOhlcvService;
import com.example.data.storage.documents.WeeklyStockPrice;
import com.example.data.storage.documents.assembler.StockPriceOHLCVAssembler;
import com.example.data.storage.repo.PriceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WeeklyOhlcvServiceImpl implements WeeklyOhlcvService {

    @Autowired
    private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;
    @Autowired
    private PriceTemplate priceTemplate;

    @Override
    public List<OHLCV> fetch(String nseSymbol, LocalDate from, LocalDate to) {

        List<WeeklyStockPrice>  stockPrices = priceTemplate.getWeekly(nseSymbol, from, to);

        if(stockPrices!=null && !stockPrices.isEmpty()){
            return stockPriceOHLCVAssembler.toWeeklyModel(stockPrices);
        }

        return new ArrayList<>();
    }
}
