package com.example.model.master;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "HOLIDAY_CALENDAR")
public class HolidayCalendar {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "HOLIDAY_ID")
	long holidayId;
	
	@Column(name = "HOLIDAY_DATE", unique=true)
	LocalDate holidayDate = LocalDate.now();

	@Column(name = "HOLIDAY_DESC")
	String holidayDesc;
	
	public long getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(long holidayId) {
		this.holidayId = holidayId;
	}

	public LocalDate getHolidayDate() {
		return holidayDate;
	}

	public void setHolidayDate(LocalDate holidayDate) {
		this.holidayDate = holidayDate;
	}

	public String getHolidayDesc() {
		return holidayDesc;
	}

	public void setHolidayDesc(String holidayDesc) {
		this.holidayDesc = holidayDesc;
	}

	@Override
	public String toString() {
		return "HolidayCalendar [holidayId=" + holidayId + ", holidayDate=" + holidayDate.toString() + ", holidayDesc="
				+ holidayDesc + "]";
	}

}
