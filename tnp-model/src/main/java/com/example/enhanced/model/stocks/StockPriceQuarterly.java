package com.example.enhanced.model.stocks;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price_quarterly")
public class StockPriceQuarterly extends StockPrice {

    public StockPriceQuarterly() {
        this.setTimeframe(Timeframe.QUARTERLY);
    }
}
