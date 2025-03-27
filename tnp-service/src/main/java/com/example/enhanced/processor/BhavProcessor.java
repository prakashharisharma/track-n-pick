package com.example.enhanced.processor;

import com.example.enhanced.model.StockPriceIN;

import java.util.List;

public interface BhavProcessor {

    public void process(List<StockPriceIN> nseBhavIOList) ;
    public void processTimeframePrice();
    public void processAndResearchTechnicals();
}
