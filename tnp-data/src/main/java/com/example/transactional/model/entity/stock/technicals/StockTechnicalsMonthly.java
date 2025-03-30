package com.example.transactional.model.entity.stock;


import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_technicals_monthly")
public class StockTechnicalsMonthly extends StockTechnicals {

    public StockTechnicalsMonthly() {
        this.setTimeframe(Timeframe.MONTHLY);
    }
}
