package com.example.external.chyl.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NSEResultHelper {

@JsonProperty("pricebandupper")
private String pricebandupper;
@JsonProperty("symbol")
private String symbol;
@JsonProperty("applicableMargin")
private String applicableMargin;
@JsonProperty("bcEndDate")
private String bcEndDate;
@JsonProperty("totalSellQuantity")
private String totalSellQuantity;
@JsonProperty("adhocMargin")
private String adhocMargin;
@JsonProperty("companyName")
private String companyName;
@JsonProperty("marketType")
private String marketType;
@JsonProperty("exDate")
private String exDate;
@JsonProperty("bcStartDate")
private String bcStartDate;
@JsonProperty("css_status_desc")
private String cssStatusDesc;
@JsonProperty("dayHigh")
private String dayHigh;
@JsonProperty("basePrice")
private String basePrice;
@JsonProperty("securityVar")
private String securityVar;
@JsonProperty("pricebandlower")
private String pricebandlower;
@JsonProperty("sellQuantity5")
private String sellQuantity5;
@JsonProperty("sellQuantity4")
private String sellQuantity4;
@JsonProperty("sellQuantity3")
private String sellQuantity3;
@JsonProperty("cm_adj_high_dt")
private String cmAdjHighDt;
@JsonProperty("sellQuantity2")
private String sellQuantity2;
@JsonProperty("dayLow")
private String dayLow;
@JsonProperty("sellQuantity1")
private String sellQuantity1;
@JsonProperty("quantityTraded")
private String quantityTraded;
@JsonProperty("pChange")
private String pChange;
@JsonProperty("totalTradedValue")
private String totalTradedValue;
@JsonProperty("deliveryToTradedQuantity")
private String deliveryToTradedQuantity;
@JsonProperty("totalBuyQuantity")
private String totalBuyQuantity;
@JsonProperty("averagePrice")
private String averagePrice;
@JsonProperty("indexVar")
private String indexVar;
@JsonProperty("cm_ffm")
private String cmFfm;
@JsonProperty("purpose")
private String purpose;
@JsonProperty("buyPrice2")
private String buyPrice2;
@JsonProperty("secDate")
private String secDate;
@JsonProperty("buyPrice1")
private String buyPrice1;
@JsonProperty("high52")
private String high52;
@JsonProperty("previousClose")
private String previousClose;
@JsonProperty("ndEndDate")
private String ndEndDate;
@JsonProperty("low52")
private String low52;
@JsonProperty("buyPrice4")
private String buyPrice4;
@JsonProperty("buyPrice3")
private String buyPrice3;
@JsonProperty("recordDate")
private String recordDate;
@JsonProperty("deliveryQuantity")
private String deliveryQuantity;
@JsonProperty("buyPrice5")
private String buyPrice5;
@JsonProperty("priceBand")
private String priceBand;
@JsonProperty("extremeLossMargin")
private String extremeLossMargin;
@JsonProperty("cm_adj_low_dt")
private String cmAdjLowDt;
@JsonProperty("varMargin")
private String varMargin;
@JsonProperty("sellPrice1")
private String sellPrice1;
@JsonProperty("sellPrice2")
private String sellPrice2;
@JsonProperty("totalTradedVolume")
private String totalTradedVolume;
@JsonProperty("sellPrice3")
private String sellPrice3;
@JsonProperty("sellPrice4")
private String sellPrice4;
@JsonProperty("sellPrice5")
private String sellPrice5;
@JsonProperty("change")
private String change;
@JsonProperty("surv_indicator")
private String survIndicator;
@JsonProperty("ndStartDate")
private String ndStartDate;
@JsonProperty("buyQuantity4")
private String buyQuantity4;
@JsonProperty("isExDateFlag")
private Boolean isExDateFlag;
@JsonProperty("buyQuantity3")
private String buyQuantity3;
@JsonProperty("buyQuantity2")
private String buyQuantity2;
@JsonProperty("buyQuantity1")
private String buyQuantity1;
@JsonProperty("series")
private String series;
@JsonProperty("faceValue")
private String faceValue;
@JsonProperty("buyQuantity5")
private String buyQuantity5;
@JsonProperty("closePrice")
private String closePrice;
@JsonProperty("open")
private String open;
@JsonProperty("isinCode")
private String isinCode;
@JsonProperty("lastPrice")
private String lastPrice;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("pricebandupper")
public String getPricebandupper() {
return pricebandupper;
}

@JsonProperty("pricebandupper")
public void setPricebandupper(String pricebandupper) {
this.pricebandupper = pricebandupper;
}

@JsonProperty("symbol")
public String getSymbol() {
return symbol;
}

@JsonProperty("symbol")
public void setSymbol(String symbol) {
this.symbol = symbol;
}

@JsonProperty("applicableMargin")
public String getApplicableMargin() {
return applicableMargin;
}

@JsonProperty("applicableMargin")
public void setApplicableMargin(String applicableMargin) {
this.applicableMargin = applicableMargin;
}

@JsonProperty("bcEndDate")
public String getBcEndDate() {
return bcEndDate;
}

@JsonProperty("bcEndDate")
public void setBcEndDate(String bcEndDate) {
this.bcEndDate = bcEndDate;
}

@JsonProperty("totalSellQuantity")
public String getTotalSellQuantity() {
return totalSellQuantity;
}

@JsonProperty("totalSellQuantity")
public void setTotalSellQuantity(String totalSellQuantity) {
this.totalSellQuantity = totalSellQuantity;
}

@JsonProperty("adhocMargin")
public String getAdhocMargin() {
return adhocMargin;
}

@JsonProperty("adhocMargin")
public void setAdhocMargin(String adhocMargin) {
this.adhocMargin = adhocMargin;
}

@JsonProperty("companyName")
public String getCompanyName() {
return companyName;
}

@JsonProperty("companyName")
public void setCompanyName(String companyName) {
this.companyName = companyName;
}

@JsonProperty("marketType")
public String getMarketType() {
return marketType;
}

@JsonProperty("marketType")
public void setMarketType(String marketType) {
this.marketType = marketType;
}

@JsonProperty("exDate")
public String getExDate() {
return exDate;
}

@JsonProperty("exDate")
public void setExDate(String exDate) {
this.exDate = exDate;
}

@JsonProperty("bcStartDate")
public String getBcStartDate() {
return bcStartDate;
}

@JsonProperty("bcStartDate")
public void setBcStartDate(String bcStartDate) {
this.bcStartDate = bcStartDate;
}

@JsonProperty("css_status_desc")
public String getCssStatusDesc() {
return cssStatusDesc;
}

@JsonProperty("css_status_desc")
public void setCssStatusDesc(String cssStatusDesc) {
this.cssStatusDesc = cssStatusDesc;
}

@JsonProperty("dayHigh")
public String getDayHigh() {
return dayHigh;
}

@JsonProperty("dayHigh")
public void setDayHigh(String dayHigh) {
this.dayHigh = dayHigh;
}

@JsonProperty("basePrice")
public String getBasePrice() {
return basePrice;
}

@JsonProperty("basePrice")
public void setBasePrice(String basePrice) {
this.basePrice = basePrice;
}

@JsonProperty("securityVar")
public String getSecurityVar() {
return securityVar;
}

@JsonProperty("securityVar")
public void setSecurityVar(String securityVar) {
this.securityVar = securityVar;
}

@JsonProperty("pricebandlower")
public String getPricebandlower() {
return pricebandlower;
}

@JsonProperty("pricebandlower")
public void setPricebandlower(String pricebandlower) {
this.pricebandlower = pricebandlower;
}

@JsonProperty("sellQuantity5")
public String getSellQuantity5() {
return sellQuantity5;
}

@JsonProperty("sellQuantity5")
public void setSellQuantity5(String sellQuantity5) {
this.sellQuantity5 = sellQuantity5;
}

@JsonProperty("sellQuantity4")
public String getSellQuantity4() {
return sellQuantity4;
}

@JsonProperty("sellQuantity4")
public void setSellQuantity4(String sellQuantity4) {
this.sellQuantity4 = sellQuantity4;
}

@JsonProperty("sellQuantity3")
public String getSellQuantity3() {
return sellQuantity3;
}

@JsonProperty("sellQuantity3")
public void setSellQuantity3(String sellQuantity3) {
this.sellQuantity3 = sellQuantity3;
}

@JsonProperty("cm_adj_high_dt")
public String getCmAdjHighDt() {
return cmAdjHighDt;
}

@JsonProperty("cm_adj_high_dt")
public void setCmAdjHighDt(String cmAdjHighDt) {
this.cmAdjHighDt = cmAdjHighDt;
}

@JsonProperty("sellQuantity2")
public String getSellQuantity2() {
return sellQuantity2;
}

@JsonProperty("sellQuantity2")
public void setSellQuantity2(String sellQuantity2) {
this.sellQuantity2 = sellQuantity2;
}

@JsonProperty("dayLow")
public String getDayLow() {
return dayLow;
}

@JsonProperty("dayLow")
public void setDayLow(String dayLow) {
this.dayLow = dayLow;
}

@JsonProperty("sellQuantity1")
public String getSellQuantity1() {
return sellQuantity1;
}

@JsonProperty("sellQuantity1")
public void setSellQuantity1(String sellQuantity1) {
this.sellQuantity1 = sellQuantity1;
}

@JsonProperty("quantityTraded")
public String getQuantityTraded() {
return quantityTraded;
}

@JsonProperty("quantityTraded")
public void setQuantityTraded(String quantityTraded) {
this.quantityTraded = quantityTraded;
}

@JsonProperty("pChange")
public String getPChange() {
return pChange;
}

@JsonProperty("pChange")
public void setPChange(String pChange) {
this.pChange = pChange;
}

@JsonProperty("totalTradedValue")
public String getTotalTradedValue() {
return totalTradedValue;
}

@JsonProperty("totalTradedValue")
public void setTotalTradedValue(String totalTradedValue) {
this.totalTradedValue = totalTradedValue;
}

@JsonProperty("deliveryToTradedQuantity")
public String getDeliveryToTradedQuantity() {
return deliveryToTradedQuantity;
}

@JsonProperty("deliveryToTradedQuantity")
public void setDeliveryToTradedQuantity(String deliveryToTradedQuantity) {
this.deliveryToTradedQuantity = deliveryToTradedQuantity;
}

@JsonProperty("totalBuyQuantity")
public String getTotalBuyQuantity() {
return totalBuyQuantity;
}

@JsonProperty("totalBuyQuantity")
public void setTotalBuyQuantity(String totalBuyQuantity) {
this.totalBuyQuantity = totalBuyQuantity;
}

@JsonProperty("averagePrice")
public String getAveragePrice() {
return averagePrice;
}

@JsonProperty("averagePrice")
public void setAveragePrice(String averagePrice) {
this.averagePrice = averagePrice;
}

@JsonProperty("indexVar")
public String getIndexVar() {
return indexVar;
}

@JsonProperty("indexVar")
public void setIndexVar(String indexVar) {
this.indexVar = indexVar;
}

@JsonProperty("cm_ffm")
public String getCmFfm() {
return cmFfm;
}

@JsonProperty("cm_ffm")
public void setCmFfm(String cmFfm) {
this.cmFfm = cmFfm;
}

@JsonProperty("purpose")
public String getPurpose() {
return purpose;
}

@JsonProperty("purpose")
public void setPurpose(String purpose) {
this.purpose = purpose;
}

@JsonProperty("buyPrice2")
public String getBuyPrice2() {
return buyPrice2;
}

@JsonProperty("buyPrice2")
public void setBuyPrice2(String buyPrice2) {
this.buyPrice2 = buyPrice2;
}

@JsonProperty("secDate")
public String getSecDate() {
return secDate;
}

@JsonProperty("secDate")
public void setSecDate(String secDate) {
this.secDate = secDate;
}

@JsonProperty("buyPrice1")
public String getBuyPrice1() {
return buyPrice1;
}

@JsonProperty("buyPrice1")
public void setBuyPrice1(String buyPrice1) {
this.buyPrice1 = buyPrice1;
}

@JsonProperty("high52")
public String getHigh52() {
return high52;
}

@JsonProperty("high52")
public void setHigh52(String high52) {
this.high52 = high52;
}

@JsonProperty("previousClose")
public String getPreviousClose() {
return previousClose;
}

@JsonProperty("previousClose")
public void setPreviousClose(String previousClose) {
this.previousClose = previousClose;
}

@JsonProperty("ndEndDate")
public String getNdEndDate() {
return ndEndDate;
}

@JsonProperty("ndEndDate")
public void setNdEndDate(String ndEndDate) {
this.ndEndDate = ndEndDate;
}

@JsonProperty("low52")
public String getLow52() {
return low52;
}

@JsonProperty("low52")
public void setLow52(String low52) {
this.low52 = low52;
}

@JsonProperty("buyPrice4")
public String getBuyPrice4() {
return buyPrice4;
}

@JsonProperty("buyPrice4")
public void setBuyPrice4(String buyPrice4) {
this.buyPrice4 = buyPrice4;
}

@JsonProperty("buyPrice3")
public String getBuyPrice3() {
return buyPrice3;
}

@JsonProperty("buyPrice3")
public void setBuyPrice3(String buyPrice3) {
this.buyPrice3 = buyPrice3;
}

@JsonProperty("recordDate")
public String getRecordDate() {
return recordDate;
}

@JsonProperty("recordDate")
public void setRecordDate(String recordDate) {
this.recordDate = recordDate;
}

@JsonProperty("deliveryQuantity")
public String getDeliveryQuantity() {
return deliveryQuantity;
}

@JsonProperty("deliveryQuantity")
public void setDeliveryQuantity(String deliveryQuantity) {
this.deliveryQuantity = deliveryQuantity;
}

@JsonProperty("buyPrice5")
public String getBuyPrice5() {
return buyPrice5;
}

@JsonProperty("buyPrice5")
public void setBuyPrice5(String buyPrice5) {
this.buyPrice5 = buyPrice5;
}

@JsonProperty("priceBand")
public String getPriceBand() {
return priceBand;
}

@JsonProperty("priceBand")
public void setPriceBand(String priceBand) {
this.priceBand = priceBand;
}

@JsonProperty("extremeLossMargin")
public String getExtremeLossMargin() {
return extremeLossMargin;
}

@JsonProperty("extremeLossMargin")
public void setExtremeLossMargin(String extremeLossMargin) {
this.extremeLossMargin = extremeLossMargin;
}

@JsonProperty("cm_adj_low_dt")
public String getCmAdjLowDt() {
return cmAdjLowDt;
}

@JsonProperty("cm_adj_low_dt")
public void setCmAdjLowDt(String cmAdjLowDt) {
this.cmAdjLowDt = cmAdjLowDt;
}

@JsonProperty("varMargin")
public String getVarMargin() {
return varMargin;
}

@JsonProperty("varMargin")
public void setVarMargin(String varMargin) {
this.varMargin = varMargin;
}

@JsonProperty("sellPrice1")
public String getSellPrice1() {
return sellPrice1;
}

@JsonProperty("sellPrice1")
public void setSellPrice1(String sellPrice1) {
this.sellPrice1 = sellPrice1;
}

@JsonProperty("sellPrice2")
public String getSellPrice2() {
return sellPrice2;
}

@JsonProperty("sellPrice2")
public void setSellPrice2(String sellPrice2) {
this.sellPrice2 = sellPrice2;
}

@JsonProperty("totalTradedVolume")
public String getTotalTradedVolume() {
return totalTradedVolume;
}

@JsonProperty("totalTradedVolume")
public void setTotalTradedVolume(String totalTradedVolume) {
this.totalTradedVolume = totalTradedVolume;
}

@JsonProperty("sellPrice3")
public String getSellPrice3() {
return sellPrice3;
}

@JsonProperty("sellPrice3")
public void setSellPrice3(String sellPrice3) {
this.sellPrice3 = sellPrice3;
}

@JsonProperty("sellPrice4")
public String getSellPrice4() {
return sellPrice4;
}

@JsonProperty("sellPrice4")
public void setSellPrice4(String sellPrice4) {
this.sellPrice4 = sellPrice4;
}

@JsonProperty("sellPrice5")
public String getSellPrice5() {
return sellPrice5;
}

@JsonProperty("sellPrice5")
public void setSellPrice5(String sellPrice5) {
this.sellPrice5 = sellPrice5;
}

@JsonProperty("change")
public String getChange() {
return change;
}

@JsonProperty("change")
public void setChange(String change) {
this.change = change;
}

@JsonProperty("surv_indicator")
public String getSurvIndicator() {
return survIndicator;
}

@JsonProperty("surv_indicator")
public void setSurvIndicator(String survIndicator) {
this.survIndicator = survIndicator;
}

@JsonProperty("ndStartDate")
public String getNdStartDate() {
return ndStartDate;
}

@JsonProperty("ndStartDate")
public void setNdStartDate(String ndStartDate) {
this.ndStartDate = ndStartDate;
}

@JsonProperty("buyQuantity4")
public String getBuyQuantity4() {
return buyQuantity4;
}

@JsonProperty("buyQuantity4")
public void setBuyQuantity4(String buyQuantity4) {
this.buyQuantity4 = buyQuantity4;
}

@JsonProperty("isExDateFlag")
public Boolean getIsExDateFlag() {
return isExDateFlag;
}

@JsonProperty("isExDateFlag")
public void setIsExDateFlag(Boolean isExDateFlag) {
this.isExDateFlag = isExDateFlag;
}

@JsonProperty("buyQuantity3")
public String getBuyQuantity3() {
return buyQuantity3;
}

@JsonProperty("buyQuantity3")
public void setBuyQuantity3(String buyQuantity3) {
this.buyQuantity3 = buyQuantity3;
}

@JsonProperty("buyQuantity2")
public String getBuyQuantity2() {
return buyQuantity2;
}

@JsonProperty("buyQuantity2")
public void setBuyQuantity2(String buyQuantity2) {
this.buyQuantity2 = buyQuantity2;
}

@JsonProperty("buyQuantity1")
public String getBuyQuantity1() {
return buyQuantity1;
}

@JsonProperty("buyQuantity1")
public void setBuyQuantity1(String buyQuantity1) {
this.buyQuantity1 = buyQuantity1;
}

@JsonProperty("series")
public String getSeries() {
return series;
}

@JsonProperty("series")
public void setSeries(String series) {
this.series = series;
}

@JsonProperty("faceValue")
public String getFaceValue() {
return faceValue;
}

@JsonProperty("faceValue")
public void setFaceValue(String faceValue) {
this.faceValue = faceValue;
}

@JsonProperty("buyQuantity5")
public String getBuyQuantity5() {
return buyQuantity5;
}

@JsonProperty("buyQuantity5")
public void setBuyQuantity5(String buyQuantity5) {
this.buyQuantity5 = buyQuantity5;
}

@JsonProperty("closePrice")
public String getClosePrice() {
return closePrice;
}

@JsonProperty("closePrice")
public void setClosePrice(String closePrice) {
this.closePrice = closePrice;
}

@JsonProperty("open")
public String getOpen() {
return open;
}

@JsonProperty("open")
public void setOpen(String open) {
this.open = open;
}

@JsonProperty("isinCode")
public String getIsinCode() {
return isinCode;
}

@JsonProperty("isinCode")
public void setIsinCode(String isinCode) {
this.isinCode = isinCode;
}

@JsonProperty("lastPrice")
public String getLastPrice() {
return lastPrice;
}

@JsonProperty("lastPrice")
public void setLastPrice(String lastPrice) {
this.lastPrice = lastPrice;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

@Override
public String toString() {
	return "Datum [pricebandupper=" + pricebandupper + ", symbol=" + symbol + ", applicableMargin=" + applicableMargin
			+ ", bcEndDate=" + bcEndDate + ", totalSellQuantity=" + totalSellQuantity + ", adhocMargin=" + adhocMargin
			+ ", companyName=" + companyName + ", marketType=" + marketType + ", exDate=" + exDate + ", bcStartDate="
			+ bcStartDate + ", cssStatusDesc=" + cssStatusDesc + ", dayHigh=" + dayHigh + ", basePrice=" + basePrice
			+ ", securityVar=" + securityVar + ", pricebandlower=" + pricebandlower + ", sellQuantity5=" + sellQuantity5
			+ ", sellQuantity4=" + sellQuantity4 + ", sellQuantity3=" + sellQuantity3 + ", cmAdjHighDt=" + cmAdjHighDt
			+ ", sellQuantity2=" + sellQuantity2 + ", dayLow=" + dayLow + ", sellQuantity1=" + sellQuantity1
			+ ", quantityTraded=" + quantityTraded + ", pChange=" + pChange + ", totalTradedValue=" + totalTradedValue
			+ ", deliveryToTradedQuantity=" + deliveryToTradedQuantity + ", totalBuyQuantity=" + totalBuyQuantity
			+ ", averagePrice=" + averagePrice + ", indexVar=" + indexVar + ", cmFfm=" + cmFfm + ", purpose=" + purpose
			+ ", buyPrice2=" + buyPrice2 + ", secDate=" + secDate + ", buyPrice1=" + buyPrice1 + ", high52=" + high52
			+ ", previousClose=" + previousClose + ", ndEndDate=" + ndEndDate + ", low52=" + low52 + ", buyPrice4="
			+ buyPrice4 + ", buyPrice3=" + buyPrice3 + ", recordDate=" + recordDate + ", deliveryQuantity="
			+ deliveryQuantity + ", buyPrice5=" + buyPrice5 + ", priceBand=" + priceBand + ", extremeLossMargin="
			+ extremeLossMargin + ", cmAdjLowDt=" + cmAdjLowDt + ", varMargin=" + varMargin + ", sellPrice1="
			+ sellPrice1 + ", sellPrice2=" + sellPrice2 + ", totalTradedVolume=" + totalTradedVolume + ", sellPrice3="
			+ sellPrice3 + ", sellPrice4=" + sellPrice4 + ", sellPrice5=" + sellPrice5 + ", change=" + change
			+ ", survIndicator=" + survIndicator + ", ndStartDate=" + ndStartDate + ", buyQuantity4=" + buyQuantity4
			+ ", isExDateFlag=" + isExDateFlag + ", buyQuantity3=" + buyQuantity3 + ", buyQuantity2=" + buyQuantity2
			+ ", buyQuantity1=" + buyQuantity1 + ", series=" + series + ", faceValue=" + faceValue + ", buyQuantity5="
			+ buyQuantity5 + ", closePrice=" + closePrice + ", open=" + open + ", isinCode=" + isinCode + ", lastPrice="
			+ lastPrice + ", additionalProperties=" + additionalProperties + "]";
}


}
