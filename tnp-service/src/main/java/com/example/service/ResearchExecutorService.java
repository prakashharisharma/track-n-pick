package com.example.service;

import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;

public interface ResearchExecutorService {

    public void executeFundamental(Stock stock);

    public void executeTechnical(Timeframe timeframe, Stock stock, LocalDate sessionDate);
}
