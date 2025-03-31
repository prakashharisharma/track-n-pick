package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;


import com.example.data.transactional.entities.TradingHoliday;
import com.example.data.transactional.entities.SpecialTradingSession;
import com.example.data.transactional.repo.SpecialTradingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.data.transactional.repo.TradingHolidayRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class CalendarService {

	@Autowired
	private TradingHolidayRepository tradingHolidayRepository;

	@Autowired
	private SpecialTradingSessionRepository specialTradingSessionRepository;

	@Autowired
	private MiscUtil miscUtil;

	public boolean isWorkingDay(LocalDate sessionDate) {
		// If it's a special trading session, it's a working day even if it's a holiday
		if (isSpecialTradingSession(sessionDate)) {
			return true;
		}

		// If it's a holiday and NOT a special trading session, it's NOT a working day
		if (isHoliday(sessionDate)) {
			return false;
		}

		DayOfWeek dayOfWeek = sessionDate.getDayOfWeek();

		// If it's a regular weekday (Monday to Friday) and not a holiday, it's a working day
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}

	public boolean isHoliday(LocalDate sessionDate) {

		boolean isHoliday = false;

		Optional<TradingHoliday> tradingHolidayOptional = tradingHolidayRepository.findBySessionDate(sessionDate);

		if (tradingHolidayOptional.isPresent()) {
			isHoliday = true;
		}

		return isHoliday;
	}

	public boolean isSpecialTradingSession(LocalDate sessionDate) {

		boolean isSpecialTradingSession = false;

		Optional<SpecialTradingSession> specialTradingSessionOptional = specialTradingSessionRepository.findBySessionDate(sessionDate);

		if (specialTradingSessionOptional.isPresent()) {
			isSpecialTradingSession = true;
		}

		return isSpecialTradingSession;
	}

	public LocalDate previousWorkingDay() {

		LocalDate currentDate = miscUtil.currentDate();

		return previousWorkingDay(currentDate);
	}

	public List<TradingHoliday> holidays() {

		LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 01);

		LocalDate yearLastdate = LocalDate.of(2019, Month.DECEMBER, 31);
		return tradingHolidayRepository.findBySessionDateBetween(fromDate, yearLastdate);

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

	public boolean isLastTradingSessionOfWeek(LocalDate sessionDate) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(sessionDate)) {
			return false;
		}

		// If today is Friday, check if Saturday or Sunday has a special trading session
		if (sessionDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
			LocalDate saturday = sessionDate.with(DayOfWeek.SATURDAY);
			LocalDate sunday = sessionDate.with(DayOfWeek.SUNDAY);
			return !(isSpecialTradingSession(saturday) || isSpecialTradingSession(sunday));
			// If either Sat or Sun has a special session, Friday is NOT last trading session
		}

		// If today is Thursday, check if Friday is a holiday (Then Thursday is the last session)
		if (sessionDate.getDayOfWeek() == DayOfWeek.THURSDAY) {
			LocalDate friday = sessionDate.plusDays(1);
			if (isHoliday(friday)) {
				return true; // If Friday is a holiday, Thursday is the last session
			}
		}

		return false;
	}

	public boolean isLastTradingSessionOfMonth(LocalDate sessionDate) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(sessionDate)) {
			return false;
		}

		// Find the last calendar day of the month
		LocalDate lastDay = YearMonth.from(sessionDate).atEndOfMonth();

		// Check if the last day is Saturday or Sunday and has a special trading session
		if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			if (isSpecialTradingSession(lastDay)) {
				return sessionDate.equals(lastDay); // If today is the special session day, return true
			}
			// If no special session, move to the last valid weekday
			lastDay = lastDay.minusDays(1);
		}

		// Move back if last trading day is a holiday
		while (isHoliday(lastDay) || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			lastDay = lastDay.minusDays(1);
		}

		// If today is the last valid trading session of the month, return true
		return sessionDate.equals(lastDay);
	}


	public boolean isLastTradingSessionOfQuarter(LocalDate sessionDate) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(sessionDate)) {
			return false;
		}

		// Determine the last month of the quarter
		int month = sessionDate.getMonthValue();
		int lastMonthOfQuarter = ((month - 1) / 3 + 1) * 3; // 3, 6, 9, 12

		// Find the last calendar day of the quarter
		LocalDate lastDay = YearMonth.of(sessionDate.getYear(), lastMonthOfQuarter).atEndOfMonth();

		// Check if the last day is Saturday or Sunday and has a special trading session
		if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			if (isSpecialTradingSession(lastDay)) {
				return sessionDate.equals(lastDay); // If today is the special session day, return true
			}
			// If no special session, move to the last valid weekday
			lastDay = lastDay.minusDays(1);
		}

		// Move back if last trading day is a holiday
		while (isHoliday(lastDay) || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			lastDay = lastDay.minusDays(1);
		}

		// If today is the last valid trading session of the quarter, return true
		return sessionDate.equals(lastDay);
	}

	public boolean isLastTradingSessionOfYear(LocalDate sessionDate) {
		// If today is a holiday, it's not a trading session
		if (isHoliday(sessionDate)) {
			return false;
		}

		// Find the last calendar day of the year (December 31st)
		LocalDate lastDay = LocalDate.of(sessionDate.getYear(), Month.DECEMBER, 31);

		// Check if the last day is Saturday or Sunday and has a special trading session
		if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			if (isSpecialTradingSession(lastDay)) {
				return sessionDate.equals(lastDay); // If today is the special session day, return true
			}
			// If no special session, move to the last valid weekday
			lastDay = lastDay.minusDays(1);
		}

		// Move back if last trading day is a holiday
		while (isHoliday(lastDay) || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			lastDay = lastDay.minusDays(1);
		}

		// If today is the last valid trading session of the year, return true
		return sessionDate.equals(lastDay);
	}

	public LocalDate nextTradingDate(LocalDate sessionDate) {

		LocalDate nextTradingDate = sessionDate;

		if (DayOfWeek.FRIDAY == sessionDate.getDayOfWeek()) {
			nextTradingDate = sessionDate.plusDays(3);
		}else if (DayOfWeek.SATURDAY == sessionDate.getDayOfWeek()) {
			nextTradingDate = sessionDate.plusDays(2);
		}
		else {
			nextTradingDate = sessionDate.plusDays(1);
		}

		if (this.isHoliday(nextTradingDate)) {
			return nextTradingDate(nextTradingDate);
		}

		return nextTradingDate;
	}

	public LocalDate previousTradingSession(LocalDate sessionDate) {

		LocalDate previousSessionDate = sessionDate;

		if (DayOfWeek.MONDAY == sessionDate.getDayOfWeek()) {
			previousSessionDate = sessionDate.minusDays(3);
		}else if (DayOfWeek.SUNDAY == sessionDate.getDayOfWeek()) {
			previousSessionDate = sessionDate.minusDays(2);
		}
		else {
			previousSessionDate = sessionDate.minusDays(1);
		}

		if (this.isHoliday(previousSessionDate)) {
			return previousTradingSession(previousSessionDate);
		}

		return previousSessionDate;
	}
}
