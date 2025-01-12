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

	public enum Strategy {
		PRICE_ACTION, MA_WITH_MACD, MACD
	}

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

	@Column(name = "STRATEGY")
	@Enumerated(EnumType.STRING)
	Strategy strategy;

	@Column(name = "RESEARCH_DATE")
	LocalDate researchDate;

	@Column(name = "EXIT_DATE")
	LocalDate exitDate;

	@Column(name = "NEXT_TRADING_DATE")
	LocalDate nextTradingDate;

	@Column(name = "MODIFIED_DATE")
	LocalDate modifiedAt;

	@Column(name = "CREATED_DATE")
	LocalDate createdAt;

	@Column(name = "RESEARCH_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double researchPrice;

	@Column(name = "EXIT_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double exitPrice;

	@Column(name = "IS_CONFIRMED")
	boolean confirmed = false;

	@Column(name = "IS_VERIFIED")
	boolean verified = false;

	@Column(name = "STOP_LOSS", columnDefinition="Decimal(10,2) default '0.00'")
	double stopLoss;

	@Column(name = "TARGET", columnDefinition="Decimal(10,2) default '0.00'")
	double target;

	@Column(name = "VOLUME")
	Long volume;

	@Column(name = "WEEKLY_VOLUME")
	Long weeklyVolume;

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

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public double getTarget() {
		return target;
	}

	public void setTarget(double target) {
		this.target = target;
	}

	public LocalDate getNextTradingDate() {
		return nextTradingDate;
	}

	public void setNextTradingDate(LocalDate nextTradingDate) {
		this.nextTradingDate = nextTradingDate;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Long getWeeklyVolume() {
		return weeklyVolume;
	}

	public void setWeeklyVolume(Long weeklyVolume) {
		this.weeklyVolume = weeklyVolume;
	}

	public LocalDate getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(LocalDate modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public String toString() {
		return "ResearchLedgerTechnical{" +
				"srlId=" + srlId +
				", stock=" + stock +
				", researchStatus=" + researchStatus +
				", researchDate=" + researchDate +
				", exitDate=" + exitDate +
				", researchPrice=" + researchPrice +
				", exitPrice=" + exitPrice +
				'}';
	}

}
