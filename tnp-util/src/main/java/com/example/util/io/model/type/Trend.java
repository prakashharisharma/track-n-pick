package com.example.util.io.model.type;

import java.io.Serializable;


public class Trend implements Serializable{
	public enum Strength {SHORT, MEDIUM, LONG, INVALID}
	private  Strength strength;
	private Momentum momentum;

	public Trend(Strength strength, Momentum momentum) {
		this.strength = strength;
		this.momentum = momentum;
	}

	public Strength getStrength() {
		return strength;
	}

	public void setStrength(Strength strength) {
		this.strength = strength;
	}

	public Momentum getMomentum() {
		return momentum;
	}

	public void setMomentum(Momentum momentum) {
		this.momentum = momentum;
	}
}
