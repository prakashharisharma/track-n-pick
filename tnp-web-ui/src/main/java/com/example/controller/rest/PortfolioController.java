package com.example.controller.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.UserProfile;
import com.example.security.LoginService;
import com.example.service.PortfolioService;
import com.example.service.StockService;
import com.example.service.UserService;
import com.example.ui.model.StockSearch;
import com.example.ui.model.UIRenderStock;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private StockService stockService;
	
	
	@Autowired
	private LoginService loginService;
	
	@Autowired
	private UiRenderUtil uiRenderUtil;
	
	@GetMapping(value="/current", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getPortfolioStocks() {

		
		List<UserPortfolio> userPortfolioList = portfolioService.userPortfolio(loginService.getLoginUserProfile());
		
		return ResponseEntity.ok(uiRenderUtil.renderPortfolio(userPortfolioList));
	}
	
	
	@PostMapping(value = "/addstock", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addStock(@RequestBody UIRenderStock stock) {

		System.out.println(stock);
		

		if(stock.getStockid() > 0) {
		
			Stock addStock =  stockService.getStockById(stock.getStockid());
			portfolioService.addStock(loginService.getLoginUserProfile(), addStock, stock.getBuySellPrice(), stock.getQunatity());
			
		}
		

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping(value = "/sellstock", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> sellStock(@RequestBody UIRenderStock stock) {

		
		if(stock.getStockid() > 0) {
		
			Stock sellStock =  stockService.getStockById(stock.getStockid());
			portfolioService.sellStock(loginService.getLoginUserProfile(), sellStock, stock.getBuySellPrice(), stock.getQunatity());
			
		}

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@GetMapping(value = "/searchstock", produces = {MediaType.APPLICATION_ATOM_XML_VALUE,  MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<List<StockSearch>> searchStock1(@RequestParam String query) {

		List<StockSearch> stocksList = new ArrayList<>();

		
		List<UserPortfolio> userPortfolioList = portfolioService.userPortfolio(loginService.getLoginUserProfile());
		
		userPortfolioList.forEach(u -> {
			stocksList.add(new StockSearch(u.getStock().getStockId(), u.getStock().getCompanyName() + " - ["+u.getStock().getNseSymbol() +"]"));
		} );
		
		System.out.println("searchStock" + query);

		System.out.println(stocksList);

		List<StockSearch> searchResult = stocksList.stream().filter(s -> s.getCompanyNameAndSymbol().contains(query)).collect(Collectors.toList());
		
		System.out.println(searchResult);
		
		if(searchResult.isEmpty()) {
			System.out.println("DEFAULT");
			
			List<StockSearch> noSearchResult = new ArrayList<>();
			
			noSearchResult.add(new StockSearch(1,"No Result Found"));
			System.out.println(noSearchResult);	
			return ResponseEntity.ok(noSearchResult);
		}
		
		
		return ResponseEntity.ok(searchResult);
	}
	
	
	
}
