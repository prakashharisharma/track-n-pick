package com.example.model.ledger;

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

import com.example.model.ledger.ValuationLedger.Category;
import com.example.model.master.Stock;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;


@Entity
@Table(name = "STOCK_RESEARCH_LEDGER")
public class ResearchLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SRL_ID")
	long srlId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stock;
	
	@Column(name = "ENTRY_DATE")
	LocalDate entryhDate = LocalDate.now();
	
	@Column(name = "ENTRY_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double entryPrice;

	@Column(name = "EXIT_DATE")
	LocalDate exitDate = LocalDate.now();
	
	@Column(name = "EXIT_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double exitPrice;
	
	@Column(name = "RESEARCH_TYPE")
	@Enumerated(EnumType.STRING)
	ResearchType researchType;
	
	@Column(name = "RESEARCH_STATUS")
	@Enumerated(EnumType.STRING)
	ResearchTrigger researchStatus;
	
	@Column(name = "IS_NOTIFIED_BUY")
	boolean notifiedBuy = false;

	@Column(name = "IS_NOTIFIED_SELL")
	boolean notifiedSell = false;
	
	@Column(name = "IS_NOTIFIED_STORAGE")
	boolean notifiedStorage = false;
	
	@Column(name = "CATEGORY")
	@Enumerated(EnumType.STRING)
	Category category;
	
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

	public LocalDate getEntryhDate() {
		return entryhDate;
	}

	public void setEntryhDate(LocalDate entryhDate) {
		this.entryhDate = entryhDate;
	}

	public double getEntryPrice() {
		return entryPrice;
	}

	public void setEntryPrice(double entryPrice) {
		this.entryPrice = entryPrice;
	}

	public LocalDate getExitDate() {
		return exitDate;
	}

	public void setExitDate(LocalDate exitDate) {
		this.exitDate = exitDate;
	}

	public double getExitPrice() {
		return exitPrice;
	}

	public void setExitPrice(double exitPrice) {
		this.exitPrice = exitPrice;
	}

	public ResearchType getResearchType() {
		return researchType;
	}

	public void setResearchType(ResearchType researchType) {
		this.researchType = researchType;
	}

	public ResearchTrigger getResearchStatus() {
		return researchStatus;
	}

	public void setResearchStatus(ResearchTrigger researchStatus) {
		this.researchStatus = researchStatus;
	}


	public boolean isNotifiedBuy() {
		return notifiedBuy;
	}

	public void setNotifiedBuy(boolean notifiedBuy) {
		this.notifiedBuy = notifiedBuy;
	}

	public boolean isNotifiedSell() {
		return notifiedSell;
	}

	public void setNotifiedSell(boolean notifiedSell) {
		this.notifiedSell = notifiedSell;
	}

	public boolean isNotifiedStorage() {
		return notifiedStorage;
	}

	public void setNotifiedStorage(boolean notifiedStorage) {
		this.notifiedStorage = notifiedStorage;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "ResearchLedger [srlId=" + srlId + ", stock=" + stock.getNseSymbol() + ", entryhDate=" + entryhDate + ", entryPrice="
				+ entryPrice + ", exitDate=" + exitDate + ", exitPrice=" + exitPrice + ", researchType=" + researchType
				+ ", researchStatus=" + researchStatus + ", notifiedBuy=" + notifiedBuy + ", notifiedSell="
				+ notifiedSell + "]";
	}

	
}
