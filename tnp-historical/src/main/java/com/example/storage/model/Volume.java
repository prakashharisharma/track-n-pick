package com.example.storage.model;

public class Volume {

	private Long volume;

	private Long average;


	public Volume() {
	}

	public Volume(Long volume, Long average) {
		this.volume = volume;
		this.average = average;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Long getAverage() {
		return average;
	}

	public void setAverage(Long average) {
		this.average = average;
	}


	@Override
	public String toString() {
		return "Volume{" +
				"volume=" + volume +
				", average=" + average +
				'}';
	}
}
