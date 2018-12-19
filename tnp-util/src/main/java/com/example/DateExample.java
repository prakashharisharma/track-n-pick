package com.example;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class DateExample {

	public static void main(String[] args) {

		LocalDate localDate = LocalDate.now();

		System.out.println(localDate.toString());
		
		DayOfWeek dayOfWeek = localDate.getDayOfWeek();
		
		int date;
		
		if(DayOfWeek.MONDAY == dayOfWeek) {
			date = localDate.getDayOfMonth() - 3;
		}else if(DayOfWeek.SUNDAY == dayOfWeek) {
			date = localDate.getDayOfMonth() - 2;
		}else {
			date = localDate.getDayOfMonth() - 1;
		}
		
		int year = localDate.getYear();

		String month = localDate.getMonth().getDisplayName( TextStyle.SHORT , Locale.US ).toUpperCase() ;
		
		if(date <= 0) {
			
			 localDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
			 
			 if (localDate.getDayOfWeek().getValue() > 5) {
			        localDate = localDate.minusDays(localDate.getDayOfWeek().getValue() - 5);
			 }
			 
			 date = localDate.getDayOfMonth();
			 
			 month = localDate.getMonth().getDisplayName( TextStyle.SHORT , Locale.US ).toUpperCase() ;
			 
			 year = localDate.getYear();
		}

		
		System.out.println(date+month+year);
		
		
	}
}
