package com.example.model;

import java.io.Serializable;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",", skipFirstLine = true)
public class SectorMaster implements Serializable{

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
		SectorMaster other = (SectorMaster) obj;
		if (sectorName == null) {
			if (other.sectorName != null)
				return false;
		} else if (!sectorName.equals(other.sectorName))
			return false;
		return true;
	}
	
}