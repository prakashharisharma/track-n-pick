package com.example.service;

public enum MovingAverageLength {
    HIGHEST(1),
    HIGH(2),
    MEDIUM(3),
    LOW(4),
    LOWEST(5);

    private final int weight;

    MovingAverageLength(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public int getReverseWeight() {
        return 6 - weight; // Makes LOWEST => 5, LOW => 4, ..., HIGHEST => 1
    }

    public MovingAverageLength getHigher() {
        return fromWeightOrDefault(this.weight - 1, this);
    }

    public MovingAverageLength getLower() {
        return fromWeightOrDefault(this.weight + 1, this);
    }

    private static MovingAverageLength fromWeightOrDefault(
            int weight, MovingAverageLength defaultValue) {
        for (MovingAverageLength length : values()) {
            if (length.weight == weight) {
                return length;
            }
        }
        return defaultValue;
    }
}
