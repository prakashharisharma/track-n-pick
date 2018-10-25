package com.example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateExample {

	public static void main(String[] args) {
		
		LocalDate ld = LocalDate.now();
		
		
		System.out.println(ld);
		
		//Date
		System.out.println(ld.getDayOfMonth());
		
		DayOfWeek dayOfWeek = ld.getDayOfWeek();
		
		int day;
		
		if(DayOfWeek.MONDAY == dayOfWeek) {
			day = ld.getDayOfMonth() - 3;
		}else if(DayOfWeek.SUNDAY == dayOfWeek) {
			day = ld.getDayOfMonth() - 2;
		}else {
			day = ld.getDayOfMonth() - 1;
		}
		
		System.out.println(day);
		//Year
		System.out.println(ld.getYear());
		
		

		String output = ld.getMonth().getDisplayName( TextStyle.SHORT , Locale.US ) ;
		
		System.out.println(output.toUpperCase());
		
		//Day
		System.out.println(ld.getDayOfWeek().name());
		
		

		
		
	}
}
