package com.example.service;

public class MAEvaluationResult {
    private final MovingAverageLength length;

    //MA value
    private double value;
    private final boolean supportSide;
    private final boolean nearSupport;
    private final boolean breakout;
    private final boolean nearResistance;
    private final boolean breakdown;

    public MAEvaluationResult(
            MovingAverageLength length,
            double value,
            boolean supportSide,
            boolean nearSupport,
            boolean breakout,
            boolean nearResistance,
            boolean breakdown) {
        this.length = length;
        this.value = value;
        this.supportSide = supportSide;
        this.nearSupport = nearSupport;
        this.breakout = breakout;
        this.nearResistance = nearResistance;
        this.breakdown = breakdown;
    }

    public MovingAverageLength getLength() {
        return length;
    }

    public double getValue() {
        return value;
    }

    public boolean isSupportSide() {
        return supportSide;
    }

    public boolean isNearSupport() {
        return nearSupport;
    }

    public boolean isBreakout() {
        return breakout;
    }

    public boolean isNearResistance() {
        return nearResistance;
    }

    public boolean isBreakdown() {
        return breakdown;
    }

    @Override
    public String toString() {
        return "MAEvaluationResult{"
                + "length="
                + length
                + " value="
                + value
                + ", supportSide="
                + supportSide
                + ", nearSupport="
                + nearSupport
                + ", breakdown="
                + breakdown
                + ", breakout="
                + breakout
                + ", nearResistance="
                + nearResistance
                + '}';
    }
}
