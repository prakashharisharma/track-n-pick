package com.example.storage.model;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Document(collection = "technicals_history")
public class StockTechnicals {

	@Id
	protected String id;

	protected String nseSymbol;

	protected Instant bhavDate = Instant.now();

	protected Volume volume;

	protected OnBalanceVolume obv;
	protected SimpleMovingAverage sma;

	protected ExponentialMovingAverage ema;

	protected AverageDirectionalIndex adx;

	protected RelativeStrengthIndex rsi;

	protected MovingAverageConvergenceDivergence macd;

	
	public StockTechnicals() {
		super();
		
	}

	public StockTechnicals(String nseSymbol, Instant bhavDate, Volume volume, OnBalanceVolume obv, SimpleMovingAverage sma, ExponentialMovingAverage ema, AverageDirectionalIndex adx, RelativeStrengthIndex rsi, MovingAverageConvergenceDivergence macd) {
		this.nseSymbol = nseSymbol;
		this.bhavDate = bhavDate;
		this.volume = volume;
		this.obv = obv;
		this.sma = sma;
		this.ema = ema;
		this.adx = adx;
		this.rsi = rsi;
		this.macd = macd;
	}
}
