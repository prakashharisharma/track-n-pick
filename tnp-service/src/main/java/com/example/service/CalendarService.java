package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

import javax.transaction.Transactional;

import com.example.model.master.SpecialTradingSession;
import com.example.repo.master.SpecialTradingSessionRepository;
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
	private SpecialTradingSessionRepository specialTradingSessionRepository;

	@Autowired
	private MiscUtil miscUtil;

	public boolean isWorkingDay(LocalDate date) {
		// If it's a special trading session, it's a working day even if it's a holiday
		if (isSpecialTradingSession(date)) {
			return true;
		}

		// If it's a holiday and NOT a special trading session, it's NOT a working day
		if (isHoliday(date)) {
			return false;
		}

		DayOfWeek dayOfWeek = date.getDayOfWeek();

		// If it's a regular weekday (Monday to Friday) and not a holiday, it's a working day
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}

	public boolean isHoliday(LocalDate date) {

		boolean isHoliday = false;

		HolidayCalendar holidaycalendar = holidayCalendarRepository.findByHolidayDate(date);

		if (holidaycalendar != null) {
			isHoliday = true;
		}

		return isHoliday;
	}

	public boolean isSpecialTradingSession(LocalDate date) {

		boolean isSpecialTradingSession = false;

		SpecialTradingSession specialTradingSession = specialTradingSessionRepository.findBySessionDate(date);

		if (specialTradingSession != null) {
			isSpecialTradingSession = true;
		}

		return isSpecialTradingSession;
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

	public boolean isLastTradingSessionOfWeek(LocalDate date) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(date)) {
			return false;
		}

		// If today is Friday, check if Saturday or Sunday has a special trading session
		if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
			LocalDate saturday = date.with(DayOfWeek.SATURDAY);
			LocalDate sunday = date.with(DayOfWeek.SUNDAY);
			return !(isSpecialTradingSession(saturday) || isSpecialTradingSession(sunday));
			// If either Sat or Sun has a special session, Friday is NOT last trading session
		}

		// If today is Thursday, check if Friday is a holiday (Then Thursday is the last session)
		if (date.getDayOfWeek() == DayOfWeek.THURSDAY) {
			LocalDate friday = date.plusDays(1);
			if (isHoliday(friday)) {
				return true; // If Friday is a holiday, Thursday is the last session
			}
		}

		return false;
	}

	public boolean isLastTradingSessionOfMonth(LocalDate date) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(date)) {
			return false;
		}

		// Find the last calendar day of the month
		LocalDate lastDay = YearMonth.from(date).atEndOfMonth();

		// Check if the last day is Saturday or Sunday and has a special trading session
		if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			if (isSpecialTradingSession(lastDay)) {
				return date.equals(lastDay); // If today is the special session day, return true
			}
			// If no special session, move to the last valid weekday
			lastDay = lastDay.minusDays(1);
		}

		// Move back if last trading day is a holiday
		while (isHoliday(lastDay) || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			lastDay = lastDay.minusDays(1);
		}

		// If today is the last valid trading session of the month, return true
		return date.equals(lastDay);
	}


	public boolean isLastTradingSessionOfQuarter(LocalDate date) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(date)) {
			return false;
		}

		// Determine the last month of the quarter
		int month = date.getMonthValue();
		int lastMonthOfQuarter = ((month - 1) / 3 + 1) * 3; // 3, 6, 9, 12

		// Find the last calendar day of the quarter
		LocalDate lastDay = YearMonth.of(date.getYear(), lastMonthOfQuarter).atEndOfMonth();

		// Check if the last day is Saturday or Sunday and has a special trading session
		if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			if (isSpecialTradingSession(lastDay)) {
				return date.equals(lastDay); // If today is the special session day, return true
			}
			// If no special session, move to the last valid weekday
			lastDay = lastDay.minusDays(1);
		}

		// Move back if last trading day is a holiday
		while (isHoliday(lastDay) || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			lastDay = lastDay.minusDays(1);
		}

		// If today is the last valid trading session of the quarter, return true
		return date.equals(lastDay);
	}

	public boolean isLastTradingSessionOfYear(LocalDate date) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(date)) {
			return false;
		}

		// Find the last calendar day of the year (December 31st)
		LocalDate lastDay = LocalDate.of(date.getYear(), Month.DECEMBER, 31);

		// Check if the last day is Saturday or Sunday and has a special trading session
		if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			if (isSpecialTradingSession(lastDay)) {
				return date.equals(lastDay); // If today is the special session day, return true
			}
			// If no special session, move to the last valid weekday
			lastDay = lastDay.minusDays(1);
		}

		// Move back if last trading day is a holiday
		while (isHoliday(lastDay) || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			lastDay = lastDay.minusDays(1);
		}

		// If today is the last valid trading session of the year, return true
		return date.equals(lastDay);
	}

	public LocalDate nextTradingDate(LocalDate localDate) {

		LocalDate nextTradingDate = localDate;

		if (DayOfWeek.FRIDAY == localDate.getDayOfWeek()) {
			nextTradingDate = localDate.plusDays(3);
		}else if (DayOfWeek.SATURDAY == localDate.getDayOfWeek()) {
			nextTradingDate = localDate.plusDays(2);
		}
		else {
			nextTradingDate = localDate.plusDays(1);
		}

		if (this.isHoliday(nextTradingDate)) {
			return nextTradingDate(nextTradingDate);
		}

		return nextTradingDate;
	}

	public LocalDate previousTradingSession(LocalDate localDate) {

		LocalDate previousSessionDate = localDate;

		if (DayOfWeek.MONDAY == localDate.getDayOfWeek()) {
			previousSessionDate = localDate.minusDays(3);
		}else if (DayOfWeek.SUNDAY == localDate.getDayOfWeek()) {
			previousSessionDate = localDate.minusDays(2);
		}
		else {
			previousSessionDate = localDate.minusDays(1);
		}

		if (this.isHoliday(previousSessionDate)) {
			return previousTradingSession(previousSessionDate);
		}

		return previousSessionDate;
	}
}
