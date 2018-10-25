package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.master.Stock;
import com.example.repo.StockRepository;

@RestController
@RequestMapping("/stocks")
public class StockController {

	@Autowired
	private StockRepository stockRepository;
	
	@GetMapping(value="/", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<Stock>> getStocksMaster() {

		return ResponseEntity.ok(stockRepository.findAll());
	}
}
