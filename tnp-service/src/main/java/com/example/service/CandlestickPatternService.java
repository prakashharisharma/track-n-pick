package com.example.service;

import com.example.data.transactional.entities.CandlestickPattern;
import com.example.data.transactional.entities.StockPrice;
import java.util.List;
import java.util.Optional;

public interface CandlestickPatternService {

    List<CandlestickPattern> findLast3Within5DaysBefore(StockPrice stockPrice);

    boolean hasAtLeastTwoPatternsWithSentiment(StockPrice stockPrice, CandlestickPattern.Sentiment sentiment);

    Optional<CandlestickPattern> findCurrentPattern(StockPrice stockPrice);

    CandlestickPattern create(CandlestickPattern pattern);
}
