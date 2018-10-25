package com.example.chyl.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NSEResult {

@JsonProperty("tradedDate")
private String tradedDate;
@JsonProperty("data")
private List<NSEResultHelper> data = null;
@JsonProperty("optLink")
private String optLink;
@JsonProperty("otherSeries")
private List<String> otherSeries = null;
@JsonProperty("futLink")
private String futLink;
@JsonProperty("lastUpdateTime")
private String lastUpdateTime;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("tradedDate")
public String getTradedDate() {
return tradedDate;
}

@JsonProperty("tradedDate")
public void setTradedDate(String tradedDate) {
this.tradedDate = tradedDate;
}

@JsonProperty("data")
public List<NSEResultHelper> getData() {
return data;
}

@JsonProperty("data")
public void setData(List<NSEResultHelper> data) {
this.data = data;
}

@JsonProperty("optLink")
public String getOptLink() {
return optLink;
}

@JsonProperty("optLink")
public void setOptLink(String optLink) {
this.optLink = optLink;
}

@JsonProperty("otherSeries")
public List<String> getOtherSeries() {
return otherSeries;
}

@JsonProperty("otherSeries")
public void setOtherSeries(List<String> otherSeries) {
this.otherSeries = otherSeries;
}

@JsonProperty("futLink")
public String getFutLink() {
return futLink;
}

@JsonProperty("futLink")
public void setFutLink(String futLink) {
this.futLink = futLink;
}

@JsonProperty("lastUpdateTime")
public String getLastUpdateTime() {
return lastUpdateTime;
}

@JsonProperty("lastUpdateTime")
public void setLastUpdateTime(String lastUpdateTime) {
this.lastUpdateTime = lastUpdateTime;
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
	return "NSE [tradedDate=" + tradedDate + ", data=" + data + ", optLink=" + optLink + ", otherSeries=" + otherSeries
			+ ", futLink=" + futLink + ", lastUpdateTime=" + lastUpdateTime + ", additionalProperties="
			+ additionalProperties + "]";
}


}