package com.example.dto.io;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockAnalysis {
    private LocalDate scanDate;
    private LocalDate currentCloseDate;
    private ResearchTechnical.Strategy strategy;
    private Stock stock;
    private MarketCapCategory marketCap;
    private boolean is4Incr;
    private boolean isAvgIncr;
    private boolean isVolIncr;
    private boolean isLowRejected;
    private boolean isLongLowerWick;
    private double close;
    private double currentClose;
    private double currentHigh;
    private double entryPrice;
    private double stopLoss;
    private double breakdownLevel;
    private double hardStopLoss;
    private double risk;
    private double target;
    private double changePercent;
    private boolean isExitCandidate;
    private boolean isReEntry;
}
