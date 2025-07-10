package com.example.service;

import com.example.dto.common.OHLCV;
import java.time.LocalDate;

public interface DailySupportResistanceService {

    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to);
}
