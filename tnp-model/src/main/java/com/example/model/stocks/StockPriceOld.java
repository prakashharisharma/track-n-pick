package com.example.model.stocks;

import java.io.Serializable;
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "STOCK_PRICE_OLD")
@JsonIgnoreProperties(ignoreUnknown = true, 
value = {"Volume", "PercentageDiff", "ChangePercent"})
public class StockPriceOld implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1495123844495916776L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STOCK_PRICE_ID")
	long stockPriceId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;

	@JsonProperty("LastTradedPrice")
	@Column(name = "CURRENT_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double currentPrice;


	@Column(name = "OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double open;
	@Column(name = "HIGH", columnDefinition="Decimal(10,2) default '0.00'")
	double high;
	@Column(name = "LOW", columnDefinition="Decimal(10,2) default '0.00'")
	double low;
	@Column(name = "CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double close;


	@Column(name = "PREV_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevOpen;
	@Column(name = "PREV_HIGH", columnDefinition="Decimal(10,2) default '0.00'")
	double prevHigh;
	@Column(name = "PREV_LOW", columnDefinition="Decimal(10,2) default '0.00'")
	double prevLow;
	@Column(name = "PREV_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevClose;


	@Column(name = "PREV_PREV_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevOpen;
	@Column(name = "PREV_PREV_HIGH", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevHigh;
	@Column(name = "PREV_PREV_LOW", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevLow;
	@Column(name = "PREV_PREV_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevClose;


	@JsonProperty("FiftyTwoWeekLow")
	@Column(name = "YEAR_LOW", columnDefinition="Decimal(10,2) default '0.00'")
	double yearLow;
	@Column(name = "YEAR_LOW_DATE")
	LocalDate yearLowDate = LocalDate.now();
	@JsonProperty("FiftyTwoWeekHigh")
	@Column(name = "YEAR_HIGH", columnDefinition="Decimal(10,2) default '0.00'")
	double yearHigh;
	@Column(name = "YEAR_HIGH_DATE")
	LocalDate yearHighDate = LocalDate.now();


	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();

	@Column(name = "BHAV_DATE")
	LocalDate bhavDate = LocalDate.now();
	@Column(name = "PREV_BHAV_DATE")
	LocalDate prevBhavDate = LocalDate.now();
	@Column(name = "PREV_PREV_BHAV_DATE")
	LocalDate prevPrevBhavDate = LocalDate.now();


}
