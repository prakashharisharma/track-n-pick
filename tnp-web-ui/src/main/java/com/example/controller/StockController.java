package com.example.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.repo.master.StockRepository;
import com.example.service.StockService;
import com.example.ui.model.StockSearch;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockService stockService;

	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Stock>> getStocksMaster() {

		return ResponseEntity.ok(stockRepository.findAll());
	}

	@GetMapping(value = "/searchstock", produces = { MediaType.APPLICATION_ATOM_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<StockSearch>> searchStock1(@RequestParam String query) {

		List<StockSearch> stocksList = new ArrayList<>();

		List<Stock> stocksMasterList = stockService.activeStocks();

		stocksMasterList.forEach(s -> {
			stocksList.add(new StockSearch(s.getStockId(), s.getCompanyName() + " - [" + s.getNseSymbol() + "]"));
		});

		List<StockSearch> searchResult = stocksList.stream().filter(s -> s.getCompanyNameAndSymbol().contains(query))
				.collect(Collectors.toList());

		if (searchResult.isEmpty()) {

			System.out.println("DEFAULT");
			
			List<StockSearch> noSearchResult = new ArrayList<>();
			
			noSearchResult.add(new StockSearch(0, "No Result Found"));
			
			return ResponseEntity.ok(noSearchResult);
		}

		return ResponseEntity.ok(searchResult);
	}
}
