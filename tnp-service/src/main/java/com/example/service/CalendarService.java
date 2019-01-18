package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.HolidayCalendar;
import com.example.repo.master.HolidayCalendarRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class CalendarService {

	@Autowired
	private HolidayCalendarRepository holidayCalendarRepository;
	
	@Autowired
	private MiscUtil miscUtil;
	
	public boolean isHoliday(LocalDate date) {
		
		boolean isHoliday = false;
		
		HolidayCalendar holidaycalendar = holidayCalendarRepository.findByHolidayDate(date);
		
		if(holidaycalendar != null) {
			isHoliday = true;
		}
		
		return isHoliday;
	}
	
	public LocalDate previousWorkingDay() {
		
		LocalDate currentDate = miscUtil.currentDate();
		
		return previousWorkingDay(currentDate);
	}
	
	public List<HolidayCalendar> holidays(){
		
		LocalDate fromDate = LocalDate.of(2018,Month.JANUARY,01);
		
		LocalDate yearLastdate = LocalDate.of(2019,Month.DECEMBER,31);
		return holidayCalendarRepository.findByHolidayDateBetween(fromDate, yearLastdate);
		//return holidayCalendarRepository.findByHolidayDateBetween(miscUtil.currentDate(), yearLastdate);
	}
	
	public LocalDate previousWorkingDay(LocalDate currentDateParam) {
		
		LocalDate currentDate = currentDateParam;
		
		DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
		
		int date;

		if (DayOfWeek.MONDAY == dayOfWeek) {
			date = currentDate.getDayOfMonth() - 3;
			
			currentDate = currentDate.minusDays(3);
			
		} else if (DayOfWeek.SUNDAY == dayOfWeek) {
			date = currentDate.getDayOfMonth() - 2;
			currentDate = currentDate.minusDays(2);
		} else {
			date = currentDate.getDayOfMonth() - 1;
			currentDate = currentDate.minusDays(1);
		}

		
		//System.out.println(currentDate);
		
		/*if (date <= 0) {

			System.out.println(date);
			
			currentDate = currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
	
			if (currentDate.getDayOfWeek().getValue() > 5) {
				currentDate = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 5);
			}
			
		
		}*/
		
		//System.out.println(currentDate);
		
		if(this.isHoliday(currentDate)) {
			
			return previousWorkingDay(currentDate);
		}else {
			
			return currentDate;
		}
		
	}
	
}
