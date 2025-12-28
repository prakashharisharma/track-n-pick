package com.example.service;

public class SupportZone {
    private double level;
    private double min;
    private double max;
    private int touches;
    private int strength;

    // Getters and setters
    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public int getTouches() {
        return touches;
    }

    public void setTouches(int touches) {
        this.touches = touches;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public String toString() {
        return String.format(
                "SupportZone{level=%.2f-range=%.2f-%.2f-touches=%d-strength=%d}",
                level, min, max, touches, strength);
    }
}
