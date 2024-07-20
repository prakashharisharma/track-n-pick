package com.example.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class MiscUtil {

	private static int min = 10;
	private static int max = 500;

	public String formatDouble(double value) {

		DecimalFormat dec = new DecimalFormat("#0.00");

		return dec.format(value);
	}

	public long getInterval() {

		return new Random().nextInt(max - min + 1) + min;
	}
	
	public void delay() throws InterruptedException {
		Thread.sleep(50000);
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

		String existingMonth = modifiedDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase();

		if (resultsMonths.contains(existingMonth)) {
			return false;
		} else if (resultsMonths.contains(month)) {
			return true;
		}

		return false;
	}

	public LocalDate currentDate() {
		return LocalDate.now();
	}

	public LocalDate currentYearFirstDay() {

		LocalDate yearFirstdate = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());

		return yearFirstdate;
	}

	public LocalDate currentYearLastDay() {

		LocalDate yearLasttdate = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

		return yearLasttdate;
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
		LocalDate currentDatePrevYear  = this.currentDate().minusMonths(12);
		
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
