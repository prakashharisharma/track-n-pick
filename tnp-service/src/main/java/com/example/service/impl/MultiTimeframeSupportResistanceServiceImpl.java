package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MultiTimeframeSupportResistanceServiceImpl
        implements MultiTimeframeSupportResistanceService {

    private final TimeframeSupportResistanceService timeframeSupportResistanceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final MacdIndicatorService macdIndicatorService;

    @Override
    public boolean isBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Stock stock = stockPrice.getStock();

        // --- Fetch 2 higher timeframe data (e.g., for Daily: Weekly and Monthly) ---
        Timeframe ht1 = timeframe.getHigher(); // e.g., Weekly
        Timeframe ht2 = ht1.getHigher(); // e.g., Monthly

        StockPrice htPrice1 = stockPriceService.get(stock, ht1);
        StockPrice htPrice2 = stockPriceService.get(stock, ht2);
        if (htPrice1 == null || htPrice2 == null) return false;

        StockTechnicals htTech1 = stockTechnicalsService.get(stock, ht1);
        StockTechnicals htTech2 = stockTechnicalsService.get(stock, ht2);
        if (htTech1 == null || htTech2 == null) return false;

        // --- Shift MACD history if HTF session is older ---
        if (!htPrice1.getSessionDate().isBefore(stockPrice.getSessionDate())) {
            shiftMacd(htTech1);
        }

        if (!htPrice2.getSessionDate().isBefore(stockPrice.getSessionDate())) {
            shiftMacd(htTech2);
        }

        // --- Evaluate breakout on both higher timeframes ---
        boolean breakoutHT1 =
                timeframeSupportResistanceService.isBreakout(ht1, stockPrice, stockTechnicals).isBreakout();
        boolean breakoutHT2 =
                timeframeSupportResistanceService.isBreakout(ht2, stockPrice, stockTechnicals).isBreakout();

        // --- Confirm with MACD on either of the higher timeframes ---
        boolean macdConfirmed =
                isMacdConfirmingBreakout(htTech1)
                        || isMacdConfirmingBreakout(htTech2)
                        || isMacdConfirmingBreakout(stockTechnicals);

        return breakoutHT1 && breakoutHT2 && macdConfirmed;
    }

    private void shiftMacd(StockTechnicals tech) {
        tech.setMacd(tech.getPrevMacd());
        tech.setPrevMacd(tech.getPrev2Macd());
        tech.setSignal(tech.getPrevSignal());
        tech.setPrevSignal(tech.getPrev2Signal());
    }

    private boolean isMacdConfirmingBreakout(StockTechnicals st) {

        if (st == null) {
            return false;
        }

        double macd = st.getMacd();
        double signal = st.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd < signal && macdIndicatorService.isHistogramBelowZero(st)) {
            return macdIndicatorService.isMacdIncreased(st)
                    && macdIndicatorService.isSignalDecreased(st)
                    && macdIndicatorService.isHistogramIncreased(st);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(st);
    }

    @Override
    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        return timeframeSupportResistanceService.isNearSupport(
                        timeframe.getHigher(), stockPrice, stockTechnicals)
                && timeframeSupportResistanceService.isNearSupport(
                        timeframe.getHigher().getHigher(), stockPrice, stockTechnicals);
    }

    @Override
    public boolean isBreakdown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        return timeframeSupportResistanceService.isBreakdown(
                        timeframe.getHigher(), stockPrice, stockTechnicals)
                && timeframeSupportResistanceService.isBreakdown(
                        timeframe.getHigher().getHigher(), stockPrice, stockTechnicals);
    }

    @Override
    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        return timeframeSupportResistanceService.isNearResistance(
                        timeframe.getHigher(), stockPrice, stockTechnicals)
                && timeframeSupportResistanceService.isNearResistance(
                        timeframe.getHigher().getHigher(), stockPrice, stockTechnicals);
    }
}
