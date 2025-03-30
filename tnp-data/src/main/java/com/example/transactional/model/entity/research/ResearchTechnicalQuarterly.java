package com.example.transactional.model.research;


import com.example.data.common.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "research_technical_quarterly")
public class ResearchTechnicalQuarterly extends ResearchTechnical{

    public ResearchTechnicalQuarterly() {
        this.setTimeframe(Timeframe.QUARTERLY);
    }
}
