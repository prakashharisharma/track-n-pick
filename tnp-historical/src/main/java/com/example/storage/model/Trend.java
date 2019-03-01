package com.example.storage.model;

import com.example.util.io.model.type.DirectionIO;

public class Trend {

	private DirectionIO longTermTrend;
	
	private DirectionIO midTermTrend;
	
	private DirectionIO currentTrend;

	public Trend() {
		super();
		
	}

	public Trend(DirectionIO longTermTrend, DirectionIO midTermTrend, DirectionIO currentTrend) {
		super();
		this.longTermTrend = longTermTrend;
		this.midTermTrend = midTermTrend;
		this.currentTrend = currentTrend;
	}

	public DirectionIO getLongTermTrend() {
		return longTermTrend;
	}

	public void setLongTermTrend(DirectionIO longTermTrend) {
		this.longTermTrend = longTermTrend;
	}

	public DirectionIO getMidTermTrend() {
		return midTermTrend;
	}

	public void setMidTermTrend(DirectionIO midTermTrend) {
		this.midTermTrend = midTermTrend;
	}

	public DirectionIO getCurrentTrend() {
		return currentTrend;
	}

	public void setCurrentTrend(DirectionIO currentTrend) {
		this.currentTrend = currentTrend;
	}

	@Override
	public String toString() {
		return "Trend [longTermTrend=" + longTermTrend + ", midTermTrend=" + midTermTrend + ", currentTrend="
				+ currentTrend + "]";
	}
	
	
}
