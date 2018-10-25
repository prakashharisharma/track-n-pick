package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	
	private static int times[] = {1000,2000,1500,2500,1200,1800,500,2100,3000,3500,3300,1750,5000,4700,4250,2900,6500,7000};
	
	
	@Override
	public void run(String... arg0) throws InterruptedException {}

	private static long getInterval() {
		
		int no = getRandomNumberInRange(1, 20);
		
		if(no > 17) {
			
			no = no%17;
			
			return times[no];
			
		}else {
			
			return times[no];
			
		}
	}
	
	private static int getRandomNumberInRange(int min, int max) {
		return (int)(Math.random() * ((max - min) + 1)) + min;
	}
	
}
