package com.example.service;

import com.example.data.transactional.entities.Stock;
import com.example.dto.common.OHLCV;
import java.time.LocalDate;

public interface WeeklySupportResistanceService {

    public OHLCV supportAndResistance(Stock stock);

    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to);
}
