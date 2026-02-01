package com.example.service;

import com.example.data.transactional.entities.SpecialTradingSession;
import com.example.data.transactional.entities.TradingHoliday;
import com.example.data.transactional.repo.SpecialTradingSessionRepository;
import com.example.data.transactional.repo.TradingHolidayRepository;
import com.example.util.MiscUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class CalendarService {

    @Autowired private TradingHolidayRepository tradingHolidayRepository;

    @Autowired private SpecialTradingSessionRepository specialTradingSessionRepository;

    @Autowired private MiscUtil miscUtil;

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

        Optional<TradingHoliday> tradingHolidayOptional =
                tradingHolidayRepository.findBySessionDate(sessionDate);

        if (tradingHolidayOptional.isPresent()) {
            isHoliday = true;
        }

        return isHoliday;
    }

    public boolean isSpecialTradingSession(LocalDate sessionDate) {

        boolean isSpecialTradingSession = false;

        Optional<SpecialTradingSession> specialTradingSessionOptional =
                specialTradingSessionRepository.findBySessionDate(sessionDate);

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

        DayOfWeek dayOfWeek = sessionDate.getDayOfWeek();

        // For weekend days: only trading if special session
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            if (!isSpecialTradingSession(sessionDate)) {
                return false; // Not a trading day at all
            }
        }

        // Check all remaining days in the week
        LocalDate checkDate = sessionDate.plusDays(1);

        while (checkDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            // Skip if holiday
            if (!isHoliday(checkDate)) {
                DayOfWeek checkDayOfWeek = checkDate.getDayOfWeek();

                // Check if it's a trading day
                if (checkDayOfWeek != DayOfWeek.SATURDAY && checkDayOfWeek != DayOfWeek.SUNDAY) {
                    // Weekday - always a trading day if not holiday
                    return false; // Found a later trading day
                } else if (isSpecialTradingSession(checkDate)) {
                    // Weekend day that's a special session
                    return false; // Found a later trading day
                }
            }
            checkDate = checkDate.plusDays(1);
        }

        // No trading days found after this date
        return true;
    }

    public boolean isLastTradingSessionOfMonth(LocalDate sessionDate) {
        // If today is a holiday, it's not a trading session
        if (isHoliday(sessionDate)) {
            return false;
        }

        // Find the last calendar day of the month
        LocalDate lastDay = YearMonth.from(sessionDate).atEndOfMonth();

        // Check if the last day is Saturday or Sunday and has a special trading session
        if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY
                || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            if (isSpecialTradingSession(lastDay)) {
                return sessionDate.equals(
                        lastDay); // If today is the special session day, return true
            }
            // If no special session, move to the last valid weekday
            lastDay = lastDay.minusDays(1);
        }

        // Move back if last trading day is a holiday
        while (isHoliday(lastDay)
                || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY
                || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
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
        if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY
                || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            if (isSpecialTradingSession(lastDay)) {
                return sessionDate.equals(
                        lastDay); // If today is the special session day, return true
            }
            // If no special session, move to the last valid weekday
            lastDay = lastDay.minusDays(1);
        }

        // Move back if last trading day is a holiday
        while (isHoliday(lastDay)
                || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY
                || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
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
        if (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY
                || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            if (isSpecialTradingSession(lastDay)) {
                return sessionDate.equals(
                        lastDay); // If today is the special session day, return true
            }
            // If no special session, move to the last valid weekday
            lastDay = lastDay.minusDays(1);
        }

        // Move back if last trading day is a holiday
        while (isHoliday(lastDay)
                || lastDay.getDayOfWeek() == DayOfWeek.SATURDAY
                || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            lastDay = lastDay.minusDays(1);
        }

        // If today is the last valid trading session of the year, return true
        return sessionDate.equals(lastDay);
    }

    public LocalDate nextTradingSession(LocalDate sessionDate) {
        LocalDate nextTradingDate = sessionDate;

        // First, handle special trading session logic
        if (DayOfWeek.FRIDAY == sessionDate.getDayOfWeek()) {
            LocalDate saturday = sessionDate.plusDays(1);
            LocalDate sunday = sessionDate.plusDays(2);

            // Check weekend days only (Saturday, Sunday)
            if (isSpecialTradingSession(saturday) && !this.isHoliday(saturday)) {
                return saturday;
            } else if (isSpecialTradingSession(sunday) && !this.isHoliday(sunday)) {
                return sunday;
            }
            nextTradingDate = sessionDate.plusDays(3); // Monday

        } else if (DayOfWeek.SATURDAY == sessionDate.getDayOfWeek()) {

            LocalDate sunday = sessionDate.plusDays(1);
            if (isSpecialTradingSession(sunday) && !this.isHoliday(sunday)) {
                return sunday;
            }
            nextTradingDate = sessionDate.plusDays(2); // Monday

        } else {
            // Monday through Thursday - check next day
            LocalDate nextDay = sessionDate.plusDays(1);
            if (isSpecialTradingSession(nextDay) && !this.isHoliday(nextDay)) {
                return nextDay;
            }
            nextTradingDate = nextDay;
        }

        // Check for holidays and recursively find the next trading session
        if (this.isHoliday(nextTradingDate)) {
            return nextTradingSession(nextTradingDate);
        }

        return nextTradingDate;
    }

    public LocalDate previousTradingSession(LocalDate sessionDate) {
        LocalDate previousSessionDate = sessionDate;

        // First, handle special trading session logic
        if (DayOfWeek.MONDAY == sessionDate.getDayOfWeek()) {
            LocalDate sunday = sessionDate.minusDays(1);
            LocalDate saturday = sessionDate.minusDays(2);

            if (isSpecialTradingSession(sunday) && !this.isHoliday(sunday)) {
                return sunday;
            } else if (isSpecialTradingSession(saturday) && !this.isHoliday(saturday)) {
                return saturday;
            }
            previousSessionDate = sessionDate.minusDays(3);

        } else if (DayOfWeek.SUNDAY == sessionDate.getDayOfWeek()) {

            LocalDate saturday = sessionDate.minusDays(1);
            if (isSpecialTradingSession(saturday) && !this.isHoliday(saturday)) {
                return saturday;
            }
            previousSessionDate = sessionDate.minusDays(2);

        } else {
            // Tuesday through Friday
            // Check previous day for special sessions
            LocalDate previousDay = sessionDate.minusDays(1);
            if (isSpecialTradingSession(previousDay) && !this.isHoliday(previousDay)) {
                return previousDay;
            }
            previousSessionDate = previousDay;
        }

        // Check for holidays and recursively find the previous trading session
        if (this.isHoliday(previousSessionDate)) {
            return previousTradingSession(previousSessionDate);
        }

        return previousSessionDate;
    }
}
