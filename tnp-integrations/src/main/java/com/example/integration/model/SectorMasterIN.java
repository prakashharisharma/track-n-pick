package com.example.integration.model;

import java.io.Serializable;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",", skipFirstLine = true)
public class SectorMasterIN implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4818179611963128527L;

	@DataField(pos = 1)
    private String sectorName;
	
	@DataField(pos = 2)
    private double sectorPe;
	
	@DataField(pos = 3)
    private double sectorPb;
	
	@DataField(pos = 4)
    private double variationPe;
	
	@DataField(pos = 5)
    private double variationPb;

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
		return "SectorMaster [sectorName=" + sectorName + ", sectorPe=" + sectorPe + ", sectorPb=" + sectorPb + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sectorName == null) ? 0 : sectorName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SectorMasterIN other = (SectorMasterIN) obj;
		if (sectorName == null) {
			if (other.sectorName != null)
				return false;
		} else if (!sectorName.equals(other.sectorName))
			return false;
		return true;
	}
	
}
