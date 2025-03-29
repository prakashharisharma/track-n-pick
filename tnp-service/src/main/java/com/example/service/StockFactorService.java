package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.transactional.model.master.Stock;
import com.example.transactional.model.stocks.StockFactor;
import com.example.transactional.repo.stocks.StockFactorRepository;

@Transactional
@Service
public class StockFactorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StockFactorService.class);
	
	@Autowired
	private StockFactorRepository stockFactorRepository;
	
	public List<Stock> stocksToUpdateFactor(){
		
		List<StockFactor> factorList = stockFactorRepository.findAllByOrderByLastModifiedAsc();
		
		List<Stock> stockList = factorList.stream().map(sf -> sf.getStock()).filter(s -> s.isActive()).limit(25).collect(Collectors.toList());
		
		return stockList;
	}
	
}
