package com.example.util;

import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;

@Deprecated
public class TestMailSSL {

  
    public static void main(String[] args) {   
    	
    	LocalDate dateBefore;
    	LocalDate dateAfter;
    	long daysBetween = DAYS.between(LocalDate.now().minusDays(20), LocalDate.now());
    	
    	
    	System.out.println(LocalDate.now().getMonthValue());
    	
    	System.out.println(daysBetween);
    	
   }

}