package com.example.storage.model;

public class Macd {

    private Double macd;

    private Double signal;

    private  Double avg3;

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

    public Double getAvg3() {
        return avg3;
    }

    public void setAvg3(Double avg3) {
        this.avg3 = avg3;
    }
}
