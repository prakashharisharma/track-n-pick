package com.example.service;

public class CrossOverUtil {

    /**
     * A bullish crossover occurs when the shorter / fast moving average crosses above the longer /
     * slow moving average.
     *
     * @param prevFastAvg
     * @param prevSlowAvg
     * @param fastAvg
     * @param slowAvg
     * @return
     */
    public static boolean isFastCrossesAboveSlow(
            double prevFastAvg, double prevSlowAvg, double fastAvg, double slowAvg) {
        if (prevSlowAvg >= prevFastAvg) {
            if (fastAvg > slowAvg) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Bearish
     *
     * @param prevFastAvg
     * @param prevSlowAvg
     * @param fastAvg
     * @param slowAvg
     * @return
     */
    public static boolean isSlowCrossesBelowFast(
            double prevFastAvg, double prevSlowAvg, double fastAvg, double slowAvg) {

        if (prevFastAvg >= prevSlowAvg) {
            if (fastAvg < slowAvg) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
