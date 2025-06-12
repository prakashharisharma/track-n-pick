package com.example.dto.integration;

import com.example.dto.config.SentimentColorDeserializer;
import com.example.dto.type.SentimentColor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.util.List;

@Data
public class StockOverviewResponse {

    private StockOverviewData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockOverviewData {

        private double divyield_data;

        private double roeOnTop;
        private List<PriceInsight> priceInsights;

        @JsonDeserialize(using = SentimentColorDeserializer.class)
        private SentimentColor qualityColor;
        private String qualityInsight;
        private double qualityValue;

        @JsonDeserialize(using = SentimentColorDeserializer.class)
        private SentimentColor technicalColor;
        private String technicalInsight;
        private double technicalValue;

        @JsonDeserialize(using = SentimentColorDeserializer.class)
        private SentimentColor valuationColor;
        private String valuationInsight;
        private double valuationValue;

        private double dividendAmount;
    }

    @Data
    public static class PriceInsight {
        private String color;
        private String icon;
        private String longtext;
        private String param;
        private String shorttext;
        private String title;
        private String url;
        private String urlhelp;
        private double value;
    }
}

