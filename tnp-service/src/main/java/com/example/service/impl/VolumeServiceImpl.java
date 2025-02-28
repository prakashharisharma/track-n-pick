package com.example.service.impl;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.BreakoutLedgerService;
import com.example.service.VolumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VolumeServiceImpl implements VolumeService {

    private static final double DAILY_BENCHMARK = 8.0;
    private static final double WEEKLY_BENCHMARK = 4.0;
    private static final double MONTHLY_BENCHMARK = 2.0;

    private static long MIN_AVG_VOLUME = 50000;

    private static double MIN_VALUE = 1000.0;
    @Autowired
    private BreakoutLedgerService breakoutLedgerService;


    /**
     * Volume > Monthly
     * Volume > Weekly * 2.5
     * Or Weekly > Monthly
     * Volume > Weekly * 2
     * @param stock
     * @return
     */
    @Override
    public boolean isVolumeAboveWeeklyAndMonthlyAverage(Stock stock) {

        return this.isVolumeAboveWeeklyAndMonthlyAverage(stock, WEEKLY_BENCHMARK, MONTHLY_BENCHMARK);
    }

    @Override
    public boolean isPreviousSessionVolumeAboveWeeklyAndMonthlyAverage(Stock stock) {
        return this.isPreviousSessionVolumeAboveWeeklyAndMonthlyAverage(stock, WEEKLY_BENCHMARK, MONTHLY_BENCHMARK);
    }

    @Override
    public boolean isVolumeAboveWeeklyAndMonthlyAverage(Stock stock, final double weeklyBenchMark, final double monthlyBenchMark) {
        StockTechnicals stockTechnicals = stock.getTechnicals();;
        boolean isAboveAverage = Boolean.FALSE;
        if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * this.validate(monthlyBenchMark)){
            if(stockTechnicals.getVolumeAvg5() > stockTechnicals.getVolumeAvg20()){
                isAboveAverage = Boolean.TRUE;
            }else if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg5() * this.validate(weeklyBenchMark)){
                isAboveAverage = Boolean.TRUE;
            }
        }else if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20()){
            if(stockTechnicals.getVolumeAvg5() > stockTechnicals.getVolumeAvg20() * this.validate(monthlyBenchMark)){
                isAboveAverage = Boolean.TRUE;
            }else if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg5() * this.validate(weeklyBenchMark) * this.validate(monthlyBenchMark)){
                isAboveAverage = Boolean.TRUE;
            }
        }

        if(isAboveAverage){
            this.createLedgerEntry(stock);
        }

        return isAboveAverage;
    }

    @Override
    public boolean isPreviousSessionVolumeAboveWeeklyAndMonthlyAverage(Stock stock, double weeklyBenchMark, double monthlyBenchMark) {
        StockTechnicals stockTechnicals = stock.getTechnicals();;
        boolean isAboveAverage = Boolean.FALSE;
        if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolumeAvg20Prev() * this.validate(monthlyBenchMark)){
            if(stockTechnicals.getVolumeAvg5Prev() > stockTechnicals.getVolumeAvg20Prev()){
                isAboveAverage = Boolean.TRUE;
            }else if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolumeAvg5Prev() * this.validate(weeklyBenchMark)){
                isAboveAverage = Boolean.TRUE;
            }
        }else if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolumeAvg20Prev()){
            if(stockTechnicals.getVolumeAvg5Prev() > stockTechnicals.getVolumeAvg20Prev() * this.validate(monthlyBenchMark)){
                isAboveAverage = Boolean.TRUE;
            }else if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolumeAvg5Prev() * this.validate(weeklyBenchMark) * this.validate(monthlyBenchMark)){
                isAboveAverage = Boolean.TRUE;
            }
        }

        if(isAboveAverage){
            this.createLedgerEntry(stock);
        }

        return isAboveAverage;
    }

    @Override
    public boolean isVolumeAboveDailyAndWeeklyAverage(Stock stock) {
        return this.isVolumeAboveDailyAndWeeklyAverage(stock, WEEKLY_BENCHMARK, MONTHLY_BENCHMARK);
    }

    @Override
    public boolean isVolumeAboveDailyAndWeeklyAverage(Stock stock, double dailyBenchmark, double weeklyBenchmark) {
        StockTechnicals stockTechnicals = stock.getTechnicals();;
        boolean isAboveAverage = Boolean.FALSE;
        if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolumeAvg5() * this.validate(weeklyBenchmark)){
            if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg5()){
                isAboveAverage = Boolean.TRUE;
            }else if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolume() * this.validate(dailyBenchmark)){
                isAboveAverage = Boolean.TRUE;
            }
        }else if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolumeAvg5()){
            if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg5() * this.validate(weeklyBenchmark)){
                isAboveAverage = Boolean.TRUE;
            }else if(stockTechnicals.getVolumePrev() > stockTechnicals.getVolume() * this.validate(dailyBenchmark) * this.validate(weeklyBenchmark)){
                isAboveAverage = Boolean.TRUE;
            }
        }

        if(isAboveAverage){
            this.createLedgerEntry(stock);
        }

        return isAboveAverage;
    }

    @Override
    public boolean isVolumeWeeklyAboveMonthlyAverage(Stock stock) {
        return this.isVolumeHigherThanMonthlyAverage(stock, MONTHLY_BENCHMARK);
    }

    @Override
    public boolean isVolumeWeeklyAboveMonthlyAverage(Stock stock, double benchmark) {
        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolumeAvg5() > stockTechnicals.getVolumeAvg20() * this.validate(benchmark)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeHigherThanMonthlyAverage(Stock stock) {
        return this.isVolumeHigherThanMonthlyAverage(stock, MONTHLY_BENCHMARK);
    }

    @Override
    public boolean isVolumeHigherThanMonthlyAverage(Stock stock, double benchmark) {
        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * this.validate(benchmark)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isPreviousSessionVolumeAboveMonthlyAverage(Stock stock) {
        return this.isPreviousSessionVolumeAboveMonthlyAverage(stock, MONTHLY_BENCHMARK);
    }

    @Override
    public boolean isPreviousSessionVolumeAboveMonthlyAverage(Stock stock, double benchmark) {
        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolumePrev() < stockTechnicals.getVolumeAvg20Prev() * this.validate(benchmark)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeAboveWeeklyAverage(Stock stock) {

        return this.isVolumeAboveWeeklyAverage(stock, WEEKLY_BENCHMARK);
    }

    @Override
    public boolean isVolumeAboveWeeklyAverage(Stock stock, double benchmark) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg5() * this.validate(benchmark)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeHigherThanPreviousSession(Stock stock) {

        return this.isVolumeHigherThanPreviousSession(stock, DAILY_BENCHMARK);
    }

    @Override
    public boolean isVolumeHigherThanPreviousSession(Stock stock, double benchmark) {
        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() > stockTechnicals.getVolumePrev() * this.validate(benchmark)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeLowerThanPreviousSession(Stock stock) {
        return this.isVolumeLowerThanPreviousSession(stock, 1.0);
    }

    @Override
    public boolean isVolumeLowerThanPreviousSession(Stock stock, double benchmark) {
        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() < stockTechnicals.getVolumePrev() * this.validate(benchmark)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeIncreasedDailyMonthly(Stock stock) {
        return this.isVolumeIncreasedDailyMonthly(stock, 2.0, 2.0);
    }

    @Override
    public boolean isVolumeIncreasedDailyMonthly(Stock stock, double benchMark, double benchMarkPreviousSession) {


        if(this.isVolumeHigherThanMonthlyAverage(stock, this.validate(benchMark)) && this.isVolumeHigherThanPreviousSession(stock, 1.0) ){
            return Boolean.TRUE;
        }else if (this.isPreviousSessionVolumeAboveMonthlyAverage(stock, this.validate(benchMarkPreviousSession)) && this.isVolumeLowerThanPreviousSession(stock, 1.0)){
            return Boolean.TRUE;
        }else if(this.isVolumeHigherThanMonthlyAverage(stock, (benchMark > 2.0 ? benchMark /2 : benchMark)) && this.isPreviousSessionVolumeAboveMonthlyAverage(stock, (benchMarkPreviousSession > 2.0 ? benchMarkPreviousSession /2 : benchMarkPreviousSession)) ){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeIncreasedWeeklyMonthly(Stock stock) {
        return this.isVolumeAboveWeeklyAndMonthlyAverage(stock) || this.isPreviousSessionVolumeAboveWeeklyAndMonthlyAverage(stock);
    }

    @Override
    public boolean isVolumeIncreasedWeeklyMonthly(Stock stock, double benchMark, double benchMarkPreviousSession) {
        return this.isVolumeAboveWeeklyAndMonthlyAverage(stock,benchMark, benchMark/2) || this.isPreviousSessionVolumeAboveWeeklyAndMonthlyAverage(stock, benchMarkPreviousSession, benchMarkPreviousSession/2);
    }

    private void createLedgerEntry(Stock stock){
        breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.VOLUME_HIGH);
    }

    private double validate(double benchmark){
        return benchmark <= 0.0 ? 1.0 : benchmark;
    }

}
