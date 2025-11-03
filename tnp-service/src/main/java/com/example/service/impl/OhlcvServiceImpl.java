package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.storage.documents.StockPrice;
import com.example.data.storage.repo.PriceTemplate;
import com.example.dto.assembler.StockPriceOHLCVAssembler;
import com.example.dto.common.OHLCV;
import com.example.service.OhlcvService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OhlcvServiceImpl implements OhlcvService {

    @Autowired private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;
    @Autowired private PriceTemplate priceTemplate;

    private static Map<String, List<OHLCV>> ohlcvsMap = new HashMap<>();

    @Override
    // @Cacheable(value = "ohlcvsFetch", key = "{#nseSymbol, #from, #to}")
    public List<OHLCV> fetch(String nseSymbol, LocalDate from, LocalDate to) {

        String key = nseSymbol + "-" + from + "-" + to;

        List<OHLCV> ohlcvList = ohlcvsMap.get(key);

        if (ohlcvList != null) {
            return ohlcvList;
        } else {
            List<StockPrice> stockPrices = priceTemplate.get(nseSymbol, from, to);

            if (stockPrices != null && !stockPrices.isEmpty()) {
                ohlcvList = stockPriceOHLCVAssembler.toModel(stockPrices);
                ohlcvsMap.put(key, ohlcvList);
                return ohlcvList;
            }

            return new ArrayList<>();
        }
        // return new ArrayList<>();
    }

    @Override
    // @Cacheable(value = "ohlcvsFetch", key = "{#timeframe, #nseSymbol, #from, #to}")
    public List<OHLCV> fetch(Timeframe timeframe, String nseSymbol, LocalDate from, LocalDate to) {

        String key = timeframe + "-" + nseSymbol + "-" + from + "-" + to;

        List<OHLCV> ohlcvList = ohlcvsMap.get(key);

        if (ohlcvList != null) {
            return ohlcvList;
        } else {
            List<StockPrice> stockPrices = priceTemplate.get(timeframe, nseSymbol, from, to);

            if (stockPrices != null && !stockPrices.isEmpty()) {
                ohlcvList = stockPriceOHLCVAssembler.toModel(stockPrices);
                ohlcvsMap.put(key, ohlcvList);
                return ohlcvList;
            }
            return new ArrayList<>();
        }

        // return new ArrayList<>();
    }
}
