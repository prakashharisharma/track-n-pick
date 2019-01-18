package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.um.UserProfile;
import com.example.service.UserService;
import com.example.service.WatchListService;

@Service
public class WatchListUpdateProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(WatchListUpdateProcessor.class);
	
	@Autowired
	private WatchListService watchListService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		
		LOGGER.info("UPDATE WATCHLIST START");
		
		UserProfile user = userService.getUserById(1);
		
		watchListService.updateWatchListAddStocks(user);
		
		LOGGER.info("UPDATE WATCHLIST END");
	}

}
