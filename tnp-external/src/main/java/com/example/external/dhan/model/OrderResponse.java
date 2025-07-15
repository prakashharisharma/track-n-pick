package com.example.external.dhan.model;

import com.example.data.transactional.entities.type.dhan.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderResponse {
    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("orderStatus")
    private OrderStatus status;
}
