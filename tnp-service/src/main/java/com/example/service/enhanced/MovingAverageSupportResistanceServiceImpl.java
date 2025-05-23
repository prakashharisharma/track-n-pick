package com.example.service.enhanced;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MovingAverageSupportResistanceServiceImpl
        implements MovingAverageSupportResistanceService {

    private final MovingAverageType maType;

    @Autowired private SupportResistanceConfirmationService supportResistanceConfirmationService;
    @Autowired private BreakoutBreakdownConfirmationService breakoutBreakdownConfirmationService;
    @Autowired private SupportResistanceUtilService supportResistanceService;
    @Autowired private CandleStickService candleStickService;
    @Autowired private BreakoutService breakoutService;
    @Autowired private FormulaService formulaService;
    @Autowired private StockPriceService<StockPrice> stockPriceService;
    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    public MovingAverageSupportResistanceServiceImpl(MovingAverageType maType) {
        this.maType = maType;
    }

    @Override
    public boolean isBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double ma = maType.resolve(timeframe, stockTechnicals);
        double prevMa = maType.resolvePrev(timeframe, stockTechnicals);

        return breakoutBreakdownConfirmationService.isBreakoutConfirmed(
                        timeframe, stockPrice, stockTechnicals, ma)
                && breakoutService.isBreakOut(stockPrice, ma, prevMa);
    }

    @Override
    public boolean isBreakdown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double ma = maType.resolve(timeframe, stockTechnicals);
        double prevMa = maType.resolvePrev(timeframe, stockTechnicals);

        return breakoutBreakdownConfirmationService.isBreakdownConfirmed(
                        timeframe, stockPrice, stockTechnicals, ma)
                && breakoutService.isBreakDown(stockPrice, ma, prevMa);
    }

    @Override
    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double ma = maType.resolve(timeframe, stockTechnicals);
        double prevMa = maType.resolvePrev(timeframe, stockTechnicals);

        if (supportResistanceService.isNearSupport(
                stockPrice.getOpen(),
                stockPrice.getHigh(),
                stockPrice.getLow(),
                stockPrice.getClose(),
                ma)) {
            return true;
        }

        return supportResistanceConfirmationService.isSupportConfirmed(
                        timeframe, stockPrice, stockTechnicals, ma)
                && supportResistanceService.isNearSupport(
                        stockPrice.getPrevOpen(),
                        stockPrice.getPrevHigh(),
                        stockPrice.getPrevLow(),
                        stockPrice.getPrevClose(),
                        prevMa);
    }

    @Override
    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        double ma = maType.resolve(timeframe, stockTechnicals);
        double prevMa = maType.resolvePrev(timeframe, stockTechnicals);

        if (supportResistanceService.isNearResistance(
                stockPrice.getOpen(),
                stockPrice.getHigh(),
                stockPrice.getLow(),
                stockPrice.getClose(),
                ma)) {
            return true;
        }

        return supportResistanceConfirmationService.isResistanceConfirmed(
                        timeframe, stockPrice, stockTechnicals, ma)
                && supportResistanceService.isNearResistance(
                        stockPrice.getPrevOpen(),
                        stockPrice.getPrevHigh(),
                        stockPrice.getPrevLow(),
                        stockPrice.getPrevClose(),
                        prevMa);
    }
}
