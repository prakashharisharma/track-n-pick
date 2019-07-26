package com.example.model.master;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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

	@Column(name = "SECTOR_PE",columnDefinition="Decimal(10,4) default '20.00'")
	double sectorPe = 20.00;

	@Column(name = "SECTOR_PB",columnDefinition="Decimal(10,4) default '1.00'")
	double sectorPb = 1.00;

	@Column(name = "SECTOR_CURR_RATIO",columnDefinition="Decimal(10,4) default '1.00'")
	double sectorCurrentRatio = 1.00;	
	
	@Column(name = "VARIATION_PE",columnDefinition="Decimal(10,4) default '2.00'")
	double variationPe = 2.00;
	
	@Column(name = "VARIATION_PB",columnDefinition="Decimal(10,4) default '0.50'")
	double variationPb = 0.50;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sector")
	private Set<Stock> stocks = new HashSet<Stock>(0);
	
	public Sector() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Sector(String sectorName) {
		super();
		this.sectorName = sectorName;
	}

	public Sector(String sectorName, double sectorPe, double sectorPb,double variationPe,double variationPb) {
		super();
		this.sectorName = sectorName;
		this.sectorPe = sectorPe;
		this.sectorPb = sectorPb;
		this.variationPe = variationPe;
		this.variationPb = variationPb;
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

	public Set<Stock> getStocks() {
		return stocks;
	}

	public void setStocks(Set<Stock> stocks) {
		this.stocks = stocks;
	}

	public double getSectorCurrentRatio() {
		return sectorCurrentRatio;
	}

	public void setSectorCurrentRatio(double sectorCurrentRatio) {
		this.sectorCurrentRatio = sectorCurrentRatio;
	}

	@Override
	public String toString() {
		return "Sector [sectorId=" + sectorId + ", sectorName=" + sectorName + ", sectorPe=" + sectorPe + ", sectorPb="
				+ sectorPb + ", sectorCurrentRatio=" + sectorCurrentRatio + "]";
	}


}
