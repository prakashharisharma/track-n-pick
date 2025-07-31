package com.example.service.utils;

import com.example.data.transactional.entities.StockPrice;
import com.example.service.SupportResistanceZones;
import java.util.Comparator;
import java.util.List;

public class SupportResistanceZoneUtils {

    public static class Zone {
        private final double start;
        private final double end;

        public Zone(double start, double end) {
            this.start = start;
            this.end = end;
        }

        public double getStart() {
            return start;
        }

        public double getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "Zone{" + "start=" + start + ", end=" + end + '}';
        }
    }

    public static SupportResistanceZones calculateSupportResistanceZones(StockPrice stockPrice) {
        List<Double> highs =
                List.of(
                        stockPrice.getPrevHigh(),
                        stockPrice.getPrev2High(),
                        stockPrice.getPrev3High(),
                        stockPrice.getPrev4High(),
                        stockPrice.getPrev5High(),
                        stockPrice.getPrev6High());

        List<Double> lows =
                List.of(
                        stockPrice.getPrevLow(),
                        stockPrice.getPrev2Low(),
                        stockPrice.getPrev3Low(),
                        stockPrice.getPrev4Low(),
                        stockPrice.getPrev5Low(),
                        stockPrice.getPrev6Low());

        Zone resistanceZone = getExtremeZone(highs, true); // true = upper extremes
        Zone supportZone = getExtremeZone(lows, false); // false = lower extremes

        return SupportResistanceZones.builder()
                .resistance(resistanceZone)
                .support(supportZone)
                .build();
    }

    private static Zone getExtremeZone(List<Double> levels, boolean pickTop) {
        List<Double> sorted =
                levels.stream()
                        .filter(v -> v != null && v > 0)
                        .sorted(pickTop ? Comparator.reverseOrder() : Comparator.naturalOrder())
                        .limit(3) // take top 3 or bottom 3
                        .sorted()
                        .toList();

        if (sorted.isEmpty()) return new Zone(0, 0);

        double start = roundToHalf(sorted.get(0));
        double end = roundToHalf(sorted.get(sorted.size() - 1));
        return new Zone(start, end);
    }

    private static double roundToHalf(double value) {
        return Math.round(value * 2) / 2.0;
    }
}
