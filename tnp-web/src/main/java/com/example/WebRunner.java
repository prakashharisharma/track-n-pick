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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
    @Qualifier("omegaPriceActionSignalEvaluator")
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

    private static Map<String, StockPrice> stockPriceMap = new ConcurrentHashMap<>();
    private static Map<String, StockTechnicals> stockTechnicalsMap = new ConcurrentHashMap<>();

    @Override
    public void run(String... arg0) throws InterruptedException, IOException {

        // https://dhanhq.co/docs/v2/authentication/#access-token
        log.info("Application started....");

        //  bhavProcessor.processTechnicals();
        // bhavProcessor.processResearch();
        //  this.allocatePositions();
        this.scanMaster();

        // Stock stock = stockService.getStockByNseSymbol("HCLTECH");

        // StockPrice stockPrice = updatePriceService.buildBack(Timeframe.MONTHLY, stock,
        // miscUtil.currentDate());

        // StockTechnicals stockTechnicals  = updateTechnicalsService.buildBack(Timeframe.MONTHLY,
        // stock, miscUtil.currentDate());

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

    private void scanMaster() {
        List<StockAnalysis> monthlyAnalysis = new ArrayList<>();

        monthlyAnalysis.addAll(this.scanAll());

        System.out.println("MONTHLY Analyzed: ");
        System.out.println(
                "ScanDate,Symbol,Strategy,MarketCap,Close,CurrentClose,Entry,SL,DynamicSL,Risk,Target,Change%,isExitCandidate");

        List<Allocation> allocateFunds = null;
        /*
         allocateFunds  = this.allocateFunds(this.sortAndPrint(monthlyAnalysis), 1800000.0, 0.095);

        for (Allocation allocation : allocateFunds) {
            System.out.println(
                    allocation.getStock().getStock().getNseSymbol()
                            + " : ₹ "
                            + allocation.getAllocatedAmount());
        }
        */
        System.out.println("Fixed Allocation: ");
        System.out.println(
                "ScanDate,CloseDate,Symbol,Strategy,MarketCap,Close,CurrentClose,Entry,SL,DynamicSL,Risk,Target,Change%,isExitCandidate");

        // allocateFunds = this.allocateFundsFixed(this.sortAndPrint(monthlyAnalysis), 1800000.0,
        // 0.095);

        allocateFunds =
                this.allocateFundsByStrategyAndMarketCap(
                        this.sortAndPrint(monthlyAnalysis), 2_50_000.0, 0.50);

        allocateFunds.sort(Comparator.comparing(a -> a.getStock().getScanDate()));

        double totalInvested = 0;
        double totalCurrentValue = 0;
        System.out.println("\n=== ALLOCATION SUMMARY ===");

        // For per-scanDate aggregation
        Map<LocalDate, List<Allocation>> byScanDate =
                allocateFunds.stream()
                        .collect(
                                Collectors.groupingBy(
                                        a -> a.getStock().getScanDate(),
                                        TreeMap::new, // ensures dates are sorted
                                        Collectors.toList()));
        // For per-strategy aggregation
        Map<ResearchTechnical.Strategy, List<Allocation>> byStrategy =
                allocateFunds.stream()
                        .collect(
                                Collectors.groupingBy(
                                        a -> a.getStock().getStrategy(),
                                        TreeMap::new, // ensures dates are sorted
                                        Collectors.toList()));

        // For per-marketCap aggregation
        Map<MarketCapCategory, List<Allocation>> byMarketCap =
                allocateFunds.stream()
                        .collect(
                                Collectors.groupingBy(
                                        a -> a.getStock().getMarketCap(),
                                        TreeMap::new, // ensures dates are sorted
                                        Collectors.toList()));

        for (Allocation allocation : allocateFunds) {
            StockAnalysis stockAnalysis = allocation.getStock();
            double allocatedAmount = allocation.getAllocatedAmount();
            String symbol = stockAnalysis.getStock().getNseSymbol();
            double entryPrice = stockAnalysis.getEntryPrice();
            double currentPrice = stockAnalysis.getCurrentClose();
            double stopLoss = stockAnalysis.getStopLoss();

            totalInvested += allocatedAmount;
            totalCurrentValue += allocatedAmount * (Math.max(stopLoss, currentPrice) / entryPrice);

            double stockGainLossPercent =
                    ((Math.max(stopLoss, currentPrice) / entryPrice) - 1) * 100;

            System.out.println(
                    stockAnalysis.getScanDate()
                            + " | "
                            + symbol
                            + " | "
                            + stockAnalysis.getMarketCap()
                            + " | "
                            + stockAnalysis.getStrategy()
                            + " : ₹ "
                            + String.format("%.2f", allocatedAmount)
                            + " | Entry: ₹ "
                            + String.format("%.2f", entryPrice)
                            + " | Risk: "
                            + String.format("%.2f", stockAnalysis.getRisk())
                            + "%"
                            + " | Current: ₹ "
                            + String.format("%.2f", currentPrice)
                            + " | "
                            + stockAnalysis.getCurrentCloseDate()
                            + " | Gain/Loss: "
                            + String.format("%.2f", stockGainLossPercent)
                            + "%");
        }

        StringBuilder sbCSV = new StringBuilder();
        // ===== TOTAL PORTFOLIO SUMMARY =====
        double totalGainLoss = totalCurrentValue - totalInvested;
        double totalGainLossPercent = (totalGainLoss / totalInvested) * 100;

        sbCSV.append(String.format("%.2f", totalGainLossPercent) + "%");
        sbCSV.append(", ");

        System.out.println("\n=== PORTFOLIO SUMMARY ===");
        System.out.println("Total Invested: ₹ " + String.format("%.2f", totalInvested));
        System.out.println("Current Value: ₹ " + String.format("%.2f", totalCurrentValue));
        System.out.println("Total Gain/Loss: ₹ " + String.format("%.2f", totalGainLoss));
        System.out.println(
                "Total Gain/Loss %: " + String.format("%.2f", totalGainLossPercent) + "%");

        // ===== PER-STRATEGY SUMMARY =====
        System.out.println("\n=== STRATEGY-WISE SUMMARY ===");

        Map<ResearchTechnical.Strategy, String> strategyPerformanceMap = new HashMap<>();

        strategyPerformanceMap.put(ResearchTechnical.Strategy.ALPHA, "0.00%");
        strategyPerformanceMap.put(ResearchTechnical.Strategy.ULTIMA, "0.00%");
        strategyPerformanceMap.put(ResearchTechnical.Strategy.OMEGA, "0.00%");
        strategyPerformanceMap.put(ResearchTechnical.Strategy.GAMA, "0.00%");

        for (Map.Entry<ResearchTechnical.Strategy, List<Allocation>> entry :
                byStrategy.entrySet()) {
            ResearchTechnical.Strategy strategy = entry.getKey();
            List<Allocation> allocations = entry.getValue();

            double strategyInvested = 0;
            double strategyCurrentValue = 0;

            for (Allocation allocation : allocations) {
                StockAnalysis sa = allocation.getStock();
                double allocatedAmount = allocation.getAllocatedAmount();
                double entryPrice = sa.getEntryPrice();
                double currentPrice = sa.getCurrentClose();
                double stopLoss = sa.getStopLoss();

                strategyInvested += allocatedAmount;
                strategyCurrentValue +=
                        allocatedAmount * (Math.max(stopLoss, currentPrice) / entryPrice);
            }

            double strategyGainLoss = strategyCurrentValue - strategyInvested;
            double strategyGainLossPercent = (strategyGainLoss / strategyInvested) * 100;

            System.out.println(
                    strategy
                            + " | Invested: ₹ "
                            + String.format("%.2f", strategyInvested)
                            + " | Current: ₹ "
                            + String.format("%.2f", strategyCurrentValue)
                            + " | Gain/Loss: ₹ "
                            + String.format("%.2f", strategyGainLoss)
                            + " | "
                            + String.format("%.2f", strategyGainLossPercent)
                            + "%");
            strategyPerformanceMap.put(
                    strategy, String.format("%.2f", strategyGainLossPercent) + "%");
            // sbCSV.append(String.format("%.2f", strategyGainLossPercent) + "%");
            // sbCSV.append(", ");
        }
        sbCSV.append(strategyPerformanceMap.get(ResearchTechnical.Strategy.ALPHA));
        sbCSV.append(", ");
        sbCSV.append(strategyPerformanceMap.get(ResearchTechnical.Strategy.ULTIMA));
        sbCSV.append(", ");
        sbCSV.append(strategyPerformanceMap.get(ResearchTechnical.Strategy.OMEGA));
        sbCSV.append(", ");
        sbCSV.append(strategyPerformanceMap.get(ResearchTechnical.Strategy.GAMA));
        sbCSV.append(", ");

        // ===== PER-MARKETCAP SUMMARY =====
        System.out.println("\n=== MARKETCAP-WISE SUMMARY ===");

        Map<MarketCapCategory, String> marketCapPerformanceMap = new HashMap<>();

        marketCapPerformanceMap.put(MarketCapCategory.SMALLCAP, "0.00%");
        marketCapPerformanceMap.put(MarketCapCategory.MIDCAP, "0.00%");
        marketCapPerformanceMap.put(MarketCapCategory.LARGECAP, "0.00%");
        marketCapPerformanceMap.put(MarketCapCategory.MEGACAP, "0.00%");

        for (Map.Entry<MarketCapCategory, List<Allocation>> entry : byMarketCap.entrySet()) {
            MarketCapCategory marketCap = entry.getKey();
            List<Allocation> allocations = entry.getValue();

            double marketCapInvested = 0;
            double marketCapCurrentValue = 0;

            for (Allocation allocation : allocations) {
                StockAnalysis sa = allocation.getStock();
                double allocatedAmount = allocation.getAllocatedAmount();
                double entryPrice = sa.getEntryPrice();
                double currentPrice = sa.getCurrentClose();
                double stopLoss = sa.getStopLoss();

                marketCapInvested += allocatedAmount;
                marketCapCurrentValue +=
                        allocatedAmount * (Math.max(stopLoss, currentPrice) / entryPrice);
            }

            double marketCapGainLoss = marketCapCurrentValue - marketCapInvested;
            double marketCapGainLossPercent = (marketCapGainLoss / marketCapInvested) * 100;

            System.out.println(
                    marketCap
                            + " | Invested: ₹ "
                            + String.format("%.2f", marketCapInvested)
                            + " | Current: ₹ "
                            + String.format("%.2f", marketCapCurrentValue)
                            + " | Gain/Loss: ₹ "
                            + String.format("%.2f", marketCapGainLoss)
                            + " | "
                            + String.format("%.2f", marketCapGainLossPercent)
                            + "%");

            marketCapPerformanceMap.put(
                    marketCap, String.format("%.2f", marketCapGainLossPercent) + "%");
            // sbCSV.append(String.format("%.2f", marketCapGainLossPercent) + "%");
            // sbCSV.append(", ");
        }

        sbCSV.append(marketCapPerformanceMap.get(MarketCapCategory.SMALLCAP));
        sbCSV.append(", ");
        sbCSV.append(marketCapPerformanceMap.get(MarketCapCategory.MIDCAP));
        sbCSV.append(", ");
        sbCSV.append(marketCapPerformanceMap.get(MarketCapCategory.LARGECAP));
        sbCSV.append(", ");
        sbCSV.append(marketCapPerformanceMap.get(MarketCapCategory.MEGACAP));
        sbCSV.append(", ");
        // ===== PER-SCANDATE SUMMARY =====
        System.out.println("\n=== SCANDATE-WISE SUMMARY ===");

        Map<String, String> portfolioPerformanceMap = new HashMap<>();

        portfolioPerformanceMap.put("P1", "0.00%");
        portfolioPerformanceMap.put("P2", "0.00%");
        portfolioPerformanceMap.put("P3", "0.00%");

        for (Map.Entry<LocalDate, List<Allocation>> entry : byScanDate.entrySet()) {
            LocalDate scanDate = entry.getKey();
            List<Allocation> allocations = entry.getValue();

            double dateInvested = 0;
            double dateCurrentValue = 0;

            for (Allocation allocation : allocations) {
                StockAnalysis sa = allocation.getStock();
                double allocatedAmount = allocation.getAllocatedAmount();
                double entryPrice = sa.getEntryPrice();
                double currentPrice = sa.getCurrentClose();
                double stopLoss = sa.getStopLoss();

                dateInvested += allocatedAmount;
                dateCurrentValue +=
                        allocatedAmount * (Math.max(stopLoss, currentPrice) / entryPrice);
            }

            double dateGainLoss = dateCurrentValue - dateInvested;
            double dateGainLossPercent = (dateGainLoss / dateInvested) * 100;

            System.out.println(
                    scanDate
                            + " | Invested: ₹ "
                            + String.format("%.2f", dateInvested)
                            + " | Current: ₹ "
                            + String.format("%.2f", dateCurrentValue)
                            + " | Gain/Loss: ₹ "
                            + String.format("%.2f", dateGainLoss)
                            + " | "
                            + String.format("%.2f", dateGainLossPercent)
                            + "%");
            // sbCSV.append(String.format("%.2f", dateGainLossPercent) + "%");
            // sbCSV.append(", ");

            LocalDate lastSession =
                    calendarService.previousTradingSession(
                            miscUtil.currentDate().withDayOfMonth(1));

            LocalDate firstSession =
                    calendarService.nextTradingSession(miscUtil.previousMonthLastDay());

            LocalDate secondSession = calendarService.nextTradingSession(firstSession);

            if (scanDate.isEqual(lastSession)) {
                portfolioPerformanceMap.put("P1", String.format("%.2f", dateGainLossPercent) + "%");
            } else if (scanDate.isEqual(firstSession)) {
                portfolioPerformanceMap.put("P2", String.format("%.2f", dateGainLossPercent) + "%");
            } else if (scanDate.isEqual(secondSession)) {
                portfolioPerformanceMap.put("P3", String.format("%.2f", dateGainLossPercent) + "%");
            }
        }

        sbCSV.append(portfolioPerformanceMap.get("P1"));
        sbCSV.append(", ");
        sbCSV.append(portfolioPerformanceMap.get("P2"));
        sbCSV.append(", ");
        sbCSV.append(portfolioPerformanceMap.get("P3"));

        System.out.println(
                miscUtil.currentDate().format(DateTimeFormatter.ofPattern("MMM-yyyy"))
                        + ", "
                        + sbCSV);
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

    /**
     * Entry - 2025-10-01,HEMIPROP Research Date high and prev month High resistance
     * 2025-09-30,CENTURYPLY Entry Price should not resist on current month High
     *
     * <p>intraday - 10% or 5% of prevClose (if circuit is 5) sell half
     *
     * <p>Exit - (ema5 > ema20 > ema50) 2025-06-30,TRENT if Gap Down exit immediately (ema5 > ema20
     * > ema50) 2025-06-30,DEEPAKFERT If Breakdown Ema5 prev Red with avg incr and vol incr and vol
     * > avg exit immediately (ema5 > ema20 > ema50) 2025-06-30,PNB If Breakdown EMA5 with Prev Red
     * or PrevUpperWickDomiant after 15 Exit immediately (ema5 > ema20 > ema50) 2025-07-02,JKTYRE
     * engulfing red with breakdown ema5 after 15 exit immediately (ema5 > ema20 > ema50)
     * 2025-07-02,BHARATFORG if breakdown ema20 exit immediately on last SessionDay exit on
     * prevClose on 1st SessionDay exit on prevClose
     *
     * @return
     */
    /*
    private List<StockAnalysis> scanAll(){
       List<Stock> stocks = stockService.getActiveStocks();
      //  List<Stock> stocks = stockService.getForActivity();
        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        LocalDate sessionDate = calendarService.previousTradingSession(miscUtil.currentDate());

        for (Stock stock : stocks) {

            StockPrice stockPrice =
                    updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

            StockTechnicals stockTechnicals =
                    updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

            if (!this.isInititalValidated(stockPrice, stockTechnicals)) {
                continue;
            }
            if (!this.isMonthlySatisfied(stockPrice, stockTechnicals)) {
                continue;
            }
            stockAnalysed.addAll(alphaScannerLastDay(stockPrice, stockTechnicals));
            stockAnalysed.addAll(omegaScannerLastDay(stockPrice, stockTechnicals));
            stockAnalysed.addAll(ultimaScannerLastDay(stockPrice, stockTechnicals));
            stockAnalysed.addAll(alphaScanner(stockPrice, stockTechnicals));
            stockAnalysed.addAll(omegaScanner(stockPrice, stockTechnicals));
            stockAnalysed.addAll(ultimaScanner(stockPrice, stockTechnicals));

        }

        return stockAnalysed;
    }*/

    private List<StockAnalysis> scanAll() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            List<Stock> stocks = stockService.getActiveStocks();
            // List<Stock> stocks = stockService.getForActivity();
            List<StockAnalysis> stockAnalysed = Collections.synchronizedList(new ArrayList<>());

            LocalDate sessionDate = calendarService.previousTradingSession(miscUtil.currentDate());

            // Process all stocks in parallel
            List<CompletableFuture<Void>> stockFutures =
                    stocks.stream()
                            .map(
                                    stock ->
                                            CompletableFuture.runAsync(
                                                    () ->
                                                            processStock(
                                                                    stock,
                                                                    sessionDate,
                                                                    stockAnalysed),
                                                    executor))
                            .collect(Collectors.toList());

            // Wait for all stock processing to complete
            CompletableFuture.allOf(stockFutures.toArray(new CompletableFuture[0])).join();

            return stockAnalysed;

        } finally {
            executor.shutdown();
        }
    }

    private void processStock(
            Stock stock, LocalDate sessionDate, List<StockAnalysis> stockAnalysed) {
        try {
            StockPrice stockPrice =
                    updatePriceService.buildBack(Timeframe.MONTHLY, stock, sessionDate);
            StockTechnicals stockTechnicals =
                    updateTechnicalsService.buildBack(Timeframe.MONTHLY, stock, sessionDate);

            if (!this.isInititalValidated(stockPrice, stockTechnicals)) return;
            if (!this.isMonthlySatisfied(stockPrice, stockTechnicals)) return;

            // Execute all scanners in parallel
            /*
            List<CompletableFuture<List<StockAnalysis>>> scannerFutures =
                    Arrays.asList(
                            CompletableFuture.supplyAsync(
                                    () -> gamaScannerLastDay(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> gamaScanner(stockPrice, stockTechnicals))
                            );*/

            List<CompletableFuture<List<StockAnalysis>>> scannerFutures =
                    Arrays.asList(
                            CompletableFuture.supplyAsync(
                                    () -> alphaScannerLastDay(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> ultimaScannerLastDay(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> omegaScannerLastDay(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> gamaScannerLastDay(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> alphaScanner(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> ultimaScanner(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> omegaScanner(stockPrice, stockTechnicals)),
                            CompletableFuture.supplyAsync(
                                    () -> gamaScanner(stockPrice, stockTechnicals)));

            // Collect results as they complete
            CompletableFuture.allOf(scannerFutures.toArray(new CompletableFuture[0]))
                    .thenRun(
                            () -> {
                                for (CompletableFuture<List<StockAnalysis>> future :
                                        scannerFutures) {
                                    try {
                                        stockAnalysed.addAll(future.join());
                                    } catch (Exception e) {
                                        System.err.println(
                                                "Scanner execution failed: " + e.getMessage());
                                    }
                                }
                            })
                    .join(); // Wait for all scanners for this stock to complete

        } catch (Exception e) {
            System.err.println("Stock processing failed for " + stock + ": " + e.getMessage());
        }
    }

    /** 1. close above prev high 2. Close above ema5 and ema20 3. volume above 12 months average */
    private List<StockAnalysis> alphaScannerLastDay(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        LocalDate sessionDate =
                calendarService.previousTradingSession(miscUtil.currentDate().withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = miscUtil.currentDate();
        }

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        double close = stockPrice.getClose();
        if (close > stockPrice.getPrevHigh()) {
            double ema5 = stockTechnicals.getEma5();

            boolean isCloseBelowEma5OREma5Decreasing =
                    (close < ema5 && CandleStickUtils.isRed(stockPrice))
                            || (stockTechnicals.getPrevEma5() != null
                                    && ema5 < stockTechnicals.getPrevEma5());

            if (!isCloseBelowEma5OREma5Decreasing) {
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();

                if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    if (close > ema20 && close > ema50) {
                        long volumeAvg = stockTechnicals.getVolumeAvg10();
                        long volume = stockTechnicals.getVolume();
                        if (volume > volumeAvg * 1.5) {

                            // Daily check

                            StockPrice stockPriceDaily =
                                    getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                            StockTechnicals stockTechnicalsDaily =
                                    getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);

                            Optional<StockAnalysis> stockAnalysisOptional =
                                    isDailyEntrySatisFied(
                                            stockPrice,
                                            stockPriceDaily,
                                            stockTechnicalsDaily,
                                            sessionDate,
                                            ResearchTechnical.Strategy.ALPHA);
                            if (stockAnalysisOptional.isPresent()) {
                                stockAnalysed.add(stockAnalysisOptional.get());
                            }
                        }
                    }
                }
            }
        }

        return stockAnalysed;
    }

    private List<StockAnalysis> gamaScannerLastDay(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        LocalDate sessionDate =
                calendarService.previousTradingSession(miscUtil.currentDate().withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = miscUtil.currentDate();
        }

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        boolean isHigherLow = CandleStickUtils.isHigherLow(stockPrice);

        boolean isLowerHigh = CandleStickUtils.isLowerHigh(stockPrice);
        double close = stockPrice.getClose();
        if (CandleStickUtils.isGreen(stockPrice)
                && close < stockPrice.getPrevOpen()
                && isHigherLow
                && isLowerHigh) {

            // System.out.println(" Here1 " + stock.getNseSymbol());

            double ema5 = stockTechnicals.getEma5();
            double ema20 = stockTechnicals.getEma20();
            double ema50 = stockTechnicals.getEma50();
            double ema100 =
                    MovingAverageUtil.getMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double ema200 =
                    MovingAverageUtil.getMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma5 = stockTechnicals.getPrevEma5();
            double prevEma20 = stockTechnicals.getPrevEma20();
            double prevEma50 = stockTechnicals.getPrevEma50();
            double prevEma100 =
                    MovingAverageUtil.getPrevMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma200 =
                    MovingAverageUtil.getPrevMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);

            double prevEma5Weighted = formulaService.applyPercentChange(prevEma5, 2.0);
            double prevEma20Weighted = formulaService.applyPercentChange(prevEma20, 2.0);
            double prevEma50Weighted = formulaService.applyPercentChange(prevEma50, 2.0);
            double prevEma100Weighted = formulaService.applyPercentChange(prevEma100, 2.0);
            double prevEma200Weighted = formulaService.applyPercentChange(prevEma200, 2.0);

            double prevEma5WeightedNegative = formulaService.applyPercentChange(prevEma5, -2.0);
            double prevEma20WeightedNegative = formulaService.applyPercentChange(prevEma20, -2.0);
            double prevEma50WeightedNegative = formulaService.applyPercentChange(prevEma50, -2.0);
            double prevEma100WeightedNegative = formulaService.applyPercentChange(prevEma100, -2.0);
            double prevEma200WeightedNegative = formulaService.applyPercentChange(prevEma200, -2.0);

            double prevOpen = stockPrice.getPrevOpen();
            double prevLow = stockPrice.getPrevLow();
            double prevClose = stockPrice.getPrevClose();

            boolean isLowRejectedEma5 =
                    (prevOpen > prevEma5
                                    && prevLow <= prevEma5Weighted
                                    && prevClose > prevEma5WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema5;

            boolean isLowRejectedEma20 =
                    (prevOpen > prevEma20
                                    && prevLow <= prevEma20Weighted
                                    && prevClose > prevEma20WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema20;

            boolean isLowRejectedEma50 =
                    (prevOpen > prevEma50
                                    && prevLow <= prevEma50Weighted
                                    && prevClose > prevEma50WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema50;

            boolean isLowRejectedEma100 =
                    (prevOpen > prevEma100
                                    && prevLow <= prevEma100Weighted
                                    && prevClose > prevEma100WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema100;

            boolean isLowRejectedEma200 =
                    (prevOpen > prevEma200
                                    && prevLow <= prevEma200Weighted
                                    && prevClose > prevEma200WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema200;

            if (((isLowRejectedEma5 || isLowRejectedEma20 || isLowRejectedEma50)
                            && (ema20 > prevEma20 && ema50 > prevEma50))
                    || ((isLowRejectedEma100 || isLowRejectedEma200)
                            && (ema50 > prevEma50 && ema200 > prevEma200))) {

                boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice);
                boolean isPrevDoji = CandleStickUtils.isPrevVerySmallBody(stockPrice);
                // boolean isUpperWickDominant = CandleStickUtils.isUpperWickDominant(stockPrice);

                if (!isDoji && !isPrevDoji) {
                    // if(ema20 > prevEma20 && ema50 > prevEma50){

                    // System.out.println(" Here2 " + stock.getNseSymbol());
                    // if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    //  if (close > ema20 && close > ema50) {
                    // long volumeAvg = stockTechnicals.getVolumeAvg10();
                    // long volume = stockTechnicals.getVolume();
                    //  if (volume > volumeAvg * 1.5) {

                    // Daily check

                    StockPrice stockPriceDaily =
                            getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                    StockTechnicals stockTechnicalsDaily =
                            getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);

                    Optional<StockAnalysis> stockAnalysisOptional =
                            isDailyEntrySatisFied(
                                    stockPrice,
                                    stockPriceDaily,
                                    stockTechnicalsDaily,
                                    sessionDate,
                                    ResearchTechnical.Strategy.GAMA);
                    if (stockAnalysisOptional.isPresent()) {
                        stockAnalysed.add(stockAnalysisOptional.get());
                    }
                }
                //  }
                // }
                // }
                // }
            }
        }

        return stockAnalysed;
    }

    private List<StockAnalysis> gamaScanner(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        boolean isHigherLow = CandleStickUtils.isHigherLow(stockPrice);
        boolean isLowerHigh = CandleStickUtils.isLowerHigh(stockPrice);
        double close = stockPrice.getClose();
        if (CandleStickUtils.isGreen(stockPrice)
                && close < stockPrice.getPrevOpen()
                && isHigherLow
                && isLowerHigh) {

            //  System.out.println(" Here1 " + stock.getNseSymbol());

            double ema5 = stockTechnicals.getEma5();
            double ema20 = stockTechnicals.getEma20();
            double ema50 = stockTechnicals.getEma50();
            double ema100 =
                    MovingAverageUtil.getMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double ema200 =
                    MovingAverageUtil.getMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma5 = stockTechnicals.getPrevEma5();
            double prevEma20 = stockTechnicals.getPrevEma20();
            double prevEma50 = stockTechnicals.getPrevEma50();
            double prevEma100 =
                    MovingAverageUtil.getPrevMovingAverage100(
                            stockTechnicals.getTimeframe(), stockTechnicals);
            double prevEma200 =
                    MovingAverageUtil.getPrevMovingAverage200(
                            stockTechnicals.getTimeframe(), stockTechnicals);

            double prevEma5Weighted = formulaService.applyPercentChange(prevEma5, 2.0);
            double prevEma20Weighted = formulaService.applyPercentChange(prevEma20, 2.0);
            double prevEma50Weighted = formulaService.applyPercentChange(prevEma50, 2.0);
            double prevEma100Weighted = formulaService.applyPercentChange(prevEma100, 2.0);
            double prevEma200Weighted = formulaService.applyPercentChange(prevEma200, 2.0);

            double prevEma5WeightedNegative = formulaService.applyPercentChange(prevEma5, -2.0);
            double prevEma20WeightedNegative = formulaService.applyPercentChange(prevEma20, -2.0);
            double prevEma50WeightedNegative = formulaService.applyPercentChange(prevEma50, -2.0);
            double prevEma100WeightedNegative = formulaService.applyPercentChange(prevEma100, -2.0);
            double prevEma200WeightedNegative = formulaService.applyPercentChange(prevEma200, -2.0);

            double prevOpen = stockPrice.getPrevOpen();
            double prevLow = stockPrice.getPrevLow();
            double prevClose = stockPrice.getPrevClose();

            boolean isLowRejectedEma5 =
                    (prevOpen > prevEma5
                                    && prevLow <= prevEma5Weighted
                                    && prevClose > prevEma5WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema5;

            boolean isLowRejectedEma20 =
                    (prevOpen > prevEma20
                                    && prevLow <= prevEma20Weighted
                                    && prevClose > prevEma20WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema20;

            boolean isLowRejectedEma50 =
                    (prevOpen > prevEma50
                                    && prevLow <= prevEma50Weighted
                                    && prevClose > prevEma50WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema50;

            boolean isLowRejectedEma100 =
                    (prevOpen > prevEma100
                                    && prevLow <= prevEma100Weighted
                                    && prevClose > prevEma100WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema100;

            boolean isLowRejectedEma200 =
                    (prevOpen > prevEma200
                                    && prevLow <= prevEma200Weighted
                                    && prevClose > prevEma200WeightedNegative)
                            && CandleStickUtils.isPrevSessionRed(stockPrice)
                            && close > ema200;

            if (((isLowRejectedEma5 || isLowRejectedEma20 || isLowRejectedEma50)
                            && (ema20 > prevEma20 && ema50 > prevEma50))
                    || ((isLowRejectedEma100 || isLowRejectedEma200)
                            && (ema50 > prevEma50 && ema200 > prevEma200))) {

                boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice);
                boolean isPrevDoji = CandleStickUtils.isPrevVerySmallBody(stockPrice);

                // boolean isUpperWickDominant = CandleStickUtils.isUpperWickDominant(stockPrice);
                // boolean isUpperWickDominant = false;
                if (!isDoji && !isPrevDoji) {
                    // System.out.println(" Here2 " + stock.getNseSymbol());

                    // if(ema20 > prevEma20 && ema50 > prevEma50) {

                    // if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    //  if (close > ema20 && close > ema50) {
                    long volumeAvg = stockTechnicals.getVolumeAvg10();
                    long volume = stockTechnicals.getVolume();
                    //  if (volume > volumeAvg * 1.5) {

                    // Daily check
                    LocalDate currentMonthFirstSession =
                            calendarService.nextTradingSession(miscUtil.previousMonthLastDay());

                    LocalDate currentMonthSecondSession =
                            calendarService.nextTradingSession(currentMonthFirstSession);

                    LocalDate currentMonthThirdSession =
                            calendarService.nextTradingSession(currentMonthSecondSession);
                    LocalDate sessionDate = currentMonthFirstSession;
                    LocalDate sessionDateTill = currentMonthThirdSession;
                    LocalDate ohlcvFrom = currentMonthFirstSession;

                    while (sessionDate.isBefore(sessionDateTill)) {

                        OHLCV curentMonthOlcv =
                                monthlySupportResistanceService.supportAndResistance(
                                        stock.getNseSymbol(), ohlcvFrom, sessionDate);

                        boolean interactsWithHigherTimeframe =
                                (stockPrice.getClose() >= curentMonthOlcv.getOpen())
                                        || (stockPrice.getClose() >= curentMonthOlcv.getLow())
                                        || (curentMonthOlcv.getLow() >= stockPrice.getLow());

                        if (interactsWithHigherTimeframe) {

                            StockPrice stockPriceDaily =
                                    getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                            if (stockPriceDaily.getClose() > stockPrice.getClose()) {

                                StockTechnicals stockTechnicalsDaily =
                                        getStockTechnicalsFromMap(
                                                Timeframe.DAILY, stock, sessionDate);

                                Optional<StockAnalysis> stockAnalysisOptional =
                                        isDailyEntrySatisFied(
                                                stockPrice,
                                                stockPriceDaily,
                                                stockTechnicalsDaily,
                                                sessionDate,
                                                ResearchTechnical.Strategy.GAMA);

                                if (stockAnalysisOptional.isPresent()) {
                                    stockAnalysed.add(stockAnalysisOptional.get());
                                    break;
                                }
                            }
                        }

                        sessionDate = calendarService.nextTradingSession(sessionDate);
                    }
                }
                // }
            }
        }

        return stockAnalysed;
    }

    private List<StockAnalysis> alphaScanner(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();

        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        // LocalDate sessionDate = calendarService.previousTradingSession(miscUtil.currentDate());

        /**
         * 1. close above prev high 1. Close above ema5 and ema20 3. volume above 12 months average
         */
        double close = stockPrice.getClose();
        if (close > stockPrice.getPrevHigh()) {
            double ema5 = stockTechnicals.getEma5();

            boolean isCloseBelowEma5OREma5Decreasing =
                    (close < ema5 && CandleStickUtils.isRed(stockPrice))
                            || (stockTechnicals.getPrevEma5() != null
                                    && ema5 < stockTechnicals.getPrevEma5());

            if (!isCloseBelowEma5OREma5Decreasing) {
                double ema20 = stockTechnicals.getEma20();
                double ema50 = stockTechnicals.getEma50();

                if (ema20 != 0.0 && ema50 != 0.0 && ema20 > ema50) {
                    if (close > ema20 && close > ema50) {
                        long volumeAvg = stockTechnicals.getVolumeAvg10();
                        long volume = stockTechnicals.getVolume();
                        if (volume > volumeAvg * 1.5) {

                            // Daily check
                            LocalDate currentMonthFirstSession =
                                    calendarService.nextTradingSession(
                                            miscUtil.previousMonthLastDay());

                            LocalDate currentMonthSecondSession =
                                    calendarService.nextTradingSession(currentMonthFirstSession);

                            LocalDate currentMonthThirdSession =
                                    calendarService.nextTradingSession(currentMonthSecondSession);
                            LocalDate sessionDate = currentMonthFirstSession;
                            LocalDate sessionDateTill = currentMonthThirdSession;
                            LocalDate ohlcvFrom = currentMonthFirstSession;

                            while (sessionDate.isBefore(sessionDateTill)) {

                                OHLCV curentMonthOlcv =
                                        monthlySupportResistanceService.supportAndResistance(
                                                stock.getNseSymbol(), ohlcvFrom, sessionDate);

                                boolean interactsWithHigherTimeframe =
                                        (stockPrice.getClose() >= curentMonthOlcv.getOpen())
                                                || (stockPrice.getClose()
                                                        >= curentMonthOlcv.getLow())
                                                || (curentMonthOlcv.getLow()
                                                        >= stockPrice.getLow());

                                if (interactsWithHigherTimeframe) {

                                    StockPrice stockPriceDaily =
                                            getStockPriceFromMap(
                                                    Timeframe.DAILY, stock, sessionDate);

                                    if (stockPriceDaily.getClose() > stockPrice.getClose()) {

                                        StockTechnicals stockTechnicalsDaily =
                                                getStockTechnicalsFromMap(
                                                        Timeframe.DAILY, stock, sessionDate);

                                        Optional<StockAnalysis> stockAnalysisOptional =
                                                isDailyEntrySatisFied(
                                                        stockPrice,
                                                        stockPriceDaily,
                                                        stockTechnicalsDaily,
                                                        sessionDate,
                                                        ResearchTechnical.Strategy.ALPHA);

                                        if (stockAnalysisOptional.isPresent()) {
                                            stockAnalysed.add(stockAnalysisOptional.get());
                                            break;
                                        }
                                    }
                                }

                                sessionDate = calendarService.nextTradingSession(sessionDate);
                            }
                        }
                    }
                }
            }
        }

        return stockAnalysed;
    }

    private List<StockAnalysis> ultimaScannerLastDay(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();
        //  List<Stock> stocks = stockService.getForActivity();
        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        LocalDate sessionDate =
                calendarService.previousTradingSession(miscUtil.currentDate().withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = miscUtil.currentDate();
        }
        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double close = stockPrice.getClose();

        double ema5Weighted = formulaService.applyPercentChange(ema5, 2.0);
        double ema5WeightedNegative = formulaService.applyPercentChange(ema5, -2.0);

        double ema20Weighted = formulaService.applyPercentChange(ema20, 2.0);
        double ema20WeightedNegative = formulaService.applyPercentChange(ema20, -2.0);

        double ema50Weighted = formulaService.applyPercentChange(ema50, 2.0);
        double ema50WeightedNegative = formulaService.applyPercentChange(ema50, -2.0);

        boolean isLowRejectedEma5 =
                (open > ema5 && low <= ema5Weighted && close > ema5WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma20 =
                (open > ema20 && low <= ema20Weighted && close > ema20WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma50 =
                (open > ema50 && low <= ema50Weighted && close > ema50WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);
        boolean isEma5Above20 = ema5 > ema20;
        boolean isEma20Above50 = ema20 > ema50;
        boolean isEma50Above200 = ema50 > ema200;
        boolean isEma20And50Increasing = ema20 > prevEma20 && ema50 > prevEma50;
        boolean isEma5And20Increasing = ema5 > prevEma5 && ema20 > prevEma20;
        boolean isEma50And200Increasing = ema50 > prevEma50 && ema200 > prevEma200;
        // Monthly Align Bullish
        if ((isLowRejectedEma5 && isEma5Above20 && isEma5And20Increasing)
                || (isLowRejectedEma20 && isEma20Above50 && isEma20And50Increasing)
                || (isLowRejectedEma50 && isEma50Above200 && isEma50And200Increasing)) {

            if (stockTechnicals.getVolume() < stockTechnicals.getVolumeAvg20()) {
                // Daily check

                StockPrice stockPriceDaily =
                        getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                StockTechnicals stockTechnicalsDaily =
                        getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);

                Optional<StockAnalysis> stockAnalysisOptional =
                        isDailyEntrySatisFied(
                                stockPrice,
                                stockPriceDaily,
                                stockTechnicalsDaily,
                                sessionDate,
                                ResearchTechnical.Strategy.ULTIMA);
                if (stockAnalysisOptional.isPresent()) {
                    stockAnalysed.add(stockAnalysisOptional.get());
                }
            }
            //       }
            // }
            // }
        }

        return stockAnalysed;
    }

    private List<StockAnalysis> omegaScannerLastDay(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();
        // List<Stock> stocks = stockService.getForActivity();
        List<StockAnalysis> stockAnalysed = new ArrayList<>();
        LocalDate sessionDate =
                calendarService.previousTradingSession(miscUtil.currentDate().withDayOfMonth(1));

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())
                && LocalTime.now().isAfter(LocalTime.of(15, 30, 00))) {
            sessionDate = miscUtil.currentDate();
        }
        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double close = stockPrice.getClose();

        boolean isLowRejected =
                (open > ema5 && low < ema5 && close > ema5)
                        || (open > ema20 && low < ema20 && close > ema20);

        boolean isPrevHigherHighAndHigherLow =
                CandleStickUtils.isPrevHigherHigh(stockPrice)
                        && CandleStickUtils.isPrevHigherLow(stockPrice);

        // Monthly Align Bullish
        if (isPrevHigherHighAndHigherLow && CandleStickUtils.isRed(stockPrice)) {

            // Monthly closed above ema5
            if (close > ema20 && ema5 > ema20) {

                // Monthly REd and prev Green
                if (isLowRejected
                        && (CandleStickUtils.isPrevSessionGreen(stockPrice)
                                || CandleStickUtils.isPrev2SessionGreen(stockPrice)
                                || CandleStickUtils.isPrev3SessionGreen(stockPrice))) {

                    if (MovingAverageUtil.increasingMaCount(stockTechnicals) >= 5) {

                        StockPrice stockPriceDaily =
                                getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                        StockTechnicals stockTechnicalsDaily =
                                getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);

                        Optional<StockAnalysis> stockAnalysisOptional =
                                isDailyEntrySatisFied(
                                        stockPrice,
                                        stockPriceDaily,
                                        stockTechnicalsDaily,
                                        sessionDate,
                                        ResearchTechnical.Strategy.OMEGA);
                        if (stockAnalysisOptional.isPresent()) {
                            stockAnalysed.add(stockAnalysisOptional.get());
                        }
                    }
                }
            }
        }

        return stockAnalysed;
    }

    private List<StockAnalysis> omegaScanner(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();
        // List<Stock> stocks = stockService.getForActivity();
        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double close = stockPrice.getClose();

        boolean isLowRejected =
                (open > ema5 && low < ema5 && close > ema5)
                        || (open > ema20 && low < ema20 && close > ema20);

        boolean isPrevHigherHighAndHigherLow =
                CandleStickUtils.isPrevHigherHigh(stockPrice)
                        && CandleStickUtils.isPrevHigherLow(stockPrice);

        // Monthly Align Bullish
        if (isPrevHigherHighAndHigherLow && CandleStickUtils.isRed(stockPrice)) {

            // Monthly closed above ema5
            if (close > ema20) {

                // Monthly REd and prev Green
                if (isLowRejected
                        && (CandleStickUtils.isPrevSessionGreen(stockPrice)
                                || CandleStickUtils.isPrev2SessionGreen(stockPrice)
                                || CandleStickUtils.isPrev3SessionGreen(stockPrice))) {

                    if (MovingAverageUtil.increasingMaCount(stockTechnicals) >= 5) {

                        LocalDate currentMonthFirstSession =
                                calendarService.nextTradingSession(miscUtil.previousMonthLastDay());

                        LocalDate currentMonthSecondSession =
                                calendarService.nextTradingSession(currentMonthFirstSession);

                        LocalDate currentMonthThirdSession =
                                calendarService.nextTradingSession(currentMonthSecondSession);
                        LocalDate sessionDate = currentMonthFirstSession;
                        LocalDate sessionDateTill = currentMonthThirdSession;
                        LocalDate ohlcvFrom = currentMonthFirstSession;

                        while (sessionDate.isBefore(sessionDateTill)) {

                            OHLCV curentMonthOlcv =
                                    monthlySupportResistanceService.supportAndResistance(
                                            stock.getNseSymbol(), ohlcvFrom, sessionDate);

                            boolean interactsWithHigherTimeframe =
                                    (stockPrice.getClose() >= curentMonthOlcv.getOpen())
                                            || (stockPrice.getClose() >= curentMonthOlcv.getLow())
                                            || (curentMonthOlcv.getLow() >= stockPrice.getLow());

                            if (interactsWithHigherTimeframe) {

                                StockPrice stockPriceDaily =
                                        getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                                if (stockPriceDaily.getClose() > stockPrice.getClose()) {

                                    StockTechnicals stockTechnicalsDaily =
                                            getStockTechnicalsFromMap(
                                                    Timeframe.DAILY, stock, sessionDate);

                                    Optional<StockAnalysis> stockAnalysisOptional =
                                            isDailyEntrySatisFied(
                                                    stockPrice,
                                                    stockPriceDaily,
                                                    stockTechnicalsDaily,
                                                    sessionDate,
                                                    ResearchTechnical.Strategy.OMEGA);

                                    if (stockAnalysisOptional.isPresent()) {
                                        stockAnalysed.add(stockAnalysisOptional.get());
                                        break;
                                    }
                                }
                            }

                            sessionDate = calendarService.nextTradingSession(sessionDate);
                        }
                    }
                }
            }
        }

        return stockAnalysed;
    }

    /**
     * 1. ema20 and ema50 increasing 2. Candle Red 3. open and close above ema20 4. ema20 > ema50 5.
     * low should be < ema20 + 2 % 6. volume should be < avg 7. first candle should be close >
     * monthly and HHHL
     */
    // remove smallcap
    private List<StockAnalysis> ultimaScanner(
            StockPrice stockPrice, StockTechnicals stockTechnicals) {

        Stock stock = stockPrice.getStock();
        //  List<Stock> stocks = stockService.getForActivity();
        List<StockAnalysis> stockAnalysed = new ArrayList<>();

        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double ema200 =
                MovingAverageUtil.getMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double prevEma5 = stockTechnicals.getPrevEma5();
        double prevEma20 = stockTechnicals.getPrevEma20();
        double prevEma50 = stockTechnicals.getPrevEma50();
        double prevEma200 =
                MovingAverageUtil.getPrevMovingAverage200(
                        stockTechnicals.getTimeframe(), stockTechnicals);
        double close = stockPrice.getClose();

        double ema5Weighted = formulaService.applyPercentChange(ema5, 2.0);
        double ema5WeightedNegative = formulaService.applyPercentChange(ema5, -2.0);

        double ema20Weighted = formulaService.applyPercentChange(ema20, 2.0);
        double ema20WeightedNegative = formulaService.applyPercentChange(ema20, -2.0);

        double ema50Weighted = formulaService.applyPercentChange(ema50, 2.0);
        double ema50WeightedNegative = formulaService.applyPercentChange(ema50, -2.0);

        boolean isLowRejectedEma5 =
                (open > ema5 && low <= ema5Weighted && close > ema5WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma20 =
                (open > ema20 && low <= ema20Weighted && close > ema20WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);

        boolean isLowRejectedEma50 =
                (open > ema50 && low <= ema50Weighted && close > ema50WeightedNegative)
                        && CandleStickUtils.isRed(stockPrice);
        boolean isEma5Above20 = ema5 > ema20;
        boolean isEma20Above50 = ema20 > ema50;
        boolean isEma50Above200 = ema50 > ema200;
        boolean isEma20And50Increasing = ema20 > prevEma20 && ema50 > prevEma50;
        boolean isEma5And20Increasing = ema5 > prevEma5 && ema20 > prevEma20;
        boolean isEma50And200Increasing = ema50 > prevEma50 && ema200 > prevEma200;
        // Monthly Align Bullish
        if ((isLowRejectedEma5 && isEma5Above20 && isEma5And20Increasing)
                || (isLowRejectedEma20 && isEma20Above50 && isEma20And50Increasing)
                || (isLowRejectedEma50 && isEma50Above200 && isEma50And200Increasing)) {

            // Monthly closed above ema5
            // if (ema20 > ema50) {

            //   if (ema20 > stockTechnicals.getPrevEma20()) {
            //     if (ema50 > stockTechnicals.getPrevEma50()) {

            if (stockTechnicals.getVolume() < stockTechnicals.getVolumeAvg20()) {

                LocalDate currentMonthFirstSession =
                        calendarService.nextTradingSession(miscUtil.previousMonthLastDay());

                LocalDate currentMonthSecondSession =
                        calendarService.nextTradingSession(currentMonthFirstSession);

                LocalDate currentMonthThirdSession =
                        calendarService.nextTradingSession(currentMonthSecondSession);
                LocalDate sessionDate = currentMonthFirstSession;
                LocalDate sessionDateTill = currentMonthThirdSession;

                while (sessionDate.isBefore(sessionDateTill)) {

                    StockPrice stockPriceDaily =
                            getStockPriceFromMap(Timeframe.DAILY, stock, sessionDate);

                    if (CandleStickUtils.isHigherHigh(stockPriceDaily)
                            && CandleStickUtils.isHigherLow(stockPriceDaily)) {

                        if (stockPriceDaily.getClose() > stockPrice.getClose()) {

                            StockTechnicals stockTechnicalsDaily =
                                    getStockTechnicalsFromMap(Timeframe.DAILY, stock, sessionDate);

                            Optional<StockAnalysis> stockAnalysisOptional =
                                    isDailyEntrySatisFied(
                                            stockPrice,
                                            stockPriceDaily,
                                            stockTechnicalsDaily,
                                            sessionDate,
                                            ResearchTechnical.Strategy.ULTIMA);

                            if (stockAnalysisOptional.isPresent()) {
                                stockAnalysed.add(stockAnalysisOptional.get());
                                break;
                            }
                        }
                    }

                    sessionDate = calendarService.nextTradingSession(sessionDate);
                }
                //      }
                //    }
                //  }
            }
        }

        return stockAnalysed;
    }

    private StockPrice getStockPriceFromMap(
            Timeframe timeframe, Stock stock, LocalDate sessionDate) {
        String key = timeframe + "-" + stock.getNseSymbol() + "-" + sessionDate;

        StockPrice stockPrice = stockPriceMap.get(key);

        if (stockPrice != null) {
            return stockPrice;
        }

        StockPrice stockPriceDaily =
                updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
        stockPriceMap.put(key, stockPriceDaily);

        return stockPriceDaily;
    }

    private StockTechnicals getStockTechnicalsFromMap(
            Timeframe timeframe, Stock stock, LocalDate sessionDate) {
        String key = timeframe + "-" + stock.getNseSymbol() + "-" + sessionDate;

        StockTechnicals stockTechnicals = stockTechnicalsMap.get(key);

        if (stockTechnicals != null) {
            return stockTechnicals;
        }

        StockTechnicals stockTechnicalsDaily =
                updateTechnicalsService.buildBack(Timeframe.DAILY, stock, sessionDate);

        stockTechnicalsMap.put(key, stockTechnicalsDaily);

        return stockTechnicalsDaily;
    }

    private boolean isMonthlySatisfied(StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double ema5 = stockTechnicals.getEma5();
        double ema20 = stockTechnicals.getEma20();

        boolean isDoji = CandleStickUtils.isVerySmallBody(stockPrice) && (ema5 < ema20);
        if (!isDoji) {
            boolean isUpperWickDominant =
                    CandleStickUtils.isUpperWickDominant(stockPrice)
                            && stockPrice.getOpen() > ema5
                            && stockPrice.getClose() > ema5;
            if (!isUpperWickDominant) {
                double prevEma5 =
                        stockTechnicals.getPrevEma5() != null ? stockTechnicals.getPrevEma5() : 0.0;
                boolean isPrevUpperWickDominant =
                        CandleStickUtils.isPrevUpperWickDominant(stockPrice)
                                && stockPrice.getPrevOpen() > prevEma5
                                && stockPrice.getPrevClose() > prevEma5;
                boolean isAllMaAlignedBullish =
                        MovingAverageUtil.isAllMaAlignedBullish(
                                stockPrice.getTimeframe(), stockTechnicals);
                boolean isHangingMan =
                        stockPrice.getClose() > ema5
                                && CandleStickUtils.isLowerWickDominant(stockPrice)
                                && isAllMaAlignedBullish;
                // if (!isHangingMan) {
                boolean isEngulfing =
                        stockPrice.getOpen() >= stockPrice.getPrevClose()
                                && stockPrice.getClose() < stockPrice.getPrevOpen()
                                && CandleStickUtils.isRed(stockPrice)
                                && CandleStickUtils.isPrevSessionGreen(stockPrice)
                                && isAllMaAlignedBullish;
                // if (!isEngulfing) {
                if (!isPrevUpperWickDominant) {

                    boolean isHighAndPrevHighEqual =
                            Math.floor(stockPrice.getHigh())
                                    == Math.floor(stockPrice.getPrevHigh());

                    // if (!isHighAndPrevHighEqual) {

                    boolean isHigherHighRed =
                            (CandleStickUtils.isHigherHigh(stockPrice)
                                            || stockPrice.getOpen() > stockPrice.getPrevClose())
                                    && CandleStickUtils.isRed(stockPrice);
                    //  if (!isHigherHighRed) {

                    return true;
                    //   }
                    //  }
                    //  }
                    //  }
                }
            }
        }
        return false;
    }

    private Optional<StockAnalysis> isDailyEntrySatisFied(
            StockPrice stockPriceMonthly,
            StockPrice stockPriceDaily,
            StockTechnicals stockTechnicalsDaily,
            LocalDate sessionDate,
            ResearchTechnical.Strategy strategy) {

        Stock stock = stockPriceDaily.getStock();

        boolean isGreen = CandleStickUtils.isGreen(stockPriceDaily);

        double closeDaily = stockPriceDaily.getClose();
        double lowDaily = stockPriceDaily.getLow();
        boolean isEma5LowRejected =
                stockTechnicalsDaily.getEma5() > lowDaily
                        && stockTechnicalsDaily.getEma5() < closeDaily;
        boolean isEma20LowRejected =
                stockTechnicalsDaily.getEma20() > lowDaily
                        && stockTechnicalsDaily.getEma20() < closeDaily;
        boolean isEma50LowRejected =
                stockTechnicalsDaily.getEma50() > lowDaily
                        && stockTechnicalsDaily.getEma50() < closeDaily;
        boolean isEma100LowRejected =
                stockTechnicalsDaily.getEma100() > lowDaily
                        && stockTechnicalsDaily.getEma100() < closeDaily;
        boolean isEma200LowRejected =
                stockTechnicalsDaily.getEma200() > lowDaily
                        && stockTechnicalsDaily.getEma200() < closeDaily;
        boolean isLowRejected =
                isEma5LowRejected
                        || isEma20LowRejected
                        || isEma50LowRejected
                        || isEma100LowRejected
                        || isEma200LowRejected;

        double dailyVariation =
                formulaService.calculateAbsChangePercentage(
                        stockPriceDaily.getOpen(), stockPriceDaily.getHigh());

        boolean isOpenAndLowEqual = CandleStickUtils.isOpenAndLowEqual(stockPriceDaily);

        boolean isUpperWickLongerThanLowerWick =
                !isOpenAndLowEqual
                        && CandleStickUtils.isUpperWickLongerThanLowerWick(stockPriceDaily);

        double ema5 = stockTechnicalsDaily.getEma5();
        double ema20 = stockTechnicalsDaily.getEma20();
        double ema50 = stockTechnicalsDaily.getEma50();

        double prevEma5 = stockTechnicalsDaily.getPrevEma5();
        double prevEma20 = stockTechnicalsDaily.getPrevEma20();
        double prevEma50 = stockTechnicalsDaily.getPrevEma50();

        double prev2Ema5 = stockTechnicalsDaily.getPrev2Ema5();

        boolean isOpenAndCloseAboveEma5 =
                stockPriceDaily.getOpen() > ema5
                        && stockPriceDaily.getClose() > ema5
                        && MovingAverageUtil.isAllMaAlignedBullish(
                                stockTechnicalsDaily.getTimeframe(), stockTechnicalsDaily);

        double bodyVariation =
                formulaService.calculateAbsChangePercentage(
                        stockPriceDaily.getOpen(), stockPriceDaily.getClose());

        // boolean isDailyBodyExtended = isUpperWickLongerThanLowerWick && bodyVariation >=7.0;
        boolean isDailyBodyExtended = false;
        // boolean isDailyExtendedVariation =  false;
        // System.out.println("dailyVariation "+ stock.getNseSymbol()+" " +
        // stockPriceDaily.getOpen()+" "+ stockPriceDaily.getHigh() + " " + dailyVariation);

        boolean isEma5OnTop = (ema5 > ema20 && ema20 > ema50);

        if (!this.isDead(stockPriceDaily) && CandleStickUtils.isGreen(stockPriceDaily)) {
            boolean isDailyExtendedVariation =
                    isOpenAndCloseAboveEma5
                            && isUpperWickLongerThanLowerWick
                            && dailyVariation >= 10.0;
            boolean isExtendedGap = isEma5OnTop && CandleStickUtils.isRisingWindow(stockPriceDaily);
            boolean isExtendedFromEma5 =
                    isEma5OnTop
                            && formulaService.calculateChangePercentage(
                                            ema5, stockPriceDaily.getClose())
                                    > 5.0;
            boolean isPrevExtendedFromEma5 =
                    isEma5OnTop
                            && formulaService.calculateChangePercentage(
                                            prevEma5, stockPriceDaily.getPrevClose())
                                    > 5.0;

            if (!isDailyExtendedVariation
                    && !isExtendedFromEma5
                    && !isExtendedGap
                    && !isPrevExtendedFromEma5) {

                long avgVol = stockTechnicalsDaily.getVolumeAvg20();

                boolean isAvgVolumeSufficient =
                        avgVol > 20_000 || avgVol * stockTechnicalsDaily.getEma20() > 200_00_000;

                if (isAvgVolumeSufficient) {

                    if (isGreen || isLowRejected) {
                        // System.out.println(" Here3 " + stock.getNseSymbol());
                        boolean isPRevREdORLowerHighLowerLow =
                                CandleStickUtils.isPrevSessionRed(stockPriceDaily)
                                        || CandleStickUtils.isPrevLowerHigh(stockPriceDaily)
                                                && CandleStickUtils.isPrevLowerLow(stockPriceDaily);

                        boolean isPRevGreenAndHigherLow =
                                CandleStickUtils.isPrevSessionGreen(stockPriceDaily)
                                        && CandleStickUtils.isPrev2SessionRed(stockPriceDaily)
                                        && CandleStickUtils.isPrevHigherLow(stockPriceDaily)
                                        && stockPriceDaily.getPrevClose()
                                                > stockPriceDaily.getPrev2Open();

                        if (isPRevREdORLowerHighLowerLow || isPRevGreenAndHigherLow) {
                            boolean isVolOrAvgIncr =
                                    stockTechnicalsDaily.getVolume()
                                                    > stockTechnicalsDaily.getPrevVolume()
                                            || stockTechnicalsDaily.getVolumeAvg20()
                                                    > stockTechnicalsDaily.getPrevVolumeAvg20();
                            //     System.out.println(" Here4 " + stock.getNseSymbol());
                            MarketCapCategory marketCapCategory =
                                    MarketCapCategory.classify(
                                            fundamentalResearchService.marketCap(stockPriceDaily));

                            if (isVolOrAvgIncr) {

                                double volThreshold = 1.0;

                                if (marketCapCategory == MarketCapCategory.LARGECAP) {
                                    volThreshold = 1.0;
                                }
                                if (marketCapCategory == MarketCapCategory.MIDCAP) {
                                    volThreshold = 1.5;
                                }
                                if (marketCapCategory == MarketCapCategory.SMALLCAP) {
                                    volThreshold = 2.0;
                                }
                                if (marketCapCategory == MarketCapCategory.MICROCAP) {
                                    volThreshold = 2.5;
                                }

                                boolean isVolumeAboveAverage =
                                        (marketCapCategory == MarketCapCategory.MEGACAP)
                                                ? stockTechnicalsDaily.getVolume()
                                                        > stockTechnicalsDaily.getPrevVolume()
                                                : stockTechnicalsDaily.getVolume()
                                                        > stockTechnicalsDaily.getVolumeAvg20()
                                                                * volThreshold;

                                boolean isVolumeIncr =
                                        stockTechnicalsDaily.getVolume()
                                                        > stockTechnicalsDaily.getVolumeAvg20()
                                                && stockTechnicalsDaily.getVolume()
                                                        > 1.40
                                                                * stockTechnicalsDaily
                                                                        .getPrevVolume();

                                // System.out.println(" Here5 " + stock.getNseSymbol() + "
                                // "+volThreshold);
                                if (isVolumeAboveAverage || isVolumeIncr) {
                                    //  System.out.println(" Here6 " + stock.getNseSymbol() );

                                    boolean isEma5Lowest = ema5 < ema20 && ema20 < ema50;
                                    boolean isPrevEma5Lowest =
                                            prevEma5 < prevEma20 && prevEma20 < prevEma50;

                                    boolean isUpperWickDominant =
                                            (stockPriceDaily.getClose() > ema5 && !isEma5Lowest)
                                                    && CandleStickUtils.isStrongRange(
                                                            stockPriceDaily.getTimeframe(),
                                                            stockPriceDaily,
                                                            stockTechnicalsDaily)
                                                    && CandleStickUtils.isUpperWickDominant(
                                                            stockPriceDaily)
                                                    && ((closeDaily > stockTechnicalsDaily.getEma5()
                                                                    && closeDaily
                                                                            > stockTechnicalsDaily
                                                                                    .getEma20())
                                                            || (stockPriceDaily.getPrevClose()
                                                                                    > prevEma5
                                                                            && !isPrevEma5Lowest)
                                                                    && CandleStickUtils
                                                                            .isPrevStrongRange(
                                                                                    stockPriceDaily
                                                                                            .getTimeframe(),
                                                                                    stockPriceDaily,
                                                                                    stockTechnicalsDaily)
                                                                    && CandleStickUtils
                                                                            .isPrevUpperWickDominant(
                                                                                    stockPriceDaily));
                                    if (!isUpperWickDominant) {
                                        //  System.out.println(" Here7 " + stock.getNseSymbol() );
                                        boolean isLowerHighAndLowerLow =
                                                CandleStickUtils.isRed(stockPriceDaily)
                                                        && CandleStickUtils.isLowerHigh(
                                                                stockPriceDaily)
                                                        && CandleStickUtils.isLowerLow(
                                                                stockPriceDaily);
                                        if (!isLowerHighAndLowerLow) {
                                            // System.out.println(" Here8 " + stock.getNseSymbol()
                                            // );
                                            boolean isHarami =
                                                    CandleStickUtils.isRed(stockPriceDaily)
                                                            && CandleStickUtils.isPrevSessionGreen(
                                                                    stockPriceDaily)
                                                            && CandleStickUtils.isLowerHigh(
                                                                    stockPriceDaily)
                                                            && CandleStickUtils.isHigherLow(
                                                                    stockPriceDaily);
                                            boolean isDoji =
                                                    CandleStickUtils.isVerySmallBody(
                                                            stockPriceDaily);

                                            boolean isHaningMan =
                                                    MovingAverageUtil.isAllMaAlignedBullish(
                                                                    stockPriceDaily.getTimeframe(),
                                                                    stockTechnicalsDaily)
                                                            && CandleStickUtils
                                                                    .isLowerWickLongerThanUpperWick(
                                                                            stockPriceDaily)
                                                            && stockPriceDaily.getOpen() > ema5
                                                            && stockPriceDaily.getClose() > ema5;
                                            if (!isHarami && !isDoji && !isHaningMan) {

                                                boolean isRsiInRange =
                                                        stockTechnicalsDaily.getRsi() < 65.0;

                                                if (isRsiInRange) {
                                                    boolean isBreakOutEma5 =
                                                            stockPriceDaily.getClose() > ema5
                                                                    && stockPriceDaily
                                                                                    .getPrevClose()
                                                                            < prevEma5;
                                                    boolean isPrevBreakOutEma5 =
                                                            stockPriceDaily.getPrevClose()
                                                                            > prevEma5
                                                                    && stockPriceDaily
                                                                                    .getPrev2Close()
                                                                            < prev2Ema5;

                                                    if (isBreakOutEma5 || isPrevBreakOutEma5) {
                                                        double entryPrice =
                                                                Math.min(
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        Math.min(
                                                                                                stockPriceMonthly
                                                                                                        .getClose(),
                                                                                                stockPriceDaily
                                                                                                        .getPrevClose()),
                                                                                        strategy
                                                                                                        == ResearchTechnical
                                                                                                                .Strategy
                                                                                                                .OMEGA
                                                                                                ? 2.0
                                                                                                : 1.5),
                                                                        stockPriceDaily.getHigh());

                                                        entryPrice =
                                                                Math.max(
                                                                        entryPrice,
                                                                        stockPriceDaily.getClose());

                                                        double target =
                                                                this.calculateTarget(
                                                                        entryPrice,
                                                                        Timeframe.MONTHLY);

                                                        LocalDate currentCloseDate =
                                                                miscUtil.currentMonthLastDay();

                                                        if (calendarService
                                                                .isLastTradingSessionOfMonth(
                                                                        sessionDate)) {
                                                            currentCloseDate =
                                                                    calendarService
                                                                            .previousTradingSession(
                                                                                    YearMonth.from(
                                                                                                    sessionDate
                                                                                                            .plusMonths(
                                                                                                                    1))
                                                                                            .atEndOfMonth());
                                                        }

                                                        StockPrice stockPriceCurrent =
                                                                updatePriceService.buildBack(
                                                                        Timeframe.DAILY,
                                                                        stock,
                                                                        currentCloseDate);
                                                        StockTechnicals stockTechnicalsCurrent =
                                                                updateTechnicalsService.buildBack(
                                                                        Timeframe.DAILY,
                                                                        stock,
                                                                        currentCloseDate);

                                                        double stopLoss = stockPriceDaily.getLow();

                                                        double currentClose =
                                                                stockPriceCurrent.getClose();

                                                        double dynamicStopLoss =
                                                                this.calculateDynamicStopLoss(
                                                                        stopLoss,
                                                                        stockPriceCurrent,
                                                                        stockTechnicalsCurrent,
                                                                        Timeframe.MONTHLY);

                                                        boolean isExitCandidate =
                                                                this.isExitCandidate(
                                                                        dynamicStopLoss,
                                                                        stockPriceCurrent,
                                                                        stockTechnicalsCurrent);

                                                        double risk =
                                                                ((entryPrice - stopLoss)
                                                                                / entryPrice)
                                                                        * 100.0;
                                                        // System.out.println("risk " + risk);

                                                        double riskThreshold = 1.0;

                                                        if (marketCapCategory
                                                                == MarketCapCategory.MEGACAP) {
                                                            riskThreshold = 2.5;
                                                        } else if (marketCapCategory
                                                                == MarketCapCategory.LARGECAP) {
                                                            riskThreshold = 2.0;
                                                        } else if (marketCapCategory
                                                                == MarketCapCategory.MIDCAP) {
                                                            riskThreshold = 3.5;
                                                        } else if (marketCapCategory
                                                                == MarketCapCategory.SMALLCAP) {
                                                            riskThreshold = 2.5;
                                                        } else {
                                                            riskThreshold = 2.0;
                                                        }

                                                        boolean isUpperWickSizeConfirmed =
                                                                candleStickConfirmationService
                                                                        .isUpperWickSizeConfirmed(
                                                                                Timeframe.DAILY,
                                                                                stockPriceDaily,
                                                                                stockTechnicalsDaily);
                                                        boolean isInvertedHammer =
                                                                CandleStickUtils
                                                                                .isUpperWickDominant(
                                                                                        stockPriceDaily)
                                                                        && closeDaily < ema50
                                                                        && closeDaily
                                                                                < MovingAverageUtil
                                                                                        .getMovingAverage200(
                                                                                                Timeframe
                                                                                                        .DAILY,
                                                                                                stockTechnicalsDaily);
                                                        if ((CandleStickUtils.isStrongRange(
                                                                                Timeframe.DAILY,
                                                                                stockPriceDaily,
                                                                                stockTechnicalsDaily)
                                                                        && isUpperWickSizeConfirmed)
                                                                || isInvertedHammer) {
                                                            double candleRisk =
                                                                    ((stockPriceDaily.getHigh()
                                                                                            - stockPriceDaily
                                                                                                    .getLow())
                                                                                    / stockPriceDaily
                                                                                            .getHigh())
                                                                            * 100.0;
                                                            System.out.println(
                                                                    "Here1 "
                                                                            + stock.getNseSymbol()
                                                                            + " risk "
                                                                            + risk
                                                                            + " riskThreshold "
                                                                            + riskThreshold);
                                                            if (candleRisk < riskThreshold * 2
                                                                    && candleRisk <= 5.0) {
                                                                riskThreshold = candleRisk;
                                                            }
                                                        }

                                                        // riskWeitagr = 1.5;
                                                        // riskThreshold = riskThreshold +
                                                        // riskWeitagr;
                                                        // SMALLCAP = 2.5,LARGECAP = 2.0, MIDCAP =
                                                        // 3.5,
                                                        // MEGACAP = 2.5
                                                        System.out.println(
                                                                "Here2 "
                                                                        + stock.getNseSymbol()
                                                                        + " risk "
                                                                        + risk
                                                                        + " riskThreshold "
                                                                        + riskThreshold);
                                                        if (risk > riskThreshold
                                                                && risk < riskThreshold * 3
                                                                && Math.floor(risk)
                                                                        <= riskThreshold + 1.0) {

                                                            // System.out.println("risk recalculated
                                                            // " +
                                                            // risk);

                                                            entryPrice =
                                                                    formulaService
                                                                            .applyPercentChange(
                                                                                    entryPrice,
                                                                                    -1
                                                                                            * (risk
                                                                                                    - riskThreshold));

                                                            if (marketCapCategory
                                                                            == MarketCapCategory
                                                                                    .MEGACAP
                                                                    || marketCapCategory
                                                                            == MarketCapCategory
                                                                                    .LARGECAP) {
                                                                entryPrice =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        entryPrice,
                                                                                        1.0);
                                                            } else if (marketCapCategory
                                                                    == MarketCapCategory.MIDCAP) {
                                                                entryPrice =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        entryPrice,
                                                                                        0.8);
                                                            } else {
                                                                entryPrice =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        entryPrice,
                                                                                        0.50);
                                                            }
                                                        }

                                                        if (entryPrice
                                                                < stockPriceDaily.getClose()) {
                                                            entryPrice =
                                                                    formulaService
                                                                            .applyPercentChange(
                                                                                    stockPriceDaily
                                                                                            .getClose(),
                                                                                    1.0);
                                                        }

                                                        entryPrice =
                                                                Math.min(
                                                                        entryPrice,
                                                                        stockPriceDaily.getHigh());

                                                        double sellPrice =
                                                                formulaService.applyPercentChange(
                                                                        currentClose, -1 * 0.5);

                                                        double realizedStopLoss =
                                                                formulaService.applyPercentChange(
                                                                        stopLoss, -1 * 0.5);
                                                        double per =
                                                                formulaService
                                                                        .calculateChangePercentage(
                                                                                entryPrice,
                                                                                Math.max(
                                                                                        realizedStopLoss,
                                                                                        sellPrice));
                                                        risk =
                                                                ((entryPrice - stopLoss)
                                                                                / entryPrice)
                                                                        * 100.0;
                                                        System.out.println(
                                                                "Here3 "
                                                                        + stock.getNseSymbol()
                                                                        + " risk "
                                                                        + risk
                                                                        + " riskThreshold "
                                                                        + riskThreshold);
                                                        // if (Math.floor(risk) <= riskThreshold) {
                                                        if (Math.floor(risk)
                                                                <= riskThreshold + 2.0) {

                                                            if (risk <= riskThreshold + 0.5) {
                                                                System.out.println(
                                                                        "Here4 "
                                                                                + stock
                                                                                        .getNseSymbol()
                                                                                + " risk "
                                                                                + risk
                                                                                + " riskThreshold "
                                                                                + riskThreshold);
                                                                riskThreshold = riskThreshold + 0.5;
                                                            }

                                                            if (risk > riskThreshold) {
                                                                System.out.println(
                                                                        "Here5 "
                                                                                + stock
                                                                                        .getNseSymbol()
                                                                                + " risk "
                                                                                + risk
                                                                                + " riskThreshold "
                                                                                + riskThreshold);
                                                                entryPrice =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        entryPrice,
                                                                                        -1
                                                                                                * (risk
                                                                                                        - riskThreshold));
                                                                risk =
                                                                        ((entryPrice - stopLoss)
                                                                                        / entryPrice)
                                                                                * 100.0;
                                                                per =
                                                                        formulaService
                                                                                .calculateChangePercentage(
                                                                                        entryPrice,
                                                                                        Math.max(
                                                                                                realizedStopLoss,
                                                                                                sellPrice));
                                                            }

                                                            StockPrice stockPriceNextDay =
                                                                    getStockPriceFromMap(
                                                                            Timeframe.DAILY,
                                                                            stock,
                                                                            calendarService
                                                                                    .nextTradingSession(
                                                                                            sessionDate));

                                                            System.out.println(
                                                                    "Here6 "
                                                                            + stock.getNseSymbol()
                                                                            + " risk "
                                                                            + risk
                                                                            + " riskThreshold "
                                                                            + riskThreshold);
                                                            if (Math.floor(risk)
                                                                    <= Math.floor(riskThreshold)) {
                                                                System.out.println(
                                                                        "Here7 "
                                                                                + stock
                                                                                        .getNseSymbol()
                                                                                + " risk "
                                                                                + risk
                                                                                + " riskThreshold "
                                                                                + riskThreshold);

                                                                entryPrice =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        entryPrice,
                                                                                        1.50);
                                                                entryPrice =
                                                                        Math.min(
                                                                                entryPrice,
                                                                                stockPriceDaily
                                                                                        .getHigh());

                                                                double entryPriceClose =
                                                                        formulaService
                                                                                .applyPercentChange(
                                                                                        stockPriceDaily
                                                                                                .getClose(),
                                                                                        0.75);
                                                                entryPrice =
                                                                        Math.max(
                                                                                entryPriceClose,
                                                                                entryPrice);
                                                                if (!miscUtil.isBackTest()
                                                                        || entryPrice
                                                                                > stockPriceNextDay
                                                                                        .getLow()) {
                                                                    StockAnalysis stockAnalysis =
                                                                            new StockAnalysis(
                                                                                    sessionDate,
                                                                                    currentCloseDate,
                                                                                    strategy,
                                                                                    stock,
                                                                                    marketCapCategory,
                                                                                    true,
                                                                                    true,
                                                                                    true,
                                                                                    true,
                                                                                    true,
                                                                                    stockPriceMonthly
                                                                                            .getClose(),
                                                                                    currentClose,
                                                                                    formulaService
                                                                                            .ceilToNearestTen(
                                                                                                    entryPrice),
                                                                                    stopLoss,
                                                                                    dynamicStopLoss,
                                                                                    risk,
                                                                                    target,
                                                                                    per,
                                                                                    isExitCandidate);
                                                                    return Optional.of(
                                                                            stockAnalysis);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean isDead(StockPrice stockPrice) {

        if (stockPrice.getOpen().equals(stockPrice.getHigh())
                && stockPrice.getOpen().equals(stockPrice.getLow())
                && stockPrice.getOpen().equals(stockPrice.getClose())) {
            return true;
        } else if (stockPrice.getPrevOpen().equals(stockPrice.getPrevHigh())
                && stockPrice.getPrevOpen().equals(stockPrice.getPrevLow())
                && stockPrice.getPrevOpen().equals(stockPrice.getPrevClose())) {
            return true;
        } else if (stockPrice.getPrev2Open().equals(stockPrice.getPrev2High())
                && stockPrice.getPrev2Open().equals(stockPrice.getPrev2Low())
                && stockPrice.getPrev2Open().equals(stockPrice.getPrev2Close())) {
            return true;
        } else if (stockPrice.getPrev3Open().equals(stockPrice.getPrev3High())
                && stockPrice.getPrev3Open().equals(stockPrice.getPrev3Low())
                && stockPrice.getPrev3Open().equals(stockPrice.getPrev3Close())) {
            return true;
        } else if (stockPrice.getPrev4Open().equals(stockPrice.getPrev4High())
                && stockPrice.getPrev4Open().equals(stockPrice.getPrev4Low())
                && stockPrice.getPrev4Open().equals(stockPrice.getPrev4Close())) {
            return true;
        } else if (stockPrice.getPrev5Open().equals(stockPrice.getPrev5High())
                && stockPrice.getPrev5Open().equals(stockPrice.getPrev5Low())
                && stockPrice.getPrev5Open().equals(stockPrice.getPrev5Close())) {
            return true;
        }

        return false;
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

        if (!fundamentalResearchService.isPriceInRange(stockPrice)) {
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

    private List<StockAnalysis> omegaScannerMonthly() {

        List<String> symbolsOct25 = new ArrayList<>();
        /*
        symbolsOct25.add("SHARDACROP");
        symbolsOct25.add("SCHAEFFLER");
        symbolsOct25.add("PRIMESECU");
        symbolsOct25.add("ETHOSLTD");
        symbolsOct25.add("PRIVISCL");
        symbolsOct25.add("BEL");
        symbolsOct25.add("AMBER");
        symbolsOct25.add("ADANIPORTS");
        symbolsOct25.add("GLOBUSSPR");
        symbolsOct25.add("PTCIL");
        symbolsOct25.add("MINDACORP");
        symbolsOct25.add("SAMHI");
        symbolsOct25.add("AHLUCONT");
        symbolsOct25.add("INDUSTOWER");
        symbolsOct25.add("EPIGRAL");
        symbolsOct25.add("ASHAPURMIN");
        symbolsOct25.add("NUVAMA");
        symbolsOct25.add("SHALBY");
        symbolsOct25.add("LUMAXIND");
        symbolsOct25.add("MOIL");
        symbolsOct25.add("LORDSCHLO");
        symbolsOct25.add("MAXESTATES");
        symbolsOct25.add("CSBBANK");*/

        List<Stock> stocks = stockService.getActiveStocks();
        // List<Stock> stocks = stockService.getForActivity();

        List<Stock> result = new ArrayList<>();
        for (String symbol : symbolsOct25) {
            Stock stock = stockService.getStockByNseSymbol(symbol);
            result.add(stock);
        }

        LocalDate currentMonthFirstSession =
                calendarService.nextTradingSession(miscUtil.previousMonthLastDay());
        System.out.println("currentMonthFirstSession: " + currentMonthFirstSession);
        LocalDate currentMonthSecondSession =
                calendarService.nextTradingSession(currentMonthFirstSession);
        System.out.println("currentMonthSecondSession: " + currentMonthSecondSession);
        LocalDate currentMonthThirdSession =
                calendarService.nextTradingSession(currentMonthSecondSession);
        int counter = 0;
        if (result.isEmpty()) {
            for (Stock stock : stocks) {

                LocalDate sessionDate = currentMonthFirstSession;
                LocalDate sessionDateTill = currentMonthThirdSession;

                System.out.println("sessionDate: " + sessionDate);
                System.out.println("sessionDateTill: " + sessionDateTill);
                while (sessionDate.isBefore(sessionDateTill)) {
                    StockPrice stockPrice =
                            updatePriceService.buildBack(Timeframe.DAILY, stock, sessionDate);
                    StockTechnicals stockTechnicals =
                            updateTechnicalsService.buildBack(Timeframe.DAILY, stock, sessionDate);

                    TradeSetup tradeSetup =
                            simplePriceActionSignalEvaluator.evaluateEntry(
                                    stockPrice.getTimeframe(), stock, stockPrice, stockTechnicals);

                    if (stockPrice != null && stockTechnicals != null) {

                        if (tradeSetup.isActive()) {

                            if (tradeSetup.getSubStrategy()
                                    == ResearchTechnical.SubStrategy.MONTHLY_BREAKOUT) {
                                ++counter;
                                result.add(stock);
                            }
                        }
                    } else {
                        System.out.println("data not found " + stock.getNseSymbol());
                    }
                    sessionDate = calendarService.nextTradingSession(sessionDate);
                }
            }
        }

        System.out.println("monthly counter " + counter);

        List<StockAnalysis> analyzed = this.filterAnalyzed(result, Timeframe.MONTHLY);

        // this.sortAndPrint(analyzed);

        return analyzed;
    }

    private List<StockAnalysis> omegaScannerWeekly() {

        List<String> symbolsOct25 = new ArrayList<>();
        symbolsOct25.add("MINDACORP");
        symbolsOct25.add("SHARDACROP");
        symbolsOct25.add("CENTRUM");
        symbolsOct25.add("GRAPHITE");

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
            // StockPrice stockPriceDaily = stockPriceService.get(stock, Timeframe.DAILY);
            // StockTechnicals stockTechnicalsDaily = stockTechnicalsService.get(stock,
            // Timeframe.DAILY);
            // System.out.println("Here1 " + stock.getNseSymbol());
            StockPrice stockPriceDaily =
                    updatePriceService.buildBack(
                            Timeframe.DAILY, stock, miscUtil.currentMonthLastDay());
            StockTechnicals stockTechnicalsDaily =
                    updateTechnicalsService.buildBack(
                            Timeframe.DAILY, stock, miscUtil.currentMonthLastDay());

            // StockPrice stockPriceHt = stockPriceService.get(stock, timeframeHt);
            // StockTechnicals stockTechnicalsHt = stockTechnicalsService.get(stock, timeframeHt);

            StockPrice stockPriceHt =
                    updatePriceService.buildBack(
                            Timeframe.MONTHLY,
                            stock,
                            calendarService.previousTradingSession(miscUtil.currentDate()));
            StockTechnicals stockTechnicalsHt =
                    updateTechnicalsService.buildBack(
                            Timeframe.MONTHLY,
                            stock,
                            calendarService.previousTradingSession(miscUtil.currentDate()));

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

            System.out.println("ohlcvFromTo " + ohlcvFromTo);

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
                            stopLoss, stockPriceDaily, stockTechnicalsDaily, timeframeHt);

            boolean isExitCandidate =
                    this.isExitCandidate(dynamicStopLoss, stockPriceDaily, stockTechnicalsDaily);

            double risk = ((entryPrice - stopLoss) / entryPrice) * 100.0;

            double target = this.calculateTarget(entryPrice, timeframeHt);

            // System.out.println("Reached here " + stock.getNseSymbol());

            if (risk < (timeframeHt == Timeframe.MONTHLY ? 5.0 : 3.0)) {
                // System.out.println("Here5 " + stock.getNseSymbol());
                analyzed.add(
                        new StockAnalysis(
                                miscUtil.currentDate(),
                                miscUtil.currentDate(),
                                ResearchTechnical.Strategy.OMEGA,
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
            double initialStopLoss,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe) {

        double dynamicStopLoss = initialStopLoss;

        double minTarget = timeframe == Timeframe.WEEKLY ? 2.0 : 8.0;

        // if (currentChg > minTarget) {
        dynamicStopLoss =
                Math.max(
                        MovingAverageUtil.getMovingAverage5(
                                stockTechnicals.getTimeframe(), stockTechnicals),
                        stockPrice.getLow());
        // }
        return dynamicStopLoss;
    }

    private boolean isExitCandidate(
            double dynamicStopLoss, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        boolean isExitCandidate = false;

        if (stockPrice.getClose() < stockTechnicals.getEma20()) {
            isExitCandidate = true;
        }

        if (!isExitCandidate && !(CandleStickUtils.isLowerWickDominant(stockPrice))) {
            isExitCandidate = stockPrice.getClose() < dynamicStopLoss;
        }

        if (!isExitCandidate) {
            if (stockPrice.getClose() < stockTechnicals.getEma5()
                    && CandleStickUtils.isRed(stockPrice)
                    && (CandleStickUtils.isPrevSessionRed(stockPrice)
                            || (CandleStickUtils.isLowerLow(stockPrice)
                                    && CandleStickUtils.isLowerHigh(stockPrice)))) {
                if ((stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20())
                        || (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume())) {
                    isExitCandidate = true;
                }
            }
        }

        if (!isExitCandidate) {
            if (stockPrice.getClose() < stockTechnicals.getEma5()) {
                if (stockPrice.getPrevClose() < stockTechnicals.getPrevEma5()) {
                    isExitCandidate = true;
                }
            }
        }

        if (!isExitCandidate) {
            if (stockPrice.getClose() < stockTechnicals.getEma5()) {
                if (CandleStickUtils.isRed(stockPrice)
                        && CandleStickUtils.isPrevSessionGreen(stockPrice)) {
                    if (stockPrice.getOpen() > stockPrice.getPrevClose()
                            && stockPrice.getClose() < stockPrice.getPrevOpen()) {
                        isExitCandidate = true;
                    }
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

    private List<StockAnalysis> sortAndPrint(List<StockAnalysis> analyzed) {
        // Step 1: remove duplicates (based on stock + scanDate)
        analyzed =
                analyzed.stream()
                        .filter(sa -> sa.getMarketCap() != MarketCapCategory.MICROCAP)
                        .filter(
                                sa -> {
                                    if (sa.getMarketCap() == MarketCapCategory.MEGACAP) {
                                        return sa.getRisk()
                                                < 3.0; // Stricter risk filter for MEGACAP
                                    } else if (sa.getMarketCap() == MarketCapCategory.LARGECAP) {
                                        return sa.getRisk()
                                                < 2.5; // Stricter risk filter for MEGACAP
                                    } else if (sa.getMarketCap() == MarketCapCategory.MIDCAP) {
                                        return sa.getRisk()
                                                < 3.5; // Stricter risk filter for MEGACAP
                                    } else if (sa.getMarketCap() == MarketCapCategory.SMALLCAP) {
                                        return sa.getRisk()
                                                < 3.0; // Stricter risk filter for MEGACAP
                                    }
                                    return sa.getRisk() <= 1.5; // Normal risk filter for others
                                })
                        .collect(
                                Collectors.collectingAndThen(
                                        Collectors.toMap(
                                                sa -> sa.getStock().getNseSymbol(),
                                                sa -> sa,
                                                (sa1, sa2) -> sa1),
                                        map -> new ArrayList<>(map.values())));

        // Step 2: sort by your existing multi-criteria comparator
        analyzed.sort(
                Comparator.comparing(StockAnalysis::getScanDate)
                        .thenComparing(StockAnalysis::getStrategy)
                        .thenComparing(StockAnalysis::getRisk, Comparator.reverseOrder()));
        // .thenComparing(StockAnalysis::isLowRejected, Comparator.reverseOrder())
        // .thenComparing(sa -> sa.isAvgIncr() && sa.isVolIncr(),
        // Comparator.reverseOrder())
        // .thenComparing(sa -> sa.isLongLowerWick(), Comparator.reverseOrder())
        // .thenComparing(StockAnalysis::isAvgIncr, Comparator.reverseOrder()));

        // Step 3: print results
        for (StockAnalysis sa : analyzed) {

            System.out.printf(
                    "%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%%,%s%n",
                    sa.getScanDate(),
                    sa.getCurrentCloseDate(),
                    sa.getStock().getNseSymbol(),
                    sa.getStrategy(),
                    sa.getMarketCap(),
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
        return analyzed;
    }

    public List<Allocation> allocateFundsByStrategyAndMarketCap(
            List<StockAnalysis> stocks, double totalFund, double maxCapPer) {

        List<Allocation> allAllocations = new ArrayList<>();

        // 1️⃣ Group by scanDate
        Map<LocalDate, List<StockAnalysis>> byDate =
                stocks.stream()
                        .collect(
                                Collectors.groupingBy(
                                        StockAnalysis::getScanDate,
                                        LinkedHashMap::new,
                                        Collectors.toList()));

        for (Map.Entry<LocalDate, List<StockAnalysis>> dateEntry : byDate.entrySet()) {
            LocalDate date = dateEntry.getKey();
            List<StockAnalysis> dateStocks = dateEntry.getValue();
            if (dateStocks.isEmpty()) continue;

            // --- Define weights ---
            Map<ResearchTechnical.Strategy, Double> strategyWeights =
                    Map.of(
                            ResearchTechnical.Strategy.ALPHA, 0.60,
                            ResearchTechnical.Strategy.ULTIMA, 0.20,
                            ResearchTechnical.Strategy.OMEGA, 0.10,
                            ResearchTechnical.Strategy.GAMA, 0.10);

            Map<MarketCapCategory, Double> marketCapWeights =
                    Map.of(
                            MarketCapCategory.SMALLCAP, 0.45,
                            MarketCapCategory.MIDCAP, 0.30,
                            MarketCapCategory.LARGECAP, 0.10,
                            MarketCapCategory.MEGACAP, 0.15);

            // 2️⃣ Prepare mutable allocation holders
            class MutableAlloc {
                StockAnalysis stock;
                double allocated = 0.0;
                boolean capped = false;

                MutableAlloc(StockAnalysis s) {
                    this.stock = s;
                }
            }
            List<MutableAlloc> mutList =
                    dateStocks.stream().map(MutableAlloc::new).collect(Collectors.toList());
            double maxAmountPerStock = totalFund * maxCapPer;

            // 3️⃣ Build 2D matrix [Strategy × MarketCap] for available combinations
            Map<ResearchTechnical.Strategy, Map<MarketCapCategory, List<MutableAlloc>>>
                    byStrategyCap = new LinkedHashMap<>();
            for (MutableAlloc m : mutList) {
                ResearchTechnical.Strategy strategy = m.stock.getStrategy();
                MarketCapCategory cap = m.stock.getMarketCap();
                byStrategyCap
                        .computeIfAbsent(strategy, k -> new LinkedHashMap<>())
                        .computeIfAbsent(cap, k -> new ArrayList<>())
                        .add(m);
            }

            // 4️⃣ Compute normalization (only for existing combos)
            double totalWeightAvailable =
                    byStrategyCap.entrySet().stream()
                            .flatMap(
                                    s ->
                                            s.getValue().keySet().stream()
                                                    .map(
                                                            c ->
                                                                    strategyWeights.getOrDefault(
                                                                                    s.getKey(), 0.0)
                                                                            * marketCapWeights
                                                                                    .getOrDefault(
                                                                                            c,
                                                                                            0.0)))
                            .mapToDouble(Double::doubleValue)
                            .sum();

            // 5️⃣ Allocate funds proportionally
            for (var stratEntry : byStrategyCap.entrySet()) {
                ResearchTechnical.Strategy strategy = stratEntry.getKey();
                for (var capEntry : stratEntry.getValue().entrySet()) {
                    MarketCapCategory cap = capEntry.getKey();
                    List<MutableAlloc> stocksInBucket = capEntry.getValue();

                    double comboWeight =
                            strategyWeights.getOrDefault(strategy, 0.0)
                                    * marketCapWeights.getOrDefault(cap, 0.0);

                    double comboFund = (comboWeight / totalWeightAvailable) * totalFund;
                    double perStock = comboFund / stocksInBucket.size();

                    for (MutableAlloc m : stocksInBucket) {
                        double allocation = Math.min(perStock, maxAmountPerStock);
                        m.allocated = allocation;
                        m.capped = (allocation >= maxAmountPerStock - 1e-6);
                    }
                }
            }

            // 6️⃣ Redistribute leftover (from capping) - FIXED SECTION
            double totalAllocated = mutList.stream().mapToDouble(m -> m.allocated).sum();
            double leftover = totalFund - totalAllocated;

            if (leftover > 1e-6) {
                // Use ArrayList instead of immutable list from .toList()
                List<MutableAlloc> uncapped =
                        mutList.stream()
                                .filter(m -> !m.capped)
                                .collect(
                                        Collectors.toCollection(
                                                ArrayList::new)); // FIX: Use mutable ArrayList

                int iter = 0;
                while (leftover > 1e-6 && !uncapped.isEmpty() && iter < 100) {
                    double perAdd = leftover / uncapped.size();

                    // Use ListIterator for safe removal or track items to remove
                    List<MutableAlloc> toRemove = new ArrayList<>();
                    for (MutableAlloc m : uncapped) {
                        double available = maxAmountPerStock - m.allocated;
                        if (available > 1e-6) {
                            double add = Math.min(perAdd, available);
                            m.allocated += add;
                            leftover -= add;
                            if (Math.abs(m.allocated - maxAmountPerStock) < 1e-6) {
                                toRemove.add(m);
                            }
                        }
                    }
                    // Remove capped stocks from uncapped list
                    uncapped.removeAll(toRemove);
                    iter++;
                }
            }

            // 7️⃣ Final rounding + summary
            double finalTotal = 0.0;
            for (MutableAlloc m : mutList) {
                double rounded = Math.round(m.allocated * 100.0) / 100.0;
                allAllocations.add(new Allocation(m.stock, rounded));
                finalTotal += rounded;
            }

            System.out.printf("=== DATE %s ===%n", date);
            System.out.printf("Total Fund: ₹%.2f | Allocated: ₹%.2f%n", totalFund, finalTotal);
            for (MutableAlloc m : mutList) {
                System.out.printf(
                        "  %-12s | %-8s | %-8s : ₹%.2f%n",
                        m.stock.getStock().getNseSymbol(),
                        m.stock.getStrategy(),
                        m.stock.getMarketCap(),
                        m.allocated);
            }
            System.out.println();
        }

        return allAllocations;
    }

    /**
     * Allocate funds based on market cap + inverse risk weighting, with max cap per stock and
     * redistribution of leftover funds.
     *
     * @param stocks List of StockAnalysis objects
     * @param fund Total fund to allocate
     * @param maxCapPer Maximum allocation per stock as fraction (e.g., 0.075 for 7.5%)
     * @return List of Allocation objects with allocated amounts
     */
    public List<Allocation> allocateFunds(
            List<StockAnalysis> stocks, double fund, double maxCapPer) {
        // Step 1: define base weights by market cap
        Map<MarketCapCategory, Double> baseWeights =
                Map.of(
                        MarketCapCategory.SMALLCAP, 0.167,
                        MarketCapCategory.MIDCAP, 0.389,
                        MarketCapCategory.LARGECAP, 0.277,
                        MarketCapCategory.MEGACAP, 0.167);

        // Step 2: find total weight for only present market caps
        Set<MarketCapCategory> presentCaps =
                stocks.stream().map(StockAnalysis::getMarketCap).collect(Collectors.toSet());

        double totalWeight = presentCaps.stream().mapToDouble(baseWeights::get).sum();

        // Step 3: normalize weights
        Map<MarketCapCategory, Double> normalizedWeights = new HashMap<>();
        for (MarketCapCategory cap : presentCaps) {
            normalizedWeights.put(cap, baseWeights.get(cap) / totalWeight);
        }

        List<Allocation> allocations = new ArrayList<>();

        // Step 4: allocate by market cap
        for (MarketCapCategory cap : presentCaps) {
            double capFund = fund * normalizedWeights.get(cap);

            // Get stocks in this cap
            List<StockAnalysis> capStocks =
                    stocks.stream()
                            .filter(s -> s.getMarketCap() == cap)
                            .collect(Collectors.toList());

            // Track allocations and remaining stocks
            Map<StockAnalysis, Double> stockAllocMap = new LinkedHashMap<>();
            Set<StockAnalysis> remainingStocks = new HashSet<>(capStocks);

            double remainingFund = capFund;

            while (!remainingStocks.isEmpty() && remainingFund > 0) {
                // Calculate sum of inverse risks for remaining stocks
                double sumInverseRisk =
                        remainingStocks.stream().mapToDouble(s -> 1.0 / s.getRisk()).sum();

                Iterator<StockAnalysis> iterator = remainingStocks.iterator();

                while (iterator.hasNext()) {
                    StockAnalysis stock = iterator.next();
                    double fraction = (1.0 / stock.getRisk()) / sumInverseRisk;
                    double allocatedAmount = remainingFund * fraction;

                    // Apply max cap per stock
                    double maxAmount = fund * maxCapPer;
                    if (stockAllocMap.containsKey(stock)) {
                        allocatedAmount += stockAllocMap.get(stock);
                    }

                    if (allocatedAmount >= maxAmount) {
                        allocatedAmount = maxAmount;
                        iterator.remove(); // Stock reached max, remove from remaining
                    }

                    stockAllocMap.put(stock, allocatedAmount);
                }

                // Recalculate remaining fund
                double allocatedSum =
                        stockAllocMap.values().stream().mapToDouble(Double::doubleValue).sum();
                remainingFund = capFund - allocatedSum;
            }

            // Add final allocations
            stockAllocMap.forEach(
                    (stock, amount) -> allocations.add(new Allocation(stock, amount)));
        }

        return allocations;
    }

    /**
     * Allocate funds equally within each market cap category, respecting maxCapPer per stock and
     * redistributing leftover funds.
     *
     * @param stocks List of StockAnalysis objects
     * @param fund Total fund to allocate
     * @param maxCapPer Maximum allocation per stock as fraction (e.g., 0.075 for 7.5%)
     * @return List of Allocation objects with allocated amounts
     */
    public List<Allocation> allocateFundsFixed(
            List<StockAnalysis> stocks, double fund, double maxCapPer) {
        // Step 1: define base weights by market cap
        Map<MarketCapCategory, Double> baseWeights =
                Map.of(
                        MarketCapCategory.SMALLCAP, 0.167,
                        MarketCapCategory.MIDCAP, 0.389,
                        MarketCapCategory.LARGECAP, 0.277,
                        MarketCapCategory.MEGACAP, 0.167);

        // Step 2: find total weight for only present market caps
        Set<MarketCapCategory> presentCaps =
                stocks.stream().map(StockAnalysis::getMarketCap).collect(Collectors.toSet());

        double totalWeight = presentCaps.stream().mapToDouble(baseWeights::get).sum();

        // Step 3: normalize weights
        Map<MarketCapCategory, Double> normalizedWeights = new HashMap<>();
        for (MarketCapCategory cap : presentCaps) {
            normalizedWeights.put(cap, baseWeights.get(cap) / totalWeight);
        }

        List<Allocation> allocations = new ArrayList<>();

        // Step 4: allocate by market cap
        for (MarketCapCategory cap : presentCaps) {
            double capFund = fund * normalizedWeights.get(cap);

            // Get stocks in this cap
            List<StockAnalysis> capStocks =
                    stocks.stream()
                            .filter(s -> s.getMarketCap() == cap)
                            .collect(Collectors.toList());

            int n = capStocks.size();
            double equalShare = capFund / n;
            double maxAmount = fund * maxCapPer;

            // First pass: allocate min(equalShare, maxAmount)
            Map<StockAnalysis, Double> stockAllocMap = new LinkedHashMap<>();
            double leftover = 0.0;
            for (StockAnalysis stock : capStocks) {
                double allocated = Math.min(equalShare, maxAmount);
                stockAllocMap.put(stock, allocated);
                leftover += (equalShare - allocated); // track leftover for redistribution
            }

            // Redistribute leftover to stocks that haven't hit maxCapPer
            if (leftover > 0) {
                for (StockAnalysis stock : capStocks) {
                    double current = stockAllocMap.get(stock);
                    double additional = Math.min(leftover, maxAmount - current);
                    stockAllocMap.put(stock, current + additional);
                    leftover -= additional;
                    if (leftover <= 0) break;
                }
            }

            // Add final allocations
            stockAllocMap.forEach(
                    (stock, amount) -> allocations.add(new Allocation(stock, amount)));
        }

        return allocations;
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
