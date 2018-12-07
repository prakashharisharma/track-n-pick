package com.example.util;

import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;


public class TestMailSSL {

  
    public static void main(String[] args) {   
    	
    	LocalDate dateBefore;
    	LocalDate dateAfter;
    	long daysBetween = DAYS.between(LocalDate.now().minusDays(20), LocalDate.now());
    	
    	System.out.println(daysBetween);
    	
   }

}