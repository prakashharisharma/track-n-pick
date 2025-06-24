package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.EvaluationLog;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.CrossOverUtil;
import com.example.service.EvaluationLogService;
import com.example.service.RsiIndicatorService;
import com.example.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RsiIndicatorServiceImpl implements RsiIndicatorService {

    private static double DEFAULT_OVERSOLD = 40.0;

    private static double DEFAULT_OVERBOUGHT = 70.0;

    private final EvaluationLogService evaluationLogService;

    @Override
    public boolean isBullish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {

            evaluationLogService.add(
                    stockTechnicals,
                    EvaluationLog.Type.NEUTRAL,
                    "isBullish: RSI or PrevRSI is null → false");

            return false;
        }

        double rsi = stockTechnicals.getRsi();
        double prevRsi = stockTechnicals.getPrevRsi();

        if (rsi > prevRsi) {
            boolean isRsiEnteredBullishZone =
                    CrossOverUtil.isFastCrossesAboveSlow(
                            prevRsi, DEFAULT_OVERSOLD,
                            rsi, DEFAULT_OVERSOLD);

            boolean isRsiSustainedAboveOversold = Math.ceil(prevRsi) > DEFAULT_OVERSOLD;

            evaluationLogService.add(
                    stockTechnicals,
                    (isRsiEnteredBullishZone || isRsiSustainedAboveOversold)
                            ? EvaluationLog.Type.POSITIVE
                            : EvaluationLog.Type.NEUTRAL,
                    StringUtils.format(
                            "isBullish: RSI rising → rsi:{} > prevRsi:{} | enteredBullishZone:{} |"
                                    + " sustainedAbove:{}",
                            rsi,
                            prevRsi,
                            isRsiEnteredBullishZone,
                            isRsiSustainedAboveOversold));

            return isRsiEnteredBullishZone || isRsiSustainedAboveOversold;
        }

        evaluationLogService.add(
                stockTechnicals,
                EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        "isBullish: RSI not rising → rsi:{} <= prevRsi:{} → false", rsi, prevRsi));

        return false;
    }

    @Override
    public boolean isOverSold(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        return stockTechnicals.getPrevRsi() <= DEFAULT_OVERSOLD
                || stockTechnicals.getRsi() <= DEFAULT_OVERSOLD;
    }

    @Override
    public boolean isBearish(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        double overBoughtFactor = this.getOverBoughtFactor(stockTechnicals);
        if (stockTechnicals.getRsi() < stockTechnicals.getPrevRsi()) {
            boolean isRsiEnteredBearishZone =
                    CrossOverUtil.isSlowCrossesBelowFast(
                            stockTechnicals.getPrevRsi(), overBoughtFactor,
                            stockTechnicals.getRsi(), overBoughtFactor);

            return isRsiEnteredBearishZone
                    || Math.ceil(stockTechnicals.getPrevRsi()) < overBoughtFactor;
        }

        return false;
    }

    @Override
    public boolean isOverBought(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        double overBoughtFactor = this.getOverBoughtFactor(stockTechnicals);

        return stockTechnicals.getPrevRsi() >= overBoughtFactor
                || stockTechnicals.getRsi() >= overBoughtFactor;
    }

    private double getOverBoughtFactor(StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockTechnicals.getTimeframe();

        return switch (timeframe) {
            case WEEKLY -> 75.0;
            case MONTHLY -> 80.0;
            default -> 70.0; // fallback value if needed
        };
    }

    @Override
    public double rsi(StockTechnicals stockTechnicals) {
        return stockTechnicals.getRsi();
    }

    @Override
    public boolean isIncreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }
        return stockTechnicals.getRsi() > stockTechnicals.getPrevRsi();
    }

    @Override
    public boolean isDecreasing(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null
                || stockTechnicals.getRsi() == null
                || stockTechnicals.getPrevRsi() == null) {
            return false;
        }

        return stockTechnicals.getRsi() < stockTechnicals.getPrevRsi();
    }
}
