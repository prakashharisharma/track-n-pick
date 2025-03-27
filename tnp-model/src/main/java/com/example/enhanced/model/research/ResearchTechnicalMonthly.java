package com.example.enhanced.model.research;

import com.example.util.io.model.type.Timeframe;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "research_technical_monthly")
public class ResearchTechnicalMonthly extends ResearchTechnical{

    public ResearchTechnicalMonthly() {
        this.setTimeframe(Timeframe.MONTHLY);
    }
}
