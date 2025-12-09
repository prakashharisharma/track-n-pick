package com.example.service.scanner;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.io.StockAnalysis;
import com.example.service.CalendarService;
import com.example.service.UpdatePriceService;
import com.example.service.UpdateTechnicalsService;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.scanner.validator.DailyEntryValidator;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmegaScanner {
    private final FundamentalResearchService fundamentalResearchService;

    private final MiscUtil miscUtil;
    private final CalendarService calendarService;

    private final FormulaService formulaService;

    private final DailyEntryValidator dailyEntryValidator;

    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;

    public boolean test(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        // Stock stock = stockPrice.getStock();

        double low = stockPrice.getLow();
        double prevLow = stockPrice.getPrevLow();
        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        double open = stockPrice.getOpen();
        double prevOpen = stockPrice.getPrevOpen();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();

        double prevEma5 =
                stockTechnicals.getPrevEma5() != null ? stockTechnicals.getPrevEma5() : 0.0;
        double prevEma20 =
                stockTechnicals.getPrevEma20() != null ? stockTechnicals.getPrevEma20() : 0.0;
        double prevEma50 =
                stockTechnicals.getPrevEma50() != null ? stockTechnicals.getPrevEma50() : 0.0;
        double rsi = stockTechnicals.getRsi();
        long volume = stockTechnicals.getVolume() != null ? stockTechnicals.getVolume() : 0;
        long prevVolume =
                stockTechnicals.getPrevVolume() != null ? stockTechnicals.getPrevVolume() : 0;

        /** 1. Close above prev High */
        MarketCapCategory marketCapCategory =
                MarketCapCategory.classify(fundamentalResearchService.marketCap(stockPrice));
        if ((marketCapCategory != MarketCapCategory.MICROCAP)) {
            double changePct =
                    formulaService.calculateChangePercentage(
                            stockPrice.getPrevClose(), stockPrice.getClose());

            boolean isEma5Incr = ema5 > prevEma5;
            boolean isEma20Incr = ema20 > prevEma20;
            boolean isEma50Incr = ema50 > prevEma50;

            boolean isEma5LowRejected = ema5 > 0.0 && low < ema5 && close > ema5;
            boolean isEma20LowRejected = ema20 > 0.0 && low < ema20 && close > ema20;
            boolean isEma50LowRejected = ema50 > 0.0 && low < ema50 && close > ema50;

            boolean isLowRejected = isEma5LowRejected || isEma20LowRejected || isEma50LowRejected;

            boolean isPrevEma5LowRejected =
                    prevEma5 > 0.0 && prevLow < prevEma5 && prevClose > prevEma5;
            boolean isPrevEma20LowRejected =
                    prevEma20 > 0.0 && prevLow < prevEma20 && prevClose > prevEma20;
            boolean isPrevEma50LowRejected =
                    prevEma50 > 0.0 && prevLow < prevEma50 && prevClose > prevEma50;

            boolean isPrevLowRejected =
                    isPrevEma5LowRejected || isPrevEma20LowRejected || isPrevEma50LowRejected;

            boolean isEma5Breakout = prevClose < ema5 && close > ema5;
            boolean isEma20Breakout = prevClose < ema20 && close > ema20;
            boolean isEma50Breakout = prevClose < ema50 && close > ema50;

            boolean isBreakout = isEma5Breakout || isEma20Breakout || isEma50Breakout;

            boolean isPrevEma5Breakout = prev2Close < prevEma5 && prevClose > prevEma5;
            boolean isPrevEma20Breakout = prev2Close < prevEma20 && prevClose > prevEma20;
            boolean isPrevEma50Breakout = prev2Close < prevEma50 && prevClose > prevEma50;

            boolean isPrevBreakout =
                    isPrevEma5Breakout || isPrevEma20Breakout || isPrevEma50Breakout;

            boolean isAllIncr =
                    (isEma5Incr && isEma20Incr && isEma50Incr)
                            || (isEma5Incr
                                    && isEma50Incr
                                    && (isEma20LowRejected || isEma20Breakout))
                            || (isEma20Incr && isEma50Incr && (isEma5LowRejected || isEma5Breakout))
                            || (isEma5Incr
                                    && isEma20Incr
                                    && (isEma50LowRejected || isEma50Breakout));

            if (ema5 > ema20 && ema20 > ema50) {
                if (isEma5Incr && isEma20Incr && isEma50Incr) {

                    boolean condition1 =
                            close > prevClose
                                    && CandleStickUtils.isGreen(stockPrice)
                                    && CandleStickUtils.isPrevSessionGreen(stockPrice);
                    boolean condition2 = close > prevOpen && isEma5LowRejected;
                    boolean condition3 =
                            CandleStickUtils.isHigherHigh(stockPrice) && isEma5LowRejected;
                    if (condition1 || condition2 || condition3) {
                        boolean isEmaSatisfied = ema20 > 0.0 && ema50 > 0.0;

                        boolean rsiFilter = false;

                        if (rsi < 60) {
                            rsiFilter = changePct < 30;
                        } else if (rsi < 65) {
                            rsiFilter = changePct < 20;
                        } else if (rsi < 75) {
                            rsiFilter = changePct < 18;
                        } else {
                            rsiFilter = false;
                        }

                        boolean isVolumeNotOverExtended =
                                (volume / prevVolume) < 5.0
                                        && ((volume / prevVolume) > 1.0
                                                || stockTechnicals.getVolumeAvg10()
                                                        > stockTechnicals.getPrevVolumeAvg10());

                        boolean isWeakVolume =
                                (volume < prevVolume
                                        && stockTechnicals.getVolumeAvg10()
                                                < stockTechnicals.getPrevVolumeAvg10());

                        if (changePct < 12.0 && !isWeakVolume && isVolumeNotOverExtended) {
                            boolean isPrevDoji =
                                    CandleStickUtils.isPrevVerySmallBody(stockPrice)
                                            && prevClose > prevEma5;

                            boolean isDojiConfirmed =
                                    isPrevDoji
                                            && !CandleStickUtils.isVerySmallBody(stockPrice)
                                            && CandleStickUtils.isRed(stockPrice);

                            boolean isDoji =
                                    CandleStickUtils.isVerySmallBody(stockPrice) && close > ema5;

                            if (!isDojiConfirmed && !isDoji) {

                                double prevEmaDiff = prevEma5 - prevEma20;
                                double emaDiff = ema5 - ema20;
                                boolean isEmaDiffWidening = emaDiff > prevEmaDiff;

                                if (isEmaDiffWidening) {
                                    if (this.isUpperWickLessThan40Percent(
                                            open, stockPrice.getHigh(), low, close)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isUpperWickLessThan40Percent(
            double open, double high, double low, double close) {
        double upperWick = high - Math.max(open, close);
        double range = high - low;

        // Avoid division by zero
        if (range == 0) {
            return true; // or false, depending on your requirement
        }

        double upperWickPercentage = (upperWick / range) * 100;
        return upperWickPercentage < 40;
    }

    public List<StockAnalysis> scan(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        double high = stockPrice.getHigh();
        double low = stockPrice.getLow();
        double close = stockPrice.getClose();
        double prevLevel =
                CandleStickUtils.isPrevSessionGreen(stockPrice)
                        ? stockPrice.getPrevClose()
                        : stockPrice.getPrevOpen();
        prevLevel = formulaService.applyPercentChange(prevLevel, 1.0);
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();

        boolean isAboveEma50or20Close =
                (ema50 != 0.0 && close > ema50) || (ema50 == 0.0 && ema20 != 0.0 && close > ema20);

        if (isAboveEma50or20Close
                && close > prevLevel
                && CandleStickUtils.isHigherHigh(stockPrice)
                && CandleStickUtils.isHigherLow(stockPrice)) {

            if (!CandleStickUtils.isPrevVerySmallBody(stockPrice)) {

                double changePer =
                        formulaService.calculateChangePercentage(
                                stockPrice.getPrevClose(), stockPrice.getClose());

                if (changePer < 12.0 && changePer > 0.0) {

                    if ((ema50 != 0.0 && ema20 > ema50) || (ema20 != 0.0 && ema5 > ema20)) {

                        long volumeAvg = stockTechnicals.getVolumeAvg10();
                        long prevVolumeAvg = stockTechnicals.getPrevVolumeAvg10();
                        long volume = stockTechnicals.getVolume();
                        long prevVolume = stockTechnicals.getPrevVolume();

                        boolean isVolumeIndicator =
                                (volume > volumeAvg * 1.5)
                                        || (volume > prevVolume * 1.5)
                                        || (volume > prevVolume && volumeAvg > prevVolumeAvg);
                        if (isVolumeIndicator) {
                            boolean isOnTop = ema5 > ema20 && close > ema5 && low > ema5;
                            boolean heavyVolumeOnTop =
                                    isOnTop
                                            && ((volume > volumeAvg * 1.5)
                                                    || (volume > prevVolume * 1.5));

                            double ema5DistanceToLow =
                                    formulaService.calculateChangePercentage(ema5, low);
                            double ema5DistanceToHigh =
                                    formulaService.calculateChangePercentage(ema5, high);
                            if (!heavyVolumeOnTop
                                    && ema5DistanceToLow < 2.0
                                    && ema5DistanceToHigh < 20.0) {

                                double ema5And20Diff = ema5 - ema20;
                                double prevEma5And20Diff =
                                        stockTechnicals.getPrevEma5()
                                                - stockTechnicals.getPrevEma20();

                                if (ema5And20Diff > prevEma5And20Diff) {
                                    // Daily check
                                    LocalDate currentMonthFirstSession =
                                            calendarService.nextTradingSession(
                                                    miscUtil.previousMonthLastDay());

                                    LocalDate currentMonthSecondSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthFirstSession);

                                    LocalDate currentMonthThirdSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthSecondSession);
                                    LocalDate sessionDate = currentMonthFirstSession;
                                    LocalDate sessionDateTill = currentMonthSecondSession;
                                    LocalDate ohlcvFrom = currentMonthFirstSession;

                                    while (sessionDate.isBefore(sessionDateTill)) {

                                        StockPrice stockPriceDaily =
                                                updatePriceService.buildBack(
                                                        Timeframe.DAILY, stock, sessionDate);
                                        // getStockPriceFromMap(Timeframe.DAILY, stock,
                                        // sessionDate);

                                        StockTechnicals stockTechnicalsDaily =
                                                updateTechnicalsService.buildBack(
                                                        Timeframe.DAILY, stock, sessionDate);
                                        // getStockTechnicalsFromMap(Timeframe.DAILY, stock,
                                        // sessionDate);

                                        Optional<StockAnalysis> stockAnalysisOptional =
                                                dailyEntryValidator.isValid(
                                                        stockPrice,
                                                        stockPriceDaily,
                                                        stockTechnicals,
                                                        stockTechnicalsDaily,
                                                        sessionDate,
                                                        ResearchTechnical.Strategy.OMEGA);

                                        if (stockAnalysisOptional.isPresent()) {
                                            stockAnalysed.add(stockAnalysisOptional.get());
                                            break;
                                        }
                                        // }
                                        // }

                                        sessionDate =
                                                calendarService.nextTradingSession(sessionDate);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return stockAnalysed;
    }
}
