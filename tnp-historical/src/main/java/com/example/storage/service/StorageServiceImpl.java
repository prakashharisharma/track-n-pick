package com.example.storage.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.storage.model.deprecated.Stock;
import com.example.storage.model.deprecated.StockPriceD;
import com.example.storage.repo.TradingSessionTemplate;

@Service
public class StorageServiceImpl implements StorageService {

	@Autowired
	private TradingSessionTemplate storageTemplate;


	@Override
	public void addStock(String isinCode, String companyName, String nseSymbol, String bseCode, String sectorName) {
		Stock stock = storageTemplate.findByNseSymbol(nseSymbol);

		if (stock != null) {
			stock.setCompanyName(companyName);
			stock.setBseCode(bseCode);
			stock.setSectorName(sectorName);
			
		} else {
			stock = new Stock(isinCode, companyName, nseSymbol, bseCode, sectorName);

			
		}
		storageTemplate.create(stock);
	}

	@Override
	public void updatePrice(String nseSymbol, double open, double high, double low, double close, double last,
			double prevClose, long totalTradedQuantity, double totalTradedValue, long totalTrades, Instant bhavInstant) {

/*		DateTimeFormatter formatter_1 = new DateTimeFormatterBuilder().parseCaseInsensitive()
				.appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH);

		LocalDate localdateBhavDate = LocalDate.parse(bhavDateStr, formatter_1);
		
		Instant bhavInstant = localdateBhavDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		*/
		StockPriceD stockPrice = new StockPriceD(open, high, low, close, last, prevClose, totalTradedQuantity,
				totalTradedValue, totalTrades, bhavInstant);

		storageTemplate.addPrice(nseSymbol, stockPrice);
	}

	@Override
	public double getSMA(String nseSymbol, int days) {
		
		double movingAverage = storageTemplate.getAveragePrice(nseSymbol, days);
		
		return movingAverage;
	}

	@Override
	public double getRSI(String nseSymbol, int days) {
		
		return storageTemplate.getRSI(nseSymbol, days);
	}

	@Override
	public double getyearHigh(String nseSymbol) {
		
		return storageTemplate.getyearHigh(nseSymbol);
	}

	@Override
	public double getyearLow(String nseSymbol) {
		
		return storageTemplate.getyearLow(nseSymbol);
	}

	@Override
	public double getCurrentPrice(String nseSymbol) {
		// TODO Auto-generated method stub
		return storageTemplate.getCurrentPrice(nseSymbol);
	}

	@Override
	public double getAverageGain(String nseSymbol, int days) {
		double averageGain = storageTemplate.getTotalGain(nseSymbol, days) / days;
		return averageGain;
	}

	@Override
	public double getAverageLoss(String nseSymbol, int days) {
		double averageLoss = Math.abs(storageTemplate.getTotalLoss(nseSymbol, days)) / days;
		return averageLoss;
	}


	
}
