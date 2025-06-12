package com.example.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class MAThresholdsConfig {

    private static final Map<MAInteractionType, Map<MovingAverageLength, Double>> thresholds =
            new EnumMap<>(MAInteractionType.class);

    static {
        thresholds.put(
                MAInteractionType.BREAKOUT,
                Map.of(
                        MovingAverageLength.LOWEST, 10.0, // MA5
                        MovingAverageLength.LOW, 7.0, // MA20
                        MovingAverageLength.MEDIUM, 5.0, // MA50
                        MovingAverageLength.HIGH, 2.0 // MA100
                        // MA200 (HIGHEST) => anchor, no check
                        ));

        thresholds.put(
                MAInteractionType.BREAKDOWN,
                Map.of(
                        MovingAverageLength.LOW, 2.0,
                        MovingAverageLength.MEDIUM, 5.0,
                        MovingAverageLength.HIGH, 7.0,
                        MovingAverageLength.HIGHEST, 10.0
                        // MA200 (LOWEST) => anchor, no check
                        ));

        thresholds.put(
                MAInteractionType.SUPPORT,
                Map.of(
                        MovingAverageLength.LOWEST, 10.0, // MA5
                        MovingAverageLength.LOW, 7.0,
                        MovingAverageLength.MEDIUM, 5.0,
                        MovingAverageLength.HIGH, 2.0
                        // MA5 (HIGHEST) => anchor, no check
                        ));

        thresholds.put(
                MAInteractionType.RESISTANCE,
                Map.of(
                        MovingAverageLength.LOW, 2.0,
                        MovingAverageLength.MEDIUM, 5.0,
                        MovingAverageLength.HIGH, 7.0,
                        MovingAverageLength.HIGHEST, 10.0
                        // MA5 (LOWEST) => anchor, no check
                        ));
    }

    public static Optional<Double> getThreshold(
            MAInteractionType type, MovingAverageLength maLength) {
        return Optional.ofNullable(thresholds.getOrDefault(type, Map.of()).get(maLength));
    }
}
