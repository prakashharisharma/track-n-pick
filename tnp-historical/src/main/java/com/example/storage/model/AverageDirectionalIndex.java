package com.example.storage.model;

public class AverageDirectionalIndex {

    private Double tr;

    private Double plusDm;

    private Double minusDm;

    private Double atr;

    private Double smoothedPlusDM;

    private Double smoothedMinusDM;

    private Double plusDi;

    private Double minusDi;

    private Double dx;

    private Double adx;

    private Double avg3;

    public AverageDirectionalIndex() {
    }

    public AverageDirectionalIndex(Double atr, Double smoothedPlusDM,Double smoothedMinusDM, Double plusDi, Double minusDi, Double dx, Double adx) {
        this.atr = atr;
        this.smoothedPlusDM = smoothedPlusDM;
        this.smoothedMinusDM = smoothedMinusDM;
        this.plusDi = plusDi;
        this.minusDi = minusDi;
        this.dx = dx;
        this.adx = adx;
    }

    public Double getTr() {
        return tr;
    }

    public void setTr(Double tr) {
        this.tr = tr;
    }

    public Double getPlusDm() {
        return plusDm;
    }

    public void setPlusDm(Double plusDm) {
        this.plusDm = plusDm;
    }

    public Double getMinusDm() {
        return minusDm;
    }

    public void setMinusDm(Double minusDm) {
        this.minusDm = minusDm;
    }

    public Double getAtr() {
        return atr;
    }

    public void setAtr(Double atr) {
        this.atr = atr;
    }

    public Double getSmoothedPlusDM() {
        return smoothedPlusDM;
    }

    public void setSmoothedPlusDM(Double smoothedPlusDM) {
        this.smoothedPlusDM = smoothedPlusDM;
    }

    public Double getSmoothedMinusDM() {
        return smoothedMinusDM;
    }

    public void setSmoothedMinusDM(Double smoothedMinusDM) {
        this.smoothedMinusDM = smoothedMinusDM;
    }

    public Double getPlusDi() {
        return plusDi;
    }

    public void setPlusDi(Double plusDi) {
        this.plusDi = plusDi;
    }

    public Double getMinusDi() {
        return minusDi;
    }

    public void setMinusDi(Double minusDi) {
        this.minusDi = minusDi;
    }

    public Double getDx() {
        return dx;
    }

    public void setDx(Double dx) {
        this.dx = dx;
    }

    public Double getAdx() {
        return adx;
    }

    public void setAdx(Double adx) {
        this.adx = adx;
    }

    public Double getAvg3() {
        return avg3;
    }

    public void setAvg3(Double avg3) {
        this.avg3 = avg3;
    }

    @Override
    public String toString() {
        return "AverageDirectionalIndex{" +
                "tr=" + tr +
                ", plusDm=" + plusDm +
                ", minusDm=" + minusDm +
                ", atr=" + atr +
                ", smoothedPlusDM=" + smoothedPlusDM +
                ", smoothedMinusDM=" + smoothedMinusDM +
                ", plusDi=" + plusDi +
                ", minusDi=" + minusDi +
                ", dx=" + dx +
                ", adx=" + adx +
                ", avg3=" + avg3 +
                '}';
    }
}
