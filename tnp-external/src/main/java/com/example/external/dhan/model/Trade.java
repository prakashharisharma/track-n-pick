package com.example.external.dhan.model;

import com.example.data.transactional.entities.type.dhan.OrderType;
import com.example.data.transactional.entities.type.dhan.ProductType;
import com.example.data.transactional.entities.type.dhan.TransactionType;
import lombok.Data;

@Data
public class Trade {
    private String dhanClientId;
    private String orderId;
    private String exchangeOrderId;
    private String exchangeTradeId;
    private TransactionType transactionType;
    private String exchangeSegment;
    private ProductType productType;
    private OrderType orderType;
    private String tradingSymbol;
    private String customSymbol;
    private String securityId;
    private int tradedQuantity;
    private double tradedPrice;
    private String createTime;
    private String updateTime;
    private String exchangeTime;
    private String drvExpiryDate;
    private String drvOptionType;
    private double drvStrikePrice;
}
