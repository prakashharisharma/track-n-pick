package com.example.dto;

import com.example.model.ledger.ResearchLedgerTechnical;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TradeSetup implements Serializable {

    private double entryPrice;
    private double targetPrice;
    private double stopLossPrice;
    private double risk;
    private double correction;
    private boolean active;


    private ResearchLedgerTechnical.Strategy strategy;

    private ResearchLedgerTechnical.SubStrategy subStrategy;

}
