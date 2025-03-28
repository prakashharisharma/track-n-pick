package com.example.transactional.model.stocks;


import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "stock_technicals_quarterly")
public class StockTechnicalsQuarterly extends StockTechnicals {

    public StockTechnicalsQuarterly() {
        this.setTimeframe(Timeframe.QUARTERLY);
    }
}
