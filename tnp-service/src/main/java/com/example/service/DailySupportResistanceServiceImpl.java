package com.example.service;

import com.example.dto.common.OHLCV;
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
@Service
@RequiredArgsConstructor
public class DailySupportResistanceServiceImpl implements DailySupportResistanceService {

    private final OhlcvService ohlcvService;
    private final MiscUtil miscUtil;

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

            OHLCV dailyyOpen = ohlcvList.get(0);
            OHLCV dailyyHigh =
                    Collections.max(
                            ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getHigh()));
            OHLCV dailyLow =
                    Collections.min(ohlcvList, Comparator.comparingDouble(ohlcv -> ohlcv.getLow()));
            OHLCV dailyClose = ohlcvList.get(ohlcvList.size() - 1);
            long dailyVolume = ohlcvList.stream().mapToLong(OHLCV::getVolume).sum();
            Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

            if (bhavDate != null) {
                result.setBhavDate(bhavDate);
            }

            if (dailyyOpen.getOpen() != null) {
                result.setOpen(dailyyOpen.getOpen());
            }
            if (dailyyHigh.getHigh() != null) {
                result.setHigh(dailyyHigh.getHigh());
            }
            if (dailyLow.getLow() != null) {
                result.setLow(dailyLow.getLow());
            }
            if (dailyClose.getClose() != null) {
                result.setClose(dailyClose.getClose());
            }

            result.setVolume(dailyVolume);
        }

        return result;
    }
}
