package com.example.service;

public enum MovingAverageLength {
    HIGHEST(1, 5),
    HIGH(2, 20),
    MEDIUM(3, 50),
    LOW(4, 100),
    LOWEST(5, 200);

    private final int weight;
    private final int maDays;

    MovingAverageLength(int weight, int maDays) {
        this.weight = weight;
        this.maDays = maDays;
    }

    public int getMaDays() {
        return maDays;
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

    public MovingAverageLength getHigher(boolean sortByValue) {
        return sortByValue ? getHigher() : getLower();
    }

    public MovingAverageLength getLower(boolean sortByValue) {
        return sortByValue ? getLower() : getHigher();
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
