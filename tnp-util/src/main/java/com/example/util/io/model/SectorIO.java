package com.example.util.io.model;

import java.io.Serializable;

public class SectorIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1495674084604184012L;

    private String sectorName;

    private double sectorPe;

    private double sectorPb;

	public SectorIO(String sectorName, double sectorPe, double sectorPb) {
		super();
		this.sectorName = sectorName;
		this.sectorPe = sectorPe;
		this.sectorPb = sectorPb;
	}

	public String getSectorName() {
		return sectorName;
	}

	public void setSectorName(String sectorName) {
		this.sectorName = sectorName;
	}

	public double getSectorPe() {
		return sectorPe;
	}

	public void setSectorPe(double sectorPe) {
		this.sectorPe = sectorPe;
	}

	public double getSectorPb() {
		return sectorPb;
	}

	public void setSectorPb(double sectorPb) {
		this.sectorPb = sectorPb;
	}

	@Override
	public String toString() {
		return "SectorIO [sectorName=" + sectorName + ", sectorPe=" + sectorPe + ", sectorPb=" + sectorPb + "]";
	}
    
    
}
