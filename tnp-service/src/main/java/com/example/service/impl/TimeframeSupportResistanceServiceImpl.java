package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.*;
import com.example.service.util.StockPriceUtil;
import com.example.util.MiscUtil;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
        if(trend == Trend.LONG || trend == Trend.MEDIUM || trend ==Trend.SHORT) {
            if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousWeek(stock, miscUtil.previousWeekFirstDay()), Boolean.FALSE)) {
                if (monthlySupportResistanceService.isBullish(stock)) {
                    if (weeklySupportResistanceService.isBullish(stock)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }
        else if(trend == Trend.LONG || trend == Trend.MEDIUM) {
            if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousMonth(stock, miscUtil.previousMonthFirstDay()), Boolean.FALSE)) {
                if (quarterlySupportResistanceService.isBullish(stock)) {
                    if (monthlySupportResistanceService.isBullish(stock) || weeklySupportResistanceService.isBullish(stock)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }
        else if(trend == Trend.LONG) {
            if (candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousQuarter(stock, miscUtil.previousQuarterFirstDay()), Boolean.FALSE)) {
                if (yearlySupportResistanceService.isBullish(stock)) {
                    if (quarterlySupportResistanceService.isBullish(stock) || monthlySupportResistanceService.isBullish(stock) || weeklySupportResistanceService.isBullish(stock)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }


        return Boolean.FALSE;
    }

    @Override
    public boolean isBearish(Stock stock, Trend trend) {
        if(trend ==Trend.SHORT || trend ==Trend.MEDIUM || trend ==Trend.LONG) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousWeek(stock, miscUtil.previousWeekFirstDay()), Boolean.FALSE)) {
                if (monthlySupportResistanceService.isBearish(stock)) {
                    if (weeklySupportResistanceService.isBearish(stock)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }
        else if(trend ==Trend.MEDIUM || trend ==Trend.LONG) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousMonth(stock, miscUtil.previousMonthFirstDay()), Boolean.FALSE)) {
                if (quarterlySupportResistanceService.isBearish(stock)) {
                    if (monthlySupportResistanceService.isBearish(stock) || weeklySupportResistanceService.isBearish(stock)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }
        else if(trend ==Trend.LONG) {
            if (candleStickHelperService.isBearishConfirmed(StockPriceUtil.buildStockPricePreviousQuarter(stock, miscUtil.previousQuarterFirstDay()), Boolean.FALSE)) {
                if (yearlySupportResistanceService.isBearish(stock)) {
                    if (quarterlySupportResistanceService.isBearish(stock) || monthlySupportResistanceService.isBearish(stock) || weeklySupportResistanceService.isBearish(stock)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }
}
