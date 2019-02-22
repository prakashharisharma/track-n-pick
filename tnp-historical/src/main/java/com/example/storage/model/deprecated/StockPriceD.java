package com.example.storage.model.deprecated;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Deprecated
@Document
public class StockPriceD {

/*	@Id
    private ObjectId _id;
*/
	
	double open;
	double high;
	double low;
	double close;
	double last;
	double prevClose;
	long totalTradedQuantity;
	double totalTradedValue;
	long totalTrades;
	
	Instant bhavDate = Instant.now();
	
	double change;
	
	public StockPriceD() {
		super();
		// this._id = ObjectId.get();
	}

	public StockPriceD(double close) {
		super();
		this.close = close;
		// this._id = ObjectId.get();
	}



	public StockPriceD(double open, double high, double low, double close, double last, double prevClose,
			long totalTradedQuantity, double totalTradedValue, long totalTrades, Instant bhavDate) {
		super();
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.last = last;
		this.prevClose = prevClose;
		this.totalTradedQuantity = totalTradedQuantity;
		this.totalTradedValue = totalTradedValue;
		this.totalTrades = totalTrades;
		this.bhavDate = bhavDate;
		this.change = (close - prevClose );
		// this._id = ObjectId.get();
	}


	/*public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}*/

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}



	public double getHigh() {
		return high;
	}



	public void setHigh(double high) {
		this.high = high;
	}



	public double getLow() {
		return low;
	}



	public void setLow(double low) {
		this.low = low;
	}



	public double getClose() {
		return close;
	}



	public void setClose(double close) {
		this.close = close;
	}



	public double getLast() {
		return last;
	}



	public void setLast(double last) {
		this.last = last;
	}



	public double getPrevClose() {
		return prevClose;
	}



	public void setPrevClose(double prevClose) {
		this.prevClose = prevClose;
	}



	public long getTotalTradedQuantity() {
		return totalTradedQuantity;
	}



	public void setTotalTradedQuantity(long totalTradedQuantity) {
		this.totalTradedQuantity = totalTradedQuantity;
	}



	public double getTotalTradedValue() {
		return totalTradedValue;
	}



	public void setTotalTradedValue(double totalTradedValue) {
		this.totalTradedValue = totalTradedValue;
	}


	public long getTotalTrades() {
		return totalTrades;
	}

	public void setTotalTrades(long totalTrades) {
		this.totalTrades = totalTrades;
	}

	public Instant getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	@Override
	public String toString() {
		return "StockPrice [open=" + open + ", high=" + high + ", low=" + low + ", close=" + close + ", last=" + last
				+ ", prevClose=" + prevClose + ", totalTradedQuantity=" + totalTradedQuantity + ", totalTradedValue="
				+ totalTradedValue + ", totalTrades=" + totalTrades + ", bhavDate=" + bhavDate + ", change=" + change
				+ "]";
	}


	
}
