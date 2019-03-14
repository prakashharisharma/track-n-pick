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
@Table(name = "CROSSOVER_LEDGER")
public class CrossOverLedger implements Serializable{

	public enum CrossOverType {BULLISH, BEARISH}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8164636241914901195L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "CROSSOVER_LEDGER_ID")
	long crossOverId;

	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "RESEARCH_DATE")
	LocalDate researchDate = LocalDate.now();
	
	@Column(name = "CROSSOVER_TYPE")
	@Enumerated(EnumType.STRING)
	CrossOverType crossOverType;

	public CrossOverLedger() {
		super();
	}

	public CrossOverLedger(Stock stockId, LocalDate researchDate, CrossOverType crossOverType) {
		super();
		this.stockId = stockId;
		this.researchDate = researchDate;
		this.crossOverType = crossOverType;
	}

	public long getCrossOverId() {
		return crossOverId;
	}

	public void setCrossOverId(long crossOverId) {
		this.crossOverId = crossOverId;
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

	public CrossOverType getCrossOverType() {
		return crossOverType;
	}

	public void setCrossOverType(CrossOverType crossOverType) {
		this.crossOverType = crossOverType;
	}

	@Override
	public String toString() {
		return "CrossOverLedger [crossOverId=" + crossOverId + ", stockId=" + stockId + ", researchDate=" + researchDate
				+ ", crossOverType=" + crossOverType + "]";
	}
	
}
