package com.example.transactional.model.entity.stock;


import com.example.data.common.type.Timeframe;

import javax.persistence.*;

@Entity
@Table(name = "stock_price_yearly")
public class StockPriceYearly extends StockPrice {

    public StockPriceYearly() {
        this.setTimeframe(Timeframe.YEARLY);
    }
}
