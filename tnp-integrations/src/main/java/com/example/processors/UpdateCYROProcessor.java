package com.example.processors;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.um.UserProfile;
import com.example.service.FundsLedgerService;
import com.example.service.UserService;

@Service
public class UpdateCYROProcessor implements Processor {

	@Autowired
	private FundsLedgerService fundsLedgerService;
	@Autowired
	private UserService userService;

	@Override
	public void process(Exchange arg0) throws Exception {
		List<UserProfile> activeUsers = userService.activeUsers();

		for (UserProfile user : activeUsers) {
			fundsLedgerService.addCYROFund(user);
		}

	}

}
