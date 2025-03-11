package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.*;
import com.example.service.util.StockPriceUtil;
import com.example.util.MiscUtil;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TimeframeSupportResistanceServiceImpl implements TimeframeSupportResistanceService {

    @Autowired
    private YearlySupportResistanceService yearlySupportResistanceService;

    @Autowired
    private QuarterlySupportResistanceService quarterlySupportResistanceService;
    @Autowired
    private MonthlySupportResistanceService monthlySupportResistanceService;

    @Autowired
    private WeeklySupportResistanceService weeklySupportResistanceService;

    @Autowired
    private DailySupportResistanceService dailySupportResistanceService;
    @Autowired
    private CandleStickHelperService candleStickHelperService;

    @Autowired
    private MiscUtil miscUtil;
    /**
     *
     * 1. Breakout monthly and weekly resistance
     * 1.1. Breakout can be confirmed using PlusDi increasing, MinusDi decreasing and ADX increasing
     * 2. Near support monthly and weekly
     * 2.1 Support can be confirmed using RSI
     * a. Check for prior 2 weeks candle and see if bullish
     * b. Check for daily candle and see if bullish
     * Should be executed in a downtrend
     * @param stock
     * @return
     *
     */
    @Override
    public boolean isBullish(Stock stock, Trend trend) {
        /*
         * 1. Breakout
         */
        if(this.isBreakout(stock, trend)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Support
         */
        if(this.isNearSupport(stock, trend)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBreakout(Stock stock, Trend trend) {
        if(trend.getMomentum() == Momentum.PULLBACK) {
            if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousWeek(stock, miscUtil.previousWeekFirstDay()), Boolean.FALSE)) {
                //if(this.isMultiTimeFrameBreakout(stock) || yearlySupportResistanceService.isBreakout(stock) || quarterlySupportResistanceService.isBreakout(stock) || monthlySupportResistanceService.isBreakout(stock) || weeklySupportResistanceService.isBreakout(stock) || dailySupportResistanceService.isBreakout(stock)){
                    if(this.isMultiTimeFrameBreakout(stock, trend) || weeklySupportResistanceService.isBreakout(stock)){
                        return Boolean.TRUE;
                }
            }
        }

        else if(trend.getMomentum() == Momentum.CORRECTION) {
            if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousMonth(stock, miscUtil.previousMonthFirstDay()), Boolean.FALSE)) {
                //if(this.isMultiTimeFrameBreakout(stock) || yearlySupportResistanceService.isBreakout(stock) || quarterlySupportResistanceService.isBreakout(stock) || monthlySupportResistanceService.isBreakout(stock) || weeklySupportResistanceService.isBreakout(stock)){
                if(this.isMultiTimeFrameBreakout(stock, trend) || monthlySupportResistanceService.isBreakout(stock)){
                    return Boolean.TRUE;
                }
            }
        }
        else if(trend.getMomentum() == Momentum.BOTTOM) {
            //if(this.isMultiTimeFrameBreakout(stock) || yearlySupportResistanceService.isBreakout(stock) || quarterlySupportResistanceService.isBreakout(stock) || monthlySupportResistanceService.isBreakout(stock)){
            if(this.isMultiTimeFrameBreakout(stock, trend) || quarterlySupportResistanceService.isBreakout(stock) || yearlySupportResistanceService.isBreakout(stock)){
                if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousQuarter(stock, miscUtil.previousQuarterFirstDay()), Boolean.FALSE)) {
                      return Boolean.TRUE;
                    }
                }
                else if (yearlySupportResistanceService.isYearHighRecentLowCorrection(stock)) {
                    return Boolean.TRUE;
                }
                else if (yearlySupportResistanceService.isYearHighYearLowCorrection(stock)) {
                    return Boolean.TRUE;
                }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isNearSupport(Stock stock, Trend trend) {
        if(trend.getMomentum() ==Momentum.PULLBACK) {
            if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousWeek(stock, miscUtil.previousWeekFirstDay()), Boolean.FALSE)) {
                //if(this.isMultiTimeFrameSupport(stock) || yearlySupportResistanceService.isNearSupport(stock) || quarterlySupportResistanceService.isNearSupport(stock) || monthlySupportResistanceService.isNearSupport(stock) || weeklySupportResistanceService.isNearSupport(stock) || dailySupportResistanceService.isNearSupport(stock)){
                if(this.isMultiTimeFrameSupport(stock, trend) || weeklySupportResistanceService.isNearSupport(stock)){
                    return Boolean.TRUE;
                }
            }
        }

        else if(trend.getMomentum() == Momentum.CORRECTION) {
            if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousMonth(stock, miscUtil.previousMonthFirstDay()), Boolean.FALSE)) {
                //if(this.isMultiTimeFrameSupport(stock) || yearlySupportResistanceService.isNearSupport(stock) || quarterlySupportResistanceService.isNearSupport(stock) || monthlySupportResistanceService.isNearSupport(stock) || weeklySupportResistanceService.isNearSupport(stock) ){
                if(this.isMultiTimeFrameSupport(stock, trend) || monthlySupportResistanceService.isNearSupport(stock)){
                    return Boolean.TRUE;
                }
            }
        }
        else if(trend.getMomentum() == Momentum.BOTTOM) {
            //if(this.isMultiTimeFrameSupport(stock) || yearlySupportResistanceService.isNearSupport(stock) || quarterlySupportResistanceService.isNearSupport(stock) || monthlySupportResistanceService.isNearSupport(stock)){
            if(this.isMultiTimeFrameSupport(stock, trend)  || quarterlySupportResistanceService.isNearSupport(stock) || yearlySupportResistanceService.isNearSupport(stock)){
                if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousQuarter(stock, miscUtil.previousQuarterFirstDay()), Boolean.FALSE)) {
                        return Boolean.TRUE;
                    }
                }
            else if (yearlySupportResistanceService.isYearHighRecentLowCorrection(stock)) {
                return Boolean.TRUE;
            }
            else if (yearlySupportResistanceService.isYearHighYearLowCorrection(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isMultiTimeFrameBreakout(Stock stock, Trend trend) {


        if(trend.getMomentum() == Momentum.PULLBACK) {
            if (monthlySupportResistanceService.isBreakout(stock) && weeklySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
            else if (monthlySupportResistanceService.isBreakout(stock) && dailySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }else if (weeklySupportResistanceService.isBreakout(stock) && dailySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.CORRECTION) {
            if (quarterlySupportResistanceService.isBreakout(stock) && monthlySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isBreakout(stock) && weeklySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }else if (monthlySupportResistanceService.isBreakout(stock) && weeklySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.BOTTOM){
            if (yearlySupportResistanceService.isBreakout(stock) && quarterlySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            } else if (yearlySupportResistanceService.isBreakout(stock) && monthlySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isBreakout(stock) && monthlySupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isMultiTimeFrameSupport(Stock stock, Trend trend) {

        if(trend.getMomentum() == Momentum.PULLBACK) {
            if (monthlySupportResistanceService.isNearSupport(stock) && weeklySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
            else if (monthlySupportResistanceService.isNearSupport(stock) && dailySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }else if (weeklySupportResistanceService.isNearSupport(stock) && dailySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.CORRECTION) {
            if (quarterlySupportResistanceService.isNearSupport(stock) && monthlySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isNearSupport(stock) && weeklySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }else if (monthlySupportResistanceService.isNearSupport(stock) && weeklySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.BOTTOM){
            if (yearlySupportResistanceService.isNearSupport(stock) && quarterlySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            } else if (yearlySupportResistanceService.isNearSupport(stock) && monthlySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isNearSupport(stock) && monthlySupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearish(Stock stock, Trend trend) {
        /*
         * 1. Breakdown support
         */
        if(this.isBreakdown(stock, trend)){
            return Boolean.TRUE;
        }
        /*
         * 2. Near Resistance
         */
        if(this.isNearResistance(stock, trend)){
            return Boolean.TRUE;
        }

        return Boolean.TRUE;
    }

    @Override
    public boolean isBreakdown(Stock stock, Trend trend) {
        if(trend.getMomentum() ==Momentum.RECOVERY) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousQuarter(stock, miscUtil.previousQuarterFirstDay()), Boolean.FALSE)) {
                //if(this.isMultiTimeFrameBreakdown(stock)|| yearlySupportResistanceService.isBreakdown(stock)|| quarterlySupportResistanceService.isBreakdown(stock) || monthlySupportResistanceService.isBreakdown(stock) || weeklySupportResistanceService.isBreakdown(stock) || dailySupportResistanceService.isBreakdown(stock)){
                if(this.isMultiTimeFrameBreakdown(stock, trend) || weeklySupportResistanceService.isBreakdown(stock) ){
                    return Boolean.TRUE;
                }
            }
        }
        else if(trend.getMomentum() ==Momentum.ADVANCE) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousMonth(stock, miscUtil.previousMonthFirstDay()), Boolean.FALSE)) {
                //if(this.isMultiTimeFrameBreakdown(stock) || quarterlySupportResistanceService.isBreakdown(stock) || monthlySupportResistanceService.isBreakdown(stock) || weeklySupportResistanceService.isBreakdown(stock) || dailySupportResistanceService.isBreakdown(stock)){
                if(this.isMultiTimeFrameBreakdown(stock, trend) || monthlySupportResistanceService.isBreakdown(stock)){
                    return Boolean.TRUE;
                }
            }
        }
        else if(trend.getMomentum() ==Momentum.TOP) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousWeek(stock, miscUtil.previousWeekFirstDay()), Boolean.FALSE)) {
                if(this.isMultiTimeFrameBreakdown(stock, trend) || yearlySupportResistanceService.isBreakdown(stock) || quarterlySupportResistanceService.isBreakdown(stock)){
                    return Boolean.TRUE;
                }
            }
        }



        return Boolean.FALSE;
    }

    @Override
    public boolean isNearResistance(Stock stock, Trend trend) {
        if(trend.getMomentum() ==Momentum.RECOVERY) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousQuarter(stock, miscUtil.previousQuarterFirstDay()), Boolean.FALSE)) {
                if(this.isMultiTimeFrameResistance(stock, trend)  || weeklySupportResistanceService.isNearResistance(stock) ){
                    return Boolean.TRUE;
                }
            }
        }
        else if(trend.getMomentum() ==Momentum.ADVANCE) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousMonth(stock, miscUtil.previousMonthFirstDay()), Boolean.FALSE)) {
                if(this.isMultiTimeFrameResistance(stock, trend) || monthlySupportResistanceService.isNearResistance(stock)){
                    return Boolean.TRUE;
                }
            }
        }
        else if(trend.getMomentum() ==Momentum.TOP) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousWeek(stock, miscUtil.previousWeekFirstDay()), Boolean.FALSE)) {
                if(this.isMultiTimeFrameResistance(stock, trend)  || yearlySupportResistanceService.isNearResistance(stock) || quarterlySupportResistanceService.isNearResistance(stock)){
                    return Boolean.TRUE;
                }
            }
        }



        return Boolean.FALSE;
    }

    @Override
    public boolean isMultiTimeFrameBreakdown(Stock stock, Trend trend) {

        if(trend.getMomentum() == Momentum.RECOVERY) {
            if (monthlySupportResistanceService.isBreakdown(stock) && weeklySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
            else if (monthlySupportResistanceService.isBreakdown(stock) && dailySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }else if (weeklySupportResistanceService.isBreakdown(stock) && dailySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.ADVANCE) {
            if (quarterlySupportResistanceService.isBreakdown(stock) && monthlySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isBreakdown(stock) && weeklySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }else if (monthlySupportResistanceService.isBreakdown(stock) && weeklySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.TOP){
            if (yearlySupportResistanceService.isBreakdown(stock) && quarterlySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            } else if (yearlySupportResistanceService.isBreakdown(stock) && monthlySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isBreakdown(stock) && monthlySupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }


        return Boolean.FALSE;
    }

    @Override
    public boolean isMultiTimeFrameResistance(Stock stock, Trend trend) {


        if(trend.getMomentum() == Momentum.RECOVERY) {
            if (monthlySupportResistanceService.isNearResistance(stock) && weeklySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
            else if (monthlySupportResistanceService.isNearResistance(stock) && dailySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }else if (weeklySupportResistanceService.isNearResistance(stock) && dailySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.ADVANCE) {
            if (quarterlySupportResistanceService.isNearResistance(stock) && monthlySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isNearResistance(stock) && weeklySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }else if (monthlySupportResistanceService.isNearResistance(stock) && weeklySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.TOP){
            if (yearlySupportResistanceService.isNearResistance(stock) && quarterlySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            } else if (yearlySupportResistanceService.isNearResistance(stock) && monthlySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
            else if (quarterlySupportResistanceService.isNearResistance(stock) && monthlySupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
