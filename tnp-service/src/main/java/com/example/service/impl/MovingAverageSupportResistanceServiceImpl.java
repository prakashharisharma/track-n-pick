package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MovingAverageSupportResistanceServiceImpl implements MovingAverageSupportResistanceService {

    @Autowired
    private ShortTermMovingAverageSupportResistanceService shortTermMovingAverageSupportResistanceService;
    @Autowired
    private MediumTermMovingAverageSupportResistanceService mediumTermMovingAverageSupportResistanceService;
    @Autowired
    private LongTermMovingAverageSupportResistanceService longTermMovingAverageSupportResistanceService;

    @Autowired
    private  SupportResistanceConfirmationService supportResistanceConfirmationService;
    @Autowired
    private BreakoutConfirmationService breakoutConfirmationService;


    @Autowired
    private SupportResistanceUtilService supportResistanceService;

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private BreakoutService breakoutService;

    @Autowired
    private FormulaService formulaService;

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
        if (trend.getMomentum() == Momentum.PULLBACK) {
            if (this.isMultiAverageBreakout(stock,trend) ||  mediumTermMovingAverageSupportResistanceService.isBreakout(stock) || shortTermMovingAverageSupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        }
        else if (trend.getMomentum() == Momentum.CORRECTION) {
            if (this.isMultiAverageBreakout(stock,trend) || longTermMovingAverageSupportResistanceService.isBreakout(stock) || mediumTermMovingAverageSupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        } else if (trend.getMomentum() == Momentum.BOTTOM) {
            if (this.isMultiAverageBreakout(stock,trend) || longTermMovingAverageSupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }

        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isNearSupport(Stock stock, Trend trend) {
        if (trend.getMomentum() == Momentum.PULLBACK) {
            if (this.isMultiAverageSupport(stock,trend) || mediumTermMovingAverageSupportResistanceService.isNearSupport(stock) || shortTermMovingAverageSupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
        }
        else if (trend.getMomentum() == Momentum.CORRECTION) {
            if (this.isMultiAverageSupport(stock,trend) || longTermMovingAverageSupportResistanceService.isNearSupport(stock) || mediumTermMovingAverageSupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
        } else if (trend.getMomentum() == Momentum.BOTTOM) {
            if (this.isMultiAverageSupport(stock,trend) || longTermMovingAverageSupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }

        }
        return Boolean.FALSE;
    }

    public boolean isMultiAverageBreakout(Stock stock, Trend trend) {
        if(trend.getMomentum() == Momentum.PULLBACK){
            if (mediumTermMovingAverageSupportResistanceService.isBreakout(stock) && shortTermMovingAverageSupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.CORRECTION){
            if (longTermMovingAverageSupportResistanceService.isBreakout(stock) && mediumTermMovingAverageSupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.BOTTOM){
            if (longTermMovingAverageSupportResistanceService.isBreakout(stock) && mediumTermMovingAverageSupportResistanceService.isBreakout(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    public boolean isMultiAverageSupport(Stock stock, Trend trend) {

        if(trend.getMomentum() == Momentum.PULLBACK){
            if (mediumTermMovingAverageSupportResistanceService.isNearSupport(stock) && shortTermMovingAverageSupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.CORRECTION){
            if (longTermMovingAverageSupportResistanceService.isNearSupport(stock) && mediumTermMovingAverageSupportResistanceService.isNearSupport(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.BOTTOM){
            if (longTermMovingAverageSupportResistanceService.isNearSupport(stock) && mediumTermMovingAverageSupportResistanceService.isNearSupport(stock)) {
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
        if (trend.getMomentum() == Momentum.RECOVERY) {
            if (this.isMultiAverageBreakDown(stock,trend) || mediumTermMovingAverageSupportResistanceService.isBreakdown(stock) || shortTermMovingAverageSupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }
        else if (trend.getMomentum() == Momentum.ADVANCE) {
            if ( this.isMultiAverageBreakDown(stock,trend) || longTermMovingAverageSupportResistanceService.isBreakdown(stock) || mediumTermMovingAverageSupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }
        else if (trend.getMomentum() == Momentum.TOP) {
            if (this.isMultiAverageBreakDown(stock,trend) || longTermMovingAverageSupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isNearResistance(Stock stock, Trend trend) {
        if (trend.getMomentum() == Momentum.RECOVERY) {
            if (this.isMultiAverageResistance(stock,trend) ||  mediumTermMovingAverageSupportResistanceService.isNearResistance(stock) || shortTermMovingAverageSupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }
        else if (trend.getMomentum() == Momentum.ADVANCE) {
            if ( this.isMultiAverageResistance(stock,trend) || longTermMovingAverageSupportResistanceService.isNearResistance(stock) || mediumTermMovingAverageSupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        } else if (trend.getMomentum() == Momentum.TOP) {
            if (this.isMultiAverageResistance(stock,trend) || longTermMovingAverageSupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private boolean isMultiAverageBreakDown(Stock stock, Trend trend) {

        if(trend.getMomentum() == Momentum.RECOVERY){
            if (mediumTermMovingAverageSupportResistanceService.isBreakdown(stock) && shortTermMovingAverageSupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.ADVANCE){
            if (longTermMovingAverageSupportResistanceService.isBreakdown(stock) && mediumTermMovingAverageSupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.TOP){
            if (longTermMovingAverageSupportResistanceService.isBreakdown(stock) && mediumTermMovingAverageSupportResistanceService.isBreakdown(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isMultiAverageResistance(Stock stock, Trend trend) {

        if(trend.getMomentum() == Momentum.RECOVERY){
            if (mediumTermMovingAverageSupportResistanceService.isNearResistance(stock) && shortTermMovingAverageSupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.ADVANCE){
            if (longTermMovingAverageSupportResistanceService.isNearResistance(stock) && mediumTermMovingAverageSupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }
        else if(trend.getMomentum() == Momentum.TOP){
            if (longTermMovingAverageSupportResistanceService.isNearResistance(stock) && mediumTermMovingAverageSupportResistanceService.isNearResistance(stock)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
