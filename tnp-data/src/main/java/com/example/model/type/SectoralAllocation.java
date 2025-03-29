package com.example.model.type;

import java.io.Serializable;

public class SectoralAllocation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4231834461451149327L;
	private String sectorName;
	private Double allocation;
	public SectoralAllocation() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SectoralAllocation(String sectorName, Double allocation) {
		super();
		this.sectorName = sectorName;
		this.allocation = allocation;
	}
	public String getSectorName() {
		return sectorName;
	}
	public void setSectorName(String sectorName) {
		this.sectorName = sectorName;
	}
	public Double getAllocation() {
		return allocation;
	}
	public void setAllocation(Double allocation) {
		this.allocation = allocation;
	}
	
	
	
}
