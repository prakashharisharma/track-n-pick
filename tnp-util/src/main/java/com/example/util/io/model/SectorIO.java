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
    
    private double variationPe;
    
    private double variationPb;

	public SectorIO(String sectorName, double sectorPe, double sectorPb,double variationPe,double variationPb) {
		super();
		this.sectorName = sectorName;
		this.sectorPe = sectorPe;
		this.sectorPb = sectorPb;
		this.variationPe = variationPe;
		this.variationPb = variationPb;
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

	public double getVariationPe() {
		return variationPe;
	}

	public void setVariationPe(double variationPe) {
		this.variationPe = variationPe;
	}

	public double getVariationPb() {
		return variationPb;
	}

	public void setVariationPb(double variationPb) {
		this.variationPb = variationPb;
	}

	@Override
	public String toString() {
		return "SectorIO [sectorName=" + sectorName + ", sectorPe=" + sectorPe + ", sectorPb=" + sectorPb + "]";
	}
    
    
}
