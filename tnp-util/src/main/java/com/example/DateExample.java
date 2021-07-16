package com.example;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;


public class DateExample {

	public static void main(String[] args) throws ParseException {

			String dateStr_1="15-JAN-2019";
		
		   DateTimeFormatter formatter_1=new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH);
		   LocalDate localDate_1= LocalDate.parse(dateStr_1,formatter_1);
		   
		   System.out.println("Input String with value: "+dateStr_1);
		   System.out.println("Converted Date in default ISO format: "+localDate_1+"\n");
		
		   Date bhavDate = new SimpleDateFormat("dd-MMM-yyyy").parse(dateStr_1);
		   Timestamp timestamp = new java.sql.Timestamp(bhavDate.getTime());
		
		   System.out.println(bhavDate);
		   System.out.println(timestamp);
		   
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
