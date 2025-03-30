package com.example.transactional.model.entity.stock;


import com.example.data.common.type.Timeframe;

import javax.persistence.*;

@Entity
@Table(name = "stock_price_weekly")
public class StockPriceWeekly extends StockPrice {

    public StockPriceWeekly() {
        this.setTimeframe(Timeframe.WEEKLY);
    }
}

