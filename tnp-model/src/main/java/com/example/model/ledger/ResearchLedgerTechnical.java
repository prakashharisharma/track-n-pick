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
	
	@ManyToOne
	@JoinColumn(name = "ENTRY_CROSS")
	CrossOverLedger entryCrossOver;
	
	@ManyToOne
	@JoinColumn(name = "EXIT_CROSS")
	CrossOverLedger exitCrossOver;
	
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



	public CrossOverLedger getEntryCrossOver() {
		return entryCrossOver;
	}

	public void setEntryCrossOver(CrossOverLedger entryCrossOver) {
		this.entryCrossOver = entryCrossOver;
	}

	public CrossOverLedger getExitCrossOver() {
		return exitCrossOver;
	}

	public void setExitCrossOver(CrossOverLedger exitCrossOver) {
		this.exitCrossOver = exitCrossOver;
	}

	@Override
	public String toString() {
		return "ResearchLedgerTechnical [srlId=" + srlId + ", stock=" + stock + ", researchStatus=" + researchStatus
				+ ", notified=" + notified  + ", entryCrossOver="
				+ entryCrossOver + ", exitCrossOver=" + exitCrossOver + "]";
	}



}
