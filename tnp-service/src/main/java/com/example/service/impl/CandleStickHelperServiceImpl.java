package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.CandleStickHelperService;
import com.example.service.CandleStickService;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.service.CandleStickService.MAX_WICK_SIZE;
import static com.example.service.CandleStickService.MIN_RANGE;

@Slf4j
@Service
public class CandleStickHelperServiceImpl implements CandleStickHelperService {

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private FormulaService formulaService;

    @Override
    public boolean isUpperWickSizeConfirmed(Stock stock) {

        StockPrice stockPrice = stock.getStockPrice();

        double risk = Math.abs(formulaService.calculateChangePercentage(stockPrice.getHigh(), stockPrice.getLow()));

        if((candleStickService.upperWickSize(stock) <= FibonacciRatio.RATIO_161_8 * 10 ) && risk <= 5.0){
            return Boolean.TRUE;
        }else if((candleStickService.upperWickSize(stock) <= MAX_WICK_SIZE) && risk <= 10.0){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isLowerWickSizeConfirmed(Stock stock) {

        if(candleStickService.range(stock) >= MIN_RANGE ){
            return Boolean.TRUE;
        }else if (candleStickService.lowerWickSize(stock) <= candleStickService.body(stock) * 3){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishConfirmed(Stock stock, boolean isWickCheck) {

            if(
                candleStickService.isBullishhMarubozu(stock) ||
                        (!isWickCheck && candleStickService.isBullishSoldiers(stock))||
                        candleStickService.isBullishKicker(stock) ||
                        candleStickService.isBullishSeparatingLine(stock) ||
                        candleStickService.isBullishSash(stock) ||
                        candleStickService.isPiercingPattern(stock) ||
                        candleStickService.isBullishOutsideBar(stock) ||
                        candleStickService.isBullishEngulfing(stock) ||
                        candleStickService.isHammer(stock) ||
                        (!isWickCheck && candleStickService.isOpenLow(stock)) ||
                        candleStickService.isBullishPinBar(stock) ||
                        candleStickService.isDoubleLow(stock) ||
                        candleStickService.isTweezerBottom(stock) ||
                        candleStickService.isBullishInsideBar(stock) ||
                        candleStickService.isBullishHarami(stock) ||
                        candleStickService.isInDecisionPrevConfirmationBullish(stock)) {
                if (isWickCheck) {
                     if(this.isUpperWickSizeConfirmed(stock)){
                    log.info("{} bullish candlestick active on {}", stock.getNseSymbol(), stock.getStockPrice().getBhavDate());
                    return Boolean.TRUE;
                     }
                }else {
                    log.info("{} bullish candlestick active on {} wickCheck {}", stock.getNseSymbol(), stock.getStockPrice().getBhavDate(),isWickCheck);
                    return Boolean.TRUE;
                }
            }
            else if(!isWickCheck && (candleStickService.isHigherHigh(stock) && candleStickService.isHigherLow(stock))){

                if(candleStickService.isHammer(stock)
                        || (candleStickService.isPreviousDayGreen(stock) && candleStickService.isCloseAbovePrevClose(stock))
                        || (candleStickService.isPreviousDayRed(stock) && candleStickService.isCloseAbovePrevOpen(stock))
                        ) {
                log.info("{} bullish candlestick higher high and higher low on {}", stock.getNseSymbol(), stock.getStockPrice().getBhavDate());
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishConfirmed(Stock stock, boolean isWickCheck) {

        if (
                candleStickService.isBearishMarubozu(stock) ||
                        (!isWickCheck && candleStickService.isBearishSoldiers(stock) )||
                        candleStickService.isBearishKicker(stock) ||
                        candleStickService.isBearishSeparatingLine(stock) ||
                        candleStickService.isBearishSash(stock) ||
                        candleStickService.isDarkCloudCover(stock) ||
                        candleStickService.isBearishOutsideBar(stock) ||
                        candleStickService.isBearishEngulfing(stock) ||
                        candleStickService.isShootingStar(stock) ||
                        (!isWickCheck && candleStickService.isOpenHigh(stock)) ||
                        candleStickService.isBearishPinBar(stock) ||
                        candleStickService.isDoubleHigh(stock) ||
                        candleStickService.isTweezerTop(stock) ||
                        candleStickService.isBearishInsideBar(stock) ||
                        candleStickService.isBearishHarami(stock) ||
                        candleStickService.isInDecisionPrevConfirmationBearish(stock)) {
            if (isWickCheck) {
                if (this.isLowerWickSizeConfirmed(stock)) {
                    log.info("{} bearish candlestick active on {}", stock.getNseSymbol(), stock.getStockPrice().getBhavDate());
                    return Boolean.TRUE;
                }
            } else {
                log.info("{} bearish candlestick active on {} wickCheck {}", stock.getNseSymbol(), stock.getStockPrice().getBhavDate(), isWickCheck);
                return Boolean.TRUE;
            }
        } else if (!isWickCheck && (candleStickService.isLowerHigh(stock) && candleStickService.isLowerLow(stock))) {
            if (candleStickService.isShootingStar(stock)
                    || (candleStickService.isPreviousDayGreen(stock) && candleStickService.isCloseBelowPrevOpen(stock))
                    || (candleStickService.isPreviousDayRed(stock) && candleStickService.isCloseBelowPrevClose(stock))
            ) {
                log.info("{} bearish candlestick lower high & lower low on {}", stock.getNseSymbol(), stock.getStockPrice().getBhavDate());
                return Boolean.TRUE;
            }

        }
        return Boolean.FALSE;
    }

}
