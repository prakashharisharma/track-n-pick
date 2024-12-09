package com.example.storage.model;

public class Volume {

	private Long volume;

	private Long weeklyAverage;

	private Long monthlyAverage;


	public Volume() {
	}

	public Volume(Long volume, Long weeklyAverage, Long monthlyAverage) {
		this.volume = volume;
		this.weeklyAverage = weeklyAverage;
		this.monthlyAverage = monthlyAverage;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Long getWeeklyAverage() {
		return weeklyAverage;
	}

	public void setWeeklyAverage(Long weeklyAverage) {
		this.weeklyAverage = weeklyAverage;
	}

	public Long getMonthlyAverage() {
		return monthlyAverage;
	}

	public void setMonthlyAverage(Long monthlyAverage) {
		this.monthlyAverage = monthlyAverage;
	}

	@Override
	public String toString() {
		return "Volume{" +
				"volume=" + volume +
				", weeklyAverage=" + weeklyAverage +
				", monthlyAverage=" + monthlyAverage +
				'}';
	}
}
