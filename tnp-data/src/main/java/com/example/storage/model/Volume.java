package com.example.storage.model;

public class Volume {

	private Long volume;
	private Long avg5;
	private Long avg10;
	private Long avg20;
	private Long avg50;


	public Volume() {
	}

	public Volume(Long volume, Long avg5, Long avg10,Long avg20, Long avg50) {
		this.volume = volume;
		this.avg5 = avg5;
		this.avg10 = avg10;
		this.avg20 = avg20;
		this.avg50 = avg50;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Long getAvg5() {
		return avg5;
	}

	public void setAvg5(Long avg5) {
		this.avg5 = avg5;
	}

	public Long getAvg10() {
		return avg10;
	}

	public void setAvg10(Long avg10) {
		this.avg10 = avg10;
	}

	public Long getAvg20() {
		return avg20;
	}

	public void setAvg20(Long avg20) {
		this.avg20 = avg20;
	}

	public Long getAvg50() {
		return avg50;
	}

	public void setAvg50(Long avg50) {
		this.avg50 = avg50;
	}

	@Override
	public String toString() {
		return "Volume{" +
				"volume=" + volume +
				", weeklyAverage=" + avg5 +
				", monthlyAverage=" + avg50 +
				'}';
	}
}
