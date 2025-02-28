package com.example.service;

import com.example.dto.OHLCV;

import java.time.LocalDate;
import java.util.List;

public interface OhlcvService {

    List<OHLCV> fetch(String nseSymbol, LocalDate from, LocalDate to);
}
