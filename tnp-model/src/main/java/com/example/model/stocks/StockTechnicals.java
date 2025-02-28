package com.example.model.stocks;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.*;

import com.example.model.master.Stock;
import com.example.util.io.model.type.Trend;

@Entity
@Table(name = "STOCK_TECHNICALS")
public class StockTechnicals implements Serializable{

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

	@Column(name = "TREND")
	@Enumerated(EnumType.STRING)
	private Trend trend = Trend.UP;
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

	@Column(name = "MACD", columnDefinition="decimal(10,2) default '0.00'")
	double macd;

	@Column(name = "PREV_MACD", columnDefinition="decimal(10,2) default '0.00'")
	double prevMacd;


	@Column(name = "SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
	double signal;

	@Column(name = "PREV_SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
	double prevSignal;
	
	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();

	@Column(name = "OBV", columnDefinition="decimal(10,2) default '0.00'")
	Long obv;

	@Column(name = "OBV_AVG", columnDefinition="decimal(10,2) default '0.00'")
	Long obvAvg;

	@Column(name = "PREV_OBV", columnDefinition="decimal(10,2) default '0.00'")
	Long prevObv;

	@Column(name = "PREV_OBV_AVG", columnDefinition="decimal(10,2) default '0.00'")
	Long prevObvAvg;

	@Column(name = "PREV_PREV_OBV", columnDefinition="decimal(10,2) default '0.00'")
	Long prevPrevObv;

	@Column(name = "PREV_PREV_OBV_AVG", columnDefinition="decimal(10,2) default '0.00'")
	Long prevPrevObvAvg;

	@Column(name = "VOLUME")
	Long volume;

	@Column(name = "VOLUME_PREV")
	Long volumePrev;
	
	@Column(name = "VOLUME_AVG5")
	Long volumeAvg5;

	@Column(name = "VOLUME_AVG5_PREV")
	Long volumeAvg5Prev;

	@Column(name = "VOLUME_AVG20")
	Long volumeAvg20;

	@Column(name = "VOLUME_AVG20_PREV")
	Long volumeAvg20Prev;

	@Column(name = "VOLUME_AVG50")
	Long volumeAvg50;

	@Column(name = "VOLUME_AVG50_PREV")
	Long volumeAvg50Prev;

	@Column(name = "ADX", columnDefinition="decimal(10,2) default '0.00'")
	double adx;

	@Column(name = "PREV_ADX", columnDefinition="decimal(10,2) default '0.00'")
	double prevAdx;

	@Column(name = "PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double plusDi;

	@Column(name = "PREV_PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double prevPlusDi;

	@Column(name = "MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double minusDi;

	@Column(name = "PREV_MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
	double prevMinusDi;

	@Column(name = "PIVOT_POINT", columnDefinition="decimal(10,2) default '0.00'")
	double pivotPoint;

	@Column(name = "S1", columnDefinition="decimal(10,2) default '0.00'")
	double firstSupport;

	@Column(name = "S2", columnDefinition="decimal(10,2) default '0.00'")
	double secondSupport;

	@Column(name = "S3", columnDefinition="decimal(10,2) default '0.00'")
	double thirdSupport;

	@Column(name = "R1", columnDefinition="decimal(10,2) default '0.00'")
	double firstResistance;

	@Column(name = "R2", columnDefinition="decimal(10,2) default '0.00'")
	double secondResistance;

	@Column(name = "R3", columnDefinition="decimal(10,2) default '0.00'")
	double thirdResistance;

	@Column(name = "PREV_PIVOT_POINT", columnDefinition="decimal(10,2) default '0.00'")
	double prevPivotPoint;

	@Column(name = "PREV_S1", columnDefinition="decimal(10,2) default '0.00'")
	double prevFirstSupport;

	@Column(name = "PREV_S2", columnDefinition="decimal(10,2) default '0.00'")
	double prevSecondSupport;

	@Column(name = "PREV_S3", columnDefinition="decimal(10,2) default '0.00'")
	double prevThirdSupport;

	@Column(name = "PREV_R1", columnDefinition="decimal(10,2) default '0.00'")
	double prevFirstResistance;

	@Column(name = "PREV_R2", columnDefinition="decimal(10,2) default '0.00'")
	double prevSecondResistance;

	@Column(name = "PREV_R3", columnDefinition="decimal(10,2) default '0.00'")
	double prevThirdResistance;

	@Column(name = "PREV_YEAR_OPEN", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearOpen;
	@Column(name = "PREV_YEAR_HIGH", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearHigh;
	@Column(name = "PREV_YEAR_LOW", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearLow;
	@Column(name = "PREV_YEAR_CLOSE", columnDefinition="decimal(10,2) default '0.00'")
	double prevYearClose;

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

	public long getStockTechnicalsId() {
		return stockTechnicalsId;
	}

	public void setStockTechnicalsId(long stockTechnicalsId) {
		this.stockTechnicalsId = stockTechnicalsId;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public LocalDate getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(LocalDate bhavDate) {
		this.bhavDate = bhavDate;
	}

	public Trend getTrend() {
		return trend;
	}

	public void setTrend(Trend trend) {
		this.trend = trend;
	}

	public double getSma5() {
		return sma5;
	}

	public void setSma5(double sma5) {
		this.sma5 = sma5;
	}

	public double getPrevSma5() {
		return prevSma5;
	}

	public void setPrevSma5(double prevSma5) {
		this.prevSma5 = prevSma5;
	}

	public double getSma10() {
		return sma10;
	}

	public void setSma10(double sma10) {
		this.sma10 = sma10;
	}

	public double getPrevSma10() {
		return prevSma10;
	}

	public void setPrevSma10(double prevSma10) {
		this.prevSma10 = prevSma10;
	}

	public double getSma20() {
		return sma20;
	}

	public void setSma20(double sma20) {
		this.sma20 = sma20;
	}

	public double getPrevSma20() {
		return prevSma20;
	}

	public void setPrevSma20(double prevSma20) {
		this.prevSma20 = prevSma20;
	}

	public double getSma50() {
		return sma50;
	}

	public void setSma50(double sma50) {
		this.sma50 = sma50;
	}

	public double getPrevSma50() {
		return prevSma50;
	}

	public void setPrevSma50(double prevSma50) {
		this.prevSma50 = prevSma50;
	}

	public double getSma100() {
		return sma100;
	}

	public void setSma100(double sma100) {
		this.sma100 = sma100;
	}

	public double getPrevSma100() {
		return prevSma100;
	}

	public void setPrevSma100(double prevSma100) {
		this.prevSma100 = prevSma100;
	}

	public double getSma200() {
		return sma200;
	}

	public void setSma200(double sma200) {
		this.sma200 = sma200;
	}

	public double getPrevSma200() {
		return prevSma200;
	}

	public void setPrevSma200(double prevSma200) {
		this.prevSma200 = prevSma200;
	}

	public double getEma5() {
		return ema5;
	}

	public void setEma5(double ema5) {
		this.ema5 = ema5;
	}

	public double getPrevEma5() {
		return prevEma5;
	}

	public void setPrevEma5(double prevEma5) {
		this.prevEma5 = prevEma5;
	}

	public double getPrevPrevEma5() {
		return prevPrevEma5;
	}

	public void setPrevPrevEma5(double prevPrevEma5) {
		this.prevPrevEma5 = prevPrevEma5;
	}

	public double getEma10() {
		return ema10;
	}

	public void setEma10(double ema10) {
		this.ema10 = ema10;
	}

	public double getPrevEma10() {
		return prevEma10;
	}

	public void setPrevEma10(double prevEma10) {
		this.prevEma10 = prevEma10;
	}

	public double getEma20() {
		return ema20;
	}

	public void setEma20(double ema20) {
		this.ema20 = ema20;
	}

	public double getPrevEma20() {
		return prevEma20;
	}

	public void setPrevEma20(double prevEma20) {
		this.prevEma20 = prevEma20;
	}

	public double getPrevPrevEma10() {
		return prevPrevEma10;
	}

	public void setPrevPrevEma10(double prevPrevEma10) {
		this.prevPrevEma10 = prevPrevEma10;
	}

	public double getPrevPrevEma20() {
		return prevPrevEma20;
	}

	public void setPrevPrevEma20(double prevPrevEma20) {
		this.prevPrevEma20 = prevPrevEma20;
	}

	public double getPrevPrevEma50() {
		return prevPrevEma50;
	}

	public void setPrevPrevEma50(double prevPrevEma50) {
		this.prevPrevEma50 = prevPrevEma50;
	}

	public double getEma50() {
		return ema50;
	}

	public void setEma50(double ema50) {
		this.ema50 = ema50;
	}

	public double getPrevEma50() {
		return prevEma50;
	}

	public void setPrevEma50(double prevEma50) {
		this.prevEma50 = prevEma50;
	}

	public double getEma100() {
		return ema100;
	}

	public void setEma100(double ema100) {
		this.ema100 = ema100;
	}

	public double getPrevEma100() {
		return prevEma100;
	}

	public void setPrevEma100(double prevEma100) {
		this.prevEma100 = prevEma100;
	}

	public double getEma200() {
		return ema200;
	}

	public void setEma200(double ema200) {
		this.ema200 = ema200;
	}

	public double getPrevEma200() {
		return prevEma200;
	}

	public void setPrevEma200(double prevEma200) {
		this.prevEma200 = prevEma200;
	}

	public double getRsi() {
		return rsi;
	}

	public void setRsi(double rsi) {
		this.rsi = rsi;
	}

	public double getPrevRsi() {
		return prevRsi;
	}

	public void setPrevRsi(double prevRsi) {
		this.prevRsi = prevRsi;
	}

	public double getMacd() {
		return macd;
	}

	public void setMacd(double macd) {
		this.macd = macd;
	}

	public double getPrevMacd() {
		return prevMacd;
	}

	public void setPrevMacd(double prevMacd) {
		this.prevMacd = prevMacd;
	}

	public double getSignal() {
		return signal;
	}

	public void setSignal(double signal) {
		this.signal = signal;
	}

	public double getPrevSignal() {
		return prevSignal;
	}

	public void setPrevSignal(double prevSignal) {
		this.prevSignal = prevSignal;
	}

	public LocalDate getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDate lastModified) {
		this.lastModified = lastModified;
	}

	public Long getObv() {
		return obv;
	}

	public void setObv(Long obv) {
		this.obv = obv;
	}

	public Long getObvAvg() {
		return obvAvg;
	}

	public void setObvAvg(Long obvAvg) {
		this.obvAvg = obvAvg;
	}

	public Long getPrevObv() {
		return prevObv;
	}

	public void setPrevObv(Long prevObv) {
		this.prevObv = prevObv;
	}

	public Long getPrevObvAvg() {
		return prevObvAvg;
	}

	public void setPrevObvAvg(Long prevObvAvg) {
		this.prevObvAvg = prevObvAvg;
	}

	public Long getPrevPrevObv() {
		return prevPrevObv;
	}

	public void setPrevPrevObv(Long prevPrevObv) {
		this.prevPrevObv = prevPrevObv;
	}

	public Long getPrevPrevObvAvg() {
		return prevPrevObvAvg;
	}

	public void setPrevPrevObvAvg(Long prevPrevObvAvg) {
		this.prevPrevObvAvg = prevPrevObvAvg;
	}

	public Long getVolume() {
		return volume!= null ? volume : 1l;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Long getVolumeAvg5() {
		return volumeAvg5;
	}

	public void setVolumeAvg5(Long volumeAvg5) {
		this.volumeAvg5 = volumeAvg5;
	}

	public Long getVolumeAvg5Prev() {
		return volumeAvg5Prev;
	}

	public void setVolumeAvg5Prev(Long volumeAvg5Prev) {
		this.volumeAvg5Prev = volumeAvg5Prev;
	}

	public Long getVolumePrev() {
		return volumePrev;
	}

	public void setVolumePrev(Long volumePrev) {
		this.volumePrev = volumePrev;
	}

	public Long getVolumeAvg20() {
		return volumeAvg20;
	}

	public void setVolumeAvg20(Long volumeAvg20) {
		this.volumeAvg20 = volumeAvg20;
	}

	public Long getVolumeAvg20Prev() {
		return volumeAvg20Prev;
	}

	public void setVolumeAvg20Prev(Long volumeAvg20Prev) {
		this.volumeAvg20Prev = volumeAvg20Prev;
	}


	public double getAdx() {
		return adx;
	}

	public void setAdx(double adx) {
		this.adx = adx;
	}

	public double getPlusDi() {
		return plusDi;
	}

	public void setPlusDi(double plusDi) {
		this.plusDi = plusDi;
	}

	public double getMinusDi() {
		return minusDi;
	}

	public void setMinusDi(double minusDi) {
		this.minusDi = minusDi;
	}

	public double getPrevAdx() {
		return prevAdx;
	}

	public void setPrevAdx(double prevAdx) {
		this.prevAdx = prevAdx;
	}

	public double getPrevPlusDi() {
		return prevPlusDi;
	}

	public void setPrevPlusDi(double prevPlusDi) {
		this.prevPlusDi = prevPlusDi;
	}

	public double getPrevMinusDi() {
		return prevMinusDi;
	}

	public void setPrevMinusDi(double prevMinusDi) {
		this.prevMinusDi = prevMinusDi;
	}

	public double getPivotPoint() {
		return pivotPoint;
	}

	public void setPivotPoint(double pivotPoint) {
		this.pivotPoint = pivotPoint;
	}

	public double getFirstSupport() {
		return firstSupport;
	}

	public void setFirstSupport(double firstSupport) {
		this.firstSupport = firstSupport;
	}

	public double getSecondSupport() {
		return secondSupport;
	}

	public void setSecondSupport(double secondSupport) {
		this.secondSupport = secondSupport;
	}

	public double getThirdSupport() {
		return thirdSupport;
	}

	public void setThirdSupport(double thirdSupport) {
		this.thirdSupport = thirdSupport;
	}

	public double getFirstResistance() {
		return firstResistance;
	}

	public void setFirstResistance(double firstResistance) {
		this.firstResistance = firstResistance;
	}

	public double getSecondResistance() {
		return secondResistance;
	}

	public void setSecondResistance(double secondResistance) {
		this.secondResistance = secondResistance;
	}

	public double getThirdResistance() {
		return thirdResistance;
	}

	public void setThirdResistance(double thirdResistance) {
		this.thirdResistance = thirdResistance;
	}

	public double getPrevPivotPoint() {
		return prevPivotPoint;
	}

	public void setPrevPivotPoint(double prevPivotPoint) {
		this.prevPivotPoint = prevPivotPoint;
	}

	public double getPrevFirstSupport() {
		return prevFirstSupport;
	}

	public void setPrevFirstSupport(double prevFirstSupport) {
		this.prevFirstSupport = prevFirstSupport;
	}

	public double getPrevSecondSupport() {
		return prevSecondSupport;
	}

	public void setPrevSecondSupport(double prevSecondSupport) {
		this.prevSecondSupport = prevSecondSupport;
	}

	public double getPrevThirdSupport() {
		return prevThirdSupport;
	}

	public void setPrevThirdSupport(double prevThirdSupport) {
		this.prevThirdSupport = prevThirdSupport;
	}

	public double getPrevFirstResistance() {
		return prevFirstResistance;
	}

	public void setPrevFirstResistance(double prevFirstResistance) {
		this.prevFirstResistance = prevFirstResistance;
	}

	public double getPrevSecondResistance() {
		return prevSecondResistance;
	}

	public void setPrevSecondResistance(double prevSecondResistance) {
		this.prevSecondResistance = prevSecondResistance;
	}

	public double getPrevThirdResistance() {
		return prevThirdResistance;
	}

	public void setPrevThirdResistance(double prevThirdResistance) {
		this.prevThirdResistance = prevThirdResistance;
	}

	public Long getVolumeAvg50() {
		return volumeAvg50;
	}

	public void setVolumeAvg50(Long volumeAvg50) {
		this.volumeAvg50 = volumeAvg50;
	}

	public Long getVolumeAvg50Prev() {
		return volumeAvg50Prev;
	}

	public void setVolumeAvg50Prev(Long volumeAvg50Prev) {
		this.volumeAvg50Prev = volumeAvg50Prev;
	}

	public double getPrevPrevEma100() {
		return prevPrevEma100;
	}

	public void setPrevPrevEma100(double prevPrevEma100) {
		this.prevPrevEma100 = prevPrevEma100;
	}

	public double getPrevPrevEma200() {
		return prevPrevEma200;
	}

	public void setPrevPrevEma200(double prevPrevEma200) {
		this.prevPrevEma200 = prevPrevEma200;
	}

	public double getPrevMonthHigh() {
		return prevMonthHigh;
	}

	public void setPrevMonthHigh(double prevMonthHigh) {
		this.prevMonthHigh = prevMonthHigh;
	}

	public double getPrevMonthLow() {
		return prevMonthLow;
	}

	public void setPrevMonthLow(double prevMonthLow) {
		this.prevMonthLow = prevMonthLow;
	}

	public double getPrevWeekHigh() {
		return prevWeekHigh;
	}

	public void setPrevWeekHigh(double prevWeekHigh) {
		this.prevWeekHigh = prevWeekHigh;
	}

	public double getPrevWeekLow() {
		return prevWeekLow;
	}

	public void setPrevWeekLow(double prevWeekLow) {
		this.prevWeekLow = prevWeekLow;
	}

	public double getPrevWeekOpen() {
		return prevWeekOpen;
	}

	public void setPrevWeekOpen(double prevWeekOpen) {
		this.prevWeekOpen = prevWeekOpen;
	}

	public double getPrevWeekClose() {
		return prevWeekClose;
	}

	public void setPrevWeekClose(double prevWeekClose) {
		this.prevWeekClose = prevWeekClose;
	}

	public double getPrevPrevWeekOpen() {
		return prevPrevWeekOpen;
	}

	public void setPrevPrevWeekOpen(double prevPrevWeekOpen) {
		this.prevPrevWeekOpen = prevPrevWeekOpen;
	}

	public double getPrevPrevWeekHigh() {
		return prevPrevWeekHigh;
	}

	public void setPrevPrevWeekHigh(double prevPrevWeekHigh) {
		this.prevPrevWeekHigh = prevPrevWeekHigh;
	}

	public double getPrevPrevWeekLow() {
		return prevPrevWeekLow;
	}

	public void setPrevPrevWeekLow(double prevPrevWeekLow) {
		this.prevPrevWeekLow = prevPrevWeekLow;
	}

	public double getPrevPrevWeekClose() {
		return prevPrevWeekClose;
	}

	public void setPrevPrevWeekClose(double prevPrevWeekClose) {
		this.prevPrevWeekClose = prevPrevWeekClose;
	}

	public double getPrevYearHigh() {
		return prevYearHigh;
	}

	public void setPrevYearHigh(double prevYearHigh) {
		this.prevYearHigh = prevYearHigh;
	}

	public double getPrevYearLow() {
		return prevYearLow;
	}

	public void setPrevYearLow(double prevYearLow) {
		this.prevYearLow = prevYearLow;
	}

	public double getPrevYearOpen() {
		return prevYearOpen;
	}

	public void setPrevYearOpen(double prevYearOpen) {
		this.prevYearOpen = prevYearOpen;
	}

	public double getPrevYearClose() {
		return prevYearClose;
	}

	public void setPrevYearClose(double prevYearClose) {
		this.prevYearClose = prevYearClose;
	}

	public double getPrevQuarterOpen() {
		return prevQuarterOpen;
	}

	public void setPrevQuarterOpen(double prevQuarterOpen) {
		this.prevQuarterOpen = prevQuarterOpen;
	}

	public double getPrevQuarterHigh() {
		return prevQuarterHigh;
	}

	public void setPrevQuarterHigh(double prevQuarterHigh) {
		this.prevQuarterHigh = prevQuarterHigh;
	}

	public double getPrevQuarterLow() {
		return prevQuarterLow;
	}

	public void setPrevQuarterLow(double prevQuarterLow) {
		this.prevQuarterLow = prevQuarterLow;
	}

	public double getPrevQuarterClose() {
		return prevQuarterClose;
	}

	public void setPrevQuarterClose(double prevQuarterClose) {
		this.prevQuarterClose = prevQuarterClose;
	}

	public double getPrevPrevQuarterOpen() {
		return prevPrevQuarterOpen;
	}

	public void setPrevPrevQuarterOpen(double prevPrevQuarterOpen) {
		this.prevPrevQuarterOpen = prevPrevQuarterOpen;
	}

	public double getPrevPrevQuarterHigh() {
		return prevPrevQuarterHigh;
	}

	public void setPrevPrevQuarterHigh(double prevPrevQuarterHigh) {
		this.prevPrevQuarterHigh = prevPrevQuarterHigh;
	}

	public double getPrevPrevQuarterLow() {
		return prevPrevQuarterLow;
	}

	public void setPrevPrevQuarterLow(double prevPrevQuarterLow) {
		this.prevPrevQuarterLow = prevPrevQuarterLow;
	}

	public double getPrevPrevQuarterClose() {
		return prevPrevQuarterClose;
	}

	public void setPrevPrevQuarterClose(double prevPrevQuarterClose) {
		this.prevPrevQuarterClose = prevPrevQuarterClose;
	}

	public double getPrevMonthOpen() {
		return prevMonthOpen;
	}

	public void setPrevMonthOpen(double prevMonthOpen) {
		this.prevMonthOpen = prevMonthOpen;
	}

	public double getPrevMonthClose() {
		return prevMonthClose;
	}

	public void setPrevMonthClose(double prevMonthClose) {
		this.prevMonthClose = prevMonthClose;
	}

	public double getPrevPrevMonthOpen() {
		return prevPrevMonthOpen;
	}

	public void setPrevPrevMonthOpen(double prevPrevMonthOpen) {
		this.prevPrevMonthOpen = prevPrevMonthOpen;
	}

	public double getPrevPrevMonthHigh() {
		return prevPrevMonthHigh;
	}

	public void setPrevPrevMonthHigh(double prevPrevMonthHigh) {
		this.prevPrevMonthHigh = prevPrevMonthHigh;
	}

	public double getPrevPrevMonthLow() {
		return prevPrevMonthLow;
	}

	public void setPrevPrevMonthLow(double prevPrevMonthLow) {
		this.prevPrevMonthLow = prevPrevMonthLow;
	}

	public double getPrevPrevMonthClose() {
		return prevPrevMonthClose;
	}

	public void setPrevPrevMonthClose(double prevPrevMonthClose) {
		this.prevPrevMonthClose = prevPrevMonthClose;
	}

	@Override
	public String toString() {
		return "StockTechnicals{" +
				"stockTechnicalsId=" + stockTechnicalsId +
				", stock=" + stock +
				", bhavDate=" + bhavDate +
				", sma5=" + sma5 +
				", prevSma5=" + prevSma5 +
				", sma10=" + sma10 +
				", prevSma10=" + prevSma10 +
				", sma20=" + sma20 +
				", prevSma20=" + prevSma20 +
				", sma50=" + sma50 +
				", prevSma50=" + prevSma50 +
				", sma100=" + sma100 +
				", prevSma100=" + prevSma100 +
				", sma200=" + sma200 +
				", prevSma200=" + prevSma200 +
				", ema5=" + ema5 +
				", prevEma5=" + prevEma5 +
				", ema10=" + ema10 +
				", prevEma10=" + prevEma10 +
				", ema20=" + ema20 +
				", prevEma20=" + prevEma20 +
				", ema50=" + ema50 +
				", prevEma50=" + prevEma50 +
				", ema100=" + ema100 +
				", prevEma100=" + prevEma100 +
				", ema200=" + ema200 +
				", prevEma200=" + prevEma200 +
				", rsi=" + rsi +
				", prevRsi=" + prevRsi +
				", macd=" + macd +
				", prevMacd=" + prevMacd +
				", signal=" + signal +
				", prevSignal=" + prevSignal +
				", lastModified=" + lastModified +
				", volume=" + volume +
				", avgVolume=" + volumeAvg5 +
				", adx=" + adx +
				", prevAdx=" + prevAdx +
				", plusDi=" + plusDi +
				", minusDi=" + minusDi +
				'}';
	}
}
