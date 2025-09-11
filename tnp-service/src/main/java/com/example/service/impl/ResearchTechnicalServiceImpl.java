package com.example.service.impl;

import com.example.common.config.CacheManagerNameConstants;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.data.transactional.repo.ResearchTechnicalRepository;
import com.example.data.transactional.view.ResearchTechnicalResult;
import com.example.dto.common.TradeSetup;
import com.example.dto.io.PriceInfoDto;
import com.example.dto.mapper.StockTechnicalsMapper;
import com.example.dto.response.ResearchTechnicalDetailsCurrentResponse;
import com.example.dto.response.ResearchTechnicalDetailsHistoryResponse;
import com.example.external.NSEPriceInfoFetcher;
import com.example.service.*;
import com.example.service.ConfidenceScoreCalculator;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.SupportResistanceZoneUtils;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchTechnicalServiceImpl implements ResearchTechnicalService {
    private final VolumeIndicatorService volumeIndicatorService;

    private final ResearchTechnicalRepository<ResearchTechnical> researchTechnicalRepository;

    private final EvaluationLogService evaluationLogService;
    private final FormulaService formulaService;

    private final StockPriceHelperService stockPriceHelperService;

    private final MiscUtil miscUtil;
    private final CalendarService calendarService;

    private final MacdIndicatorService macdIndicatorService;

    private final TargetService targetService;

    private final UserService userService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private final FundamentalResearchService fundamentalResearchService;

    private final PositionService positionService;

    private final ResearchInsightService researchInsightService;

    private final NSEPriceInfoFetcher nsePriceInfoFetcher;

    private static final Map<Timeframe, Supplier<ResearchTechnical>> STOCK_PRICE_CREATORS =
            Map.of(
                    Timeframe.DAILY, ResearchTechnicalDaily::new,
                    Timeframe.WEEKLY, ResearchTechnicalWeekly::new,
                    Timeframe.MONTHLY, ResearchTechnicalMonthly::new,
                    Timeframe.QUARTERLY, ResearchTechnicalQuarterly::new,
                    Timeframe.YEARLY, ResearchTechnicalYearly::new);

    @Override
    public ResearchTechnical entry(
            Stock stock,
            Timeframe timeframe,
            TradeSetup tradeSetup,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            LocalDate sessionDate) {
        // Find existing research or create new one
        ResearchTechnical researchTechnical =
                researchTechnicalRepository
                        .findByStockIdAndTimeframeAndType(
                                stock.getStockId(), timeframe, Trade.Type.BUY)
                        .orElseGet(
                                () ->
                                        STOCK_PRICE_CREATORS
                                                .getOrDefault(
                                                        timeframe,
                                                        () -> {
                                                            throw new IllegalArgumentException(
                                                                    "Unsupported timeframe: "
                                                                            + timeframe);
                                                        })
                                                .get());

        // If this is an update, save previous values
        if (researchTechnical.getResearchTechnicalsId() != null) {
            researchTechnical.setPrevResearchDate(researchTechnical.getResearchDate());
            researchTechnical.setPrevEntryStrategy(researchTechnical.getEntryStrategy());
            researchTechnical.setPrevEntrySubStrategy(researchTechnical.getEntrySubStrategy());
            researchTechnical.setPrevEntryPrice(researchTechnical.getEntryPrice());
            researchTechnical.setPrevTarget(researchTechnical.getTarget());
            researchTechnical.setPrevStopLoss(researchTechnical.getStopLoss());
            researchTechnical.setPrevScore(researchTechnical.getScore());
        }

        // Set new values
        researchTechnical.setStock(stock);
        researchTechnical.setTimeframe(timeframe);
        researchTechnical.setType(Trade.Type.BUY);
        researchTechnical.setEntryStrategy(tradeSetup.getStrategy());
        researchTechnical.setEntrySubStrategy(tradeSetup.getSubStrategy());

        double priority =
                (Double.valueOf(tradeSetup.getStrategy().getPriority())
                                + Double.valueOf(tradeSetup.getSubStrategy().getPriority()))
                        / 2;

        researchTechnical.setPriority(priority);

        PriceInfoDto priceInfoDto = nsePriceInfoFetcher.getPriceInfo(stock.getNseSymbol());
        researchTechnical.setTickSize(priceInfoDto.getTickSize());
        researchTechnical.setPriceBand(priceInfoDto.getPriceBand());

        researchTechnical.setEntryPrice(
                this.calculateResearchPrice(tradeSetup, stockPrice, researchTechnical));

        double stopLoss = this.calculateStopLoss(tradeSetup, stockPrice, researchTechnical);

        if (Math.abs(
                        formulaService.calculateChangePercentage(
                                researchTechnical.getEntryPrice(), stopLoss))
                > MAX_RISK) {
            stopLoss =
                    formulaService.applyPercentChange(
                            researchTechnical.getEntryPrice(), -1 * MAX_RISK);
            stopLoss = Math.min(stockPrice.getLow(), stopLoss);
        }

        researchTechnical.setStopLoss(stopLoss);

        researchTechnical.setRisk(
                Math.abs(
                        formulaService.calculateChangePercentage(
                                researchTechnical.getEntryPrice(),
                                researchTechnical.getStopLoss())));

        researchTechnical.setTarget(targetService.calculateTarget(stockPrice, researchTechnical));

        double confidenceScore =
                ConfidenceScoreCalculator.calculateConfidenceScore(
                        researchTechnical.getEntryStrategy().getPriority(),
                        researchTechnical.getEntrySubStrategy().getPriority(),
                        researchTechnical.getRisk(),
                        fundamentalResearchService.marketCap(researchTechnical.getStock()),
                        researchTechnical.getEntryPrice(),
                        ConfidenceScoreCalculator.calculateVolumeScore(stockTechnicals),
                        ConfidenceScoreCalculator.calculateMacdScore(
                                stockTechnicals, macdIndicatorService),
                        researchInsightService.valuationScore(stock));

        double volumeScore = 0.0;

        if (volumeIndicatorService.isMinVolumeAvg(stockTechnicals, 2.0)
                && volumeIndicatorService.isMinVolume(stockTechnicals, 2.0)) {
            volumeScore = volumeScore + 0.75;
        } else if (volumeIndicatorService.isMinVolume(stockTechnicals, 2.0)) {
            volumeScore = volumeScore + 0.50;
        } else if (volumeIndicatorService.isMinVolumeAvg(stockTechnicals, 2.0)) {
            volumeScore = volumeScore + 0.25;
        }

        researchTechnical.setVolumeScore(volumeScore);

        double score = miscUtil.roundToTwoDecimals(confidenceScore + volumeScore);
        score = Math.min(score, 10.0);
        researchTechnical.setScore(score);
        researchTechnical.setResearchDate(sessionDate);
        researchTechnical.setLastModified(LocalDateTime.now());

        boolean isRiskWithinLimit =
                isRiskWithinLimit(
                        timeframe,
                        stockPrice,
                        stockTechnicals,
                        researchTechnical.getEntrySubStrategy(),
                        researchTechnical.getRisk());
        evaluationLogService.add(
                stockPrice,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format("isRiskWithinLimit: {}", isRiskWithinLimit));

        boolean isTargetValid =
                targetService.isTargetValid(
                        researchTechnical.getEntryPrice(), researchTechnical.getTarget());

        evaluationLogService.add(
                stockPrice,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format("isTargetValid: {}", isTargetValid));

        boolean isUpperCircuit =
                formulaService.roundToNearestTick(
                                formulaService.applyPercentChange(
                                        stockPrice.getPrevClose(),
                                        researchTechnical.getPriceBand()),
                                researchTechnical.getTickSize())
                        == stockPrice.getClose();

        if (isRiskWithinLimit
                && isTargetValid
                && researchInsightService.isStrongInsights(
                        stockPrice, ResearchTechnical.Strategy.INVESTMENT)
                && !isUpperCircuit) {

            researchTechnical = researchTechnicalRepository.save(researchTechnical);
        }

        return researchTechnical;
    }

    public boolean isRiskWithinLimit(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical.SubStrategy subStrategy,
            double risk) {

        double weight = subStrategy.getPriority();
        double riskBuffer = 0.0;

        if (weight >= 10) {
            riskBuffer = (timeframe == Timeframe.DAILY) ? 3.0 : 1.5;
        } else if (weight >= 9) {
            riskBuffer = (timeframe == Timeframe.DAILY) ? 2.0 : 1.0;
        } else if (weight >= 8) {
            riskBuffer = (timeframe == Timeframe.DAILY) ? 1.0 : 0.5;
        }

        double limit = 0.0;
        boolean result = false;

        if (subStrategy.isBreakout()) {
            switch (timeframe) {
                case DAILY -> limit = 7.0 + riskBuffer;
                case WEEKLY -> limit = 8.5 + riskBuffer;
                case MONTHLY -> limit = 10.0 + riskBuffer;
            }
        } else if (subStrategy.isSupport()) {
            switch (timeframe) {
                case DAILY -> limit = 5.0 + riskBuffer;
                case WEEKLY -> limit = 7.5 + riskBuffer;
                case MONTHLY -> limit = 10.0 + riskBuffer;
            }
        }
        if (subStrategy == ResearchTechnical.SubStrategy.BREAKOUT) {
            limit = limit + 3.5;
        } else if (subStrategy == ResearchTechnical.SubStrategy.MA200_BREAKOUT
                || subStrategy == ResearchTechnical.SubStrategy.LOWEST_BREAKOUT) {
            limit = limit + 3.0;
        } else if (subStrategy == ResearchTechnical.SubStrategy.MA100_BREAKOUT
                || subStrategy == ResearchTechnical.SubStrategy.LOW_BREAKOUT) {
            limit = limit + 2.0;
        } else if (subStrategy == ResearchTechnical.SubStrategy.MA50_BREAKOUT
                || subStrategy == ResearchTechnical.SubStrategy.MEDIUM_BREAKOUT) {
            limit = limit + 1.0;
        }

        result = risk <= limit;

        evaluationLogService.add(
                stockTechnicals,
                result ? EvaluationLog.Type.POSITIVE : EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        "{} {} RISK → risk:{} weight:{} buffer:{} limit:{} → isValid:{}",
                        timeframe.name(),
                        subStrategy,
                        risk,
                        weight,
                        riskBuffer,
                        limit,
                        result));

        return result;
    }

    @Override
    public ResearchTechnical exit(
            Stock stock,
            Timeframe timeframe,
            TradeSetup tradeSetup,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            LocalDate sessionDate) {
        // Find existing BUY research entry
        ResearchTechnical existingResearch =
                researchTechnicalRepository
                        .findByStockIdAndTimeframeAndType(
                                stock.getStockId(), timeframe, Trade.Type.BUY)
                        .orElse(null);

        if (existingResearch == null) {
            throw new IllegalStateException(
                    "No BUY research entry found for this stock and timeframe.");
        }

        PriceInfoDto priceInfoDto = nsePriceInfoFetcher.getPriceInfo(stock.getNseSymbol());
        existingResearch.setTickSize(priceInfoDto.getTickSize());
        existingResearch.setPriceBand(priceInfoDto.getPriceBand());

        existingResearch.setExitDate(sessionDate);

        double exitPrice =
                (Math.min(stockPrice.getOpen(), stockPrice.getClose()) + stockPrice.getLow()) / 2;

        if (CandleStickUtils.isGreen(stockPrice)) {
            exitPrice = stockPrice.getClose();
        }

        if (tradeSetup.getSubStrategy() == ResearchTechnical.SubStrategy.TARGET_ACHIEVED) {

            exitPrice =
                    Math.max(
                            existingResearch.getTarget(),
                            (stockPrice.getOpen() + stockPrice.getHigh()) / 2);

            if (CandleStickUtils.isGreen(stockPrice)
                    && existingResearch.getTarget() > stockPrice.getClose()
                    && existingResearch.getTarget() < stockPrice.getHigh()) {
                exitPrice =
                        Math.min(
                                existingResearch.getTarget(),
                                (stockPrice.getOpen() + stockPrice.getHigh()) / 2);
            }

            if (CandleStickUtils.isRed(stockPrice)) {
                exitPrice =
                        Math.min(
                                existingResearch.getTarget(),
                                Math.min(stockPrice.getOpen(), stockPrice.getClose()));
            }
        }

        existingResearch.setExitPrice(
                formulaService.floorToNearestTick(exitPrice, existingResearch.getTickSize()));

        existingResearch.setType(Trade.Type.SELL);
        existingResearch.setExitStrategy(tradeSetup.getStrategy());
        existingResearch.setExitSubStrategy(tradeSetup.getSubStrategy());
        existingResearch.setLastModified(LocalDateTime.now());

        boolean isLowerCircuit =
                formulaService.roundToNearestTick(
                                formulaService.applyPercentChange(
                                        stockPrice.getPrevClose(),
                                        -1 * existingResearch.getPriceBand()),
                                existingResearch.getTickSize())
                        == stockPrice.getClose();

        if (!isLowerCircuit) {
            return researchTechnicalRepository.save(existingResearch);
        }
        return existingResearch;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResearchTechnical get(Stock stock, Timeframe timeframe, Trade.Type type) {
        return researchTechnicalRepository
                .findByStockIdAndTimeframeAndType(stock.getStockId(), timeframe, type)
                .orElse(null);
    }

    @Override
    public List<ResearchTechnical> getAll(Trade.Type type) {
        return researchTechnicalRepository.findAllByType(type);
    }

    @Override
    public List<ResearchTechnical> getLatestBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateAndTypeOrderByRiskAscVolumeScoreDescScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY);
    }

    @Override
    public List<ResearchTechnical> getAllResearchWithinAYear(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateWithinOneYearOrderByScoreDescPriorityDesc(
                        sessionDate, sessionDate.minusYears(1));
    }

    @Override
    public List<ResearchTechnical> getLatestInvestmentBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateNotAndTypeAndEntryStrategyOrderByScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY, ResearchTechnical.Strategy.INVESTMENT);
    }

    @Override
    public List<ResearchTechnical> getLatestCandleStickBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateNotAndTypeAndEntryStrategyOrderByScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY, ResearchTechnical.Strategy.CANDLESTICK);
    }

    @Override
    public List<ResearchTechnical> getRecentHybridBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateNotAndTypeAndEntryStrategyOrderByScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY, ResearchTechnical.Strategy.HYBRID);
    }

    @Override
    public List<ResearchTechnical> getRecentDynamicBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateNotAndTypeAndEntryStrategyOrderByScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY, ResearchTechnical.Strategy.DYNAMIC);
    }

    @Override
    public List<ResearchTechnical> getRecentBasicBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateNotAndTypeAndEntryStrategyOrderByScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY, ResearchTechnical.Strategy.BASIC);
    }

    @Override
    public List<ResearchTechnical> getLatestSellResearch(LocalDate sessionDate) {
        return researchTechnicalRepository.findAllByExitDateAndType(sessionDate, Trade.Type.SELL);
    }

    private double calculateStopLoss(
            TradeSetup tradeSetup, StockPrice stockPrice, ResearchTechnical researchTechnical) {

        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.BASIC) {

            double stopLoss = Math.min(stockPrice.getLow(), stockPrice.getPrevLow());

            return formulaService.floorToNearestTick(stopLoss, researchTechnical.getTickSize());
        }

        if (researchTechnical.getEntryStrategy() == ResearchTechnical.Strategy.INVESTMENT) {

            double sl =
                    Math.min(
                            formulaService.applyPercentChange(
                                    researchTechnical.getEntryPrice(), -1 * MAX_RISK),
                            stockPrice.getLow() - researchTechnical.getTickSize());

            return formulaService.floorToNearestTick(sl, researchTechnical.getTickSize());
        }

        if (researchTechnical.getEntrySubStrategy() == ResearchTechnical.SubStrategy.LOWEST_BREAKOUT
                || researchTechnical.getEntrySubStrategy()
                        == ResearchTechnical.SubStrategy.LOW_BREAKOUT
                || researchTechnical.getEntrySubStrategy()
                        == ResearchTechnical.SubStrategy.MEDIUM_BREAKOUT
                || researchTechnical.getEntrySubStrategy()
                        == ResearchTechnical.SubStrategy.MA100_BREAKOUT) {

            SupportResistanceZones currentZones =
                    SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);
            SupportResistanceZoneUtils.Zone currentSupport = currentZones.getSupport();

            return formulaService.floorToNearestTick(
                    formulaService.applyPercentChange(
                            Math.max(
                                    CandleStickUtils.isPrevSessionRed(stockPrice)
                                            ? stockPrice.getPrevLow()
                                            : stockPrice.getLow(),
                                    (stockPriceHelperService.findLowestLow(stockPrice)
                                                    + currentSupport.getStart())
                                            / 2),
                            -1 * 0.05),
                    researchTechnical.getTickSize());
        }

        SupportResistanceZones currentZones =
                SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);
        SupportResistanceZoneUtils.Zone currentSupport = currentZones.getSupport();

        double stopLoss = stockPrice.getLow();

        if (CandleStickUtils.isPrevSessionRed(stockPrice)) {
            stopLoss = Math.min(stopLoss, stockPrice.getPrevLow());
        }

        return formulaService.floorToNearestTick(
                formulaService.applyPercentChange(stopLoss, -1 * 0.05),
                researchTechnical.getTickSize());
    }

    private double calculateResearchPrice(
            TradeSetup tradeSetup, StockPrice stockPrice, ResearchTechnical researchTechnical) {

        if (tradeSetup.getResearchPrice() > 0.0) {
            return Math.min(
                    formulaService.ceilToNearestTick(
                            tradeSetup.getResearchPrice(), researchTechnical.getTickSize()),
                    stockPrice.getHigh());
        }

        ResearchTechnical.SubStrategy subStrategy = tradeSetup.getSubStrategy();

        boolean isRedCandle = CandleStickUtils.isRed(stockPrice);

        double researchPrice = stockPrice.getHigh();

        researchPrice =
                isRedCandle
                        ? (stockPrice.getOpen()
                                + (stockPrice.getHigh() - stockPrice.getOpen()) * 0.50)
                        : (stockPrice.getClose()
                                + (stockPrice.getHigh() - stockPrice.getClose()) * 0.50);

        return Math.min(
                formulaService.ceilToNearestTick(researchPrice, researchTechnical.getTickSize()),
                stockPrice.getHigh());
    }

    @Override
    public Page<ResearchTechnicalResult> searchHistory(
            int page,
            int size,
            Trade.Type type,
            Timeframe timeframe,
            String sortBy,
            String direction) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        direction.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending());

        List<LocalDate> currentDates = this.getCurrentDates();

        Page<ResearchTechnical> researchPage =
                researchTechnicalRepository.searchHistory(type, timeframe, currentDates, pageable);

        return researchPage.map(this::mapToResult);
    }

    @Override
    public Page<ResearchTechnicalResult> searchCurrent(
            int page,
            int size,
            Trade.Type type,
            Timeframe timeframe,
            String sortBy,
            String direction) {

        List<LocalDate> currentDates = this.getCurrentDates();
        return this.search(page, size, type, timeframe, currentDates, sortBy, direction);
    }

    private List<LocalDate> getCurrentDates() {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate current = miscUtil.currentDate();
        dates.add(current); // always add today

        LocalDate prevSession = calendarService.previousTradingSession(current);
        dates.add(prevSession);

        LocalDate firstDayOfWeek = miscUtil.currentWeekFirstDay();
        LocalDate prevWeekSession = calendarService.previousTradingSession(firstDayOfWeek);

        if (!calendarService.isLastTradingSessionOfWeek(prevSession)) {
            dates.add(prevWeekSession);
        }

        LocalDate firstDayOfMonth = miscUtil.currentMonthFirstDay();
        LocalDate prevMonthSession = calendarService.previousTradingSession(firstDayOfMonth);
        if (!calendarService.isLastTradingSessionOfMonth(prevSession)) {
            dates.add(prevMonthSession);
        }
        return dates;
    }

    public Page<ResearchTechnicalResult> search(
            int page,
            int size,
            Trade.Type type,
            Timeframe timeframe,
            List<LocalDate> dates,
            String sortBy,
            String direction) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        direction.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending());

        Page<ResearchTechnical> researchPage =
                researchTechnicalRepository.search(type, timeframe, dates, pageable);

        return researchPage.map(this::mapToResult);
    }

    private ResearchTechnicalResult mapToResult(ResearchTechnical researchTechnical) {
        Double price = null;
        LocalDate date = null;

        if (researchTechnical.getType() == Trade.Type.BUY) {
            price = researchTechnical.getEntryPrice();
            date = researchTechnical.getResearchDate();
        } else if (researchTechnical.getType() == Trade.Type.SELL) {
            price = researchTechnical.getExitPrice();
            date = researchTechnical.getExitDate();
        }

        StockPrice stockPrice =
                stockPriceService.get(researchTechnical.getStock(), Timeframe.DAILY);
        double currentPrice = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double changePercent = prevClose != 0 ? ((currentPrice - prevClose) / prevClose) * 100 : 0;

        Stock stock = researchTechnical.getStock();

        return ResearchTechnicalResult.builder()
                .id(researchTechnical.getResearchTechnicalsId())
                .symbol(stock.getNseSymbol())
                .name(stock.getCompanyName())
                .timeframe(researchTechnical.getTimeframe())
                .type(researchTechnical.getType())
                .score(researchTechnical.getScore())
                .price(currentPrice)
                .researchPrice(price)
                .changePercent(miscUtil.roundToTwoDecimals(changePercent))
                .researchDate(date) // Mapping based on type
                .build();
    }

    @Override
    public ResearchTechnicalDetailsCurrentResponse getCurrentDetails(
            Long userId, Long researchTechnicalId) {

        User user = userService.get(userId);

        ResearchTechnical researchTechnical =
                researchTechnicalRepository
                        .findById(researchTechnicalId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "ResearchTechnical not found for"
                                                        + " researchTechnicalId: "
                                                        + researchTechnicalId));

        return this.mapToCurrentDetails(user, researchTechnical);
    }

    private ResearchTechnicalDetailsCurrentResponse mapToCurrentDetails(
            User user, ResearchTechnical researchTechnical) {
        Double price = null;
        LocalDate date = null;

        if (researchTechnical.getType() == Trade.Type.BUY) {
            price = researchTechnical.getEntryPrice();
            date = researchTechnical.getResearchDate();
        } else if (researchTechnical.getType() == Trade.Type.SELL) {
            price = researchTechnical.getExitPrice();
            date = researchTechnical.getExitDate();
        }

        StockPrice stockPrice =
                stockPriceService.get(researchTechnical.getStock(), Timeframe.DAILY);
        double currentPrice = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double changePercent = prevClose != 0 ? ((currentPrice - prevClose) / prevClose) * 100 : 0;

        Stock stock = researchTechnical.getStock();

        StockTechnicals stockTechnicals =
                stockTechnicalsService.get(stock, researchTechnical.getTimeframe());
        long positionSize = positionService.calculate(user, researchTechnical);

        long adjustedPositionSize =
                positionService.calculateAdjustedPositionSize(
                        user, researchTechnical, positionSize);

        return ResearchTechnicalDetailsCurrentResponse.builder()
                .id(researchTechnical.getResearchTechnicalsId())
                .symbol(stock.getNseSymbol())
                .name(stock.getCompanyName())
                .timeframe(researchTechnical.getTimeframe())
                .type(researchTechnical.getType())
                .score(researchTechnical.getScore())
                .price(currentPrice)
                .researchPrice(price)
                .changePercent(miscUtil.roundToTwoDecimals(changePercent))
                .researchDate(date)
                .sector(stock.getSector().getSectorName())
                .marketCap(fundamentalResearchService.marketCap(stock))
                .positionSize(positionSize)
                .adjustedPositionSize(adjustedPositionSize)
                .technicals(StockTechnicalsMapper.toDTO(stockTechnicals))
                .build();
    }

    @Override
    public ResearchTechnicalDetailsHistoryResponse getHistoryDetails(Long researchTechnicalId) {

        ResearchTechnical researchTechnical =
                researchTechnicalRepository
                        .findById(researchTechnicalId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "ResearchTechnical not found for"
                                                        + " researchTechnicalId: "
                                                        + researchTechnicalId));

        return this.mapToHistoryDetails(researchTechnical);
    }

    private ResearchTechnicalDetailsHistoryResponse mapToHistoryDetails(
            ResearchTechnical researchTechnical) {

        StockPrice stockPrice =
                stockPriceService.get(researchTechnical.getStock(), Timeframe.DAILY);

        double currentPrice = stockPrice.getClose();
        double prevClose = researchTechnical.getEntryPrice();
        double changePercent = prevClose != 0 ? ((currentPrice - prevClose) / prevClose) * 100 : 0;

        if (researchTechnical.getExitDate() != null) {

            currentPrice = researchTechnical.getExitPrice();
            prevClose = researchTechnical.getEntryPrice();
            changePercent = prevClose != 0 ? ((currentPrice - prevClose) / prevClose) * 100 : 0;
        }

        Stock stock = researchTechnical.getStock();

        return ResearchTechnicalDetailsHistoryResponse.builder()
                .id(researchTechnical.getResearchTechnicalsId())
                .symbol(stock.getNseSymbol())
                .name(stock.getCompanyName())
                .timeframe(researchTechnical.getTimeframe())
                .type(researchTechnical.getType())
                .score(researchTechnical.getScore())
                .price(stockPrice.getClose())
                .researchDate(researchTechnical.getResearchDate())
                .researchPrice(researchTechnical.getEntryPrice())
                .exitDate(researchTechnical.getExitDate())
                .exitPrice(researchTechnical.getExitPrice())
                .changePercent(miscUtil.roundToTwoDecimals(changePercent))
                .sector(stock.getSector().getSectorName())
                .marketCap(fundamentalResearchService.marketCap(stock))
                .build();
    }

    @Override
    public void updateScore(ResearchTechnical researchTechnical) {

        double risk =
                formulaService.calculateChangePercentage(
                        researchTechnical.getEntryPrice(), researchTechnical.getStopLoss());

        double mcapInCr = fundamentalResearchService.marketCap(researchTechnical.getStock());

        StockTechnicals stockTechnicals =
                stockTechnicalsService.get(
                        researchTechnical.getStock(), researchTechnical.getTimeframe());

        double confidenceScore =
                ConfidenceScoreCalculator.calculateConfidenceScore(
                        researchTechnical.getEntryStrategy().getPriority(),
                        researchTechnical.getEntrySubStrategy().getPriority(),
                        risk,
                        mcapInCr,
                        researchTechnical.getEntryPrice(),
                        ConfidenceScoreCalculator.calculateVolumeScore(stockTechnicals),
                        ConfidenceScoreCalculator.calculateMacdScore(
                                stockTechnicals, macdIndicatorService),
                        researchInsightService.valuationScore(researchTechnical.getStock()));

        System.out.println(
                researchTechnical.getStock().getNseSymbol()
                        + " : "
                        + researchTechnical.getEntrySubStrategy()
                        + " : "
                        + risk
                        + " : "
                        + mcapInCr
                        + " : "
                        + researchTechnical.getEntryPrice()
                        + " :"
                        + miscUtil.roundToTwoDecimals(confidenceScore));
        researchTechnical.setScore(confidenceScore);
        researchTechnicalRepository.save(researchTechnical);
    }

    @Override
    @Cacheable(
            value = "getLatest", // your cache name here
            key = "#stock.stockId", // cache per user ID
            cacheManager = CacheManagerNameConstants.CACHE_12_HOUR)
    public Optional<ResearchTechnical> getLatest(Stock stock) {

        return researchTechnicalRepository.findTopByStockOrderByResearchTechnicalsIdDesc(stock);
    }

    @Override
    @Cacheable(
            value = "getTickSize", // your cache name here
            key = "#stock.stockId", // cache per user ID
            cacheManager = CacheManagerNameConstants.CACHE_12_HOUR)
    public double getTickSize(Stock stock) {

        Optional<ResearchTechnical> researchTechnicalOptional = this.getLatest(stock);

        if (researchTechnicalOptional.isPresent()
                && researchTechnicalOptional.get().getTickSize() != 0.0) {
            return researchTechnicalOptional.get().getTickSize();
        }

        return 0.1;
    }
}
