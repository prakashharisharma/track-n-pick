package com.example.transactional.model.research;


import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "research_technical_weekly")
public class ResearchTechnicalWeekly extends ResearchTechnical{

    public ResearchTechnicalWeekly() {
        this.setTimeframe(Timeframe.WEEKLY);
    }
}
