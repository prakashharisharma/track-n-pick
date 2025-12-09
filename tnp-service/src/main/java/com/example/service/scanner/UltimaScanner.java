package com.example.service.scanner;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.io.StockAnalysis;
import com.example.service.CalendarService;
import com.example.service.UpdatePriceService;
import com.example.service.UpdateTechnicalsService;
import com.example.service.scanner.validator.DailyEntryValidator;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UltimaScanner {

    private final MiscUtil miscUtil;
    private final CalendarService calendarService;

    private final FormulaService formulaService;

    private final DailyEntryValidator dailyEntryValidator;

    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;

    public boolean isValid(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double close = stockPrice.getClose();

        double ema5Weighted = formulaService.applyPercentChange(ema5, 2.0);
        double ema5WeightedNegative = formulaService.applyPercentChange(ema5, -2.0);

        double ema20Weighted = formulaService.applyPercentChange(ema20, 2.0);
        double ema20WeightedNegative = formulaService.applyPercentChange(ema20, -2.0);

        double ema50Weighted = formulaService.applyPercentChange(ema50, 2.0);
        double ema50WeightedNegative = formulaService.applyPercentChange(ema50, -2.0);

        boolean isLowRejectedEma5 =
                (open > ema5 && low <= ema5Weighted && close > ema5WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma20 =
                (open > ema20 && low <= ema20Weighted && close > ema20WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma50 =
                (open > ema50 && low <= ema50Weighted && close > ema50WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);
        boolean isEma5Above20 = ema5 > ema20;
        boolean isEma20Above50 = ema20 > ema50;
        boolean isEma50Above200 = ema50 > ema200;
        boolean isEma20And50Increasing = ema20 > prevEma20 && ema50 > prevEma50;
        boolean isEma5And20Increasing = ema5 > prevEma5 && ema20 > prevEma20;
        boolean isEma50And200Increasing = ema50 > prevEma50 && ema200 > prevEma200;
        boolean isUpperWick2xLowerWick =
                CandleStickUtils.upperWickSize(stockPrice)
                        >= 2 * CandleStickUtils.lowerWickSize(stockPrice);

        if (!isUpperWick2xLowerWick) {
            // Monthly Align Bullish
            if ((isLowRejectedEma5 && isEma5Above20 && isEma5And20Increasing)
                    || (isLowRejectedEma20 && isEma20Above50 && isEma20And50Increasing)
                    || (isLowRejectedEma50 && isEma50Above200 && isEma50And200Increasing)) {

                if (stockTechnicals.getVolume() < stockTechnicals.getVolumeAvg20()) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<StockAnalysis> scan(
            StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate currentDate) {

        Stock stock = stockPrice.getStock();
        //  List<Stock> stocks = stockService.getForActivity();
        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double close = stockPrice.getClose();

        double ema5Weighted = formulaService.applyPercentChange(ema5, 2.0);
        double ema5WeightedNegative = formulaService.applyPercentChange(ema5, -2.0);

        double ema20Weighted = formulaService.applyPercentChange(ema20, 2.0);
        double ema20WeightedNegative = formulaService.applyPercentChange(ema20, -2.0);

        double ema50Weighted = formulaService.applyPercentChange(ema50, 2.0);
        double ema50WeightedNegative = formulaService.applyPercentChange(ema50, -2.0);

        boolean isLowRejectedEma5 =
                (open > ema5 && low <= ema5Weighted && close > ema5WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma20 =
                (open > ema20 && low <= ema20Weighted && close > ema20WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma50 =
                (open > ema50 && low <= ema50Weighted && close > ema50WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);
        boolean isEma5Above20 = ema5 > ema20;
        boolean isEma20Above50 = ema20 > ema50;
        boolean isEma50Above200 = ema50 > ema200;
        boolean isEma20And50Increasing = ema20 > prevEma20 && ema50 > prevEma50;
        boolean isEma5And20Increasing = ema5 > prevEma5 && ema20 > prevEma20;
        boolean isEma50And200Increasing = ema50 > prevEma50 && ema200 > prevEma200;
        boolean isUpperWick2xLowerWick =
                CandleStickUtils.upperWickSize(stockPrice)
                        >= 2 * CandleStickUtils.lowerWickSize(stockPrice);

        if (!isUpperWick2xLowerWick) {
            // Monthly Align Bullish
            if ((isLowRejectedEma5 && isEma5Above20 && isEma5And20Increasing)
                    || (isLowRejectedEma20 && isEma20Above50 && isEma20And50Increasing)
                    || (isLowRejectedEma50 && isEma50Above200 && isEma50And200Increasing)) {

                if (stockTechnicals.getVolume() < stockTechnicals.getVolumeAvg20()) {
                    LocalDate previousMonthLastDay =
                            calendarService.previousTradingSession(currentDate.withDayOfMonth(1));
                    LocalDate currentMonthFirstSession =
                            calendarService.nextTradingSession(previousMonthLastDay);

                    LocalDate currentMonthSecondSession =
                            calendarService.nextTradingSession(currentMonthFirstSession);

                    LocalDate currentMonthThirdSession =
                            calendarService.nextTradingSession(currentMonthSecondSession);
                    LocalDate sessionDate = currentMonthFirstSession;
                    LocalDate sessionDateTill = currentMonthThirdSession;

                    while (sessionDate.isBefore(sessionDateTill)) {

                        // StockPrice stockPriceDaily = getStockPriceFromMap(Timeframe.DAILY, stock,
                        // sessionDate);
                        StockPrice stockPriceDaily =
                                updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);

                        if (CandleStickUtils.isHigherHigh(stockPriceDaily)
                                && CandleStickUtils.isHigherLow(stockPriceDaily)) {

                            if (stockPriceDaily.getClose() > stockPrice.getClose()) {

                                // StockTechnicals stockTechnicalsDaily =
                                // getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);
                                StockTechnicals stockTechnicalsDaily =
                                        updateTechnicalsService.buildBack(
                                                Timeframe.DAILY, stock, sessionDate);
                                Optional<StockAnalysis> stockAnalysisOptional =
                                        dailyEntryValidator.isValid2(
                                                stockPrice,
                                                stockPriceDaily,
                                                stockTechnicals,
                                                stockTechnicalsDaily,
                                                sessionDate,
                                                ResearchTechnical.Strategy.ULTIMA);

                                if (stockAnalysisOptional.isPresent()) {
                                    stockAnalysed.add(stockAnalysisOptional.get());
                                    break;
                                }
                            }
                        }

                        sessionDate = calendarService.nextTradingSession(sessionDate);
                    }
                }
                //    }
                //  }
            }
        }

        return stockAnalysed;
    }

    public List<StockAnalysis> scanLastDay(
            StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate currentDate) {

        Stock stock = stockPrice.getStock();
        //  List<Stock> stocks = stockService.getForActivity();
        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        LocalDate sessionDate =
                calendarService.previousTradingSession(currentDate.withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(currentDate)
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = currentDate;
        }
        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double close = stockPrice.getClose();

        double ema5Weighted = formulaService.applyPercentChange(ema5, 2.0);
        double ema5WeightedNegative = formulaService.applyPercentChange(ema5, -2.0);

        double ema20Weighted = formulaService.applyPercentChange(ema20, 2.0);
        double ema20WeightedNegative = formulaService.applyPercentChange(ema20, -2.0);

        double ema50Weighted = formulaService.applyPercentChange(ema50, 2.0);
        double ema50WeightedNegative = formulaService.applyPercentChange(ema50, -2.0);

        boolean isLowRejectedEma5 =
                (open > ema5 && low <= ema5Weighted && close > ema5WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma20 =
                (open > ema20 && low <= ema20Weighted && close > ema20WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma50 =
                (open > ema50 && low <= ema50Weighted && close > ema50WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);
        boolean isEma5Above20 = ema5 > ema20;
        boolean isEma20Above50 = ema20 > ema50;
        boolean isEma50Above200 = ema50 > ema200;
        boolean isEma20And50Increasing = ema20 > prevEma20 && ema50 > prevEma50;
        boolean isEma5And20Increasing = ema5 > prevEma5 && ema20 > prevEma20;
        boolean isEma50And200Increasing = ema50 > prevEma50 && ema200 > prevEma200;
        boolean isUpperWick2xLowerWick =
                CandleStickUtils.upperWickSize(stockPrice)
                        >= 2 * CandleStickUtils.lowerWickSize(stockPrice);

        if (!isUpperWick2xLowerWick) {
            // Monthly Align Bullish
            if ((isLowRejectedEma5 && isEma5Above20 && isEma5And20Increasing)
                    || (isLowRejectedEma20 && isEma20Above50 && isEma20And50Increasing)
                    || (isLowRejectedEma50 && isEma50Above200 && isEma50And200Increasing)) {

                if (stockTechnicals.getVolume() < stockTechnicals.getVolumeAvg20()) {
                    // Daily check

                    // StockPrice stockPriceDaily = getStockPriceFromMap(Timeframe.DAILY, stock,
                    // sessionDate);
                    StockPrice stockPriceDaily =
                            updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);

                    // StockTechnicals stockTechnicalsDaily =
                    // getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);
                    StockTechnicals stockTechnicalsDaily =
                            updateTechnicalsService.buildBack(Timeframe.DAILY, stock, sessionDate);

                    Optional<StockAnalysis> stockAnalysisOptional =
                            dailyEntryValidator.isValid2(
                                    stockPrice,
                                    stockPriceDaily,
                                    stockTechnicals,
                                    stockTechnicalsDaily,
                                    sessionDate,
                                    ResearchTechnical.Strategy.ULTIMA);
                    if (stockAnalysisOptional.isPresent()) {
                        stockAnalysed.add(stockAnalysisOptional.get());
                    }
                }
            }
            // }
            // }
        }

        return stockAnalysed;
    }
}
