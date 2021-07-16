package com.example.storage.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.storage.model.TradingSession;
import com.example.storage.repo.TradingSessionTemplate;
import com.example.storage.service.TradingSessionService;

@Service
public class TradingSessionServiceImpl implements TradingSessionService {

	@Autowired
	private TradingSessionTemplate tradingSessionTemplate;
	
	@Override
	public void addTradingSession(TradingSession tradingSession) {
		
		tradingSessionTemplate.createTS(tradingSession);
	}

}
