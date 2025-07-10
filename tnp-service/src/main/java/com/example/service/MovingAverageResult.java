package com.example.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MovingAverageResult {
    private final Double value;
    private final Double prevValue;
}
