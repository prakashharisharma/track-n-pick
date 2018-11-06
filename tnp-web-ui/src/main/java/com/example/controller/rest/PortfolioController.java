package com.example.controller.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.controller.model.Person;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.service.PortfolioService;
import com.example.service.UserService;
import com.example.ui.model.PortfolioStock;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private UiRenderUtil uiRenderUtil;
	
	private static List<Stock> stockList = new ArrayList<>();
	
	static {
		stockList.add(new Stock("ABC", "ABC", "25.23"));
		stockList.add(new Stock("XYZ", "XYZ", "19.23"));
		stockList.add(new Stock("DEF", "DEF", "25.23"));
		stockList.add(new Stock("ABC1", "ABC", "20.23"));
		stockList.add(new Stock("XYZ1", "XYZ", "15.23"));
		stockList.add(new Stock("DEF1", "DEF", "13.23"));
		stockList.add(new Stock("ABC2", "ABC", "25.23"));
		stockList.add(new Stock("XYZ2", "XYZ", "10.23"));
		stockList.add(new Stock("DEF2", "DEF", "15.23"));
		stockList.add(new Stock("ABC3", "ABC", "25.23"));
		stockList.add(new Stock("XYZ3", "XYZ", "35.23"));
		stockList.add(new Stock("DEF3", "DEF", "25.23"));
	}
	
	
	@GetMapping(value="/", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<PortfolioStock>> getPortfolioStocks() {

		User user = userService.getUserById(1);
		
		List<UserPortfolio> userPortfolioList = portfolioService.userPortfolio(user);
		
		return ResponseEntity.ok(uiRenderUtil.renderPortfolio(userPortfolioList));
	}
	
	@PostMapping(value = "/saveContact", consumes = MediaType.APPLICATION_JSON_VALUE)
	 public ResponseEntity<?> saveContact(@RequestBody Person person) {
		
		System.out.println(person);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	 }
}
