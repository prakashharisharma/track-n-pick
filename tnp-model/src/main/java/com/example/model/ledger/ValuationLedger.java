package com.example.model.ledger;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;

@Entity
@Table(name = "VALUATION_LEDGER")
public class ValuationLedger implements Serializable {

	public enum Type {UNDERVALUE, OVERVALUE}
	
	public enum Category { STRONG, TWEAKED }
	
	public enum Status {OPEN, CLOSE};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3225990275526508375L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "UNDERVALUE_ID")
	long undervalueId;

	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "RESEARCH_DATE")
	LocalDate researchDate = LocalDate.now();
	
	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();
	
	@Column(name = "PE" , columnDefinition="Decimal(10,2) default '0.00'")
	double pe;
	
	@Column(name = "PB" , columnDefinition="Decimal(10,2) default '0.00'")
	double pb;
	
	@Column(name = "NEW_PE" , columnDefinition="Decimal(10,2) default '0.00'")
	double newPe;
	
	@Column(name = "NEW_PB" , columnDefinition="Decimal(10,2) default '0.00'")
	double newPb;
	
	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	Type type;
	
	@Column(name = "CATEGORY")
	@Enumerated(EnumType.STRING)
	Category category;
	
	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	Status status;
	
	public ValuationLedger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ValuationLedger(long undervalueId, Stock stockId, LocalDate researchDate, double pe, double pb) {
		super();
		this.undervalueId = undervalueId;
		this.stockId = stockId;
		this.researchDate = researchDate;
		this.pe = pe;
		this.pb = pb;
	}

	public long getUndervalueId() {
		return undervalueId;
	}

	public void setUndervalueId(long undervalueId) {
		this.undervalueId = undervalueId;
	}

	public Stock getStockId() {
		return stockId;
	}

	public void setStockId(Stock stockId) {
		this.stockId = stockId;
	}

	public LocalDate getResearchDate() {
		return researchDate;
	}

	public void setResearchDate(LocalDate researchDate) {
		this.researchDate = researchDate;
	}

	public double getPe() {
		return pe;
	}

	public void setPe(double pe) {
		this.pe = pe;
	}

	public double getPb() {
		return pb;
	}

	public void setPb(double pb) {
		this.pb = pb;
	}

	public LocalDate getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDate lastModified) {
		this.lastModified = lastModified;
	}

	public double getNewPe() {
		return newPe;
	}

	public void setNewPe(double newPe) {
		this.newPe = newPe;
	}

	public double getNewPb() {
		return newPb;
	}

	public void setNewPb(double newPb) {
		this.newPb = newPb;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	
	
}
