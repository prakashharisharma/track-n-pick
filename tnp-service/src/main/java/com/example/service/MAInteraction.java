package com.example.service;

public class MAInteraction {
    private final MovingAverageLength length;
    private final double value;
    private final boolean supportSide;

    private MAInteraction(MovingAverageLength length, double value, boolean supportSide) {
        this.length = length;
        this.value = value;
        this.supportSide = supportSide;
    }

    public static MAInteraction of(MovingAverageLength length, double value, boolean supportSide) {
        return new MAInteraction(length, value, supportSide);
    }

    public MovingAverageLength getLength() {
        return length;
    }

    public double getValue() {
        return value;
    }

    public boolean supportSide() {
        return supportSide;
    }

    @Override
    public String toString() {
        return "MAInteraction{"
                + "length="
                + length
                + ", value="
                + value
                + ", supportSide="
                + supportSide
                + '}';
    }
}
