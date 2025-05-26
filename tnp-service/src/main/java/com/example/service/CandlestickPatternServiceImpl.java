package com.example.service;

import com.example.data.transactional.entities.CandlestickPattern;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.repo.CandlestickPatternRepository;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CandlestickPatternServiceImpl implements CandlestickPatternService {

    private final CandlestickPatternRepository repository;

    @Override
    public List<CandlestickPattern> findLast3Within5DaysBefore(StockPrice stockPrice) {
        List<CandlestickPattern> patterns = repository.findLast3Within5DaysBefore(stockPrice);
        return patterns.size() > 3 ? patterns.subList(0, 3) : patterns;
    }

    @Override
    public boolean hasAtLeastTwoPatternsWithSentiment(StockPrice stockPrice, CandlestickPattern.Sentiment sentiment) {
        List<CandlestickPattern> recentPatterns = findLast3Within5DaysBefore(stockPrice);
        long matchCount = recentPatterns.stream()
                .filter(p -> p.getSentiment() == sentiment)
                .count();
        return matchCount >= 2;
    }

    @Override
    public Optional<CandlestickPattern> findCurrentPattern(StockPrice stockPrice) {
        return repository.findByStockPriceAndSessionDate(stockPrice, stockPrice.getSessionDate());
    }

    @Override
    @Transactional
    public CandlestickPattern create(CandlestickPattern pattern) {
        boolean exists = repository.existsByStockPriceAndNameAndSessionDate(pattern.getStockPrice(), pattern.getName(), pattern.getSessionDate());
        if (exists) {
            log.info("Pattern {} already exists for {}", pattern.getName(), pattern.getSessionDate());
            return null; // or throw exception if desired
        }
        return repository.save(pattern);
    }

}
