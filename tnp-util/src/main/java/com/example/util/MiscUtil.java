package com.example.util;

import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class MiscUtil {

    private static int min = 848;
    private static int max = 1839;

    public double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String formatDouble(double value) {

        DecimalFormat dec = new DecimalFormat("#0.00");

        return dec.format(value);
    }

    public double formatDouble(double value, String decimalZeros) {

        DecimalFormat dec = new DecimalFormat("#0." + decimalZeros);

        return Double.parseDouble(dec.format(value));
    }

    public long getInterval() {

        return new Random().nextInt(max - min + 1) + min;
    }

    public void delay() throws InterruptedException {
        long interval = this.getInterval();
        Thread.sleep(interval);
    }

    public void delay(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    private int getRandomNumberInRange(int min, int max) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }

    // Return true if c is between a and b.
    public boolean isBetween(double a, double b, double c) {

        if (c < a || c > b) {
            return false;
        }

        return b > a ? c > a && c < b : c > b && c < a;
    }

    public boolean isResultMonth(LocalDate modifiedDate) {

        List<String> resultsMonths = new ArrayList<>();

        resultsMonths.add("FEB");
        resultsMonths.add("MAY");
        resultsMonths.add("AUG");
        resultsMonths.add("NOV");

        LocalDate localDate = LocalDate.now();

        String existingMonth =
                modifiedDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

        String month =
                localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

        if (resultsMonths.contains(existingMonth)) {
            return false;
        } else if (resultsMonths.contains(month)) {
            return true;
        }

        return false;
    }

    public boolean isBackTest() {
        // return LocalDate.now().isAfter(this.currentDate());
        return false;
    }

    public LocalDate currentDate() {

        //  return LocalDate.of(2026, 01, 16);
        return LocalDate.now();
        //    return LocalDate.now().minusMonths(1); // OCT 25
        //  return LocalDate.now().minusMonths(2); // SEP 25
        //    return LocalDate.now().minusMonths(3); //AUG 25
        //   return LocalDate.now().minusMonths(4); //JUL 25
        //     return LocalDate.now().minusMonths(5); //JUM 25
        //    return LocalDate.now().minusMonths(6); //MAY 25
        //   return LocalDate.now().minusMonths(7); //APR 25
        //  return LocalDate.now().minusMonths(8); //MAR 25
        // return LocalDate.now().minusMonths(9); //FEB 25
        //  return LocalDate.now().minusMonths(10); //JAN 25
        //  return LocalDate.now().minusMonths(11); //DEC 24
        //  return LocalDate.now().minusMonths(12); //NOV 24
        //  return LocalDate.now().minusMonths(13); //OCT 24

        //  return LocalDate.now().minusMonths(14); //SEP 24
        // return LocalDate.now().minusMonths(15); //AUG 24
        //  return LocalDate.now().minusMonths(16); //JUL 24
        // return LocalDate.now().minusMonths(17); //JUN 24
        //  return LocalDate.now().minusMonths(18); //MAY 24
        //  return LocalDate.now().minusMonths(19); // APR 24
        //  return LocalDate.now().minusMonths(20); //MAR 24
        //  return LocalDate.now().minusMonths(21); //FEB 24
        //  return LocalDate.now().minusMonths(22); //JAN 24

        // return LocalDate.now().minusMonths(23); //DEC 23
        //  return LocalDate.now().minusMonths(24); //NOV 23
        //  return LocalDate.now().minusMonths(25); //OCT 23
        //  return LocalDate.now().minusMonths(26); //SEP 23
        //  return LocalDate.now().minusMonths(27); //AUG 23
        //  return LocalDate.now().minusMonths(28); //JUL 23
        // return LocalDate.now().minusMonths(29); //JUN 23
        //  return LocalDate.now().minusMonths(30); //MAY 23
        // return LocalDate.now().minusMonths(31); //APR 23
        // return LocalDate.now().minusMonths(32); //MAR 23
        //   return LocalDate.now().minusMonths(33); //FEB 23
        //  return LocalDate.now().minusMonths(34); //JAN 23

        //   return LocalDate.now().minusMonths(35); //DEC 22
        //  return LocalDate.now().minusMonths(36); //NOV 22
        //  return LocalDate.now().minusMonths(37); //OCT 22
        //  return LocalDate.now().minusMonths(38); //SEP 22
        //  return LocalDate.now().minusMonths(39); //AUG 22
        // return LocalDate.now().minusMonths(40); //JUL 22
        // return LocalDate.now().minusMonths(41); //JUN 22
        // return LocalDate.now().minusMonths(42); //MAY 22
        // return LocalDate.now().minusMonths(43); //APR 22
        // return LocalDate.now().minusMonths(44); //MAR 22
        //  return LocalDate.now().minusMonths(45); //FEB 22
        //  return LocalDate.now().minusMonths(46); //JAN 22
    }

    public LocalDate currentYearFirstDay() {

        LocalDate yearFirstdate = this.currentDate().with(TemporalAdjusters.firstDayOfYear());

        return yearFirstdate;
    }

    public LocalDate previousMonthLastDay() {

        LocalDate previousMonthLastDay =
                this.currentDate().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        return previousMonthLastDay;
    }

    public LocalDate previousMonthFirstDay() {

        LocalDate previousMonthFirstDay =
                this.currentDate().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());

        return previousMonthFirstDay;
    }

    public LocalDate currentQuarterFirstDay() {
        final Month yearStart = Month.JANUARY;
        final int yearStartValue = yearStart.getValue();
        int monthInQuarter = (this.currentDate().getMonthValue() + 12 - yearStartValue) % 3;
        LocalDate currentQuarterStart =
                this.currentDate().withDayOfMonth(1).minusMonths(monthInQuarter);

        return currentQuarterStart;
    }

    public LocalDate quarterFirstDay(LocalDate date) {
        int month = date.getMonthValue();
        Month firstMonthOfQuarter;

        if (month <= 3) {
            firstMonthOfQuarter = Month.JANUARY;
        } else if (month <= 6) {
            firstMonthOfQuarter = Month.APRIL;
        } else if (month <= 9) {
            firstMonthOfQuarter = Month.JULY;
        } else {
            firstMonthOfQuarter = Month.OCTOBER;
        }

        return LocalDate.of(date.getYear(), firstMonthOfQuarter, 1);
    }

    public LocalDate quarterLastDay(LocalDate date) {
        int month = date.getMonthValue();
        Month lastMonthOfQuarter;

        if (month <= 3) {
            lastMonthOfQuarter = Month.MARCH;
        } else if (month <= 6) {
            lastMonthOfQuarter = Month.JUNE;
        } else if (month <= 9) {
            lastMonthOfQuarter = Month.SEPTEMBER;
        } else {
            lastMonthOfQuarter = Month.DECEMBER;
        }

        // Get the last day of the determined month
        return YearMonth.of(date.getYear(), lastMonthOfQuarter).atEndOfMonth();
    }

    public static LocalDate yearLastDay(LocalDate date) {
        return LocalDate.of(date.getYear(), Month.DECEMBER, 31);
    }

    public static LocalDate yearFirstDay(LocalDate date) {
        return LocalDate.of(date.getYear(), Month.JANUARY, 01);
    }

    public LocalDate previousQuarterLastDay() {

        LocalDate previousQuarterLastDay = this.currentQuarterFirstDay().minusDays(1);

        return previousQuarterLastDay;
    }

    public LocalDate previousQuarterFirstDay() {

        LocalDate previousQuarterStart = this.currentQuarterFirstDay().minusMonths(3);

        return previousQuarterStart;
    }

    public LocalDate currentYearLastDay() {

        LocalDate yearLasttdate = this.currentDate().with(TemporalAdjusters.lastDayOfYear());

        return yearLasttdate;
    }

    public LocalDate previousWeekLastDay() {

        LocalDate previousWeekLastDay =
                this.currentDate().with(nextOrSame(DayOfWeek.SUNDAY)).minusWeeks(1);

        return previousWeekLastDay;
    }

    public LocalDate previousWeekFirstDay() {

        LocalDate previousWeekFirstDay =
                this.currentDate().with(previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);

        return previousWeekFirstDay;
    }

    public LocalDate currentWeekFirstDay() {
        return this.currentDate().with(DayOfWeek.MONDAY);
    }

    public LocalDate nextWeekFirstDay() {
        // Go to next week's Monday
        return this.currentDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    public LocalDate nextMonthFirstDay() {
        // Go to next month's first day
        return this.currentDate().with(TemporalAdjusters.firstDayOfNextMonth());
    }

    public LocalDate currentMonthFirstDay() {

        LocalDate monthFirstDate = this.currentDate().with(TemporalAdjusters.firstDayOfMonth());

        return monthFirstDate;
    }

    public LocalDate currentMonthLastDay() {

        LocalDate monthLastDate = this.currentDate().with(TemporalAdjusters.lastDayOfMonth());

        return monthLastDate;
    }

    public LocalDate currentDatePrevYear() {
        LocalDate currentDatePrevYear = this.currentDate().minusMonths(12);

        return currentDatePrevYear;
    }

    public LocalDate currentFinYearFirstDay() {

        LocalDate FinYearFirstdate;

        if (this.currentDate().getMonthValue() < 4) {
            FinYearFirstdate = LocalDate.of(this.currentDate().getYear() - 1, Month.APRIL, 01);
        } else {
            FinYearFirstdate = LocalDate.of(this.currentDate().getYear(), Month.APRIL, 01);
        }

        return FinYearFirstdate;
    }

    public LocalDate currentFinYearLastDay() {
        LocalDate FinYearLasttdate;

        if (this.currentDate().getMonthValue() < 4) {
            FinYearLasttdate = LocalDate.of(this.currentDate().getYear(), Month.MARCH, 31);
        } else {
            FinYearLasttdate = LocalDate.of(this.currentDate().getYear() + 1, Month.MARCH, 31);
        }

        return FinYearLasttdate;
    }
}
