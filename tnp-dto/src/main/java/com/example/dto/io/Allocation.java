package com.example.dto.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Allocation {
    private StockAnalysis stock;
    private double allocatedAmount;
}
