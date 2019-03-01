package com.example.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

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
		return stockRepository.findByNseSymbol(nseSymbol);
	}

	public Stock getStockById(long stockId) {
		return stockRepository.findByStockId(stockId);
	}

	public List<Stock> getActiveStocks() {
		return stockRepository.findByActive(true);
	}

	public List<Stock> getNifty50ActiveStocks() {
		return stockRepository.findByNifty50AndActive(true, true);
	}

	public List<Stock> getNifty200ActiveStocks() {
		return stockRepository.findByNifty200AndActive(true, true);
	}

	public List<Stock> getNifty200ExcludeNifty50ActiveStocks() {
		return stockRepository.findByNifty50AndNifty200AndActive(false, true, true);
	}

	public List<Stock> getNifty500ActiveStocks() {
		return stockRepository.findByNifty50AndNifty200AndActive(false, false, true);
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

	public List<Stock> activeStocks() {

		if (allstocks == null) {
			allstocks = stockRepository.findByActive(true);
		}

		return allstocks;
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
			stock.setNifty50(false);
			stock.setNifty200(false);
			stockRepository.save(stock);
		}
	}
	
	public void setNifty50Inactive(List<Stock> discontinueList) {

		for (Stock stock : discontinueList) {
			stock.setNifty50(false);
			stockRepository.save(stock);
		}
	}

	public void setNifty200Inactive(List<Stock> discontinueList) {

		for (Stock stock : discontinueList) {
			stock.setNifty200(false);
			stockRepository.save(stock);
		}
	}

	public void setNifty50(Stock stock) {

		stock.setNifty50(true);
		stockRepository.save(stock);

	}

	public void setNifty200(Stock stock) {

		stock.setNifty200(true);
		stockRepository.save(stock);

	}

	public Stock add(String isinCode, String companyName, String nseSymbol, IndiceType primaryIndice, Sector sectorName) {

		Stock stock = new Stock(isinCode, companyName, nseSymbol,primaryIndice, sectorName);
		stockRepository.save(stock);
		return stock;
	}

	

	public StockFactor updateFactor(Stock stock) {
		
		StockFactor stockFactor = stock.getStockFactor();

		if (stock.getStockFactor() != null) {

			if (DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > notificationRules
					.getFactorIntervalDays()) {

				try {
					Thread.sleep(miscUtil.getInterval());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				LOGGER.info("Updating Factor for : " + stock.getNseSymbol());

				stockFactor = factorRediff.getFactor(stock);

				stockFactorRepository.save(stockFactor);

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

		double eps = stock.getStockFactor().getEps();

		double pe= formulaService.calculatePe(currentPrice, eps);
		
		return pe;
		
	}
	
	public double getPb(Stock stock) {
		
		StockPrice stockPrice = stock.getStockPrice();

		double currentPrice = stockPrice.getCurrentPrice();

		double bookValue = stock.getStockFactor().getBookValue();

		double pb= formulaService.calculatePb(currentPrice, bookValue);
		
		return pb;
	}
	
}
