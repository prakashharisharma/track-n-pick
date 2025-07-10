package com.example.dto.response;

import com.example.data.transactional.view.ResearchTechnicalResult;
import com.example.dto.io.StockTechnicalsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResearchTechnicalDetailsCurrentResponse extends ResearchTechnicalResult {
    double marketCap;
    long positionSize;
    long adjustedPositionSize;
    StockTechnicalsDTO technicals;
}
