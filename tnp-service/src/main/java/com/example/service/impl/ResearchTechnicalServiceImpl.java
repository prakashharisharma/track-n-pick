package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.data.transactional.repo.ResearchTechnicalRepository;
import com.example.dto.common.TradeSetup;
import com.example.service.ResearchTechnicalService;
import com.example.service.RiskFactor;
import com.example.service.StockPriceHelperService;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchTechnicalServiceImpl implements ResearchTechnicalService {

    private final ResearchTechnicalRepository<ResearchTechnical> researchTechnicalRepository;

    private final FormulaService formulaService;

    private final StockPriceHelperService stockPriceHelperService;

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

        if (existingResearchTechnical != null) {
            return existingResearchTechnical;
        }

        ResearchTechnical newResearchTechnical =
                STOCK_PRICE_CREATORS
                        .getOrDefault(
                                timeframe,
                                () -> {
                                    throw new IllegalArgumentException(
                                            "Unsupported timeframe: " + timeframe);
                                })
                        .get();

        // Create new research entry
        newResearchTechnical.setStock(stock);
        newResearchTechnical.setTimeframe(timeframe);
        newResearchTechnical.setType(Trade.Type.BUY);
        newResearchTechnical.setStrategy(tradeSetup.getStrategy());
        newResearchTechnical.setSubStrategy(tradeSetup.getSubStrategy());

        newResearchTechnical.setResearchPrice(this.calculateResearchPrice(tradeSetup, stockPrice));

        newResearchTechnical.setStopLoss(
                this.calculateStopLoss(tradeSetup, stockPrice, newResearchTechnical));
        newResearchTechnical.setTarget(
                formulaService.calculateTarget(
                        stockPrice.getHigh(),
                        stockPrice.getLow(),
                        this.calculateRiskRewardRatio(tradeSetup.getSubStrategy())));
        newResearchTechnical.setRisk(
                Math.abs(
                        formulaService.calculateChangePercentage(
                                stockPrice.getHigh(), stockPrice.getLow())));
        newResearchTechnical.setScore(
                this.calculateScore(stock, timeframe, tradeSetup, stockTechnicals, stockPrice));
        newResearchTechnical.setResearchDate(sessionDate);

        return researchTechnicalRepository.save(newResearchTechnical);
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

        existingResearch.setExitDate(sessionDate);
        existingResearch.setExitPrice(stockPrice.getClose());
        existingResearch.setType(Trade.Type.SELL);

        return researchTechnicalRepository.save(existingResearch);
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

    private double calculateRiskRewardRatio(ResearchTechnical.SubStrategy subStrategy) {

        if (subStrategy == ResearchTechnical.SubStrategy.STRONG_SUPPORT) {
            return 3.0;
        } else if (subStrategy == ResearchTechnical.SubStrategy.WEAK_SUPPORT) {
            return 2.0;
        } else if (subStrategy == ResearchTechnical.SubStrategy.STRONG_BREAKOUT) {
            return 3.0;
        } else if (subStrategy == ResearchTechnical.SubStrategy.WEAK_BREAKOUT) {
            return 2.0;
        } else if (subStrategy == ResearchTechnical.SubStrategy.BULLISH_INDICATORS) {
            return 2.0;
        }

        return 2.0;
    }

    private double calculateStopLoss(
            TradeSetup tradeSetup, StockPrice stockPrice, ResearchTechnical researchTechnical) {

        double buffer = 0.005 * stockPrice.getLow(); // 0.5% buffer
        ResearchTechnical.SubStrategy subStrategy = tradeSetup.getSubStrategy();

        double stopLoss =
                researchTechnical.getResearchPrice() > stockPrice.getLow()
                        ? stockPrice.getLow()
                        : stockPrice.getLow() - buffer;

        if (this.isSupport(subStrategy)) {
            stopLoss = stockPriceHelperService.findLowestLow(stockPrice);
        }

        return Math.max(stopLoss, 0.01); // prevent negative or 0 SL
    }

    private double calculateResearchPrice(TradeSetup tradeSetup, StockPrice stockPrice) {
        ResearchTechnical.SubStrategy subStrategy = tradeSetup.getSubStrategy();
        boolean isWeakSupport = this.isWeakSupport(subStrategy);
        boolean isWeakBreakout = this.isWeakBreakout(subStrategy);
        boolean isRedCandle = CandleStickUtils.isRed(stockPrice);

        double researchPrice = stockPrice.getHigh();

        if (isWeakSupport) {
            researchPrice =
                    isRedCandle
                            ? (stockPrice.getLow()
                                    + (stockPrice.getClose() - stockPrice.getLow()) * 0.25)
                            : (stockPrice.getLow()
                                    + (stockPrice.getOpen() - stockPrice.getLow()) * 0.25);
        } else if (isWeakBreakout) {
            researchPrice =
                    isRedCandle
                            ? (stockPrice.getClose()
                                    + (stockPrice.getOpen() - stockPrice.getClose()) * 0.25)
                            : (stockPrice.getClose()
                                    + (stockPrice.getHigh() - stockPrice.getClose()) * 0.25);
        } else {

            researchPrice =
                    isRedCandle
                            ? (stockPrice.getOpen()
                                    + (stockPrice.getHigh() - stockPrice.getOpen()) * 0.50)
                            : (stockPrice.getClose()
                                    + (stockPrice.getHigh() - stockPrice.getClose()) * 0.50);
        }

        return Math.min(formulaService.ceilToNearestQuarter(researchPrice), stockPrice.getHigh());
    }

    private boolean isSupport(ResearchTechnical.SubStrategy subStrategy) {
        return subStrategy == ResearchTechnical.SubStrategy.WEAK_SUPPORT
                || subStrategy == ResearchTechnical.SubStrategy.STRONG_SUPPORT;
    }

    private boolean isWeakSupport(ResearchTechnical.SubStrategy subStrategy) {
        return subStrategy == ResearchTechnical.SubStrategy.WEAK_SUPPORT;
    }

    private boolean isWeakBreakout(ResearchTechnical.SubStrategy subStrategy) {
        return subStrategy == ResearchTechnical.SubStrategy.WEAK_BREAKOUT
                || subStrategy == ResearchTechnical.SubStrategy.WEAK_SWING;
    }

    public double calculateScore(
            Stock stock,
            Timeframe timeframe,
            TradeSetup tradeSetup,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice) {

        double score = 0.0;
        double bullishScore =
                this.calculateBullishScore(stock, timeframe, stockTechnicals, stockPrice);

        ResearchTechnical.Strategy strategy = tradeSetup.getStrategy();
        ResearchTechnical.SubStrategy subStrategy = tradeSetup.getSubStrategy();

        if (strategy == ResearchTechnical.Strategy.SWING) {
            if (subStrategy == ResearchTechnical.SubStrategy.TEMA) {
                if (bullishScore > 0.0) {
                    return RiskFactor.SWING_TEMA + bullishScore;
                }
            } else if (subStrategy == ResearchTechnical.SubStrategy.RM) {
                if (bullishScore > 0.0) {
                    return RiskFactor.SWING_RM + bullishScore;
                }
            }
        }

        if (strategy == ResearchTechnical.Strategy.PRICE) {
            if (subStrategy == ResearchTechnical.SubStrategy.RMAO) {
                if (bullishScore > 0.0) {
                    return RiskFactor.PRICE_RMAO + bullishScore;
                }
            } else if (subStrategy == ResearchTechnical.SubStrategy.SRTF) {
                if (bullishScore > 0.0) {
                    return RiskFactor.PRICE_SRTF + bullishScore;
                }
            } else if (subStrategy == ResearchTechnical.SubStrategy.SRMA) {
                if (bullishScore > 0.0) {
                    return RiskFactor.PRICE_SRMA + bullishScore;
                }
            }
        }

        if (strategy == ResearchTechnical.Strategy.VOLUME) {
            if (subStrategy == ResearchTechnical.SubStrategy.HV) {
                if (bullishScore > 0.0) {
                    return RiskFactor.VOLUME_HV + bullishScore;
                }
            }
        }

        return score;
    }

    private Double calculateBullishScore(
            Stock stock,
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice) {

        double score = 0.0;
        double close = stockPrice.getClose();

        double movingAverageScore =
                this.checkAndIncrease(
                        close, this.getMovingAverage5(timeframe, stockTechnicals), score);
        if (movingAverageScore > score) {
            score = movingAverageScore + 0.00;
        }
        movingAverageScore =
                this.checkAndIncrease(
                        close, this.getMovingAverage10(timeframe, stockTechnicals), score);
        if (movingAverageScore > score) {
            score = movingAverageScore + 0.10;
        }
        movingAverageScore =
                this.checkAndIncrease(
                        close, this.getMovingAverage20(timeframe, stockTechnicals), score);
        if (movingAverageScore > score) {
            score = movingAverageScore + 0.20;
        }
        movingAverageScore =
                this.checkAndIncrease(
                        close, this.getMovingAverage50(timeframe, stockTechnicals), score);
        if (movingAverageScore > score) {
            score = movingAverageScore + 0.30;
        }
        movingAverageScore =
                this.checkAndIncrease(
                        close, this.getMovingAverage100(timeframe, stockTechnicals), score);
        if (movingAverageScore > score) {
            score = movingAverageScore + 0.40;
        }
        movingAverageScore =
                this.checkAndIncrease(
                        close, this.getMovingAverage200(timeframe, stockTechnicals), score);
        if (movingAverageScore > score) {
            score = movingAverageScore + 0.50;
        }

        return score;
    }

    private double getMovingAverage5(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma5();
    }

    private double getMovingAverage10(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma10();
    }

    private double getMovingAverage20(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma20();
    }

    private double getMovingAverage50(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getSma50()
                : stockTechnicals.getEma50();
    }

    private double getMovingAverage100(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getSma100()
                : stockTechnicals.getEma100();
    }

    private double getMovingAverage200(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getSma200()
                : stockTechnicals.getEma200();
    }

    private Double checkAndIncrease(double close, double ema, double score) {

        if (close >= ema && ema > 0.0) {
            score = score + 0.10;
        }

        /*
        if(ema == 0.0){
        	score = score - 0.5;
        }*/

        return score;
    }
}
