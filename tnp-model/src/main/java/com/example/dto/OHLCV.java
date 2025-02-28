package com.example.dto;

import java.time.Instant;

public class OHLCV {
    //@Id
    //private String id;
    //private String nseSymbol;
    private Instant bhavDate = Instant.now();
    private Double open;
    private Double high;
    private Double low;
    private Double close;

    private Long volume;


    public OHLCV() {
    }

    public OHLCV(Double open, Double high, Double low, Double close, Long volume) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public OHLCV(Instant bhavDate, Double open, Double high, Double low, Double close, Long volume) {
        //this.nseSymbol = nseSymbol;
        this.bhavDate = bhavDate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public Instant getBhavDate() {
        return bhavDate;
    }

    public void setBhavDate(Instant bhavDate) {
        this.bhavDate = bhavDate;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "OHLCV{" +
                "bhavDate=" + bhavDate +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }
}
