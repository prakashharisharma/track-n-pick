package com.example.service.impl;

import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.enhanced.service.StockTechnicalsService;
import com.example.model.ledger.BreakoutLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicalsOld;
import com.example.service.BreakoutLedgerService;
import com.example.service.CrossOverUtil;
import com.example.service.MacdIndicatorService;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Timeframe;
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
    public boolean isMacdCrossedSignal(StockTechnicals stockTechnicals) {

        boolean isMAcdCrossedSignal = CrossOverUtil.isFastCrossesAboveSlow(stockTechnicals.getPrevMacd(),
                stockTechnicals.getPrevSignal(), stockTechnicals.getMacd(),
                stockTechnicals.getSignal());


        return isMAcdCrossedSignal;
    }

    @Override
    public boolean isSignalNearHistogram(StockTechnicals stockTechnicals) {

        if(stockTechnicals!=null){

            double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

            if(stockTechnicals.getSignal() < 0.0 || histogram < 0.0){
                return Boolean.TRUE;
            }else if(stockTechnicals.getSignal() <= histogram){
               // breakoutLedgerService.addPositive(stock, timeframe, BreakoutLedger.BreakoutCategory.SIGNAL_NEAR_HISTOGRAM);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isHistogramGreen(StockTechnicals stockTechnicals) {
        //StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        return formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal()) > 0;
    }

    @Override
    public boolean isHistogramIncreased(StockTechnicals stockTechnicals) {

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
    public boolean isHistogramDecreased(StockTechnicals stockTechnicals) {

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
