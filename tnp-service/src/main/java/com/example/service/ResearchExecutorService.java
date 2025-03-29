package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.master.Stock;


import java.time.LocalDate;

public interface ResearchExecutorService {

    public void executeFundamental(Stock stock);

    public void executeTechnical(Timeframe timeframe, Stock stock, LocalDate sessionDate);
}
