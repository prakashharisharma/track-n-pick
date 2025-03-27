package com.example.model.stocks;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.*;

import com.example.model.master.Stock;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "STOCK_TECHNICALS_OLD")
public class StockTechnicalsOld implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5870154333285682947L;


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STOCK_TECHNICALS_ID")
	long stockTechnicalsId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;

	@Column(name = "BHAV_DATE")
	LocalDate bhavDate = LocalDate.now();
	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();


	@Column(name = "SMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double sma5;
	@Column(name = "PREV_SMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma5;

	@Column(name = "SMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double sma10;
	@Column(name = "PREV_SMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma10;

	@Column(name = "SMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double sma20;
	@Column(name = "PREV_SMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma20;

	@Column(name = "SMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double sma50;
	@Column(name = "PREV_SMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma50;
	
	@Column(name = "SMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double sma100;
	@Column(name = "PREV_SMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma100;
	
	@Column(name = "SMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double sma200;
	@Column(name = "PREV_SMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma200;


	@Column(name = "EMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double ema5;
	@Column(name = "PREV_EMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma5;
	@Column(name = "PREV_PREV_EMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevEma5;

	@Column(name = "EMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double ema10;
	@Column(name = "PREV_EMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma10;
	@Column(name = "PREV_PREV_EMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevEma10;

	@Column(name = "EMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double ema20;
	@Column(name = "PREV_EMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma20;
	@Column(name = "PREV_PREV_EMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevEma20;

	@Column(name = "EMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double ema50;
	@Column(name = "PREV_EMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma50;
	@Column(name = "PREV_PREV_EMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevEma50;

	@Column(name = "EMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double ema100;
	@Column(name = "PREV_EMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma100;
	@Column(name = "PREV_PREV_EMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevEma100;

	@Column(name = "EMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double ema200;
	@Column(name = "PREV_EMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma200;
	@Column(name = "PREV_PREV_EMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevEma200;


	@Column(name = "RSI", columnDefinition="decimal(10,2) default '0.00'")
	double rsi;
	@Column(name = "PREV_RSI", columnDefinition="decimal(10,2) default '0.00'")
	double prevRsi;
	@Column(name = "PREV_PREV_RSI", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevRsi;

	@Column(name = "MACD", columnDefinition="decimal(10,2) default '0.00'")
	double macd;
	@Column(name = "PREV_MACD", columnDefinition="decimal(10,2) default '0.00'")
	double prevMacd;
	@Column(name = "PREV_PREV_MACD", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevMacd;

	@Column(name = "SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
	double signal;
	@Column(name = "PREV_SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
	double prevSignal;
	@Column(name = "PREV_PREV_SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevSignal;


	@Column(name = "OBV", columnDefinition="bigint default '0'")
	Long obv;
	@Column(name = "PREV_OBV", columnDefinition="bigint default '0'")
	Long prevObv;
	@Column(name = "PREV_PREV_OBV", columnDefinition="bigint default '0'")
	Long prevPrevObv;

	@Column(name = "OBV_AVG", columnDefinition="bigint default '0'")
	Long obvAvg;
	@Column(name = "PREV_OBV_AVG", columnDefinition="bigint default '0'")
	Long prevObvAvg;
	@Column(name = "PREV_PREV_OBV_AVG", columnDefinition="bigint default '0'")
	Long prevPrevObvAvg;


	@Column(name = "VOLUME", columnDefinition="bigint default '0'")
	Long volume;
	@Column(name = "PREV_VOLUME", columnDefinition="bigint default '0'")
	Long prevVolume;
	@Column(name = "PREV_PREV_VOLUME", columnDefinition="bigint default '0'")
	Long prevPrevVolume;
	
	@Column(name = "VOLUME_AVG5", columnDefinition="bigint default '0'")
	Long volumeAvg5;
	@Column(name = "PREV_VOLUME_AVG5", columnDefinition="bigint default '0'")
	Long prevVolumeAvg5;
	@Column(name = "PREV_PREV_VOLUME_AVG5", columnDefinition="bigint default '0'")
	Long prevPrevVolumeAvg5;

	@Column(name = "VOLUME_AVG20", columnDefinition="bigint default '0'")
	Long volumeAvg20;
	@Column(name = "PREV_VOLUME_AVG20", columnDefinition="bigint default '0'")
	Long prevVolumeAvg20;
	@Column(name = "PREV_PREV_VOLUME_AVG20", columnDefinition="bigint default '0'")
	Long prevPrevVolumeAvg20;

	@Column(name = "VOLUME_AVG50", columnDefinition="bigint default '0'")
	Long volumeAvg50;
	@Column(name = "PREV_VOLUME_AVG50", columnDefinition="bigint default '0'")
	Long prevVolumeAvg50;
	@Column(name = "PREV_PREV_VOLUME_AVG50", columnDefinition="bigint default '0'")
	Long prevPrevVolumeAvg50;


	@Column(name = "ADX", columnDefinition="decimal(10,2) default '0.00'")
	double adx;
	@Column(name = "PREV_ADX", columnDefinition="decimal(10,2) default '0.00'")
	double prevAdx;
	@Column(name = "PREV_PREV_ADX", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevAdx;

	@Column(name = "PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double plusDi;
	@Column(name = "PREV_PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double prevPlusDi;
	@Column(name = "PREV_PREV_PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPlusDi;

	@Column(name = "MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double minusDi;
	@Column(name = "PREV_MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double prevMinusDi;
	@Column(name = "PREV_PREV_MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevMinusDi;


	@Column(name = "PREV_YEAR_OPEN", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearOpen;
	@Column(name = "PREV_YEAR_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearHigh;
	@Column(name = "PREV_YEAR_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearLow;
	@Column(name = "PREV_YEAR_CLOSE", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearClose;

	@Column(name = "PREV_PREV_YEAR_OPEN", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevYearOpen;
	@Column(name = "PREV_PREV_YEAR_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevYearHigh;
	@Column(name = "PREV_PREV_YEAR_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevYearLow;
	@Column(name = "PREV_PREV_YEAR_CLOSE", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevYearClose;

	@Column(name = "PREV_PREV_PREV_YEAR_OPEN", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevYearOpen;
	@Column(name = "PREV_PREV_PREV_YEAR_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevYearHigh;
	@Column(name = "PREV_PREV_PREV_YEAR_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevYearLow;
	@Column(name = "PREV_PREV_PREV_YEAR_CLOSE", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevYearClose;


	@Column(name = "PREV_QUARTER_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevQuarterOpen;
	@Column(name = "PREV_QUARTER_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevQuarterHigh;
	@Column(name = "PREV_QUARTER_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevQuarterLow;
	@Column(name = "PREV_QUARTER_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevQuarterClose;

	@Column(name = "PREV_PREV_QUARTER_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevQuarterOpen;
	@Column(name = "PREV_PREV_QUARTER_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevQuarterHigh;
	@Column(name = "PREV_PREV_QUARTER_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevQuarterLow;
	@Column(name = "PREV_PREV_QUARTER_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevQuarterClose;

	@Column(name = "PREV_PREV_PREV_QUARTER_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevPrevQuarterOpen;
	@Column(name = "PREV_PREV_PREV_QUARTER_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevQuarterHigh;
	@Column(name = "PREV_PREV_PREV_QUARTER_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevQuarterLow;
	@Column(name = "PREV_PREV_PREV_QUARTER_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevPrevQuarterClose;


	@Column(name = "PREV_MONTH_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevMonthOpen;
	@Column(name = "PREV_MONTH_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevMonthHigh;
	@Column(name = "PREV_MONTH_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevMonthLow;
	@Column(name = "PREV_MONTH_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevMonthClose;

	@Column(name = "PREV_PREV_MONTH_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevMonthOpen;
	@Column(name = "PREV_PREV_MONTH_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevMonthHigh;
	@Column(name = "PREV_PREV_MONTH_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevMonthLow;
	@Column(name = "PREV_PREV_MONTH_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevMonthClose;

	@Column(name = "PREV_PREV_PREV_MONTH_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevPrevMonthOpen;
	@Column(name = "PREV_PREV_PREV_MONTH_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevMonthHigh;
	@Column(name = "PREV_PREV_PREV_MONTH_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevMonthLow;
	@Column(name = "PREV_PREV_PREV_MONTH_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevPrevMonthClose;


	@Column(name = "PREV_WEEK_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevWeekOpen;
	@Column(name = "PREV_WEEK_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevWeekHigh;
	@Column(name = "PREV_WEEK_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevWeekLow;
	@Column(name = "PREV_WEEK_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevWeekClose;

	@Column(name = "PREV_PREV_WEEK_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevWeekOpen;
	@Column(name = "PREV_PREV_WEEK_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevWeekHigh;
	@Column(name = "PREV_PREV_WEEK_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevWeekLow;
	@Column(name = "PREV_PREV_WEEK_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevWeekClose;

	@Column(name = "PREV_PREV_PREV_WEEK_OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevPrevWeekOpen;
	@Column(name = "PREV_PREV_PREV_WEEK_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevWeekHigh;
	@Column(name = "PREV_PREV_PREV_WEEK_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevPrevPrevWeekLow;
	@Column(name = "PREV_PREV_PREV_WEEK_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevPrevPrevWeekClose;


}
