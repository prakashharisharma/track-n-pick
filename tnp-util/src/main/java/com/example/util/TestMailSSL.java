package com.example.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.DAYS;

@Deprecated
public class TestMailSSL {

  
    public static void main(String[] args) {   

    	LocalDate date = LocalDate.now();
    
    	DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    	String text = date.format(formatters);
    
    	
   }

}