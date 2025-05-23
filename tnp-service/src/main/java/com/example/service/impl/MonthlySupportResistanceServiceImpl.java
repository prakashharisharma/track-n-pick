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
public class MonthlySupportResistanceServiceImpl implements MonthlySupportResistanceService {

    private final OhlcvService ohlcvService;

    private final MiscUtil miscUtil;

    @Override
    public OHLCV supportAndResistance(Stock stock) {

        LocalDate from = miscUtil.previousMonthFirstDay();
        LocalDate to = miscUtil.previousMonthLastDay();

        log.info("{} from {} to {}", stock.getNseSymbol(), from, to);

        OHLCV result = new OHLCV();
        result.setHigh(0.0);
        result.setLow(0.0);
        List<OHLCV> ohlcvList = ohlcvService.fetch(stock.getNseSymbol(), from, to);

        if (!ohlcvList.isEmpty()) {
            OHLCV monthlyOpen = ohlcvList.get(0);
            OHLCV monthlyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV monthlyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV monthlyClose = ohlcvList.get(ohlcvList.size() - 1);
            long monthlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            result.setOpen(monthlyOpen.getOpen());
            result.setHigh(monthlyHigh.getHigh());
            result.setLow(monthlyLow.getLow());
            result.setClose(monthlyClose.getClose());
            result.setVolume(monthlyVolume);
            result.setBhavDate(bhavDate);
        }

        return result;
    }

    @Override
    public OHLCV supportAndResistance(String nseSymbol, LocalDate from, LocalDate to) {

        log.info("{} from {} to {}", nseSymbol, from, to);

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
            OHLCV monthlyOpen = ohlcvList.get(0);
            OHLCV monthlyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV monthlyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV monthlyClose = ohlcvList.get(ohlcvList.size() - 1);
            long monthlyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            if (bhavDate != null) {
                result.setBhavDate(bhavDate);
            }

            if (monthlyOpen.getOpen() != null) {
                result.setOpen(monthlyOpen.getOpen());
            }
            if (monthlyHigh.getHigh() != null) {
                result.setHigh(monthlyHigh.getHigh());
            }
            if (monthlyLow.getLow() != null) {
                result.setLow(monthlyLow.getLow());
            }
            if (monthlyClose.getClose() != null) {
                result.setClose(monthlyClose.getClose());
            }

            result.setVolume(monthlyVolume);
        }

        return result;
    }
}
