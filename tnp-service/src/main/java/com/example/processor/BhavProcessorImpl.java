package com.example.processor;

import com.example.data.common.type.Timeframe;
import com.example.data.storage.documents.StockPrice;
import com.example.data.storage.documents.StockTechnicals;
import com.example.data.storage.repo.PriceTemplate;
import com.example.data.transactional.entities.Stock;
import com.example.dto.io.StockIO;
import com.example.dto.io.StockPriceIN;
import com.example.dto.io.StockPriceIO;
import com.example.model.type.Exchange;
import com.example.model.type.IndiceType;
import com.example.service.*;
import com.example.service.UpdatePriceService;
import com.example.util.MiscUtil;
import com.example.util.ThreadsUtil;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BhavProcessorImpl implements BhavProcessor {

    private final StockService stockService;

    private final SectorService sectorService;

    private final CalendarService calendarService;

    private final UpdatePriceService updatePriceService;

    private final UpdateTechnicalsService updateTechnicalsService;

    private final ResearchExecutorService researchExecutorService;

    private final PriceTemplate priceTemplate;

    private final MiscUtil miscUtil;

    /**
     * 1. Filter 2. Insert bulk in price_history 3. If list size and write result is same process to
     * kafka
     *
     * @param stockPriceList
     */
    @Override
    public void process(List<StockPriceIN> stockPriceList) {

        List<StockPriceIN> regularEquityList = this.filterRegularEquityShares(stockPriceList);

        List<StockPriceIO> stockPriceIOList = this.transform(regularEquityList);

        try {
            this.importAndProcessDailyPrice(stockPriceIOList);

            ThreadsUtil.delay();

            this.processTimeframePrice();

            ThreadsUtil.delay();

            this.processAndResearchTechnicals();

            log.info("Completed Bhav Processor");

        } catch (Exception e) {
            log.error("An error occurred while processing bhav");
        }
    }

    public List<StockPriceIN> filterRegularEquityShares(List<StockPriceIN> records) {

        Map<String, StockPriceIN> uniqueStocks = new HashMap<>();

        for (StockPriceIN record : records) {
            String symbol = record.getNseSymbol();
            String series = record.getSeries();

            // Ignore Rights Issue (-RI) stocks
            if (symbol.endsWith("-RI") || symbol.endsWith("-RE") || symbol.endsWith("-RE1")) {
                continue;
            }

            // Prioritize EQ > BE > BZ (one entry per symbol)
            if ("EQ".equals(series)
                    || ("BE".equals(series) && !uniqueStocks.containsKey(symbol))
                    || ("BZ".equals(series) && !uniqueStocks.containsKey(symbol))) {
                uniqueStocks.put(symbol, record);
            }
        }

        return new ArrayList<>(uniqueStocks.values());
    }

    private List<StockPriceIO> transform(List<StockPriceIN> stockPriceINList) {

        List<StockPriceIO> stockPriceIOList = new ArrayList<>();

        for (StockPriceIN stockPriceIN : stockPriceINList) {
            StockPriceIO stockPriceIO =
                    new StockPriceIO(
                            stockPriceIN.getSource().toUpperCase(),
                            stockPriceIN.getCompanyName(),
                            stockPriceIN.getNseSymbol(),
                            stockPriceIN.getSeries(),
                            stockPriceIN.getOpen(),
                            stockPriceIN.getHigh(),
                            stockPriceIN.getLow(),
                            stockPriceIN.getClose(),
                            stockPriceIN.getLast(),
                            stockPriceIN.getPrevClose(),
                            stockPriceIN.getTottrdqty(),
                            stockPriceIN.getTottrdval(),
                            stockPriceIN.getTimestamp(),
                            stockPriceIN.getTotaltrades(),
                            stockPriceIN.getIsin());
            stockPriceIOList.add(stockPriceIO);
        }

        return stockPriceIOList;
    }

    private void updateSeries(Stock stock, StockPriceIO stockPriceIO) {
        if (stock.getSeries() == null
                || !stock.getSeries()
                        .equalsIgnoreCase(stockPriceIO.getSeries().trim().toUpperCase())) {
            stock.setSeries(stockPriceIO.getSeries().trim().toUpperCase());
            stockService.save(stock);
        }
    }

    private Stock addStockToMaster(StockPriceIO stockPriceIO) {

        log.info("{} Adding to master", stockPriceIO.getNseSymbol());

        StockIO stockIO =
                new StockIO(
                        stockPriceIO.getCompanyName(),
                        "NIFTY",
                        stockPriceIO.getNseSymbol().trim().toUpperCase(),
                        stockPriceIO.getSeries(),
                        stockPriceIO.getIsin(),
                        IndiceType.NSE);

        if (stockPriceIO.getExchange().equalsIgnoreCase("NSE")) {
            stockIO.setSector("NSE");
            stockIO.setIndice(IndiceType.NSE);
            stockIO.setExchange(Exchange.NSE);
        }

        log.info("{} Added to master", stockPriceIO.getNseSymbol());
        return stockService.add(
                stockIO.getExchange(),
                stockIO.getSeries().trim().toUpperCase(),
                stockIO.getIsin(),
                stockIO.getCompanyName(),
                stockIO.getNseSymbol(),
                stockIO.getBseCode(),
                stockIO.getIndice(),
                sectorService.getOrAddSectorByName(stockIO.getSector()));
    }

    private void importAndProcessDailyPrice(List<StockPriceIO> stockPriceIOList) {
        int numThreads = ThreadsUtil.poolSize();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Future<List<StockPrice>>> futures = new ArrayList<>();

        // Split the data into batches for parallel processing
        int batchSize = (int) Math.ceil((double) stockPriceIOList.size() / numThreads);

        for (int i = 0; i < stockPriceIOList.size(); i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, stockPriceIOList.size());
            List<StockPriceIO> batch = stockPriceIOList.subList(start, end);

            try {
                // Submit batch processing tasks
                Future<List<StockPrice>> future = executor.submit(() -> processBatch(batch));
                futures.add(future);
                miscUtil.delay(numThreads * 16);
            } catch (Exception e) {
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

            long count = priceTemplate.delete(Timeframe.DAILY, miscUtil.currentDate());

            try {
                miscUtil.delay(25);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            log.info("Deleted {} existing entries for {}", count, miscUtil.currentDate());

            long insertCount = priceTemplate.create(allStockPrices);

            if (insertCount == allStockPrices.size()) {
                // Here we can process updateFactors
                // sendToUpdateQueue(stockPriceIOList, stockPriceIOList.get(stockPriceIOList.size()
                // - 1).getNseSymbol());
            }
        }

        // Shutdown executor
        executor.shutdown();
    }

    // Function to process a batch of StockPriceIO
    private List<StockPrice> processBatch(List<StockPriceIO> stockPriceIOList) {
        List<StockPrice> stockPriceList = new ArrayList<>();

        for (StockPriceIO stockPriceIO : stockPriceIOList) {
            try {
                StockPrice stockPrice = updatePriceService.build(stockPriceIO);
                Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

                if (stock == null) {
                    stock = this.addStockToMaster(stockPriceIO);
                }

                this.updateSeries(stock, stockPriceIO);
                updatePriceService.updatePrice(Timeframe.DAILY, stock, stockPrice);
                researchExecutorService.executeFundamental(stock);
                stockPriceList.add(stockPrice);
                miscUtil.delay(ThreadsUtil.poolSize() * 8);
            } catch (Exception e) {
                log.error(
                        "{} An error occurred while processing daily batch",
                        stockPriceIO.getNseSymbol(),
                        e);
            }
        }

        return stockPriceList;
    }

    @Override
    public void processTimeframePrice() {
        List<Stock> stockList = stockService.getActiveStocks();
        if (stockList.isEmpty()) {
            log.warn("No active stocks found to process.");
            return;
        }

        LocalDate today = miscUtil.currentDate();
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        boolean isLastWeek = calendarService.isLastTradingSessionOfWeek(today);
        boolean isLastMonth = calendarService.isLastTradingSessionOfMonth(today);
        boolean isLastQuarter = calendarService.isLastTradingSessionOfQuarter(today);
        boolean isLastYear = calendarService.isLastTradingSessionOfYear(today);

        Map<Timeframe, List<Future<List<StockPrice>>>> futuresMap = new EnumMap<>(Timeframe.class);
        for (Timeframe tf : Timeframe.values()) {
            futuresMap.put(tf, new ArrayList<>());
        }

        int batchSize = (int) Math.ceil((double) stockList.size() / numThreads);

        for (int i = 0; i < stockList.size(); i += batchSize) {
            List<Stock> batch = stockList.subList(i, Math.min(i + batchSize, stockList.size()));

            submitIfRequired(executor, futuresMap, Timeframe.YEARLY, isLastYear, batch);
            submitIfRequired(executor, futuresMap, Timeframe.QUARTERLY, isLastQuarter, batch);
            submitIfRequired(executor, futuresMap, Timeframe.MONTHLY, isLastMonth, batch);
            submitIfRequired(executor, futuresMap, Timeframe.WEEKLY, isLastWeek, batch);

            try {
                ThreadsUtil.delay(numThreads * 128); // optional single delay per batch
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (Map.Entry<Timeframe, List<Future<List<StockPrice>>>> entry : futuresMap.entrySet()) {
            Timeframe timeframe = entry.getKey();
            List<Future<List<StockPrice>>> futures = entry.getValue();

            List<StockPrice> allPrices = new ArrayList<>();
            for (Future<List<StockPrice>> future : futures) {
                try {
                    allPrices.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error while collecting results for " + timeframe + " prices", e);
                }
            }

            if (!allPrices.isEmpty()) {

                long count = priceTemplate.delete(timeframe, today);

                try {
                    miscUtil.delay(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                log.info("Deleted {} existing entries for {}", count, today);

                long insertCount = priceTemplate.create(timeframe, allPrices);

                if (insertCount == allPrices.size()) {
                    log.info("{} Price Updated", timeframe);
                } else {
                    log.warn(
                            "{} Price insert mismatch: expected {}, inserted {}",
                            timeframe,
                            allPrices.size(),
                            insertCount);
                }
            }
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void submitIfRequired(
            ExecutorService executor,
            Map<Timeframe, List<Future<List<StockPrice>>>> futuresMap,
            Timeframe timeframe,
            boolean condition,
            List<Stock> batch) {

        if (!condition) return;

        try {
            Future<List<StockPrice>> future = executor.submit(() -> processBatch(timeframe, batch));
            futuresMap.get(timeframe).add(future);
        } catch (Exception e) {
            log.error("Error while submitting {} batch", timeframe, e);
        }
    }

    private List<StockPrice> processBatch(Timeframe timeframe, List<Stock> stockBatch) {
        List<StockPrice> stockPrices = new ArrayList<>();
        for (Stock stock : stockBatch) {
            try {
                StockPrice stockPrice =
                        updatePriceService.build(timeframe, stock, miscUtil.currentDate());
                updatePriceService.updatePrice(timeframe, stock, stockPrice);
                stockPrices.add(stockPrice);
            } catch (Exception e) {
                log.error(
                        "Error processing {} batch for stock {}",
                        timeframe,
                        stock.getNseSymbol(),
                        e);
            }
        }
        return stockPrices;
    }

    @Override
    public void processAndResearchTechnicals() {
        List<Stock> stockList = stockService.getActiveStocks();

        int maxConcurrentThreads = ThreadsUtil.poolSize();
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrentThreads);

        for (Stock stock : stockList) {
            executor.submit(
                    () -> {
                        try {
                            processTechnicalsBatch(stock);
                        } catch (Exception e) {
                            log.error("{} Error processing technicals", stock.getNseSymbol(), e);
                        }
                    });

            // Delay *between* submissions to prevent MongoDB bursts
            try {
                ThreadsUtil.delay(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();
    }

    private void processTechnicalsBatch(Stock stock) {
        LocalDate today = miscUtil.currentDate();

        if (calendarService.isLastTradingSessionOfMonth(today)) {
            processOne(Timeframe.MONTHLY, stock, today);
        }

        if (calendarService.isLastTradingSessionOfWeek(today)) {
            processOne(Timeframe.WEEKLY, stock, today);
        }

        processOne(Timeframe.DAILY, stock, today);
    }

    private void processOne(Timeframe timeframe, Stock stock, LocalDate date) {
        try {
            StockTechnicals stockTechnicals = updateTechnicalsService.build(timeframe, stock, date);
            updateTechnicalsService.updateTechnicals(timeframe, stock, stockTechnicals);
            researchExecutorService.executeTechnical(timeframe, stock, date);
        } catch (Exception e) {
            log.error("{} Error processing {} batch", stock.getNseSymbol(), timeframe, e);
        }
    }
}
