package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.um.User;
import com.example.service.UserService;
import com.example.service.WatchListService;

@Service
public class UpdateWatchListProcessor implements Processor {

	@Autowired
	private WatchListService watchListService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		
		User user = userService.getUserById(1);
		
		watchListService.updateDailyWatchListAddStocks(user);
		
	}

}