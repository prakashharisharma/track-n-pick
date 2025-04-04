package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.AdxIndicatorService;
import com.example.service.SupportResistanceConfirmationService;
import com.example.service.utils.CandleStickUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SupportResistanceConfirmationServiceImpl
        implements SupportResistanceConfirmationService {

    private final AdxIndicatorService adxIndicatorService;

    @Override
    public boolean isSupportConfirmed(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double supportLevel) {

        boolean priceRejectionWithLowerWick =
                stockPrice.getLow() <= supportLevel && stockPrice.getClose() > supportLevel;

        boolean isWithin1Percent =
                stockPrice.getLow() > supportLevel && stockPrice.getLow() <= supportLevel * 0.51;

        double lowerWickSize = CandleStickUtils.lowerWickSize(stockPrice);
        double upperWickSize = CandleStickUtils.upperWickSize(stockPrice);
        double bodySize = CandleStickUtils.bodySize(stockPrice);

        boolean isLowerWickSignificant =
                lowerWickSize > (0.50 * bodySize) && lowerWickSize > upperWickSize;

        boolean strongClose =
                (stockPrice.getClose() - stockPrice.getLow())
                        > (0.5 * (stockPrice.getHigh() - stockPrice.getLow()));

        boolean priceRejection =
                priceRejectionWithLowerWick
                        || (isWithin1Percent && (isLowerWickSignificant || strongClose));
        boolean adxStrong = stockTechnicals.getAdx() > 20;
        boolean dmiPositive = stockTechnicals.getPlusDi() > stockTechnicals.getMinusDi();

        double atr = stockTechnicals.getAtr();
        boolean atrSupportConfirmed = (stockPrice.getLow() - supportLevel) > (-1.5 * atr);

        if (priceRejection) {
            log.info(
                    "[{}] Support confirmed: Price rejection at support {} with bullish pattern",
                    stockPrice.getStock().getNseSymbol(),
                    supportLevel);
            return true;
        }

        if (adxStrong && dmiPositive) {
            log.info(
                    "[{}] Support confirmed: ADX strong ({}) & DMI+ leading",
                    stockPrice.getStock().getNseSymbol(),
                    stockTechnicals.getAdx());
            return true;
        }

        if (atrSupportConfirmed) {
            log.info(
                    "[{}] Support confirmed: Price held within 1.5x ATR of support ({})",
                    stockPrice.getStock().getNseSymbol(),
                    atr);
            return true;
        }

        log.info(
                "[{}] No support confirmation at {}",
                stockPrice.getStock().getNseSymbol(),
                supportLevel);
        return false;
    }

    @Override
    public boolean isResistanceConfirmed(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double resistanceLevel) {
        boolean priceRejectionWithUpperWick =
                stockPrice.getHigh() >= resistanceLevel && stockPrice.getClose() < resistanceLevel;

        boolean isWithin1Percent =
                stockPrice.getHigh() < resistanceLevel * 0.51
                        && stockPrice.getHigh() >= resistanceLevel;

        double lowerWickSize = CandleStickUtils.lowerWickSize(stockPrice);
        double upperWickSize = CandleStickUtils.upperWickSize(stockPrice);
        double bodySize = CandleStickUtils.bodySize(stockPrice);

        boolean isUpperWickSignificant =
                upperWickSize > (0.50 * bodySize) && upperWickSize > lowerWickSize;

        boolean weakClose =
                (stockPrice.getHigh() - stockPrice.getClose())
                        > (0.5 * (stockPrice.getHigh() - stockPrice.getLow()));

        boolean priceRejection =
                priceRejectionWithUpperWick
                        || (isWithin1Percent && (isUpperWickSignificant || weakClose));

        boolean adxStrong = stockTechnicals.getAdx() > 20;
        boolean dmiNegative = stockTechnicals.getMinusDi() > stockTechnicals.getPlusDi();

        double atr = stockTechnicals.getAtr();
        boolean atrResistanceConfirmed = (stockPrice.getHigh() - resistanceLevel) < (1.5 * atr);

        if (priceRejection) {
            log.info(
                    "[{}] Resistance confirmed: Price rejection at resistance {} with bearish"
                            + " pattern",
                    stockPrice.getStock().getNseSymbol(),
                    resistanceLevel);
            return true;
        }

        if (adxStrong && dmiNegative) {
            log.info(
                    "[{}] Resistance confirmed: ADX strong ({}) & DMI- leading",
                    stockPrice.getStock().getNseSymbol(),
                    stockTechnicals.getAdx());
            return true;
        }

        if (atrResistanceConfirmed) {
            log.info(
                    "[{}] Resistance confirmed: Price held within 1.5x ATR of resistance ({})",
                    stockPrice.getStock().getNseSymbol(),
                    atr);
            return true;
        }

        log.info(
                "[{}] No resistance confirmation at {}",
                stockPrice.getStock().getNseSymbol(),
                resistanceLevel);
        return false;
    }
}
