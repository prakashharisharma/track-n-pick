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

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchTechnicalServiceImpl implements ResearchTechnicalService {

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
        // Ensure no prior BUY research exists for the timeframe
        ResearchTechnical existingResearchTechnical =
                researchTechnicalRepository
                        .findByStockIdAndTimeframeAndType(
                                stock.getStockId(), timeframe, Trade.Type.BUY)
                        .orElse(null);

        ResearchTechnical newResearchTechnical =
                STOCK_PRICE_CREATORS
                        .getOrDefault(
                                timeframe,
                                () -> {
                                    throw new IllegalArgumentException(
                                            "Unsupported timeframe: " + timeframe);
                                })
                        .get();

        if (existingResearchTechnical != null) {
            newResearchTechnical.setPrevResearchDate(existingResearchTechnical.getResearchDate());
            newResearchTechnical.setPrevEntryStrategy(existingResearchTechnical.getEntryStrategy());
            newResearchTechnical.setPrevEntrySubStrategy(
                    existingResearchTechnical.getEntrySubStrategy());
            newResearchTechnical.setPrevEntryPrice(existingResearchTechnical.getEntryPrice());
            newResearchTechnical.setPrevTarget(existingResearchTechnical.getTarget());
            newResearchTechnical.setPrevStopLoss(existingResearchTechnical.getStopLoss());
            newResearchTechnical.setPrevScore(existingResearchTechnical.getScore());
        }

        // Create new research entry
        newResearchTechnical.setStock(stock);
        newResearchTechnical.setTimeframe(timeframe);
        newResearchTechnical.setType(Trade.Type.BUY);
        newResearchTechnical.setEntryStrategy(tradeSetup.getStrategy());
        newResearchTechnical.setEntrySubStrategy(tradeSetup.getSubStrategy());
        newResearchTechnical.setPriority(Double.valueOf(tradeSetup.getSubStrategy().getPriority()));

        PriceInfoDto priceInfoDto = nsePriceInfoFetcher.getPriceInfo(stock.getNseSymbol());
        newResearchTechnical.setTickSize(priceInfoDto.getTickSize());
        newResearchTechnical.setPriceBand(priceInfoDto.getPriceBand());

        newResearchTechnical.setEntryPrice(
                this.calculateResearchPrice(tradeSetup, stockPrice, newResearchTechnical));

        newResearchTechnical.setStopLoss(
                this.calculateStopLoss(tradeSetup, stockPrice, newResearchTechnical));
        newResearchTechnical.setRisk(
                Math.abs(
                        formulaService.calculateChangePercentage(
                                newResearchTechnical.getEntryPrice(),
                                newResearchTechnical.getStopLoss())));
        newResearchTechnical.setTarget(
                targetService.calculateTarget(stockPrice, newResearchTechnical));

        double confidenceScore =
                ConfidenceScoreCalculator.calculateConfidenceScore(
                        newResearchTechnical.getEntryStrategy().getPriority(),
                        newResearchTechnical.getEntrySubStrategy().getPriority(),
                        newResearchTechnical.getRisk(),
                        fundamentalResearchService.marketCap(newResearchTechnical.getStock()),
                        newResearchTechnical.getEntryPrice(),
                        ConfidenceScoreCalculator.calculateVolumeScore(stockTechnicals),
                        ConfidenceScoreCalculator.calculateMacdScore(
                                stockTechnicals, macdIndicatorService),
                        researchInsightService.valuationScore(stock));

        newResearchTechnical.setScore(miscUtil.roundToTwoDecimals(confidenceScore));

        newResearchTechnical.setResearchDate(sessionDate);
        newResearchTechnical.setLastModified(LocalDateTime.now());

        boolean isRiskWithinLimit =
                isRiskWithinLimit(
                        timeframe,
                        stockPrice,
                        stockTechnicals,
                        newResearchTechnical.getEntrySubStrategy(),
                        newResearchTechnical.getRisk());
        evaluationLogService.add(
                stockPrice,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format("isRiskWithinLimit: {}", isRiskWithinLimit));

        boolean isTargetValid =
                targetService.isTargetValid(
                        newResearchTechnical.getEntryPrice(), newResearchTechnical.getTarget());

        evaluationLogService.add(
                stockPrice,
                EvaluationLog.Type.POSITIVE,
                StringUtils.format("isTargetValid: {}", isTargetValid));

        boolean isUpperCircuit =
                formulaService.roundToNearestTick(
                                formulaService.applyPercentChange(
                                        stockPrice.getPrevClose(),
                                        newResearchTechnical.getPriceBand()),
                                newResearchTechnical.getTickSize())
                        == stockPrice.getClose();

        if (isRiskWithinLimit
                && isTargetValid
                && researchInsightService.isStrongInsights(stockPrice)
                && !isUpperCircuit) {

            newResearchTechnical = researchTechnicalRepository.save(newResearchTechnical);
        }

        return newResearchTechnical;
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

        if (subStrategy == ResearchTechnical.SubStrategy.MA200_BREAKOUT
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

        if (tradeSetup.getSubStrategy() == ResearchTechnical.SubStrategy.TARGET_ACHIEVED) {
            exitPrice = existingResearch.getTarget();
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
        return researchTechnicalRepository.findAllByResearchDateAndTypeOrderByScoreDescPriorityDesc(
                sessionDate, Trade.Type.BUY);
    }

    @Override
    public List<ResearchTechnical> getLatestInvestmentBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateNotAndTypeAndEntryStrategyOrderByScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY, ResearchTechnical.Strategy.INVESTMENT);
    }

    @Override
    public List<ResearchTechnical> getRecentHybridBuyResearch(LocalDate sessionDate) {
        return researchTechnicalRepository
                .findAllByResearchDateNotAndTypeAndEntryStrategyOrderByScoreDescPriorityDesc(
                        sessionDate, Trade.Type.BUY, ResearchTechnical.Strategy.HYBRID);
    }

    @Override
    public List<ResearchTechnical> getLatestSellResearch(LocalDate sessionDate) {
        return researchTechnicalRepository.findAllByExitDateAndType(sessionDate, Trade.Type.SELL);
    }

    private double calculateStopLoss(
            TradeSetup tradeSetup, StockPrice stockPrice, ResearchTechnical researchTechnical) {

        if (researchTechnical.getEntrySubStrategy() == ResearchTechnical.SubStrategy.LOWEST_BREAKOUT
                || researchTechnical.getEntrySubStrategy()
                        == ResearchTechnical.SubStrategy.LOW_BREAKOUT
                || researchTechnical.getEntrySubStrategy()
                        == ResearchTechnical.SubStrategy.MA200_BREAKOUT
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
            return formulaService.ceilToNearestTick(
                    tradeSetup.getResearchPrice(), researchTechnical.getTickSize());
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
