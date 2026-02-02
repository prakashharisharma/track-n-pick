package com.example.service.scanner;

import com.example.data.transactional.entities.StockPrice;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeeklyAnalysisResult {
    private StockPrice weeklyCandle;

    // Analysis results
    private List<PriceLevel> supportLevels = new ArrayList<>();
    private List<PriceLevel> resistanceLevels = new ArrayList<>();
    private PriceLevel nearestSupport;
    private PriceLevel nearestResistance;
    private Double supportDistance;
    private Double resistanceDistance;

    private List<LevelInteraction> allInteractions = new ArrayList<>();
    private List<LevelInteraction> strongInteractions = new ArrayList<>();
    private List<LevelInteraction> confirmedBreakouts = new ArrayList<>();
    private List<LevelInteraction> strongHolds = new ArrayList<>();

    // Scores
    private double overallScore; // 0-100
    private double breakoutScore; // 0-100
    private double supportScore; // 0-100
    private double riskRewardRatio;

    // Signals
    private TradingSignal primarySignal;
    private ConfidenceLevel confidence;
    private String summary;
}
