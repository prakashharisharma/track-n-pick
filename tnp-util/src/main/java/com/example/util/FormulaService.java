package com.example.util;

import java.util.ArrayList;
import java.util.List;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormulaService {

    private static final int RISK_REWARD_RATIO = 2;

    private static final double MIN_TARGET_PER = 10.0;

    public static final double EPSILON_PER = 0.01;
    // public static final double EPSILON_PER = 0.1618;
    @Autowired private MiscUtil miscUtil;

    public double calculateRs(double averageGain, double averageLoss) {

        return (averageLoss == 0.00) ? 0.00 : (averageGain / averageLoss);
        // double rs = averageGain / averageLoss;

        // return rs;
    }

    public double calculateRsi(double rs) {

        double rsi = 50.00;
        try {
            rsi = (100 - (100 / (1 + rs)));
        } catch (Exception e) {
            rsi = 50.00;
        }
        return rsi;
    }

    public double calculateSmoothedRsi(double smoothedRs) {
        double smoothedRsi = this.calculateRsi(smoothedRs);

        return smoothedRsi;
    }

    public double calculateSmoothedRs(
            double prevAverageGain,
            double prevAverageLoss,
            double currentGain,
            double currentLoss) {

        double smoothedRs =
                (((prevAverageGain * 13) + currentGain) / 14)
                        / (((prevAverageLoss * 13) + currentLoss) / 14);

        return smoothedRs;
    }

    public double calculateStochasticOscillatorValue(
            double currentPrice, double low14, double high14) {
        double stochasticOscillatorValue = 0.00;

        if ((high14 - low14 != 0)) {
            stochasticOscillatorValue = ((currentPrice - low14) / (high14 - low14)) * 100;
        } else {
            stochasticOscillatorValue = 0.00;
        }
        return stochasticOscillatorValue;
    }

    public double calculateRateOfChange(double current, double previous) {
        double rateOfChange = 0.00;
        if (previous != 0) {
            rateOfChange = ((current - previous) / previous) * 100;
        } else {
            rateOfChange = current;
        }
        return rateOfChange;
    }

    public double calculatePe(double currentPrice, double eps) {

        double pe = 0.0;

        if (eps != 0.00) {
            pe = currentPrice / eps;
        }

        return pe;
    }

    public double calculatePb(double currentPrice, double bookValue) {

        double pb = 0.0;

        if (bookValue != 0.00) {
            pb = currentPrice / bookValue;
        }

        return pb;
    }

    /**
     * Calculates the percentage that a given fraction represents of a base number.
     *
     * @param baseNumber the total or reference value (denominator)
     * @param fraction the part of the base number to be expressed as a percentage (numerator)
     * @return the percentage value representing (fraction / baseNumber) * 100
     * @throws IllegalArgumentException if baseNumber is zero
     */
    public double calculatePercentage(double baseNumber, double fraction) {
        if (baseNumber == 0) {
            throw new IllegalArgumentException(
                    "Base number cannot be zero to avoid division by zero.");
        }
        return (fraction / baseNumber) * 100;
    }

    /**
     * Calculates the fraction (part) of a given base number based on a percentage rate.
     *
     * @param baseNumber the total or reference value
     * @param rate the percentage rate (e.g., 25 for 25%)
     * @return the fraction representing (rate / 100) * baseNumber
     */
    public double calculateFraction(double baseNumber, double rate) {
        if (rate < 0 || rate > 100) {
            throw new IllegalArgumentException("Rate should be between 0 and 100.");
        }
        return (rate / 100) * baseNumber;
    }

    /**
     * Calculates the percentage change from num1 to num2.
     *
     * <p>The formula used is: ((num2 - num1) / num1) * 100. If num1 is zero, the method returns 0.0
     * to avoid division by zero.
     *
     * @param num1 the initial value (base value)
     * @param num2 the new value
     * @return the percentage change from num1 to num2, or 0.0 if num1 is zero
     */
    public double calculateChangePercentage(double num1, double num2) {
        if (num1 == 0.0) {
            return 0.0;
        }
        return ((num2 - num1) / num1) * 100;
    }

    /**
     * Applies a percentage change to the given number.
     *
     * <p>For example, if num = 100 and percentChange = 10, the result will be 110 (100 increased by
     * 10%).
     *
     * @param num the original number
     * @param percentChange the percentage change to apply (can be positive or negative)
     * @return the new value after applying the percentage change
     */
    public double applyPercentChange(double num, double percentChange) {
        return num * (1 + percentChange / 100);
    }

    public long calculateOBV(
            long prevOBV, double prevClose, double currentClose, long currentVolume) {
        long OBV = 0;

        if (currentClose > prevClose) {
            OBV = prevOBV + currentVolume;
        } else if (currentClose < prevClose) {
            OBV = prevOBV - currentVolume;
        } else {
            OBV = prevOBV;
        }

        return OBV;
    }

    public double calculateAverage(double num1, double num2) {

        return (num1 + num2) / 2;
    }

    public double getEMAMultiplier(int timePeriod) {

        double multiplier = (2.0 / ((double) timePeriod + 1.0));

        return miscUtil.formatDouble(multiplier, "0000");
    }

    public double calculateEMA(double close, double prevEMA, int timePeriod) {

        double ema;

        double K = this.getEMAMultiplier(timePeriod);

        // System.out.println("(("+close+"  * "+K+") + ("+prevEMA +" * (1 - "+K+")))");

        ema = ((close * K) + (prevEMA * (1 - K)));

        return ema;
    }

    public double calculateXirr(List<Transaction> transactions) {

        double xirr = 0.00;

        try {
            xirr = new Xirr(transactions).xirr() * 100;
        } catch (Exception e) {
            System.out.println("An error occured while calculating xirr");
            e.printStackTrace();
        }

        return xirr;
    }

    public double calculateCagr(double initialCapital, double currentCapital, long years) {

        if (years <= 0) {
            years = 1;
        }

        double expYears = 1 / years;

        return ((Math.pow((currentCapital / initialCapital), expYears) - 1) * 100);
    }

    /**
     * TR is the greater of the current high - current low, current high - previous close, or
     * current low - previous close
     *
     * @param high
     * @param low
     * @param prevClose
     * @return
     */
    public double calculateTR(double high, double low, double prevClose) {
        double tr1 = high - low;
        double tr2 = Math.abs(high - prevClose);
        double tr3 = Math.abs(low - prevClose);
        return Math.max(tr1, Math.max(tr2, tr3));
    }

    /**
     * +DM = current high - previous high.
     *
     * @param high
     * @param prevHigh
     * @param low
     * @param prevLow
     * @return
     */
    public double calculatePlusDM(double high, double prevHigh, double low, double prevLow) {
        double upMove = high - prevHigh;
        double downMove = prevLow - low;
        if (upMove > downMove && upMove > 0) {
            return upMove;
        } else {
            return 0;
        }
    }

    public double calculatePlusDi(double smoothedPlusDm, double atr) {
        if (atr == 0.00) {
            return 0.00;
        }
        return 100 * Math.abs(smoothedPlusDm / atr);
    }

    public double calculateMinusDi(double smoothedMinusDm, double atr) {
        if (atr == 0.00) {
            return 0.00;
        }
        return 100 * Math.abs(smoothedMinusDm / atr);
    }

    /**
     * -DM = previous low - current low
     *
     * @param high
     * @param prevHigh
     * @param low
     * @param prevLow
     * @return
     */
    public double calculateMinusDM(double high, double prevHigh, double low, double prevLow) {
        double downMove = prevLow - low;
        double upMove = high - prevHigh;
        if (downMove > upMove && downMove > 0) {
            return downMove;
        } else {
            return 0;
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Average_true_range ((prevDaySmoothedAverage * (n-1) +
     * close))/n; NOTE :- The first SmoothedAverage is calculated using the arithmetic mean formula
     * or simple moving average
     *
     * @param prevDaySmoothedAverage
     * @param current
     * @return
     */
    public double calculateSmoothedMovingAverage(
            double prevDaySmoothedAverage, double current, int days) {

        return (((prevDaySmoothedAverage * (days - 1)) + current)) / days;
    }

    public long calculateSmoothedMovingAverage(
            long prevDaySmoothedAverage, long current, int days) {

        return (((prevDaySmoothedAverage * (days - 1)) + current)) / days;
    }

    public double calculateDX(double plusDI, double minusDI) {
        return 100 * Math.abs(plusDI - minusDI) / (plusDI + minusDI);
    }

    public double calculateHistogram(double macd, double signal) {
        return macd - signal;
    }

    public double floorRound(double num) {
        return Math.floor(num * 10) / 10.0;
    }

    public boolean isEpsilonEqual(double num1, double num2) {
        return this.isEpsilonEqual(num1, num2, FibonacciRatio.RATIO_23_6);
    }

    public boolean isEpsilonEqual(double num1, double num2, double ratio) {

        double epsilonPer = 0.0786;

        double smallNum = num1;
        double bigNum = num2;

        if (num1 > num2) {
            smallNum = num2;
            bigNum = num1;
        }

        double epsilon = this.calculateFraction(smallNum, ratio);

        if (Math.abs(bigNum - smallNum) <= epsilon) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    public List<Double> fibonacciRetracements(double low, double high) {

        List<Double> retracements = new ArrayList<>();

        double move = high - low;

        retracements.add(high - this.calculateFraction(move, 23.6));
        retracements.add(high - this.calculateFraction(move, 38.2));
        retracements.add(high - this.calculateFraction(move, 50));
        retracements.add(high - this.calculateFraction(move, 61.8));

        return retracements;
    }

    public List<Double> fibonacciExtensions(double low, double high) {

        List<Double> extensions = new ArrayList<>();

        double move = high - low;

        extensions.add(high + this.calculateFraction(move, 23.6));
        extensions.add(high + this.calculateFraction(move, 38.2));
        extensions.add(high + this.calculateFraction(move, 50));
        extensions.add(high + this.calculateFraction(move, 61.8));

        return extensions;
    }

    public List<Double> fibonacciExtensions(double low, double high, double entry) {

        List<Double> extensions = new ArrayList<>();

        double move = high - low;

        extensions.add(entry + this.calculateFraction(move, 23.6));
        extensions.add(entry + this.calculateFraction(move, 38.2));
        extensions.add(entry + this.calculateFraction(move, 50));
        extensions.add(entry + this.calculateFraction(move, 61.8));

        return extensions;
    }

    public double calculateTarget(double entryPrice, double stopLoss, double riskRewardRatio) {

        return (riskRewardRatio * (entryPrice - stopLoss)) + entryPrice;
        /*
        if( this.calculateChangePercentage(entryPrice, target) < MIN_TARGET_PER ){
        	target = entryPrice + this.calculatePercentage(entryPrice, MIN_TARGET_PER);
        }
        return target;
        	*/
    }

    public boolean inRange(double min, double max, double num) {
        return Math.max(min, num) == Math.min(num, max);
    }

    public double ceilToNearestHalf(double value) {
        return Math.ceil(value * 2) / 2.0;
    }

    public double ceilToNearestQuarter(double value) {
        return Math.ceil(value * 4) / 4.0;
    }
}
