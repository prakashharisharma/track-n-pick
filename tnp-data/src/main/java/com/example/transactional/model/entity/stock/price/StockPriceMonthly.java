package com.example.transactional.model.entity.stock;


import com.example.data.common.type.Timeframe;

import javax.persistence.*;

@Entity
@Table(name = "stock_price_monthly")
public class StockPriceMonthly extends StockPrice {
    public StockPriceMonthly() {
        this.setTimeframe(Timeframe.MONTHLY);
    }
}
