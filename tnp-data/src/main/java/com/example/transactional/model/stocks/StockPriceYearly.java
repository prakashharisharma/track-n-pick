package com.example.transactional.model.stocks;


import com.example.data.common.type.Timeframe;

import javax.persistence.*;

@Entity
@Table(name = "stock_price_yearly")
public class StockPriceYearly extends StockPrice {

    public StockPriceYearly() {
        this.setTimeframe(Timeframe.YEARLY);
    }
}
