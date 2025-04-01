package com.example.service;

import com.example.dto.OHLCV;
import java.time.LocalDate;
import java.util.List;

public interface WeeklyOhlcvService {
    List<OHLCV> fetch(String nseSymbol, LocalDate from, LocalDate to);
}
