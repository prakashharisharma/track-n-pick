package com.example.transactional.processor;

import com.example.transactional.model.StockPriceIN;

import java.util.List;

public interface BhavProcessor {

    public void process(List<StockPriceIN> nseBhavIOList) ;
    public void processTimeframePrice();
    public void processAndResearchTechnicals();
}
