package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.storage.documents.*;
import com.example.data.storage.repo.PriceTemplate;
import com.example.data.storage.repo.TechnicalsTemplate;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockTechnicalsDaily;
import com.example.data.transactional.entities.StockTechnicalsMonthly;
import com.example.dto.assembler.StockPriceOHLCVAssembler;
import com.example.dto.common.OHLCV;
import com.example.dto.io.StockPriceIO;
import com.example.external.ta.service.McService;
import com.example.service.*;
import com.example.service.StockTechnicalsService;
import com.example.service.calc.*;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UpdateTechnicalsServiceImpl implements UpdateTechnicalsService {
    private static final long MIN_TRADING_DAYS = 32;

    @Autowired private YearlySupportResistanceService yearlySupportResistanceService;

    @Autowired private QuarterlySupportResistanceService quarterlySupportResistanceService;
    @Autowired private MonthlySupportResistanceService monthlySupportResistanceService;

    @Autowired private WeeklySupportResistanceService weeklySupportResistanceService;

    @Autowired private StockService stockService;

    @Autowired private ResearchLedgerFundamentalService researchLedgerFundamentalService;

    @Autowired private TechnicalsTemplate technicalsTemplate;

    @Autowired private FormulaService formulaService;

    @Autowired private PriceTemplate priceTemplate;

    @Autowired private ObjectMapper mapper;

    @Autowired private OnBalanceVolumeCalculatorService onBalanceVolumeCalculatorService;
    @Autowired private RelativeStrengthIndexCalculatorService relativeStrengthIndexService;

    @Autowired private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

    @Autowired private SimpleMovingAverageCalculatorService simpleMovingAverageCalculatorService;

    @Autowired
    private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

    @Autowired private VolumeAverageCalculatorService volumeAverageCalculatorService;

    @Autowired private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;

    @Autowired private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;

    @Autowired
    private StockTechnicalsService<com.example.data.transactional.entities.StockTechnicals>
            stockTechnicalsService;

    @Autowired private OhlcvService ohlcvService;

    @Autowired private WeeklyOhlcvService weeklyOhlcvService;

    @Autowired private MiscUtil miscUtil;

    @Autowired private McService mcService;

    @Autowired private CalendarService calendarService;

    @Override
    public void updateTechnicals(Timeframe timeframe, Stock stock, LocalDate sessionDate) {

        try {
            log.info("{} starting technicals update", stock.getNseSymbol());
            // List<OHLCV> ohlcvList = mcService.getMCOHLP(stock.getNseSymbol(), 3, 700);
            // LocalDate to  = calendarService.previousTradingSession(LocalDate.now());
            List<OHLCV> ohlcvList = this.fetch(timeframe, stock.getNseSymbol(), sessionDate);

            StockTechnicals stockTechnicals =
                    this.calculate(
                            stock.getNseSymbol(),
                            ohlcvList.get(ohlcvList.size() - 1).getBhavDate(),
                            ohlcvList);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

            System.out.println(
                    stock.getNseSymbol() + " : " + " Historical " + " on " + sessionDate);
            System.out.println(objectWriter.writeValueAsString(stockTechnicals));

            technicalsTemplate.upsert(timeframe, stockTechnicals);

            com.example.data.transactional.entities.StockTechnicals stockTechnicals1 =
                    stockTechnicalsService.createOrUpdate(
                            stock,
                            timeframe,
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
                            sessionDate);
            System.out.println(
                    stock.getNseSymbol() + " : " + " Transactional " + " on " + sessionDate);
            System.out.println(stockTechnicals1);

        } catch (Exception e) {
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
        log.info(
                "{} Starting technicals update. {}",
                stockPriceIO.getNseSymbol(),
                stockPriceIO.getTimeFrame());
        try {

            StockTechnicals stockTechnicals = this.build(stockPriceIO);

            // technicalsTemplate.upsert(timeframe, stockTechnicals);
            stockTechnicalsService.createOrUpdate(
                    stock,
                    timeframe,
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
                    stockTechnicals.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());

            // miscUtil.delay(50);
        } catch (Exception e) {

            log.error("{} Error while updating technicals ", stockPriceIO.getNseSymbol(), e);
        }
        log.info("{} Completed technicals update.", stockPriceIO.getNseSymbol());
    }

    private StockTechnicals updateTechnicalsHistory(
            Timeframe timeframe, StockPriceIO stockPriceIO) {
        log.info(
                "{} Updating technicals history timeframe {} ",
                stockPriceIO.getNseSymbol(),
                timeframe);

        Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

        StockTechnicals stockTechnicals = this.build(stockPriceIO);

        technicalsTemplate.upsert(timeframe, stockTechnicals);
        stockTechnicalsService.createOrUpdate(
                stock,
                timeframe,
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
                stockTechnicals.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());

        return stockTechnicals;
    }

    @Override
    public StockTechnicals calculate(
            String nseSymbol,
            Instant bhavDate,
            List<OHLCV> ohlcvList,
            StockTechnicals stockTechnicalsPreviousSession,
            long tradingDays) {
        // SimpleMovingAverage sma = new SimpleMovingAverage(stockPriceIO.getClose(),
        // stockPriceIO.getClose(), stockPriceIO.getClose(), stockPriceIO.getClose(),
        // stockPriceIO.getClose(), stockPriceIO.getClose());

        Volume volume =
                this.build(
                        nseSymbol,
                        ohlcvList,
                        stockTechnicalsPreviousSession.getVolume(),
                        tradingDays);

        OnBalanceVolume onBalanceVolume =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getObv(), tradingDays);

        SimpleMovingAverage sma =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getSma(), tradingDays);

        ExponentialMovingAverage ema =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getEma(), tradingDays);

        AverageDirectionalIndex adx =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getAdx(), tradingDays);

        RelativeStrengthIndex rsi =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getRsi(), tradingDays);

        MovingAverageConvergenceDivergence macd =
                this.build(
                        nseSymbol,
                        ohlcvList,
                        stockTechnicalsPreviousSession.getMacd(),
                        tradingDays);

        return new StockTechnicals(
                nseSymbol, bhavDate, volume, onBalanceVolume, sma, ema, adx, rsi, macd);
    }

    @Override
    public StockTechnicals calculate(String nseSymbol, Instant bhavDate, List<OHLCV> ohlcvList) {

        StockTechnicals stockTechnicalsPreviousSession = this.init(nseSymbol, bhavDate);

        if (ohlcvList.size() <= 1) {
            return stockTechnicalsPreviousSession;
        }

        int tradingDays = 2;

        Volume volume =
                this.build(
                        nseSymbol,
                        ohlcvList,
                        stockTechnicalsPreviousSession.getVolume(),
                        tradingDays);

        OnBalanceVolume onBalanceVolume =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getObv(), tradingDays);

        SimpleMovingAverage sma =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getSma(), tradingDays);

        ExponentialMovingAverage ema =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getEma(), tradingDays);

        AverageDirectionalIndex adx =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getAdx(), tradingDays);

        RelativeStrengthIndex rsi =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getRsi(), tradingDays);

        MovingAverageConvergenceDivergence macd =
                this.build(
                        nseSymbol,
                        ohlcvList,
                        stockTechnicalsPreviousSession.getMacd(),
                        tradingDays);

        return new StockTechnicals(
                nseSymbol, bhavDate, volume, onBalanceVolume, sma, ema, adx, rsi, macd);
    }

    public StockTechnicals calculateBK(String nseSymbol, Instant bhavDate, List<OHLCV> ohlcvList) {

        StockTechnicals stockTechnicalsPreviousSession = this.init(nseSymbol, bhavDate);

        if (ohlcvList.size() <= 1) {
            return stockTechnicalsPreviousSession;
        }

        int tradingDays = 2;

        Volume volume =
                this.buildBK(
                        nseSymbol,
                        ohlcvList,
                        stockTechnicalsPreviousSession.getVolume(),
                        tradingDays);

        /*
        OnBalanceVolume onBalanceVolume =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getObv(), tradingDays);
        */
        SimpleMovingAverage sma =
                this.buildBK(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getSma(), tradingDays);

        ExponentialMovingAverage ema =
                this.buildBK(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getEma(), tradingDays);

        AverageDirectionalIndex adx =
                this.buildBK(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getAdx(), tradingDays);

        RelativeStrengthIndex rsi =
                this.build(
                        nseSymbol, ohlcvList, stockTechnicalsPreviousSession.getRsi(), tradingDays);
        /*
        MovingAverageConvergenceDivergence macd =
                this.build(
                        nseSymbol,
                        ohlcvList,
                        stockTechnicalsPreviousSession.getMacd(),
                        tradingDays);*/

        return new StockTechnicals(
                nseSymbol,
                bhavDate,
                volume,
                new OnBalanceVolume(0l, 0l),
                sma,
                ema,
                adx,
                rsi,
                new MovingAverageConvergenceDivergence());
    }

    @Override
    public void updateTechnicals(
            Timeframe timeframe, Stock stock, StockTechnicals stockTechnicals) {
        log.info("{} Updating {} technicals", stock.getNseSymbol(), timeframe);

        stockTechnicalsService.createOrUpdate(
                stock,
                timeframe,
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
                stockTechnicals.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());

        log.info("{} Updated {} technicals", stock.getNseSymbol(), timeframe);
    }

    @Override
    public StockTechnicals build(Timeframe timeframe, Stock stock, LocalDate sessionDate) {

        String nseSymbol = stock.getNseSymbol();

        LocalDate to = sessionDate;

        List<OHLCV> ohlcvList = this.fetch(timeframe, nseSymbol, to);

        if (ohlcvList.size() <= 1) {
            return this.init(
                    stock.getNseSymbol(),
                    sessionDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
        }

        Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

        return this.calculate(nseSymbol, bhavDate, ohlcvList);
    }

    public StockTechnicals buildBK(Timeframe timeframe, Stock stock, LocalDate sessionDate) {

        String nseSymbol = stock.getNseSymbol();

        LocalDate to = sessionDate;

        List<OHLCV> ohlcvList = this.fetch(timeframe, nseSymbol, to);

        if (ohlcvList.size() <= 1) {
            return this.init(
                    stock.getNseSymbol(),
                    sessionDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
        }

        Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

        return this.calculateBK(nseSymbol, bhavDate, ohlcvList);
    }

    @Override
    // @Cacheable(value = "stockTechnicals", key = "{#stock.nseSymbol, #timeframe, #sessionDate}")
    public com.example.data.transactional.entities.StockTechnicals buildBack(
            Timeframe timeframe, Stock stock, LocalDate sessionDate) {

        StockTechnicals stockTechnicals = this.buildBK(timeframe, stock, sessionDate);

        com.example.data.transactional.entities.StockTechnicals st = null;
        if (timeframe == Timeframe.MONTHLY) {
            st = new StockTechnicalsMonthly();
        } else if (timeframe == Timeframe.WEEKLY) {
            st = new StockTechnicalsMonthly();
        } else {
            st = new StockTechnicalsDaily();
        }

        st.setSessionDate(sessionDate);
        st.setTimeframe(timeframe);
        st.setStock(stock);
        st.setEma5(
                stockTechnicals.getEma().getAvg5() != null
                        ? stockTechnicals.getEma().getAvg5()
                        : 0.00);
        st.setPrevEma5(
                stockTechnicals.getEma().getPrevAvg5() != null
                        ? stockTechnicals.getEma().getPrevAvg5()
                        : 0.00);
        st.setPrev2Ema5(
                stockTechnicals.getEma().getPrev2Avg5() != null
                        ? stockTechnicals.getEma().getPrev2Avg5()
                        : 0.00);

        st.setPrev3Ema5(
                stockTechnicals.getEma().getPrev3Avg5() != null
                        ? stockTechnicals.getEma().getPrev3Avg5()
                        : 0.00);
        st.setPrev4Ema5(
                stockTechnicals.getEma().getPrev4Avg5() != null
                        ? stockTechnicals.getEma().getPrev4Avg5()
                        : 0.00);
        st.setPrev5Ema5(
                stockTechnicals.getEma().getPrev5Avg5() != null
                        ? stockTechnicals.getEma().getPrev5Avg5()
                        : 0.00);
        st.setPrev6Ema5(
                stockTechnicals.getEma().getPrev6Avg5() != null
                        ? stockTechnicals.getEma().getPrev6Avg5()
                        : 0.00);
        st.setPrev7Ema5(
                stockTechnicals.getEma().getPrev7Avg5() != null
                        ? stockTechnicals.getEma().getPrev7Avg5()
                        : 0.00);
        st.setPrev8Ema5(
                stockTechnicals.getEma().getPrev8Avg5() != null
                        ? stockTechnicals.getEma().getPrev8Avg5()
                        : 0.00);
        st.setPrev9Ema5(
                stockTechnicals.getEma().getPrev9Avg5() != null
                        ? stockTechnicals.getEma().getPrev9Avg5()
                        : 0.00);
        st.setPrev10Ema5(
                stockTechnicals.getEma().getPrev10Avg5() != null
                        ? stockTechnicals.getEma().getPrev10Avg5()
                        : 0.00);
        st.setPrev11Ema5(
                stockTechnicals.getEma().getPrev11Avg5() != null
                        ? stockTechnicals.getEma().getPrev11Avg5()
                        : 0.00);
        st.setEma10(
                stockTechnicals.getEma().getAvg10() != null
                        ? stockTechnicals.getEma().getAvg10()
                        : 0.00);
        st.setPrevEma10(
                stockTechnicals.getEma().getPrevAvg10() != null
                        ? stockTechnicals.getEma().getPrevAvg10()
                        : 0.00);
        st.setPrev2Ema10(
                stockTechnicals.getEma().getPrev2Avg10() != null
                        ? stockTechnicals.getEma().getPrev2Avg10()
                        : 0.00);

        st.setEma20(
                stockTechnicals.getEma().getAvg20() != null
                        ? stockTechnicals.getEma().getAvg20()
                        : 0.00);
        st.setPrevEma20(
                stockTechnicals.getEma().getPrevAvg20() != null
                        ? stockTechnicals.getEma().getPrevAvg20()
                        : 0.00);
        st.setPrev2Ema20(
                stockTechnicals.getEma().getPrev2Avg20() != null
                        ? stockTechnicals.getEma().getPrev2Avg20()
                        : 0.00);

        st.setEma50(
                stockTechnicals.getEma().getAvg50() != null
                        ? stockTechnicals.getEma().getAvg50()
                        : 0.00);
        st.setPrevEma50(
                stockTechnicals.getEma().getPrevAvg50() != null
                        ? stockTechnicals.getEma().getPrevAvg50()
                        : 0.00);
        st.setPrev2Ema50(
                stockTechnicals.getEma().getPrev2Avg50() != null
                        ? stockTechnicals.getEma().getPrev2Avg50()
                        : 0.00);

        st.setEma100(stockTechnicals.getEma().getAvg100());
        st.setPrevEma100(stockTechnicals.getEma().getPrevAvg100());
        st.setPrev2Ema100(stockTechnicals.getEma().getPrev2Avg100());

        st.setEma200(stockTechnicals.getEma().getAvg200());
        st.setPrevEma200(stockTechnicals.getEma().getPrevAvg200());
        st.setPrev2Ema200(stockTechnicals.getEma().getPrev2Avg200());

        st.setSma100(stockTechnicals.getSma().getAvg100());
        st.setPrevSma100(stockTechnicals.getSma().getPrevAvg100());
        st.setPrev2Sma100(stockTechnicals.getSma().getPrev2Avg100());

        st.setSma200(stockTechnicals.getSma().getAvg200());
        st.setPrevSma200(stockTechnicals.getSma().getPrevAvg200());
        st.setPrev2Sma200(stockTechnicals.getSma().getPrev2Avg200());

        st.setVolume(stockTechnicals.getVolume().getVolume());
        st.setPrevVolume(stockTechnicals.getVolume().getPrevVolume());
        st.setPrev2Volume(stockTechnicals.getVolume().getPrev2Volume());

        st.setVolumeAvg5(stockTechnicals.getVolume().getAvg5());
        st.setPrevVolumeAvg5(stockTechnicals.getVolume().getPrevAvg5());
        st.setPrev2VolumeAvg5(stockTechnicals.getVolume().getPrev2Avg5());

        st.setVolumeAvg10(stockTechnicals.getVolume().getAvg10());
        st.setPrevVolumeAvg10(stockTechnicals.getVolume().getPrevAvg10());
        st.setPrev2VolumeAvg10(stockTechnicals.getVolume().getPrev2Avg10());

        st.setVolumeAvg20(stockTechnicals.getVolume().getAvg20());
        st.setPrevVolumeAvg20(stockTechnicals.getVolume().getPrevAvg20());
        st.setPrev2VolumeAvg20(stockTechnicals.getVolume().getPrev2Avg20());

        st.setRsi(stockTechnicals.getRsi().getRsi());

        st.setAdx(stockTechnicals.getAdx().getAdx());
        st.setPrevAdx(stockTechnicals.getAdx().getPrevAdx());

        // System.out.println(st);
        return st;
    }

    private StockTechnicals build(StockPriceIO stockPriceIO) {

        String nseSymbol = stockPriceIO.getNseSymbol();

        LocalDate to = LocalDate.ofInstant(stockPriceIO.getBhavDate(), ZoneOffset.UTC);

        List<OHLCV> ohlcvList = this.fetch(stockPriceIO.getTimeFrame(), nseSymbol, to);

        if (ohlcvList.size() <= 1) {
            return this.init(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate());
        }

        Instant bhavDate = ohlcvList.get(ohlcvList.size() - 1).getBhavDate();

        return this.calculate(nseSymbol, bhavDate, ohlcvList);
    }

    private StockTechnicals init(String nseSymbol, Instant bhavDate) {

        StockTechnicals stockTechnicals = new StockTechnicals();

        stockTechnicals.setBhavDate(bhavDate);
        stockTechnicals.setNseSymbol(nseSymbol);

        Volume volume = new Volume(0l, 0l, 0l, 0l, 0l);
        stockTechnicals.setVolume(volume);

        OnBalanceVolume onBalanceVolume = new OnBalanceVolume(0l, 0l);
        stockTechnicals.setObv(onBalanceVolume);

        SimpleMovingAverage sma = new SimpleMovingAverage(0.00, 0.00, 0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setSma(sma);

        ExponentialMovingAverage ema =
                new ExponentialMovingAverage(0.00, 0.00, 0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setEma(ema);

        AverageDirectionalIndex adx =
                new AverageDirectionalIndex(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setAdx(adx);

        RelativeStrengthIndex rsi = new RelativeStrengthIndex(0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setRsi(rsi);

        MovingAverageConvergenceDivergence macd =
                new MovingAverageConvergenceDivergence(0.00, 0.00, 0.00, 0.00);
        stockTechnicals.setMacd(macd);

        return stockTechnicals;
    }

    private int limit(long tradingDays) {
        return tradingDays > MIN_TRADING_DAYS ? 2 : 700;
    }

    private List<OHLCV> fetch(Timeframe timeFrame, String nseSymbol, LocalDate to) {

        LocalDate from = to.minusYears(3);

        if (timeFrame == Timeframe.WEEKLY) {
            from = to.minusYears(5);
        }

        if (timeFrame == Timeframe.MONTHLY) {
            from = to.minusYears(17);
        }

        log.info("{} fetching {} OHLCV from {} to {}", nseSymbol, timeFrame, from, to);

        return ohlcvService.fetch(timeFrame, nseSymbol, from, to);
    }

    private Volume build(
            String nseSymbol, List<OHLCV> ohlcvList, Volume prevVolume, long tradingDays) {

        OHLCV ohlcv = ohlcvList.get(ohlcvList.size() - 1);

        int resultIndex = ohlcvList.size() - 1;

        long avgVolume5 = volumeAverageCalculatorService.calculate(ohlcvList, 5).get(resultIndex);
        long avgVolume10 = volumeAverageCalculatorService.calculate(ohlcvList, 10).get(resultIndex);
        long avgVolume20 = volumeAverageCalculatorService.calculate(ohlcvList, 20).get(resultIndex);
        long avgVolume50 = volumeAverageCalculatorService.calculate(ohlcvList, 50).get(resultIndex);

        return new Volume(ohlcv.getVolume(), avgVolume5, avgVolume10, avgVolume20, avgVolume50);
    }

    private Volume buildBK(
            String nseSymbol, List<OHLCV> ohlcvList, Volume prevVolumeObj, long tradingDays) {

        int resultIndex = ohlcvList.size() - 1;

        OHLCV ohlcv = ohlcvList.get(resultIndex);
        List<Long> avg5List = volumeAverageCalculatorService.calculate(ohlcvList, 5);
        List<Long> avg10List = volumeAverageCalculatorService.calculate(ohlcvList, 12);
        List<Long> avg20List = volumeAverageCalculatorService.calculate(ohlcvList, 20);

        long avgVolume5 = avg5List.get(resultIndex);
        long avgVolume10 = avg10List.get(resultIndex);
        long avgVolume20 = avg20List.get(resultIndex);

        Volume volume = new Volume(ohlcv.getVolume(), avgVolume5, avgVolume10, avgVolume20, 0l);

        // --- Previous day volumes (prev) ---
        long prevVolume = 0l,
                prevVolume5 = 0L,
                prevVolume10 = 0L,
                prevAvg5 = 0l,
                prevAvg10 = 0l,
                prevAvg20 = 0L,
                prevVolume50 = 0L;
        if (resultIndex >= 1) {
            prevAvg5 = avg5List.get(resultIndex - 1);
            prevAvg10 = avg10List.get(resultIndex - 1);
            prevAvg20 = avg20List.get(resultIndex - 1);
            ohlcv = ohlcvList.get(resultIndex - 1);
            prevVolume = ohlcv.getVolume();
        }
        // System.out.println(nseSymbol +" prevAvg10 " +prevAvg10 +" prevAvg20" +prevAvg20);
        volume.setPrevVolume(prevVolume);
        volume.setPrevAvg10(prevAvg10);
        volume.setPrevAvg5(prevAvg5);
        volume.setPrevAvg20(prevAvg20);

        // --- Day-before-previous volumes (prev2) ---
        long prev2Volume = 0l,
                prev2Volume5 = 0L,
                prev2Volume10 = 0L,
                prev2Avg10 = 0l,
                prev2Avg20 = 0L,
                prev2Volume50 = 0L;
        if (resultIndex >= 2) {
            prev2Volume5 = avg5List.get(resultIndex - 2);
            prev2Avg10 = avg10List.get(resultIndex - 2);
            prev2Avg20 = avg20List.get(resultIndex - 2);
            ohlcv = ohlcvList.get(resultIndex - 2);
            prev2Volume = ohlcv.getVolume();
        }
        volume.setPrev2Volume(prev2Volume);
        volume.setPrev2Avg20(prev2Avg20);
        volume.setPrev2Avg10(prev2Avg10);
        volume.setPrev2Avg5(prev2Volume5);

        return volume;
    }

    private OnBalanceVolume build(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            OnBalanceVolume prevOnBalanceVolume,
            long tradingDays) {

        /*
        if(tradingDays > MIN_TRADING_DAYS) {
            return onBalanceVolumeCalculatorService.calculate(ohlcvList, prevOnBalanceVolume);
        }*/

        return onBalanceVolumeCalculatorService.calculate(ohlcvList).get(ohlcvList.size() - 1);
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

        return sum / count;
    }

    private SimpleMovingAverage build(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            SimpleMovingAverage prevSimpleMovingAverage,
            long tradingDays) {

        int resultIndex = ohlcvList.size() - 1;
        double sma5 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 5).get(resultIndex);
        ;
        double sma10 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 10).get(resultIndex);
        double sma20 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 20).get(resultIndex);
        double sma50 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 50).get(resultIndex);
        double sma100 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 100).get(resultIndex);
        double sma200 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 200).get(resultIndex);

        return new SimpleMovingAverage(
                miscUtil.formatDouble(sma5, "00"),
                miscUtil.formatDouble(sma10, "00"),
                miscUtil.formatDouble(sma20, "00"),
                miscUtil.formatDouble(sma50, "00"),
                miscUtil.formatDouble(sma100, "00"),
                miscUtil.formatDouble(sma200, "00"));
    }

    private SimpleMovingAverage buildBK(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            SimpleMovingAverage prevSimpleMovingAverage,
            long tradingDays) {

        int resultIndex = ohlcvList.size() - 1;
        double sma5 = simpleMovingAverageCalculatorService.calculate(ohlcvList, 5).get(resultIndex);
        double sma10 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 10).get(resultIndex);
        double sma20 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 20).get(resultIndex);
        double sma50 =
                simpleMovingAverageCalculatorService.calculate(ohlcvList, 50).get(resultIndex);
        List<Double> sma100List = simpleMovingAverageCalculatorService.calculate(ohlcvList, 100);
        List<Double> sma200List = simpleMovingAverageCalculatorService.calculate(ohlcvList, 200);

        SimpleMovingAverage simpleMovingAverage =
                new SimpleMovingAverage(
                        miscUtil.formatDouble(sma5, "00"),
                        miscUtil.formatDouble(sma10, "00"),
                        miscUtil.formatDouble(sma20, "00"),
                        miscUtil.formatDouble(sma50, "00"),
                        miscUtil.formatDouble(sma100List.get(resultIndex), "00"),
                        miscUtil.formatDouble(sma200List.get(resultIndex), "00"));

        // --- Previous EMA values (yesterday) ---
        double prevEma5 = 0.0,
                prevEma10 = 0.0,
                prevEma20 = 0.0,
                prevEma50 = 0.0,
                prevSma100 = 0.0,
                prevSma200 = 0.0;
        if (resultIndex >= 1) {

            prevSma100 = sma100List.get(resultIndex - 1);
            prevSma200 = sma200List.get(resultIndex - 1);
        }

        simpleMovingAverage.setPrevAvg100(miscUtil.formatDouble(prevSma100, "00"));
        simpleMovingAverage.setPrevAvg200(miscUtil.formatDouble(prevSma200, "00"));
        // --- Previous2 EMA values (day before yesterday) ---
        double prev2Ema5 = 0.0,
                prev2Ema10 = 0.0,
                prev2Ema20 = 0.0,
                prev2Ema50 = 0.0,
                prev2Sma100 = 0.0,
                prev2Sma200 = 0.0;
        if (resultIndex >= 2) {
            prev2Sma100 = sma100List.get(resultIndex - 2);
            prev2Sma200 = sma200List.get(resultIndex - 2);
        }
        simpleMovingAverage.setPrevAvg100(miscUtil.formatDouble(prev2Sma100, "00"));
        simpleMovingAverage.setPrevAvg200(miscUtil.formatDouble(prev2Sma200, "00"));
        return simpleMovingAverage;
    }

    private ExponentialMovingAverage build(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            ExponentialMovingAverage prevExponentialMovingAverage,
            long tradingDays) {

        int resultIndex = ohlcvList.size() - 1;
        double ema5 =
                exponentialMovingAverageService.calculate(ohlcvList, 5).get(ohlcvList.size() - 1);
        double ema10 = exponentialMovingAverageService.calculate(ohlcvList, 10).get(resultIndex);
        double ema20 = exponentialMovingAverageService.calculate(ohlcvList, 20).get(resultIndex);
        double ema50 = exponentialMovingAverageService.calculate(ohlcvList, 50).get(resultIndex);
        double ema100 = exponentialMovingAverageService.calculate(ohlcvList, 100).get(resultIndex);
        double ema200 = exponentialMovingAverageService.calculate(ohlcvList, 200).get(resultIndex);

        return new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);
    }

    private ExponentialMovingAverage buildBK(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            ExponentialMovingAverage prevExponentialMovingAverage,
            long tradingDays) {
        int resultIndex = ohlcvList.size() - 1;
        // Compute all EMA series once
        List<Double> ema5List = exponentialMovingAverageService.calculate(ohlcvList, 5);
        List<Double> ema10List = exponentialMovingAverageService.calculate(ohlcvList, 10);
        List<Double> ema20List = exponentialMovingAverageService.calculate(ohlcvList, 20);
        List<Double> ema50List = exponentialMovingAverageService.calculate(ohlcvList, 50);
        List<Double> ema100List = exponentialMovingAverageService.calculate(ohlcvList, 100);
        List<Double> ema200List = exponentialMovingAverageService.calculate(ohlcvList, 200);

        // --- Current EMA values ---
        double ema5 = ema5List.get(resultIndex);
        double ema10 = ema10List.get(resultIndex);
        double ema20 = ema20List.get(resultIndex);
        double ema50 = ema50List.get(resultIndex);
        double ema100 = ema100List.get(resultIndex);
        double ema200 = ema200List.get(resultIndex);

        // --- Previous EMA values (yesterday) ---
        double prevEma5 = 0.0,
                prevEma10 = 0.0,
                prevEma20 = 0.0,
                prevEma50 = 0.0,
                prevEma100 = 0.0,
                prevEma200 = 0.0;
        if (resultIndex >= 1) {
            prevEma5 = ema5List.get(resultIndex - 1);
            prevEma10 = ema10List.get(resultIndex - 1);
            prevEma20 = ema20List.get(resultIndex - 1);
            prevEma50 = ema50List.get(resultIndex - 1);
            prevEma100 = ema100List.get(resultIndex - 1);
            prevEma200 = ema200List.get(resultIndex - 1);
        }

        // --- Previous2 EMA values (day before yesterday) ---
        double prev2Ema5 = 0.0,
                prev2Ema10 = 0.0,
                prev2Ema20 = 0.0,
                prev2Ema50 = 0.0,
                prev2Ema100 = 0.0,
                prev2Ema200 = 0.0;
        if (resultIndex >= 2) {
            prev2Ema5 = ema5List.get(resultIndex - 2);
            prev2Ema10 = ema10List.get(resultIndex - 2);
            prev2Ema20 = ema20List.get(resultIndex - 2);
            prev2Ema50 = ema50List.get(resultIndex - 2);
            prev2Ema100 = ema100List.get(resultIndex - 2);
            prev2Ema200 = ema200List.get(resultIndex - 2);
        }

        ExponentialMovingAverage exponentialMovingAverage =
                new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);

        exponentialMovingAverage.setPrevAvg5(prevEma5);
        exponentialMovingAverage.setPrevAvg10(prevEma10);
        exponentialMovingAverage.setPrevAvg20(prevEma20);
        exponentialMovingAverage.setPrevAvg50(prevEma50);
        exponentialMovingAverage.setPrevAvg100(prevEma100);
        exponentialMovingAverage.setPrevAvg200(prevEma200);

        exponentialMovingAverage.setPrev2Avg5(prev2Ema5);
        exponentialMovingAverage.setPrev2Avg10(prev2Ema10);
        exponentialMovingAverage.setPrev2Avg20(prev2Ema20);
        exponentialMovingAverage.setPrev2Avg50(prev2Ema50);
        exponentialMovingAverage.setPrev2Avg100(prev2Ema100);
        exponentialMovingAverage.setPrev2Avg200(prev2Ema200);

        double prev3Ema5 = 0.0;
        if (resultIndex >= 3) {
            prev3Ema5 = ema5List.get(resultIndex - 3);
        }
        exponentialMovingAverage.setPrev3Avg5(prev3Ema5);
        double prev4Ema5 = 0.0;
        if (resultIndex >= 4) {
            prev4Ema5 = ema5List.get(resultIndex - 4);
        }
        exponentialMovingAverage.setPrev4Avg5(prev4Ema5);
        double prev5Ema5 = 0.0;
        if (resultIndex >= 5) {
            prev5Ema5 = ema5List.get(resultIndex - 5);
        }
        exponentialMovingAverage.setPrev5Avg5(prev5Ema5);
        double prev6Ema5 = 0.0;
        if (resultIndex >= 6) {
            prev6Ema5 = ema5List.get(resultIndex - 6);
        }
        exponentialMovingAverage.setPrev6Avg5(prev6Ema5);
        double prev7Ema5 = 0.0;
        if (resultIndex >= 7) {
            prev7Ema5 = ema5List.get(resultIndex - 7);
        }
        exponentialMovingAverage.setPrev7Avg5(prev7Ema5);
        double prev8Ema5 = 0.0;
        if (resultIndex >= 8) {
            prev8Ema5 = ema5List.get(resultIndex - 8);
        }
        exponentialMovingAverage.setPrev8Avg5(prev8Ema5);
        double prev9Ema5 = 0.0;
        if (resultIndex >= 9) {
            prev9Ema5 = ema5List.get(resultIndex - 9);
        }
        exponentialMovingAverage.setPrev9Avg5(prev9Ema5);
        double prev10Ema5 = 0.0;
        if (resultIndex >= 10) {
            prev10Ema5 = ema5List.get(resultIndex - 10);
        }
        exponentialMovingAverage.setPrev10Avg5(prev10Ema5);

        double prev11Ema5 = 0.0;
        if (resultIndex >= 11) {
            prev11Ema5 = ema5List.get(resultIndex - 11);
        }
        exponentialMovingAverage.setPrev11Avg5(prev11Ema5);
        return exponentialMovingAverage;
    }

    private AverageDirectionalIndex build(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            AverageDirectionalIndex prevAverageDirectionalIndex,
            long tradingDays) {

        return averageDirectionalIndexService.calculate(ohlcvList).get(ohlcvList.size() - 1);
    }

    private AverageDirectionalIndex buildBK(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            AverageDirectionalIndex prevAverageDirectionalIndex,
            long tradingDays) {

        AverageDirectionalIndex averageDirectionalIndex =
                averageDirectionalIndexService.calculate(ohlcvList).get(ohlcvList.size() - 1);
        prevAverageDirectionalIndex =
                averageDirectionalIndexService.calculate(ohlcvList).get(ohlcvList.size() - 2);
        averageDirectionalIndex.setPrevAdx(prevAverageDirectionalIndex.getAdx());
        return averageDirectionalIndex;
    }

    private RelativeStrengthIndex build(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            RelativeStrengthIndex prevRelativeStrengthIndex,
            long tradingDays) {

        return relativeStrengthIndexService.calculate(ohlcvList).get(ohlcvList.size() - 1);
    }

    private MovingAverageConvergenceDivergence build(
            String nseSymbol,
            List<OHLCV> ohlcvList,
            MovingAverageConvergenceDivergence prevMovingAverageConvergenceDivergence,
            long tradingDays) {

        return movingAverageConvergenceDivergenceService
                .calculate(ohlcvList)
                .get(ohlcvList.size() - 1);
    }
}
