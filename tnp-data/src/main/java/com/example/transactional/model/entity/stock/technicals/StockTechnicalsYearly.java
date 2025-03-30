package com.example.transactional.model.entity.stock;


import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_technicals_yearly")
public class StockTechnicalsYearly extends StockTechnicals {

    public StockTechnicalsYearly() {
        this.setTimeframe(Timeframe.YEARLY);
    }
}
