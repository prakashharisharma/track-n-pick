package com.example.transactional.model.entity.stock;


import com.example.data.common.type.Timeframe;

import javax.persistence.*;

@Entity
@Table(name = "stock_price_quarterly")
public class StockPriceQuarterly extends StockPrice {

    public StockPriceQuarterly() {
        this.setTimeframe(Timeframe.QUARTERLY);
    }
}
