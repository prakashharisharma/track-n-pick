package com.example.transactional.model.stocks;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_technicals_monthly")
public class StockTechnicalsMonthly extends StockTechnicals {

    public StockTechnicalsMonthly() {
        this.setTimeframe(Timeframe.MONTHLY);
    }
}
