package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "candlesticks_history")
public class CandleStick {
	
	public enum Color {RED, GREEN};
	
	public enum Gap {UP, DOWN, NONE};
	
	public enum Pattern {HAMMER, DOJI,BULLISH_MARUBOZU,BEARISH_MARUBOZU, SHOOTING_STAR, NONE};
	
	@Id
	private String id;

	private String nseSymbol;
	
	private Double open;
	private Double high;
	private Double low;
	private Double close;
	private Double prevClose;
	
	private Double head;
	
	private Double body;
	
	private Double tail;
	
	private Color color;
	
	private Gap gap;
	
	private Pattern pattern;
	
	private Instant bhavDate = Instant.now();

	public CandleStick(String nseSymbol, Double open, Double high, Double low, Double close, Double prevClose,
			Instant bhavDate) {
		super();
		this.nseSymbol = nseSymbol;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.prevClose = prevClose;
		this.bhavDate = bhavDate;
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

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getPrevClose() {
		return prevClose;
	}

	public void setPrevClose(Double prevClose) {
		this.prevClose = prevClose;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Gap getGap() {
		return gap;
	}

	public void setGap(Gap gap) {
		this.gap = gap;
	}

	public Instant getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
	}

	public Double getHead() {
		return head;
	}

	public void setHead(Double head) {
		this.head = head;
	}

	public Double getBody() {
		return body;
	}

	public void setBody(Double body) {
		this.body = body;
	}

	public Double getTail() {
		return tail;
	}

	public void setTail(Double tail) {
		this.tail = tail;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public String toString() {
		return "CandleStick [id=" + id + ", nseSymbol=" + nseSymbol + ", open=" + open + ", high=" + high + ", low="
				+ low + ", close=" + close + ", prevClose=" + prevClose + ", head=" + head + ", body=" + body
				+ ", tail=" + tail + ", color=" + color + ", gap=" + gap + ", pattern=" + pattern + ", bhavDate="
				+ bhavDate + "]";
	}
	
}
