package com.example.service.calc.impl;

import com.example.data.storage.documents.AverageDirectionalIndex;
import com.example.dto.common.OHLCV;
import com.example.service.calc.AverageDirectionalIndexCalculatorService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AverageDirectionalIndexServiceCalculatorImpl
        implements AverageDirectionalIndexCalculatorService {

    @Autowired private FormulaService formulaService;

    @Autowired private MiscUtil miscUtil;

    @Override
    public List<AverageDirectionalIndex> calculate(List<OHLCV> ohlcvList) {

        // long startTime = System.currentTimeMillis();

        List<Double> plusDMList = new ArrayList<>(ohlcvList.size());

        List<Double> minusDMList = new ArrayList<>(ohlcvList.size());

        List<Double> trList = new ArrayList<>(ohlcvList.size());

        List<Double> atrList = new ArrayList<>(ohlcvList.size());

        List<Double> smoothedPlusDMList = new ArrayList<>(ohlcvList.size());

        List<Double> smoothedMinusDMList = new ArrayList<>(ohlcvList.size());

        List<Double> plusDIList = new ArrayList<>(ohlcvList.size());

        List<Double> minusDIList = new ArrayList<>(ohlcvList.size());

        List<Double> dxList = new ArrayList<>(ohlcvList.size());

        List<Double> adxList = new ArrayList<>(ohlcvList.size());

        plusDMList.add(0, 0.00);
        minusDMList.add(0, 0.00);
        trList.add(0, 0.00);
        atrList.add(0, 0.00);
        smoothedPlusDMList.add(0, 00.00);
        smoothedMinusDMList.add(0, 0.00);
        plusDIList.add(0, 0.00);
        minusDIList.add(0, 0.00);
        dxList.add(0, 0.00);
        adxList.add(0, 0.00);

        for (int i = 1; i < ohlcvList.size(); i++) {

            double plusDM =
                    formulaService.calculatePlusDM(
                            ohlcvList.get(i).getHigh(),
                            ohlcvList.get(i - 1).getHigh(),
                            ohlcvList.get(i).getLow(),
                            ohlcvList.get(i - 1).getLow());
            plusDMList.add(i, plusDM);
            double minusDM =
                    formulaService.calculateMinusDM(
                            ohlcvList.get(i).getHigh(),
                            ohlcvList.get(i - 1).getHigh(),
                            ohlcvList.get(i).getLow(),
                            ohlcvList.get(i - 1).getLow());
            minusDMList.add(i, minusDM);
            double tr =
                    formulaService.calculateTR(
                            ohlcvList.get(i).getHigh(),
                            ohlcvList.get(i).getLow(),
                            ohlcvList.get(i - 1).getClose());
            trList.add(i, tr);

            if (i <= 13) {
                atrList.add(i, 0.00);
                smoothedPlusDMList.add(i, 0.00);
                smoothedMinusDMList.add(i, 0.00);
                plusDIList.add(i, 0.00);
                minusDIList.add(i, 0.00);
                dxList.add(i, 0.00);
                adxList.add(i, 0.00);
            }

            if (i > 13) {
                double atr = 0.00;
                double smoothedPlusDM = 0.00;
                double smoothedMinusDM = 0.00;
                if (i == 14) {
                    atr = this.calculateSimpleAverage(trList, 1, 14);

                    smoothedPlusDM = this.calculateSimpleAverage(plusDMList, 1, 14);

                    smoothedMinusDM = this.calculateSimpleAverage(minusDMList, 1, 14);

                } else {

                    atr =
                            formulaService.calculateSmoothedMovingAverage(
                                    atrList.get(i - 1), trList.get(i), 14);

                    smoothedPlusDM =
                            formulaService.calculateSmoothedMovingAverage(
                                    smoothedPlusDMList.get(i - 1), plusDMList.get(i), 14);

                    smoothedMinusDM =
                            formulaService.calculateSmoothedMovingAverage(
                                    smoothedMinusDMList.get(i - 1), minusDMList.get(i), 14);
                }

                atrList.add(i, atr);
                smoothedMinusDMList.add(i, smoothedMinusDM);
                smoothedPlusDMList.add(i, smoothedPlusDM);

                double plusDi = formulaService.calculatePlusDi(smoothedPlusDM, atr);
                // System.out.println("plusDi " + plusDi);
                plusDIList.add(i, plusDi);
                double minusDi = formulaService.calculateMinusDi(smoothedMinusDM, atr);
                minusDIList.add(i, minusDi);
                // System.out.println("minusDi " + minusDi);
                double dx = formulaService.calculateDX(plusDi, minusDi);
                dxList.add(i, dx);
                adxList.add(i, 0.00);

                // log.debug("atr: {} plusDi: {} minusDi:{} dx: {}", atr, plusDi, minusDi, dx );
            }

            if (i > 26) {
                double adx = 0.00;

                if (i == 27) {
                    adx = this.calculateSimpleAverage(dxList, 14, 27, 14);

                } else {
                    adx =
                            formulaService.calculateSmoothedMovingAverage(
                                    adxList.get(i - 1), dxList.get(i), 14);
                }
                adxList.add(i, adx);
            }
        }

        // long endTime = System.currentTimeMillis();

        // log.info("Time took to calculate EMA {}ms",(endTime - startTime));
        return this.mapResult(
                atrList,
                smoothedPlusDMList,
                smoothedMinusDMList,
                plusDIList,
                minusDIList,
                dxList,
                adxList);
    }

    @Override
    public AverageDirectionalIndex calculate(
            List<OHLCV> ohlcvList, AverageDirectionalIndex prevAverageDirectionalIndex) {

        if (ohlcvList.size() < 2) {
            throw new IllegalArgumentException(
                    "Invalid OHLCV list, Please pass ohlcvList[0] - prevDay and ohlcvList[1] -"
                            + " currentDay");
        }

        int i = 1;

        double plusDM =
                formulaService.calculatePlusDM(
                        ohlcvList.get(i).getHigh(),
                        ohlcvList.get(i - 1).getHigh(),
                        ohlcvList.get(i).getLow(),
                        ohlcvList.get(i - 1).getLow());
        double minusDM =
                formulaService.calculateMinusDM(
                        ohlcvList.get(i).getHigh(),
                        ohlcvList.get(i - 1).getHigh(),
                        ohlcvList.get(i).getLow(),
                        ohlcvList.get(i - 1).getLow());
        double tr =
                formulaService.calculateTR(
                        ohlcvList.get(i).getHigh(),
                        ohlcvList.get(i).getLow(),
                        ohlcvList.get(i - 1).getClose());

        double atr =
                formulaService.calculateSmoothedMovingAverage(
                        prevAverageDirectionalIndex.getAtr(), tr, 14);
        double smoothedPlusDM =
                formulaService.calculateSmoothedMovingAverage(
                        prevAverageDirectionalIndex.getSmoothedPlusDM(), plusDM, 14);
        double smoothedMinusDM =
                formulaService.calculateSmoothedMovingAverage(
                        prevAverageDirectionalIndex.getSmoothedMinusDM(), minusDM, 14);

        double plusDi = formulaService.calculatePlusDi(smoothedPlusDM, atr);
        double minusDi = formulaService.calculateMinusDi(smoothedMinusDM, atr);
        double dx = formulaService.calculateDX(plusDi, minusDi);
        double adx =
                formulaService.calculateSmoothedMovingAverage(
                        prevAverageDirectionalIndex.getAdx(), dx, 14);

        return new AverageDirectionalIndex(
                atr,
                smoothedPlusDM,
                smoothedMinusDM,
                plusDi,
                minusDi,
                dx,
                miscUtil.formatDouble(adx, "00"));
    }

    private List<AverageDirectionalIndex> mapResult(
            List<Double> atrList,
            List<Double> smoothedPlusDMList,
            List<Double> smoothedMinusDMList,
            List<Double> plusDiList,
            List<Double> minusDIList,
            List<Double> dxList,
            List<Double> adxList) {
        List<AverageDirectionalIndex> averageDirectionalIndexList = new ArrayList<>();
        for (int i = 0; i < atrList.size(); i++) {
            averageDirectionalIndexList.add(
                    new AverageDirectionalIndex(
                            atrList.get(i),
                            smoothedPlusDMList.get(i),
                            smoothedMinusDMList.get(i),
                            plusDiList.get(i),
                            minusDIList.get(i),
                            dxList.get(i),
                            miscUtil.formatDouble(adxList.get(i), "00")));
        }
        return averageDirectionalIndexList;
    }

    private double calculateSimpleAverage(List<Double> ohlcvList, int startIndex, int days) {

        double sum = 0.00;

        for (int i = startIndex; i < days + 1; i++) {
            sum = sum + ohlcvList.get(i);
        }

        return sum / days;
    }

    private double calculateSimpleAverage(
            List<Double> ohlcvList, int startIndex, int endIndex, int days) {

        double sum = 0.00;

        for (int i = startIndex; i <= endIndex; i++) {

            sum = sum + ohlcvList.get(i);
        }

        return sum / days;
    }
}
