package com.example.open.api.controller.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.RuleService;
import com.example.service.StockService;
import com.example.ui.model.StockDetailsIO;
import com.example.ui.model.StockSearch;
import com.example.util.FormulaService;

@RestController
@RequestMapping("/public/api/stocks")
public class PublicStockController {

	@Autowired
	private StockService stockService;

	@Autowired
	private FormulaService formulaService;
	
	@Autowired
	private RuleService ruleService;
	
	@GetMapping(value = "/{stockId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getStockFundamentals(@PathVariable long stockId) {

		Stock stock = stockService.getStockById(stockId);
		
		if(stock != null) {
		
		StockPrice stockPrice = stock.getStockPrice();
		
		StockFactor stockFactor = stock.getStockFactor();

		StockTechnicals stockTechnicals = stock.getTechnicals();
		
		double pe = formulaService.calculatePe(stockPrice.getCurrentPrice(), stockFactor.getEps());
		
		double pb = formulaService.calculatePb(stockPrice.getCurrentPrice(), stockFactor.getBookValue());
		
		String valuation = "NUETRAL";
		
		if(ruleService.isUndervalued(stock)) {
			valuation = "UNDERVALUE";
		}else if(ruleService.isOvervalued(stock)) {
			valuation = "OVERVALUED";
		}else {
			valuation = "NUETRAL";
		}
		
		StockDetailsIO StockDetails = new com.example.ui.model.StockDetailsIO(stock.getNseSymbol(), stock.getSector().getSectorName(), stockPrice.getCurrentPrice(), stockPrice.getYearLow(), stockPrice.getYearHigh(), stockFactor.getMarketCap(), stockFactor.getDebtEquity(), stockFactor.getCurrentRatio(), stockFactor.getQuickRatio(), stockFactor.getDividend(), pb, pe, stock.getSector().getSectorPe(), stockFactor.getReturnOnEquity(), stockFactor.getReturnOnCapital(), stockTechnicals.getRsi(), stockTechnicals.getLongTermTrend(), stockTechnicals.getMidTermTrend(), stockTechnicals.getCurrentTrend(),valuation);
		
		return ResponseEntity.ok(StockDetails);
		}else {
			return ResponseEntity.ok("NOT FOUND");
		}
	}
	
	@GetMapping(value = "/searchstock", produces = { MediaType.APPLICATION_ATOM_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<StockSearch>> searchStock1(@RequestParam String query) {

		List<StockSearch> stocksList = new ArrayList<>();

		List<Stock> stocksMasterList = stockService.activeStocks();

		stocksMasterList.forEach(s -> {
			stocksList.add(new StockSearch(s.getStockId(), s.getCompanyName() + " - [" + s.getNseSymbol() + "]"));
		});

		List<StockSearch> searchResult = stocksList.stream().filter(s -> s.getCompanyNameAndSymbol().toLowerCase().contains(query.toLowerCase()))
				.collect(Collectors.toList());

		if (searchResult.isEmpty()) {

			List<StockSearch> noSearchResult = new ArrayList<>();
			
			noSearchResult.add(new StockSearch(0, "No Result Found"));
			
			return ResponseEntity.ok(noSearchResult);
		}

		return ResponseEntity.ok(searchResult);
	}
}
