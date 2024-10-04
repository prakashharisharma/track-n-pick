package com.example.service.impl;

import com.example.service.RelativeStrengthIndexService;
import com.example.storage.model.AverageDirectionalIndex;
import com.example.storage.model.OHLCV;
import com.example.storage.model.RelativeStrengthIndex;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RelativeStrengthIndexServiceImpl implements RelativeStrengthIndexService {

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private MiscUtil miscUtil;

    @Override
    public List<RelativeStrengthIndex> calculate(List<OHLCV> ohlcvList) {

            long startTime = System.currentTimeMillis();

            List<Double> upMoves = new ArrayList<>(ohlcvList.size());
            List<Double> downMoves = new ArrayList<>(ohlcvList.size());
            List<Double> avgUps = new ArrayList<>(ohlcvList.size());
            List<Double> avgDowns = new ArrayList<>(ohlcvList.size());
            List<Double> rsList = new ArrayList<>(ohlcvList.size());
            List<Double> rsiList = new ArrayList<>(ohlcvList.size());

        upMoves.add(0, 0.00);
        downMoves.add(0, 0.00);
        avgUps.add(0, 0.00);
        avgDowns.add(0, 0.00);
        rsList.add(0, 0.00);
        rsiList.add(0, 0.00);

            for(int i=0; i < ohlcvList.size(); i++){

                if(i>0) {

                    //double upMove = (ohlcvList.get(i).getClose() - ohlcvList.get(i-1).getClose() ) > 0 ? (ohlcvList.get(i).getClose() - ohlcvList.get(i-1).getClose() ) : 0;
                    //double downMove = (ohlcvList.get(i-1).getClose() - ohlcvList.get(i).getClose() ) > 0 ? (ohlcvList.get(i-1).getClose() - ohlcvList.get(i).getClose() ) : 0;
                    double upMove = this.calculateUpMove(ohlcvList.get(i).getClose(), ohlcvList.get(i-1).getClose());
                    double downMove = this.calculateDownMove(ohlcvList.get(i).getClose(), ohlcvList.get(i-1).getClose());

                    upMoves.add(i, upMove);
                    downMoves.add(i, downMove);

                    if(i < 14){
                        avgUps.add(i, 0.00);
                        avgDowns.add(i, 0.00);
                        rsList.add(i, 0.00);
                        rsiList.add(i, 0.00);
                    }

                    if(i==14){

                        double sumU=0.00;
                        double sumD=0.00;

                        for(int j=1; j<=14; j++){

                            sumU = sumU + upMoves.get(j);
                            sumD = sumD + downMoves.get(j);
                        }

                        avgUps.add(i, sumU/14);
                        avgDowns.add(i, sumD/14);

                    }else if(i > 14){

                        double smoothedAvgUpMove = formulaService.calculateSmoothedMovingAverage(avgUps.get(i-1), upMoves.get(i), 14);

                        double smoothedAvgDownMove = formulaService.calculateSmoothedMovingAverage(avgDowns.get(i-1), downMoves.get(i), 14);

                        avgUps.add(i, smoothedAvgUpMove);
                        avgDowns.add(i, smoothedAvgDownMove);

                    }

                    double rs = formulaService.calculateRs( avgUps.get(i) , avgDowns.get(i));
                    double rsi = formulaService.calculateRsi(rs);

                    rsList.add(i, rs);
                    rsiList.add(i, rsi);
                }

            }

            long endTime = System.currentTimeMillis();

            log.info("Time took to calculate {}ms", (endTime - startTime));

           // int resultIndex= ohlcvList.size()-1;
            return this.mapResult(avgUps, avgDowns, rsList, rsiList);
           // return new RelativeStrengthIndex( avgUps.get(resultIndex), avgDowns.get(resultIndex),rsList.get(resultIndex), rsiList.get(resultIndex) );
        }

    @Override
    public RelativeStrengthIndex calculate(List<OHLCV> ohlcvList, RelativeStrengthIndex prevRelativeStrengthIndex) {

        if(ohlcvList.size()!=2){
            throw new IllegalArgumentException("Invalid OHLCV list, Please pass ohlcvList[0] - prevDay and ohlcvList[1] - currentDay");
        }

        double upMove = this.calculateUpMove(ohlcvList.get(1).getClose(), ohlcvList.get(0).getClose());
        double downMove = this.calculateDownMove(ohlcvList.get(1).getClose(), ohlcvList.get(0).getClose());
        double avgUpMove = formulaService.calculateSmoothedMovingAverage(prevRelativeStrengthIndex.getAvgUpMove(), upMove, 14);
        double avgDownMove = formulaService.calculateSmoothedMovingAverage(prevRelativeStrengthIndex.getAvgDownMove(), downMove, 14);
        double rs = formulaService.calculateRs(avgUpMove , avgDownMove);
        double rsi = formulaService.calculateRsi(rs);
        return new RelativeStrengthIndex(avgUpMove, avgDownMove, rs, miscUtil.formatDouble(rsi,"00"));
    }

    private List<RelativeStrengthIndex> mapResult(List<Double> avgUps, List<Double> avgDowns, List<Double> rsList, List<Double> rsiList){
        List<RelativeStrengthIndex> averageDirectionalIndexList = new ArrayList<>();
        for(int i = 0; i < avgUps.size(); i++){
            averageDirectionalIndexList.add(new RelativeStrengthIndex(avgUps.get(i), avgDowns.get(i),rsList.get(i), miscUtil.formatDouble(rsiList.get(i),"00") ));
        }
        return averageDirectionalIndexList;
        }

        private double calculateUpMove(double close, double prevClose){

            return (close - prevClose ) > 0 ? (close - prevClose ) : 0;
        }

        private double calculateDownMove(double close, double prevClose){
            return (prevClose - close ) > 0 ? (prevClose - close ) : 0;
        }
    }
