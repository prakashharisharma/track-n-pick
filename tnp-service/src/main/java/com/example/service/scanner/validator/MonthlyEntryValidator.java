package com.example.service.scanner.validator;

import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyEntryValidator {

    private final MiscUtil miscUtil;
    private final CalendarService calendarService;
    private final FormulaService formulaService;
    private final UpdatePriceService updatePriceService;
    private final UpdateTechnicalsService updateTechnicalsService;

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final FundamentalResearchService fundamentalResearchService;

    private final MonthlySupportResistanceService monthlySupportResistanceService;

    public boolean isValid(
            StockPrice stockPrice, StockTechnicals stockTechnicals, boolean skipDojiCheck) {

        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();

        boolean isMAAligned =
                (ema5 > ema20 && ema20 > 0.00) || (ema20 > ema50 && ema5 > ema50 && ema50 > 0.0);

        if (isMAAligned) {
            // boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice) && (ema5 < ema20);
            double candleSize =
                    formulaService.calculateChangePercentage(
                            stockPrice.getPrevClose(), stockPrice.getClose());
            double ema5Distance =
                    formulaService.calculateChangePercentage(ema5, stockPrice.getClose());
            if ((candleSize <= 30.0 || ema5Distance <= 25.0) && stockTechnicals.getRsi() < 80.0) {
                // if (skipDojiCheck || !isDoji) {
                boolean isUpperWickDominant =
                        CandleStickUtils.isUpperWickDominant(stockPrice)
                                && stockPrice.getOpen() > ema5
                                && stockPrice.getClose() > ema5;
                if (!isUpperWickDominant) {
                    double prevEma5 =
                            stockTechnicals.getPrevEma5() != null
                                    ? stockTechnicals.getPrevEma5()
                                    : 0.0;
                    boolean isPrevUpperWickDominant =
                            CandleStickUtils.isPrevUpperWickDominant(stockPrice)
                                    && stockPrice.getPrevOpen() > prevEma5
                                    && stockPrice.getPrevClose() > prevEma5
                                    && CandleStickUtils.isRed(stockPrice);

                    if (!isPrevUpperWickDominant) {
                        boolean isHighRejectedEma5 =
                                stockPrice.getOpen() < ema5
                                        && stockPrice.getHigh() > ema5
                                        && stockPrice.getClose() < ema5;
                        if (!isHighRejectedEma5) {
                            return true;
                        }
                    }
                }
                // }
            }
        }
        return false;
    }
}
