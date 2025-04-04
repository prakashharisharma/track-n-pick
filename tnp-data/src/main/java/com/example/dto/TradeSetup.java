package com.example.dto;

import com.example.data.transactional.entities.ResearchTechnical;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeSetup implements Serializable {

    private boolean active;
    private ResearchTechnical.Strategy strategy;
    private ResearchTechnical.SubStrategy subStrategy;
}
