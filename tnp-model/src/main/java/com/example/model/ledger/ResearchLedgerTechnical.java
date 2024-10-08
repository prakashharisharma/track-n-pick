package com.example.model.ledger;

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
import com.example.util.io.model.ResearchIO.ResearchTrigger;

import java.time.LocalDate;

@Entity
@Table(name = "STOCK_RESEARCH_LEDGER_TECHNICAL")
public class ResearchLedgerTechnical {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SRL_ID")
	long srlId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stock;
	
	@Column(name = "RESEARCH_STATUS")
	@Enumerated(EnumType.STRING)
	ResearchTrigger researchStatus;
	
	@Column(name = "IS_NOTIFIED")
	boolean notified = false;

	@Column(name = "RESEARCH_DATE")
	LocalDate researchDate;

	@Column(name = "EXIT_DATE")
	LocalDate exitDate;

	@Column(name = "RESEARCH_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double researchPrice;

	@Column(name = "EXIT_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double exitPrice;

	@Column(name = "RESEARCH_RULE")
	Integer researchRule;

	@Column(name = "EXIT_RULE")
	Integer exitRule;

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

	public ResearchTrigger getResearchStatus() {
		return researchStatus;
	}

	public void setResearchStatus(ResearchTrigger researchStatus) {
		this.researchStatus = researchStatus;
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	public LocalDate getResearchDate() {
		return researchDate;
	}

	public void setResearchDate(LocalDate researchDate) {
		this.researchDate = researchDate;
	}

	public LocalDate getExitDate() {
		return exitDate;
	}

	public void setExitDate(LocalDate exitDate) {
		this.exitDate = exitDate;
	}

	public double getResearchPrice() {
		return researchPrice;
	}

	public void setResearchPrice(double researchPrice) {
		this.researchPrice = researchPrice;
	}

	public double getExitPrice() {
		return exitPrice;
	}

	public void setExitPrice(double exitPrice) {
		this.exitPrice = exitPrice;
	}

	public Integer getResearchRule() {
		return researchRule;
	}

	public void setResearchRule(Integer researchRule) {
		this.researchRule = researchRule;
	}

	public Integer getExitRule() {
		return exitRule;
	}

	public void setExitRule(Integer exitRule) {
		this.exitRule = exitRule;
	}

	@Override
	public String toString() {
		return "ResearchLedgerTechnical{" +
				"srlId=" + srlId +
				", stock=" + stock +
				", researchStatus=" + researchStatus +
				", notified=" + notified +
				", researchDate=" + researchDate +
				", exitDate=" + exitDate +
				", researchPrice=" + researchPrice +
				", exitPrice=" + exitPrice +
				", researchRule=" + researchRule +
				", exitRule=" + exitRule +
				'}';
	}

}
