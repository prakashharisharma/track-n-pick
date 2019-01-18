package com.example.storage.service;

import java.time.LocalDate;
import java.util.List;

import com.example.storage.model.Stock;
import com.example.storage.model.StockPrice;



public interface StorageService {

    Stock findByNseSymbol(String nseSymbol);

    List<StockPrice> findPriceByNseSymbol(String nseSymbol);
    double getSMA(String nseSymbol, int days);
    
     double getRSI(String nseSymbol, int days);
    
    void deleteAll();

    List<Stock> findAll();

    Stock saveStock(Stock stock);
    
    public void addStock(String isinCode, String companyName, String nseSymbol, String bseCode, String sectorName);
    
    public void updatePrice(String nseSymbol,double open, double high, double low, double close, double last, double prevClose,
			long totalTradedQuantity, double totalTradedValue, long totalTrades, String bhavDate);
}
