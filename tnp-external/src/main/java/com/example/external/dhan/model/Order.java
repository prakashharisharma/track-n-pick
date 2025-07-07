package com.example.external.dhan.model;

import com.example.data.transactional.entities.type.dhan.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Order {
    @JsonProperty("dhanClientId")
    private String dhanClientId;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("orderStatus")
    private OrderStatus orderStatus;

    @JsonProperty("transactionType")
    private TransactionType transactionType;

    @JsonProperty("exchangeSegment")
    private String exchangeSegment;

    @JsonProperty("productType")
    private ProductType productType;

    @JsonProperty("orderType")
    private OrderType orderType;

    private Validity validity;

    private String tradingSymbol;

    @JsonProperty("securityId")
    private String securityId;

    private Integer quantity;

    @JsonProperty("disclosedQuantity")
    private Integer disclosedQuantity;

    private Double price;

    @JsonProperty("triggerPrice")
    private Double triggerPrice;

    @JsonProperty("afterMarketOrder")
    private Boolean afterMarketOrder;

    @JsonProperty("boProfitValue")
    private Double boProfitValue;

    @JsonProperty("boStopLossValue")
    private Double boStopLossValue;

    private String legName;

    @JsonProperty("createTime")
    private LocalDateTime createTime;

    @JsonProperty("updateTime")
    private LocalDateTime updateTime;

    @JsonProperty("exchangeTime")
    private LocalDateTime exchangeTime;

    @JsonProperty("drvExpiryDate")
    private LocalDateTime drvExpiryDate;

    @JsonProperty("drvOptionType")
    private String drvOptionType;

    @JsonProperty("drvStrikePrice")
    private Double drvStrikePrice;

    @JsonProperty("omsErrorCode")
    private String omsErrorCode;

    @JsonProperty("omsErrorDescription")
    private String omsErrorDescription;

    @JsonProperty("algoId")
    private String algoId;

    @JsonProperty("remainingQuantity")
    private Integer remainingQuantity;

    @JsonProperty("averageTradedPrice")
    private Double averageTradedPrice;

    @JsonProperty("filledQty")
    private Integer filledQty;
}
