package com.example.processors;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.um.User;
import com.example.service.PerformanceLedgerService;
import com.example.service.UserService;

@Service
public class UpdateMonthlyValueProcessor implements Processor{

	@Autowired
	private UserService userService;
	
	@Autowired
	private PerformanceLedgerService performanceLedgerService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		
		List<User> activeUsers = userService.activeUsers();
		
		for(User user : activeUsers) {
			performanceLedgerService.updateMonthlyPerformance(user);
		}
		
	}

}