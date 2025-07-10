package com.example.external.dhan.model;

import com.example.data.transactional.entities.type.dhan.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class OrderRequest {
    @JsonProperty("dhanClientId")
    private String dhanClientId;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("transactionType")
    private TransactionType transactionType;

    @JsonProperty("exchangeSegment")
    private String exchangeSegment;

    @JsonProperty("productType")
    private ProductType productType;

    @JsonProperty("orderType")
    private OrderType orderType;

    private Validity validity;

    @JsonProperty("securityId")
    private String securityId;

    private String quantity;

    @JsonProperty("disclosedQuantity")
    private String disclosedQuantity;

    private String price;

    @JsonProperty("triggerPrice")
    private String triggerPrice;

    @JsonProperty("afterMarketOrder")
    private Boolean afterMarketOrder;

    @JsonProperty("amoTime")
    private AmoTime amoTime;

    @JsonProperty("boProfitValue")
    private String boProfitValue;

    @JsonProperty("boStopLossValue")
    private String boStopLossValue;
}
