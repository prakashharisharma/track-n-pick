package com.example.service.dhan.model;

public record PositionDetails(
        long finalQuantity, double finalValue, long disclosedQuantity, double remainingFunds) {}
