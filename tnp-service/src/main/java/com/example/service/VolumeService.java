package com.example.service;

import com.example.model.master.Stock;

public interface VolumeService {

    public boolean isVolumeAboveWeeklyAndMonthlyAverage(Stock stock);

    public boolean isPreviousSessionVolumeAboveWeeklyAndMonthlyAverage(Stock stock);

    public boolean isVolumeAboveWeeklyAndMonthlyAverage(Stock stock, double weeklyBenchmark, double monthlyBenchmark);

    public boolean isPreviousSessionVolumeAboveWeeklyAndMonthlyAverage(Stock stock, double weeklyBenchmark, double monthlyBenchmark);


    public boolean isVolumeAboveDailyAndWeeklyAverage(Stock stock);

    public boolean isVolumeAboveDailyAndWeeklyAverage(Stock stock, double dailyBenchmark, double weeklyBenchmark);


    public boolean isVolumeWeeklyAboveMonthlyAverage(Stock stock);

    public boolean isVolumeWeeklyAboveMonthlyAverage(Stock stock, double benchmark);

    public boolean isPreviousSessionVolumeWeeklyAboveMonthlyAverage(Stock stock, double benchmark);
    public boolean isVolumeAboveMonthlyAverage(Stock stock);
    public boolean isVolumeAboveMonthlyAverage(Stock stock, double benchmark);

    public boolean isPreviousSessionVolumeAboveMonthlyAverage(Stock stock);

    public boolean isPreviousSessionVolumeAboveMonthlyAverage(Stock stock, double benchmark);

    public boolean isVolumeAboveWeeklyAverage(Stock stock);
    public boolean isVolumeAboveWeeklyAverage(Stock stock, double benchmark);

    public boolean isPreviousSessionVolumeAboveWeeklyAverage(Stock stock);
    public boolean isPreviousSessionVolumeAboveWeeklyAverage(Stock stock, double benchmark);

    public boolean isVolumeHigherThanPreviousSession(Stock stock);
    public boolean isVolumeHigherThanPreviousSession(Stock stock, double benchmark);

    public boolean isVolumeLowerThanPreviousSession(Stock stock);
    public boolean isVolumeLowerThanPreviousSession(Stock stock, double benchmark);

    public boolean isVolumeIncreasedDailyMonthly(Stock stock);

    public boolean isVolumeIncreasedDailyMonthly(Stock stock, double benchMark, double benchMarkPreviousSession);

    public boolean isVolumeIncreasedDailyWeekly(Stock stock);

    public boolean isVolumeIncreasedDailyWeekly(Stock stock, double benchMark, double benchMarkPreviousSession);


    public boolean isVolumeIncreasedWeeklyMonthly(Stock stock);

    public boolean isVolumeIncreasedWeeklyMonthly(Stock stock, double benchMark, double benchMarkPreviousSession);

}
