package com.example.processor;



import com.example.dto.io.StockPriceIN;

import java.util.List;

public interface BhavProcessor {

    public void process(List<StockPriceIN> nseBhavIOList) ;
    public void processTimeframePrice();
    public void processAndResearchTechnicals();
}
