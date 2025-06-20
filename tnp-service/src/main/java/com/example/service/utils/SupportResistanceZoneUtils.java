package com.example.service.utils;

import com.example.data.transactional.entities.StockPrice;
import com.example.service.SupportResistanceZones;
import java.util.ArrayList;
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

        List<Double> opens =
                List.of(
                        stockPrice.getOpen(),
                        stockPrice.getPrevOpen(),
                        stockPrice.getPrev2Open(),
                        stockPrice.getPrev3Open(),
                        stockPrice.getPrev4Open(),
                        stockPrice.getPrev5Open());

        System.out.println("open: " + opens);

        List<Double> highs =
                List.of(
                        stockPrice.getHigh(),
                        stockPrice.getPrevHigh(),
                        stockPrice.getPrev2High(),
                        stockPrice.getPrev3High(),
                        stockPrice.getPrev4High(),
                        stockPrice.getPrev5High());

        System.out.println("high: " + highs);

        List<Double> lows =
                List.of(
                        stockPrice.getLow(),
                        stockPrice.getPrevLow(),
                        stockPrice.getPrev2Low(),
                        stockPrice.getPrev3Low(),
                        stockPrice.getPrev4Low(),
                        stockPrice.getPrev5Low());
        System.out.println("low: " + lows);

        List<Double> closes =
                List.of(
                        stockPrice.getClose(),
                        stockPrice.getPrevClose(),
                        stockPrice.getPrev2Close(),
                        stockPrice.getPrev3Close(),
                        stockPrice.getPrev4Close(),
                        stockPrice.getPrev5Close());

        System.out.println("close: " + closes);

        Zone resistanceZone = getClusteredZone(highs);
        Zone supportZone = getClusteredZone(lows);

        System.out.println("supportZone: " + supportZone);

        System.out.println("resistanceZone: " + resistanceZone);

        return SupportResistanceZones.builder()
                .support(supportZone)
                .resistance(resistanceZone)
                .build();
    }

    private static Zone getClusteredZone(List<Double> levels) {
        if (levels.isEmpty()) return new Zone(0, 0);

        List<Double> sorted = new ArrayList<>(levels);
        sorted.sort(Double::compare);

        double avg = sorted.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double zoneWidth = avg * 0.02; // 2% of average price

        int maxCount = 0;
        double bestStart = sorted.get(0);
        double bestEnd = sorted.get(0);

        for (int i = 0; i < sorted.size(); i++) {
            double start = sorted.get(i);
            List<Double> cluster = new ArrayList<>();
            cluster.add(start);

            for (int j = i + 1; j < sorted.size(); j++) {
                double end = sorted.get(j);
                if (end - start <= zoneWidth) {
                    cluster.add(end);
                } else {
                    break;
                }
            }

            if (cluster.size() > maxCount) {
                maxCount = cluster.size();
                bestStart = cluster.get(0);
                bestEnd = cluster.get(cluster.size() - 1);
            }
        }

        return new Zone(roundToHalf(bestStart), roundToHalf(bestEnd));
    }

    private static double roundToHalf(double value) {
        return Math.round(value * 2) / 2.0;
    }
}
