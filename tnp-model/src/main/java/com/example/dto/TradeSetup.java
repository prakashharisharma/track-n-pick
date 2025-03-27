package com.example.dto;

import com.example.enhanced.model.research.ResearchTechnical;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


@Data
@Builder
public class TradeSetup implements Serializable {

    private boolean active;
    private ResearchTechnical.Strategy strategy;
    private ResearchTechnical.SubStrategy subStrategy;
}
