package com.example.service.calc.impl;

import com.example.dto.OHLCV;
import com.example.service.calc.VolumeAverageCalculatorService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VolumeAverageCalculatorServiceImpl implements VolumeAverageCalculatorService {

    @Autowired private FormulaService formulaService;

    @Autowired private MiscUtil miscUtil;

    @Override
    public List<Long> calculate(List<OHLCV> ohlcvList, int days) {

        List<Long> smaList = new ArrayList<>(ohlcvList.size());

        for (int i = 0; i < ohlcvList.size(); i++) {

            if (i < days - 1) {
                smaList.add(i, 0l);
            } else if (i == days - 1) {
                long sma = this.calculateSimpleAverage(ohlcvList, i, days);
                smaList.add(i, sma);
            } else {

                long sma = this.calculateSimpleAverage(ohlcvList, i, days);
                smaList.add(i, sma);
            }
        }
        return smaList;
    }

    @Override
    public Long calculate(OHLCV ohlcv, long prevSmoothedAverageVolume, int days) {
        return formulaService.calculateSmoothedMovingAverage(
                prevSmoothedAverageVolume, ohlcv.getVolume(), days);
    }

    private long calculateSimpleAverage(List<OHLCV> ohlcvList, int index, int days) {

        long sum = 0l;

        for (int i = index; i >= index - days + 1; i--) {
            sum = sum + ohlcvList.get(i).getVolume();
        }

        return sum / days;
    }
}
