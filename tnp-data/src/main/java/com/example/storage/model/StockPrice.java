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
@Document(collection = "price_history")
public class StockPrice {

	@Id
	protected String id;
	protected String nseSymbol;
	protected Instant bhavDate = Instant.now();
	protected Double open;
	protected Double high;
	protected Double low;
	protected Double close;
	protected Long volume = 0l;


	public StockPrice() {
		super();
	}

	public StockPrice(String nseSymbol, Instant bhavDate, Double open, Double high, Double low, Double close, Long volume) {
		this.nseSymbol = nseSymbol;
		this.bhavDate = bhavDate;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}

}
