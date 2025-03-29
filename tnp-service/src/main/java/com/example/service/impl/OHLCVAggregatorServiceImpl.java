package com.example.service.impl;

import com.example.dto.OHLCV;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.time.*;
import java.util.*;

import com.example.service.OHLCVAggregatorService;
import com.example.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OHLCVAggregatorServiceImpl implements OHLCVAggregatorService {

    private final CalendarService calendarService;


    private static Instant getStartOfWeek(Instant instant) {
        return instant.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    private static Instant getStartOfMonth(Instant instant) {
        return instant.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .withDayOfMonth(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    private static Instant getStartOfQuarter(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        int quarterStartMonth = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), quarterStartMonth, 1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    private static Instant getStartOfYear(Instant instant) {
        return instant.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .withDayOfYear(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    @Override
    public List<OHLCV> aggregateToWeekly(List<OHLCV> dailyOHLCV) {
        return aggregate(dailyOHLCV, OHLCVAggregatorServiceImpl::getStartOfWeek, calendarService::isLastTradingSessionOfWeek);
    }

    @Override
    public List<OHLCV> aggregateToMonthly(List<OHLCV> dailyOHLCV) {
        return aggregate(dailyOHLCV, OHLCVAggregatorServiceImpl::getStartOfMonth, calendarService::isLastTradingSessionOfMonth);
    }
    @Override
    public List<OHLCV> aggregateToQuarterly(List<OHLCV> dailyOHLCV) {
        return aggregate(dailyOHLCV, OHLCVAggregatorServiceImpl::getStartOfQuarter, calendarService::isLastTradingSessionOfQuarter);
    }
    @Override
    public List<OHLCV> aggregateToYearly(List<OHLCV> dailyOHLCV) {
        return aggregate(dailyOHLCV, OHLCVAggregatorServiceImpl::getStartOfYear, calendarService::isLastTradingSessionOfYear);
    }

    private List<OHLCV> aggregate(List<OHLCV> dailyOHLCV, TimeframeAggregator aggregator, Predicate<LocalDate> isLastTradingSession) {
        Map<Instant, List<OHLCV>> grouped = dailyOHLCV.stream()
                .filter(ohlcv -> isLastTradingSession.test(ohlcv.getBhavDate().atZone(ZoneOffset.UTC).toLocalDate())) // Ensures only prior completed periods
                .collect(Collectors.groupingBy(ohlcv -> aggregator.getStart(ohlcv.getBhavDate())));

        return grouped.entrySet().stream()
                .map(entry -> aggregateOHLC(entry.getValue()))
                .sorted(Comparator.comparing(OHLCV::getBhavDate))
                .collect(Collectors.toList());
    }

    private OHLCV aggregateOHLC(List<OHLCV> ohlcvs) {
        double open = ohlcvs.get(0).getOpen();
        double high = ohlcvs.stream().mapToDouble(OHLCV::getHigh).max().orElse(open);
        double low = ohlcvs.stream().mapToDouble(OHLCV::getLow).min().orElse(open);
        double close = ohlcvs.get(ohlcvs.size() - 1).getClose();
        long volume = ohlcvs.stream().mapToLong(OHLCV::getVolume).sum();

        Instant lastBhavDate = ohlcvs.stream().map(OHLCV::getBhavDate).max(Comparator.naturalOrder()).orElse(null);

        return new OHLCV(lastBhavDate, open, high, low, close, volume);
    }

    @FunctionalInterface
    private interface TimeframeAggregator {
        Instant getStart(Instant instant);
    }
}