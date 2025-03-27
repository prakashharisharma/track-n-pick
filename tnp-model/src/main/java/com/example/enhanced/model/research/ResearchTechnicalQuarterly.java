package com.example.enhanced.model.research;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "research_technical_quarterly")
public class ResearchTechnicalQuarterly extends ResearchTechnical{

    public ResearchTechnicalQuarterly() {
        this.setTimeframe(Timeframe.QUARTERLY);
    }
}
