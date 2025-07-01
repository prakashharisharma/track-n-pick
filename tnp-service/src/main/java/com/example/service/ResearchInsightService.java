package com.example.service;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.transactional.entities.EvaluationLog;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.dto.integration.StockOverviewResponse;
import com.example.dto.type.SentimentColor;
import com.example.external.Research360Client;
import com.example.model.type.IndiceType;
import com.example.service.impl.FundamentalResearchService;
import com.example.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResearchInsightService {

    private static final Map<String, Double> valuationMap = new HashMap<>();
    private final Research360Client research360Client;

    private final EvaluationLogService evaluationLogService;

    private final FundamentalResearchService fundamentalResearchService;

    public boolean isStrongInsights(StockPrice stockPrice) {
        Stock stock = stockPrice.getStock();
        try {

            StockOverviewResponse stockOverviewResponse =
                    research360Client.fetchStockOverview(stock.getIsinCode());

            if (stockOverviewResponse == null || stockOverviewResponse.getData() == null) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        "isStrongInsights: No stock overview data returned → false");
                return false;
            }

            valuationMap.put(
                    stock.getNseSymbol(), stockOverviewResponse.getData().getValuationValue());

            SentimentColor qualityColor = stockOverviewResponse.getData().getQualityColor();
            SentimentColor valuationColor = stockOverviewResponse.getData().getValuationColor();
            SentimentColor technicalColor = stockOverviewResponse.getData().getTechnicalColor();

            if (qualityColor == null || qualityColor == SentimentColor.NEGATIVE) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        StringUtils.format(
                                "isStrongInsights: qualityColor:{} is null or NEGATIVE → false",
                                qualityColor));
                return false;
            }

            if (valuationColor == SentimentColor.NEGATIVE
                    && technicalColor == SentimentColor.NEGATIVE) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.NEUTRAL,
                        "isStrongInsights: Both valuation and technical colors are NEGATIVE →"
                                + " false");
                return false;
            }

            int score = this.calculateScore(qualityColor, valuationColor, technicalColor);



            boolean result = (valuationColor == SentimentColor.NEGATIVE) ? score >= 6 : score >= 5;

            evaluationLogService.add(
                    stockPrice,
                    result ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "isStrongInsights: qualityColor={}, valuationColor={},"
                                    + " technicalColor={}, score={}, threshold={} → {}",
                            qualityColor,
                            valuationColor,
                            technicalColor,
                            score,
                            (valuationColor == SentimentColor.NEGATIVE ? 6 : 5),
                            result));

            MarketCapCategory marketCapCategory = MarketCapCategory.classify(fundamentalResearchService.marketCap(stock));

            if(marketCapCategory == MarketCapCategory.MEGACAP || marketCapCategory == MarketCapCategory.LARGECAP){
                return qualityColor != SentimentColor.NEGATIVE && score >= 5;
            }

            return result;

        } catch (Exception e) {
            log.error(
                    "An error occurred while getting research 360 insights for {}",
                    stock.getNseSymbol(),
                    e);
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEUTRAL,
                    "isStrongInsights: Exception occurred while fetching research360 data");
        }

        return false;
    }

    private int calculateScore(
            SentimentColor qualityColor,
            SentimentColor valuationColor,
            SentimentColor technicalColor) {

        if (qualityColor == null || valuationColor == null || technicalColor == null) {
            return 0;
        }

        return qualityColor.getWeight() + valuationColor.getWeight() + technicalColor.getWeight();
    }

    public double valuationScore(Stock stock) {

        if (valuationMap.containsKey(stock.getNseSymbol())) {
            return valuationMap.get(stock.getNseSymbol());
        }

        return 0.0;
    }
}
