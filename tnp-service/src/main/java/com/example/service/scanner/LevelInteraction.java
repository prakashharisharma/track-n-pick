package com.example.service.scanner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelInteraction {
    private PriceLevel level;
    private double distanceFromClose;
    private double distancePercent;
    private boolean tested;
    private boolean highTested;
    private boolean lowTested;
    private boolean breakout;
    private Double breakoutPercent;
    private boolean strongHold;
    private double strength;
}
