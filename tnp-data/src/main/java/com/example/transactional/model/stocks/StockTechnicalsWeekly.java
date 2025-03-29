package com.example.transactional.model.stocks;


import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_technicals_weekly")
public class StockTechnicalsWeekly extends StockTechnicals {


    public StockTechnicalsWeekly() {
        this.setTimeframe(Timeframe.DAILY);
    }
}
