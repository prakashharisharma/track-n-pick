package com.example.service;

import com.example.transactional.model.master.Stock;
import com.example.util.io.model.type.Timeframe;
import com.example.util.io.model.type.Trend;

public interface TrendService {


    public Trend detect(Stock stock, Timeframe timeframe);


}
