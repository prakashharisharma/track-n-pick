package com.example.storage.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.example.storage.model.deprecated.Stock;
import com.example.storage.model.deprecated.StockPriceD;

public interface StorageService {

    double getSMA(String nseSymbol, int days);
    
     double getRSI(String nseSymbol, int days);
     
     double getAverageGain(String nseSymbol, int days);
     
     double getAverageLoss(String nseSymbol, int days);
     
     double getyearHigh(String nseSymbol);
     
     double getyearLow(String nseSymbol);
    
     double getCurrentPrice(String nseSymbol);



    
    public void addStock(String isinCode, String companyName, String nseSymbol, String bseCode, String sectorName);
    
    public void updatePrice(String nseSymbol,double open, double high, double low, double close, double last, double prevClose,
			long totalTradedQuantity, double totalTradedValue, long totalTrades, Instant bhavInstant);
}
