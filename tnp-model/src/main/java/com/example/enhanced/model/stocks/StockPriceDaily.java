package com.example.enhanced.model.stocks;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price_daily")
public class StockPriceDaily extends StockPrice {
    public StockPriceDaily() {
        this.setTimeframe(Timeframe.DAILY);
    }
}
