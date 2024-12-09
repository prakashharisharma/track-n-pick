package com.example.service;

import com.example.model.master.Stock;

public interface ResearchExecutorService {

    public void executeFundamental(Stock stock);

    public void executeTechnical(Stock stock);

}
