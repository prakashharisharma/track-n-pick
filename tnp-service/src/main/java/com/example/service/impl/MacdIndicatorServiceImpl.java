package com.example.service.impl;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.BreakoutLedgerService;
import com.example.service.CrossOverUtil;
import com.example.service.MacdIndicatorService;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MacdIndicatorServiceImpl implements MacdIndicatorService {

    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Autowired
    private FormulaService formulaService;
    @Override
    public boolean isMacdCrossedSignal(Stock stock) {
        boolean isMAcdCrossedSignal = CrossOverUtil.isFastCrossesAboveSlow(stock.getTechnicals().getPrevMacd(),
                stock.getTechnicals().getPrevSignal(), stock.getTechnicals().getMacd(),
                stock.getTechnicals().getSignal());

        if(isMAcdCrossedSignal){
            breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.MACD_CROSSED_SIGNAL);
        }

        return isMAcdCrossedSignal;
    }

    @Override
    public boolean isSignalNearHistogram(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals!=null){

            double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

            if(stockTechnicals.getSignal() < 0.0 || histogram < 0.0){
                return Boolean.TRUE;
            }else if(stockTechnicals.getSignal() <= histogram){
                breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.SIGNAL_NEAR_HISTOGRAM);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isHistogramGreen(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        return formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal()) > 0;
    }

    @Override
    public boolean isHistogramIncreased(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals ==null){
            return Boolean.FALSE;
        }

        double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

        double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());

        if(histogram > prevHistogram)  {
            return Boolean.TRUE;

        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isHistogramDecreased(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(stockTechnicals ==null){
            return Boolean.FALSE;
        }

        double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

        double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());

        if(histogram < prevHistogram)  {
            return Boolean.TRUE;

        }
        return Boolean.FALSE;
    }
}
