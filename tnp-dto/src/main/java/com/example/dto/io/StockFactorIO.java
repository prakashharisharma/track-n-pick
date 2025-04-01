package com.example.dto.io;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class StockFactorIO implements Serializable {

    /** */
    private static final long serialVersionUID = -7265805744759390934L;

    String nseSymbol;

    double marketCap;

    double debtEquity;

    double currentRatio;

    double quickRatio;

    double dividend;

    double bookValue;

    double eps;

    double returnOnEquity;

    double returnOnCapital;

    double faceValue;

    LocalDate quarterEnded = LocalDate.now();

    Instant quarterEndedInstant = Instant.now();

    public StockFactorIO(
            String nseSymbol,
            double marketCap,
            double debtEquity,
            double currentRatio,
            double quickRatio,
            double dividend,
            double bookValue,
            double eps,
            double returnOnEquity,
            double returnOnCapital,
            double faceValue,
            LocalDate quarterEnded) {
        super();
        this.nseSymbol = nseSymbol;
        this.marketCap = marketCap;
        this.debtEquity = debtEquity;
        this.currentRatio = currentRatio;
        this.quickRatio = quickRatio;
        this.dividend = dividend;
        this.bookValue = bookValue;
        this.eps = eps;
        this.returnOnEquity = returnOnEquity;
        this.returnOnCapital = returnOnCapital;
        this.faceValue = faceValue;
        this.quarterEnded = quarterEnded;

        this.quarterEndedInstant = quarterEnded.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public String getNseSymbol() {
        return nseSymbol;
    }

    public void setNseSymbol(String nseSymbol) {
        this.nseSymbol = nseSymbol;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(double marketCap) {
        this.marketCap = marketCap;
    }

    public double getDebtEquity() {
        return debtEquity;
    }

    public void setDebtEquity(double debtEquity) {
        this.debtEquity = debtEquity;
    }

    public double getCurrentRatio() {
        return currentRatio;
    }

    public void setCurrentRatio(double currentRatio) {
        this.currentRatio = currentRatio;
    }

    public double getQuickRatio() {
        return quickRatio;
    }

    public void setQuickRatio(double quickRatio) {
        this.quickRatio = quickRatio;
    }

    public double getDividend() {
        return dividend;
    }

    public void setDividend(double dividend) {
        this.dividend = dividend;
    }

    public double getBookValue() {
        return bookValue;
    }

    public void setBookValue(double bookValue) {
        this.bookValue = bookValue;
    }

    public double getEps() {
        return eps;
    }

    public void setEps(double eps) {
        this.eps = eps;
    }

    public double getReturnOnEquity() {
        return returnOnEquity;
    }

    public void setReturnOnEquity(double returnOnEquity) {
        this.returnOnEquity = returnOnEquity;
    }

    public double getReturnOnCapital() {
        return returnOnCapital;
    }

    public void setReturnOnCapital(double returnOnCapital) {
        this.returnOnCapital = returnOnCapital;
    }

    public double getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(double faceValue) {
        this.faceValue = faceValue;
    }

    public LocalDate getQuarterEnded() {
        return quarterEnded;
    }

    public void setQuarterEnded(LocalDate quarterEnded) {
        this.quarterEnded = quarterEnded;
    }

    public Instant getQuarterEndedInstant() {
        return quarterEndedInstant;
    }

    public void setQuarterEndedInstant(Instant quarterEndedInstant) {
        this.quarterEndedInstant = quarterEndedInstant;
    }

    @Override
    public String toString() {
        return "StockFactorIO [nseSymbol="
                + nseSymbol
                + ", marketCap="
                + marketCap
                + ", debtEquity="
                + debtEquity
                + ", currentRatio="
                + currentRatio
                + ", quickRatio="
                + quickRatio
                + ", dividend="
                + dividend
                + ", bookValue="
                + bookValue
                + ", eps="
                + eps
                + ", returnOnEquity="
                + returnOnEquity
                + ", returnOnCapital="
                + returnOnCapital
                + ", faceValue="
                + faceValue
                + ", quarterEnded="
                + quarterEnded
                + "]";
    }
}
