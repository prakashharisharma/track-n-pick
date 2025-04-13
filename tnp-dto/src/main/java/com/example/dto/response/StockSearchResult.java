package com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSearchResult {
    Long id;
    String symbol;
    String name;
    double price;
    double change;
    double changePercent;
    long volume;
    long marketCap;
    String sector;
    Long sectorId;
}
