package com.example.service;

import com.example.dto.OHLCV;

import java.util.List;

public interface OHLCVAggregatorService {

    public List<OHLCV> aggregateToWeekly(List<OHLCV> dailyOHLCV) ;

    public List<OHLCV> aggregateToMonthly(List<OHLCV> dailyOHLCV);

    public List<OHLCV> aggregateToQuarterly(List<OHLCV> dailyOHLCV);

    public List<OHLCV> aggregateToYearly(List<OHLCV> dailyOHLCV);
}
