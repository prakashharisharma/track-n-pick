package com.example.storage.model;

public class MovingAverageConvergenceDivergence {

    private Double ema12;

    private Double ema26;
    private Double macd;

    private Double signal;

    public MovingAverageConvergenceDivergence() {
    }

    public MovingAverageConvergenceDivergence(Double ema12, Double ema26, Double macd, Double signal) {
        this.ema12 = ema12;
        this.ema26 = ema26;
        this.macd = macd;
        this.signal = signal;
    }

    public Double getEma12() {
        return ema12;
    }

    public void setEma12(Double ema12) {
        this.ema12 = ema12;
    }

    public Double getEma26() {
        return ema26;
    }

    public void setEma26(Double ema26) {
        this.ema26 = ema26;
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

    @Override
    public String toString() {
        return "MovingAverageConvergenceDivergence{" +
                "ema12=" + ema12 +
                ", ema26=" + ema26 +
                ", macd=" + macd +
                ", signal=" + signal +
                '}';
    }
}
