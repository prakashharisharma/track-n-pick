package com.example.api.controller.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.example.transactional.model.master.Stock;
import com.example.transactional.repo.master.StockRepository;
import com.example.service.StockService;

import com.example.util.FormulaService;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockService stockService;

	@Autowired
	private FormulaService formulaService;
	
	@GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Stock>> getStocksMaster() {

		return ResponseEntity.ok(stockRepository.findAll());
	}

}
