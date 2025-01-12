package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
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

		if (holidaycalendar != null) {
			isHoliday = true;
		}

		return isHoliday;
	}

	public LocalDate previousWorkingDay() {

		LocalDate currentDate = miscUtil.currentDate();

		return previousWorkingDay(currentDate);
	}

	public List<HolidayCalendar> holidays() {

		LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 01);

		LocalDate yearLastdate = LocalDate.of(2019, Month.DECEMBER, 31);
		return holidayCalendarRepository.findByHolidayDateBetween(fromDate, yearLastdate);

	}

	public LocalDate previousWorkingDay(LocalDate currentDateParam) {

		LocalDate currentDate = currentDateParam;

		DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

		if (DayOfWeek.MONDAY == dayOfWeek) {

			currentDate = currentDate.minusDays(3);

		} else if (DayOfWeek.SUNDAY == dayOfWeek) {

			currentDate = currentDate.minusDays(2);
		} else {

			currentDate = currentDate.minusDays(1);
		}

		if (this.isHoliday(currentDate)) {

			return previousWorkingDay(currentDate);
		}
		return currentDate;


	}

	public LocalDate nextTradingDate(LocalDate localDate) {

		LocalDate nextTradingDate = localDate;

		if (DayOfWeek.FRIDAY == localDate.getDayOfWeek()) {
			nextTradingDate = localDate.plusDays(3);
		} else {
			nextTradingDate = localDate.plusDays(1);
		}

		if (this.isHoliday(nextTradingDate)) {
			return nextTradingDate(nextTradingDate);
		}

		return nextTradingDate;
	}
}
