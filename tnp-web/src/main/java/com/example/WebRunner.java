package com.example;

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
import com.example.dto.io.BseSectorListResponse;
import com.example.dto.io.FinancialsSummaryDto;
import com.example.dto.io.SectorIO;
import com.example.dto.io.StockPriceIO;
import com.example.external.*;
import com.example.external.factor.FactorRediff;
import com.example.external.ta.service.McService;
import com.example.processor.BhavProcessor;
import com.example.service.*;
import com.example.service.calc.*;
import com.example.service.impl.FundamentalResearchService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class WebRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebRunner.class);

    @Autowired private BhavcopyService bhavcopyService;

    // @Autowired private DailySupportResistanceService dailySupportResistanceService;

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

    @Autowired private ResearchExecutorService researchExecutorService;
    @Autowired private OnBalanceVolumeCalculatorService onBalanceVolumeCalculatorService;

    @Autowired private RelativeStrengthIndexCalculatorService rsiService;

    @Autowired private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

    @Autowired
    private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

    @Autowired private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;

    @Autowired private McService mcService;

    @Autowired private QuarterlySupportResistanceService quarterlySupportResistanceService;

    @Autowired private MonthlySupportResistanceService monthlySupportResistanceService;

    @Autowired private WeeklySupportResistanceService weeklySupportResistanceService;

    @Autowired private StockRepository stockRepository;

    @Autowired private StockPriceHelperService stockPriceHelperService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Autowired private BreakoutService breakoutService;

    @Autowired private ResearchTechnicalService<ResearchTechnical> researchTechnicalService;

    @Autowired private CandleStickConfirmationService candleStickHelperService;
    @Autowired private SwingActionService swingActionService;

    @Autowired private PriceActionService priceActionService;

    @Autowired private FundamentalResearchService fundamentalResearchService;
    @Autowired private CandleStickService candleStickService;

    @Autowired private UpdateTechnicalsService updateTechnicalsService;

    @Autowired private PositionService positionService;

    @Autowired private TrendService trendService;

    @Autowired private DynamicTrendService dynamicTrendService;

    @Autowired private YearlySupportResistanceService yearlySupportResistanceService;

    @Autowired private TimeframeSupportResistanceService timeframeSupportResistanceService;

    @Autowired private SectorDownloadService sectorDownloadService;

    @Autowired private StockFinancialsService stockFinancialsService;

    @Autowired private NSEFinancialsFetcher nseFinancialsFetcher;

    @Autowired private NSEXmlService nseXmlService;

    @Autowired private FinancialsSummaryService financialsSummaryService;

    @Autowired
    private NSETotalIssuedSharesAndFaceValueFetcher nseTotalIssuedSharesAndFaceValueFetcher;

    @Autowired private VolumeIndicatorService volumeIndicatorService;

    @Autowired
    private DynamicMovingAverageSupportResolverService dynamicMovingAverageSupportResolverService;

    @Override
    public void run(String... arg0) throws InterruptedException, IOException {

        log.info("Application started....");

        //bhavProcessor.processAndResearchTechnicals();

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

        // this.allocatePositions();

        // this.updateRemainigSectorsActivityFromNSE();

        // this.scanBullishCandleStickPattern();
        // this.scanBearishCandleStickPattern();
        // this.testTrend();

        // this.updateFinaicials();
        // this.testScore();
        // this.updatePriceHistory();
        // this.updateTechnicals();
        // this.processBhavFromApi();

        // this.updateSectorsActivity();
        // this.updateFinancialsForStocks();
        // this.processPriceUpdate();
        // this.processTechnicalsUpdate();

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
        this.testDynamicSR();
        // this.updateScore();
        System.out.println("STARTED");
    }

    private void updateScore() {
        List<ResearchTechnical> researchTechnicalList =
                researchTechnicalService.getAll(Trade.Type.BUY);

        for (ResearchTechnical researchTechnical : researchTechnicalList) {
            if (researchTechnical.getResearchDate().isAfter(LocalDate.of(2025, 5, 18))) {
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
        stockList.add(stockService.getStockByNseSymbol("MMFL"));
        stockList.add(stockService.getStockByNseSymbol("DCMSRIND"));
        stockList.add(stockService.getStockByNseSymbol("NBIFIN"));
        stockList.add(stockService.getStockByNseSymbol("WEIZMANIND"));


        for (Stock stock : stockList) {
            StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
            StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, Timeframe.DAILY);


            List<MAEvaluationResult> maEvaluationResults =
                    dynamicMovingAverageSupportResolverService.evaluateInteractions(Timeframe.DAILY, stockPrice, stockTechnicals);

            maEvaluationResults.forEach(
                    mae -> {
                        System.out.println(stock.getNseSymbol() + " : " + stockPrice.getClose() + " : " + mae);
                    });



            Optional<MAEvaluationResult> evaluationResultOptional= dynamicMovingAverageSupportResolverService
                    .evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);

            if(evaluationResultOptional.isPresent()) {
                MAEvaluationResult evaluationResult = evaluationResultOptional.get();

                if (evaluationResult.isNearSupport()){
                    System.out.println("SUPPORT : " + stock.getNseSymbol() + " : " + stockPrice.getClose() + " : " + evaluationResult);
                }
                else if (evaluationResult.isBreakout()){
                System.out.println("BREAKOUT : " + stock.getNseSymbol() + " : " + stockPrice.getClose() + " : " + evaluationResult);
                }
                else if (evaluationResult.isNearResistance()){
                    System.out.println("RESISTANCE : " + stock.getNseSymbol() + " : " + stockPrice.getClose() + " : " + evaluationResult);
                }
                else if (evaluationResult.isBreakdown()){
                    System.out.println("BREAKDOWN : " + stock.getNseSymbol() + " : " + stockPrice.getClose() + " : " + evaluationResult);
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

    /** Position Size = (Total trading fund * Risk%)/SL% */
    private void allocatePositions() {

        double capital = 78000.0;
        double riskFactor = 1.0;

        // User user = new User();
        User user = userService.getUserByUsername("phsdhan");

        List<ResearchTechnical> researchTechnicalList =
                researchTechnicalService.getAll(Trade.Type.BUY);

        for (ResearchTechnical researchTechnical : researchTechnicalList) {

            double allottedAmount = ((capital * riskFactor) / researchTechnical.getRisk());

            long positionSize = (long) (allottedAmount / researchTechnical.getResearchPrice());

            double reward =
                    formulaService.calculateChangePercentage(
                            researchTechnical.getResearchPrice(), researchTechnical.getTarget());

            double risk = formulaService.calculateFraction(capital, riskFactor);
            double stopLoss =
                    (researchTechnical.getResearchPrice() - researchTechnical.getStopLoss());
            positionSize = (long) (risk / stopLoss);
            positionSize = positionService.calculate(user.getId(), researchTechnical);

            System.out.println(researchTechnical.getStock().getNseSymbol() + " " + positionSize);
        }
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
                miscUtil.delay();
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

    private void scanBullishCandleStickPattern() {

        System.out.println("******* Scanning Bullish *******");
        List<Stock> stockList = stockService.getActiveStocks();

        for (Stock stock : stockList) {

            if (stock.getSeries() != null && stock.getSeries().equalsIgnoreCase("EQ")) {
                if (fundamentalResearchService.isMcapInRange(stock)) {
                    // candleStickExecutorService.executeBullish(stock);
                    if (researchLedgerFundamentalService.isResearchActive(stock)) {
                        System.out.println("FUNDAMENTAL " + stock.getNseSymbol());
                    }
                    System.out.println("******* DAILY :" + stock.getNseSymbol() + " *******");

                    StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
                    StockTechnicals stockTechnicals =
                            stockTechnicalsService.get(stock, Timeframe.DAILY);
                    TradeSetup tradeSetup = priceActionService.breakOut(stock, Timeframe.DAILY);
                    // System.out.println(stock.getNseSymbol() + " : Price Action " +
                    // tradeSetup.isActive());
                    tradeSetup = swingActionService.breakOut(stock, Timeframe.DAILY);
                    System.out.println(
                            stock.getNseSymbol() + " : Swing Action " + tradeSetup.isActive());
                    if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
                        System.out.println("******* WEEKLY :" + stock.getNseSymbol() + " *******");
                        stockPrice = stockPriceService.get(stock, Timeframe.WEEKLY);
                        stockTechnicals = stockTechnicalsService.get(stock, Timeframe.WEEKLY);
                        tradeSetup = priceActionService.breakOut(stock, Timeframe.WEEKLY);

                        tradeSetup = swingActionService.breakOut(stock, Timeframe.WEEKLY);
                    }
                    if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
                        System.out.println("******* MONTHLY :" + stock.getNseSymbol() + " *******");
                        priceActionService.breakOut(stock, Timeframe.MONTHLY);
                        swingActionService.breakOut(stock, Timeframe.MONTHLY);
                    }
                }
            }
        }
    }

    private void scanBearishCandleStickPattern() {

        System.out.println("******* Scanning Bullish *******");
        List<Stock> stockList = stockService.getActiveStocks();

        System.out.println("******* Scanning Bearish From Master *******");

        for (Stock stock : stockList) {
            if (stock.getSeries() != null && stock.getSeries().equalsIgnoreCase("EQ")) {
                if (fundamentalResearchService.isMcapInRange(stock)) {
                    /*
                    if(researchLedgerTechnicalService.isActive(stock, ResearchIO.ResearchTrigger.BUY)){
                    	System.out.println("RESEARCH " + stock.getNseSymbol());
                    }*/

                    if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
                        System.out.println("******* MONTHLY :" + stock.getNseSymbol() + " *******");
                        priceActionService.breakDown(stock, Timeframe.MONTHLY);
                    }
                    if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
                        System.out.println("******* WEEKLY :" + stock.getNseSymbol() + " *******");
                        priceActionService.breakDown(stock, Timeframe.WEEKLY);
                    }
                    System.out.println("******* DAILY :" + stock.getNseSymbol() + " *******");
                    priceActionService.breakDown(stock, Timeframe.DAILY);
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

    private void processBhavFromApi() {
        List<Stock> stockList = stockRepository.findByActivityCompleted(false);

        int countTotal = stockList.size();

        for (Stock stock : stockList) {
            long startTime = System.currentTimeMillis();
            System.out.println("Starting activity for " + stock.getNseSymbol());

            try {
                List<OHLCV> ohlcvList = mcService.getMCOHLP(stock.getNseSymbol(), 25, 6137);

                if (ohlcvList != null && !ohlcvList.isEmpty()) {
                    System.out.println("Deleting existing bhav " + stock.getNseSymbol());
                    long count = priceTemplate.delete(stock.getNseSymbol());
                    miscUtil.delay(25);
                    System.out.println(
                            "Deleted existing bhav " + count + " " + stock.getNseSymbol());
                }

                List<com.example.data.storage.documents.StockPrice> stockPriceList =
                        new ArrayList<>();
                com.example.data.storage.documents.StockPrice stockPrice = null;

                for (OHLCV ohlcv : ohlcvList) {
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
                                        stock.getIsinCode());
                        // System.out.println("Debug1 " + stock.getNseSymbol());
                        stockPriceIO.setBhavDate(ohlcv.getBhavDate());
                        // System.out.println("Debug2 " + stock.getNseSymbol());
                        stockPriceIO.setTimestamp(
                                ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
                        // System.out.println("Debug3 " + stock.getNseSymbol());
                        stockPrice =
                                new com.example.data.storage.documents.StockPrice(
                                        stockPriceIO.getNseSymbol(),
                                        stockPriceIO.getBhavDate(),
                                        stockPriceIO.getOpen(),
                                        stockPriceIO.getHigh(),
                                        stockPriceIO.getLow(),
                                        stockPriceIO.getClose(),
                                        stockPriceIO.getTottrdqty());
                        // System.out.println("Debug4 " + stock.getNseSymbol());
                        stockPriceList.add(stockPrice);
                    }
                }
                System.out.println("Debug5 " + stock.getNseSymbol());
                priceTemplate.create(stockPriceList);
                System.out.println("Debug6 " + stock.getNseSymbol());
                stock.setActivityCompleted(true);
                System.out.println("Debug7 " + stock.getNseSymbol());
                stockRepository.save(stock);
                System.out.println("Debug8 " + stock.getNseSymbol());
                --countTotal;
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
                System.out.println("An error occured while getting data " + stock.getNseSymbol());
            }
        }
    }

    public void processPriceUpdate() {
        List<Stock> stockList = stockRepository.findByActivityCompleted(false);
        int threadCount = Runtime.getRuntime().availableProcessors(); // Use CPU cores
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        AtomicInteger countTotal = new AtomicInteger(stockList.size());

        for (Stock stock : stockList) {
            executorService.submit(
                    () -> {
                        try {
                            processYearlyPriceUpdate(stock);
                            processQuarterlyPriceUpdate(stock);
                            processMonthlyPriceUpdate(stock);
                            processWeeklyPriceUpdate(stock);
                            processDailyPriceUpdate(stock);

                            stock.setActivityCompleted(true);
                            stockRepository.save(stock);

                            System.out.println("Remaining: " + countTotal.decrementAndGet());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        executorService.shutdown(); // No new tasks will be accepted
    }

    private void processYearlyPriceUpdate(Stock stock) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting yearly price update for " + stock.getNseSymbol());

        try {
            LocalDate initialDate = LocalDate.of(2019, 01, 01);

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
                                    stock.getIsinCode());

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

                    updatePriceService.updatePrice(Timeframe.YEARLY, stock, stockPrice);

                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = miscUtil.yearLastDay(from);

            } while (to.isBefore(LocalDate.of(2025, 05, 8)));

            // priceTemplate.create(Timeframe.YEARLY, stockPriceList);

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

    private void processQuarterlyPriceUpdate(Stock stock) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting quarterly activity for " + stock.getNseSymbol());

        try {

            LocalDate initialDate = LocalDate.of(2023, 01, 01);

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
                                    stock.getIsinCode());

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

                    updatePriceService.updatePrice(Timeframe.QUARTERLY, stock, stockPrice);
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = miscUtil.quarterLastDay(from);

            } while (to.isBefore(LocalDate.of(2025, 05, 8)));

            // priceTemplate.create(Timeframe.QUARTERLY, stockPriceList);

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

    private void processMonthlyPriceUpdate(Stock stock) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting monthly activity for " + stock.getNseSymbol());

        try {

            LocalDate initialDate = LocalDate.of(2024, 10, 01);

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
                                    stock.getIsinCode());

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

                    updatePriceService.updatePrice(Timeframe.MONTHLY, stock, stockPrice);
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.lastDayOfMonth());

            } while (to.isBefore(LocalDate.of(2025, 05, 8)));

            // priceTemplate.create(Timeframe.MONTHLY,stockPriceList);

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

    private void processWeeklyPriceUpdate(Stock stock) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting weekly activity for " + stock.getNseSymbol());

        try {

            LocalDate initialDate = LocalDate.of(2025, 01, 01);

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
                                    stock.getIsinCode());

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

                    updatePriceService.updatePrice(Timeframe.WEEKLY, stock, stockPrice);
                    stockPriceList.add(stockPrice);
                }

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            } while (to.isBefore(LocalDate.of(2025, 05, 8)));

            // priceTemplate.create(Timeframe.WEEKLY, stockPriceList);

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

    private void processDailyPriceUpdate(Stock stock) {
        /*

        long startTime = System.currentTimeMillis();
        System.out.println("Starting daily activity for " + stock.getNseSymbol());

        try {

            LocalDate initialDate = LocalDate.of(2025, 03, 17);

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
                                    stock.getIsinCode());

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

            } while (to.isBefore(LocalDate.of(2025, 05, 8)));

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
        }*/
    }

    public void processTechnicalsUpdate() {
        List<Stock> stockList = stockRepository.findByActivityCompleted(false);
        int threadCount = Runtime.getRuntime().availableProcessors(); // Use CPU cores
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        AtomicInteger countTotal = new AtomicInteger(stockList.size());

        for (Stock stock : stockList) {
            executorService.submit(
                    () -> {
                        try {

                            processMonthlyTechnicalsUpdate(stock);
                            processWeeklyTechnicalsUpdate(stock);
                            processDailyTechnicalsUpdate(stock);

                            stock.setActivityCompleted(true);
                            stockRepository.save(stock);

                            System.out.println("Remaining: " + countTotal.decrementAndGet());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        executorService.shutdown(); // No new tasks will be accepted
    }

    private void processMonthlyTechnicalsUpdate(Stock stock) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting monthly activity for " + stock.getNseSymbol());

        try {

            LocalDate initialDate = LocalDate.of(2024, 9, 01);

            LocalDate from = initialDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate to = from.with(TemporalAdjusters.lastDayOfMonth());

            com.example.data.storage.documents.StockTechnicals stockTechnicals = null;
            do {
                System.out.println("monthly from: " + from + " to: " + to);

                stockTechnicals = updateTechnicalsService.build(Timeframe.MONTHLY, stock, to);

                updateTechnicalsService.updateTechnicals(Timeframe.MONTHLY, stock, stockTechnicals);

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.lastDayOfMonth());

            } while (to.isBefore(LocalDate.now()));

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

    private void processWeeklyTechnicalsUpdate(Stock stock) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting weekly activity for " + stock.getNseSymbol());

        try {

            LocalDate initialDate = LocalDate.of(2025, 02, 07);

            LocalDate from = initialDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            com.example.data.storage.documents.StockTechnicals stockTechnicals = null;
            do {

                System.out.println("weekly from: " + from + " to: " + to);

                stockTechnicals = updateTechnicalsService.build(Timeframe.WEEKLY, stock, to);

                updateTechnicalsService.updateTechnicals(Timeframe.WEEKLY, stock, stockTechnicals);

                from = to.plusDays(1);
                to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            } while (to.isBefore(LocalDate.now()));

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

    private void processDailyTechnicalsUpdate(Stock stock) {

        long startTime = System.currentTimeMillis();
        System.out.println("Starting daily activity for " + stock.getNseSymbol());

        try {

            LocalDate initialDate = LocalDate.of(2025, 03, 13);

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

            } while (to.isBefore(LocalDate.now()));

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
