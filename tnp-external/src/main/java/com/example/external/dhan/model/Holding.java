package com.example.external.dhan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Holding {

    private String exchange;

    @JsonProperty("tradingSymbol")
    private String tradingSymbol;

    @JsonProperty("securityId")
    private String securityId;

    private String isin;

    @JsonProperty("totalQty")
    private Integer totalQty;

    @JsonProperty("dpQty")
    private Integer dpQty;

    @JsonProperty("t1Qty")
    private Integer t1Qty;

    @JsonProperty("availableQty")
    private Integer availableQty;

    @JsonProperty("collateralQty")
    private Integer collateralQty;

    @JsonProperty("avgCostPrice")
    private Double avgCostPrice;
}
