package com.example.service;

import static com.example.data.transactional.entities.ResearchTechnical.Strategy.BASIC;
import static com.example.data.transactional.entities.ResearchTechnical.Strategy.INVESTMENT;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.Trade;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import java.time.LocalDate;
import java.util.*;
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
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private boolean validateBasicResearch(
            ResearchTechnical rt,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean isInvestment) {
        if (stockPrice == null || stockPrice.getClose() == null || stockPrice.getLow() == null)
            return false;
        if (rt.getResearchDate() == null) return false;

        if (rt.getEntryStrategy() == BASIC) {
            return true;
        }

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

        if (evaluationResultOptional.isPresent() && evaluationResultOptional.get().isBreakdown()) {
            return false;
        }

        double ma5 =
                MovingAverageUtil.getMovingAverage5(stockPrice.getTimeframe(), stockTechnicals);
        double close = stockPrice.getClose();
        double entryPrice = rt.getEntryPrice();

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST,
                        stockPrice.getTimeframe(),
                        stockTechnicals,
                        true);

        if (close > highestMovingAverageResult.getValue()) {
            // System.out.println(rt.getStock().getNseSymbol() + " " + close +" > " +
            // highestMovingAverageResult.getValue());
            return false;
        }

        if (!isInvestment) {
            // Not investment: either close > ma5 OR entryPrice > ma5
            if (!(close >= ma5 || entryPrice <= ma5)) {
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

    private boolean validateVolumeAvg(
            ResearchTechnical rt, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        if (rt.getEntryStrategy() == BASIC) {
            return true;
        }

        return stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20();
    }

    private void adjustPriceAndRisk(
            ResearchTechnical rt, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Double originalEntry = rt.getEntryPrice();
        Double dayLow = stockPrice.getLow();
        Double close = stockPrice.getClose();
        Double open = stockPrice.getOpen();
        double newEntryPrice = originalEntry;

        double ma5 =
                MovingAverageUtil.getMovingAverage5(
                        stockTechnicals.getTimeframe(), stockTechnicals);

        if (dayLow < originalEntry) {

            newEntryPrice = dayLow;

            if (close < originalEntry) {
                newEntryPrice = (dayLow + Math.min(close, open)) / 2;
            }

        } else {
            newEntryPrice =
                    formulaService.applyPercentChange((stockPrice.getClose() + ma5) / 2, .5);

            if (CandleStickUtils.isLowerWickLongerThanUpperWick(stockPrice)) {
                newEntryPrice = formulaService.applyPercentChange(newEntryPrice, 0.50);
                if (CandleStickUtils.isHigherHigh(stockPrice)
                        && CandleStickUtils.isHigherLow(stockPrice)) {
                    newEntryPrice = formulaService.applyPercentChange(newEntryPrice, 0.50);
                    if (rt.getEntryStrategy() == INVESTMENT) {
                        newEntryPrice = formulaService.applyPercentChange(newEntryPrice, 0.25);
                    }
                    if (stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20()) {
                        newEntryPrice = formulaService.applyPercentChange(newEntryPrice, 0.25);
                    }
                }
            }

            newEntryPrice = Math.max(newEntryPrice, formulaService.applyPercentChange(ma5, 0.5));
        }

        if (rt.getEntryStrategy() == BASIC) {
            newEntryPrice = formulaService.applyPercentChange(originalEntry, 0.25);
        }

        newEntryPrice = formulaService.ceilToNearestTick(newEntryPrice, rt.getTickSize());

        rt.setEntryPrice(newEntryPrice);
        /*
        rt.setRisk(
                Math.abs(
                        formulaService.calculateChangePercentage(
                                rt.getEntryPrice(), rt.getStopLoss())));*/
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
                            if (rt.getEntryStrategy() == BASIC) {
                                return true;
                            }

                            StockPrice stockPrice =
                                    stockPriceService.get(rt.getStock(), Timeframe.DAILY);
                            StockTechnicals stockTechnicals =
                                    stockTechnicalsService.get(rt.getStock(), Timeframe.DAILY);

                            if (!validateBasicResearch(
                                    rt, stockPrice, stockTechnicals, isInvestment)) return false;
                            if (!validateResearchDate(rt, sessionDate, maxDays)) return false;
                            if (!validateVolumeAvg(rt, stockPrice, stockTechnicals)) return false;

                            adjustPriceAndRisk(rt, stockPrice, stockTechnicals);
                            if (rt.getStopLoss() >= rt.getEntryPrice()) {
                                return false;
                            }
                            return true;
                        })
                .collect(Collectors.toList());
    }

    public List<ResearchTechnical> getRecentBasicResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentBasicBuyResearch(sessionDate),
                sessionDate,
                14,
                false);
    }

    public List<ResearchTechnical> getPreviousInvestmentResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getLatestInvestmentBuyResearch(sessionDate),
                sessionDate,
                14,
                true);
    }

    public List<ResearchTechnical> getPreviousCandleStickResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getLatestCandleStickBuyResearch(sessionDate),
                sessionDate,
                7,
                false);
    }

    public List<ResearchTechnical> getRecentHybridResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentHybridBuyResearch(sessionDate),
                sessionDate,
                7,
                false);
    }

    public List<ResearchTechnical> getRecentDynamicResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentDynamicBuyResearch(sessionDate),
                sessionDate,
                7,
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

                            if (target < stockPrice.getClose()) {
                                target = stockPrice.getClose();
                            }

                            boolean isWithinPriceBand =
                                    formulaService.isWithinPercentage(
                                            close, target, rt.getPriceBand());

                            // Check research date conditions
                            LocalDate researchDate = rt.getResearchDate();
                            /*
                            if (researchDate != null && rt.getEntryStrategy()!=BASIC) {
                                long daysBetween =
                                        java.time.temporal.ChronoUnit.DAYS.between(
                                                researchDate, currentDate);


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
                            }*/

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
        return Comparator.comparing(ResearchTechnical::getRisk)
                .thenComparing(ResearchTechnical::getScore, Comparator.reverseOrder())
                .thenComparing(ResearchTechnical::getVolumeScore, Comparator.reverseOrder())
                .thenComparing(ResearchTechnical::getPriority, Comparator.reverseOrder());
    }

    public static List<ResearchTechnical> distributeInvestmentsStable(
            List<ResearchTechnical> researchTechnicalForBuyOrders) {

        if (researchTechnicalForBuyOrders == null || researchTechnicalForBuyOrders.isEmpty()) {
            return Collections.emptyList();
        }

        List<ResearchTechnical> investments =
                researchTechnicalForBuyOrders.stream()
                        .filter(rt -> rt.getEntryStrategy() == INVESTMENT)
                        .collect(Collectors.toList());

        List<ResearchTechnical> basics =
                researchTechnicalForBuyOrders.stream()
                        .filter(rt -> rt.getEntryStrategy() == BASIC)
                        .collect(Collectors.toList());

        List<ResearchTechnical> others =
                researchTechnicalForBuyOrders.stream()
                        .filter(rt -> rt.getEntryStrategy() != INVESTMENT)
                        .filter(rt -> rt.getEntryStrategy() != BASIC)
                        .collect(Collectors.toList());

        int total = researchTechnicalForBuyOrders.size();
        int invCount = investments.size();

        if (invCount == 0) {
            // just return basics on top + rest
            List<ResearchTechnical> result = new ArrayList<>(basics);
            result.addAll(others);
            return result;
        }

        List<ResearchTechnical> distributed = new ArrayList<>(total);

        // calculate spacing (spread investments as evenly as possible)
        int gap = (int) Math.ceil((double) (others.size() + investments.size()) / invCount);

        int invIndex = 0;
        int otherIndex = 0;

        for (int i = 0; i < others.size() + investments.size(); i++) {
            if (invIndex < invCount && i % gap == 0) {
                distributed.add(investments.get(invIndex++));
            } else if (otherIndex < others.size()) {
                distributed.add(others.get(otherIndex++));
            }
        }

        // prepend basics
        List<ResearchTechnical> result = new ArrayList<>(total);
        result.addAll(basics);
        result.addAll(distributed);

        return result;
    }
}
