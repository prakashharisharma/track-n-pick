package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.CandleStickService;
import com.example.service.util.StockPriceUtil;
import com.example.util.FibonacciRatio;
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
    public boolean isDead(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();
        if(stockPrice.getOpen() == stockPrice.getHigh()){
            if(stockPrice.getOpen() == stockPrice.getLow()){
                if(stockPrice.getOpen() == stockPrice.getClose()){
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public double upperWickSize(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();
        if(this.isGreen(stock)) {
            return Math.abs(formulaService.calculateChangePercentage(stockPrice.getClose(), stockPrice.getHigh()));
        }
        return Math.abs(formulaService.calculateChangePercentage(stockPrice.getOpen(), stockPrice.getHigh()));
    }

    @Override
    public double lowerWickSize(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isGreen(stock)) {
            return Math.abs(formulaService.calculateChangePercentage(stockPrice.getOpen(), stockPrice.getLow()));
        }

        return Math.abs(formulaService.calculateChangePercentage(stockPrice.getClose(), stockPrice.getLow()));
    }

    @Override
    public double body(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isGreen(stock)) {
            return Math.abs(formulaService.calculateChangePercentage(stockPrice.getOpen(), stockPrice.getClose()));
        }

        return Math.abs(formulaService.calculateChangePercentage(stockPrice.getClose(), stockPrice.getOpen()));

    }

    @Override
    public double bodyPrev(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();;

        return this.body(StockPriceUtil.buildStockPrice(stock.getNseSymbol(), stockPrice.getPrevOpen(), stockPrice.getPrevHigh(), stockPrice.getPrevLow(), stockPrice.getPrevClose()));
    }

    @Override
    public double range(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        return Math.abs(formulaService.calculateChangePercentage(stockPrice.getLow(), stockPrice.getHigh()));
    }

    @Override
    public double rangePrev(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();;

        return this.range(StockPriceUtil.buildStockPrice(stock.getNseSymbol(), stockPrice.getPrevOpen(), stockPrice.getPrevHigh(), stockPrice.getPrevLow(), stockPrice.getPrevClose()));
    }

    @Override
    public boolean isCloseAbovePrevClose(Stock stock) {

            return stock.getStockPrice().getClose() > stock.getStockPrice().getPrevClose();

    }

    @Override
    public boolean isCloseBelowPrevClose(Stock stock) {

            return stock.getStockPrice().getClose() < stock.getStockPrice().getPrevClose();

    }

    @Override
    public boolean isOpenAbovePrevClose(Stock stock) {
        return stock.getStockPrice().getOpen() > stock.getStockPrice().getPrevClose();
    }

    @Override
    public boolean isOpenBelowPrevClose(Stock stock) {
        return stock.getStockPrice().getOpen() < stock.getStockPrice().getPrevClose();
    }

    @Override
    public boolean isOpenAtPrevOpen(Stock stock) {
        return stock.getStockPrice().getOpen() == stock.getStockPrice().getPrevOpen();
    }

    @Override
    public boolean isOpenAtPrevClose(Stock stock) {
        return stock.getStockPrice().getOpen() == stock.getStockPrice().getPrevClose();
    }

    @Override
    public boolean isOpenAbovePrevOpen(Stock stock) {
        return stock.getStockPrice().getOpen() > stock.getStockPrice().getPrevOpen();
    }

    @Override
    public boolean isOpenBelowPrevOpen(Stock stock) {
        return stock.getStockPrice().getOpen() < stock.getStockPrice().getPrevOpen();
    }

    @Override
    public boolean isCloseAbovePrevOpen(Stock stock) {
        return stock.getStockPrice().getClose() > stock.getStockPrice().getPrevOpen();
    }

    @Override
    public boolean isCloseBelowPrevOpen(Stock stock) {
        return stock.getStockPrice().getClose() < stock.getStockPrice().getPrevOpen();
    }

    @Override
    public boolean isOpenAndLowEqual(Stock stock) {
        return stock.getStockPrice().getOpen() == stock.getStockPrice().getLow();
    }

    @Override
    public boolean isOpenAndHighEqual(Stock stock) {
        return stock.getStockPrice().getOpen() == stock.getStockPrice().getHigh();
    }

    @Override
    public boolean isCloseAndLowEqual(Stock stock) {
        return stock.getStockPrice().getClose() == stock.getStockPrice().getLow();
    }

    @Override
    public boolean isCloseAndHighEqual(Stock stock) {
        return stock.getStockPrice().getClose() == stock.getStockPrice().getHigh();
    }

    @Override
    public boolean isCloseBelowPrevLow(Stock stock) {
        return stock.getStockPrice().getClose() < stock.getStockPrice().getPrevLow();
    }

    @Override
    public boolean isHigherHigh(Stock stock){
        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice.getHigh() > stockPrice.getPrevHigh()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isHigherLow(Stock stock){
        StockPrice stockPrice = stock.getStockPrice();
        if (stockPrice.getLow() > stockPrice.getPrevLow()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isLowerHigh(Stock stock){
        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice.getHigh() < stockPrice.getPrevHigh()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isLowerLow(Stock stock){
        StockPrice stockPrice = stock.getStockPrice();
        if (stockPrice.getLow() < stockPrice.getPrevLow()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

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

        double bodySize = stockPrice.getHigh() - stockPrice.getOpen();

        double upperWickSize = stockPrice.getHigh() - stockPrice.getClose();

        if(this.isRed(stock)) {
            bodySize = stockPrice.getHigh() - stockPrice.getClose();
            upperWickSize = stockPrice.getHigh() - stockPrice.getOpen();
        }

        double highWickPerOfBody =  formulaService.calculatePercentage(bodySize, upperWickSize);

        if(highWickPerOfBody >= benchmark){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isSellingWickPresent(double open, double high, double low, double close, double benchmark) {

        return this.isSellingWickPresent(StockPriceUtil.buildStockPrice("NA",open, high, low, close), benchmark);
    }

    @Override
    public boolean isBuyingWickPresent(Stock stock) {

        return this.isBuyingWickPresent(stock, BUYING_WICK_PER);

    }

    @Override
    public boolean isBuyingWickPresent(Stock stock, double benchmark) {
        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice.getLow() == stockPrice.getClose()){
            return Boolean.FALSE;
        }

        double bodySize =  stockPrice.getClose() - stockPrice.getLow();

        double lowerWickSize = stockPrice.getOpen() - stockPrice.getLow();

        if(this.isRed(stock)) {
            bodySize =  stockPrice.getOpen() - stockPrice.getLow();
            lowerWickSize = stockPrice.getClose() - stockPrice.getLow();
        }


        double lowerWickPerOfBody =  formulaService.calculatePercentage(bodySize, lowerWickSize);
        //log.debug("{} -  {} lowerWickSize, {} bodySize, {} buyingWick% {}", stock.getNseSymbol(), lowerWickSize, bodySize, lowerWickPerOfBody);
        if(Math.ceil(lowerWickPerOfBody) >= benchmark){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBuyingWickPresent(double open, double high, double low, double close, double benchmark) {


        return this.isBuyingWickPresent(StockPriceUtil.buildStockPrice("NA" ,open, high, low, close), benchmark);
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
    public boolean isGapUp(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isOpenAbovePrevClose(stock) || this.isOpenAtPrevClose(stock)){
           // if(this.isHigherHigh(stock) && this.isHigherLow(stock)){
                //if(stockPrice.getLow() > stockPrice.getPrevClose()){
                    return Boolean.TRUE;
                //}
            //}
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isGapDown(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isOpenBelowPrevClose(stock) || this.isOpenAtPrevClose(stock)){
           // if(this.isLowerHigh(stock) && this.isLowerLow(stock)){
                //if(stockPrice.getHigh() < stockPrice.getPrevOpen()){
                    return Boolean.TRUE;
                //}
           // }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isRisingWindow(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isGreen(stock)) {
            if (stockPrice.getLow() > stockPrice.getPrevHigh()) {
                log.info("{} rising window active", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isFallingWindow(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isRed(stock)) {
            if (stockPrice.getHigh() < stockPrice.getPrevLow()) {
                log.info("{} falling window active", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isPreviousDayGreen(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice!=null && stockPrice.getPrevOpen() < stockPrice.getPrevClose()){
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
    public boolean isPreviousDayRed(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();

        if(stockPrice!=null && stockPrice.getPrevOpen() > stockPrice.getPrevClose()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isDoji(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8)){

            if (this.upperWickSize(stock) <= this.lowerWickSize(stock) * 3) {
            if (this.lowerWickSize(stock) <= this.upperWickSize(stock) * 3) {
            if (this.upperWickSize(stock) >= this.body(stock) * 2) {
              if (this.lowerWickSize(stock) >= this.body(stock) * 2) {
                  log.info("{} doji candle active", stock.getNseSymbol());
                    return Boolean.TRUE;
                   }
                 }
               }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isDojiPrev(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();

        stock = StockPriceUtil.buildStockPrice(stock.getNseSymbol(),
                stockPrice.getPrevOpen(), stockPrice.getPrevHigh(), stockPrice.getPrevLow(), stockPrice.getPrevClose());

        return isDoji(stock);
    }

    @Override
    public boolean isGravestoneDoji(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8)){
            if(stockPrice.getOpen() == stockPrice.getLow()){
                        log.info("{} graveStone doji candle active", stock.getNseSymbol());
                        return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isDragonflyDoji(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8)){
            if(stockPrice.getOpen() == stockPrice.getHigh()){

                                log.info("{} dragonFly doji candle active", stock.getNseSymbol());

                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishPinBar(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();


        if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8)){
            if(this.lowerWickSize(stock) > this.upperWickSize(stock) * 3) {
                log.info("{} bullish pin bar active", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishPinBar(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();

        if(formulaService.isEpsilonEqual(stockPrice.getOpen(), stockPrice.getClose(), FibonacciRatio.RATIO_261_8)){
            if(this.upperWickSize(stock) > this.lowerWickSize(stock) * 3) {
                log.info("{} bearish pin bar active", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isSpinningTop(Stock stock) {

        double bodySize = this.body(stock);
        if (bodySize > FibonacciRatio.RATIO_261_8 && bodySize <= MIN_BODY_SIZE) {
            if (this.upperWickSize(stock) <= this.lowerWickSize(stock) * 3) {
                if (this.lowerWickSize(stock) <= this.upperWickSize(stock) * 3) {
                    if (this.upperWickSize(stock) >= this.body(stock) * 2) {
                        if (this.lowerWickSize(stock) >= this.body(stock) * 2) {
                            log.info("{} spinning top candle active", stock.getNseSymbol());
                            return Boolean.TRUE;
                        }
                    }
                }
            }
    }


        return Boolean.FALSE;
    }

    @Override
    public boolean isSpinningTopPrev(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();

        stock = StockPriceUtil.buildStockPrice(stock.getNseSymbol(),
                stockPrice.getPrevOpen(), stockPrice.getPrevHigh(), stockPrice.getPrevLow(), stockPrice.getPrevClose());

        return this.isSpinningTop(stock);
    }

    @Override
    public boolean isInDecision(Stock stock) {
        return this.isDoji(stock) || this.isSpinningTop(stock);
    }

    @Override
    public boolean isInDecisionPrev(Stock stock) {
        return this.isDojiPrev(stock) || this.isSpinningTopPrev(stock);
    }

    @Override
    public boolean isInDecisionPrevConfirmationBullish(Stock stock) {
        boolean isConfirmation = Boolean.FALSE;

        if(this.isGreen(stock) && this.isInDecisionPrev(stock)){
            if(!this.isInDecision(stock)) {
                if (this.isHigherHigh(stock)) {
                    if (this.isPreviousDayRed(stock) && this.isCloseAbovePrevOpen(stock)) {
                        isConfirmation = Boolean.TRUE;
                    } else if (this.isPreviousDayGreen(stock) && this.isCloseAbovePrevClose(stock)) {
                        isConfirmation = Boolean.TRUE;
                    }
                } else if (this.isHammer(stock)) {
                    isConfirmation = Boolean.TRUE;
                }
            }
        }

        if(isConfirmation){
            log.info("{} bullish doji confirmed", stock.getNseSymbol());
        }

        return isConfirmation;
    }

    @Override
    public boolean isInDecisionPrevConfirmationBearish(Stock stock) {
        boolean isConfirmation = Boolean.FALSE;
        //
        if(this.isRed(stock) && this.isInDecisionPrev(stock)) {
            if(!this.isInDecision(stock)) {
                if (this.isLowerLow(stock)) {
                    if (this.isPreviousDayRed(stock) && this.isCloseBelowPrevClose(stock)) {
                        isConfirmation = Boolean.TRUE;
                    } else if (this.isPreviousDayGreen(stock) && this.isCloseBelowPrevOpen(stock)) {
                        isConfirmation = Boolean.TRUE;
                    }
                }else if (this.isShootingStar(stock)) {
                    isConfirmation = Boolean.TRUE;
                }
            }
        }

        if(isConfirmation){
            log.info("{} bearish doji confirmed", stock.getNseSymbol());
        }

        return isConfirmation;
    }

    @Override
    public boolean isHangingMan(Stock stock) {

        double bodySize = this.body(stock);
        if(this.lowerWickSize(stock) > bodySize * 3){
            if (this.upperWickSize(stock) < bodySize) {
                if (bodySize > FibonacciRatio.RATIO_261_8) {
                    if (this.isGreen(stock) || (this.isRed(stock) && bodySize <= FibonacciRatio.RATIO_100_0 * 10)) {
                        log.info("{} hangingMan / hammer candle active", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isHammer(Stock stock) {
        //if(this.isPreviousDayRed(stock)) {
            return this.isHangingMan(stock);
        //}
        //return Boolean.FALSE;
    }

    @Override
    public boolean isShootingStar(Stock stock) {

        double bodySize = this.body(stock);
        if(this.upperWickSize(stock) > bodySize * 3) {
            if (this.lowerWickSize(stock) < bodySize){
                if (bodySize > FibonacciRatio.RATIO_261_8) {
                    if (this.isRed(stock) || (this.isGreen(stock) && bodySize <= FibonacciRatio.RATIO_100_0 * 10)) {
                        log.info("{} shooting star / inverted hammer candle active", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
        }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isInvertedHammer(Stock stock) {
        //if(this.isPreviousDayRed(stock)) {
            return this.isShootingStar(stock);
        //}
        //return Boolean.FALSE;
    }

    @Override
    public boolean isOpenHigh(Stock stock) {

        double bodySize = this.body(stock);

        if (bodySize > FibonacciRatio.RATIO_261_8) {
            if(this.isOpenAndHighEqual(stock) && !this.isHammer(stock)){
                log.info("{} open high candle active", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isOpenLow(Stock stock) {
        double bodySize = this.body(stock);

        if (bodySize > FibonacciRatio.RATIO_261_8) {
            if(this.isOpenAndLowEqual(stock) && !this.isShootingStar(stock)){
                log.info("{} open low candle active", stock.getNseSymbol());
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishEngulfing(Stock stock) {

        if(this.isRed(stock) && (this.isPreviousDayGreen(stock))){
            if(this.body(stock) > this.bodyPrev(stock)  && this.body(stock) >= MIN_BODY_SIZE ) {
                if (this.isOpenAbovePrevClose(stock)) {
                    if (this.isCloseBelowPrevOpen(stock)) {
                        log.info("{} bearish engulfing candle active", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishEngulfing(Stock stock) {
        if(this.isGreen(stock) && (this.isPreviousDayRed(stock))){
            if(this.body(stock) > this.bodyPrev(stock)  && this.body(stock) >= MIN_BODY_SIZE) {
                if (this.isOpenBelowPrevClose(stock)) {
                    if (this.isCloseAbovePrevOpen(stock)) {
                        log.info("{} bullish engulfing candle active", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishOutsideBar(Stock stock) {

        if(this.isGreen(stock) && (this.isPreviousDayRed(stock))){
            if( this.range(stock) > this.rangePrev(stock)  && this.range(stock) >= MIN_RANGE) {
                if(this.body(stock) >= this.bodyPrev(stock) ){
                    if(this.lowerWickSize(stock) > this.upperWickSize(stock)) {
                        if (this.isLowerLow(stock)) {
                            if (this.isHigherHigh(stock)) {
                                log.info("Bullish Outside bar candle active {}", stock.getNseSymbol());
                                return Boolean.TRUE;
                            }
                        }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishOutsideBar(Stock stock) {

        if(this.isRed(stock) && (this.isPreviousDayGreen(stock))){
            if(this.range(stock) > this.rangePrev(stock)  && this.range(stock) >= MIN_RANGE) {
                if(this.body(stock) >= this.bodyPrev(stock) ) {
                    if(this.lowerWickSize(stock) < this.upperWickSize(stock)) {
                        if (this.isHigherHigh(stock)) {
                            if (this.isLowerLow(stock)) {
                                log.info("Bearish Outside bar candle active {}", stock.getNseSymbol());
                                return Boolean.TRUE;
                            }
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
                this.isOpenAndHighEqual(stock)
                &&
                this.isCloseAndLowEqual(stock)
            ){
                if(this.body(stock) >= MIN_BODY_SIZE) {
                    log.info("Bearish Marubozu candle active {}", stock.getNseSymbol());
                    return Boolean.TRUE;
                }
            }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishhMarubozu(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

            if(this.isGreen(stock)
                &&
                this.isOpenAndLowEqual(stock)
                &&
                this.isCloseAndHighEqual(stock)
            ){
                if(this.body(stock) >= MIN_BODY_SIZE) {
                    log.info("{} Bullish Marubozu candle active", stock.getNseSymbol());
                    return Boolean.TRUE;
                }
            }

        return Boolean.FALSE;
    }

    @Override
    public boolean isTweezerTop(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isRed(stock)){
            if((this.isPreviousDayRed(stock) && this.isOpenAtPrevOpen(stock)) || ( this.isPreviousDayGreen(stock) && this.isOpenAtPrevClose(stock))){
                if(this.body(stock) >= MIN_BODY_SIZE) {
                    log.info("{} Tweezer Top candle active", stock.getNseSymbol());
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDoubleHigh(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isRed(stock)){
            if(formulaService.isEpsilonEqual(stockPrice.getHigh(), stockPrice.getPrevHigh(), FibonacciRatio.RATIO_161_8)){
                if(this.range(stock) >= MIN_RANGE) {
                    log.info("{} double high candle active", stock.getNseSymbol());
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDoubleTop(Stock stock) {
        return false;
    }


    @Override
    public boolean isTweezerBottom(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isGreen(stock)){
            if((this.isPreviousDayRed(stock) && this.isOpenAtPrevClose(stock)) || ( this.isPreviousDayGreen(stock) && this.isOpenAtPrevOpen(stock))){
                if(this.body(stock) >= MIN_BODY_SIZE) {
                    log.info("{} Tweezer Bottom candle active", stock.getNseSymbol());
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDoubleLow(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isGreen(stock)){
            if(formulaService.isEpsilonEqual(stockPrice.getLow(), stockPrice.getPrevLow(), FibonacciRatio.RATIO_161_8)){
                if(this.range(stock) >= MIN_RANGE) {
                    log.info("{} Double low candle active", stock.getNseSymbol());
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isDoubleBottom(Stock stock) {
        return false;
    }

    @Override
    public boolean isDarkCloudCover(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isRed(stock) && this.isPreviousDayGreen(stock)){
            double prevMid = (stockPrice.getPrevOpen() + stockPrice.getPrevClose())/2;
            if(stockPrice.getOpen() > stockPrice.getPrevClose()){
                if(stockPrice.getClose() <= prevMid){
                    if(this.bodyPrev(stock) >= MIN_BODY_SIZE ) {
                        if(this.isCloseAbovePrevOpen(stock)) {
                            log.info("Dark Cloud Cover candle active {}", stock.getNseSymbol());
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isPiercingPattern(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        if(this.isGreen(stock) && this.isPreviousDayRed(stock)){
            double prevMid = (stockPrice.getPrevOpen() + stockPrice.getPrevClose())/2;
            if(stockPrice.getOpen() < stockPrice.getPrevClose()){
                if(stockPrice.getClose() >= prevMid){
                    if(this.bodyPrev(stock) >= MIN_BODY_SIZE) {
                        if(this.isCloseBelowPrevOpen(stock)) {
                            log.info("Piercing Pattern candle active {}", stock.getNseSymbol());
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }



    @Override
    public boolean isBullishKicker(Stock stock) {

        if (this.isGreen(stock) && this.isPreviousDayRed(stock)) {
            if(this.isGapUp(stock)) {
                if (this.bodyPrev(stock) >= MIN_BODY_SIZE) {
                    if (this.isOpenAbovePrevOpen(stock)) {
                        log.info("Bullish Kicker candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }
    @Override
    public boolean isBearishKicker(Stock stock) {

        if (this.isRed(stock) && this.isPreviousDayGreen(stock)) {
            if (this.isGapDown(stock)) {
                if (this.bodyPrev(stock) >= MIN_BODY_SIZE) {
                    if (this.isOpenBelowPrevOpen(stock)) {
                        log.info("Bearish Kicker candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishSash(Stock stock) {

        if(this.isGreen(stock) && this.isPreviousDayRed(stock)){
            if(this.isGapUp(stock)){
                if(this.body(stock) >= MIN_BODY_SIZE){
                    if(this.isOpenBelowPrevOpen(stock) && this.isCloseAbovePrevOpen(stock)) {
                        log.info("Bullish Sash candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishSash(Stock stock) {
        if(this.isRed(stock) && this.isPreviousDayGreen(stock)){
            if(this.isGapDown(stock)){
                if(this.body(stock) >= MIN_BODY_SIZE){
                    if(this.isOpenAbovePrevOpen(stock) && this.isCloseBelowPrevOpen(stock)) {
                        log.info("Bearish Sash candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishSeparatingLine(Stock stock) {
        if(this.isGreen(stock) && (this.isPreviousDayRed(stock))){
            if(this.isGapUp(stock)){
                if(this.body(stock) >= MIN_BODY_SIZE){
                    if(this.isOpenAtPrevOpen(stock)) {
                        log.info("Bullish Separating Line candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishSeparatingLine(Stock stock) {
        if(this.isRed(stock) && (this.isPreviousDayGreen(stock))){
            if(this.isGapDown(stock)){
                if(this.body(stock) >= MIN_BODY_SIZE){
                    if(this.isOpenAtPrevOpen(stock)) {
                        log.info("Bearish Separating Line candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishHarami(Stock stock) {

        if(this.isRed(stock) && this.isPreviousDayGreen(stock)){
                    if (this.isOpenBelowPrevClose(stock)) {
                        if (this.isCloseAbovePrevOpen(stock)) {
                            if (this.bodyPrev(stock) >= FibonacciRatio.RATIO_161_8 * 10) {
                                //if (this.isLowerHigh(stock) && this.isHigherLow(stock)) {
                                    log.info("Bearish Harami candle active {}", stock.getNseSymbol());
                                    return Boolean.TRUE;
                                //}
                            }
                        }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishHarami(Stock stock) {

        if(this.isGreen(stock) && this.isPreviousDayRed(stock)) {
                    if (this.isOpenAbovePrevClose(stock)) {
                        if (this.isCloseBelowPrevOpen(stock)) {
                            if (this.bodyPrev(stock) >= MIN_BODY_SIZE) {
                                //if (this.isLowerHigh(stock) && this.isHigherLow(stock)) {
                                    log.info("Bullish Harami candle active {}", stock.getNseSymbol());
                                    return Boolean.TRUE;
                                //}
                            }
                        }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishInsideBar(Stock stock) {

        if(this.isGreen(stock) && (this.isPreviousDayRed(stock))){
            if(this.range(stock) <= this.rangePrev(stock)  && this.rangePrev(stock) >= FibonacciRatio.RATIO_38_2 * 100) {
                if (this.isLowerHigh(stock)) {
                    if (this.isHigherLow(stock)) {
                        log.info("Bullish Inside bar candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishInsideBar(Stock stock) {

        if(this.isRed(stock) && (this.isPreviousDayGreen(stock))){
            if(this.range(stock) < this.rangePrev(stock)  && this.rangePrev(stock) >= MIN_RANGE) {
                if (this.isLowerHigh(stock)) {
                    if (this.isHigherLow(stock)) {
                        log.info("Bearish Inside bar candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishSoldiers(Stock stock) {

        if(this.isGreen(stock) && (this.isPreviousDayGreen(stock))){
            if(!this.isShootingStar(stock)) {
                if (this.isHigherHigh(stock) && this.isHigherLow(stock)) {
                    if (this.body(stock) > MIN_BODY_SIZE || ((this.body(stock) > this.bodyPrev(stock)) && this.range(stock) > MIN_RANGE)) {
                        log.info("Bullish Soldiers candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishSoldiers(Stock stock) {
        if(this.isRed(stock) && (this.isPreviousDayRed(stock))){
            if(!this.isHammer(stock)) {
                if (this.isLowerHigh(stock) && this.isLowerLow(stock)) {
                    if (this.body(stock) > MIN_BODY_SIZE || ((this.body(stock) > this.bodyPrev(stock)) && this.range(stock) > MIN_RANGE)) {
                        log.info("Bearish Soldiers candle active {}", stock.getNseSymbol());
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }
}
