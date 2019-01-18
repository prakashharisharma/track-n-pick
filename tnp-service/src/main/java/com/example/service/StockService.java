package com.example.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.CylhService;
import com.example.factor.FactorRediff;
import com.example.model.master.Sector;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.repo.master.StockRepository;
import com.example.repo.stocks.StockFactorRepository;
import com.example.repo.stocks.StockPriceRepository;
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
	private CylhService cylhService;

	@Autowired
	private RulesNotification notificationRules;

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

	public void setNifty50Inactive(List<Stock> discontinueList) {

		for (Stock stock : discontinueList) {
			stock.setNifty50(false);
			stockRepository.save(stock);
		}
	}

	public void setNifty50(Stock stock) {

		stock.setNifty50(true);
		stockRepository.save(stock);

	}

	public Stock add(String isinCode, String companyName, String nseSymbol, String sector) {
		// Stock stock = new Stock(isinCode, companyName, nseSymbol,
		// sectorService.getOrAddSectorByName(sector));
		Stock stock = new Stock(isinCode, companyName, nseSymbol, sector);
		stockRepository.save(stock);
		return stock;
	}

	public Stock add(String isinCode, String companyName, String nseSymbol, Sector sectorName) {
		// Stock stock = new Stock(isinCode, companyName, nseSymbol,
		// sectorService.getOrAddSectorByName(sector));
		Stock stock = new Stock(isinCode, companyName, nseSymbol, sectorName);
		stockRepository.save(stock);
		return stock;
	}

	public void updateNifty50PriceAndFactor() {

		List<Stock> nift50List = this.getNifty50ActiveStocks();

		nift50List.forEach(stock -> {

			if (stock.getStockPrice().getLastModified().isBefore(LocalDate.now())) {
				LOGGER.info("Updating Price for : " + stock.getNseSymbol());
				StockPrice stockPrice = cylhService.getChylPrice(stock);

				stockPriceRepository.save(stockPrice);
			}

			if (DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > notificationRules
					.getFactorIntervalDays()) {

				LOGGER.info("Updating Factor for : " + stock.getNseSymbol());

				StockFactor stockFactor = factorRediff.getFactor(stock);

				stockFactorRepository.save(stockFactor);

			}

		});
	}

	/*
	 * public void temp() { List<Stock> stk = stockRepository.findAll();
	 * 
	 * for(Stock s : stk) { s.getSector();
	 * 
	 * Sector sc= sectorService.getSectorByName(s.getSector());
	 * 
	 * s.setSectorName(sc); stockRepository.save(s);
	 * 
	 * } }
	 */
}
