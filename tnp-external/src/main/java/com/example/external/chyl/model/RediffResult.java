package com.example.external.chyl.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class RediffResult {

    @JsonProperty("LastTradedPrice")
    private String lastTradedPrice;

    @JsonProperty("Volume")
    private String volume;

    @JsonProperty("PercentageDiff")
    private String percentageDiff;

    @JsonProperty("FiftyTwoWeekHigh")
    private String fiftyTwoWeekHigh;

    @JsonProperty("FiftyTwoWeekLow")
    private String fiftyTwoWeekLow;

    @JsonProperty("LastTradedTime")
    private String lastTradedTime;

    @JsonProperty("ChangePercent")
    private String changePercent;

    @JsonProperty("Change")
    private String change;

    @JsonProperty("MarketCap")
    private String marketCap;

    @JsonProperty("High")
    private String high;

    @JsonProperty("Low")
    private String low;

    @JsonProperty("PrevClose")
    private String prevClose;

    @JsonProperty("BonusSplitStatus")
    private String bonusSplitStatus;

    @JsonProperty("BonusSplitRatio")
    private String bonusSplitRatio;

    @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("LastTradedPrice")
    public String getLastTradedPrice() {
        return lastTradedPrice;
    }

    @JsonProperty("LastTradedPrice")
    public void setLastTradedPrice(String lastTradedPrice) {
        this.lastTradedPrice = lastTradedPrice;
    }

    @JsonProperty("Volume")
    public String getVolume() {
        return volume;
    }

    @JsonProperty("Volume")
    public void setVolume(String volume) {
        this.volume = volume;
    }

    @JsonProperty("PercentageDiff")
    public String getPercentageDiff() {
        return percentageDiff;
    }

    @JsonProperty("PercentageDiff")
    public void setPercentageDiff(String percentageDiff) {
        this.percentageDiff = percentageDiff;
    }

    @JsonProperty("FiftyTwoWeekHigh")
    public String getFiftyTwoWeekHigh() {
        return fiftyTwoWeekHigh;
    }

    @JsonProperty("FiftyTwoWeekHigh")
    public void setFiftyTwoWeekHigh(String fiftyTwoWeekHigh) {
        this.fiftyTwoWeekHigh = fiftyTwoWeekHigh;
    }

    @JsonProperty("FiftyTwoWeekLow")
    public String getFiftyTwoWeekLow() {
        return fiftyTwoWeekLow;
    }

    @JsonProperty("FiftyTwoWeekLow")
    public void setFiftyTwoWeekLow(String fiftyTwoWeekLow) {
        this.fiftyTwoWeekLow = fiftyTwoWeekLow;
    }

    @JsonProperty("LastTradedTime")
    public String getLastTradedTime() {
        return lastTradedTime;
    }

    @JsonProperty("LastTradedTime")
    public void setLastTradedTime(String lastTradedTime) {
        this.lastTradedTime = lastTradedTime;
    }

    @JsonProperty("ChangePercent")
    public String getChangePercent() {
        return changePercent;
    }

    @JsonProperty("ChangePercent")
    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    @JsonProperty("Change")
    public String getChange() {
        return change;
    }

    @JsonProperty("Change")
    public void setChange(String change) {
        this.change = change;
    }

    @JsonProperty("MarketCap")
    public String getMarketCap() {
        return marketCap;
    }

    @JsonProperty("MarketCap")
    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    @JsonProperty("High")
    public String getHigh() {
        return high;
    }

    @JsonProperty("High")
    public void setHigh(String high) {
        this.high = high;
    }

    @JsonProperty("Low")
    public String getLow() {
        return low;
    }

    @JsonProperty("Low")
    public void setLow(String low) {
        this.low = low;
    }

    @JsonProperty("PrevClose")
    public String getPrevClose() {
        return prevClose;
    }

    @JsonProperty("PrevClose")
    public void setPrevClose(String prevClose) {
        this.prevClose = prevClose;
    }

    @JsonProperty("BonusSplitStatus")
    public String getBonusSplitStatus() {
        return bonusSplitStatus;
    }

    @JsonProperty("BonusSplitStatus")
    public void setBonusSplitStatus(String bonusSplitStatus) {
        this.bonusSplitStatus = bonusSplitStatus;
    }

    @JsonProperty("BonusSplitRatio")
    public String getBonusSplitRatio() {
        return bonusSplitRatio;
    }

    @JsonProperty("BonusSplitRatio")
    public void setBonusSplitRatio(String bonusSplitRatio) {
        this.bonusSplitRatio = bonusSplitRatio;
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
        return "RediffStock [lastTradedPrice="
                + lastTradedPrice
                + ", volume="
                + volume
                + ", percentageDiff="
                + percentageDiff
                + ", fiftyTwoWeekHigh="
                + fiftyTwoWeekHigh
                + ", fiftyTwoWeekLow="
                + fiftyTwoWeekLow
                + ", lastTradedTime="
                + lastTradedTime
                + ", changePercent="
                + changePercent
                + ", change="
                + change
                + ", marketCap="
                + marketCap
                + ", high="
                + high
                + ", low="
                + low
                + ", prevClose="
                + prevClose
                + ", bonusSplitStatus="
                + bonusSplitStatus
                + ", bonusSplitRatio="
                + bonusSplitRatio
                + ", additionalProperties="
                + additionalProperties
                + "]";
    }
}
