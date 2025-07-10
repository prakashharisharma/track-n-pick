package com.example.dto.io;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Builder
public class FinancialsSummaryDto {

    private long issuedSize;

    private double faceValue;
}
