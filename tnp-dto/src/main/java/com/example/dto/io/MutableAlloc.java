package com.example.dto.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MutableAlloc {
    private StockAnalysis stock;
    private double allocated = 0.0;
    private boolean capped = false;
    private double performanceScore;

    public MutableAlloc(StockAnalysis s) {
        this.stock = s;
        // Performance score will be calculated separately in the allocation method
        this.performanceScore = 1.0; // Default score
    }
}
