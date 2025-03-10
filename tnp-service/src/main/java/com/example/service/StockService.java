package com.example.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import com.example.util.DownloadCounterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.external.chyl.service.CylhService;
import com.example.external.factor.FactorRediff;
import com.example.model.master.Sector;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.repo.master.StockRepository;
import com.example.repo.stocks.StockFactorRepository;
import com.example.repo.stocks.StockPriceRepository;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.io.model.StockIO;
import com.example.util.io.model.StockIO.IndiceType;
import com.example.util.rules.RulesNotification;

@Transactional
@Service
public class StockService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@Autowired
	private StockFactorRepository stockFactorRepository;

	@Autowired
	private FactorRediff factorRediff;
	@Autowired
	private FormulaService formulaService;
	@Autowired
	private CylhService cylhService;

	@Autowired
	private RulesNotification notificationRules;

	@Autowired
	private MiscUtil miscUtil;

	private static List<Stock> allstocks = null;

	public Stock getStockByIsinCode(String isinCode) {
		return stockRepository.findByIsinCode(isinCode);
	}

	public Stock getStockByNseSymbol(String nseSymbol) {
		return stockRepository.findFirstByNseSymbol(nseSymbol);
	}

	public Stock getStockById(long stockId) {
		return stockRepository.findByStockId(stockId);
	}

	public List<Stock> getActiveStocks() {
		return stockRepository.findByActive(true);
	}


	public Stock save(Stock stock) {
		return stockRepository.save(stock);
	}

	public void updateCurrentPrice(Stock stock, double currentPrice) {

		StockPrice stockPrice = stock.getStockPrice();

		if (stockPrice != null) {
			stockPrice.setCurrentPrice(currentPrice);
		} else {
			stockPrice = new StockPrice();
			stockPrice.setCurrentPrice(currentPrice);
			stockPrice.setStock(stock);
		}

		stock.setStockPrice(stockPrice);

		stockRepository.save(stock);

		LOGGER.info("CURRENT PRICE UPDATED :" + stock.getNseSymbol() + " : " + currentPrice);
	}

	public void updateCylhPrice(Stock stock) {

		StockPrice stockPrice = cylhService.getChylPrice(stock);

		stockPriceRepository.save(stockPrice);
	}

	public List<Stock> nifty500(){
		
		List<StockIO.IndiceType> indiceList = new ArrayList<>();
		
		indiceList.add(StockIO.IndiceType.NIFTY50);
		indiceList.add(StockIO.IndiceType.NIFTY100);
		indiceList.add(StockIO.IndiceType.NIFTY250);
		indiceList.add(StockIO.IndiceType.NIFTY500);
		
		List<Stock> stockList = stockRepository.findByActiveAndPrimaryIndiceIn(true, indiceList);
		
		return stockList;
	}
	
	public List<Stock> nifty50(){
		
		List<StockIO.IndiceType> indiceList = new ArrayList<>();
		
		indiceList.add(StockIO.IndiceType.NIFTY50);
		
		List<Stock> stockList = stockRepository.findByActiveAndPrimaryIndiceIn(true, indiceList);
		
		return stockList;
	}
	
	public List<Stock> nifty100(){
		
		List<StockIO.IndiceType> indiceList = new ArrayList<>();
		
		indiceList.add(StockIO.IndiceType.NIFTY50);
		indiceList.add(StockIO.IndiceType.NIFTY100);
		
		List<Stock> stockList = stockRepository.findByActiveAndPrimaryIndiceIn(true, indiceList);
		
		return stockList;
	}
	
	public List<Stock> activeStocks() {

		if (allstocks == null) {
			allstocks = stockRepository.findByActive(true);
		}

		return allstocks;
	}

	public List<Stock> getForActivity() {
		return stockRepository.findByActivityCompleted(Boolean.FALSE);
	}

	public void setInactive(List<Stock> discontinueList) {

		for (Stock stock : discontinueList) {
			stock.setActive(false);
			stockRepository.save(stock);
		}
	}

	public void resetMaster() {
		List<Stock> stocksList = stockRepository.findByActive(true);
		for (Stock stock : stocksList) {
			stock.setActive(false);
			stock.setPrimaryIndice(StockIO.IndiceType.NIFTY1500);
			stockRepository.save(stock);
		}
	}
	
	public void resetFactors() {
		
		List<Stock> stocksList = stockRepository.findByActive(true);
		
		int i=0;
		
		for (Stock stock : stocksList) {
			i++;
			
			StockFactor stockFactor = stock.getFactor();
			
			stockFactor.setLastModified(LocalDate.now().minusDays(20 + i));
			
			stockFactorRepository.save(stockFactor);
			
			if(i == 20) {
				i=0;
			}
			
		}
		
	}
	
	public Stock add(StockIO.Exchange exchange, String series, String isinCode, String companyName, String nseSymbol, String bseCode, IndiceType primaryIndice,
					 Sector sectorName) {

		Stock stock = new Stock(exchange, isinCode, companyName, nseSymbol,  primaryIndice, sectorName);

		if(bseCode!=null && !bseCode.isBlank()){
			stock.setBseCode(bseCode);
		}

		stock.setSeries(series);
		stock.setActive(Boolean.TRUE);

		stock = stockRepository.save(stock);

		//this.updatePrice(stock);
		
		//this.updateFactor(stock);

		return stock;
	}

	public boolean isActive(String nseSymbol) {

		boolean isActive = false;

		Stock stock = this.getStockByNseSymbol(nseSymbol);

		if (stock != null && stock.isActive()) {
			isActive = true;
		}

		return isActive;
	}


	
	public StockPrice updatePrice(Stock stock) {
		StockPrice stockPrice = stock.getStockPrice();
		
		if(stockPrice == null) {
			stockPrice = new StockPrice();
			
			stockPrice.setStock(stock);
			
			stockPriceRepository.save(stockPrice);
		}
		
		
		return stockPrice;
	}
	
	public StockFactor updateFactor(Stock stock) {

		LOGGER.info("Updating Factor for : " + stock.getNseSymbol());

		StockFactor stockFactor = stock.getFactor();

		if (stock.getFactor() != null) {

			if (DAYS.between(stock.getFactor().getLastModified(), LocalDate.now()) > notificationRules.getFactorIntervalDays()) {

				//try {

					if(DownloadCounterUtil.get() < notificationRules.getApiCallCounter()) {

						//long interval = miscUtil.getInterval();

						//Thread.sleep(interval);

						stockFactor = factorRediff.getFactor(stock);

						stockFactorRepository.save(stockFactor);

						DownloadCounterUtil.increment();
					}else{
						LOGGER.warn("Counter exceeds {} for {} ", DownloadCounterUtil.get(), stock.getNseSymbol());
					}
					
				//} catch (InterruptedException e) {
				//	LOGGER.error("An error occured while updating factor {}", stock.getNseSymbol(), e);
				//}

			}else{
				LOGGER.info("Factors recently updated.. {}", stock.getNseSymbol());
			}
		} else {

			stockFactor = factorRediff.getFactor(stock);

			stockFactorRepository.save(stockFactor);

		}

		return stockFactor;
	}

	public double getPe(Stock stock) {

		StockPrice stockPrice = stock.getStockPrice();

		double currentPrice = stockPrice.getCurrentPrice();

		double eps = stock.getFactor().getEps();

		double pe = formulaService.calculatePe(currentPrice, eps);

		return pe;

	}

	public double getPb(Stock stock) {

		StockPrice stockPrice = stock.getStockPrice();

		double currentPrice = stockPrice.getCurrentPrice();

		double bookValue = stock.getFactor().getBookValue();

		double pb = formulaService.calculatePb(currentPrice, bookValue);

		return pb;
	}

	
	
}
