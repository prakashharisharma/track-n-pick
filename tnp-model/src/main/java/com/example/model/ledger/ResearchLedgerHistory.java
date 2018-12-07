package com.example.model.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.example.model.master.Stock;

@Entity
@Table(name = "STOCK_RESEARCH_HISTORY_LEDGER")
public class ResearchLedgerHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SRHL_ID")
	long srlId;
	
	@Column(name = "NSE_SYMBOL")
	String nseSymbol;
	
	@Column(name = "RESEARCH_DATE")
	LocalDate researchDate;
	
	@Column(name = "RESEARCH_PRICE")
	double researchPrice;
	
	@Column(name = "TARGET_PRICE")
	double targetPrice;
	
	@Column(name = "TARGET_DATE")
	LocalDate targetDate;

	public long getSrlId() {
		return srlId;
	}

	public void setSrlId(long srlId) {
		this.srlId = srlId;
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public LocalDate getResearchDate() {
		return researchDate;
	}

	public void setResearchDate(LocalDate researchDate) {
		this.researchDate = researchDate;
	}

	public double getResearchPrice() {
		return researchPrice;
	}

	public void setResearchPrice(double researchPrice) {
		this.researchPrice = researchPrice;
	}

	public double getTargetPrice() {
		return targetPrice;
	}

	public void setTargetPrice(double targetPrice) {
		this.targetPrice = targetPrice;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(LocalDate targetDate) {
		this.targetDate = targetDate;
	}
	
}
