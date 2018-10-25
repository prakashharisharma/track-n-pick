package com.example.chyl.service;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;

public interface CylhService {

	StockPrice getChylPrice(Stock stock);
	
}
