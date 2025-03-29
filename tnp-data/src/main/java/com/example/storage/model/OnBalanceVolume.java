package com.example.storage.model;

public class OnBalanceVolume {

    private Long obv;

    private Long average;

    public OnBalanceVolume(Long obv, Long average) {
        this.obv = obv;
        this.average = average;
    }

    public Long getObv() {
        return obv;
    }

    public void setObv(Long obv) {
        this.obv = obv;
    }

    public Long getAverage() {
        return average;
    }

    public void setAverage(Long average) {
        this.average = average;
    }

    @Override
    public String toString() {
        return "OnBalanceVolume{" +
                "obv=" + obv +
                ", average=" + average +
                '}';
    }
}
