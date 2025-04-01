package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.dto.OHLCV;
import com.example.service.OhlcvService;
import com.example.data.storage.documents.StockPrice;
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
public class OhlcvServiceImpl implements OhlcvService {


    @Autowired
    private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;
    @Autowired
    private PriceTemplate priceTemplate;

    @Override
    public List<OHLCV> fetch(String nseSymbol, LocalDate from, LocalDate to) {

        List<StockPrice>  stockPrices = priceTemplate.get(nseSymbol, from, to);

        if(stockPrices!=null && !stockPrices.isEmpty()){
            return stockPriceOHLCVAssembler.toModel(stockPrices);
        }

        return new ArrayList<>();
    }

    @Override
    public List<OHLCV> fetch(Timeframe timeframe, String nseSymbol, LocalDate from, LocalDate to) {

        List<StockPrice>  stockPrices = priceTemplate.get(timeframe, nseSymbol, from, to);

        if(stockPrices!=null && !stockPrices.isEmpty()){
            return stockPriceOHLCVAssembler.toModel(stockPrices);
        }

        return new ArrayList<>();
    }
}
