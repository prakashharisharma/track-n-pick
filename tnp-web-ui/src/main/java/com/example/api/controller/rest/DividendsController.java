package com.example.api.controller.rest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.example.model.ledger.DividendLedger;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.security.LoginService;
import com.example.service.DividendLedgerService;
import com.example.service.StockService;
import com.example.service.UserService;
import com.example.ui.model.UIDividend;

@RestController
@RequestMapping("/api/dividends")
public class DividendsController {
	
	@Autowired
	private LoginService loginService;
	
	@Autowired
	private DividendLedgerService dividendLedgerService;
	
	@Autowired
	private StockService stockService;
	
	@GetMapping(value = "/recent", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UIDividend>> recentHistory() {

		List<UIDividend> fundList = new ArrayList<>();

		List<DividendLedger> fundLedgerList = dividendLedgerService.recentDividends(loginService.getLoginUserProfile());
		
		fundLedgerList.forEach(dl -> {
			
			fundList.add(new UIDividend(dl.getStockId().getNseSymbol(), dl.getQuantity(), dl.getPerShareAmount(), dl.getExDate().toString(), dl.getRecordDate().toString(), dl.getTransactionDate().toString()));
			
		});
		
		return ResponseEntity.ok(fundList);
	}
	
	@PostMapping(value = "/adddividend", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addStock(@RequestBody UIDividend dividend) {


		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate transactionDate = LocalDate.parse(dividend.getTransactionDate(), formatter);
        LocalDate exDate = LocalDate.parse(dividend.getExDate(), formatter);
        LocalDate recordDate = LocalDate.parse(dividend.getRecordDate(), formatter);

        Stock dividendStock =  stockService.getStockById(dividend.getStockid());
        
        dividendLedgerService.addDividend(loginService.getLoginUserProfile(), dividendStock, dividend.getPerShareAmount(), exDate, recordDate, transactionDate);
        
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
