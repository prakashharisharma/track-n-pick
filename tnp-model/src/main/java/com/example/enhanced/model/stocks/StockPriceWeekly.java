package com.example.enhanced.model.stocks;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price_weekly")
public class StockPriceWeekly extends StockPrice {

    public StockPriceWeekly() {
        this.setTimeframe(Timeframe.WEEKLY);
    }
}

