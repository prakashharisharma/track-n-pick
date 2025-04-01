package com.example.service.calc.impl;

import com.example.data.storage.documents.OnBalanceVolume;
import com.example.dto.OHLCV;
import com.example.service.calc.OnBalanceVolumeCalculatorService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OnBalanceVolumeCalculatorServiceImpl implements OnBalanceVolumeCalculatorService {

    private static int SMA_DAYS = 9;

    @Autowired private FormulaService formulaService;

    @Autowired private MiscUtil miscUtil;

    @Override
    public List<OnBalanceVolume> calculate(List<OHLCV> ohlcvList) {
        List<OnBalanceVolume> onBalanceVolumeList = new ArrayList<>(ohlcvList.size());
        long obv = 0; // Initial OBV
        long obvAverage = 0;
        OnBalanceVolume onBalanceVolume = new OnBalanceVolume(0l, 0l);
        onBalanceVolumeList.add(onBalanceVolume);
        for (int i = 1; i < ohlcvList.size(); i++) {
            obv = this.calculateObv(ohlcvList.get(i), ohlcvList.get(i - 1), obv);
            onBalanceVolumeList.add(i, new OnBalanceVolume(obv, 0l));
            // onBalanceVolumeList.add(i,onBalanceVolume);
            if (i < SMA_DAYS - 1) {
                obvAverage = 0;
            } else if (i == SMA_DAYS - 1) {
                obvAverage = this.calculateSimpleAverage(onBalanceVolumeList, i, SMA_DAYS);
            } else {
                obvAverage = this.calculateSimpleAverage(onBalanceVolumeList, i, SMA_DAYS);
                // obvAverage =
                // formulaService.calculateSmoothedMovingAverage(onBalanceVolumeList.get(i-1).getAverage(),obv, SMMA_DAYS);
                // obvAverage = this.calculateSimpleAverage(onBalanceVolumeList,obv, i-SMA_DAYS+1,
                // SMA_DAYS);
            }

            onBalanceVolume = new OnBalanceVolume(obv, obvAverage);
            // System.out.println(LocalDate.ofInstant(ohlcvList.get(i).getBhavDate(),
            // ZoneOffset.UTC) +" : "+onBalanceVolume);
            onBalanceVolumeList.add(i, onBalanceVolume);
        }

        return onBalanceVolumeList;
    }

    @Override
    public OnBalanceVolume calculate(List<OHLCV> ohlcvList, OnBalanceVolume prevOnBalanceVolume) {

        if (ohlcvList.size() < 2) {
            throw new IllegalArgumentException(
                    "Invalid OHLCV list, Please pass ohlcvList[0] - prevDay and ohlcvList[1] -"
                            + " currentDay");
        }

        OHLCV currentSession = ohlcvList.get(ohlcvList.size() - 1);
        OHLCV previousSession = ohlcvList.get(ohlcvList.size() - 2);

        long obv = this.calculateObv(currentSession, previousSession, prevOnBalanceVolume.getObv());

        long average =
                formulaService.calculateSmoothedMovingAverage(
                        obv, prevOnBalanceVolume.getAverage(), SMA_DAYS);

        return new OnBalanceVolume(obv, average);
    }

    private long calculateObv(OHLCV currentSessionOhlcv, OHLCV previousSessionOhlcv, long obv) {

        if (currentSessionOhlcv.getClose() > previousSessionOhlcv.getClose()) {

            return obv + currentSessionOhlcv.getVolume();
        } else if (currentSessionOhlcv.getClose() < previousSessionOhlcv.getClose()) {
            return obv - currentSessionOhlcv.getVolume();
        }

        return obv;
    }

    private long calculateSimpleAverage(List<OnBalanceVolume> ohlcvList, int index, int days) {

        long sum = 0l;

        for (int i = index; i >= index - days + 1; i--) {
            sum = sum + ohlcvList.get(i).getObv();
        }

        return sum / days;
    }
}
