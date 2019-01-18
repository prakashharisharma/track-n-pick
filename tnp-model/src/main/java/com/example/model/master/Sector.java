package com.example.model.master;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SECTORS")
public class Sector implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1613826068077828692L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SECTOR_ID")
	long sectorId;
	
	//@NaturalId
	@Column(name = "SECTOR_NAME")
	String sectorName;

	@Column(name = "SECTOR_PE")
	double sectorPe;

	@Column(name = "SECTOR_PB")
	double sectorPb;

	public Sector() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Sector(String sectorName, double sectorPe, double sectorPb) {
		super();
		this.sectorName = sectorName;
		this.sectorPe = sectorPe;
		this.sectorPb = sectorPb;
	}

	public long getSectorId() {
		return sectorId;
	}

	public void setSectorId(long sectorId) {
		this.sectorId = sectorId;
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
		return "Sector [sectorId=" + sectorId + ", sectorName=" + sectorName + ", sectorPe=" + sectorPe + ", sectorPb="
				+ sectorPb + "]";
	}

}
