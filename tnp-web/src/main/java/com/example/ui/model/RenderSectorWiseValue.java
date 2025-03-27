package com.example.ui.model;

public class RenderSectorWiseValue {

	String sector;
	double allocation;
	public RenderSectorWiseValue(String sector, double allocation) {
		super();
		this.sector = sector;
		this.allocation = allocation;
	}
	public String getSector() {
		return sector;
	}
	public void setSector(String sector) {
		this.sector = sector;
	}
	public double getAllocation() {
		return allocation;
	}
	public void setAllocation(double allocation) {
		this.allocation = allocation;
	}
	
}
