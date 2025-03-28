package com.example.transactional.model.ledger;

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

import com.example.transactional.model.master.Stock;
import com.example.transactional.model.um.Trade;


@Entity
@Table(name = "STOCK_RESEARCH_LEDGER_FUNDAMENTAL")
public class ResearchLedgerFundamental {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SRL_ID")
	long srlId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stock;
	
	@Column(name = "RESEARCH_STATUS")
	@Enumerated(EnumType.STRING)
	Trade.Type researchStatus;
	
	@Column(name = "IS_NOTIFIED")
	boolean notified = false;
	
	@ManyToOne
	@JoinColumn(name = "ENTRY_VALUATION")
	ValuationLedger entryValuation;
	
	@ManyToOne
	@JoinColumn(name = "EXIT_VALUATION")
	ValuationLedger exitValuation;
	
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

	public Trade.Type getResearchStatus() {
		return researchStatus;
	}

	public void setResearchStatus(Trade.Type researchStatus) {
		this.researchStatus = researchStatus;
	}


	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}


	public ValuationLedger getEntryValuation() {
		return entryValuation;
	}

	public void setEntryValuation(ValuationLedger entryValuation) {
		this.entryValuation = entryValuation;
	}

	public ValuationLedger getExitValuation() {
		return exitValuation;
	}

	public void setExitValuation(ValuationLedger exitValuation) {
		this.exitValuation = exitValuation;
	}

	@Override
	public String toString() {
		return "ResearchLedgerFundamental [srlId=" + srlId + ", stock=" + stock + ", researchStatus=" + researchStatus
				+ ", notified=" + notified  + ", entryValuation="
				+ entryValuation + ", exitValuation=" + exitValuation + "]";
	}

	

}
