package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SupportResistanceServiceImpl implements SupportResistanceService {

    private final SupportResistanceConfirmationService supportResistanceConfirmationService;
    private final BreakoutBreakdownConfirmationService breakoutBreakdownConfirmationService;
    private final SupportResistanceUtilService supportResistanceService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    // private final CandleStickService candleStickService;
    private final BreakoutService breakoutService;
    // @Autowired private CalendarService calendarService;
    // @Autowired private FormulaService formulaService;
    // @Autowired private OhlcvService ohlcvService;
    // @Autowired private MiscUtil miscUtil;

    @Override
    public boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        Stock stock = stockPrice.getStock();
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals htTechnicals = stockTechnicalsService.get(stock, timeframe);

        double resistance = htStockPrice.getHigh();
        if (stockPrice.getOpen() < htStockPrice.getLow()) {
            resistance = htStockPrice.getLow();
        }

        if (breakoutBreakdownConfirmationService.isBreakoutConfirmed(
                timeframe, stockPrice, unused, resistance)) {
            return breakoutService.isBreakOut(stockPrice, resistance, resistance);
        }

        return false;
    }

    @Override
    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Stock stock = stockPrice.getStock();
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals htTechnicals = stockTechnicalsService.get(stock, timeframe);

        double support = htStockPrice.getLow();
        if (stockPrice.getOpen() > htStockPrice.getHigh()) {
            support = htStockPrice.getHigh();
        }

        if (supportResistanceService.isNearSupport(
                stockPrice,
                support) && supportResistanceConfirmationService.isSupportConfirmed(
                timeframe, stockPrice, stockTechnicals, support)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        Stock stock = stockPrice.getStock();
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals htTechnicals = stockTechnicalsService.get(stock, timeframe);

        double support = htStockPrice.getLow();
        if (stockPrice.getOpen() > htStockPrice.getHigh()) {
            support = htStockPrice.getHigh();
        }

        if (breakoutBreakdownConfirmationService.isBreakdownConfirmed(
                timeframe, stockPrice, unused, support)) {
            return breakoutService.isBreakDown(stockPrice, support, support);
        }

        return false;
    }

    @Override
    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Stock stock = stockPrice.getStock();
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals htTechnicals = stockTechnicalsService.get(stock, timeframe);

        double resistance = htStockPrice.getHigh();
        if (stockPrice.getOpen() < htStockPrice.getLow()) {
            resistance = htStockPrice.getLow();
        }

        if (supportResistanceService.isNearResistance(
                stockPrice,
                resistance) && supportResistanceConfirmationService.isResistanceConfirmed(
                timeframe, stockPrice, stockTechnicals, resistance)) {
            return true;
        }


        return false;
    }
}
