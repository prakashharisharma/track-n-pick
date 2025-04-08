package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.service.TrendService;
import com.example.service.utils.MovingAverageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrendServiceImpl implements TrendService {

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private boolean isShortestTermUpTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {

        if (timeframe == Timeframe.DAILY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage10(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage10(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage10(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage10(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }
        return Boolean.FALSE;
    }
    /**
     * EMA5, EMA10, EMA20
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermUpTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {

        if (timeframe == Timeframe.DAILY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }
        return Boolean.FALSE;
    }

    /**
     * EMA20, EMA50, EMA100
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermUpTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {
        if (timeframe == Timeframe.DAILY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    /**
     * EMA50, EMA100, EMA200
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermUpTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {

        if (timeframe == Timeframe.DAILY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage200(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage200(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isUpTrend(
                    MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage100(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    private boolean isUpTrend(double average, double prevAverage, double close, double prevClose) {

        // Ensure previous EMA is valid
        if (prevAverage == 0.0) {
            return true; // Not enough data to determine trend, will return true by default
        }

        // Condition 1: Price Above EMA (Price trading above moving average)
        boolean priceAboveEMA = close > average;

        // Condition 2: Slope of EMA (EMA is trending upward)
        boolean emaSlopeUp = average > prevAverage;

        // Condition 3: EMA Support Bounce (Price was below EMA before, now above)
        boolean emaSupportBounce = (prevClose < prevAverage) && (close > prevAverage);

        // Confirm Uptrend: Either price is above rising EMA or EMA bounce is confirmed
        return (priceAboveEMA && emaSlopeUp) || (emaSlopeUp && emaSupportBounce);
    }

    private boolean isShortestTermDownTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {

        if (timeframe == Timeframe.DAILY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    /**
     * EMA5, EMA10, EMA20
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermDownTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {

        if (timeframe == Timeframe.DAILY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    /**
     * EMA20, EMA50, EMA100
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermDownTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {

        if (timeframe == Timeframe.DAILY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    /**
     * EMA50, EMA100, EMA200
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermDownTrend(
            Timeframe timeframe, StockTechnicals stockTechnicals, StockPrice stockPrice) {

        if (timeframe == Timeframe.DAILY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage200(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage200(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isDownTrend(
                    MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals),
                    MovingAverageUtil.getPrevMovingAverage100(timeframe, stockTechnicals),
                    stockPrice.getClose(),
                    stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    private boolean isDownTrend(
            double average, double prevAverage, double close, double prevClose) {

        // Ensure previous EMA is valid
        if (prevAverage == 0.0) {
            return true; // Not enough data to determine trend, will return true by default
        }

        // Condition 1: Price Below EMA
        boolean priceBelowEMA = close < average;

        // Condition 2: Slope of EMA (EMA is falling)
        boolean emaSlopeDown = average < prevAverage;

        // Condition 3: EMA Resistance Bounce (Price was above EMA before, now below)
        boolean emaResistanceBounce = (prevClose > prevAverage) && (close < average);

        // Confirm Downtrend: Either price is below a falling EMA or price rejected from EMA.
        return (priceBelowEMA && emaSlopeDown) || (emaSlopeDown && emaResistanceBounce);
    }

    @Override
    public Trend detect(Stock stock, Timeframe timeframe) {

        Trend.Phase phase = Trend.Phase.SIDEWAYS;

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        if (stockTechnicals == null || stockPrice == null) {
            System.out.println("stockTechnicals or stockPrice not found");
            log.info("stockTechnicals or stockPrice not found");
            return new Trend(Trend.Direction.INVALID, Trend.Strength.INVALID, phase);
        }
        boolean shortestTermDown =
                this.isShortestTermDownTrend(timeframe, stockTechnicals, stockPrice);
        boolean shortestTermUp = this.isShortestTermUpTrend(timeframe, stockTechnicals, stockPrice);

        boolean shortTermDown = this.isShortTermDownTrend(timeframe, stockTechnicals, stockPrice);
        boolean shortTermUp = this.isShortTermUpTrend(timeframe, stockTechnicals, stockPrice);

        boolean mediumTermDown = this.isMediumTermDownTrend(timeframe, stockTechnicals, stockPrice);
        boolean mediumTermUp = this.isMediumTermUpTrend(timeframe, stockTechnicals, stockPrice);

        boolean longTermDown = this.isLongTermDownTrend(timeframe, stockTechnicals, stockPrice);
        boolean longTermUp = this.isLongTermUpTrend(timeframe, stockTechnicals, stockPrice);

        // log.info("timeframe:{}, shortestTermUp: {}, shortTermUp:{}, mediumTermUp:{},
        // longTermUp:{}",timeframe, shortestTermUp, shortestTermUp, mediumTermUp, longTermUp);
        // log.info("timeframe:{}, shortestTermDown: {}, shortTermDown:{}, mediumTermDown:{},
        // longTermDown:{}",timeframe, shortestTermDown, shortestTermDown, mediumTermDown,
        // longTermDown);
        // 1️⃣ DIP: Shortest-term down, but short-term (20 EMA) is flat or uncertain, while medium &
        // long-term up
        if (shortestTermDown && shortTermUp && mediumTermUp && longTermUp) {
            phase = Trend.Phase.DIP;
        }

        // 2️⃣ PULLBACK: Short-term downtrend (5 EMA & 20 EMA falling), but medium & long-term
        // uptrend
        if (shortestTermDown && shortTermDown && mediumTermUp && longTermUp) {
            phase = Trend.Phase.PULLBACK;
        }

        // 3️⃣ CORRECTION: Short-term & medium-term downtrend, but long-term uptrend
        if (shortTermDown && shortTermDown && mediumTermDown && longTermUp) {
            phase = Trend.Phase.CORRECTION;
        }

        // 4️⃣ BOTTOM: Strong downtrend across all timeframes (5, 20, 50, 200 EMA all bearish)
        if (shortestTermDown && shortTermDown && mediumTermDown && longTermDown) {
            phase = Trend.Phase.BOTTOM;
        }

        // 5️⃣ EARLY RECOVERY: 5 EMA is rising, but 20 EMA, 50 EMA & 200 EMA are still bearish
        if (shortestTermUp && shortTermDown && mediumTermDown && longTermDown) {
            phase = Trend.Phase.EARLY_RECOVERY;
        }

        // 6️⃣ RECOVERY: 5 EMA & 20 EMA rising, but 50 EMA & 200 EMA still bearish
        if (shortestTermUp && shortTermUp && mediumTermDown && longTermDown) {
            phase = Trend.Phase.RECOVERY;
        }

        // 7️⃣ ADVANCE (Reversal): Short-term & medium-term uptrend, but long-term still bearish
        if (shortestTermUp && shortTermUp && mediumTermUp && longTermDown) {
            phase = Trend.Phase.ADVANCE;
        }

        // 8️⃣ TOP: Short-term weakening while medium & long-term are still bullish
        if (shortestTermUp && shortTermUp && mediumTermUp && longTermUp) {
            phase = Trend.Phase.TOP;
        }

        if (phase == Trend.Phase.BOTTOM
                || phase == Trend.Phase.CORRECTION
                || phase == Trend.Phase.PULLBACK
                || phase == Trend.Phase.DIP) {
            if (longTermDown) {
                return new Trend(Trend.Direction.DOWN, Trend.Strength.LONG, phase);
            } else if (mediumTermDown) {
                return new Trend(Trend.Direction.DOWN, Trend.Strength.MEDIUM, phase);
            } else if (shortTermDown) {
                return new Trend(Trend.Direction.DOWN, Trend.Strength.SHORT, phase);
            } else if (shortestTermDown) {
                return new Trend(Trend.Direction.DOWN, Trend.Strength.WEAK, phase);
            }
            return new Trend(Trend.Direction.DOWN, Trend.Strength.WEAK, phase);
        } else if (phase == Trend.Phase.TOP
                || phase == Trend.Phase.ADVANCE
                || phase == Trend.Phase.RECOVERY
                || phase == Trend.Phase.EARLY_RECOVERY) {
            if (longTermUp) {
                return new Trend(Trend.Direction.UP, Trend.Strength.LONG, phase);
            } else if (mediumTermUp) {
                return new Trend(Trend.Direction.UP, Trend.Strength.MEDIUM, phase);
            } else if (shortTermUp) {
                return new Trend(Trend.Direction.UP, Trend.Strength.SHORT, phase);
            } else if (shortestTermUp) {
                return new Trend(Trend.Direction.UP, Trend.Strength.STRONG, phase);
            }

            return new Trend(Trend.Direction.UP, Trend.Strength.STRONG, phase);
        }

        // 7️⃣ Sideways: No clear trend (EMAs are flat or mixed)
        return new Trend(Trend.Direction.INVALID, Trend.Strength.INVALID, phase);
    }
}
