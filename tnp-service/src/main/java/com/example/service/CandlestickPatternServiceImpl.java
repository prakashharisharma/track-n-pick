package com.example.service;

import com.example.data.transactional.entities.CandlestickPattern;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.repo.CandlestickPatternRepository;
import java.util.List;
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
    @Transactional
    public CandlestickPattern create(CandlestickPattern pattern) {
        return repository.save(pattern);
    }
}
