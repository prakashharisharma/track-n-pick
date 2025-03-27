package com.example.util.io.model.type;

import java.io.Serializable;

public class Trend implements Serializable{
	public enum Direction {UP, DOWN, INVALID}
	public enum Strength {SHORT, MEDIUM, LONG, INVALID, STRONG, WEAK}

	public enum Momentum  {
		SIDEWAYS,
		PULLBACK, CORRECTION, BOTTOM,
		RECOVERY, ADVANCE, TOP
	}

	private Direction direction;
	private Strength strength;
	private Momentum momentum;

	public Trend(Direction direction, Strength strength, Momentum momentum) {
		this.direction = direction;
		this.strength = strength;
		this.momentum = momentum;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
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
