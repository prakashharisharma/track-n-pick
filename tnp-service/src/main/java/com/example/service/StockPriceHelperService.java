package com.example.service;

import static com.example.service.CandleStickService.MAX_WICK_SIZE;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.util.Arrays;
import java.util.Objects;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class StockPriceHelperService {

    @Autowired private BreakoutLedgerService breakoutLedgerService;
    @Autowired private FormulaService formulaService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private MiscUtil miscUtil;

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
        return stockPrice != null
                && stockTechnicals != null
                && stockPrice.getClose() != null
                && stockTechnicals.getEma20() != null
                && stockPrice.getClose() > stockTechnicals.getEma20();
    }

    public boolean isAboveEma50(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return stockPrice != null
                && stockTechnicals != null
                && stockPrice.getClose() != null
                && stockTechnicals.getEma50() != null
                && stockPrice.getClose() > stockTechnicals.getEma50();
    }

    public boolean isBelowEma20(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return stockPrice != null
                && stockTechnicals != null
                && stockPrice.getClose() != null
                && stockTechnicals.getEma20() != null
                && stockPrice.getClose() < stockTechnicals.getEma20();
    }

    public boolean isBelowEma50(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        return stockPrice != null
                && stockTechnicals != null
                && stockPrice.getClose() != null
                && stockTechnicals.getEma50() != null
                && stockPrice.getClose() < stockTechnicals.getEma50();
    }

    public boolean isHigherTimeFrameHighBreakout(Timeframe timeframe, StockPrice stockPrice) {
        if (stockPrice == null || timeframe == null || stockPrice.getStock() == null) {
            return false;
        }

        StockPrice higherTimeframePrice =
                stockPriceService.get(stockPrice.getStock(), timeframe.getHigher());

        if (higherTimeframePrice == null) {
            return false; // Ensure the higher timeframe price exists
        }

        if (higherTimeframePrice.getSessionDate() != null
                && higherTimeframePrice.getSessionDate().isBefore(miscUtil.currentDate())) {
            return stockPrice.getClose() > higherTimeframePrice.getHigh();
        }

        return stockPrice.getClose() > higherTimeframePrice.getPrevHigh();
    }

    public boolean isHigher2TimeFrameHighBreakout(Timeframe timeframe, StockPrice stockPrice) {
        if (stockPrice == null || timeframe == null || stockPrice.getStock() == null) {
            return false;
        }

        StockPrice higherTimeframePrice =
                stockPriceService.get(stockPrice.getStock(), timeframe.getHigher().getHigher());

        if (higherTimeframePrice == null) {
            return false; // Ensure the higher timeframe price exists
        }

        if (higherTimeframePrice.getSessionDate() != null
                && higherTimeframePrice.getSessionDate().isBefore(miscUtil.currentDate())) {
            return stockPrice.getClose() > higherTimeframePrice.getHigh();
        }

        return stockPrice.getClose() > higherTimeframePrice.getPrevHigh();
    }

    public boolean isHigherTimeFrameHighBreakdown(Timeframe timeframe, StockPrice stockPrice) {
        if (stockPrice == null || timeframe == null || stockPrice.getStock() == null) {
            return false;
        }

        StockPrice higherTimeframePrice =
                stockPriceService.get(stockPrice.getStock(), timeframe.getHigher());

        if (higherTimeframePrice == null) {
            return false; // Ensure the higher timeframe price exists
        }

        if (higherTimeframePrice.getSessionDate() != null
                && higherTimeframePrice.getSessionDate().isBefore(miscUtil.currentDate())) {
            return stockPrice.getClose() < higherTimeframePrice.getLow();
        }

        return stockPrice.getClose() < higherTimeframePrice.getPrevLow();
    }

    public boolean isHigher2TimeFrameHighBreakdown(Timeframe timeframe, StockPrice stockPrice) {
        if (stockPrice == null || timeframe == null || stockPrice.getStock() == null) {
            return false;
        }

        StockPrice higherTimeframePrice =
                stockPriceService.get(stockPrice.getStock(), timeframe.getHigher().getHigher());

        if (higherTimeframePrice == null) {
            return false; // Ensure the higher timeframe price exists
        }

        if (higherTimeframePrice.getSessionDate() != null
                && higherTimeframePrice.getSessionDate().isBefore(miscUtil.currentDate())) {
            return stockPrice.getClose() < higherTimeframePrice.getLow();
        }

        return stockPrice.getClose() < higherTimeframePrice.getPrevLow();
    }

    public Double findLowestLow(StockPrice stockPrice) {
        if (stockPrice == null) {
            return null;
        }

        // Get the lows from the current and previous 4 sessions
        Double[] lows = {
            stockPrice.getLow(),
            stockPrice.getPrevLow(),
            stockPrice.getPrev2Low(),
            stockPrice.getPrev3Low(),
            stockPrice.getPrev4Low()
        };

        // Find the lowest low from the array, defaulting to the current low if no valid lows are
        // found
        return Arrays.stream(lows)
                .filter(Objects::nonNull) // Filter out null values (in case any of the lows are
                // null)
                .min(Double::compareTo) // Find the minimum low value
                .orElse(stockPrice.getLow()); // Default to current low if no valid lows are found
    }

    public Double findHighestHigh(StockPrice stockPrice) {
        if (stockPrice == null) {
            return null;
        }

        // Get the highs from the current and previous 4 sessions
        Double[] highs = {
            stockPrice.getHigh(),
            stockPrice.getPrevHigh(),
            stockPrice.getPrev2High(),
            stockPrice.getPrev3High(),
            stockPrice.getPrev4High()
        };

        // Find the highest high from the array, defaulting to the current high if no valid highs
        // are found
        return Arrays.stream(highs)
                .filter(Objects::nonNull) // Filter out null values (in case any of the highs are
                // null)
                .max(Double::compareTo) // Find the maximum high value
                .orElse(
                        stockPrice
                                .getHigh()); // Default to current high if no valid highs are found
    }

    public StockPrice buildPrevSessionStockPrice(StockPrice stockPrice){

        stockPrice.setOpen(stockPrice.getPrevOpen());
        stockPrice.setHigh(stockPrice.getPrevHigh());
        stockPrice.setLow(stockPrice.getPrevLow());
        stockPrice.setClose(stockPrice.getPrevClose());

        stockPrice.setPrevOpen(stockPrice.getPrev2Open());
        stockPrice.setPrevHigh(stockPrice.getPrev2High());
        stockPrice.setPrevLow(stockPrice.getPrev2Low());
        stockPrice.setPrevClose(stockPrice.getPrev2Close());

        stockPrice.setPrev2Open(stockPrice.getPrev3Open());
        stockPrice.setPrev2High(stockPrice.getPrev3High());
        stockPrice.setPrev2Low(stockPrice.getPrev3Low());
        stockPrice.setPrev2Close(stockPrice.getPrev3Close());

        stockPrice.setPrev3Open(stockPrice.getPrev4Open());
        stockPrice.setPrev3High(stockPrice.getPrev4High());
        stockPrice.setPrev3Low(stockPrice.getPrev4Low());
        stockPrice.setPrev3Close(stockPrice.getPrev4Close());

        stockPrice.setPrev4Open(stockPrice.getPrev5Open());
        stockPrice.setPrev4High(stockPrice.getPrev5High());
        stockPrice.setPrev4Low(stockPrice.getPrev5Low());
        stockPrice.setPrev4Close(stockPrice.getPrev5Close());

        stockPrice.setPrev5Open(stockPrice.getPrev6Open());
        stockPrice.setPrev5High(stockPrice.getPrev6High());
        stockPrice.setPrev5Low(stockPrice.getPrev6Low());
        stockPrice.setPrev5Close(stockPrice.getPrev6Close());

        return  stockPrice;
    }
}
