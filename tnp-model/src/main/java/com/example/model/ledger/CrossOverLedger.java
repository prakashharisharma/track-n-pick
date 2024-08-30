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
	
	public enum CrossOverCategory { CROSS20, CROSS50, CROSS100, CROSS200 , VIPR, VIPF, BO50BULLISH, BO50BEARISH, BO200BULLISH, BO200BEARISH }
	
	public enum Status {OPEN, CLOSE};
	
	
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
	
	@Column(name = "CLOSE_DATE")
	LocalDate closeDate = LocalDate.now();
	
	@Column(name = "CROSSOVER_TYPE")
	@Enumerated(EnumType.STRING)
	CrossOverType crossOverType;
	
	@Column(name = "CATEGORY")
	@Enumerated(EnumType.STRING)
	CrossOverCategory crossOverCategory;
	
	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	Status status;

	@Column(name = "SHORT_AVG", columnDefinition="Decimal(10,2) default '0.00'")
	double shortAvg;
	
	@Column(name = "LONG_AVG", columnDefinition="Decimal(10,2) default '0.00'")
	double longAvg;
	
	@Column(name = "PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double price;
	
	@Column(name = "PREV_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevClose;
	
	@Column(name = "VOLUME")
	Long volume;
	
	@Column(name = "AVG_VOLUME")
	Long avgVolume;
	
	public CrossOverLedger() {
		super();
	}

	public CrossOverLedger(Stock stockId, LocalDate researchDate, CrossOverType crossOverType,CrossOverCategory crossOverCategory,double shortAvg,double longAvg,Status status) {
		super();
		this.stockId = stockId;
		this.researchDate = researchDate;
		this.crossOverType = crossOverType;
		this.crossOverCategory = crossOverCategory;
		this.shortAvg = shortAvg;
		this.longAvg =longAvg;
		this.status = status;
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

	public CrossOverCategory getCrossOverCategory() {
		return crossOverCategory;
	}

	public void setCrossOverCategory(CrossOverCategory crossOverCategory) {
		this.crossOverCategory = crossOverCategory;
	}

	public double getShortAvg() {
		return shortAvg;
	}

	public void setShortAvg(double shortAvg) {
		this.shortAvg = shortAvg;
	}

	public double getLongAvg() {
		return longAvg;
	}

	public void setLongAvg(double longAvg) {
		this.longAvg = longAvg;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public LocalDate getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(LocalDate closeDate) {
		this.closeDate = closeDate;
	}


	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public long getVolume() {
		return volume != null ? volume : 0;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getAvgVolume() {
		return avgVolume != null ? avgVolume : 0;
	}

	public void setAvgVolume(long avgVolume) {
		this.avgVolume = avgVolume;
	}

	public double getPrevClose() {
		return prevClose;
	}

	public void setPrevClose(double prevClose) {
		this.prevClose = prevClose;
	}

	@Override
	public String toString() {
		return "CrossOverLedger [crossOverId=" + crossOverId + ", stockId=" + stockId + ", researchDate=" + researchDate
				+ ", crossOverType=" + crossOverType + "]";
	}
	
}
