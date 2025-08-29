package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.Trade;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DhanOrderSchedulerHelperService {

    private final FormulaService formulaService;
    private final ResearchTechnicalService<ResearchTechnical> researchTechnicalService;
    private final CalendarService calendarService;
    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private boolean validateBasicResearch(
            ResearchTechnical rt,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean isInvestment) {
        if (stockPrice == null || stockPrice.getClose() == null || stockPrice.getLow() == null)
            return false;
        if (rt.getResearchDate() == null) return false;

        double ma5 =
                MovingAverageUtil.getMovingAverage5(stockPrice.getTimeframe(), stockTechnicals);
        double close = stockPrice.getClose();
        double entryPrice = rt.getEntryPrice();
        if (isInvestment) {
            // Investment: MA5 must be <= close OR entryPrice < ma5
            if (!(close < ma5 || entryPrice > ma5)) return false;
        } else {
            // Not investment: either close > ma5 OR entryPrice > ma5
            if (!(close > ma5 || entryPrice < ma5)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateResearchDate(ResearchTechnical rt, LocalDate sessionDate, int maxDays) {
        long daysBetween =
                java.time.temporal.ChronoUnit.DAYS.between(rt.getResearchDate(), sessionDate);
        return daysBetween > 0 && daysBetween <= maxDays;
    }

    private void adjustPriceAndRisk(
            ResearchTechnical rt, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Double originalEntry = rt.getEntryPrice();
        Double dayLow = stockPrice.getLow();
        Double close = stockPrice.getClose();
        Double open = stockPrice.getOpen();
        double newEntryPrice = originalEntry;
        if (dayLow < originalEntry) {

            newEntryPrice = dayLow;

            if (close < originalEntry) {
                newEntryPrice = (dayLow + Math.min(close, open)) / 2;
            }

        } else if (close > originalEntry && open > originalEntry && dayLow > originalEntry) {
            newEntryPrice = (dayLow + originalEntry) / 2;
        }

        newEntryPrice = formulaService.ceilToNearestTick(newEntryPrice, rt.getTickSize());

        rt.setEntryPrice(newEntryPrice);

        rt.setRisk(
                Math.abs(
                        formulaService.calculateChangePercentage(
                                rt.getEntryPrice(), rt.getStopLoss())));
    }

    private List<ResearchTechnical> filterAndProcessResearches(
            List<ResearchTechnical> researchTechnicals,
            LocalDate sessionDate,
            int maxDays,
            boolean isInvestment) {
        return researchTechnicals.stream()
                .filter(rt -> rt.getEntryPrice() != null && rt.getStock() != null)
                .filter(rt -> rt.getVolumeScore() == 0.75 || rt.getScore() >= 8.5)
                .filter(
                        rt -> {
                            StockPrice stockPrice =
                                    stockPriceService.get(rt.getStock(), Timeframe.DAILY);
                            StockTechnicals stockTechnicals =
                                    stockTechnicalsService.get(rt.getStock(), Timeframe.DAILY);

                            if (!validateBasicResearch(
                                    rt, stockPrice, stockTechnicals, isInvestment)) return false;
                            if (!validateResearchDate(rt, sessionDate, maxDays)) return false;

                            adjustPriceAndRisk(rt, stockPrice, stockTechnicals);
                            if (rt.getStopLoss() >= rt.getEntryPrice()) {
                                return false;
                            }
                            return true;
                        })
                .collect(Collectors.toList());
    }

    public List<ResearchTechnical> getPreviousInvestmentResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getLatestInvestmentBuyResearch(sessionDate),
                sessionDate,
                30,
                true);
    }

    public List<ResearchTechnical> getRecentHybridResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentHybridBuyResearch(sessionDate),
                sessionDate,
                10,
                false);
    }

    public List<ResearchTechnical> getRecentDynamicResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentDynamicBuyResearch(sessionDate),
                sessionDate,
                10,
                false);
    }

    public List<ResearchTechnical> getNearTargetResearches(LocalDate currentDate) {

        List<ResearchTechnical> researchTechnicals =
                researchTechnicalService.getAll(Trade.Type.BUY);

        return researchTechnicals.stream()
                .filter(rt -> rt.getTarget() != null && rt.getStock() != null)
                .filter(
                        rt -> {
                            StockPrice stockPrice =
                                    stockPriceService.get(rt.getStock(), Timeframe.DAILY);
                            if (stockPrice == null || stockPrice.getClose() == null) return false;

                            double close = stockPrice.getClose();
                            double entryPrice = rt.getEntryPrice();
                            double target = rt.getTarget();

                            boolean isWithinPriceBand =
                                    formulaService.isWithinPercentage(
                                            close, target, rt.getPriceBand());

                            // Check research date conditions
                            LocalDate researchDate = rt.getResearchDate();

                            if (researchDate != null) {
                                long daysBetween =
                                        java.time.temporal.ChronoUnit.DAYS.between(
                                                researchDate, currentDate);

                                if (calendarService
                                                .previousTradingSession(currentDate)
                                                .getDayOfWeek()
                                        == DayOfWeek.FRIDAY) {}

                                if ((calendarService
                                                                .previousTradingSession(currentDate)
                                                                .getDayOfWeek()
                                                        == DayOfWeek.FRIDAY
                                                || calendarService
                                                                .previousTradingSession(currentDate)
                                                                .getDayOfWeek()
                                                        == DayOfWeek.THURSDAY
                                                || daysBetween <= 2)
                                        && !isWithinPriceBand) {
                                    // Within 2 days - set target as 5% above entry

                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.05, rt.getTickSize()));

                                    return true;
                                } else if (daysBetween <= 4 && !isWithinPriceBand) {
                                    // Within 4 days - set target as 7.5% above entry
                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.075, rt.getTickSize()));
                                    return true;
                                } else if (daysBetween <= 6 && !isWithinPriceBand) {
                                    // Within 6 days - set target as 10% above entry
                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.10, rt.getTickSize()));
                                    return true;
                                } else if (daysBetween <= 8 && !isWithinPriceBand) {
                                    // Within 8 days - set target as 12.5% above entry
                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.125, rt.getTickSize()));
                                    return true;
                                } else if (daysBetween <= 10 && !isWithinPriceBand) {
                                    // Within 10 days - set target as 15% above entry
                                    rt.setExitPrice(
                                            formulaService.roundToNearestTick(
                                                    entryPrice * 1.15, rt.getTickSize()));
                                    return true;
                                }
                            }

                            // Original logic for within priceBand% of target
                            if (isWithinPriceBand) {
                                rt.setExitPrice(target);
                                return true;
                            }

                            return false;
                        })
                .collect(Collectors.toList());
    }

    public static Comparator<ResearchTechnical> byDateVolumeScoreDescComparator() {
        return Comparator.comparing(ResearchTechnical::getResearchDate)
                .reversed()
                .thenComparing(ResearchTechnical::getVolumeScore, Comparator.reverseOrder())
                .thenComparing(ResearchTechnical::getScore, Comparator.reverseOrder());
    }
}
