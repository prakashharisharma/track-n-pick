package com.example.storage.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Document(collection = "weekly_technicals_history")
public class WeeklyStockTechnicals extends StockTechnicals{

    public WeeklyStockTechnicals() {
        super();

    }

    public WeeklyStockTechnicals(String nseSymbol, Instant bhavDate, Volume volume, OnBalanceVolume obv, SimpleMovingAverage sma, ExponentialMovingAverage ema, AverageDirectionalIndex adx, RelativeStrengthIndex rsi, MovingAverageConvergenceDivergence macd) {
        this.nseSymbol = nseSymbol;
        this.bhavDate = bhavDate;
        this.volume = volume;
        this.obv = obv;
        this.sma = sma;
        this.ema = ema;
        this.adx = adx;
        this.rsi = rsi;
        this.macd = macd;
    }
}
