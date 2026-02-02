package com.example.service.scanner;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceLevel implements Serializable {
    private LevelType levelType;
    private double price;
    private LocalDate date;
    private double weight; // Timeframe weight
}
