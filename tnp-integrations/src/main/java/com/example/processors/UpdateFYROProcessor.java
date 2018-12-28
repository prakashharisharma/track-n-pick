package com.example.processors;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.um.User;
import com.example.service.FundsLedgerService;
import com.example.service.UserService;

@Service
public class UpdateFYROProcessor implements Processor {

	@Autowired
	private FundsLedgerService fundsLedgerService;
	@Autowired
	private UserService userService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		List<User> activeUsers = userService.activeUsers();
		
		for(User user : activeUsers) {
			fundsLedgerService.addFYROFund(user);
		}

	}

}
