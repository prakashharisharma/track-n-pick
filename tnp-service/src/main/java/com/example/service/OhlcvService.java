package com.example.service;

import com.example.dto.OHLCV;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;
import java.util.List;

public interface OhlcvService {

    List<OHLCV> fetch(String nseSymbol, LocalDate from, LocalDate to);

    List<OHLCV> fetch(Timeframe timeframe, String nseSymbol, LocalDate from, LocalDate to);
}
