package com.example.service.impl;

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
public class WeeklySupportResistanceServiceImpl implements WeeklySupportResistanceService {

    private final OhlcvService ohlcvService;
    private final MiscUtil miscUtil;

    @Override
    public OHLCV supportAndResistance(Stock stock) {

        LocalDate from = miscUtil.previousWeekFirstDay();
        LocalDate to = miscUtil.previousWeekLastDay();

        log.info(" {} from {} to {}", stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);
        List<OHLCV> ohlcvList = ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if (!ohlcvList.isEmpty()) {

            OHLCV weeklyOpen = ohlcvList.get(0);
            OHLCV weeklyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV weeklyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV weeklyClose = ohlcvList.get(ohlcvList.size() - 1);
            long weeklyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            result.setOpen(weeklyOpen.getOpen());
            result.setHigh(weeklyHigh.getHigh());
            result.setLow(weeklyLow.getLow());
            result.setClose(weeklyClose.getClose());
            result.setVolume(weeklyVolume);
            result.setBhavDate(bhavDate);
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

            OHLCV weeklyOpen = ohlcvList.get(0);
            OHLCV weeklyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV weeklyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV weeklyClose = ohlcvList.get(ohlcvList.size() - 1);
            long weeklyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            if (bhavDate != null) {
                result.setBhavDate(bhavDate);
            }

            if (weeklyOpen.getOpen() != null) {
                result.setOpen(weeklyOpen.getOpen());
            }
            if (weeklyHigh.getHigh() != null) {
                result.setHigh(weeklyHigh.getHigh());
            }
            if (weeklyLow.getLow() != null) {
                result.setLow(weeklyLow.getLow());
            }
            if (weeklyClose.getClose() != null) {
                result.setClose(weeklyClose.getClose());
            }

            result.setVolume(weeklyVolume);
        }

        return result;
    }
}
