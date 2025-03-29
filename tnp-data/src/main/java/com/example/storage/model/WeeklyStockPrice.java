package com.example.storage.model;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Document(collection = "weekly_price_history")
public class WeeklyStockPrice extends StockPrice{

    public WeeklyStockPrice() {
        super();
    }

    public WeeklyStockPrice(String nseSymbol, Instant bhavDate, Double open, Double high, Double low, Double close, Long volume) {
        this.nseSymbol = nseSymbol;
        this.bhavDate = bhavDate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

}
