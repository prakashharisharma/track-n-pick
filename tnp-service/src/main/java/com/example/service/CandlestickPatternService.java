package com.example.service;

import com.example.data.transactional.entities.CandlestickPattern;
import com.example.data.transactional.entities.StockPrice;
import java.util.List;

public interface CandlestickPatternService {
    List<CandlestickPattern> findLast3Within5DaysBefore(StockPrice stockPrice);

    CandlestickPattern create(CandlestickPattern pattern);
}
