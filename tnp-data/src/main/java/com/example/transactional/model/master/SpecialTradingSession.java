package com.example.transactional.model.master;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "SPECIAL_TRADING_SESSION")
public class SpecialTradingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SPECIAL_TRADING_SESSION_ID")
    long id;

    @Column(name = "TRADING_SESSION_DATE", unique=true)
    LocalDate sessionDate = LocalDate.now();

    @Column(name = "TRADING_SESSION_DESC")
    String desc;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
