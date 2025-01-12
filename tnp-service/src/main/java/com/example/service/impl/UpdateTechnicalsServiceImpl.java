package com.example.service.impl;

import com.example.model.master.Stock;

import com.example.mq.producer.QueueService;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.service.*;
import com.example.service.calc.AverageDirectionalIndexCalculatorService;
import com.example.service.calc.ExponentialMovingAverageCalculatorService;
import com.example.service.calc.RelativeStrengthIndexCalculatorService;
import com.example.storage.model.*;
import com.example.storage.model.assembler.StockPriceOHLCVAssembler;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.SupportAndResistanceUtil;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.StockTechnicalsIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UpdateTechnicalsServiceImpl implements UpdateTechnicalsService {
    private static final long MIN_TRADING_DAYS = 32;

    @Autowired
    private StockService stockService;
    @Autowired
    private StockTechnicalsRepository stockTechnicalsRepository;

    @Autowired
    private QueueService queueService;

    @Autowired
    private ResearchLedgerFundamentalService researchLedgerFundamentalService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private TechnicalsTemplate technicalsTemplate;

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private PriceTemplate priceTemplate;

    @Autowired private ObjectMapper mapper;

    @Autowired
    private RelativeStrengthIndexCalculatorService relativeStrengthIndexService;

    @Autowired
    private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

    @Autowired
    private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

    @Autowired
    private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;

    @Autowired
    private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;

    @Autowired
    private MiscUtil miscUtil;

    @Override
    public void updateTechnicals(StockPriceIO stockPriceIO) {
        log.info("{} Starting technicals update.", stockPriceIO.getNseSymbol());
         try{

             StockTechnicals stockTechnicals = this.updateTechnicalsHistory(stockPriceIO);

             this.updateTechnicalsTxn(stockTechnicals, stockPriceIO);

        } catch (Exception e) {

        log.error(" {} Error while updating technicals ", stockPriceIO.getNseSymbol(), e);

        }
        log.info("{} Completed technicals update.", stockPriceIO.getNseSymbol());
    }

    private StockTechnicals updateTechnicalsHistory(StockPriceIO stockPriceIO){
        log.info("{} Updating technicals history.", stockPriceIO.getNseSymbol());


            StockTechnicals stockTechnicals = this.build(stockPriceIO);

            this.setYearHighLow(stockPriceIO, stockTechnicals);

            technicalsTemplate.upsert(stockTechnicals);
        log.info("{} Updated technicals history.", stockPriceIO.getNseSymbol());
            return stockTechnicals;

    }

    private StockTechnicals build(StockPriceIO stockPriceIO){

        String nseSymbol = stockPriceIO.getNseSymbol();

        long tradingDays = priceTemplate.count(nseSymbol);

        StockTechnicals prevStockTechnicals=this.init(stockPriceIO);

        if(tradingDays <= 1){
            return prevStockTechnicals;
        }

        if(tradingDays > MIN_TRADING_DAYS){
            prevStockTechnicals = technicalsTemplate.getPrevTechnicals(nseSymbol,  1);
        }

        //To handle if there is corrupt data
        if(prevStockTechnicals.getEma() == null){
            tradingDays = 2;
        }


        int limit = this.limit(tradingDays);

        List<OHLCV> ohlcvList = this.fetch(nseSymbol, limit);

        /*

        SimpleMovingAverage sma = this.build(nseSymbol, ohlcvList);

        if(limit == 200) {
            ohlcvList = ohlcvList.subList(ohlcvList.size() - 2, ohlcvList.size());
        } */
        SimpleMovingAverage sma = new SimpleMovingAverage(stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose());
        Volume volume = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getVolume(), tradingDays);

        ExponentialMovingAverage ema = this.build(nseSymbol, ohlcvList,prevStockTechnicals.getEma(), tradingDays);

        AverageDirectionalIndex adx = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getAdx(), tradingDays);

        RelativeStrengthIndex rsi = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getRsi(), tradingDays);

        MovingAverageConvergenceDivergence macd = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getMacd(), tradingDays);

        return new StockTechnicals(nseSymbol, stockPriceIO.getBhavDate(), volume,  sma, ema, adx, rsi, macd);
        //return new StockTechnicals(nseSymbol, stockPriceIO.getBhavDate(), volume,  sma, ema);
    }

    private StockTechnicals init(StockPriceIO stockPriceIO){

        StockTechnicals stockTechnicals = new StockTechnicals();

        stockTechnicals.setBhavDate(stockPriceIO.getBhavDate());
        stockTechnicals.setNseSymbol(stockPriceIO.getNseSymbol());

        Volume volume = new Volume(stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdqty());
        stockTechnicals.setVolume(volume);

        SimpleMovingAverage sma = new SimpleMovingAverage(0.00,0.00,0.00,0.00,0.00,0.00);
        stockTechnicals.setSma(sma);

        ExponentialMovingAverage ema = new ExponentialMovingAverage(0.00,0.00,0.00,0.00,0.00,0.00);
        stockTechnicals.setEma(ema);

        AverageDirectionalIndex adx = new AverageDirectionalIndex(0.00, 0.00,0.00, 0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setAdx(adx);

        RelativeStrengthIndex rsi = new RelativeStrengthIndex(0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setRsi(rsi);

        MovingAverageConvergenceDivergence macd = new MovingAverageConvergenceDivergence(0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setMacd(macd);


        return stockTechnicals;
    }

    private int limit(long tradingDays){
        return tradingDays > MIN_TRADING_DAYS ? 2 : 700;
    }
    private List<OHLCV> fetch(String nseSymbol, int limit){

        //int limit = tradingDays > MIN_TRADING_DAYS ? 2 : 700;

        List<StockPrice> stockPriceList =  priceTemplate.get(nseSymbol, limit);

        Collections.reverse(stockPriceList);

        return stockPriceOHLCVAssembler.toModel(stockPriceList);
    }

    private Volume build(String nseSymbol, List<OHLCV> ohlcvList, Volume prevVolume, long tradingDays){

        OHLCV ohlcv = ohlcvList.get(ohlcvList.size() -1);

        Long avgVolume5 = technicalsTemplate.getAverageVolume(nseSymbol, 5);

        Long avgVolume30 = technicalsTemplate.getAverageVolume(nseSymbol, 30);

        //avgVolume5 = formulaService.calculateSmoothedMovingAverage(avgVolume5, ohlcv.getVolume(), 5);
        return new Volume(ohlcv.getVolume(), avgVolume5, avgVolume30);
    }

    private double calculateAverage(List<OHLCV> ohlcvList, int days) {

        if (ohlcvList == null || ohlcvList.isEmpty()) {
            return 0.0; // Handle empty list
        }

        double sum = 0.0;

        int count = Math.min(days, ohlcvList.size());

        for (int i = ohlcvList.size() - count; i < ohlcvList.size(); i++) {
            sum += ohlcvList.get(i).getClose();
        }

        return  sum / count;
    }

    private SimpleMovingAverage build(String nseSymbol, List<OHLCV> ohlcvList){

        double close = 0.00;
        //double sma5 = priceTemplate.getAveragePrice(nseSymbol, close, 5);

        double sma5 = this.calculateAverage(ohlcvList, 5);
        //double sma10 = priceTemplate.getAveragePrice(nseSymbol, close, 10);
        double sma10 = this.calculateAverage(ohlcvList, 10);
        //double sma20 = priceTemplate.getAveragePrice(nseSymbol, close, 20);
        double sma20 = this.calculateAverage(ohlcvList, 20);
       // double sma50 = priceTemplate.getAveragePrice(nseSymbol, close, 50);
        double sma50 = this.calculateAverage(ohlcvList, 50);
       // double sma100 = priceTemplate.getAveragePrice(nseSymbol, close, 100);
        double sma100 = this.calculateAverage(ohlcvList, 100);
        //double sma200 = priceTemplate.getAveragePrice(nseSymbol,close, 200);
        double sma200 = this.calculateAverage(ohlcvList, 200);
        return new SimpleMovingAverage(miscUtil.formatDouble(sma5,"00"),
                miscUtil.formatDouble(sma10,"00"),
                miscUtil.formatDouble(sma20,"00"),
                miscUtil.formatDouble(sma50,"00"),
                miscUtil.formatDouble(sma100,"00"),
                miscUtil.formatDouble(sma200,"00"));
    }

    private ExponentialMovingAverage build(String nseSymbol,List<OHLCV> ohlcvList, ExponentialMovingAverage prevExponentialMovingAverage, long tradingDays){

        if(tradingDays > MIN_TRADING_DAYS) {
            OHLCV ohlcv = ohlcvList.get(1);
            double ema5 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg5(), 5);
            double ema10 = exponentialMovingAverageService.calculate(ohlcv,prevExponentialMovingAverage.getAvg10(), 10);
            double ema20 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg20(), 20);
            double ema50 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg50(), 50);
            double ema100 = exponentialMovingAverageService.calculate(ohlcv,prevExponentialMovingAverage.getAvg100(), 100);
            double ema200 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg200(), 200);

            return new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);
        }

        int resultIndex = ohlcvList.size()-1;
        double ema5 = exponentialMovingAverageService.calculate(ohlcvList, 5).get(resultIndex);;
        double ema10 = exponentialMovingAverageService.calculate(ohlcvList, 10).get(resultIndex);
        double ema20 = exponentialMovingAverageService.calculate(ohlcvList, 20).get(resultIndex);
        double ema50 = exponentialMovingAverageService.calculate(ohlcvList, 50).get(resultIndex);
        double ema100 = exponentialMovingAverageService.calculate(ohlcvList, 100).get(resultIndex);
        double ema200 = exponentialMovingAverageService.calculate(ohlcvList, 200).get(resultIndex);

        return new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);
    }

    private AverageDirectionalIndex build(String nseSymbol,List<OHLCV> ohlcvList, AverageDirectionalIndex prevAverageDirectionalIndex, long tradingDays){

        if(tradingDays > MIN_TRADING_DAYS) {
            return averageDirectionalIndexService.calculate(ohlcvList, prevAverageDirectionalIndex);
        }

        return averageDirectionalIndexService.calculate(ohlcvList).get(ohlcvList.size() -1);
    }

    private RelativeStrengthIndex build(String nseSymbol,List<OHLCV> ohlcvList, RelativeStrengthIndex prevRelativeStrengthIndex, long tradingDays){
        if(tradingDays > MIN_TRADING_DAYS) {
            return relativeStrengthIndexService.calculate(ohlcvList, prevRelativeStrengthIndex);
        }

        return relativeStrengthIndexService.calculate(ohlcvList).get(ohlcvList.size() -1);
    }

    private MovingAverageConvergenceDivergence build(String nseSymbol,List<OHLCV> ohlcvList, MovingAverageConvergenceDivergence prevMovingAverageConvergenceDivergence, long tradingDays){

        if(tradingDays > MIN_TRADING_DAYS) {
            return movingAverageConvergenceDivergenceService.calculate(ohlcvList.get(ohlcvList.size()-1), prevMovingAverageConvergenceDivergence);
        }

        return movingAverageConvergenceDivergenceService.calculate(ohlcvList).get(ohlcvList.size() -1);
    }


    private void updateTechnicalsTxn(StockTechnicals stockTechnicals, StockPriceIO stockPriceIO){

        log.info("{} Updating transactional technicals ", stockPriceIO.getNseSymbol());

        StockTechnicalsIO stockTechnicalsIO = this.createStockTechnicalsIO(stockPriceIO, stockTechnicals);

        Stock stock = stockService.getStockByNseSymbol(stockTechnicalsIO.getNseSymbol());

        com.example.model.stocks.StockTechnicals stockTechnicalsTxn = stock.getTechnicals();

        if (stockTechnicalsTxn != null && (stockTechnicalsTxn.getBhavDate().isEqual(stockPriceIO.getTimestamp())
                || stockTechnicalsTxn.getBhavDate().isAfter(stockPriceIO.getTimestamp()))) {
            log.info("{} Transactional technicals is already up to date for {}", stockTechnicalsIO.getNseSymbol(), stockPriceIO.getBhavDate());
        }else {

            if (stockTechnicalsTxn == null) {
                stockTechnicalsTxn = new
                        com.example.model.stocks.StockTechnicals();
            }

            stockTechnicalsTxn.setStock(stock);
            stockTechnicalsTxn.setBhavDate(stockPriceIO.getTimestamp());


            stockTechnicalsTxn.setPrevSma5(stockTechnicalsTxn.getSma5());
            stockTechnicalsTxn.setPrevSma10(stockTechnicalsTxn.getSma10());
            stockTechnicalsTxn.setPrevSma20(stockTechnicalsTxn.getSma20());
            stockTechnicalsTxn.setPrevSma50(stockTechnicalsTxn.getSma50());
            stockTechnicalsTxn.setPrevSma100(stockTechnicalsTxn.getSma100());
            stockTechnicalsTxn.setPrevSma200(stockTechnicalsTxn.getSma200());


            stockTechnicalsTxn.setSma5(stockTechnicalsIO.getSma5());
            stockTechnicalsTxn.setSma10(stockTechnicalsIO.getSma10());
            stockTechnicalsTxn.setSma20(stockTechnicalsIO.getSma20());
            stockTechnicalsTxn.setSma50(stockTechnicalsIO.getSma50());
            stockTechnicalsTxn.setSma100(stockTechnicalsIO.getSma100());
            stockTechnicalsTxn.setSma200(stockTechnicalsIO.getSma200());


            stockTechnicalsTxn.setPrevEma5(stockTechnicalsTxn.getEma5());
            stockTechnicalsTxn.setPrevEma10(stockTechnicalsTxn.getEma10());
            stockTechnicalsTxn.setPrevEma20(stockTechnicalsTxn.getEma20());
            stockTechnicalsTxn.setPrevEma50(stockTechnicalsTxn.getEma50());
            stockTechnicalsTxn.setPrevEma100(stockTechnicalsTxn.getEma100());
            stockTechnicalsTxn.setPrevEma200(stockTechnicalsTxn.getEma200());


            stockTechnicalsTxn.setEma5(stockTechnicalsIO.getEma5());
            stockTechnicalsTxn.setEma10(stockTechnicalsIO.getEma10());
            stockTechnicalsTxn.setEma20(stockTechnicalsIO.getEma20());
            stockTechnicalsTxn.setEma50(stockTechnicalsIO.getEma50());
            stockTechnicalsTxn.setEma100(stockTechnicalsIO.getEma100());
            stockTechnicalsTxn.setEma200(stockTechnicalsIO.getEma200());


            stockTechnicalsTxn.setPrevAdx(stockTechnicalsTxn.getAdx());
            stockTechnicalsTxn.setAdx(stockTechnicalsIO.getAdx());


            stockTechnicalsTxn.setPrevPlusDi(stockTechnicalsTxn.getPlusDi());
            stockTechnicalsTxn.setPlusDi(stockTechnicalsIO.getPlusDi());


            stockTechnicalsTxn.setPrevMinusDi(stockTechnicalsTxn.getMinusDi());
            stockTechnicalsTxn.setMinusDi(stockTechnicalsIO.getMinusDi());


            stockTechnicalsTxn.setPrevRsi(stockTechnicalsTxn.getRsi());
            stockTechnicalsTxn.setRsi(stockTechnicalsIO.getRsi());


            stockTechnicalsTxn.setPrevMacd(stockTechnicalsTxn.getMacd());
            stockTechnicalsTxn.setMacd(stockTechnicalsIO.getMacd());


            stockTechnicalsTxn.setPrevSignal(stockTechnicalsTxn.getSignal());
            stockTechnicalsTxn.setSignal(stockTechnicalsIO.getSignal());


            stockTechnicalsTxn.setPrevVolume(stockTechnicalsTxn.getVolume());
            stockTechnicalsTxn.setVolume(stockTechnicalsIO.getVolume());
            stockTechnicalsTxn.setWeeklyVolume(stockTechnicalsIO.getWeeklyVolume());
            stockTechnicalsTxn.setMonthlyVolume(stockTechnicalsIO.getMonthlyyVolume());


            stockTechnicalsTxn.setPrevPivotPoint(stockTechnicalsTxn.getPivotPoint());
            stockTechnicalsTxn.setPrevFirstResistance(stockTechnicalsTxn.getFirstResistance());
            stockTechnicalsTxn.setPrevSecondResistance(stockTechnicalsTxn.getSecondResistance());
            stockTechnicalsTxn.setPrevThirdResistance(stockTechnicalsTxn.getThirdResistance());
            stockTechnicalsTxn.setPrevFirstSupport(stockTechnicalsTxn.getFirstSupport());
            stockTechnicalsTxn.setPrevSecondSupport(stockTechnicalsTxn.getSecondSupport());
            stockTechnicalsTxn.setPrevThirdSupport(stockTechnicalsTxn.getThirdSupport());


            stockTechnicalsTxn.setPivotPoint(SupportAndResistanceUtil.pivotPoint(stockPriceIO.getHigh(), stockPriceIO.getLow(), stockPriceIO.getClose()));
            stockTechnicalsTxn.setFirstResistance(SupportAndResistanceUtil.firstResistance(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
            stockTechnicalsTxn.setSecondResistance(SupportAndResistanceUtil.secondResistance(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
            stockTechnicalsTxn.setThirdResistance(SupportAndResistanceUtil.thirdResistance(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
            stockTechnicalsTxn.setFirstSupport(SupportAndResistanceUtil.firstSupport(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
            stockTechnicalsTxn.setSecondSupport(SupportAndResistanceUtil.secondSupport(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
            stockTechnicalsTxn.setThirdSupport(SupportAndResistanceUtil.thirdSupport(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));


            stockTechnicalsTxn.setLastModified(LocalDate.now());

            stockTechnicalsRepository.save(stockTechnicalsTxn);

            log.info("{} Updated transactional technicals ", stockPriceIO.getNseSymbol());
        }
    }

    private StockTechnicalsIO createStockTechnicalsIO(StockPriceIO stockPriceIO, StockTechnicals stockTechnicals) {

        StockTechnicalsIO stockTechnicalsIO = new StockTechnicalsIO();

        stockTechnicalsIO.setNseSymbol(stockPriceIO.getNseSymbol());

        stockTechnicalsIO.setSma5(stockTechnicals.getSma().getAvg5());
        stockTechnicalsIO.setSma10(stockTechnicals.getSma().getAvg10());
        stockTechnicalsIO.setSma20(stockTechnicals.getSma().getAvg20());
        stockTechnicalsIO.setSma50(stockTechnicals.getSma().getAvg50());
        stockTechnicalsIO.setSma100(stockTechnicals.getSma().getAvg100());
        stockTechnicalsIO.setSma200(stockTechnicals.getSma().getAvg200());

        stockTechnicalsIO.setEma5(stockTechnicals.getEma().getAvg5());
        stockTechnicalsIO.setEma10(stockTechnicals.getEma().getAvg10());
        stockTechnicalsIO.setEma20(stockTechnicals.getEma().getAvg20());
        stockTechnicalsIO.setEma50(stockTechnicals.getEma().getAvg50());
        stockTechnicalsIO.setEma100(stockTechnicals.getEma().getAvg100());
        stockTechnicalsIO.setEma200(stockTechnicals.getEma().getAvg200());

        stockTechnicalsIO.setAdx(stockTechnicals.getAdx().getAdx());
        stockTechnicalsIO.setPlusDi(stockTechnicals.getAdx().getPlusDi());
        stockTechnicalsIO.setMinusDi(stockTechnicals.getAdx().getMinusDi());

        stockTechnicalsIO.setMacd(stockTechnicals.getMacd().getMacd());
        stockTechnicalsIO.setSignal(stockTechnicals.getMacd().getSignal());

        stockTechnicalsIO.setRsi(stockTechnicals.getRsi().getRsi());

        stockTechnicalsIO.setVolume(stockTechnicals.getVolume().getVolume());
        stockTechnicalsIO.setWeeklyVolume(stockTechnicals.getVolume().getWeeklyAverage());
        stockTechnicalsIO.setMonthlyyVolume(stockTechnicals.getVolume().getMonthlyAverage());

        return stockTechnicalsIO;
    }

    private void setYearHighLow(StockPriceIO stockPriceIO, StockTechnicals stockTechnicals ){

        stockTechnicals.setYearLow(stockPriceIO.getYearLow());
        stockTechnicals.setYearHigh(stockPriceIO.getYearHigh());
    }
}
