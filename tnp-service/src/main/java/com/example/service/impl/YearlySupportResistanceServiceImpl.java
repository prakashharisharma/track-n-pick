package com.example.service.impl;

import com.example.data.storage.documents.result.HighLowResult;
import com.example.data.storage.repo.PriceTemplate;
import com.example.data.transactional.entities.Stock;
import com.example.dto.common.OHLCV;
import com.example.service.*;
import com.example.util.MiscUtil;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class YearlySupportResistanceServiceImpl implements YearlySupportResistanceService {

    private final PriceTemplate priceTemplate;

    private final OhlcvService ohlcvService;

    private final MiscUtil miscUtil;

    @Override
    public OHLCV supportAndResistance(Stock stock) {

        LocalDate from = miscUtil.currentYearFirstDay().minusYears(1);
        LocalDate to = miscUtil.currentYearFirstDay().minusDays(1);

        log.info(" {} from {} to {}", stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);

        List<OHLCV> ohlcvList = ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if (!ohlcvList.isEmpty()) {
            OHLCV yearlyOpen = ohlcvList.get(0);
            OHLCV yearlyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV yearlyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV yearlyClose = ohlcvList.get(ohlcvList.size() - 1);
            long yearlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            // Instant bhavDate = ohlcvList.get(ohlcvList.size()-1).getBhavDate();

            result.setOpen(yearlyOpen.getOpen());
            result.setHigh(yearlyHigh.getHigh());
            result.setLow(yearlyLow.getLow());
            result.setClose(yearlyClose.getClose());
            result.setVolume(yearlyVolume);
            result.setBhavDate(to.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
        }

        return result;
    }

    @Override
    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to) {

        log.info(" {} from {} to {}", nseSymbol, from, to);

        OHLCV result = new OHLCV();
        result.setOpen(0.0);
        result.setHigh(0.0);
        result.setLow(0.0);
        result.setClose(0.0);
        result.setVolume(0l);
        result.setBhavDate(to.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
        List<OHLCV> ohlcvList = new ArrayList<>();
        try {
            ohlcvList = ohlcvService.fetch(nseSymbol, from, to);
        } catch (Exception e) {
            log.error("{} An error occurred while fetching bhav", nseSymbol, e);
        }

        if (!ohlcvList.isEmpty()) {
            OHLCV yearlyOpen = ohlcvList.get(0);
            OHLCV yearlyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV yearlyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV yearlyClose = ohlcvList.get(ohlcvList.size() - 1);
            long yearlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            if (bhavDate != null) {
                result.setBhavDate(bhavDate);
            }
            if (yearlyOpen.getOpen() != null) {
                result.setOpen(yearlyOpen.getOpen());
            }
            if (yearlyHigh.getHigh() != null) {
                result.setHigh(yearlyHigh.getHigh());
            }
            if (yearlyLow.getLow() != null) {
                result.setLow(yearlyLow.getLow());
            }
            if (yearlyClose.getClose() != null) {
                result.setClose(yearlyClose.getClose());
            }

            result.setVolume(yearlyVolume);
        }

        return result;
    }

    @Override
    public OHLCV supportAndResistance(String nseSymbol, LocalDate onDate) {

        LocalDate to = onDate;

        LocalDate from = to.minusYears(1);

        log.info(" {} from {} to {}", nseSymbol, from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);

        HighLowResult highLowResult = priceTemplate.getHighLowByDate(nseSymbol, from, to);

        if (highLowResult != null && !highLowResult.get_id().equalsIgnoreCase("NO_DATA_FOUND")) {
            result.setHigh(highLowResult.getHigh());
            result.setLow(highLowResult.getLow());
        }

        return result;
    }
}
