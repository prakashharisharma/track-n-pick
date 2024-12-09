package com.example.service.impl;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.CandleStickService;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandleStickServiceImpl implements CandleStickService {

    @Autowired
    private FormulaService formulaService;

    @Override
    public boolean isSellingWickPresent(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice.getHigh() == stockPrice.getOpen()){
            return Boolean.FALSE;
        }

        double openHighdiff = stockPrice.getHigh() - stockPrice.getOpen();
        double closeHighDiff = stockPrice.getHigh() - stockPrice.getClose();

        double highWickPerOfBody =  formulaService.calculatePercentRate(openHighdiff, closeHighDiff);

        if(highWickPerOfBody > 22.0){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isGreen(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice!=null && stockPrice.getOpen() < stockPrice.getClose()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isRed(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice!=null && stockPrice.getOpen() > stockPrice.getClose()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isDoji(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getClose())){
            log.info("Doji candle active {}", stock.getNseSymbol());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isGravestoneDoji(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(this.isDoji(stock)){
            if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getLow())){
                log.info("GraveStone Doji candle active {}", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isDragonflyDoji(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(this.isDoji(stock)){
            if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getHigh())){
                log.info("GragonFly Doji candle active {}", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isHangingMan(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

            double bodySize = 0.0;
            double lowerWickSize = 0.0;
            boolean upperWick = true;


            if(this.isRed(stock)){
                bodySize = stockPrice.getOpen() - stockPrice.getClose();
                lowerWickSize = stockPrice.getClose() - stockPrice.getLow();
                if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getHigh())){
                    upperWick = false;
                }
            }else{
                bodySize = stockPrice.getClose() - stockPrice.getOpen();
                lowerWickSize = stockPrice.getOpen() - stockPrice.getLow();
                if(formulaService.isEpsilonEqual(stockPrice.getClose(), stockPrice.getHigh())){
                    upperWick = false;
                }
            }


            double bodyPerOfLowerWick =  formulaService.calculatePercentRate(bodySize+lowerWickSize, bodySize);

            if(bodyPerOfLowerWick < 33.0 && !upperWick){
                log.info("HangingMan / Hammer candle active {}", stock.getNseSymbol());
                return Boolean.TRUE;
            }


        return Boolean.FALSE;
    }

    @Override
    public boolean isHammer(Stock stock) {
        return this.isHangingMan(stock);
    }

    @Override
    public boolean isShootingStar(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        double bodySize = 0.0;
        double upperWickSize = 0.0;
        boolean lowerWick = true;


        if(this.isRed(stock)){
            bodySize = stockPrice.getOpen() - stockPrice.getClose();
            upperWickSize = stockPrice.getHigh() - stockPrice.getOpen();
            if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getLow())){
                lowerWick = false;
            }
        }else{
            bodySize = stockPrice.getClose() - stockPrice.getOpen();
            upperWickSize = stockPrice.getHigh() - stockPrice.getClose();
            if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getLow())){
                lowerWick = false;
            }
        }


        double bodyPerOfUpperWick =  formulaService.calculatePercentRate(bodySize+upperWickSize, bodySize);

        if(bodyPerOfUpperWick < 33.0 && !lowerWick){
            log.info("Shooting Start / Inverted Hammer candle active {}", stock.getNseSymbol());
            return Boolean.TRUE;
        }


        return Boolean.FALSE;
    }

    @Override
    public boolean isInvertedHammer(Stock stock) {
        return this.isShootingStar(stock);
    }

    @Override
    public boolean isBearishEngulfing(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(this.isRed(stock)){
            if(stockPrice.getHigh() > stockPrice.getPrevHigh()){
                if(stockPrice.getLow() < stockPrice.getPrevLow()){
                    if(stockPrice.getOpen() > stockPrice.getPrevClose()){
                        if(stockPrice.getClose() < stockPrice.getPrevOpen()){
                            log.info("Bearish Engulfing candle active {}", stock.getNseSymbol());
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishEngulfing(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(this.isGreen(stock)){
            if(stockPrice.getHigh() > stockPrice.getPrevHigh()){
                if(stockPrice.getLow() < stockPrice.getPrevLow()){
                    if(stockPrice.getOpen() < stockPrice.getPrevClose()){
                        if(stockPrice.getClose() > stockPrice.getPrevOpen()){
                            log.info("Bullish Engulfing candle active {}", stock.getNseSymbol());
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishMarubozu(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

            if( this.isRed(stock)
                &&
                formulaService.isEpsilonEqual(stockPrice.getClose(), stockPrice.getLow())
                &&
                formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getHigh())
            ){
                log.info("Bearish Marubozu candle active {}", stock.getNseSymbol());
                return Boolean.TRUE;
            }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishhMarubozu(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

            if(this.isGreen(stock)
                &&
                formulaService.isEpsilonEqual(stockPrice.getClose(), stockPrice.getHigh())
                &&
                formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getLow())
            ){
                log.info("Bullish Marubozu candle active {}", stock.getNseSymbol());
                return Boolean.TRUE;
            }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishOpenEqualPrevClose(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
            if(this.isGreen(stock)
                    &&
                    formulaService.isEpsilonEqual(stockPrice.getPrevClose(), stockPrice.getOpen())
                    &&
                    (stockPrice.getClose() >= stockPrice.getPrevOpen())
                    &&
                    !this.isSellingWickPresent(stock)
                    &&
                    (stockPrice.getPrevOpen() > stockPrice.getPrevClose())
            ) {
                return Boolean.TRUE;
            }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishhOpenEqualPrevClose(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isRed(stock)
                &&
                formulaService.isEpsilonEqual(stockPrice.getPrevClose(), stockPrice.getOpen())
                &&
                (stockPrice.getClose() <= stockPrice.getPrevOpen())
                &&
                this.isSellingWickPresent(stock)
                &&
                (stockPrice.getPrevOpen() > stockPrice.getPrevClose())
        ) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
