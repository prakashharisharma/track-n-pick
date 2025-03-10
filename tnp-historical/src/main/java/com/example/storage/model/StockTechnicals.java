package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "technicals_history")
public class StockTechnicals {

	@Id
	private String id;

	private String nseSymbol;
	
	private Instant bhavDate = Instant.now();
	
	private Volume volume;

	private OnBalanceVolume obv;

	private SimpleMovingAverage sma;

	private ExponentialMovingAverage ema;

	private AverageDirectionalIndex adx;

	private RelativeStrengthIndex rsi;

	private MovingAverageConvergenceDivergence macd;

	private Double yearLow;

	private Double yearHigh;
	
	public StockTechnicals() {
		super();
		
	}

	public StockTechnicals(String nseSymbol, Instant bhavDate, Volume volume, SimpleMovingAverage sma, ExponentialMovingAverage ema) {
		this.nseSymbol = nseSymbol;
		this.bhavDate = bhavDate;
		this.volume = volume;
		this.sma = sma;
		this.ema = ema;
	}

	public StockTechnicals(String nseSymbol, Instant bhavDate, Volume volume, OnBalanceVolume onBalanceVolume,  SimpleMovingAverage sma, ExponentialMovingAverage ema, AverageDirectionalIndex adx, RelativeStrengthIndex rsi, MovingAverageConvergenceDivergence macd) {
		this.nseSymbol = nseSymbol;
		this.bhavDate = bhavDate;
		this.volume = volume;
		this.obv = onBalanceVolume;
		this.sma = sma;
		this.ema = ema;
		this.adx = adx;
		this.rsi = rsi;
		this.macd = macd;
	}

	@Deprecated
	public StockTechnicals(String nseSymbol, Instant bhavDate, Volume volume, Trend trend, Momentum momentum) {
		super();
		this.nseSymbol = nseSymbol;
		this.bhavDate = bhavDate;
		this.volume = volume;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public Instant getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
	}

	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	public OnBalanceVolume getObv() {
		return obv;
	}

	public void setObv(OnBalanceVolume obv) {
		this.obv = obv;
	}

	public SimpleMovingAverage getSma() {
		return sma;
	}

	public void setSma(SimpleMovingAverage sma) {
		this.sma = sma;
	}

	public ExponentialMovingAverage getEma() {
		return ema;
	}

	public void setEma(ExponentialMovingAverage ema) {
		this.ema = ema;
	}

	public AverageDirectionalIndex getAdx() {
		return adx;
	}

	public void setAdx(AverageDirectionalIndex adx) {
		this.adx = adx;
	}

	public RelativeStrengthIndex getRsi() {
		return rsi;
	}

	public void setRsi(RelativeStrengthIndex rsi) {
		this.rsi = rsi;
	}

	public MovingAverageConvergenceDivergence getMacd() {
		return macd;
	}

	public void setMacd(MovingAverageConvergenceDivergence macd) {
		this.macd = macd;
	}

	public Double getYearLow() {
		return yearLow;
	}

	public void setYearLow(Double yearLow) {
		this.yearLow = yearLow;
	}

	public Double getYearHigh() {
		return yearHigh;
	}

	public void setYearHigh(Double yearHigh) {
		this.yearHigh = yearHigh;
	}

	@Override
	public String toString() {
		return "StockTechnicals{" +
				"nseSymbol='" + nseSymbol + '\'' +
				", bhavDate=" + bhavDate +
				", volume=" + volume +
				", sma=" + sma +
				", ema=" + ema +
				", adx=" + adx +
				", rsi=" + rsi +
				", macd=" + macd +
				'}';
	}
}
