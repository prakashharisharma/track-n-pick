package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.repo.StockPriceRepository;

@Transactional
@Service
public class StockPriceService {

	@Autowired
	private StockPriceRepository stockPriceRepository;
}
