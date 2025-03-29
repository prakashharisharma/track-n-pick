package com.example.dto.io;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class TradingSessionIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8553942293479212274L;
	private Instant tradingDate = Instant.now();

	public TradingSessionIO(Instant tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public TradingSessionIO(LocalDate tradingDate) {
		super();
		this.tradingDate = tradingDate.atStartOfDay().toInstant(ZoneOffset.UTC);
	}
	
	public Instant getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Instant tradingDate) {
		this.tradingDate = tradingDate;
	}

	@Override
	public String toString() {
		return "TradingSessionIO [tradingDate=" + tradingDate + "]";
	}
	
	
	
}
