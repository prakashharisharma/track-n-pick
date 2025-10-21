package com.example.service;

import static com.example.data.transactional.entities.ResearchTechnical.Strategy.*;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.Trade;
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
    private final StockPriceService<StockPrice> stockPriceService;
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private boolean validateInitialResearch(
            ResearchTechnical rt,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean isInvestment) {
        if (stockPrice == null || stockPrice.getClose() == null || stockPrice.getLow() == null)
            return false;
        if (rt.getResearchDate() == null) return false;

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, true);

        if (evaluationResultOptional.isPresent()
                && (evaluationResultOptional.get().isBreakdown()
                        || evaluationResultOptional.get().isNearResistance())) {
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

        if (rt.getTimeframe() == Timeframe.DAILY) {
            if (close > highestMovingAverageResult.getValue()) {
                return false;
            }
        }
        if (!isInvestment) {
            // Not investment: either close > ma5 OR entryPrice > ma5
            if (!(close >= ma5 || entryPrice <= ma5)) {
                return false;
            }
        }

        StockPrice stockPriceDaily = stockPriceService.get(rt.getStock(), Timeframe.DAILY);

        // boolean isLowerLowAndLowerHigh = CandleStickUtils.isLowerLow(stockPriceDaily) &&
        // CandleStickUtils.isLowerHigh(stockPriceDaily);

        if (stockPriceDaily.getClose() < stockPrice.getLow() || stockPriceDaily.getClose() < ma5) {
            return false;
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

        return stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20();
    }

    private void adjustPriceAndRisk(
            ResearchTechnical rt, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        stockPrice = stockPriceService.get(rt.getStock(), Timeframe.DAILY);

        Double originalEntry = rt.getEntryPrice();
        Double dayLow = stockPrice.getLow();
        Double close = stockPrice.getClose();
        Double open = stockPrice.getOpen();
        double newEntryPrice = originalEntry;

        // if (dayLow < originalEntry && rt.getTimeframe() == Timeframe.DAILY) {
        if (dayLow < originalEntry) {

            newEntryPrice = dayLow;

            if (close < originalEntry) {
                newEntryPrice = (dayLow + Math.min(close, open)) / 2;
            }
        }
        rt.setEntryPrice(newEntryPrice);
    }

    private List<ResearchTechnical> filterAndProcessResearches(
            List<ResearchTechnical> researchTechnicals,
            LocalDate sessionDate,
            boolean isInvestment) {

        return researchTechnicals.stream()
                .filter(rt -> rt.getEntryPrice() != null && rt.getStock() != null)
                .filter(
                        rt ->
                                rt.getVolumeScore() >= 0.75
                                        || rt.getScore() >= 8.5
                                        || rt.getTimeframe() != Timeframe.DAILY)
                .filter(
                        rt -> {
                            StockPrice stockPrice =
                                    stockPriceService.get(rt.getStock(), rt.getTimeframe());
                            StockTechnicals stockTechnicals =
                                    stockTechnicalsService.get(rt.getStock(), rt.getTimeframe());

                            if (!validateInitialResearch(
                                    rt, stockPrice, stockTechnicals, isInvestment)) return false;
                            if (!validateResearchDate(rt, sessionDate, triggerDays(rt)))
                                return false;

                            // if (!validateVolumeAvg(rt, stockPrice, stockTechnicals)) return
                            // false;

                            adjustPriceAndRisk(rt, stockPrice, stockTechnicals);

                            if (rt.getStopLoss() >= rt.getEntryPrice()) {
                                return false;
                            }

                            return true;
                        })
                .collect(Collectors.toList());
    }

    private int triggerDays(ResearchTechnical researchTechnical) {

        Timeframe timeframe = researchTechnical.getTimeframe();
        if (timeframe == Timeframe.WEEKLY) {
            return 7 * 2;
        }

        if (timeframe == Timeframe.MONTHLY) {
            return 30 * 2;
        }
        return researchTechnical.getEntryStrategy() == SIMPLE ? 1 * 4 : 1 * 2;
    }

    public List<ResearchTechnical> getRecentBasicResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentBasicBuyResearch(sessionDate),
                sessionDate,
                false);
    }

    public List<ResearchTechnical> getRecentSimpleResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentSimpleBuyResearch(sessionDate),
                sessionDate,
                false);
    }

    public List<ResearchTechnical> getRecentFlexiResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentFlexiBuyResearch(sessionDate),
                sessionDate,
                false);
    }

    public List<ResearchTechnical> getRecentPriceResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentPriceBuyResearch(sessionDate),
                sessionDate,
                false);
    }

    public List<ResearchTechnical> getPreviousInvestmentResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getLatestInvestmentBuyResearch(sessionDate),
                sessionDate,
                true);
    }

    public List<ResearchTechnical> getPreviousCandleStickResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getLatestCandleStickBuyResearch(sessionDate),
                sessionDate,
                false);
    }

    public List<ResearchTechnical> getRecentHybridResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentHybridBuyResearch(sessionDate),
                sessionDate,
                false);
    }

    public List<ResearchTechnical> getRecentDynamicResearches(LocalDate sessionDate) {
        return filterAndProcessResearches(
                researchTechnicalService.getRecentDynamicBuyResearch(sessionDate),
                sessionDate,
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

        List<ResearchTechnical> simples =
                researchTechnicalForBuyOrders.stream()
                        .filter(rt -> rt.getEntryStrategy() == SIMPLE)
                        .collect(Collectors.toList());

        List<ResearchTechnical> others =
                researchTechnicalForBuyOrders.stream()
                        .filter(rt -> rt.getEntryStrategy() != INVESTMENT)
                        .filter(rt -> rt.getEntryStrategy() != BASIC)
                        .filter(rt -> rt.getEntryStrategy() != SIMPLE)
                        .collect(Collectors.toList());

        int total = researchTechnicalForBuyOrders.size();
        int invCount = investments.size();

        if (invCount == 0) {
            // just return basics on top + rest
            List<ResearchTechnical> result = new ArrayList<>(simples);
            result.addAll(basics);
            result.addAll(others);
            return uniqueBySymbol(result);
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
        result.addAll(simples);
        result.addAll(basics);
        result.addAll(distributed);

        return uniqueBySymbol(result);
    }

    private static List<ResearchTechnical> uniqueBySymbol(List<ResearchTechnical> list) {
        Set<String> seen = new HashSet<>();
        List<ResearchTechnical> unique = new ArrayList<>();
        for (ResearchTechnical rt : list) {

            // normalize symbol to be safe
            String symbol = rt.getStock().getNseSymbol();
            if (symbol != null) {
                symbol = symbol.trim().toUpperCase(); // remove spaces + case insensitive
            }
            if (seen.add(symbol)) {
                unique.add(rt);
            }
        }
        return unique;
    }
}
