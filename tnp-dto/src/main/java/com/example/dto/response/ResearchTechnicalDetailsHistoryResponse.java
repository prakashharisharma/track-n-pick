package com.example.dto.response;

import com.example.data.transactional.view.ResearchTechnicalResult;
import java.time.LocalDate;
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
public class ResearchTechnicalDetailsHistoryResponse extends ResearchTechnicalResult {
    double marketCap;
    LocalDate researchDate;
    double researchPrice;
    LocalDate exitDate;
    Double exitPrice;
}
