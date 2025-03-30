package com.example.transactional.model.entity.stock;


import com.example.data.common.type.Timeframe;

import javax.persistence.*;

@Entity
@Table(name = "stock_price_daily")
public class StockPriceDaily extends StockPrice {
    public StockPriceDaily() {
        this.setTimeframe(Timeframe.DAILY);
    }
}
