package com.example.transactional.repo.master;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.master.HolidayCalendar;

@Transactional
@Repository
public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, Long> {
	HolidayCalendar findByHolidayDate(LocalDate holidayDate);
	List<HolidayCalendar> findByHolidayDateBetween(LocalDate holidayDate1,LocalDate holidayDate2);
}
