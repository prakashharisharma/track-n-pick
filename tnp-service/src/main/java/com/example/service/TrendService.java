package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.transactional.model.master.Stock;

public interface TrendService {


    public Trend detect(Stock stock, Timeframe timeframe);


}
