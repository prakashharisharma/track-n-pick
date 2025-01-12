package com.example.service;

import com.example.model.master.Stock;

public interface VolumeActionService {

    public boolean isHighVolumeAboveWeeklyAverage(Stock stock);
    public boolean isVolumeAboveWeeklyAverage(Stock stock);
    public boolean isVolumeAbovePreviousDay(Stock stock);
    public boolean isVolumeAboveMonthlyAverage(Stock stock);

    public boolean isLiquidityInVolume(Stock stock);
}
