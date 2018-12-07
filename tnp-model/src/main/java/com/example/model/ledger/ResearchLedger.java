package com.example.model.ledger;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;

@Entity
@Table(name = "STOCK_RESEARCH_LEDGER")
public class ResearchLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SRL_ID")
	long srlId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;
	
	@Column(name = "RESEARCH_DATE")
	LocalDate researchDate;
	
	@Column(name = "RESEARCH_PRICE")
	double researchPrice;
	
	@Column(name = "TARGET_PRICE")
	double targetPrice;
	
	@Column(name = "TARGET_DATE")
	LocalDate targetDate;
	
	@Column(name = "IS_ACTIVE")
	boolean active = true;
	
	@Column(name = "IS_NOTIFIED")
	boolean notified = false;

	public long getSrlId() {
		return srlId;
	}

	public void setSrlId(long srlId) {
		this.srlId = srlId;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(LocalDate targetDate) {
		this.targetDate = targetDate;
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	@Override
	public String toString() {
		return "ResearchLedger [srlId=" + srlId + ", researchDate=" + researchDate + ", researchPrice=" + researchPrice
				+ ", targetPrice=" + targetPrice + ", active=" + active + "]";
	}
	
}
