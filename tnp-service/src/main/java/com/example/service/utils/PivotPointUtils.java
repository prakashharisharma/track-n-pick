package com.example.service.utils;

import com.example.data.transactional.entities.StockPrice;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PivotPointUtils {

    public static PivotLevels calculate(double high, double low, double close) {


        double pivot = (high + low + close) / 3.0;

        double r1 = (2 * pivot) - low;
        double s1 = (2 * pivot) - high;
        double r2 = pivot + (high - low);
        double s2 = pivot - (high - low);
        double r3 = high + 2 * (pivot - low);
        double s3 = low - 2 * (high - pivot);

        return new PivotLevels(pivot, r1, r2, r3, s1, s2, s3);
    }

    @Getter
    @AllArgsConstructor
    public static class PivotLevels {
        private final double pivot;
        private final double resistance1;
        private final double resistance2;
        private final double resistance3;
        private final double support1;
        private final double support2;
        private final double support3;
    }
}
