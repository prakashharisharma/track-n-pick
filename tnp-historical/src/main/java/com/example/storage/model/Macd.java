package com.example.storage.model;

public class Macd {

    private Double macd;

    private Double signal;

    public Macd() {
    }

    public Macd(Double macd, Double signal) {
        this.macd = macd;
        this.signal = signal;
    }

    public Double getMacd() {
        return macd;
    }

    public void setMacd(Double macd) {
        this.macd = macd;
    }

    public Double getSignal() {
        return signal;
    }

    public void setSignal(Double signal) {
        this.signal = signal;
    }
}
