package com.example.integration.model;

import java.io.Serializable;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import com.fasterxml.jackson.annotation.JsonProperty;

@CsvRecord(separator = ",", skipFirstLine = true)
public class StockPriceIN implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7916644484544527519L;

	@JsonProperty("TradDt")
	@DataField(pos = 1)
	private String timestamp;

	@JsonProperty("BizDt")
	@DataField(pos = 2)
	private String BizDt;

	@JsonProperty("Sgmt")
	@DataField(pos = 3)
	private String segment;

	@JsonProperty("Src")
	@DataField(pos = 4)
	private String source;

	@JsonProperty("FinInstrmTp")
	@DataField(pos = 5)
	private String finInstrmTp;

	@JsonProperty("FinInstrmId")
	@DataField(pos = 6)
	private String exchangeCode;

	@JsonProperty("ISIN")
	@DataField(pos = 7)
	private String isin;

	@JsonProperty("TckrSymb")
	@DataField(pos = 8)
	private String nseSymbol;

	@JsonProperty("SctySrs")
	@DataField(pos = 9)
	private String series;

	@JsonProperty("XpryDt")
	@DataField(pos = 10)
	private String expiryDate;

	@JsonProperty("FininstrmActlXpryDt")
	@DataField(pos = 11)
	private String fininstrmActlXpryDt;

	@JsonProperty("StrkPric")
	@DataField(pos = 12)
	private String strkPric;

	@JsonProperty("OptnTp")
	@DataField(pos = 13)
	private String optnTp;

	@JsonProperty("FinInstrmNm")
	@DataField(pos = 14)
	private String companyName;

	@JsonProperty("OpnPric")
	@DataField(pos = 15)
	private double open;

	@JsonProperty("HghPric")
	@DataField(pos = 16)
	private double high;

	@JsonProperty("LwPric")
	@DataField(pos = 17)
	private double low;

	@JsonProperty("ClsPric")
	@DataField(pos = 18)
	private double close;

	@JsonProperty("LastPric")
	@DataField(pos = 19)
	private double last;

	@JsonProperty("PrvsClsgPric")
	@DataField(pos = 20)
	private double prevClose;

	@JsonProperty("UndrlygPric")
	@DataField(pos = 21)
	private double undrlygPric;

	@JsonProperty("SttlmPric")
	@DataField(pos = 22)
	private double sttlmPric;

	@JsonProperty("OpnIntrst")
	@DataField(pos = 23)
	private String opnIntrst;

	@JsonProperty("ChngInOpnIntrst")
	@DataField(pos = 24)
	private String chngInOpnIntrst;

	@JsonProperty("TtlTradgVol")
	@DataField(pos = 25)
	private long tottrdqty;

	@JsonProperty("TtlTrfVal")
	@DataField(pos = 26)
	private double tottrdval;

	@JsonProperty("TtlNbOfTxsExctd")
	@DataField(pos = 27)
	private long totaltrades;

	@JsonProperty("SsnId")
	@DataField(pos = 28)
	private String ssnId;

	@JsonProperty("NewBrdLotQty")
	@DataField(pos = 29)
	private long newBrdLotQty;

	@JsonProperty("Rmks")
	@DataField(pos = 30)
	private String remarks;

	@JsonProperty("Rsvd1")
	@DataField(pos = 31)
	private String rsvd1;

	@JsonProperty("Rsvd2")
	@DataField(pos = 32)
	private String rsvd2;

	@JsonProperty("Rsvd3")
	@DataField(pos = 33)
	private String rsvd3;

	@JsonProperty("Rsvd4")
	@DataField(pos = 34)
	private String rsvd4;

	/*
	@JsonProperty("symbol")
	@DataField(pos = 1)
    private String nseSymbol;
	
	@DataField(pos = 2)
    private String series;
	
	@DataField(pos = 3)
    private double open;
	
	@DataField(pos = 4)
    private double high;
	
	
	@DataField(pos = 5)
    private double low;
	
    @DataField(pos = 6)
    private double close;
    
    @DataField(pos = 7)
    private double last;
    
    @DataField(pos = 8)
    private double prevClose;
    
    @DataField(pos = 9)
    private long tottrdqty;
    
    @DataField(pos = 10)
    private double tottrdval;
    
    @DataField(pos = 11)
    private String timestamp;
    
    @DataField(pos = 12)
    private long totaltrades;

    @DataField(pos = 13)
    private String isin;

    @DataField(pos = 14)
    private String dummy;
*/

	public String getBizDt() {
		return BizDt;
	}

	public void setBizDt(String bizDt) {
		BizDt = bizDt;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getFinInstrmTp() {
		return finInstrmTp;
	}

	public void setFinInstrmTp(String finInstrmTp) {
		this.finInstrmTp = finInstrmTp;
	}

	public String getExchangeCode() {
		return exchangeCode;
	}

	public void setExchangeCode(String exchangeCode) {
		this.exchangeCode = exchangeCode;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getFininstrmActlXpryDt() {
		return fininstrmActlXpryDt;
	}

	public void setFininstrmActlXpryDt(String fininstrmActlXpryDt) {
		this.fininstrmActlXpryDt = fininstrmActlXpryDt;
	}

	public String getStrkPric() {
		return strkPric;
	}

	public void setStrkPric(String strkPric) {
		this.strkPric = strkPric;
	}

	public String getOptnTp() {
		return optnTp;
	}

	public void setOptnTp(String optnTp) {
		this.optnTp = optnTp;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public double getUndrlygPric() {
		return undrlygPric;
	}

	public void setUndrlygPric(double undrlygPric) {
		this.undrlygPric = undrlygPric;
	}

	public double getSttlmPric() {
		return sttlmPric;
	}

	public void setSttlmPric(double sttlmPric) {
		this.sttlmPric = sttlmPric;
	}

	public String getOpnIntrst() {
		return opnIntrst;
	}

	public void setOpnIntrst(String opnIntrst) {
		this.opnIntrst = opnIntrst;
	}

	public String getChngInOpnIntrst() {
		return chngInOpnIntrst;
	}

	public void setChngInOpnIntrst(String chngInOpnIntrst) {
		this.chngInOpnIntrst = chngInOpnIntrst;
	}

	public String getSsnId() {
		return ssnId;
	}

	public void setSsnId(String ssnId) {
		this.ssnId = ssnId;
	}

	public long getNewBrdLotQty() {
		return newBrdLotQty;
	}

	public void setNewBrdLotQty(long newBrdLotQty) {
		this.newBrdLotQty = newBrdLotQty;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getRsvd1() {
		return rsvd1;
	}

	public void setRsvd1(String rsvd1) {
		this.rsvd1 = rsvd1;
	}

	public String getRsvd2() {
		return rsvd2;
	}

	public void setRsvd2(String rsvd2) {
		this.rsvd2 = rsvd2;
	}

	public String getRsvd3() {
		return rsvd3;
	}

	public void setRsvd3(String rsvd3) {
		this.rsvd3 = rsvd3;
	}

	public String getRsvd4() {
		return rsvd4;
	}

	public void setRsvd4(String rsvd4) {
		this.rsvd4 = rsvd4;
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

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

	public long getTottrdqty() {
		return tottrdqty;
	}

	public void setTottrdqty(long tottrdqty) {
		this.tottrdqty = tottrdqty;
	}

	public double getTottrdval() {
		return tottrdval;
	}

	public void setTottrdval(double tottrdval) {
		this.tottrdval = tottrdval;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public long getTotaltrades() {
		return totaltrades;
	}

	public void setTotaltrades(long totaltrades) {
		this.totaltrades = totaltrades;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	@Override
	public String toString() {
		return "StockPrice [symbol=" + nseSymbol + ", close=" + close + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nseSymbol == null) ? 0 : nseSymbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockPriceIN other = (StockPriceIN) obj;
		if (nseSymbol == null) {
			if (other.nseSymbol != null)
				return false;
		} else if (!nseSymbol.equals(other.nseSymbol))
			return false;
		return true;
	}
 
}
