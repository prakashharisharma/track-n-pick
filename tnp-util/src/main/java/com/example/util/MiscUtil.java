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

    private static int min = 2837;
    private static int max = 8897;

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
        System.out.println("sleeping " + interval);
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

    public LocalDate currentDate() {
        //return LocalDate.of(2025, 04, 11);
        return LocalDate.now();
    }

    public LocalDate currentYearFirstDay() {

        LocalDate yearFirstdate = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());

        return yearFirstdate;
    }

    public LocalDate previousMonthLastDay() {

        LocalDate previousMonthLastDay =
                LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        return previousMonthLastDay;
    }

    public LocalDate previousMonthFirstDay() {

        LocalDate previousMonthFirstDay =
                LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());

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

        LocalDate yearLasttdate = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

        return yearLasttdate;
    }

    public LocalDate previousWeekLastDay() {

        LocalDate previousWeekLastDay =
                LocalDate.now().with(nextOrSame(DayOfWeek.SUNDAY)).minusWeeks(1);

        return previousWeekLastDay;
    }

    public LocalDate previousWeekFirstDay() {

        LocalDate previousWeekFirstDay =
                LocalDate.now().with(previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);

        return previousWeekFirstDay;
    }

    public LocalDate currentWeekFirstDay() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    public LocalDate currentMonthFirstDay() {

        LocalDate monthFirstDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());

        return monthFirstDate;
    }

    public LocalDate currentMonthLastDay() {

        LocalDate monthLastDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

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
