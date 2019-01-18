package com.example.storage.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.storage.model.Stock;
import com.example.storage.model.StockPrice;
import com.example.storage.repo.StorageRepository;
import com.example.storage.repo.StorageTemplate;

@Service
public class StorageServiceImpl implements StorageService {

	@Autowired
	private StorageRepository stockRepository;

	@Autowired
	private StorageTemplate storageTemplate;

	@Override
	public Stock findByNseSymbol(String nseSymbol) {

		return stockRepository.findByNseSymbol(nseSymbol);
	}

	@Override
	public void deleteAll() {
		stockRepository.deleteAll();
	}

	@Override
	public List<Stock> findAll() {
		// return stockRepository.findAll();
		return storageTemplate.findAll();
	}

	@Override
	public Stock saveStock(Stock stock) {
		return stockRepository.save(stock);
	}

	@Override
	public void addStock(String isinCode, String companyName, String nseSymbol, String bseCode, String sectorName) {
		Stock stock = this.findByNseSymbol(nseSymbol);

		if (stock != null) {
			stock.setCompanyName(companyName);
			stock.setBseCode(bseCode);
			stock.setSectorName(sectorName);
			
		} else {
			stock = new Stock(isinCode, companyName, nseSymbol, bseCode, sectorName);

			
		}
		stockRepository.save(stock);
	}

	@Override
	public void updatePrice(String nseSymbol, double open, double high, double low, double close, double last,
			double prevClose, long totalTradedQuantity, double totalTradedValue, long totalTrades, String bhavDateStr) {

		DateTimeFormatter formatter_1 = new DateTimeFormatterBuilder().parseCaseInsensitive()
				.appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH);

		LocalDate localdateBhavDate = LocalDate.parse(bhavDateStr, formatter_1);
		Instant bhavInstant = localdateBhavDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		
		StockPrice stockPrice = new StockPrice(open, high, low, close, last, prevClose, totalTradedQuantity,
				totalTradedValue, totalTrades, bhavInstant);

		storageTemplate.addPrice(nseSymbol, stockPrice);
	}

	@Override
	public List<StockPrice> findPriceByNseSymbol(String nseSymbol) {
		// TODO Auto-generated method stub
		return storageTemplate.findPriceByNseSymbol(nseSymbol);
	}

	@Override
	public double getSMA(String nseSymbol, int days) {
		
		double closeSum = storageTemplate.getSMA(nseSymbol, days);
		
		double movingAverage = closeSum / days;
		
		return movingAverage;
	}

	@Override
	public double getRSI(String nseSymbol, int days) {
		// TODO Auto-generated method stub
		return storageTemplate.getRSI(nseSymbol, days);
	}
	
	
}
