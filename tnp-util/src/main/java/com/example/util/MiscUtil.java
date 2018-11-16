package com.example.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class MiscUtil {

	private static int times[] = { 1000, 2000, 1500, 2500, 1200, 1800, 500, 2100, 3000, 3500, 3300, 1750, 5000, 4700,
			4250, 2900, 6500, 7000 };

	public String formatDouble(double value) {

		DecimalFormat dec = new DecimalFormat("#0.00");

		return dec.format(value);
	}

	public long getInterval() {

		int no = getRandomNumberInRange(1, 20);

		if (no > 17) {

			no = no % 17;

			return times[no];

		} else {

			return times[no];

		}
	}

	private int getRandomNumberInRange(int min, int max) {
		return (int) (Math.random() * ((max - min) + 1)) + min;
	}

	// Return true if c is between a and b.
	public boolean isBetween(double a, double b, double c) {
		
		if(c < a || c > b ) {
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

		if(resultsMonths.contains(existingMonth)) {
			return false;
		}else if (resultsMonths.contains(month)) {
			return true;
		}

		return false;
	}
	
}
