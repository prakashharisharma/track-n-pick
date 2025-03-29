package com.example.transactional.model.research;

import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "research_technical_daily")
public class ResearchTechnicalDaily extends ResearchTechnical{

    public ResearchTechnicalDaily() {
        this.setTimeframe(Timeframe.DAILY);
    }
}
