package com.example.storage.service.async;

import javax.jms.Session;

import org.apache.activemq.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.mq.constants.QueueConstants;
import com.example.storage.model.TradingSession;
import com.example.storage.service.TradingSessionService;
import com.example.util.io.model.TradingSessionIO;

@Component
public class TradingSessionConsumer {

	@Autowired
	private TradingSessionService tradingSessionService;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_TRADING_SESSION_QUEUE)
	public void receiveMessage(@Payload TradingSessionIO tradingSessionIO, @Headers MessageHeaders headers, Message message,
			Session session) {

		System.out.println("TS_CONSUMER START " + tradingSessionIO);
		
		TradingSession tradingSession = new TradingSession(tradingSessionIO.getTradingDate());
		
		tradingSessionService.addTradingSession(tradingSession);
		
		System.out.println("TS_CONSUMER END " + tradingSessionIO);
	}
}
