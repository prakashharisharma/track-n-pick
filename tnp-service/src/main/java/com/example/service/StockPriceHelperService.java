package com.example.service;

import static com.example.service.CandleStickService.MAX_WICK_SIZE;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class StockPriceHelperService {

    @Autowired private BreakoutLedgerService breakoutLedgerService;
    @Autowired private FormulaService formulaService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    /**
     * All 3 EMA increasing shorter greater than short short greater than average
     *
     * @param stock
     * @param trend
     * @return
     */
    public boolean isTmaDivergence(Stock stock, Timeframe timeframe, Trend trend) {
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        if (timeframe == Timeframe.DAILY) {
            return this.isTmaDivergence(
                    stockTechnicals.getEma20(),
                    stockTechnicals.getEma50(),
                    stockTechnicals.getEma200(),
                    stockTechnicals.getPrevEma20(),
                    stockTechnicals.getPrevEma50(),
                    stockTechnicals.getPrevEma200());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isTmaDivergence(
                    stockTechnicals.getEma10(),
                    stockTechnicals.getEma20(),
                    stockTechnicals.getEma50(),
                    stockTechnicals.getPrevEma10(),
                    stockTechnicals.getPrevEma20(),
                    stockTechnicals.getPrevEma50());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isTmaDivergence(
                    stockTechnicals.getEma5(),
                    stockTechnicals.getEma10(),
                    stockTechnicals.getEma20(),
                    stockTechnicals.getPrevEma5(),
                    stockTechnicals.getPrevEma10(),
                    stockTechnicals.getPrevEma20());
        }

        return false;
    }

    public boolean isTmaConvergence(Stock stock, Timeframe timeframe, Trend trend) {
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        if (timeframe == Timeframe.DAILY) {
            return this.isTmaConvergence(
                    stockTechnicals.getEma20(),
                    stockTechnicals.getEma50(),
                    stockTechnicals.getEma200(),
                    stockTechnicals.getPrevEma20(),
                    stockTechnicals.getPrevEma50(),
                    stockTechnicals.getPrevEma200());
        } else if (timeframe == Timeframe.WEEKLY) {
            return this.isTmaConvergence(
                    stockTechnicals.getEma10(),
                    stockTechnicals.getEma20(),
                    stockTechnicals.getEma50(),
                    stockTechnicals.getPrevEma10(),
                    stockTechnicals.getPrevEma20(),
                    stockTechnicals.getPrevEma50());
        } else if (timeframe == Timeframe.MONTHLY) {
            return this.isTmaConvergence(
                    stockTechnicals.getEma5(),
                    stockTechnicals.getEma10(),
                    stockTechnicals.getEma20(),
                    stockTechnicals.getPrevEma5(),
                    stockTechnicals.getPrevEma10(),
                    stockTechnicals.getPrevEma20());
        }

        return false;
    }

    private boolean isTmaDivergence(
            double immediateLow,
            double average,
            double immediateHigh,
            double prevImmediateLow,
            double prevAverage,
            double prevImmediateHigh) {

        if (immediateLow > average) {
            if (average > immediateHigh) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isTmaConvergence(
            double immediateLow,
            double average,
            double immediateHigh,
            double prevImmediateLow,
            double prevAverage,
            double prevImmediateHigh) {

        if (immediateLow < average) {
            if (average < immediateHigh) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isTmaInAverageRange(double immediateLow, double average, double immediateHigh) {
        double averageTMA = this.averageTMA(immediateLow, average, immediateHigh);
        double avgLowerRange =
                formulaService.applyPercentChange(averageTMA, -1 * this.maxTmaRange());
        double avgHigherRange =
                formulaService.applyPercentChange(averageTMA, 1 * this.maxTmaRange());
        if (formulaService.inRange(
                Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateLow)) {
            if (formulaService.inRange(
                    Math.floor(avgLowerRange), Math.ceil(avgHigherRange), average)) {
                if (formulaService.inRange(
                        Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateHigh)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    private boolean isTmaInPriceRange(
            double low, double high, double immediateLow, double average, double immediateHigh) {

        double avgLowerRange = formulaService.applyPercentChange(low, -1 * this.maxTmaRange());
        double avgHigherRange = formulaService.applyPercentChange(high, 1 * this.maxTmaRange());
        if (formulaService.inRange(
                Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateLow)) {
            if (formulaService.inRange(
                    Math.floor(avgLowerRange), Math.ceil(avgHigherRange), average)) {
                if (formulaService.inRange(
                        Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateHigh)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    private double averageTMA(double immediateLow, double average, double immediateHigh) {
        return (immediateLow + average + immediateHigh) / 3;
    }

    public boolean isCloseAboveEma(Stock stock, Timeframe timeframe) {
        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (timeframe == Timeframe.DAILY) {
            return CrossOverUtil.isFastCrossesAboveSlow(
                    stockPrice.getPrevClose(),
                    stockTechnicals.getPrevEma20(),
                    stockPrice.getClose(),
                    stockTechnicals.getEma20());
        } else if (timeframe == Timeframe.WEEKLY) {
            return CrossOverUtil.isFastCrossesAboveSlow(
                    stockPrice.getPrevClose(),
                    stockTechnicals.getPrevEma10(),
                    stockPrice.getClose(),
                    stockTechnicals.getEma10());
        }
        if (timeframe == Timeframe.MONTHLY) {
            return CrossOverUtil.isFastCrossesAboveSlow(
                    stockPrice.getPrevClose(),
                    stockTechnicals.getPrevEma5(),
                    stockPrice.getClose(),
                    stockTechnicals.getEma5());
        }
        return false;
    }

    public boolean isCloseBelowEma(Stock stock, Timeframe timeframe) {
        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (timeframe == Timeframe.DAILY) {
            return CrossOverUtil.isSlowCrossesBelowFast(
                    stockPrice.getPrevClose(),
                    stockTechnicals.getPrevEma20(),
                    stockPrice.getClose(),
                    stockTechnicals.getEma20());
        } else if (timeframe == Timeframe.WEEKLY) {
            return CrossOverUtil.isSlowCrossesBelowFast(
                    stockPrice.getPrevClose(),
                    stockTechnicals.getPrevEma10(),
                    stockPrice.getClose(),
                    stockTechnicals.getEma10());
        }
        if (timeframe == Timeframe.MONTHLY) {
            return CrossOverUtil.isSlowCrossesBelowFast(
                    stockPrice.getPrevClose(),
                    stockTechnicals.getPrevEma5(),
                    stockPrice.getClose(),
                    stockTechnicals.getEma5());
        }
        return false;
    }

    private double maxTmaRange() {
        return formulaService.applyPercentChange(MAX_WICK_SIZE, FibonacciRatio.RATIO_161_8 * 100);
    }

    public boolean isAboveEma20(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return stockPrice.getClose() > stockTechnicals.getEma20() ? true : false;
    }

    public boolean isAboveEma50(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return stockPrice.getClose() > stockTechnicals.getEma50() ? true : false;
    }

    public boolean isBelowEma20(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return stockPrice.getClose() < stockTechnicals.getEma20() ? true : false;
    }

    public boolean isBelowEma50(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return stockPrice.getClose() < stockTechnicals.getEma50() ? true : false;
    }
}
