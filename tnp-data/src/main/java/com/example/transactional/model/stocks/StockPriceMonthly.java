package com.example.transactional.model.stocks;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.*;

@Entity
@Table(name = "stock_price_monthly")
public class StockPriceMonthly extends StockPrice {
    public StockPriceMonthly() {
        this.setTimeframe(Timeframe.MONTHLY);
    }
}
