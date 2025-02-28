package com.example.service.impl;

import com.example.dto.OHLCV;
import com.example.external.ta.service.McService;
import com.example.model.master.Stock;

import com.example.mq.producer.QueueService;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.service.*;
import com.example.service.calc.*;
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
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class UpdateTechnicalsServiceImpl implements UpdateTechnicalsService {
    private static final long MIN_TRADING_DAYS = 32;

    @Autowired
    private YearlySupportResistanceService yearlySupportResistanceService;

    @Autowired
    private QuarterlySupportResistanceService quarterlySupportResistanceService;
    @Autowired
    private MonthlySupportResistanceService monthlySupportResistanceService;

    @Autowired
    private WeeklySupportResistanceService weeklySupportResistanceService;

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
    private OnBalanceVolumeCalculatorService onBalanceVolumeCalculatorService;
    @Autowired
    private RelativeStrengthIndexCalculatorService relativeStrengthIndexService;

    @Autowired
    private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

    @Autowired
    private SimpleMovingAverageCalculatorService simpleMovingAverageCalculatorService;

    @Autowired
    private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

    @Autowired
    private VolumeAverageCalculatorService volumeAverageCalculatorService;

    @Autowired
    private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;

    @Autowired
    private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;

    @Autowired
    private OhlcvService ohlcvService;

    @Autowired
    private MiscUtil miscUtil;

    @Autowired
    private McService mcService;

    @Autowired
    private CalendarService calendarService;


    @Override
    public void updateTechnicals() {
        List<Stock> stockList = stockService.getForActivity();
        AtomicInteger remaing = new AtomicInteger(stockList.size());
        //symbolList.add("HAVELLS");
        stockList.forEach(stock -> {

            long startTime = System.currentTimeMillis();

            try {

                log.info("Starting activity for {}",stock.getNseSymbol());

                //List<OHLCV> ohlcvList = mcService.getMCOHLP(stock.getNseSymbol(), 5, 700);
                //List<OHLCV> ohlcvList = this.fetch(stock.getNseSymbol(), 700);

                //
                LocalDate to  = calendarService.previousTradingSession(LocalDate.now());
                LocalDate from = to.minusYears(3);
                log.info(" {} from {} to {}", stock.getNseSymbol(), from, to);
                List<OHLCV> ohlcvList =   ohlcvService.fetch(stock.getNseSymbol(), from, to);
                //

                LocalDate bhavDate = calendarService.previousTradingSession(LocalDate.now());

                StockTechnicals stockTechnicals = this.calculate(stock.getNseSymbol(),bhavDate.atStartOfDay(ZoneOffset.UTC).toInstant(),ohlcvList);

                stockTechnicals.setYearHigh(0.0);
                stockTechnicals.setYearLow(0.0);

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

                System.out.println(objectWriter.writeValueAsString(stockTechnicals));

                stock.setActivityCompleted(true);
                technicalsTemplate.upsert(stockTechnicals);
                stockService.save(stock);

            }catch(Exception e){
                log.error("An error occured while getting data {}",stock.getNseSymbol(),e);
            }

            long endTime = System.currentTimeMillis();
            log.info("Completed activity for {} took {}ms", stock.getNseSymbol() , (endTime - startTime));

            log.info("Remaing {}", remaing.decrementAndGet());

            /*
            try {
                Thread.sleep(miscUtil.getInterval());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            */

        });
    }

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

    @Override
    public StockTechnicals calculate(String nseSymbol, Instant bhavDate, List<OHLCV> ohlcvList, StockTechnicals stockTechnicalsPreviousSession, long tradingDays) {
        //SimpleMovingAverage sma = new SimpleMovingAverage(stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose());

        Volume volume = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getVolume(), tradingDays);

        OnBalanceVolume onBalanceVolume = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getObv(), tradingDays);

        SimpleMovingAverage sma = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getSma(), tradingDays);

        ExponentialMovingAverage ema = this.build(nseSymbol, ohlcvList,stockTechnicalsPreviousSession.getEma(), tradingDays);

        AverageDirectionalIndex adx = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getAdx(), tradingDays);

        RelativeStrengthIndex rsi = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getRsi(), tradingDays);

        MovingAverageConvergenceDivergence macd = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getMacd(), tradingDays);

        return new StockTechnicals(nseSymbol, bhavDate, volume, onBalanceVolume, sma, ema, adx, rsi, macd);
    }

    @Override
    public StockTechnicals calculate(String nseSymbol, Instant bhavDate, List<OHLCV> ohlcvList) {

        StockTechnicals stockTechnicalsPreviousSession=this.init(nseSymbol, bhavDate);

        if(ohlcvList.size() <= 1){
            return stockTechnicalsPreviousSession;
        }

        int tradingDays = 2;

        Volume volume = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getVolume(), tradingDays);

        OnBalanceVolume onBalanceVolume = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getObv(), tradingDays);

        SimpleMovingAverage sma = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getSma(), tradingDays);

        ExponentialMovingAverage ema = this.build(nseSymbol, ohlcvList,stockTechnicalsPreviousSession.getEma(), tradingDays);

        AverageDirectionalIndex adx = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getAdx(), tradingDays);

        RelativeStrengthIndex rsi = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getRsi(), tradingDays);

        MovingAverageConvergenceDivergence macd = this.build(nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getMacd(), tradingDays);

        return new StockTechnicals(nseSymbol, bhavDate, volume, onBalanceVolume, sma, ema, adx, rsi, macd);
    }

    private StockTechnicals build(StockPriceIO stockPriceIO){

        String nseSymbol = stockPriceIO.getNseSymbol();

        long tradingDays = priceTemplate.count(nseSymbol);

        StockTechnicals prevStockTechnicals=this.init(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate());

        if(tradingDays <= 1){
            return prevStockTechnicals;
        }

        if(tradingDays > MIN_TRADING_DAYS){
            //prevStockTechnicals = technicalsTemplate.getPrevTechnicals(nseSymbol,  1);
            LocalDate to  = calendarService.previousTradingSession(stockPriceIO.getBhavDate().atZone(ZoneOffset.UTC).toLocalDate());
            LocalDate from = to.minusYears(3);
            log.info(" {} from {} to {}", stockPriceIO.getNseSymbol(), from, to);
            prevStockTechnicals = technicalsTemplate.getPreviousSessionTechnicals(nseSymbol,  from, to);
            /*
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
            try {
                System.out.println(objectWriter.writeValueAsString(prevStockTechnicals));
            }catch(Exception e){
                log.error("An error occured while prinitin prevStockTechnicals", e);
            }
            */
        }

        int limit = this.limit(tradingDays);

        List<OHLCV> ohlcvList = this.fetch(nseSymbol, limit);

        return this.calculate(nseSymbol, stockPriceIO.getBhavDate(), ohlcvList, prevStockTechnicals, tradingDays);
    }

    private StockTechnicals init(String nseSymbol, Instant bhavDate){

        StockTechnicals stockTechnicals = new StockTechnicals();

        stockTechnicals.setBhavDate(bhavDate);
        stockTechnicals.setNseSymbol(nseSymbol);

        Volume volume = new Volume(0l, 0l,0l, 0l);
        stockTechnicals.setVolume(volume);

        OnBalanceVolume onBalanceVolume = new OnBalanceVolume(0l,0l);
        stockTechnicals.setObv(onBalanceVolume);

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

        List<StockPrice> stockPriceList =  priceTemplate.get(nseSymbol, limit);

        Collections.reverse(stockPriceList);

        return stockPriceOHLCVAssembler.toModel(stockPriceList);
    }

    private Volume build(String nseSymbol, List<OHLCV> ohlcvList, Volume prevVolume, long tradingDays){

        OHLCV ohlcv = ohlcvList.get(ohlcvList.size() -1);

        /*
        Long avgVolume5 = technicalsTemplate.getAverageVolume(nseSymbol, 5);

        Long avgVolume20 = technicalsTemplate.getAverageVolume(nseSymbol, 20);

        Long avgVolume50 = technicalsTemplate.getAverageVolume(nseSymbol, 50);
        */

        if(tradingDays > MIN_TRADING_DAYS) {

            long avgVolume5 = volumeAverageCalculatorService.calculate(ohlcv, prevVolume.getAvg5(), 5);
            long avgVolume20 = volumeAverageCalculatorService.calculate(ohlcv,prevVolume.getAvg20(), 20);
            long avgVolume50 = volumeAverageCalculatorService.calculate(ohlcv,prevVolume.getAvg50(), 50);
            return new Volume(ohlcv.getVolume(), avgVolume5, avgVolume20, avgVolume50);
        }

        int resultIndex = ohlcvList.size()-1;

        long avgVolume5 = volumeAverageCalculatorService.calculate(ohlcvList, 5).get(resultIndex);;
        long avgVolume20 = volumeAverageCalculatorService.calculate(ohlcvList, 20).get(resultIndex);
        long avgVolume50 = volumeAverageCalculatorService.calculate(ohlcvList, 50).get(resultIndex);

        return new Volume(ohlcv.getVolume(), avgVolume5, avgVolume20, avgVolume50);
    }

    private OnBalanceVolume build(String nseSymbol, List<OHLCV> ohlcvList, OnBalanceVolume prevOnBalanceVolume, long tradingDays){

        if(tradingDays > MIN_TRADING_DAYS) {
            return onBalanceVolumeCalculatorService.calculate(ohlcvList, prevOnBalanceVolume);
        }

        return onBalanceVolumeCalculatorService.calculate(ohlcvList).get(ohlcvList.size()-1);
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

    private SimpleMovingAverage build(String nseSymbol, List<OHLCV> ohlcvList, SimpleMovingAverage prevSimpleMovingAverage, long tradingDays){

        if(tradingDays > MIN_TRADING_DAYS) {
            OHLCV ohlcv = ohlcvList.get(1);
            double sma5 = simpleMovingAverageCalculatorService.calculate(ohlcv, prevSimpleMovingAverage.getAvg5(), 5);
            double sma10 = simpleMovingAverageCalculatorService.calculate(ohlcv,prevSimpleMovingAverage.getAvg10(), 10);
            double sma20 = simpleMovingAverageCalculatorService.calculate(ohlcv, prevSimpleMovingAverage.getAvg20(), 20);
            double sma50 = simpleMovingAverageCalculatorService.calculate(ohlcv, prevSimpleMovingAverage.getAvg50(), 50);
            double sma100 = simpleMovingAverageCalculatorService.calculate(ohlcv,prevSimpleMovingAverage.getAvg100(), 100);
            double sma200 = simpleMovingAverageCalculatorService.calculate(ohlcv, prevSimpleMovingAverage.getAvg200(), 200);

            return new SimpleMovingAverage(sma5, sma10, sma20, sma50, sma100, sma200);
        }

        int resultIndex = ohlcvList.size()-1;
        double sma5 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 5).get(resultIndex);;
        double sma10 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 10).get(resultIndex);
        double sma20 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 20).get(resultIndex);
        double sma50 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 50).get(resultIndex);
        double sma100 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 100).get(resultIndex);
        double sma200 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 200).get(resultIndex);

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


    @Override
    public void updateTechnicalsTxn(StockTechnicals stockTechnicals, StockPriceIO stockPriceIO){

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
                stockTechnicalsTxn.setVolume(stockTechnicalsIO.getVolume());
                stockTechnicalsTxn.setVolumeAvg5(stockTechnicalsIO.getVolumeAvg5());
                stockTechnicalsTxn.setVolumeAvg20(stockTechnicalsIO.getVolumeAvg20());
            }

            stockTechnicalsTxn.setStock(stock);
            stockTechnicalsTxn.setBhavDate(stockPriceIO.getTimestamp());
            stockTechnicalsTxn.setTrend(stockPriceIO.getTrend());

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

            stockTechnicalsTxn.setPrevPrevEma5(stockTechnicalsTxn.getPrevEma5());
            stockTechnicalsTxn.setPrevPrevEma10(stockTechnicalsTxn.getPrevEma10());
            stockTechnicalsTxn.setPrevPrevEma20(stockTechnicalsTxn.getPrevEma20());
            stockTechnicalsTxn.setPrevPrevEma50(stockTechnicalsTxn.getPrevEma50());
            stockTechnicalsTxn.setPrevPrevEma100(stockTechnicalsTxn.getPrevEma100());
            stockTechnicalsTxn.setPrevPrevEma200(stockTechnicalsTxn.getPrevEma200());


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

            stockTechnicalsTxn.setPrevPrevObv(stockTechnicalsTxn.getPrevObv());
            stockTechnicalsTxn.setPrevPrevObvAvg(stockTechnicalsTxn.getPrevObvAvg());
            stockTechnicalsTxn.setPrevObv(stockTechnicalsTxn.getObv());
            stockTechnicalsTxn.setPrevObvAvg(stockTechnicalsTxn.getObvAvg());
            stockTechnicalsTxn.setObv(stockTechnicalsIO.getObv());
            stockTechnicalsTxn.setObvAvg(stockTechnicalsIO.getObvAvg());

            stockTechnicalsTxn.setVolumePrev(stockTechnicalsTxn.getVolume());
            stockTechnicalsTxn.setVolumeAvg5Prev(stockTechnicalsTxn.getVolumeAvg5());
            stockTechnicalsTxn.setVolumeAvg20Prev(stockTechnicalsTxn.getVolumeAvg20());
            stockTechnicalsTxn.setVolumeAvg50Prev(stockTechnicalsTxn.getVolumeAvg50());

            stockTechnicalsTxn.setVolume(stockTechnicalsIO.getVolume());
            stockTechnicalsTxn.setVolumeAvg5(stockTechnicalsIO.getVolumeAvg5());
            stockTechnicalsTxn.setVolumeAvg20(stockTechnicalsIO.getVolumeAvg20());
            stockTechnicalsTxn.setVolumeAvg50(stockTechnicalsIO.getVolumeAvg50());

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

            this.updateYearlySupportResistance(stockTechnicalsTxn);
            this.updateQuarterlySupportResistance(stockTechnicalsTxn);
            this.updateMonthlySupportResistance(stockTechnicalsTxn);
            this.updateWeeklySupportResistance(stockTechnicalsTxn);

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

        stockTechnicalsIO.setObv(stockTechnicals.getObv().getObv());
        stockTechnicalsIO.setObvAvg(stockTechnicals.getObv().getAverage());

        stockTechnicalsIO.setVolume(stockTechnicals.getVolume().getVolume());
        stockTechnicalsIO.setVolumeAvg5(stockTechnicals.getVolume().getAvg5());
        stockTechnicalsIO.setVolumeAvg20(stockTechnicals.getVolume().getAvg20());
        stockTechnicalsIO.setVolumeAvg50(stockTechnicals.getVolume().getAvg50());

        return stockTechnicalsIO;
    }

    private void setYearHighLow(StockPriceIO stockPriceIO, StockTechnicals stockTechnicals ){

        stockTechnicals.setYearLow(stockPriceIO.getYearLow());
        stockTechnicals.setYearHigh(stockPriceIO.getYearHigh());
    }

    private void updateQuarterlySupportResistance(com.example.model.stocks.StockTechnicals stockTechnicalsTxn){
        LocalDate firstTradingSession = calendarService.nextTradingDate(miscUtil.previousQuarterLastDay());

        try{
        if(firstTradingSession.isEqual(stockTechnicalsTxn.getBhavDate())){
            log.info("Updating quarterly SR");
            OHLCV ohlcv = quarterlySupportResistanceService.supportAndResistance(stockTechnicalsTxn.getStock());

            stockTechnicalsTxn.setPrevPrevQuarterOpen(stockTechnicalsTxn.getPrevQuarterOpen());
            stockTechnicalsTxn.setPrevPrevQuarterHigh(stockTechnicalsTxn.getPrevQuarterHigh());
            stockTechnicalsTxn.setPrevPrevQuarterLow(stockTechnicalsTxn.getPrevQuarterLow());
            stockTechnicalsTxn.setPrevPrevQuarterClose(stockTechnicalsTxn.getPrevQuarterClose());

            stockTechnicalsTxn.setPrevQuarterOpen(ohlcv.getOpen());
            stockTechnicalsTxn.setPrevQuarterHigh(ohlcv.getHigh());
            stockTechnicalsTxn.setPrevQuarterLow(ohlcv.getLow());
            stockTechnicalsTxn.setPrevQuarterClose(ohlcv.getClose());
        }
        }catch(Exception e){
            log.error("An error occurred while updating quartely SR", e);
        }
    }

    private void updateMonthlySupportResistance(com.example.model.stocks.StockTechnicals stockTechnicalsTxn){
        LocalDate firstTradingSession = calendarService.nextTradingDate(miscUtil.previousMonthLastDay());

        try{
        if(firstTradingSession.isEqual(stockTechnicalsTxn.getBhavDate())){
            log.info("Updating monthly SR");
            OHLCV ohlcv = monthlySupportResistanceService.supportAndResistance(stockTechnicalsTxn.getStock());

            stockTechnicalsTxn.setPrevPrevMonthOpen(stockTechnicalsTxn.getPrevMonthOpen());
            stockTechnicalsTxn.setPrevPrevMonthHigh(stockTechnicalsTxn.getPrevMonthHigh());
            stockTechnicalsTxn.setPrevPrevMonthLow(stockTechnicalsTxn.getPrevMonthLow());
            stockTechnicalsTxn.setPrevPrevMonthClose(stockTechnicalsTxn.getPrevMonthClose());

            stockTechnicalsTxn.setPrevMonthOpen(ohlcv.getOpen());
            stockTechnicalsTxn.setPrevMonthHigh(ohlcv.getHigh());
            stockTechnicalsTxn.setPrevMonthLow(ohlcv.getLow());
            stockTechnicalsTxn.setPrevMonthClose(ohlcv.getClose());
        }
        }catch(Exception e){
            log.error("An error occurred while updating monthly SR", e);
        }
    }

    private void updateWeeklySupportResistance(com.example.model.stocks.StockTechnicals stockTechnicalsTxn){
        LocalDate firstTradingSession = calendarService.nextTradingDate(miscUtil.previousWeekLastDay());

        try{
        if(firstTradingSession.isEqual(stockTechnicalsTxn.getBhavDate())){
            log.info("Updating weekly SR");
            OHLCV ohlcv = weeklySupportResistanceService.supportAndResistance(stockTechnicalsTxn.getStock());

            stockTechnicalsTxn.setPrevPrevWeekOpen(stockTechnicalsTxn.getPrevWeekOpen());
            stockTechnicalsTxn.setPrevPrevWeekHigh(stockTechnicalsTxn.getPrevWeekHigh());
            stockTechnicalsTxn.setPrevPrevWeekLow(stockTechnicalsTxn.getPrevWeekLow());
            stockTechnicalsTxn.setPrevPrevWeekClose(stockTechnicalsTxn.getPrevWeekClose());

            stockTechnicalsTxn.setPrevWeekOpen(ohlcv.getOpen());
            stockTechnicalsTxn.setPrevWeekHigh(ohlcv.getHigh());
            stockTechnicalsTxn.setPrevWeekLow(ohlcv.getLow());
            stockTechnicalsTxn.setPrevWeekClose(ohlcv.getClose());
        }
        }catch(Exception e){
            log.error("An error occurred while updating weekly SR", e);
        }
    }

    private void updateYearlySupportResistance(com.example.model.stocks.StockTechnicals stockTechnicalsTxn){
        LocalDate firstTradingSession = calendarService.nextTradingDate(miscUtil.currentYearFirstDay().minusDays(1));

        try {
            if (firstTradingSession.isEqual(stockTechnicalsTxn.getBhavDate())) {
                log.info("Updating weekly SR");
                OHLCV ohlcv = yearlySupportResistanceService.supportAndResistance(stockTechnicalsTxn.getStock());
                stockTechnicalsTxn.setPrevYearOpen(ohlcv.getOpen());
                stockTechnicalsTxn.setPrevYearHigh(ohlcv.getHigh());
                stockTechnicalsTxn.setPrevYearLow(ohlcv.getLow());
                stockTechnicalsTxn.setPrevYearClose(ohlcv.getClose());
            }
        }catch(Exception e){
            log.error("An error occurred while updating yearly SR", e);
        }
    }
}
