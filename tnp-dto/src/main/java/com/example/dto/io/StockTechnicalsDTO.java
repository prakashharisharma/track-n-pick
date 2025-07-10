package com.example.dto.io;

import java.time.LocalDate;
import lombok.Data;

@Data
public class StockTechnicalsDTO {

    private Long stockTechnicalsId;
    private Double sma5 = 0.00;
    private Double sma10 = 0.00;
    private Double sma20 = 0.00;
    private Double sma50 = 0.00;
    private Double sma100 = 0.00;
    private Double sma200 = 0.00;
    private Double ema5 = 0.00;
    private Double ema10 = 0.00;
    private Double ema20 = 0.00;
    private Double ema50 = 0.00;
    private Double ema100 = 0.00;
    private Double ema200 = 0.00;
    private Double rsi = 0.00;
    private Double macd = 0.00;
    private Double signal = 0.00;
    private Long obv = 0L;
    private Long obvAvg = 0L;
    private Long volume = 0L;
    private Long volumeAvg5 = 0L;
    private Long volumeAvg10 = 0L;
    private Long volumeAvg20 = 0L;
    private Double adx = 0.00;
    private Double plusDi = 0.00;
    private Double minusDi = 0.00;
    private Double atr = 0.00;
    private LocalDate sessionDate;
}
