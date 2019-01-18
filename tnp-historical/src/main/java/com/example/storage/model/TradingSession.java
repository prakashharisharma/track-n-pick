package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "trading_sessions")
public class TradingSession {

	@Id
	private String id;
	
	
	private Instant tradingDate = Instant.now();

	public TradingSession() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TradingSession(Instant tradingDate) {
		super();
		this.tradingDate = tradingDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Instant getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(Instant tradingDate) {
		this.tradingDate = tradingDate;
	}

	@Override
	public String toString() {
		return "TadingSession [id=" + id + ", tradingDate=" + tradingDate + "]";
	}
	
	
	
}
