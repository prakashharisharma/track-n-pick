package com.example.enhanced.model.stocks;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "stock_technicals_daily")
public class StockTechnicalsDaily extends StockTechnicals {

    public StockTechnicalsDaily() {
        this.setTimeframe(Timeframe.DAILY);
    }
}
