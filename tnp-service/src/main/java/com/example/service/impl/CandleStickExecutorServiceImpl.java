package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.CandleStickExecutorService;
import com.example.service.CandleStickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandleStickExecutorServiceImpl implements CandleStickExecutorService {

    @Autowired
    private CandleStickService candleStickService;

    @Override
    public void executeBullish(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals!=null && stockTechnicals.getPrevRsi() <= 30.0) {
            candleStickService.isDoji(stock);
            candleStickService.isDragonflyDoji(stock);
            candleStickService.isHammer(stock);
            candleStickService.isInvertedHammer(stock);
            candleStickService.isBullishEngulfing(stock);
            candleStickService.isBullishhMarubozu(stock);
            candleStickService.isBullishOpenEqualPrevClose(stock);
        }
    }

    @Override
    public void executeBearish(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        if(stockTechnicals!=null && stockTechnicals.getPrevRsi() >= 70.0) {
            candleStickService.isDoji(stock);
            candleStickService.isGravestoneDoji(stock);
            candleStickService.isHangingMan(stock);
            candleStickService.isShootingStar(stock);
            candleStickService.isBearishEngulfing(stock);
            candleStickService.isBearishMarubozu(stock);
            candleStickService.isBearishhOpenEqualPrevClose(stock);
        }
    }
}
