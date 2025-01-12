package com.example.service;

import javax.transaction.Transactional;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.util.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.repo.stocks.StockPriceRepository;

@Transactional
@Service
public class StockPriceService {

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@Autowired
	private FormulaService formulaService;
	public double changePer(StockPrice stockPrice){

		return formulaService.percentageChange(stockPrice.getPrevClose(), stockPrice.getClose());
	}
}
