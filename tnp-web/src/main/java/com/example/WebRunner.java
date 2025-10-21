package com.example;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.storage.repo.PriceTemplate;
import com.example.data.storage.repo.TechnicalsTemplate;
import com.example.data.transactional.entities.*;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.*;
import com.example.data.transactional.repo.TradingHolidayRepository;
import com.example.dto.assembler.StockPriceOHLCVAssembler;
import com.example.dto.common.OHLCV;
import com.example.dto.common.TradeSetup;
import com.example.dto.integration.StockOverviewResponse;
import com.example.dto.io.*;
import com.example.external.*;
import com.example.external.factor.FactorRediff;
import com.example.external.ta.service.McService;
import com.example.processor.BhavProcessor;
import com.example.service.*;
import com.example.service.calc.*;
import com.example.service.dhan.DhanConsentLoginService;
import com.example.service.dhan.DhanOrchestratorService;
import com.example.service.dhan.DhanOrderExecutorService;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.strategy.*;
import com.example.service.utils.*;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.ThreadsUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class WebRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebRunner.class);

    @Autowired private DhanOrderSchedulerHelperService dhanOrderSchedulerHelperService;

    @Autowired private BhavcopyService bhavcopyService;

    @Autowired private OHLCVAggregatorService ohlcvAggregatorService;
    @Autowired private UserService userService;

    @Autowired private ResearchLedgerFundamentalService researchLedgerFundamentalService;
    @Autowired private OhlcvService ohlcvService;
    @Autowired private StockService stockService;
    @Autowired private SectorService sectorService;

    @Autowired private FundsLedgerService fundsLedgerService;

    @Autowired private TradingHolidayRepository tradingHolidayRepository;
    @Autowired private CalendarService calendarService;
    @Autowired private MiscUtil miscUtil;

    @Autowired private DhanOrderExecutorService dhanOrderExecutorService;

    @Autowired private CandleStickConfirmationService candleStickConfirmationService;

    @Autowired private BhavProcessor bhavProcessor;
    @Autowired private NSEIndustryFetcher sectorScrappingService;
    @Autowired private UpdatePriceService updatePriceService;

    @Autowired private TechnicalsTemplate technicalsTemplate;
    @Autowired private PriceTemplate priceTemplate;

    @Autowired private FormulaService formulaService;

    @Autowired private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;

    @Autowired private MovingAverageActionService movingAverageActionService;

    @Autowired private StockFactorService stockFactorService;

    @Autowired private FactorRediff factorRediff;

    @Autowired private FundsLedgerRepository fundsLedgerRepository;

    @Autowired
    @Qualifier("simplePriceActionSignalEvaluator")
    private TradeSignalEvaluator simplePriceActionSignalEvaluator;

    @Autowired private ResearchExecutorService researchExecutorService;

    @Autowired private OnBalanceVolumeCalculatorService onBalanceVolumeCalculatorService;

    @Autowired private RelativeStrengthIndexCalculatorService rsiService;

    @Autowired private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

    @Autowired
    private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

    @Autowired private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;
    @Autowired private DailySupportResistanceService dailySupportResistanceService;
    @Autowired private McService mcService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private QuarterlySupportResistanceService quarterlySupportResistanceService;
    @Autowired private MonthlySupportResistanceService monthlySupportResistanceService;
    @Autowired private WeeklySupportResistanceService weeklySupportResistanceService;
    @Autowired private StockRepository stockRepository;

    @Autowired private StockPriceHelperService stockPriceHelperService;
    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private ResistanceValidationService resistanceValidationService;
    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Autowired private RsiIndicatorService rsiIndicatorService;
    @Autowired private BreakoutService breakoutService;

    @Autowired private SupportResistanceUtilService supportResistanceUtilService;

    @Autowired private NSEPriceInfoFetcher nsePriceInfoFetcher;
    @Autowired private ResearchTechnicalService<ResearchTechnical> researchTechnicalService;

    @Autowired private CandleStickConfirmationService candleStickHelperService;
    @Autowired private FundamentalResearchService fundamentalResearchService;

    @Autowired private CandleStickService candleStickService;
    @Autowired private UpdateTechnicalsService updateTechnicalsService;
    @Autowired private PositionService positionService;
    @Autowired private TrendService trendService;
    @Autowired private DynamicTrendService dynamicTrendService;
    @Autowired private YearlySupportResistanceService yearlySupportResistanceService;

    @Autowired
    private MultiTimeframeSupportResistanceService multiTimeframeSupportResistanceService;

    @Autowired private SectorDownloadService sectorDownloadService;

    @Autowired private StockFinancialsService stockFinancialsService;
    @Autowired private NSEFinancialsFetcher nseFinancialsFetcher;
    @Autowired private NSEXmlService nseXmlService;
    @Autowired private FinancialsSummaryService financialsSummaryService;
    @Autowired private StockPriceRepository stockPriceRepository;

    @Autowired
    private NSETotalIssuedSharesAndFaceValueFetcher nseTotalIssuedSharesAndFaceValueFetcher;

    @Autowired private VolumeIndicatorService volumeIndicatorService;

    @Autowired
    private DynamicMovingAverageSupportResolverService dynamicMovingAverageSupportResolverService;

    @Autowired private Research360Client research360Client;

    @Autowired private DhanOrchestratorService orchestratorService;

    @Autowired private ResearchTechnicalRepository researchTechnicalRepository;
    @Autowired private BillingService billingService;

    @Autowired
    @Qualifier("investmentPriceActionSignalEvaluator")
    private InvestmentPriceActionSignalEvaluator investmentPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("megaPriceActionSignalEvaluator")
    private MegaPriceActionSignalEvaluator megaPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("priceActionService")
    private PriceActionServiceImpl priceActionService;

    @Autowired private EntryPriceService entryPriceService;

    @Autowired private TotpService totpService;

    @Autowired private DhanConsentLoginService dhanConsentLoginService;

    @Autowired private BottomPriceActionSignalEvaluator bottomPriceActionSignalEvaluator;

    @Override
    public void run(String... arg0) throws InterruptedException, IOException {

        // https://dhanhq.co/docs/v2/authentication/#access-token
        log.info("Application started....");

        //  bhavProcessor.processTechnicals();
        bhavProcessor.processResearch();
        //  this.allocatePositions();

        /*
        List<StockAnalysis> weekly = new ArrayList<>();
        List<StockAnalysis> monthly = new ArrayList<>();
        // To be execute on last session of month or Week
        //last session candle should be  green
        weekly.addAll(this.newAlgoTest(Timeframe.WEEKLY));
        monthly.addAll(this.newAlgoTest(Timeframe.MONTHLY));
        // To be execute at the end of 1st and 2nd session
        weekly.addAll(this.newAlgo2(Timeframe.WEEKLY));
        monthly.addAll(this.newAlgo2(Timeframe.MONTHLY));
        // To be execute at the end of 1st and 2nd session
        weekly.addAll(this.findWeeklySimpleBreakout());
        monthly.addAll(this.findMonthlySimpleBreakout());

        System.out.println("WEEKLY Analyzed: ");
        this.sortAndPrint(weekly);
        System.out.println("MONTHLY Analyzed: ");
        this.sortAndPrint(monthly);
        */
        /*
         int[] digits = totpService.generateToken("CQL4D7FZTBCH2JA7VITTCPHVG3C4YEJR");

         System.out.print("Digits array: ");
         for (int d : digits) {
             System.out.print(d + " ");
         }
         System.out.print("\n");

        */

        /*
         User user = userService.getUserByUsername("ritudhan");
        String token = dhanConsentLoginService.loginAndGetTokenId(user);
         System.out.println(token);
         */

        /*
        Stock stock = stockService.getStockByNseSymbol("HINDUNILVR");
        StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, Timeframe.DAILY);
        Optional<ResearchTechnical> researchTechnicalOptional = researchTechnicalService.getLatest(stock);
        if(researchTechnicalOptional.isPresent()){
            double entryPrice = entryPriceService.calculate(stockPrice, stockTechnicals, researchTechnicalOptional.get());
            System.out.println(entryPrice);
        }*/

        // this.showBilling();
        // this.makePayment();

        /*
        List<Stock> stocks = new ArrayList<>();
        Stock testStock = stockService.getStockByNseSymbol("THEJO");
        stocks.add(testStock);
         testStock = stockService.getStockByNseSymbol("NDRAUTO");
        stocks.add(testStock);
        for(Stock stock : stocks) {

            System.out.println(stock.getNseSymbol());

            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);

            System.out.println(stockPrice.getPrev6Open() + "," + stockPrice.getPrev6High() + "," + stockPrice.getPrev6Low() + "," + stockPrice.getPrev6Close());
            System.out.println(stockPrice.getPrev5Open() + "," + stockPrice.getPrev5High() + "," + stockPrice.getPrev5Low() + "," + stockPrice.getPrev5Close());
            System.out.println(stockPrice.getPrev4Open() + "," + stockPrice.getPrev4High() + "," + stockPrice.getPrev4Low() + "," + stockPrice.getPrev4Close());
            System.out.println(stockPrice.getPrev3Open() + "," + stockPrice.getPrev3High() + "," + stockPrice.getPrev3Low() + "," + stockPrice.getPrev3Close());
            System.out.println(stockPrice.getPrev2Open() + "," + stockPrice.getPrev2High() + "," + stockPrice.getPrev2Low() + "," + stockPrice.getPrev2Close());
            System.out.println(stockPrice.getPrevOpen() + "," + stockPrice.getPrevHigh() + "," + stockPrice.getPrevLow() + "," + stockPrice.getPrevClose());

            SupportResistanceZones supportResistanceZones = SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);

            SupportResistanceZoneUtils.Zone zone = supportResistanceZones.getResistance();

            System.out.println(zone.getStart() + "-" + zone.getEnd());

            zone = supportResistanceZones.getSupport();

            System.out.println(zone.getStart() + "-" + zone.getEnd());

            System.out.println("Outside Resistance " +  resistanceValidationService.isOutsideResistanceZone(
                    stockPrice));
            System.out.println("Outside Support " +  resistanceValidationService.isOutsideSupportZone(
                    stockPrice));
            System.out.println("Inside Resistance " +  resistanceValidationService.isInsideResistanceZone(
                    stockPrice));
            System.out.println("Inside Support " +  resistanceValidationService.isInsideResistanceZone(
                    stockPrice));
        }
        */
        /*
        List<ResearchTechnical> researchTechnicalList = researchTechnicalRepository.findAll();
        for (ResearchTechnical researchTechnical : researchTechnicalList) {
            PriceInfoDto priceInfoDto =
                    nsePriceInfoFetcher.getPriceInfo(researchTechnical.getStock().getNseSymbol());
            System.out.println(researchTechnical.getStock().getNseSymbol());
            System.out.println(priceInfoDto.getPriceBand());
            System.out.println(priceInfoDto.getTickSize());
            researchTechnical.setTickSize(priceInfoDto.getTickSize());
            researchTechnical.setPriceBand(priceInfoDto.getPriceBand());
            researchTechnicalRepository.save(researchTechnical);
        }

         */

        // testdetectMArketConfition();
        // testSupportResistanceZones();
        /*
        Stock stock = stockService.getStockByNseSymbol("360ONE");
        StockTechnicals stockTechnicals = updateTechnicalsService.build(Timeframe.MONTHLY, stock, LocalDate.of(2024,9,30));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        System.out.println(objectWriter.writeValueAsString(stockTechnicals));
         */

        // this.testCandleStick();
        // this.updateYearHighLow();
        // this.testObv();
        // this.testTimeFrameSR();

        // this.scanBullishCandleStickPattern();
        // this.scanBearishCandleStickPattern();
        // this.testTrend();

        // this.updateFinaicials();
        // this.testScore();
        // this.updatePriceHistory();
        // this.updateTechnicals();
        //  this.processPriceUpdate(false);
        // this.processTechnicalsUpdate();

        // this.updateSectorsActivity();
        // this.updateRemainigSectorsActivityFromNSE();
        // this.updateFinancialsForStocks();

        // this.updateSupportAndResistance();
        // updateTechnicalsService.updateTechnicals();
        // this.syncTechnicals();
        /*
        System.out.println(miscUtil.previousMonthFirstDay());
        System.out.println(miscUtil.previousMonthLastDay());
        LocalDate from = calendarService.nextTradingDate(miscUtil.previousMonthFirstDay().minusDays(1));
        LocalDate to  = calendarService.previousTradingSession(miscUtil.currentMonthFirstDay());

        List<StockPrice> stockPriceListNew =  priceTemplate.get("BAJFINANCE", LocalDate.of(2024, 12, 27), LocalDate.of(2025,1,28));

        stockPriceListNew.forEach(stockPrice -> {
        	System.out.println(stockPrice);
        });

        System.out.println("*************");
        StockPrice monthlyHigh = Collections.max(stockPriceListNew, Comparator.comparingDouble(p -> p.getHigh()));
        System.out.println(monthlyHigh);
        System.out.println("monthlyHigh: " + monthlyHigh.getHigh());
        System.out.println("*************");
        StockPrice monthlyLow = Collections.min(stockPriceListNew, Comparator.comparingDouble(p -> p.getLow()));
        System.out.println(monthlyLow);
        System.out.println("monthlyLow: " + monthlyLow.getLow());

        stockPriceListNew =  priceTemplate.get("BAJFINANCE", 20);


        stockPriceListNew.forEach(stockPrice -> {
        	System.out.println(stockPrice);
        });

        System.out.println("*************");
         monthlyHigh = Collections.max(stockPriceListNew, Comparator.comparingDouble(p -> p.getHigh()));
        System.out.println(monthlyHigh);
        System.out.println("monthlyHigh: " + monthlyHigh.getHigh());
        System.out.println("*************");
         monthlyLow = Collections.min(stockPriceListNew, Comparator.comparingDouble(p -> p.getLow()));
        System.out.println(monthlyLow);
        System.out.println("monthlyLow: " + monthlyLow.getLow());

        System.out.println("*************");
         */
        // this.testmcap();
        // this.testSignalEvaluator();
        // this.testDynamicSR();
        // this.updateScore();
        // this.testResearch360();
        // this.updatePivotLevels();
        System.out.println("STARTED");
    }

    public void testSignalEvaluator() {
        Stock stock = stockService.getStockByNseSymbol("DOMS");
        StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, Timeframe.DAILY);
        simplePriceActionSignalEvaluator.evaluateEntry(
                Timeframe.DAILY, stock, stockPrice, stockTechnicals);
    }

    private void testdetectMArketConfition() {
        /*
               List<Stock> stocks = new ArrayList<>();
               Stock stockToAdd = stockService.getStockByNseSymbol("SAPPHIRE");
               stocks.add(stockToAdd);
               stockToAdd = stockService.getStockByNseSymbol("SERVOTECH");
               stocks.add(stockToAdd);
               stockToAdd = stockService.getStockByNseSymbol("LTIM");
               stocks.add(stockToAdd);
               stockToAdd = stockService.getStockByNseSymbol("DOMS");
               stocks.add(stockToAdd);

        */

        List<Stock> stocks = stockService.getActiveStocks();

        for (Stock stock : stocks) {

            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.MONTHLY);

            if (stockPrice != null) {

                MArketConditionUtils.MarketCondition marketCondition =
                        MArketConditionUtils.detectMarketConditionFromOHLC(stockPrice);

                // System.out.println(stock.getNseSymbol() +" 1 : " + marketCondition);

                marketCondition = MArketConditionUtils.detectMarketCondition(stockPrice);
                // System.out.println(stock.getNseSymbol() +" 2 : " + marketCondition);

                marketCondition = MArketConditionUtils.detectCombinedMarketCondition(stockPrice);

                System.out.println(stock.getNseSymbol() + " 3 : " + marketCondition);
            }
        }
    }

    private void testSupportResistanceZones() {

        List<Stock> stocks = new ArrayList<>();
        Stock stockToAdd = stockService.getStockByNseSymbol("SAPPHIRE");
        stocks.add(stockToAdd);
        stockToAdd = stockService.getStockByNseSymbol("SERVOTECH");
        stocks.add(stockToAdd);
        stockToAdd = stockService.getStockByNseSymbol("LTIM");
        stocks.add(stockToAdd);

        for (Stock stock : stocks) {

            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.MONTHLY);
            if (stockPrice != null) {
                SupportResistanceZones supportResistanceZones =
                        SupportResistanceZoneUtils.calculateSupportResistanceZones(stockPrice);
                System.out.println("Support : " + supportResistanceZones.getSupport());
                System.out.println("Resistance : " + supportResistanceZones.getResistance());
            }
        }
    }

    private void updatePivotLevels() {

        List<Stock> stocks = stockService.getActiveStocks();

        for (Stock stock : stocks) {

            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.YEARLY);
            if (stockPrice != null) {
                PivotPointUtils.PivotLevels pivotLevels =
                        PivotPointUtils.calculate(
                                stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose());

                stockPrice.setPivot(pivotLevels.getPivot());

                stockPrice.setResistance1(pivotLevels.getResistance1());
                stockPrice.setResistance2(pivotLevels.getResistance2());
                stockPrice.setResistance3(pivotLevels.getResistance3());

                stockPrice.setSupport1(pivotLevels.getSupport1());
                stockPrice.setSupport2(pivotLevels.getSupport2());
                stockPrice.setSupport3(pivotLevels.getSupport3());

                stockPriceRepository.save(stockPrice);
            }
            System.out.println("Updated : " + stock.getNseSymbol());
        }
    }

    private void testResearch360() {
        Stock stock = stockService.getStockByNseSymbol("VBL");

        StockOverviewResponse stockOverviewResponse = null;
        try {
            stockOverviewResponse = research360Client.fetchStockOverview(stock.getIsinCode());
            System.out.println(
                    "Quality : "
                            + stockOverviewResponse.getData().getQualityColor()
                            + " : "
                            + stockOverviewResponse.getData().getQualityValue());
            System.out.println(
                    "Valuation : "
                            + stockOverviewResponse.getData().getValuationColor()
                            + " : "
                            + stockOverviewResponse.getData().getValuationValue());
            System.out.println(
                    "Technical : "
                            + stockOverviewResponse.getData().getTechnicalColor()
                            + " : "
                            + stockOverviewResponse.getData().getTechnicalValue());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateScore() {
        List<ResearchTechnical> researchTechnicalList =
                researchTechnicalService.getAll(Trade.Type.BUY);

        for (ResearchTechnical researchTechnical : researchTechnicalList) {
            if (researchTechnical.getResearchDate().isAfter(LocalDate.of(2025, 07, 28))) {
                researchTechnicalService.updateScore(researchTechnical);
            }
        }
    }

    private void testScore() {
        /*
        List<ResearchTechnical> researchTechnicalList = researchTechnicalService.getAll(Trade.Type.BUY);

        for(ResearchTechnical researchTechnical : researchTechnicalList){

        	double score = researchLedgerTechnicalService.calculateScore(researchTechnical);

        	System.out.println(" SYMBOL " + researchTechnical.getStock().getNseSymbol() + " SCORE " + score);
        }
         */
    }

    private void testDynamicSR() {

        // List<Stock> stockList = stockService.getActiveStocks();

        List<Stock> stockList = new ArrayList<>();
        stockList.add(stockService.getStockByNseSymbol("AETHER"));
        stockList.add(stockService.getStockByNseSymbol("GMDCLTD"));
        stockList.add(stockService.getStockByNseSymbol("HGINFRA"));
        stockList.add(stockService.getStockByNseSymbol("CELLO"));
        stockList.add(stockService.getStockByNseSymbol("OFSS"));
        stockList.add(stockService.getStockByNseSymbol("GODREJPROP"));
        stockList.add(stockService.getStockByNseSymbol("BBL"));
        stockList.add(stockService.getStockByNseSymbol("GRINDWELL"));
        stockList.add(stockService.getStockByNseSymbol("SWANENERGY"));
        stockList.add(stockService.getStockByNseSymbol("DOLLAR"));
        stockList.add(stockService.getStockByNseSymbol("RAMKY"));
        stockList.add(stockService.getStockByNseSymbol("NDRAUTO"));
        stockList.add(stockService.getStockByNseSymbol("ADANIGREEN"));
        stockList.add(stockService.getStockByNseSymbol("ALEMBICLTD"));
        stockList.add(stockService.getStockByNseSymbol("ALEMBICLTD"));

        // stockList.add(stockService.getStockByNseSymbol("KRBL"));
        // stockList.add(stockService.getStockByNseSymbol("KITEX"));
        // stockList.add(stockService.getStockByNseSymbol("ANANTRAJ"));
        // stockList.add(stockService.getStockByNseSymbol("JMFINANCIL"));
        // stockList.add(stockService.getStockByNseSymbol("MANINFRA"));
        // stockList.add(stockService.getStockByNseSymbol("INDIAGLYCO"));

        for (Stock stock : stockList) {
            System.out.println("Evaluation...." + stock.getNseSymbol());
            StockPrice stockPrice1 = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals stockTechnicals1 = stockTechnicalsService.get(stock, Timeframe.DAILY);

            StockPrice stockPrice = stockPriceService.buildPrevSessionStockPrice(stockPrice1);
            StockTechnicals stockTechnicals =
                    stockTechnicalsService.buildPrevSessionStockTechnicals(stockTechnicals1);

            List<MAEvaluationResult> maEvaluationResults =
                    dynamicMovingAverageSupportResolverService.evaluateInteractions(
                            Timeframe.DAILY, stockPrice, stockTechnicals, true);

            maEvaluationResults.forEach(
                    mae -> {
                        System.out.println(
                                stock.getNseSymbol() + " : " + stockPrice.getClose() + " : " + mae);
                    });

            Optional<MAEvaluationResult> evaluationResultOptional =
                    dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                            Timeframe.DAILY, stockPrice, stockTechnicals, true);

            if (evaluationResultOptional.isPresent()) {
                MAEvaluationResult evaluationResult = evaluationResultOptional.get();

                if (evaluationResult.isNearSupport()) {
                    System.out.println(
                            "SUPPORT : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);
                } else if (evaluationResult.isBreakout()) {
                    System.out.println(
                            "MA" + evaluationResult.getLength().getMaDays() + "_BREAKOUT");
                    System.out.println(
                            "BREAKOUT : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);

                    System.out.println("**********");
                    MovingAverageResult movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.HIGHEST,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    true);
                    System.out.println(
                            "HIGHEST : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.HIGH,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    true);
                    System.out.println(
                            "HIGH : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.MEDIUM,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    true);
                    System.out.println(
                            "MEDIUM : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.LOW,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    true);
                    System.out.println(
                            "LOW : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.LOWEST,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    true);
                    System.out.println(
                            "LOWEST : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    System.out.println("**********");
                } else if (evaluationResult.isNearResistance()) {
                    System.out.println(
                            "RESISTANCE : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);
                } else if (evaluationResult.isBreakdown()) {
                    System.out.println(
                            "BREAKDOWN : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);
                }
            }
        }
    }

    private void tesSimmpleSR() {

        List<Stock> stockList = stockService.getActiveStocks();

        /*
        List<Stock> stockList = new ArrayList<>();
        stockList.add(stockService.getStockByNseSymbol("MMFL"));
        stockList.add(stockService.getStockByNseSymbol("DCMSRIND"));
        stockList.add(stockService.getStockByNseSymbol("NBIFIN"));
        stockList.add(stockService.getStockByNseSymbol("WEIZMANIND"));
        */

        for (Stock stock : stockList) {
            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, Timeframe.DAILY);

            List<MAEvaluationResult> maEvaluationResults =
                    dynamicMovingAverageSupportResolverService.evaluateInteractions(
                            Timeframe.DAILY, stockPrice, stockTechnicals, false);

            maEvaluationResults.forEach(
                    mae -> {
                        System.out.println(
                                stock.getNseSymbol() + " : " + stockPrice.getClose() + " : " + mae);
                    });

            Optional<MAEvaluationResult> evaluationResultOptional =
                    dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                            Timeframe.DAILY, stockPrice, stockTechnicals, false);

            if (evaluationResultOptional.isPresent()) {
                MAEvaluationResult evaluationResult = evaluationResultOptional.get();

                if (evaluationResult.isNearSupport()) {
                    System.out.println(
                            "SUPPORT : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);
                } else if (evaluationResult.isBreakout()) {
                    System.out.println(
                            "BREAKOUT : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);

                    System.out.println("**********");
                    MovingAverageResult movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.HIGHEST,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    false);
                    System.out.println(
                            "HIGHEST : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.HIGH,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    false);
                    System.out.println(
                            "HIGH : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.MEDIUM,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    false);
                    System.out.println(
                            "MEDIUM : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.LOW,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    false);
                    System.out.println(
                            "LOW : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    movingAverageResult =
                            MovingAverageUtil.getMovingAverage(
                                    MovingAverageLength.LOWEST,
                                    Timeframe.DAILY,
                                    stockTechnicals,
                                    false);
                    System.out.println(
                            "LOWEST : "
                                    + movingAverageResult.getValue()
                                    + " : "
                                    + movingAverageResult.getPrevValue());
                    System.out.println("**********");
                } else if (evaluationResult.isNearResistance()) {
                    System.out.println(
                            "RESISTANCE : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);
                } else if (evaluationResult.isBreakdown()) {
                    System.out.println(
                            "BREAKDOWN : "
                                    + stock.getNseSymbol()
                                    + " : "
                                    + stockPrice.getClose()
                                    + " : "
                                    + evaluationResult);
                }
            }
        }
    }

    private void testmcap() {

        List<Stock> stockList = stockService.getActiveStocks();
        for (Stock stock : stockList) {
            double mcap = fundamentalResearchService.marketCap(stock);
            System.out.println(stock.getNseSymbol() + " : " + mcap);
        }
    }

    private void showBilling() {
        List<User> users = userService.getAllDhanApiEnabledUsers();
        for (User user : users) {
            System.out.println("Billing for " + user.getUsername());
            billingService.calculateAndRecordCharges(user);
            // BigDecimal bill = billingService.calculateAndRecordChargesForMonth(user,
            // YearMonth.now());
            BillingHistory billingHistory =
                    billingService.getBillsForMonth(user, YearMonth.now().minusMonths(1));
            System.out.println("\t\tBill No.: " + billingHistory.getBillNo());
            System.out.println("\t\tBill Month: " + billingHistory.getBillMonth());
            System.out.println("\t\tBill Date: " + billingHistory.getBillDate());
            System.out.println("\t\tAmount: " + billingHistory.getAmount());
            System.out.println("\t\tGST: " + billingHistory.getGst());
            System.out.println("\t\tTotal: " + billingHistory.getTotal());
        }
    }

    private void makePayment() {
        User user = userService.getUserByUsername("phsdhan");
        billingService.makePayment(
                user, "ES-20250801-0001", "ICICI Bank", "ICICI01", LocalDate.now());
        user = userService.getUserByUsername("ritudhan");
        billingService.makePayment(user, "ES-20250801-0002", "UPI", "PAYTM", LocalDate.now());
    }

    private void processResearchOnly() {
        List<Stock> stocks = stockService.getActiveStocks();
        for (Stock stock : stocks) {
            if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
                researchExecutorService.executeTechnical(
                        Timeframe.MONTHLY, stock, miscUtil.currentDate());
                try {
                    ThreadsUtil.delay(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
                researchExecutorService.executeTechnical(
                        Timeframe.WEEKLY, stock, miscUtil.currentDate());
                try {
                    ThreadsUtil.delay(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                ThreadsUtil.delay(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            researchExecutorService.executeTechnical(
                    Timeframe.DAILY, stock, miscUtil.currentDate());
        }
    }

    private List<StockAnalysis> newAlgoTest(Timeframe timeframe) {

        // Timeframe timeframe = Timeframe.WEEKLY;
        List<Stock> stocks = stockService.getActiveStocks();

        List<Stock> result = new ArrayList<>();

        for (Stock stock : stocks) {

            StockPrice stockPrice = stockPriceService.get(stock, timeframe);

            StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

            if (!this.isInititalValidated(stockPrice, stockTechnicals)) {
                continue;
            }

            Optional<MAEvaluationResult> evaluationResultOptional =
                    dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                            timeframe, stockPrice, stockTechnicals, false);

            if (evaluationResultOptional.isPresent()
                    && evaluationResultOptional.get().isBreakout()) {
                if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume()
                        && stockTechnicals.getVolumeAvg20()
                                > stockTechnicals.getPrevVolumeAvg20()) {
                    if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20()) {
                        if (MovingAverageUtil.increasingMaCount(stockTechnicals) >= 5) {
                            if (this.isLast2SessionRedAndCloseBelowEma5(stockPrice, stockTechnicals)
                                    || this.isLast2SessionLHLL(stockPrice)) {
                                result.add(stock);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Here is result");

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        for (Stock stock : result) {

            StockPrice stockPrice = stockPriceService.get(stock, timeframe);

            StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);

            StockTechnicals stockTechnicalsDaily =
                    stockTechnicalsService.get(stock, Timeframe.DAILY);

            double entryPrice =
                    this.calculateEntryPrice(
                            stockPrice.getOpen(), stockPrice.getClose(), timeframe);

            double target = this.calculateTarget(entryPrice, timeframe);

            double per =
                    formulaService.calculateChangePercentage(
                            entryPrice, stockPriceDaily.getClose());

            LocalDate lastSessionOfCurrentMonth =
                    calendarService.previousTradingSession(miscUtil.nextMonthFirstDay());

            LocalDate lastOfPrevMonth =
                    calendarService.previousTradingSession(miscUtil.currentMonthFirstDay());

            LocalDate lastSessionOfCurrentWeek =
                    calendarService.previousTradingSession(miscUtil.nextWeekFirstDay());

            LocalDate lastOfPrevWeek =
                    calendarService.previousTradingSession(miscUtil.currentWeekFirstDay());

            LocalDate ohlcvFromTo = lastOfPrevMonth;

            if (timeframe == Timeframe.WEEKLY) {
                ohlcvFromTo = lastOfPrevWeek;
            }

            if (miscUtil.currentDate().isEqual(lastSessionOfCurrentMonth)
                    && timeframe == Timeframe.MONTHLY) {
                ohlcvFromTo = lastSessionOfCurrentMonth;
            }

            if (miscUtil.currentDate().isEqual(lastSessionOfCurrentWeek)
                    && timeframe == Timeframe.WEEKLY) {
                ohlcvFromTo = lastSessionOfCurrentWeek;
            }

            OHLCV ohlcv =
                    monthlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), ohlcvFromTo, ohlcvFromTo);

            StockPrice stockPriceLastSession = new StockPriceDaily();
            stockPriceLastSession.setOpen(ohlcv.getOpen());
            stockPriceLastSession.setHigh(ohlcv.getHigh());
            stockPriceLastSession.setLow(ohlcv.getLow());
            stockPriceLastSession.setClose(ohlcv.getClose());
            ohlcv =
                    monthlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(),
                            calendarService.previousTradingSession(ohlcvFromTo),
                            calendarService.previousTradingSession(ohlcvFromTo));
            stockPriceLastSession.setPrevOpen(ohlcv.getOpen());
            stockPriceLastSession.setPrevHigh(ohlcv.getHigh());
            stockPriceLastSession.setPrevLow(ohlcv.getLow());
            stockPriceLastSession.setPrevClose(ohlcv.getClose());
            // last session candle should be green
            if (CandleStickUtils.isGreen(stockPriceLastSession)) {

                if (CandleStickUtils.isPrevSessionRed(stockPriceLastSession)
                        || CandleStickUtils.isPrevLowerWickDominant(stockPriceLastSession)
                        || (CandleStickUtils.prevSessionBodySize(stockPriceLastSession)
                                        < CandleStickUtils.bodySize(stockPriceLastSession)
                                && !CandleStickUtils.isPrevUpperWickDominant(
                                        stockPriceLastSession))) {

                    double stopLoss = ohlcv.getLow();

                    double dynamicStopLoss =
                            this.calculateDynamicStopLoss(
                                    per,
                                    stopLoss,
                                    stockPriceDaily,
                                    stockTechnicalsDaily,
                                    timeframe);

                    boolean isExitCandidate =
                            this.isExitCandidate(
                                    dynamicStopLoss, stockPriceDaily, stockTechnicalsDaily);

                    double risk = ((entryPrice - stopLoss) / entryPrice) * 100.0;

                    if (risk < (timeframe == Timeframe.MONTHLY ? 5.0 : 3.0)) {
                        stockAnalysed.add(
                                new StockAnalysis(
                                        ResearchTechnical.Strategy.BASIC,
                                        stock,
                                        MarketCapCategory.classify(
                                                fundamentalResearchService.marketCap(stock)),
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        stockPrice.getClose(),
                                        stockPriceDaily.getClose(),
                                        entryPrice,
                                        stopLoss,
                                        dynamicStopLoss,
                                        risk,
                                        target,
                                        per,
                                        isExitCandidate));
                    }
                }
            }
            // System.out.println("Found: " + stock.getNseSymbol() +" entry: "+entryPrice +"
            // stopLoss: "+ stopLoss +" risk: "+ risk +" target is: "+target + " %change: " +per);
        }
        // System.out.println(timeframe.name()  + " stockAnalysed: ");
        // this.sortAndPrint(stockAnalysed);
        return stockAnalysed;
    }

    private List<StockAnalysis> newAlgo2(Timeframe timeframe) {

        // Timeframe timeframe = Timeframe.MONTHLY;
        List<Stock> stocks = stockService.getActiveStocks();
        List<Stock> result = new ArrayList<>();

        for (Stock stock : stocks) {

            StockPrice stockPrice = stockPriceService.get(stock, timeframe);

            StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

            if (!this.isInititalValidated(stockPrice, stockTechnicals)) {
                continue;
            }

            Optional<MAEvaluationResult> evaluationResultOptional =
                    dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                            timeframe, stockPrice, stockTechnicals, false);
            // BreakOut EMA5 || EMA20
            if (evaluationResultOptional.isPresent()
                    && evaluationResultOptional.get().isBreakout()) {

                LocalDate currentMonthFirstSession =
                        calendarService.nextTradingSession(miscUtil.previousMonthLastDay());
                LocalDate currentMonthSecondSession =
                        calendarService.nextTradingSession(currentMonthFirstSession);

                LocalDate ohlcvFromTo = currentMonthFirstSession;

                LocalDate currentWeekFirstSession =
                        calendarService.nextTradingSession(miscUtil.previousWeekLastDay());
                LocalDate currentWeekSecondSession =
                        calendarService.nextTradingSession(currentWeekFirstSession);

                if (timeframe == Timeframe.WEEKLY) {
                    ohlcvFromTo = currentWeekFirstSession;
                }

                OHLCV ohlcv =
                        monthlySupportResistanceService.supportAndResistance(
                                stock.getNseSymbol(), ohlcvFromTo, ohlcvFromTo);

                if (CandleStickUtils.isGreen(stockPrice)
                        && CandleStickUtils.isPrevSessionRed(stockPrice)) {
                    if (!CandleStickUtils.isUpperWickDominant(stockPrice)) {
                        if (ohlcv.getOpen() < stockPrice.getClose()
                                || ohlcv.getLow() < stockPrice.getClose()) {
                            if (MovingAverageUtil.increasingMaCount(stockTechnicals) >= 5) {
                                if (stockTechnicals.getVolumeAvg20()
                                        > stockTechnicals.getPrevVolumeAvg20()) {

                                    LocalDate currentMonthThirdSession =
                                            calendarService.nextTradingSession(
                                                    currentMonthSecondSession);
                                    LocalDate currentWeekThirdSession =
                                            calendarService.nextTradingSession(
                                                    currentWeekSecondSession);

                                    LocalDate sessionDate = currentMonthFirstSession;
                                    LocalDate sessionDateTill = currentMonthThirdSession;

                                    if (timeframe == Timeframe.WEEKLY) {
                                        sessionDate = currentWeekFirstSession;
                                        sessionDateTill = currentWeekThirdSession;
                                    }

                                    while (sessionDate.isBefore(sessionDateTill)) {

                                        OHLCV ohlcvDaily =
                                                monthlySupportResistanceService
                                                        .supportAndResistance(
                                                                stock.getNseSymbol(),
                                                                sessionDate,
                                                                sessionDate);

                                        StockPrice stockPriceDaily = new StockPriceDaily();
                                        stockPriceDaily.setOpen(ohlcvDaily.getOpen());
                                        stockPriceDaily.setHigh(ohlcvDaily.getHigh());
                                        stockPriceDaily.setLow(ohlcvDaily.getLow());
                                        stockPriceDaily.setClose(ohlcvDaily.getClose());

                                        ohlcvDaily =
                                                monthlySupportResistanceService
                                                        .supportAndResistance(
                                                                stock.getNseSymbol(),
                                                                calendarService
                                                                        .previousTradingSession(
                                                                                sessionDate),
                                                                calendarService
                                                                        .previousTradingSession(
                                                                                sessionDate));

                                        stockPriceDaily.setPrevOpen(ohlcvDaily.getOpen());
                                        stockPriceDaily.setPrevHigh(ohlcvDaily.getHigh());
                                        stockPriceDaily.setPrevLow(ohlcvDaily.getLow());
                                        stockPriceDaily.setPrevClose(ohlcvDaily.getClose());

                                        // Daily Close above Monthly Close
                                        if (stockPriceDaily.getClose() > stockPrice.getClose()) {
                                            StockTechnicals stockTechnicalsDaily =
                                                    new StockTechnicalsDaily();

                                            if (stock.getNseSymbol()
                                                    .equalsIgnoreCase("BHARATGEAR")) {
                                                stockTechnicalsDaily.setVolume(169l);
                                                stockTechnicalsDaily.setPrevVolume(207l);
                                                stockTechnicalsDaily.setVolumeAvg20(171l);
                                                stockTechnicalsDaily.setPrevVolumeAvg20(170l);
                                            }

                                            if (stock.getNseSymbol().equalsIgnoreCase("MTARTECH")) {
                                                stockTechnicalsDaily.setVolume(205l);
                                                stockTechnicalsDaily.setPrevVolume(116l);
                                                stockTechnicalsDaily.setVolumeAvg20(475l);
                                                stockTechnicalsDaily.setPrevVolumeAvg20(468l);
                                            }

                                            if (stock.getNseSymbol().equalsIgnoreCase("PRIVISCL")) {
                                                stockTechnicalsDaily.setVolume(65l);
                                                stockTechnicalsDaily.setPrevVolume(18l);
                                                stockTechnicalsDaily.setVolumeAvg20(536l);
                                                stockTechnicalsDaily.setPrevVolumeAvg20(531l);
                                            }

                                            if (CandleStickUtils.isGreen(stockPriceDaily)) {

                                                if (CandleStickUtils.isPrevSessionRed(
                                                                stockPriceDaily)
                                                        || CandleStickUtils.isPrevLowerWickDominant(
                                                                stockPriceDaily)
                                                        || (CandleStickUtils.prevSessionBodySize(
                                                                                stockPriceDaily)
                                                                        < CandleStickUtils.bodySize(
                                                                                stockPriceDaily)
                                                                && !CandleStickUtils
                                                                        .isPrevUpperWickDominant(
                                                                                stockPriceDaily))) {
                                                    if (!CandleStickUtils.isUpperWickDominant(
                                                            stockPriceDaily)) {
                                                        if (stockTechnicalsDaily.getVolumeAvg20()
                                                                > stockTechnicalsDaily
                                                                        .getPrevVolumeAvg20()) {
                                                            if (stockTechnicalsDaily.getVolume()
                                                                    > stockTechnicalsDaily
                                                                            .getPrevVolume()) {

                                                                result.add(stock);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        sessionDate =
                                                calendarService.nextTradingSession(sessionDate);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Here is result");
        List<StockAnalysis> stockAnalysed = new ArrayList<>();
        for (Stock stock : result) {
            StockPrice stockPrice = stockPriceService.get(stock, timeframe);
            StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals stockTechnicalsDaily =
                    stockTechnicalsService.get(stock, Timeframe.DAILY);

            LocalDate currentMonthFirstSession =
                    calendarService.previousTradingSession(miscUtil.previousMonthLastDay());

            LocalDate ohlcvFromTo = calendarService.nextTradingSession(currentMonthFirstSession);

            OHLCV ohlcv =
                    monthlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), ohlcvFromTo, ohlcvFromTo);

            double entryPrice =
                    this.calculateEntryPrice(ohlcv.getOpen(), ohlcv.getClose(), Timeframe.MONTHLY);

            double target = this.calculateTarget(entryPrice, Timeframe.MONTHLY);

            double per =
                    formulaService.calculateChangePercentage(
                            entryPrice, stockPriceDaily.getClose());

            double stopLoss = ohlcv.getLow();

            double dynamicStopLoss =
                    this.calculateDynamicStopLoss(
                            per,
                            stopLoss,
                            stockPriceDaily,
                            stockTechnicalsDaily,
                            Timeframe.MONTHLY);

            boolean isExitCandidate =
                    this.isExitCandidate(dynamicStopLoss, stockPriceDaily, stockTechnicalsDaily);

            double risk = ((entryPrice - stopLoss) / entryPrice) * 100.0;

            if (risk < (timeframe == Timeframe.MONTHLY ? 5.0 : 3.0)) {
                stockAnalysed.add(
                        new StockAnalysis(
                                ResearchTechnical.Strategy.PRICE,
                                stock,
                                MarketCapCategory.classify(
                                        fundamentalResearchService.marketCap(stock)),
                                true,
                                true,
                                true,
                                true,
                                true,
                                stockPrice.getClose(),
                                stockPriceDaily.getClose(),
                                entryPrice,
                                stopLoss,
                                dynamicStopLoss,
                                risk,
                                target,
                                per,
                                isExitCandidate));
            }

            // System.out.println("Found: " + stock.getNseSymbol() +" entry: "+entryPrice +"
            // stopLoss: "+ stopLoss +" risk: "+ risk +" target is: "+target + " %change: " +per);
        }
        //  System.out.println(timeframe + " stockAnalysed: ");
        // this.sortAndPrint(stockAnalysed);
        return stockAnalysed;
    }

    private boolean isInititalValidated(StockPrice stockPrice, StockTechnicals stockTechnicals) {
        if (stockPrice == null || stockTechnicals == null) {
            return false;
        }

        Stock stock = stockPrice.getStock();

        boolean isEqOrBE = stock.getSeries().equalsIgnoreCase("EQ");

        if (!isEqOrBE) {
            return false;
        }

        if (stock.getSector() == null || stock.getSector().getType() == Sector.Type.ETF) {
            return false;
        }

        if (stockPrice.getTimeframe() != Timeframe.MONTHLY) {
            if (!volumeIndicatorService.isTradingValueSufficient(
                    stockPrice.getTimeframe(), stockPrice, stockTechnicals)) {
                return false;
            }
        }
        return true;
    }

    private boolean isLast2SessionRedAndCloseBelowEma5(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        // if( stockPrice.getTimeframe()==Timeframe.MONTHLY ||
        // CandleStickUtils.isPrev3SessionRed(stockPrice) && stockPrice.getPrev3Close() <
        // stockTechnicals.getPrev3Ema5() ){
        if (CandleStickUtils.isPrev2SessionRed(stockPrice)
                && stockPrice.getPrev2Close() < stockTechnicals.getPrev2Ema5()) {
            if (CandleStickUtils.isPrevSessionRed(stockPrice)
                    && stockPrice.getPrevClose() < stockTechnicals.getPrevEma5()) {
                return true;
            }
        }
        // }

        return false;
    }

    private boolean isLast2SessionLHLL(StockPrice stockPrice) {
        // if(stockPrice.getTimeframe()==Timeframe.MONTHLY ||  stockPrice.getPrev3High() <
        // stockPrice.getPrev4High() && stockPrice.getPrev3Low() < stockPrice.getPrev4Low()) {
        if (stockPrice.getPrev2High() < stockPrice.getPrev3High()) {
            if (stockPrice.getPrevHigh() < stockPrice.getPrev2High()
                    && stockPrice.getPrevLow() < stockPrice.getPrev2Low()) {
                return true;
            }
        }
        // }

        return false;
    }

    private List<StockAnalysis> findMonthlySimpleBreakout() {

        List<String> symbolsOct25 = new ArrayList<>();
        symbolsOct25.add("ICICIBANK");
        symbolsOct25.add("MBAPL");
        symbolsOct25.add("LORDSCHLO");
        symbolsOct25.add("WINDMACHIN");
        symbolsOct25.add("DIVISLAB");
        symbolsOct25.add("ERIS");
        symbolsOct25.add("GRMOVER");
        symbolsOct25.add("NUVAMA");

        List<Stock> stocks = stockService.getActiveStocks();

        List<Stock> result = new ArrayList<>();
        for (String symbol : symbolsOct25) {
            Stock stock = stockService.getStockByNseSymbol(symbol);
            result.add(stock);
        }

        int counter = 0;
        for (Stock stock : stocks) {

            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, Timeframe.DAILY);
            TradeSetup tradeSetup =
                    simplePriceActionSignalEvaluator.evaluateEntry(
                            stockPrice.getTimeframe(), stock, stockPrice, stockTechnicals);

            if (stockPrice != null && stockTechnicals != null) {

                if (tradeSetup.isActive()) {
                    ++counter;
                    if (tradeSetup.getSubStrategy()
                            == ResearchTechnical.SubStrategy.MONTHLY_BREAKOUT) {

                        result.add(stock);
                    }
                }
            } else {
                System.out.println("data not found " + stock.getNseSymbol());
            }
        }

        System.out.println("monthly counter " + counter);

        List<StockAnalysis> analyzed = this.filterAnalyzed(result, Timeframe.MONTHLY);

        // this.sortAndPrint(analyzed);

        return analyzed;
    }

    private List<StockAnalysis> findWeeklySimpleBreakout() {

        List<String> symbolsOct25 = new ArrayList<>();
        symbolsOct25.add("MINDACORP");

        List<Stock> stocks = stockService.getActiveStocks();

        List<Stock> result = new ArrayList<>();
        for (String symbol : symbolsOct25) {
            Stock stock = stockService.getStockByNseSymbol(symbol);
            result.add(stock);
        }

        int counter = 0;
        for (Stock stock : stocks) {

            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, Timeframe.DAILY);
            TradeSetup tradeSetup =
                    simplePriceActionSignalEvaluator.evaluateEntry(
                            stockPrice.getTimeframe(), stock, stockPrice, stockTechnicals);

            if (stockPrice != null && stockTechnicals != null) {

                if (tradeSetup.isActive()) {
                    ++counter;
                    if (tradeSetup.getSubStrategy()
                            == ResearchTechnical.SubStrategy.WEEKLY_BREAKOUT) {
                        result.add(stock);
                    }
                }
            } else {
                System.out.println("data not found " + stock.getNseSymbol());
            }
        }

        System.out.println("weekly counter " + counter);

        List<StockAnalysis> analyzed = this.filterAnalyzed(result, Timeframe.WEEKLY);

        // this.sortAndPrint(analyzed);

        return analyzed;
    }

    private List<StockAnalysis> filterAnalyzed(List<Stock> stocks, Timeframe timeframeHt) {
        List<StockAnalysis> analyzed = new ArrayList<>();
        for (Stock stock : stocks) {
            StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals stockTechnicalsDaily =
                    stockTechnicalsService.get(stock, Timeframe.DAILY);

            StockPrice stockPriceHt = stockPriceService.get(stock, timeframeHt);
            StockTechnicals stockTechnicalsHt = stockTechnicalsService.get(stock, timeframeHt);

            if (!this.isInititalValidated(stockPriceHt, stockTechnicalsHt)) {
                continue;
            }
            double htEma5 =
                    MovingAverageUtil.getMovingAverage5(
                            stockTechnicalsHt.getTimeframe(), stockTechnicalsHt);

            boolean isHtHigherHighAndHigherLow =
                    CandleStickUtils.isHigherHigh(stockPriceHt)
                            && CandleStickUtils.isHigherLow(stockPriceHt);

            boolean isHtLongUpperWick =
                    CandleStickUtils.upperWickSize(stockPriceHt)
                            > CandleStickUtils.lowerWickSize(stockPriceHt);

            if (CandleStickUtils.isPrevSessionGreen(stockPriceHt)
                    && (!isHtHigherHighAndHigherLow
                            || stockPriceHt.getClose() < htEma5
                            || isHtLongUpperWick)) {
                continue;
            }

            double htEma20 =
                    MovingAverageUtil.getMovingAverage20(
                            stockTechnicalsHt.getTimeframe(), stockTechnicalsHt);

            if (stockPriceHt.getClose() < htEma20) {
                continue;
            }

            boolean isHt4Incr = MovingAverageUtil.increasingMaCount(stockTechnicalsHt) >= 4;
            boolean isHtAvgIncr =
                    stockTechnicalsHt.getVolumeAvg20() > stockTechnicalsHt.getPrevVolumeAvg20();
            boolean isHtVolIncr =
                    stockTechnicalsHt.getVolume() > stockTechnicalsHt.getPrevVolume()
                            || stockTechnicalsHt.getPrevVolume()
                                    > stockTechnicalsHt.getVolume() * 1.5;

            boolean isHtLowRejected =
                    (stockPriceHt.getClose() > htEma5 && stockPriceHt.getLow() <= htEma5
                                    || stockPriceHt.getClose() > htEma20
                                            && stockPriceHt.getLow() <= htEma20)
                            && CandleStickUtils.lowerWickSize(stockPriceHt)
                                    > CandleStickUtils.upperWickSize(stockPriceHt);

            boolean isHtLongLowerWick =
                    CandleStickUtils.lowerWickSize(stockPriceHt)
                            > CandleStickUtils.upperWickSize(stockPriceHt);

            LocalDate firstOfMonth =
                    calendarService.nextTradingSession(miscUtil.previousMonthLastDay());

            LocalDate firstOfWeek =
                    calendarService.nextTradingSession(miscUtil.previousWeekLastDay());

            LocalDate ohlcvFromTo = firstOfMonth;

            if (timeframeHt == Timeframe.WEEKLY) {
                ohlcvFromTo = firstOfWeek;
            }

            OHLCV ohlcv =
                    monthlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), ohlcvFromTo, ohlcvFromTo);

            double entryPrice =
                    this.calculateEntryPrice(ohlcv.getOpen(), ohlcv.getClose(), timeframeHt);

            double stopLoss = ohlcv.getLow();

            double per =
                    formulaService.calculateChangePercentage(
                            entryPrice, stockPriceDaily.getClose());

            double dynamicStopLoss =
                    this.calculateDynamicStopLoss(
                            per, stopLoss, stockPriceDaily, stockTechnicalsDaily, timeframeHt);

            boolean isExitCandidate =
                    this.isExitCandidate(dynamicStopLoss, stockPriceDaily, stockTechnicalsDaily);

            double risk = ((entryPrice - stopLoss) / entryPrice) * 100.0;

            double target = this.calculateTarget(entryPrice, timeframeHt);

            if (risk < (timeframeHt == Timeframe.MONTHLY ? 5.0 : 3.0)) {
                analyzed.add(
                        new StockAnalysis(
                                ResearchTechnical.Strategy.SIMPLE,
                                stock,
                                MarketCapCategory.classify(
                                        fundamentalResearchService.marketCap(stock)),
                                isHt4Incr,
                                isHtAvgIncr,
                                isHtVolIncr,
                                isHtLowRejected,
                                isHtLongLowerWick,
                                ohlcv.getClose(),
                                stockPriceDaily.getClose(),
                                entryPrice,
                                stopLoss,
                                dynamicStopLoss,
                                risk,
                                target,
                                per,
                                isExitCandidate));
            }
        }
        return analyzed;
    }

    private double calculateTarget(double entryPrice, Timeframe timeframe) {
        return formulaService.applyPercentChange(
                entryPrice, timeframe == Timeframe.MONTHLY ? 24.0 : 6.0);
    }

    private double calculateEntryPrice(double open, double close, Timeframe timeframe) {
        double entryPrice = (open + close) / 2;

        entryPrice =
                formulaService.applyPercentChange(
                        entryPrice, timeframe == Timeframe.MONTHLY ? 1.25 : 0.625);

        double closePRic =
                formulaService.applyPercentChange(
                        close, timeframe == Timeframe.MONTHLY ? 0.50 : 0.25);

        entryPrice = Math.max(entryPrice, closePRic);

        return entryPrice;
    }

    private double calculateDynamicStopLoss(
            double currentChg,
            double initialStopLoss,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe) {

        double dynamicStopLoss = initialStopLoss;

        double minTarget = timeframe == Timeframe.WEEKLY ? 2.0 : 8.0;

        if (currentChg > minTarget) {
            dynamicStopLoss =
                    Math.max(
                            MovingAverageUtil.getMovingAverage5(
                                    stockTechnicals.getTimeframe(), stockTechnicals),
                            stockPrice.getLow());
        }
        return dynamicStopLoss;
    }

    private boolean isExitCandidate(
            double dynamicStopLoss, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean isExitCandidate = false;

        if (!(CandleStickUtils.isLowerWickDominant(stockPrice))) {
            isExitCandidate = stockPrice.getClose() < dynamicStopLoss;
        }

        if (!isExitCandidate) {
            if (stockPrice.getClose() < stockTechnicals.getEma5()
                    && CandleStickUtils.isRed(stockPrice)
                    && CandleStickUtils.isPrevSessionRed(stockPrice)) {
                if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume()
                        && stockTechnicals.getVolumeAvg20()
                                > stockTechnicals.getPrevVolumeAvg20()) {
                    isExitCandidate = true;
                }
            }
        }

        if (!isExitCandidate) {

            LocalDate lastSessionOfMonth =
                    calendarService.previousTradingSession(
                            miscUtil.currentMonthLastDay().plusDays(1));
            LocalDate secondLastSessionOfMonth =
                    calendarService.previousTradingSession(
                            miscUtil.currentMonthLastDay().plusDays(1));

            if (miscUtil.currentDate().isEqual(secondLastSessionOfMonth)
                    || miscUtil.currentDate().isEqual(lastSessionOfMonth)) {
                isExitCandidate = true;
            }
        }

        return isExitCandidate;
    }

    private void sortAndPrint(List<StockAnalysis> analyzed) {
        // Sort by criteria priority
        analyzed.sort(
                Comparator.comparing(StockAnalysis::getRisk)
                        .reversed()
                        .thenComparing(StockAnalysis::is4Incr)
                        .reversed()
                        .thenComparing(StockAnalysis::isLowRejected, Comparator.reverseOrder())
                        .thenComparing(
                                sa -> sa.isAvgIncr() && sa.isVolIncr(), Comparator.reverseOrder())
                        .thenComparing(sa -> sa.isLongLowerWick(), Comparator.reverseOrder())
                        .thenComparing(StockAnalysis::isAvgIncr, Comparator.reverseOrder()));

        //  Print result
        for (StockAnalysis sa : analyzed) {
            /*
            System.out.printf(
                    "Found: %-10s | 4Incr=%-5s | Avg++=%-5s | Vol++=%-5s | LowRej=%-5s | LongLowerWick=%-5s |Close=%.2f |Entry=%.2f |SL=%.2f |Risk=%.2f%% |Change=%.2f%% %n",
                    sa.getStock().getNseSymbol(),
                    sa.is4Incr(),
                    sa.isAvgIncr(),
                    sa.isVolIncr(),
                    sa.isLowRejected(),
                    sa.isLongLowerWick(),
                    sa.getClose(),
                    sa.getEntryPrice(),
                    sa.getStopLoss(),
                    sa.getRisk(),
                    sa.getChangePercent());*/
            System.out.printf(
                    "Found: %-10s |Strategy=%-10s |MArketCap=%-10s |Close=%.2f |CurrentClose=%.2f"
                            + " |Entry=%.2f |SL=%.2f |DynamicSL=%.2f |Risk=%.2f |Target=%.2f"
                            + " |Change=%.2f%% |isExitCandidate=%-5s%n",
                    sa.getStock().getNseSymbol(),
                    sa.getStrategy(),
                    sa.getCapSize(),
                    sa.getClose(),
                    sa.getCurrentClose(),
                    sa.getEntryPrice(),
                    sa.getStopLoss(),
                    sa.getDynamicStopLoss(),
                    sa.getRisk(),
                    sa.getTarget(),
                    sa.getChangePercent(),
                    sa.isExitCandidate());
        }
    }

    private void allocatePositions() {
        List<User> users = userService.getAllDhanApiEnabledUsers();

        LocalDate sessionDate = miscUtil.currentDate();

        if (LocalTime.now().isAfter(LocalTime.of(15, 30))) {
            sessionDate = miscUtil.currentDate().plusDays(1);
        }

        System.out.println("SessionDate " + sessionDate);
        LocalDate previousTradingSessionDate = calendarService.previousTradingSession(sessionDate);

        System.out.println("previousTradingSessionDate " + previousTradingSessionDate);

        List<ResearchTechnical> researchTechnicalForBuyOrders =
                researchTechnicalService.getLatestBuyResearch(previousTradingSessionDate);
        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getPreviousInvestmentResearches(
                        previousTradingSessionDate));
        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getPreviousCandleStickResearches(
                        previousTradingSessionDate));
        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getRecentHybridResearches(
                        previousTradingSessionDate));
        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getRecentDynamicResearches(
                        previousTradingSessionDate));
        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getRecentBasicResearches(
                        calendarService.previousTradingSession(sessionDate)));

        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getRecentSimpleResearches(
                        calendarService.previousTradingSession(sessionDate)));

        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getRecentFlexiResearches(
                        calendarService.previousTradingSession(sessionDate)));

        researchTechnicalForBuyOrders.addAll(
                dhanOrderSchedulerHelperService.getRecentPriceResearches(
                        calendarService.previousTradingSession(sessionDate)));

        researchTechnicalForBuyOrders.sort(
                DhanOrderSchedulerHelperService.byDateVolumeScoreDescComparator());

        researchTechnicalForBuyOrders.removeIf(
                rt -> rt.getRisk() > RiskUtil.maxRisk(rt.getTimeframe()));

        List<ResearchTechnical> reorderedResearchTechnicalForBuyOrders =
                DhanOrderSchedulerHelperService.distributeInvestmentsStable(
                        researchTechnicalForBuyOrders);

        System.out.println("Buy researches on " + previousTradingSessionDate);

        reorderedResearchTechnicalForBuyOrders.forEach(
                rt -> {
                    System.out.println(
                            rt.getStock().getNseSymbol()
                                    + " "
                                    + rt.getTimeframe()
                                    + " "
                                    + rt.getEntryStrategy()
                                    + " "
                                    + rt.getEntryPrice()
                                    + " "
                                    + rt.getResearchDate()
                                    + " "
                                    + rt.getRisk()
                                    + " "
                                    + rt.getVolumeScore()
                                    + " "
                                    + rt.getScore()
                                    + " "
                                    + rt.getTickSize());
                });

        List<ResearchTechnical> researchTechnicalsForSellOrder =
                researchTechnicalService.getLatestSellResearch(previousTradingSessionDate);

        researchTechnicalsForSellOrder.addAll(
                dhanOrderSchedulerHelperService.getNearTargetResearches(sessionDate));
        System.out.println("***************");
        System.out.println("Sell researches on " + previousTradingSessionDate);

        researchTechnicalsForSellOrder.forEach(
                rt -> {
                    System.out.println(
                            rt.getStock().getNseSymbol()
                                    + " "
                                    + rt.getExitStrategy()
                                    + " "
                                    + rt.getExitPrice()
                                    + " "
                                    + rt.getExitDate());
                });

        for (User user : users) {
            System.out.println("Allocating for " + user.getUsername());

            this.allocatePositions(
                    sessionDate,
                    user,
                    reorderedResearchTechnicalForBuyOrders,
                    researchTechnicalsForSellOrder);
        }
    }

    /** Position Size = (Total trading fund * Risk%)/SL% */
    private void allocatePositions(
            LocalDate currentDate,
            User user,
            List<ResearchTechnical> researchTechnicalForBuyOrders,
            List<ResearchTechnical> researchTechnicalsForSellOrder) {

        System.out.println("Buy orders for user " + user.getUsername());

        dhanOrderExecutorService.executeBuy(currentDate, user, researchTechnicalForBuyOrders, true);

        System.out.println("Sell orders for user " + user.getUsername());
        dhanOrderExecutorService.executeSell(user, researchTechnicalsForSellOrder, true);
    }

    private void testTrend() {

        List<Stock> stockList = stockService.getActiveStocks();
        /*
        List<Stock> stockList = new ArrayList<>();
        Stock stock = stockService.getStockByNseSymbol("AARTIPHARM");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("TRIVENI");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("TATASTEEL");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("LT");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("HINDALCO");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("SPLPETRO");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("FIEMIND");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("MPSLTD");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("GREENLAM");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("SBIN");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("JIOFIN");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("ONGC");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("ASIANPAINT");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("POWERGRID");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("RALLIS");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("HAVELLS");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("PHOENIXLTD");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("GLENMARK");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("BBOX");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("KEI");
        stockList.add(stock);
        stock = stockService.getStockByNseSymbol("SHRIRAMFIN");
        stockList.add(stock);*/
        stockList.forEach(
                stk -> {
                    Trend trend = trendService.detect(stk, Timeframe.DAILY);
                    Trend dynamicTrend = dynamicTrendService.detect(stk, Timeframe.DAILY);

                    boolean sameMomentum = trend.getMomentum() == dynamicTrend.getMomentum();

                    boolean isDown = dynamicTrend.getDirection() == Trend.Direction.DOWN;

                    if (!sameMomentum && isDown) {
                        System.out.println(
                                "STATIC -> "
                                        + stk.getNseSymbol()
                                        + " Direction: "
                                        + trend.getDirection()
                                        + " Momentum: "
                                        + trend.getMomentum());
                    }

                    if (!sameMomentum && isDown) {
                        System.out.println(
                                "DYNAMIC -> "
                                        + stk.getNseSymbol()
                                        + " Direction: "
                                        + dynamicTrend.getDirection()
                                        + " Momentum: "
                                        + dynamicTrend.getMomentum());
                    }
                });
    }

    private void testCandleStick() {
        System.out.println("******* Testing CandleSticks *******");
    }

    private void updateFinaicials() {
        List<Stock> stockList = stockRepository.findByActivityCompleted(false);

        List<String> results = new ArrayList<>();

        int countTotal = stockList.size();

        for (Stock stock : stockList) {
            try {

                long startTime = System.currentTimeMillis();
                System.out.println("Starting activity for " + stock.getNseSymbol());

                String xbrlUrl =
                        nseFinancialsFetcher.fetchXbrl(
                                stock.getNseSymbol(), LocalDate.of(2024, 12, 31));

                System.out.println(xbrlUrl);

                String xxbrlContent = nseXmlService.fetchXmlContent(xbrlUrl);

                // System.out.println(xxbrlContent);

                if (stock.getSector().getType() == Sector.Type.CORPORATE) {
                    StockFinancials stockFinancials =
                            StockFinancialsParser.parseFromXml(
                                    xxbrlContent, LocalDate.of(2024, 12, 31));
                    stockFinancials.setStock(stock);
                    System.out.println(stockFinancials);

                } else if (stock.getSector().getType() == Sector.Type.BANKING) {
                    BankingFinancials bankingFinancials =
                            BankingFinancialsParser.parseFromXml(xxbrlContent);
                    bankingFinancials.setStock(stock);
                    System.out.println(bankingFinancials);
                }

                stockRepository.save(stock);
                long endTime = System.currentTimeMillis();

                System.out.println(
                        "Completed activity for "
                                + stock.getNseSymbol()
                                + " took "
                                + (endTime - startTime)
                                + "ms");
                System.out.println("Remaining " + countTotal);

                miscUtil.delay();
            } catch (Exception e) {
                System.out.println("An Error occurred while updating price");
            }
        }
    }

    private void updateRemainigSectorsActivityFromNSE() {

        List<Stock> stockList = stockRepository.findByActivityCompleted(false);

        List<String> results = new ArrayList<>();

        int countTotal = stockList.size();

        for (Stock stock : stockList) {

            try {
                miscUtil.delay(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                long startTime = System.currentTimeMillis();
                System.out.println("Starting activity for " + stock.getNseSymbol());

                String industry = sectorScrappingService.getIndustry(stock.getNseSymbol());
                // String industry = null;
                if (!industry.equalsIgnoreCase("NSE")) {
                    log.info("Industry found: {}", industry);
                    Sector sector = sectorService.getSectorByName(industry);
                    if (sector != null) {
                        log.info("Sector found code: {}, name: {}", sector.getCode(), industry);
                        stock.setSector(sector);
                        stock.setSectorName(sector.getSectorName());
                        stock.setActivityCompleted(true);
                        stockRepository.save(stock);
                        --countTotal;
                    } else {
                        log.info("Sector Not found name: {}", industry);
                        results.add(stock.getNseSymbol());
                    }
                }

                long endTime = System.currentTimeMillis();

                System.out.println(
                        "Completed activity for "
                                + stock.getNseSymbol()
                                + " took "
                                + (endTime - startTime)
                                + "ms");
                System.out.println("Remaining " + countTotal);

                // miscUtil.delay(25);
            } catch (Exception e) {
                System.out.println("An Error occurred while updating price");
            }
        }
        System.out.println("Not in master.............");
        results.forEach(
                result -> {
                    System.out.println(result);
                });
    }

    private void updateSectorsActivity() {

        log.info("Starting update sector activity");

        List<SectorIO> sectorIOList = null;
        try {
            sectorIOList = this.loadSectors();

            for (SectorIO sectorIO : sectorIOList) {
                log.info(
                        "Starting updating code:{}, name:{}",
                        sectorIO.getCode(),
                        sectorIO.getSectorName());
                Sector sector = sectorService.getOrCreate(sectorIO);
                this.downloadAndUpdateSectorStocksList(sector);
                miscUtil.delay();
            }

        } catch (IOException | InterruptedException e) {
            log.error("An error occured", e);
        }

        log.info("Completed update sector activity");
    }

    @Transactional
    private void downloadAndUpdateSectorStocksList(Sector sector) {
        log.info("Updating stocks for code:{}, name:{}", sector.getCode(), sector.getSectorName());
        List<BseSectorListResponse> bseSectorListResponseList = null;

        try {
            bseSectorListResponseList =
                    sectorDownloadService.downloadAndProcessSectors(sector.getCode());

            if (bseSectorListResponseList != null && !bseSectorListResponseList.isEmpty()) {
                bseSectorListResponseList.forEach(
                        bseSectorResponse -> {
                            Stock stock =
                                    stockService.getStockByNseSymbol(
                                            bseSectorResponse.getSecurityName().trim());
                            if (stock != null) {
                                log.info(
                                        "Found stocks in master :{}",
                                        bseSectorResponse.getSecurityName().trim());
                                stock.setSector(sector);
                                stock.setSectorName(sector.getSectorName());
                                stock.setActivityCompleted(true);
                                stockRepository.save(stock);
                                log.info("Sector updated in master :{}", stock.getNseSymbol());
                            } else {
                                log.info(
                                        "Not Found stocks in master :{}",
                                        bseSectorResponse.getSecurityName().trim());
                            }
                        });
            }

        } catch (IOException e) {
            log.error("An error occured while updating sectors {}", sector.getCode(), e);
        }

        log.info("Updated stocks for code:{}, name:{}", sector.getCode(), sector.getSectorName());
    }

    public List<SectorIO> loadSectors() throws IOException {

        String CSV_FILE_PATH =
                System.getProperty("user.home") + "/mydrive/repo/tnp/sector_master.csv";

        List<SectorIO> sectors = new ArrayList<>();
        Path filePath = Paths.get(CSV_FILE_PATH);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("CSV file not found: " + CSV_FILE_PATH);
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { // Skip header
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 2) {
                    sectors.add(new SectorIO(values[0].trim(), values[1].trim()));
                }
            }
        }
        return sectors;
    }

    private void scanBearishCandleStickPattern() {

        System.out.println("******* Scanning Bullish *******");
        List<Stock> stockList = stockService.getActiveStocks();

        System.out.println("******* Scanning Bearish From Master *******");

        for (Stock stock : stockList) {
            if (stock.getSeries() != null && stock.getSeries().equalsIgnoreCase("EQ")) {
                if (fundamentalResearchService.isMcapInRange(stock)) {

                    if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
                        System.out.println("******* MONTHLY :" + stock.getNseSymbol() + " *******");
                    }
                    if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
                        System.out.println("******* WEEKLY :" + stock.getNseSymbol() + " *******");
                    }
                    System.out.println("******* DAILY :" + stock.getNseSymbol() + " *******");

                    // movingAverageActionService.breakDown(stock, Timeframe.DAILY);
                }
            }
        }
    }

    private void testLastTradingSession() {
        LocalDate tradingDate = LocalDate.of(2025, 03, 13);
        boolean special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

        System.out.println(tradingDate + " is " + special);

        tradingDate = LocalDate.of(2025, 01, 31);
        special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

        System.out.println(tradingDate + " is " + special);

        tradingDate = LocalDate.of(2025, 02, 28);
        special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

        System.out.println(tradingDate + " is " + special);

        tradingDate = LocalDate.of(2025, 03, 31);
        special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

        System.out.println(tradingDate + " is " + special);

        tradingDate = LocalDate.of(2025, 03, 28);
        special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

        System.out.println(tradingDate + " is " + special);

        tradingDate = LocalDate.of(2025, 06, 30);
        special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

        System.out.println(tradingDate + " is " + special);
    }

    private void updatePriceHistory() {

        List<Stock> stockList = stockRepository.findByActivityCompleted(false);

        int countTotal = stockList.size();

        for (Stock stock : stockList) {
            try {
                long startTime = System.currentTimeMillis();
                System.out.println("Starting activity for " + stock.getNseSymbol());

                System.out.println("Printing Daily Data");
                List<OHLCV> ohlcvListDaily =
                        ohlcvService.fetch(
                                stock.getNseSymbol(), LocalDate.of(2022, 01, 01), LocalDate.now());
                List<OHLCV> ohlcvList =
                        ohlcvListDaily.subList(
                                Math.max(ohlcvListDaily.size() - 5, 0), ohlcvListDaily.size());
                System.out.println("ohlcvListDaily Size " + ohlcvListDaily.size());
                System.out.println("ohlcvList Size " + ohlcvList.size());

                ohlcvList.forEach(
                        ohlcv -> {
                            System.out.println(ohlcv);
                            StockPrice stockPrice =
                                    stockPriceService.createOrUpdate(
                                            stock,
                                            Timeframe.DAILY,
                                            ohlcv.getOpen(),
                                            ohlcv.getHigh(),
                                            ohlcv.getLow(),
                                            ohlcv.getClose(),
                                            LocalDate.ofInstant(
                                                    ohlcv.getBhavDate(), ZoneOffset.UTC));
                            System.out.println(stockPrice);
                        });

                System.out.println("Printing Weekly Data");
                List<OHLCV> ohlcvListWeekly =
                        ohlcvService.fetch(
                                Timeframe.WEEKLY,
                                stock.getNseSymbol(),
                                LocalDate.of(2022, 01, 01),
                                LocalDate.now());
                ohlcvList =
                        ohlcvListWeekly.subList(
                                Math.max(ohlcvListWeekly.size() - 5, 0), ohlcvListWeekly.size());
                System.out.println("ohlcvListWeekly Size " + ohlcvListWeekly.size());
                System.out.println("ohlcvList Size " + ohlcvList.size());

                ohlcvList.forEach(
                        ohlcv -> {
                            System.out.println(ohlcv);
                            StockPrice stockPrice =
                                    stockPriceService.createOrUpdate(
                                            stock,
                                            Timeframe.WEEKLY,
                                            ohlcv.getOpen(),
                                            ohlcv.getHigh(),
                                            ohlcv.getLow(),
                                            ohlcv.getClose(),
                                            LocalDate.ofInstant(
                                                    ohlcv.getBhavDate(), ZoneOffset.UTC));
                            System.out.println(stockPrice);
                        });

                System.out.println("Printing Monthly Data");
                List<OHLCV> ohlcvListMonthly =
                        ohlcvService.fetch(
                                Timeframe.MONTHLY,
                                stock.getNseSymbol(),
                                LocalDate.of(2022, 01, 01),
                                LocalDate.now());
                ohlcvList =
                        ohlcvListMonthly.subList(
                                Math.max(ohlcvListMonthly.size() - 5, 0), ohlcvListMonthly.size());
                System.out.println("ohlcvListMonthly Size " + ohlcvListMonthly.size());
                System.out.println("ohlcvList Size " + ohlcvList.size());

                ohlcvList.forEach(
                        ohlcv -> {
                            System.out.println(ohlcv);
                            StockPrice stockPrice =
                                    stockPriceService.createOrUpdate(
                                            stock,
                                            Timeframe.MONTHLY,
                                            ohlcv.getOpen(),
                                            ohlcv.getHigh(),
                                            ohlcv.getLow(),
                                            ohlcv.getClose(),
                                            LocalDate.ofInstant(
                                                    ohlcv.getBhavDate(), ZoneOffset.UTC));
                            System.out.println(stockPrice);
                        });

                System.out.println("Printing Quarterly Data");
                List<OHLCV> ohlcvListQuarterly =
                        ohlcvService.fetch(
                                Timeframe.QUARTERLY,
                                stock.getNseSymbol(),
                                LocalDate.of(2022, 01, 01),
                                LocalDate.now());
                ohlcvList =
                        ohlcvListQuarterly.subList(
                                Math.max(ohlcvListQuarterly.size() - 5, 0),
                                ohlcvListQuarterly.size());
                System.out.println("ohlcvListQuarterly Size " + ohlcvListQuarterly.size());
                System.out.println("ohlcvList Size " + ohlcvList.size());

                ohlcvList.forEach(
                        ohlcv -> {
                            System.out.println(ohlcv);
                            StockPrice stockPrice =
                                    stockPriceService.createOrUpdate(
                                            stock,
                                            Timeframe.QUARTERLY,
                                            ohlcv.getOpen(),
                                            ohlcv.getHigh(),
                                            ohlcv.getLow(),
                                            ohlcv.getClose(),
                                            LocalDate.ofInstant(
                                                    ohlcv.getBhavDate(), ZoneOffset.UTC));
                            System.out.println(stockPrice);
                        });

                System.out.println("Printing Yearly Data");
                List<OHLCV> ohlcvListYearly =
                        ohlcvService.fetch(
                                Timeframe.YEARLY,
                                stock.getNseSymbol(),
                                LocalDate.of(2022, 01, 01),
                                LocalDate.now());
                ohlcvList =
                        ohlcvListYearly.subList(
                                Math.max(ohlcvListYearly.size() - 5, 0), ohlcvListYearly.size());
                System.out.println("ohlcvListYearly Size " + ohlcvListYearly.size());
                System.out.println("ohlcvList Size " + ohlcvList.size());

                ohlcvList.forEach(
                        ohlcv -> {
                            System.out.println(ohlcv);
                            StockPrice stockPrice =
                                    stockPriceService.createOrUpdate(
                                            stock,
                                            Timeframe.YEARLY,
                                            ohlcv.getOpen(),
                                            ohlcv.getHigh(),
                                            ohlcv.getLow(),
                                            ohlcv.getClose(),
                                            LocalDate.ofInstant(
                                                    ohlcv.getBhavDate(), ZoneOffset.UTC));
                            System.out.println(stockPrice);
                        });

                stock.setActivityCompleted(true);

                stockRepository.save(stock);
                --countTotal;
                long endTime = System.currentTimeMillis();

                System.out.println(
                        "Completed activity for "
                                + stock.getNseSymbol()
                                + " took "
                                + (endTime - startTime)
                                + "ms");
                System.out.println("Remaining " + countTotal);

                miscUtil.delay(25);
            } catch (Exception e) {
                System.out.println("An Error occurred while updating price");
            }
        }
    }

    public void updateFinancialsForStocks() {
        // Fetch the list of stocks where activity is not completed
        List<Stock> stockList = stockRepository.findByActivityCompleted(false);
        int countTotal = stockList.size();
        for (Stock stock : stockList) {

            long startTime = System.currentTimeMillis();
            System.out.println("Starting activity for " + stock.getNseSymbol());
            try {
                // Fetch financial details (issuedSize and faceValue) using the fetcher
                FinancialsSummaryDto financialsSummaryDto =
                        nseTotalIssuedSharesAndFaceValueFetcher.getIssuedSharesAndFaceValue(
                                stock.getNseSymbol());

                // Check if the DTO was received
                if (financialsSummaryDto != null) {
                    // Map DTO to entity
                    FinancialsSummary financialsSummary =
                            mapDtoToEntity(financialsSummaryDto, stock);

                    // Create or update the FinancialsSummary
                    financialsSummaryService.createOrUpdate(stock, financialsSummary);
                } else {
                    System.out.println(
                            "No financial details available for stock: " + stock.getNseSymbol());
                }
                stock.setActivityCompleted(true);

                stockRepository.save(stock);
                --countTotal;
                long endTime = System.currentTimeMillis();

                System.out.println(
                        "Completed activity for "
                                + stock.getNseSymbol()
                                + " took "
                                + (endTime - startTime)
                                + "ms");
                System.out.println("Remaining " + countTotal);

                ThreadsUtil.delay();
            } catch (Exception e) {
                System.out.println("An Error occurred while updating price");
            }
        }
    }

    // Method to map DTO to Entity
    private FinancialsSummary mapDtoToEntity(FinancialsSummaryDto dto, Stock stock) {
        return FinancialsSummary.builder()
                .stock(stock)
                .issuedSize(dto.getIssuedSize())
                .faceValue(dto.getFaceValue())
                .build();
    }

    private void updateTechnicals() {

        List<Stock> stockList = stockRepository.findByActivityCompleted(false);

        int countTotal = stockList.size();

        for (Stock stock : stockList) {
            try {
                long startTime = System.currentTimeMillis();
                System.out.println("Starting activity for " + stock.getNseSymbol());

                updateTechnicalsService.updateTechnicals(
                        Timeframe.DAILY, stock, LocalDate.of(2025, 03, 11));
                updateTechnicalsService.updateTechnicals(
                        Timeframe.DAILY, stock, LocalDate.of(2025, 03, 12));
                updateTechnicalsService.updateTechnicals(
                        Timeframe.DAILY, stock, LocalDate.of(2025, 03, 13));

                updateTechnicalsService.updateTechnicals(
                        Timeframe.WEEKLY, stock, LocalDate.of(2025, 02, 28));
                updateTechnicalsService.updateTechnicals(
                        Timeframe.WEEKLY, stock, LocalDate.of(2025, 03, 07));
                updateTechnicalsService.updateTechnicals(
                        Timeframe.WEEKLY, stock, LocalDate.of(2025, 03, 13));

                updateTechnicalsService.updateTechnicals(
                        Timeframe.MONTHLY, stock, LocalDate.of(2024, 12, 31));
                updateTechnicalsService.updateTechnicals(
                        Timeframe.MONTHLY, stock, LocalDate.of(2025, 01, 31));
                updateTechnicalsService.updateTechnicals(
                        Timeframe.MONTHLY, stock, LocalDate.of(2025, 02, 28));

                stock.setActivityCompleted(true);

                stockRepository.save(stock);
                --countTotal;
                long endTime = System.currentTimeMillis();

                System.out.println(
                        "Completed activity for "
                                + stock.getNseSymbol()
                                + " took "
                                + (endTime - startTime)
                                + "ms");
                System.out.println("Remaining " + countTotal);

                miscUtil.delay(25);
            } catch (Exception e) {
                System.out.println("An Error occurred while updating price");
            }
        }
    }

    public void processPriceUpdate(boolean updateHistory) {
        List<Stock> stockList = stockRepository.findByActivityCompleted(false);
        int threadCount = Runtime.getRuntime().availableProcessors(); // Use CPU cores
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        AtomicInteger countTotal = new AtomicInteger(stockList.size());

        int yearsBack = 9;
        int quartersBack = 9;
        int monthsBack = 9;
        int weeksBack = 9;
        int daysBack = 9;

        if (updateHistory) {
            yearsBack = 30;
            quartersBack = 30;
            monthsBack = 240;
            weeksBack = 700;
            daysBack = 700;
        }

        final LocalDate yearlyInitialDate = LocalDate.now().minusYears(yearsBack);
        final LocalDate quarterlyInitialDate = LocalDate.now().minusYears(quartersBack);
        final LocalDate monthlyInitialDate = LocalDate.now().minusMonths(monthsBack);
        final LocalDate weeklyInitialDate = LocalDate.now().minusWeeks(weeksBack);
        final LocalDate dailyInitialDate = LocalDate.now().minusDays(daysBack);

        for (Stock stock : stockList) {
            executorService.submit(
                    () -> {
                        try {
                            processYearlyPriceUpdate(stock, yearlyInitialDate, updateHistory);
                            processQuarterlyPriceUpdate(stock, quarterlyInitialDate, updateHistory);
                            processMonthlyPriceUpdate(stock, monthlyInitialDate, updateHistory);
                            processWeeklyPriceUpdate(stock, weeklyInitialDate, updateHistory);
                            if (!updateHistory) {
                                processDailyPriceUpdate(stock, dailyInitialDate);
                            }

                            stock.setActivityCompleted(true);
                            stockRepository.save(stock);
                            miscUtil.delay();

                            System.out.println("Remaining: " + countTotal.decrementAndGet());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        executorService.shutdown(); // No new tasks will be accepted
    }

    private void processYearlyPriceUpdate(
            Stock stock, LocalDate initialDate, boolean updateHistory) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting yearly price update for " + stock.getNseSymbol());

        try {

            LocalDate from = miscUtil.yearFirstDay(initialDate);
            LocalDate to = miscUtil.yearLastDay(from);

            System.out.println("yearly from: " + from + " to: " + to);
            List<com.example.data.storage.documents.StockPrice> stockPriceList = new ArrayList<>();
            com.example.data.storage.documents.StockPrice stockPrice = null;
            do {
                System.out.println("yearly from: " + from + " to: " + to);

                OHLCV ohlcv =
                        yearlySupportResistanceService.supportAndResistance(
                                stock.getNseSymbol(), from, to);

                if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
                    StockPriceIO stockPriceIO =
                            new StockPriceIO(
                                    "NSE",
                                    stock.getCompanyName(),
                                    stock.getNseSymbol(),
                                    "EQ",
                                    ohlcv.getOpen(),
                                    ohlcv.getHigh(),
                                    ohlcv.getLow(),
                                    ohlcv.getClose(),
                                    ohlcv.getClose(),
                                    ohlcv.getOpen(),
                                    ohlcv.getVolume(),
                                    0.00,
                                    ohlcv.getBhavDate()
                                            .atOffset(ZoneOffset.UTC)
                                            .toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                                    1,
                                    stock.getIsinCode(),
                                    stock.getInstrument());

                    stockPriceIO.setBhavDate(ohlcv.getBhavDate());

                    stockPriceIO.setTimestamp(
                            ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                    stockPriceIO.setTimeFrame(Timeframe.YEARLY);

                    stockPrice =
                            new com.example.data.storage.documents.StockPrice(
                                    stockPriceIO.getNseSymbol(),
                                    stockPriceIO.getBhavDate(),
                                    stockPriceIO.getOpen(),
                                    stockPriceIO.getHigh(),
                                    stockPriceIO.getLow(),
                                    stockPriceIO.getClose(),
                                    stockPriceIO.getTottrdqty());
                    if (!updateHistory) {
                        updatePriceService.updatePrice(Timeframe.YEARLY, stock, stockPrice);
                    }
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = miscUtil.yearLastDay(from);

            } while (to.isBefore(miscUtil.currentDate()));

            if (updateHistory) {

                if (stockPriceList != null && !stockPriceList.isEmpty()) {
                    System.out.println("Deleting existing bhav " + stock.getNseSymbol());
                    long count = priceTemplate.delete(Timeframe.YEARLY, stock.getNseSymbol());
                    miscUtil.delay(25);
                    System.out.println(
                            "Deleted existing bhav " + count + " " + stock.getNseSymbol());
                }

                priceTemplate.create(Timeframe.YEARLY, stockPriceList);
            }

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");

            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    private void processQuarterlyPriceUpdate(
            Stock stock, LocalDate initialDate, boolean updateHistory) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting quarterly activity for " + stock.getNseSymbol());

        try {

            LocalDate from = miscUtil.quarterFirstDay(initialDate);
            LocalDate to = miscUtil.quarterLastDay(from);

            List<com.example.data.storage.documents.StockPrice> stockPriceList = new ArrayList<>();
            com.example.data.storage.documents.StockPrice stockPrice = null;
            do {

                System.out.println("quarterly from: " + from + " to: " + to);

                OHLCV ohlcv =
                        quarterlySupportResistanceService.supportAndResistance(
                                stock.getNseSymbol(), from, to);

                if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
                    StockPriceIO stockPriceIO =
                            new StockPriceIO(
                                    "NSE",
                                    stock.getCompanyName(),
                                    stock.getNseSymbol(),
                                    "EQ",
                                    ohlcv.getOpen(),
                                    ohlcv.getHigh(),
                                    ohlcv.getLow(),
                                    ohlcv.getClose(),
                                    ohlcv.getClose(),
                                    ohlcv.getOpen(),
                                    ohlcv.getVolume(),
                                    0.00,
                                    ohlcv.getBhavDate()
                                            .atOffset(ZoneOffset.UTC)
                                            .toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                                    1,
                                    stock.getIsinCode(),
                                    stock.getInstrument());

                    stockPriceIO.setBhavDate(ohlcv.getBhavDate());

                    stockPriceIO.setTimestamp(
                            ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                    stockPriceIO.setTimeFrame(Timeframe.QUARTERLY);

                    stockPrice =
                            new com.example.data.storage.documents.StockPrice(
                                    stockPriceIO.getNseSymbol(),
                                    stockPriceIO.getBhavDate(),
                                    stockPriceIO.getOpen(),
                                    stockPriceIO.getHigh(),
                                    stockPriceIO.getLow(),
                                    stockPriceIO.getClose(),
                                    stockPriceIO.getTottrdqty());
                    if (!updateHistory) {

                        updatePriceService.updatePrice(Timeframe.QUARTERLY, stock, stockPrice);
                    }
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = miscUtil.quarterLastDay(from);

            } while (to.isBefore(miscUtil.currentDate()));

            if (updateHistory) {
                if (stockPriceList != null && !stockPriceList.isEmpty()) {
                    System.out.println("Deleting existing bhav " + stock.getNseSymbol());
                    long count = priceTemplate.delete(Timeframe.QUARTERLY, stock.getNseSymbol());
                    miscUtil.delay(25);
                    System.out.println(
                            "Deleted existing bhav " + count + " " + stock.getNseSymbol());
                }
                priceTemplate.create(Timeframe.QUARTERLY, stockPriceList);
            }

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");

            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    private void processMonthlyPriceUpdate(
            Stock stock, LocalDate initialDate, boolean updateHistory) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting monthly activity for " + stock.getNseSymbol());

        try {

            LocalDate from = initialDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate to = from.with(TemporalAdjusters.lastDayOfMonth());

            List<com.example.data.storage.documents.StockPrice> stockPriceList = new ArrayList<>();
            com.example.data.storage.documents.StockPrice stockPrice = null;
            do {
                System.out.println("monthly from: " + from + " to: " + to);

                OHLCV ohlcv =
                        monthlySupportResistanceService.supportAndResistance(
                                stock.getNseSymbol(), from, to);

                if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
                    StockPriceIO stockPriceIO =
                            new StockPriceIO(
                                    "NSE",
                                    stock.getCompanyName(),
                                    stock.getNseSymbol(),
                                    "EQ",
                                    ohlcv.getOpen(),
                                    ohlcv.getHigh(),
                                    ohlcv.getLow(),
                                    ohlcv.getClose(),
                                    ohlcv.getClose(),
                                    ohlcv.getOpen(),
                                    ohlcv.getVolume(),
                                    0.00,
                                    ohlcv.getBhavDate()
                                            .atOffset(ZoneOffset.UTC)
                                            .toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                                    1,
                                    stock.getIsinCode(),
                                    stock.getInstrument());

                    stockPriceIO.setBhavDate(ohlcv.getBhavDate());

                    stockPriceIO.setTimestamp(
                            ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                    stockPriceIO.setTimeFrame(Timeframe.MONTHLY);

                    stockPrice =
                            new com.example.data.storage.documents.StockPrice(
                                    stockPriceIO.getNseSymbol(),
                                    stockPriceIO.getBhavDate(),
                                    stockPriceIO.getOpen(),
                                    stockPriceIO.getHigh(),
                                    stockPriceIO.getLow(),
                                    stockPriceIO.getClose(),
                                    stockPriceIO.getTottrdqty());
                    if (!updateHistory) {

                        updatePriceService.updatePrice(Timeframe.MONTHLY, stock, stockPrice);
                    }
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.lastDayOfMonth());

            } while (to.isBefore(miscUtil.currentDate()));

            if (updateHistory) {
                if (stockPriceList != null && !stockPriceList.isEmpty()) {
                    System.out.println("Deleting existing bhav " + stock.getNseSymbol());
                    long count = priceTemplate.delete(Timeframe.MONTHLY, stock.getNseSymbol());
                    miscUtil.delay(25);
                    System.out.println(
                            "Deleted existing bhav " + count + " " + stock.getNseSymbol());
                }
                priceTemplate.create(Timeframe.MONTHLY, stockPriceList);
            }

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");
            // System.out.println("Remaining " + countTotal);
            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    private void processWeeklyPriceUpdate(
            Stock stock, LocalDate initialDate, boolean updateHistory) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting weekly activity for " + stock.getNseSymbol());

        try {

            LocalDate from = initialDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            List<com.example.data.storage.documents.StockPrice> stockPriceList = new ArrayList<>();
            com.example.data.storage.documents.StockPrice stockPrice = null;
            do {

                System.out.println("weekly from: " + from + " to: " + to);

                OHLCV ohlcv =
                        weeklySupportResistanceService.supportAndResistance(
                                stock.getNseSymbol(), from, to);

                if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
                    StockPriceIO stockPriceIO =
                            new StockPriceIO(
                                    "NSE",
                                    stock.getCompanyName(),
                                    stock.getNseSymbol(),
                                    "EQ",
                                    ohlcv.getOpen(),
                                    ohlcv.getHigh(),
                                    ohlcv.getLow(),
                                    ohlcv.getClose(),
                                    ohlcv.getClose(),
                                    ohlcv.getOpen(),
                                    ohlcv.getVolume(),
                                    0.00,
                                    ohlcv.getBhavDate()
                                            .atOffset(ZoneOffset.UTC)
                                            .toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                                    1,
                                    stock.getIsinCode(),
                                    stock.getInstrument());

                    stockPriceIO.setBhavDate(ohlcv.getBhavDate());

                    stockPriceIO.setTimestamp(
                            ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                    stockPriceIO.setTimeFrame(Timeframe.WEEKLY);

                    stockPrice =
                            new com.example.data.storage.documents.StockPrice(
                                    stockPriceIO.getNseSymbol(),
                                    stockPriceIO.getBhavDate(),
                                    stockPriceIO.getOpen(),
                                    stockPriceIO.getHigh(),
                                    stockPriceIO.getLow(),
                                    stockPriceIO.getClose(),
                                    stockPriceIO.getTottrdqty());
                    if (!updateHistory) {

                        updatePriceService.updatePrice(Timeframe.WEEKLY, stock, stockPrice);
                    }
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            } while (to.isBefore(miscUtil.currentDate().plusDays(3)));

            if (updateHistory) {
                if (stockPriceList != null && !stockPriceList.isEmpty()) {
                    System.out.println("Deleting existing bhav " + stock.getNseSymbol());
                    long count = priceTemplate.delete(Timeframe.WEEKLY, stock.getNseSymbol());
                    miscUtil.delay(25);
                    System.out.println(
                            "Deleted existing bhav " + count + " " + stock.getNseSymbol());
                }
                priceTemplate.create(Timeframe.WEEKLY, stockPriceList);
            }

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");

            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    private void processDailyPriceUpdate(Stock stock, LocalDate initialDate) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting daily activity for " + stock.getNseSymbol());

        try {

            LocalDate from = initialDate;
            LocalDate to = initialDate;

            List<com.example.data.storage.documents.StockPrice> stockPriceList = new ArrayList<>();
            com.example.data.storage.documents.StockPrice stockPrice = null;
            do {

                System.out.println("daily from: " + from + " to: " + to);

                OHLCV ohlcv =
                        dailySupportResistanceService.supportAndResistance(
                                stock.getNseSymbol(), from, to);

                if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
                    StockPriceIO stockPriceIO =
                            new StockPriceIO(
                                    "NSE",
                                    stock.getCompanyName(),
                                    stock.getNseSymbol(),
                                    "EQ",
                                    ohlcv.getOpen(),
                                    ohlcv.getHigh(),
                                    ohlcv.getLow(),
                                    ohlcv.getClose(),
                                    ohlcv.getClose(),
                                    ohlcv.getOpen(),
                                    ohlcv.getVolume(),
                                    0.00,
                                    ohlcv.getBhavDate()
                                            .atOffset(ZoneOffset.UTC)
                                            .toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                                    1,
                                    stock.getIsinCode(),
                                    stock.getInstrument());

                    stockPriceIO.setBhavDate(ohlcv.getBhavDate());

                    stockPriceIO.setTimestamp(
                            ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                    stockPriceIO.setTimeFrame(Timeframe.DAILY);

                    stockPrice =
                            new com.example.data.storage.documents.StockPrice(
                                    stockPriceIO.getNseSymbol(),
                                    stockPriceIO.getBhavDate(),
                                    stockPriceIO.getOpen(),
                                    stockPriceIO.getHigh(),
                                    stockPriceIO.getLow(),
                                    stockPriceIO.getClose(),
                                    stockPriceIO.getTottrdqty());

                    updatePriceService.updatePrice(Timeframe.DAILY, stock, stockPrice);
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = from;

            } while (to.isBefore(miscUtil.currentDate().plusDays(1)));

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");

            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    public void processTechnicalsUpdate() {
        List<Stock> stockList = stockRepository.findByActivityCompleted(false);
        int threadCount = Runtime.getRuntime().availableProcessors(); // Use CPU cores
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        AtomicInteger countTotal = new AtomicInteger(stockList.size());

        int yearsBack = 9;
        int quartersBack = 9;
        int monthsBack = 9;
        int weeksBack = 9;
        int daysBack = 9;

        final LocalDate monthlyInitialDate = LocalDate.now().minusMonths(monthsBack);
        final LocalDate weeklyInitialDate = LocalDate.now().minusWeeks(weeksBack);
        final LocalDate dailyInitialDate = LocalDate.now().minusDays(daysBack);

        for (Stock stock : stockList) {
            executorService.submit(
                    () -> {
                        try {

                            processMonthlyTechnicalsUpdate(stock, monthlyInitialDate);
                            processWeeklyTechnicalsUpdate(stock, weeklyInitialDate);
                            processDailyTechnicalsUpdate(stock, dailyInitialDate);

                            stock.setActivityCompleted(true);
                            stockRepository.save(stock);
                            miscUtil.delay();

                            System.out.println("Remaining: " + countTotal.decrementAndGet());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        executorService.shutdown(); // No new tasks will be accepted
    }

    private void processMonthlyTechnicalsUpdate(Stock stock, LocalDate initialDate) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting monthly activity for " + stock.getNseSymbol());

        try {

            LocalDate from = initialDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate to = from.with(TemporalAdjusters.lastDayOfMonth());

            com.example.data.storage.documents.StockTechnicals stockTechnicals = null;
            do {
                System.out.println("monthly from: " + from + " to: " + to);

                stockTechnicals = updateTechnicalsService.build(Timeframe.MONTHLY, stock, to);

                updateTechnicalsService.updateTechnicals(Timeframe.MONTHLY, stock, stockTechnicals);

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.lastDayOfMonth());

            } while (to.isBefore(miscUtil.currentDate()));

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");
            // System.out.println("Remaining " + countTotal);
            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    private void processWeeklyTechnicalsUpdate(Stock stock, LocalDate initialDate) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting weekly activity for " + stock.getNseSymbol());

        try {

            LocalDate from = initialDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            com.example.data.storage.documents.StockTechnicals stockTechnicals = null;
            do {

                System.out.println("weekly from: " + from + " to: " + to);

                stockTechnicals = updateTechnicalsService.build(Timeframe.WEEKLY, stock, to);

                System.out.println("Sessiom date: " + stockTechnicals.getBhavDate());

                updateTechnicalsService.updateTechnicals(Timeframe.WEEKLY, stock, stockTechnicals);

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            } while (to.isBefore(miscUtil.currentDate().plusDays(3)));

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");

            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    private void processDailyTechnicalsUpdate(Stock stock, LocalDate initialDate) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting daily activity for " + stock.getNseSymbol());

        try {

            LocalDate from = initialDate;
            LocalDate to = initialDate;

            List<com.example.data.storage.documents.StockPrice> stockPriceList = new ArrayList<>();
            com.example.data.storage.documents.StockTechnicals stockTechnicals = null;
            do {

                System.out.println("daily from: " + from + " to: " + to);

                stockTechnicals = updateTechnicalsService.build(Timeframe.DAILY, stock, to);

                updateTechnicalsService.updateTechnicals(Timeframe.DAILY, stock, stockTechnicals);

                from = to.plusDays(1);
                to = from;

            } while (to.isBefore(miscUtil.currentDate().plusDays(1)));

            long endTime = System.currentTimeMillis();

            System.out.println(
                    "Completed activity for "
                            + stock.getNseSymbol()
                            + " took "
                            + (endTime - startTime)
                            + "ms");

            miscUtil.delay(500);
        } catch (Exception e) {
            System.out.println("An error occured while getting data " + stock.getNseSymbol());
        }
    }

    private void syncTechnicals() {}

    private void printJson(Object stockTechnicals) {

        // pretty print
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        String json;
        try {
            json = mapper.writeValueAsString(stockTechnicals);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
