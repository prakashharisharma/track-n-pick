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

    private static double DEFAULT_SELLING_WICK_PER = 20.0;

    private static double DEFAULT_BUYING_WICK_PER = 60.0;
    @Autowired
    private FormulaService formulaService;

    @Override
    public boolean isSellingWickPresent(Stock stock) {

        return this.isSellingWickPresent(stock, DEFAULT_SELLING_WICK_PER);
    }

    @Override
    public boolean isSellingWickPresent(Stock stock, double benchmark) {
        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice.getHigh() == stockPrice.getOpen()){
            return Boolean.FALSE;
        }



        double bodySize = stockPrice.getHigh() - stockPrice.getLow();

        double upperWickSize = stockPrice.getHigh() - stockPrice.getClose();

        if(this.isRed(stock)) {
            upperWickSize = stockPrice.getHigh() - stockPrice.getOpen();
        }

        double highWickPerOfBody =  formulaService.calculatePercentRate(bodySize, upperWickSize);

        log.info("{} -  {} upperWickSize, {} bodySize, {} sellingWick% {}", stock.getNseSymbol(), upperWickSize, bodySize, highWickPerOfBody);

        if(Math.ceil(highWickPerOfBody) >= benchmark){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isSellingWickPresent(double open, double high, double low, double close, double benchmark) {

        return this.isSellingWickPresent(this.buildStockPrice(open, high, low, close), benchmark);
    }

    @Override
    public boolean isBuyingWickPresent(Stock stock) {

        return this.isBuyingWickPresent(stock, DEFAULT_BUYING_WICK_PER);

    }

    @Override
    public boolean isBuyingWickPresent(Stock stock, double benchmark) {
        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice.getLow() == stockPrice.getClose()){
            return Boolean.FALSE;
        }

        double bodySize =  stockPrice.getHigh() - stockPrice.getLow();

        double lowerWickSize = stockPrice.getClose() - stockPrice.getLow();

        if(this.isGreen(stock)) {
            lowerWickSize = stockPrice.getOpen() - stockPrice.getLow();
        }


        double lowerWickPerOfBody =  formulaService.calculatePercentRate(bodySize, lowerWickSize);
        log.info("{} -  {} lowerWickSize, {} bodySize, {} buyingWick% {}", stock.getNseSymbol(), lowerWickSize, bodySize, lowerWickPerOfBody);
        if(Math.ceil(lowerWickPerOfBody) >= benchmark){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBuyingWickPresent(double open, double high, double low, double close, double benchmark) {


        return this.isBuyingWickPresent(this.buildStockPrice(open, high, low, close), benchmark);
    }

    private Stock buildStockPrice(double open, double high, double low, double close){

        StockPrice stockPrice = new StockPrice();

        stockPrice.setOpen(open);
        stockPrice.setHigh(high);
        stockPrice.setLow(low);
        stockPrice.setClose(close);

        Stock stock = new Stock();
        stock.setNseSymbol("NA");
        stock.setStockPrice(stockPrice);

        return stock;
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
           // if(stockPrice.getHigh() > stockPrice.getPrevHigh()){
             //   if(stockPrice.getLow() < stockPrice.getPrevLow()){
                    if(stockPrice.getOpen() > stockPrice.getPrevClose()){
                        if(stockPrice.getClose() < stockPrice.getPrevOpen()){
                            log.info("Bearish Engulfing candle active {}", stock.getNseSymbol());
                            return Boolean.TRUE;
                        }
                    }
             //   }
           // }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishEngulfing(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(this.isGreen(stock)){
           // if(stockPrice.getHigh() > stockPrice.getPrevHigh()){
             //   if(stockPrice.getLow() < stockPrice.getPrevLow()){
                    if(stockPrice.getOpen() < stockPrice.getPrevClose()){
                        if(stockPrice.getClose() > stockPrice.getPrevOpen()){
                            log.info("Bullish Engulfing candle active {}", stock.getNseSymbol());
                            return Boolean.TRUE;
                        }
                    }
             //   }
           // }
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

    @Override
    public boolean isTweezerTop(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();;
        if(this.isRed(stock)){
            if(stockPrice.getPrevClose() == stockPrice.getOpen()){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
