package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.dto.OHLCV;
import com.example.service.StockTechnicalsService;
import com.example.external.ta.service.McService;
import com.example.transactional.model.master.Stock;

import com.example.transactional.repo.stocks.StockTechnicalsRepositoryOld;
import com.example.transactional.repo.stocks.WeeklyStockTechnicalsRepository;
import com.example.service.*;
import com.example.service.calc.*;
import com.example.storage.model.*;
import com.example.storage.model.assembler.StockPriceOHLCVAssembler;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.dto.io.StockPriceIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

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
    private StockTechnicalsRepositoryOld stockTechnicalsRepository;

    @Autowired
    private WeeklyStockTechnicalsRepository weeklyStockTechnicalsRepository;

    @Autowired
    private ResearchLedgerFundamentalService researchLedgerFundamentalService;

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
    private StockTechnicalsService<com.example.transactional.model.stocks.StockTechnicals> stockTechnicalsService;

    @Autowired
    private OhlcvService ohlcvService;

    @Autowired
    private WeeklyOhlcvService weeklyOhlcvService;

    @Autowired
    private MiscUtil miscUtil;

    @Autowired
    private McService mcService;

    @Autowired
    private CalendarService calendarService;


    @Override
    public void updateTechnicals(Timeframe timeframe, Stock stock, LocalDate sessionDate) {

                try {
                    log.info("{} starting technicals update", stock.getNseSymbol());
                    //List<OHLCV> ohlcvList = mcService.getMCOHLP(stock.getNseSymbol(), 3, 700);
                    //LocalDate to  = calendarService.previousTradingSession(LocalDate.now());
                    List<OHLCV> ohlcvList = this.fetch(timeframe, stock.getNseSymbol(), sessionDate);

                    StockTechnicals stockTechnicals = this.calculate(stock.getNseSymbol(), ohlcvList.get(ohlcvList.size() - 1).getBhavDate(), ohlcvList);

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

                    System.out.println( stock.getNseSymbol() + " : " +" Historical " + " on " + sessionDate);
                    System.out.println(objectWriter.writeValueAsString(stockTechnicals));

                    technicalsTemplate.upsert(timeframe, stockTechnicals);

                    com.example.transactional.model.stocks.StockTechnicals stockTechnicals1 = stockTechnicalsService.createOrUpdate(stock, timeframe,
                            stockTechnicals.getEma().getAvg5(),
                            stockTechnicals.getEma().getAvg10(),
                            stockTechnicals.getEma().getAvg20(),
                            stockTechnicals.getEma().getAvg50(),
                            stockTechnicals.getEma().getAvg100(),
                            stockTechnicals.getEma().getAvg200(),
                            stockTechnicals.getSma().getAvg5(),
                            stockTechnicals.getSma().getAvg10(),
                            stockTechnicals.getSma().getAvg20(),
                            stockTechnicals.getSma().getAvg50(),
                            stockTechnicals.getSma().getAvg100(),
                            stockTechnicals.getSma().getAvg200(),
                            stockTechnicals.getRsi().getRsi(),
                            stockTechnicals.getMacd().getMacd(),
                            stockTechnicals.getMacd().getSignal(),
                            stockTechnicals.getObv().getObv(),
                            stockTechnicals.getObv().getAverage(),
                            stockTechnicals.getVolume().getVolume(),
                            stockTechnicals.getVolume().getAvg5(),
                            stockTechnicals.getVolume().getAvg10(),
                            stockTechnicals.getVolume().getAvg20(),
                            stockTechnicals.getAdx().getAdx(),
                            stockTechnicals.getAdx().getPlusDi(),
                            stockTechnicals.getAdx().getMinusDi(),
                            stockTechnicals.getAdx().getAtr(),
                            sessionDate
                            );
                    System.out.println( stock.getNseSymbol() + " : " +" Transactional " + " on " + sessionDate);
                    System.out.println(stockTechnicals1);

                }catch(Exception e){
                    log.error("{} An error occured while updating technicals ", stock.getNseSymbol(), e);
                }
    }

    @Override
    public void updateTechnicals(Stock stock, StockPriceIO stockPriceIO) {

        stockPriceIO.setTimeFrame(Timeframe.DAILY);
        updateTechnicals(Timeframe.DAILY, stock, stockPriceIO);

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
            stockPriceIO.setTimeFrame(Timeframe.MONTHLY);
            updateTechnicals(Timeframe.MONTHLY, stock, stockPriceIO);
        }

        if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
            stockPriceIO.setTimeFrame(Timeframe.WEEKLY);
            updateTechnicals(Timeframe.WEEKLY, stock, stockPriceIO);
        }

    }

    @Override
    public void updateTechnicals(Timeframe timeframe, Stock stock, StockPriceIO stockPriceIO) {
        log.info("{} Starting technicals update. {}", stockPriceIO.getNseSymbol(), stockPriceIO.getTimeFrame());
         try{

             StockTechnicals stockTechnicals = this.build(stockPriceIO);

             //technicalsTemplate.upsert(timeframe, stockTechnicals);
             stockTechnicalsService.createOrUpdate(stock, timeframe, stockTechnicals.getEma().getAvg5(),
                     stockTechnicals.getEma().getAvg10(),
                     stockTechnicals.getEma().getAvg20(),
                     stockTechnicals.getEma().getAvg50(),
                     stockTechnicals.getEma().getAvg100(),
                     stockTechnicals.getEma().getAvg200(),
                     stockTechnicals.getSma().getAvg5(),
                     stockTechnicals.getSma().getAvg10(),
                     stockTechnicals.getSma().getAvg20(),
                     stockTechnicals.getSma().getAvg50(),
                     stockTechnicals.getSma().getAvg100(),
                     stockTechnicals.getSma().getAvg200(),
                     stockTechnicals.getRsi().getRsi(),
                     stockTechnicals.getMacd().getMacd(),
                     stockTechnicals.getMacd().getSignal(),
                     stockTechnicals.getObv().getObv(),
                     stockTechnicals.getObv().getAverage(),
                     stockTechnicals.getVolume().getVolume(),
                     stockTechnicals.getVolume().getAvg5(),
                     stockTechnicals.getVolume().getAvg10(),
                     stockTechnicals.getVolume().getAvg20(),
                     stockTechnicals.getAdx().getAdx(),
                     stockTechnicals.getAdx().getPlusDi(),
                     stockTechnicals.getAdx().getMinusDi(),
                     stockTechnicals.getAdx().getAtr(),
                     stockTechnicals.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate()
             );

             //miscUtil.delay(50);
        } catch (Exception e) {

        log.error("{} Error while updating technicals ", stockPriceIO.getNseSymbol(), e);

        }
        log.info("{} Completed technicals update.", stockPriceIO.getNseSymbol());
    }

    private StockTechnicals updateTechnicalsHistory(Timeframe timeframe, StockPriceIO stockPriceIO){
        log.info("{} Updating technicals history timeframe {} ", stockPriceIO.getNseSymbol(), timeframe);

        Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

            StockTechnicals stockTechnicals = this.build(stockPriceIO);

            technicalsTemplate.upsert(timeframe, stockTechnicals);
            stockTechnicalsService.createOrUpdate(stock, timeframe, stockTechnicals.getEma().getAvg5(),
                    stockTechnicals.getEma().getAvg10(),
                    stockTechnicals.getEma().getAvg20(),
                    stockTechnicals.getEma().getAvg50(),
                    stockTechnicals.getEma().getAvg100(),
                    stockTechnicals.getEma().getAvg200(),
                    stockTechnicals.getSma().getAvg5(),
                    stockTechnicals.getSma().getAvg10(),
                    stockTechnicals.getSma().getAvg20(),
                    stockTechnicals.getSma().getAvg50(),
                    stockTechnicals.getSma().getAvg100(),
                    stockTechnicals.getSma().getAvg200(),
                    stockTechnicals.getRsi().getRsi(),
                    stockTechnicals.getMacd().getMacd(),
                    stockTechnicals.getMacd().getSignal(),
                    stockTechnicals.getObv().getObv(),
                    stockTechnicals.getObv().getAverage(),
                    stockTechnicals.getVolume().getVolume(),
                    stockTechnicals.getVolume().getAvg5(),
                    stockTechnicals.getVolume().getAvg10(),
                    stockTechnicals.getVolume().getAvg20(),
                    stockTechnicals.getAdx().getAdx(),
                    stockTechnicals.getAdx().getPlusDi(),
                    stockTechnicals.getAdx().getMinusDi(),
                    stockTechnicals.getAdx().getAtr(),
                    stockTechnicals.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate()
            );

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

    @Override
    public void updateTechnicals(Timeframe timeframe, Stock stock, StockTechnicals stockTechnicals) {
        log.info("{} Updating {} technicals", stock.getNseSymbol(), timeframe);

        stockTechnicalsService.createOrUpdate(stock, timeframe, stockTechnicals.getEma().getAvg5(),
                stockTechnicals.getEma().getAvg10(),
                stockTechnicals.getEma().getAvg20(),
                stockTechnicals.getEma().getAvg50(),
                stockTechnicals.getEma().getAvg100(),
                stockTechnicals.getEma().getAvg200(),
                stockTechnicals.getSma().getAvg5(),
                stockTechnicals.getSma().getAvg10(),
                stockTechnicals.getSma().getAvg20(),
                stockTechnicals.getSma().getAvg50(),
                stockTechnicals.getSma().getAvg100(),
                stockTechnicals.getSma().getAvg200(),
                stockTechnicals.getRsi().getRsi(),
                stockTechnicals.getMacd().getMacd(),
                stockTechnicals.getMacd().getSignal(),
                stockTechnicals.getObv().getObv(),
                stockTechnicals.getObv().getAverage(),
                stockTechnicals.getVolume().getVolume(),
                stockTechnicals.getVolume().getAvg5(),
                stockTechnicals.getVolume().getAvg10(),
                stockTechnicals.getVolume().getAvg20(),
                stockTechnicals.getAdx().getAdx(),
                stockTechnicals.getAdx().getPlusDi(),
                stockTechnicals.getAdx().getMinusDi(),
                stockTechnicals.getAdx().getAtr(),
                stockTechnicals.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());

        log.info("{} Updated {} technicals", stock.getNseSymbol(), timeframe);
    }

    @Override
    public StockTechnicals build(Timeframe timeframe, Stock stock, LocalDate sessionDate) {

        String nseSymbol = stock.getNseSymbol();

        LocalDate to =  sessionDate;

        List<OHLCV> ohlcvList = this.fetch(timeframe, nseSymbol, to);

        if(ohlcvList.size() <= 1 ){
            return this.init(stock.getNseSymbol(), sessionDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
        }

        Instant bhavDate = ohlcvList.get(ohlcvList.size()-1).getBhavDate();

        return this.calculate(nseSymbol, bhavDate, ohlcvList);
    }

    private StockTechnicals build(StockPriceIO stockPriceIO){

        String nseSymbol = stockPriceIO.getNseSymbol();

        LocalDate to =  LocalDate.ofInstant(stockPriceIO.getBhavDate(), ZoneOffset.UTC);

        List<OHLCV> ohlcvList = this.fetch(stockPriceIO.getTimeFrame(), nseSymbol, to);

        if(ohlcvList.size() <= 1 ){
            return this.init(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate());
        }

        Instant bhavDate = ohlcvList.get(ohlcvList.size()-1).getBhavDate();

        return this.calculate(nseSymbol, bhavDate, ohlcvList);
    }

    private StockTechnicals init(String nseSymbol, Instant bhavDate){

        StockTechnicals stockTechnicals = new StockTechnicals();

        stockTechnicals.setBhavDate(bhavDate);
        stockTechnicals.setNseSymbol(nseSymbol);

        Volume volume = new Volume(0l, 0l,0l ,0l, 0l);
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

    private List<OHLCV> fetch(Timeframe timeFrame, String nseSymbol, LocalDate to){

        LocalDate from = to.minusYears(3);

        log.info("{} fetching OHLCV from {} to {}", nseSymbol, from, to);

        return  ohlcvService.fetch(timeFrame, nseSymbol, from, to);
    }

    private Volume build(String nseSymbol, List<OHLCV> ohlcvList, Volume prevVolume, long tradingDays){

        OHLCV ohlcv = ohlcvList.get(ohlcvList.size() -1);

        int resultIndex = ohlcvList.size()-1;

        long avgVolume5 = volumeAverageCalculatorService.calculate(ohlcvList, 5).get(resultIndex);
        long avgVolume10 = volumeAverageCalculatorService.calculate(ohlcvList, 10).get(resultIndex);
        long avgVolume20 = volumeAverageCalculatorService.calculate(ohlcvList, 20).get(resultIndex);
        long avgVolume50 = volumeAverageCalculatorService.calculate(ohlcvList, 50).get(resultIndex);

        return new Volume(ohlcv.getVolume(), avgVolume5,avgVolume10, avgVolume20, avgVolume50);
    }

    private OnBalanceVolume build(String nseSymbol, List<OHLCV> ohlcvList, OnBalanceVolume prevOnBalanceVolume, long tradingDays){

        /*
        if(tradingDays > MIN_TRADING_DAYS) {
            return onBalanceVolumeCalculatorService.calculate(ohlcvList, prevOnBalanceVolume);
        }*/

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


        int resultIndex = ohlcvList.size()-1;
        double ema5 = exponentialMovingAverageService.calculate(ohlcvList, 5).get(ohlcvList.size()-1);
        double ema10 = exponentialMovingAverageService.calculate(ohlcvList, 10).get(resultIndex);
        double ema20 = exponentialMovingAverageService.calculate(ohlcvList, 20).get(resultIndex);
        double ema50 = exponentialMovingAverageService.calculate(ohlcvList, 50).get(resultIndex);
        double ema100 = exponentialMovingAverageService.calculate(ohlcvList, 100).get(resultIndex);
        double ema200 = exponentialMovingAverageService.calculate(ohlcvList, 200).get(resultIndex);

        return new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);
    }

    private AverageDirectionalIndex build(String nseSymbol,List<OHLCV> ohlcvList, AverageDirectionalIndex prevAverageDirectionalIndex, long tradingDays){


        return averageDirectionalIndexService.calculate(ohlcvList).get(ohlcvList.size() -1);
    }

    private RelativeStrengthIndex build(String nseSymbol,List<OHLCV> ohlcvList, RelativeStrengthIndex prevRelativeStrengthIndex, long tradingDays){

        return relativeStrengthIndexService.calculate(ohlcvList).get(ohlcvList.size() -1);
    }

    private MovingAverageConvergenceDivergence build(String nseSymbol,List<OHLCV> ohlcvList, MovingAverageConvergenceDivergence prevMovingAverageConvergenceDivergence, long tradingDays){

        return movingAverageConvergenceDivergenceService.calculate(ohlcvList).get(ohlcvList.size() -1);
    }


}
