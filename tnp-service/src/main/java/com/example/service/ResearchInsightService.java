package com.example.service;

import com.example.data.transactional.entities.Stock;
import com.example.dto.integration.StockOverviewResponse;
import com.example.dto.type.SentimentColor;
import com.example.external.Research360Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResearchInsightService {

    private static final Map<String, Double> valuationMap = new HashMap<>();
    private final Research360Client research360Client;

    public boolean isStrongInsights(Stock stock){

        try {

            StockOverviewResponse stockOverviewResponse = research360Client.fetchStockOverview(stock.getIsinCode());

            if(stockOverviewResponse == null ||
                    stockOverviewResponse.getData() == null){
                return false;
            }
            valuationMap.put(stock.getNseSymbol(), stockOverviewResponse.getData().getValuationValue());

            if(stockOverviewResponse.getData().getQualityColor() == null || stockOverviewResponse.getData().getQualityColor() == SentimentColor.NEGATIVE){
                return false;
            }

            if(stockOverviewResponse.getData().getValuationColor() == SentimentColor.NEGATIVE && stockOverviewResponse.getData().getTechnicalColor() == SentimentColor.NEGATIVE){
                return false;
            }

            int score = this.calculateScore(
                    stockOverviewResponse.getData().getQualityColor(),
                    stockOverviewResponse.getData().getValuationColor(),
                    stockOverviewResponse.getData().getTechnicalColor()
            );

            SentimentColor valuationColor = stockOverviewResponse.getData().getValuationColor();

            if (valuationColor == SentimentColor.NEGATIVE) {
                return score >= 6;
            } else {
                return score >= 5;
            }

        } catch (Exception e) {
            log.error("An error occured while getting research 360 insights {}", stock.getNseSymbol(), e);
        }

        return false;
    }


    private int calculateScore(SentimentColor qualityColor, SentimentColor valuationColor,SentimentColor technicalColor){

        if(qualityColor == null || valuationColor == null || technicalColor == null){
            return 0;
        }

        return qualityColor.getWeight() + valuationColor.getWeight() + technicalColor.getWeight();

    }

    public double valuationScore(Stock stock){

        if(valuationMap.containsKey(stock.getNseSymbol())){
            return valuationMap.get(stock.getNseSymbol());
        }

        return 0.0;
    }

}
