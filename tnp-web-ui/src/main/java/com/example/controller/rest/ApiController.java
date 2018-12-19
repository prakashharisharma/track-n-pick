package com.example.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.service.PortfolioService;
import com.example.service.ResearchLedgerService;
import com.example.service.UserService;
import com.example.service.WatchListService;
import com.example.ui.model.UIRenderStock;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api")
public class ApiController {

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private ResearchLedgerService researchLedgerService;
	
	@Autowired
	private WatchListService watchListService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UiRenderUtil uiRenderUtil;
	
	@GetMapping(value="/portfolio/", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getPortfolioStocks() {

		User user = userService.getUserById(1);
		
		List<UserPortfolio> userPortfolioList = portfolioService.userPortfolio(user);
		
		return ResponseEntity.ok(uiRenderUtil.renderPortfolio(userPortfolioList));
	}
	
	@GetMapping(value="/research/", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getResearchStocks() {

		List<ResearchLedger> researchList = researchLedgerService.researchStocks();
		
		return ResponseEntity.ok(uiRenderUtil.renderResearchList(researchList));
	}
	
	@GetMapping(value="/watchlist/", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getWatchListStocks() {

		User user = userService.getUserById(1);
		
		List<Stock> userWatchList = watchListService.userWatchList(user);
		
		return ResponseEntity.ok(uiRenderUtil.renderWatchList(userWatchList));
	}
	
}
