package com.example.transactional.model.research;


import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "research_technical_yearly")
public class ResearchTechnicalYearly extends ResearchTechnical{

    public ResearchTechnicalYearly() {
        this.setTimeframe(Timeframe.YEARLY);
    }
}
