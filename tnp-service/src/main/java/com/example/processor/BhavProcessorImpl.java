package com.example.processor;

import com.example.data.common.type.Timeframe;
import com.example.dto.io.StockPriceIN;
import com.example.model.type.Exchange;
import com.example.model.type.IndiceType;
import com.example.service.UpdatePriceService;
import com.example.transactional.model.master.Stock;
import com.example.service.*;
import com.example.storage.model.StockPrice;
import com.example.storage.model.StockTechnicals;
import com.example.storage.repo.PriceTemplate;
import com.example.util.MiscUtil;
import com.example.dto.io.StockIO;
import com.example.dto.io.StockPriceIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@RequiredArgsConstructor
@Service
public class BhavProcessorImpl implements BhavProcessor{

    private final StockService stockService;

    private final SectorService sectorService;

    private final CalendarService calendarService;

    private final UpdatePriceService updatePriceService;

    private final UpdateTechnicalsService updateTechnicalsService;

    private final ResearchExecutorService researchExecutorService;

    private final PriceTemplate priceTemplate;

    private final MiscUtil miscUtil;

    /**
     * 1. Filter
     * 2. Insert bulk in price_history
     * 3. If list size and write result is same process to kafka
     * @param stockPriceList
     */
    @Override
    public void process(List<StockPriceIN> stockPriceList) {

        List<StockPriceIN> regularEquityList = this.filterRegularEquityShares(stockPriceList);

        List<StockPriceIO> stockPriceIOList = this.transform(regularEquityList);

        this.importAndProcessDailyPrice(stockPriceIOList);

        this.processTimeframePrice();

        this.processAndResearchTechnicals();

        log.info("Completed Bhav Processor");
    }

    public List<StockPriceIN> filterRegularEquityShares(List<StockPriceIN> records) {

        Map<String, StockPriceIN> uniqueStocks = new HashMap<>();

        for (StockPriceIN record : records) {
            String symbol = record.getNseSymbol();
            String series = record.getSeries();

            // Ignore Rights Issue (-RI) stocks
            if (symbol.endsWith("-RI")) {
                continue;
            }

            // Prioritize EQ > BE > BZ (one entry per symbol)
            if ("EQ".equals(series) ||
                    ("BE".equals(series) && !uniqueStocks.containsKey(symbol)) ||
                    ("BZ".equals(series) && !uniqueStocks.containsKey(symbol))) {
                uniqueStocks.put(symbol, record);
            }
        }

        return new ArrayList<>(uniqueStocks.values());
    }

    private List<StockPriceIO> transform(List<StockPriceIN> stockPriceINList){

        List<StockPriceIO> stockPriceIOList = new ArrayList<>();

        for(StockPriceIN stockPriceIN : stockPriceINList){
            StockPriceIO stockPriceIO = new StockPriceIO(stockPriceIN.getSource().toUpperCase(), stockPriceIN.getCompanyName(),stockPriceIN.getNseSymbol(), stockPriceIN.getSeries(), stockPriceIN.getOpen(), stockPriceIN.getHigh(), stockPriceIN.getLow(), stockPriceIN.getClose(), stockPriceIN.getLast(), stockPriceIN.getPrevClose(), stockPriceIN.getTottrdqty(), stockPriceIN.getTottrdval(), stockPriceIN.getTimestamp(), stockPriceIN.getTotaltrades(), stockPriceIN.getIsin());
            stockPriceIOList.add(stockPriceIO);
        }

        return  stockPriceIOList;
    }


    private void updateSeries(Stock stock, StockPriceIO stockPriceIO){
        if(stock.getSeries()==null || !stock.getSeries().equalsIgnoreCase(stockPriceIO.getSeries().trim().toUpperCase())) {
            stock.setSeries(stockPriceIO.getSeries().trim().toUpperCase());
            stockService.save(stock);
        }
    }

    private Stock addStockToMaster(StockPriceIO stockPriceIO){

        log.info("{} Adding to master", stockPriceIO.getNseSymbol());

        StockIO stockIO = new StockIO(stockPriceIO.getCompanyName(), "NIFTY", stockPriceIO.getNseSymbol().trim().toUpperCase(), stockPriceIO.getSeries(), stockPriceIO.getIsin(), IndiceType.NSE);

        if(stockPriceIO.getExchange().equalsIgnoreCase("NSE")) {
            stockIO.setSector("NSE");
            stockIO.setIndice(IndiceType.NSE);
            stockIO.setExchange(Exchange.NSE);
        }

        log.info("{} Added to master", stockPriceIO.getNseSymbol());
        return stockService.add(stockIO.getExchange(), stockIO.getSeries().trim().toUpperCase(), stockIO.getIsin(), stockIO.getCompanyName(), stockIO.getNseSymbol(), stockIO.getBseCode(), stockIO.getIndice(), sectorService.getOrAddSectorByName(stockIO.getSector()));
    }


    private void importAndProcessDailyPrice(List<StockPriceIO> stockPriceIOList) {
        //int numThreads = 4; // Number of threads
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Future<List<StockPrice>>> futures = new ArrayList<>();

        // Split the data into batches for parallel processing
        int batchSize = (int) Math.ceil((double) stockPriceIOList.size() / numThreads);

        for (int i = 0; i < stockPriceIOList.size(); i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, stockPriceIOList.size());
            List<StockPriceIO> batch = stockPriceIOList.subList(start, end);

            try{
            // Submit batch processing tasks
            Future<List<StockPrice>> future = executor.submit(() -> processBatch(batch));
            futures.add(future);
            miscUtil.delay(160);
            }catch(Exception e){
                log.error("An eoor occurd while processing monthly batch", e);
            }
        }

        List<StockPrice> allStockPrices = new ArrayList<>();

        // Collect results from all threads
        for (Future<List<StockPrice>> future : futures) {
            try {
                allStockPrices.addAll(future.get()); // Get results from thread
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Insert into database if not empty
        if (!allStockPrices.isEmpty()) {
            long insertCount = priceTemplate.create(allStockPrices);

            if (insertCount == allStockPrices.size()) {
                //Here we can process updateFactors
                // sendToUpdateQueue(stockPriceIOList, stockPriceIOList.get(stockPriceIOList.size() - 1).getNseSymbol());
            }
        }

        // Shutdown executor
        executor.shutdown();
    }

    // Function to process a batch of StockPriceIO
    private List<StockPrice> processBatch(List<StockPriceIO> stockPriceIOList) {
        List<StockPrice> stockPriceList = new ArrayList<>();

        for (StockPriceIO stockPriceIO : stockPriceIOList) {
            try{
            StockPrice stockPrice = updatePriceService.build(stockPriceIO);
            Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

            if(stock == null){
                stock = this.addStockToMaster(stockPriceIO);
            }

            this.updateSeries(stock, stockPriceIO);
            updatePriceService.updatePrice(Timeframe.DAILY, stock, stockPrice);
            researchExecutorService.executeFundamental(stock);
            stockPriceList.add(stockPrice);
                miscUtil.delay(32);
            }catch(Exception e){
                log.error("{} An error occurred while processing daily batch",stockPriceIO.getNseSymbol(), e);
            }
        }

        return stockPriceList;
    }

    @Override
    public void processTimeframePrice() {
        List<Stock> stockList = stockService.getActiveStocks();

        //int numThreads = 4; // Use 4 threads
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Future<List<StockPrice>>> weeklyFutures = new ArrayList<>();
        List<Future<List<StockPrice>>> monthlyFutures = new ArrayList<>();
        List<Future<List<StockPrice>>> quarterlyFutures = new ArrayList<>();
        List<Future<List<StockPrice>>> yearlyFutures = new ArrayList<>();

        // Split the stock list into batches for parallel processing
        int batchSize = (int) Math.ceil((double) stockList.size() / numThreads);

        for (int i = 0; i < stockList.size(); i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, stockList.size());
            List<Stock> batch = stockList.subList(start, end);

            // Process Yearly data if it's the last trading session of the year
            if (calendarService.isLastTradingSessionOfYear(miscUtil.currentDate())) {
                try{
                Future<List<StockPrice>> yearlyFuture = executor.submit(() -> processYearlyBatch(batch));
                yearlyFutures.add(yearlyFuture);
                miscUtil.delay(1600);
                }catch(Exception e){
                log.error("{} An eoor occurd while processing monthly batch", e);
                }
            }

            // Process Quarterly data if it's the last trading session of the quarter
            if (calendarService.isLastTradingSessionOfQuarter(miscUtil.currentDate())) {
                try{
                Future<List<StockPrice>> quarterlyFuture = executor.submit(() -> processQuarterlyBatch(batch));
                quarterlyFutures.add(quarterlyFuture);
                miscUtil.delay(1600);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing monthly batch", e);
            }
            }

            // Process Monthly data if it's the last trading session of the month
            if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
                try{
                Future<List<StockPrice>> monthlyFuture = executor.submit(() -> processMonthlyBatch(batch));
                monthlyFutures.add(monthlyFuture);
                miscUtil.delay(1600);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing monthly batch", e);
            }
            }

            // Process Weekly data if it's the last trading session of the week
            if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
                try{
                Future<List<StockPrice>> weeklyFuture = executor.submit(() -> processWeeklyBatch(batch));
                weeklyFutures.add(weeklyFuture);
                miscUtil.delay(1600);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing monthly batch", e);
            }
            }


        }

        List<StockPrice> allYearlyStockPrices = new ArrayList<>();
        List<StockPrice> allQuarterlyStockPrices = new ArrayList<>();
        List<StockPrice> allMonthlyStockPrices = new ArrayList<>();
        List<StockPrice> allWeeklyStockPrices = new ArrayList<>();


        // Collect results from all yearly threads
        for (Future<List<StockPrice>> future : yearlyFutures) {
            try {
                allYearlyStockPrices.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Collect results from all quarterly threads
        for (Future<List<StockPrice>> future : quarterlyFutures) {
            try {
                allQuarterlyStockPrices.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Collect results from all monthly threads
        for (Future<List<StockPrice>> future : monthlyFutures) {
            try {
                allMonthlyStockPrices.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Collect results from all weekly threads
        for (Future<List<StockPrice>> future : weeklyFutures) {
            try {
                allWeeklyStockPrices.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }


        // Insert Yearly data
        if (!allYearlyStockPrices.isEmpty()) {
            long insertCount = priceTemplate.create(Timeframe.YEARLY, allYearlyStockPrices);
            if (insertCount == allYearlyStockPrices.size()) {
                log.info("✅ Yearly Price Updated");
            }
        }

        // Insert Quarterly data
        if (!allQuarterlyStockPrices.isEmpty()) {
            long insertCount = priceTemplate.create(Timeframe.QUARTERLY, allQuarterlyStockPrices);
            if (insertCount == allQuarterlyStockPrices.size()) {
                log.info("✅ Quarterly Price Updated");
            }
        }
        // Insert Monthly data
        if (!allMonthlyStockPrices.isEmpty()) {
            long insertCount = priceTemplate.create(Timeframe.MONTHLY, allMonthlyStockPrices);
            if (insertCount == allMonthlyStockPrices.size()) {
                log.info("✅ Monthly Price Updated");
            }
        }

        // Insert Weekly data
        if (!allWeeklyStockPrices.isEmpty()) {
            long insertCount = priceTemplate.create(Timeframe.WEEKLY, allWeeklyStockPrices);
            if (insertCount == allWeeklyStockPrices.size()) {
                log.info("✅ Weekly Price Updated");
            }
        }

        executor.shutdown();
    }

    // Function to process a batch for yearly aggregation
    private List<StockPrice> processYearlyBatch(List<Stock> stockBatch) {
        List<StockPrice> yearlyStockPriceList = new ArrayList<>();
        for (Stock stock : stockBatch) {
            try{
            StockPrice stockPrice = updatePriceService.build(Timeframe.YEARLY, stock, miscUtil.currentDate());
            updatePriceService.updatePrice(Timeframe.YEARLY, stock, stockPrice);
            yearlyStockPriceList.add(stockPrice);
                miscUtil.delay(320);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing yearly batch",stock.getNseSymbol(), e);
            }
        }
        return yearlyStockPriceList;
    }

    // Function to process a batch for quarterly aggregation
    private List<StockPrice> processQuarterlyBatch(List<Stock> stockBatch) {
        List<StockPrice> quarterlyStockPriceList = new ArrayList<>();
        for (Stock stock : stockBatch) {
            try{
            StockPrice stockPrice = updatePriceService.build(Timeframe.QUARTERLY, stock, miscUtil.currentDate());
            updatePriceService.updatePrice(Timeframe.QUARTERLY, stock, stockPrice);
            quarterlyStockPriceList.add(stockPrice);
                miscUtil.delay(320);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing quarterly batch",stock.getNseSymbol(), e);
            }
        }
        return quarterlyStockPriceList;
    }
    // Function to process a batch for monthly aggregation
    private List<StockPrice> processMonthlyBatch(List<Stock> stockBatch) {
        List<StockPrice> monthlyStockPriceList = new ArrayList<>();
        for (Stock stock : stockBatch) {
            try {
                StockPrice stockPrice = updatePriceService.build(Timeframe.MONTHLY, stock, miscUtil.currentDate());
                updatePriceService.updatePrice(Timeframe.MONTHLY, stock, stockPrice);
                monthlyStockPriceList.add(stockPrice);
                miscUtil.delay(320);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing monthly batch",stock.getNseSymbol(), e);
            }
        }
        return monthlyStockPriceList;
    }

    // Function to process a batch for weekly aggregation
    private List<StockPrice> processWeeklyBatch(List<Stock> stockBatch) {
        List<StockPrice> weeklyStockPriceList = new ArrayList<>();
        for (Stock stock : stockBatch) {
            try{
            StockPrice stockPrice = updatePriceService.build(Timeframe.WEEKLY, stock, miscUtil.currentDate());
            updatePriceService.updatePrice(Timeframe.WEEKLY, stock, stockPrice);
            weeklyStockPriceList.add(stockPrice);
                miscUtil.delay(320);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing weekly batch",stock.getNseSymbol(), e);
            }
        }
        return weeklyStockPriceList;
    }


    @Override
    public void processAndResearchTechnicals() {
        List<Stock> stockList = stockService.getActiveStocks();
        int availableCores = Runtime.getRuntime().availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(availableCores);

        for (Stock stock : stockList) {
            try{
            executor.submit(() -> processTechnicalsBatch(stock));
            miscUtil.delay(800);
        }catch(Exception e){
            log.error("{} An eoor occurd while processing monthly batch",stock.getNseSymbol(), e);
        }
        }

        executor.shutdown();  // Shut down the executor after all tasks are submitted
    }


    private void processTechnicalsBatch(Stock stock) {

        if (calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
            try{
            StockTechnicals stockTechnicals = updateTechnicalsService.build(Timeframe.MONTHLY, stock, miscUtil.currentDate());
            updateTechnicalsService.updateTechnicals(Timeframe.MONTHLY, stock, stockTechnicals);
            researchExecutorService.executeTechnical(Timeframe.MONTHLY, stock, miscUtil.currentDate());
            miscUtil.delay(160);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing monthly batch",stock.getNseSymbol(), e);
            }
        }

        if (calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
            try{
            StockTechnicals stockTechnicals = updateTechnicalsService.build(Timeframe.WEEKLY, stock, miscUtil.currentDate());
            updateTechnicalsService.updateTechnicals(Timeframe.WEEKLY, stock, stockTechnicals);
            researchExecutorService.executeTechnical(Timeframe.WEEKLY, stock, miscUtil.currentDate());
            miscUtil.delay(160);
            }catch(Exception e){
                log.error("{} An eoor occurd while processing weekly batch",stock.getNseSymbol(), e);
            }
        }

        try{
        StockTechnicals stockTechnicals = updateTechnicalsService.build(Timeframe.DAILY, stock, miscUtil.currentDate());
        updateTechnicalsService.updateTechnicals(Timeframe.DAILY, stock, stockTechnicals);
        researchExecutorService.executeTechnical(Timeframe.DAILY, stock, miscUtil.currentDate());
        miscUtil.delay(160);
        }catch(Exception e){
            log.error("{} An eoor occurd while processing daily batch",stock.getNseSymbol(), e);
        }

    }

}
