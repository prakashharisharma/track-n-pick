package com.example.service;

public enum MovingAverageLength {
    SHORTEST(0),
    SHORT(1),
    MEDIUM(2),
    LONG(3),
    LONGEST(4);

    private final int period;

    MovingAverageLength(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }
}
