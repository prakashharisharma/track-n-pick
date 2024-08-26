package com.example.ui.model;

import java.time.LocalDate;
import java.util.Date;

public class MonthlyPerformance {

    private LocalDate date;
    private long units;

    public MonthlyPerformance() {
    }

    public MonthlyPerformance(LocalDate date, long units) {
        this.date = date;
        this.units = units;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getUnits() {
        return units;
    }

    public void setUnits(long units) {
        this.units = units;
    }
}
