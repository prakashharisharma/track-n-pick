package com.example.storage.model;

import com.example.storage.model.type.Direction;

public class Trend {

	private Direction longTermTrend;
	
	private Direction midTermTrend;
	
	private Direction currentTrend;

	public Trend() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Trend(Direction longTermTrend, Direction midTermTrend, Direction currentTrend) {
		super();
		this.longTermTrend = longTermTrend;
		this.midTermTrend = midTermTrend;
		this.currentTrend = currentTrend;
	}

	public Direction getLongTermTrend() {
		return longTermTrend;
	}

	public void setLongTermTrend(Direction longTermTrend) {
		this.longTermTrend = longTermTrend;
	}

	public Direction getMidTermTrend() {
		return midTermTrend;
	}

	public void setMidTermTrend(Direction midTermTrend) {
		this.midTermTrend = midTermTrend;
	}

	public Direction getCurrentTrend() {
		return currentTrend;
	}

	public void setCurrentTrend(Direction currentTrend) {
		this.currentTrend = currentTrend;
	}

	@Override
	public String toString() {
		return "Trend [longTermTrend=" + longTermTrend + ", midTermTrend=" + midTermTrend + ", currentTrend="
				+ currentTrend + "]";
	}
	
	
}
