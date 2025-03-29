package com.example.storage.model;

public class RelativeStrengthIndex {

    private Double avgUpMove;

    private Double avgDownMove;

    private Double rs;

    private Double rsi;

    public RelativeStrengthIndex() {

    }

    public RelativeStrengthIndex(Double avgUpMove, Double avgDownMove, Double rs, Double rsi) {
        this.avgUpMove = avgUpMove;
        this.avgDownMove = avgDownMove;
        this.rs = rs;
        this.rsi = rsi;
    }

    public Double getAvgUpMove() {
        return avgUpMove;
    }

    public void setAvgUpMove(Double avgUpMove) {
        this.avgUpMove = avgUpMove;
    }

    public Double getAvgDownMove() {
        return avgDownMove;
    }

    public void setAvgDownMove(Double avgDownMove) {
        this.avgDownMove = avgDownMove;
    }

    public Double getRs() {
        return rs;
    }

    public void setRs(Double rs) {
        this.rs = rs;
    }

    public Double getRsi() {
        return rsi;
    }

    public void setRsi(Double rsi) {
        this.rsi = rsi;
    }

    @Override
    public String toString() {
        return "RelativeStrengthIndex{" +
                "avgUpMove=" + avgUpMove +
                ", avgDownMove=" + avgDownMove +
                ", rs=" + rs +
                ", rsi=" + rsi +
                '}';
    }
}
