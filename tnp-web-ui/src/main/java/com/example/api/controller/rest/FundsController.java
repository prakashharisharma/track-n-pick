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

import com.example.model.ledger.FundsLedger;
import com.example.model.um.UserProfile;
import com.example.security.LoginService;
import com.example.service.FundsLedgerService;
import com.example.service.UserService;
import com.example.ui.model.UIFund;

@RestController
@RequestMapping("/api/funds")
public class FundsController {

	@Autowired
	private FundsLedgerService fundsLedgerService;
	
	@Autowired
	private LoginService loginService;
	
	@GetMapping(value = "/recenthistory", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UIFund>> recentHistory() {

		List<UIFund> fundList = new ArrayList<>();
		

		
		List<FundsLedger> fundLedgerList = fundsLedgerService.recentHistory(loginService.getLoginUserProfile());
		
		fundLedgerList.forEach(fl -> {
			fundList.add(new UIFund(fl.getAmount(), fl.getTransactionType().toString(), fl.getTransactionDate()));
			
		});
		
		return ResponseEntity.ok(fundList);
	}
	
	@PostMapping(value = "/managefunds", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addStock(@RequestBody UIFund fund) {

		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String txnDate = fund.getTxnDate();

        LocalDate transactionDate = LocalDate.parse(txnDate, formatter);

        if(fund.getTxnType().equalsIgnoreCase("ADD")) {
        	fundsLedgerService.addFund(loginService.getLoginUserProfile(), fund.getAmount(), transactionDate);
        }else if (fund.getTxnType().equalsIgnoreCase("WITHDRAW")) {
        	fundsLedgerService.withdrawFund(loginService.getLoginUserProfile(), fund.getAmount(), transactionDate);
        }

		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
}
