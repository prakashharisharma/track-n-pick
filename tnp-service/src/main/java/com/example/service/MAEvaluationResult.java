package com.example.service;

public class MAEvaluationResult {

    private final MovingAverageLength length;

    // MA value
    private final double value;

    private final double prevValue;

    private final boolean supportSide;
    private final boolean nearSupport;
    private final boolean breakout;
    private final boolean nearResistance;
    private final boolean breakdown;

    private final MAInteractionType interactionType;

    public MAEvaluationResult(
            MovingAverageLength length,
            double value,
            double prevValue,
            boolean supportSide,
            boolean nearSupport,
            boolean breakout,
            boolean nearResistance,
            boolean breakdown) {
        this.length = length;
        this.value = value;
        this.prevValue = value;
        this.supportSide = supportSide;
        this.nearSupport = nearSupport;
        this.breakout = breakout;
        this.nearResistance = nearResistance;
        this.breakdown = breakdown;
        this.interactionType = resolveInteractionType();
    }

    private MAInteractionType resolveInteractionType() {
        if (breakout) return MAInteractionType.BREAKOUT;
        if (breakdown) return MAInteractionType.BREAKDOWN;
        if (nearSupport) return MAInteractionType.SUPPORT;
        if (nearResistance) return MAInteractionType.RESISTANCE;
        return MAInteractionType.NEUTRAL; // Or throw or define a default UNKNOWN type
    }

    public MovingAverageLength getLength() {
        return length;
    }

    public double getValue() {
        return value;
    }

    public double getPrevValue() {
        return prevValue;
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

    public MAInteractionType getInteractionType() {
        return interactionType;
    }

    @Override
    public String toString() {
        return "MAEvaluationResult{"
                + "length="
                + length
                + ", value="
                + value
                + ", supportSide="
                + supportSide
                + ", nearSupport="
                + nearSupport
                + ", breakout="
                + breakout
                + ", breakdown="
                + breakdown
                + ", nearResistance="
                + nearResistance
                + ", interactionType="
                + interactionType
                + '}';
    }
}
