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
public class QuarterlySupportResistanceServiceImpl implements QuarterlySupportResistanceService {

    private final OhlcvService ohlcvService;

    private final MiscUtil miscUtil;

    @Override
    public OHLCV supportAndResistance(Stock stock) {

        LocalDate from = miscUtil.previousQuarterFirstDay();
        LocalDate to = miscUtil.previousQuarterLastDay();

        log.info(" {} from {} to {}", stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);
        List<OHLCV> ohlcvList = ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if (!ohlcvList.isEmpty()) {
            OHLCV quarterlyOpen = ohlcvList.get(0);
            OHLCV quarterlyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV quarterlyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV quarterlyClose = ohlcvList.get(ohlcvList.size() - 1);
            long quarterlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            result.setOpen(quarterlyOpen.getOpen());
            result.setHigh(quarterlyHigh.getHigh());
            result.setLow(quarterlyLow.getLow());
            result.setClose(quarterlyClose.getClose());
            result.setVolume(quarterlyVolume);
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
            OHLCV quarterlyOpen = ohlcvList.get(0);
            OHLCV quarterlyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV quarterlyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV quarterlyClose = ohlcvList.get(ohlcvList.size() - 1);
            long quarterlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            if (bhavDate != null) {
                result.setBhavDate(bhavDate);
            }
            if (quarterlyOpen.getOpen() != null) {
                result.setOpen(quarterlyOpen.getOpen());
            }
            if (quarterlyHigh.getHigh() != null) {
                result.setHigh(quarterlyHigh.getHigh());
            }
            if (quarterlyLow.getLow() != null) {
                result.setLow(quarterlyLow.getLow());
            }
            if (quarterlyClose.getClose() != null) {
                result.setClose(quarterlyClose.getClose());
            }

            result.setVolume(quarterlyVolume);
        }

        return result;
    }
}
