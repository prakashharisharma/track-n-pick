package com.example.external.dylh.service;

import java.util.List;

import com.example.model.master.Stock;

/**
 * 
 * This service return DYLH (Daily Year Low High)
 * 
 * @author phsharma
 *
 */

public interface DylhService {

	List<Stock> yearLowStocks();
	
	List<Stock> yearHighStocks();
}
