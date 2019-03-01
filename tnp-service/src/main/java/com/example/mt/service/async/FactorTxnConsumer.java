package com.example.mt.service.async;

import javax.jms.Session;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.RuleService;
import com.example.service.StockService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;
import com.example.util.io.model.StockFactorIO;

@Component
public class FactorTxnConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactorTxnConsumer.class);
	
	@Autowired
	private QueueService queueService;
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private RuleService ruleService;
	
	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_FACTOR_TXN_QUEUE)
	public void receiveMessage(@Payload String nseSymbol, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		LOGGER.debug(QueueConstants.MTQueue.UPDATE_FACTOR_TXN_QUEUE.toUpperCase() +" : " + nseSymbol + " : START");
		
		Stock stock = stockService.getStockByNseSymbol(nseSymbol);
		
		StockFactor prevStockFactor = null;
		
		StockFactor newStockFactor = null;
		
		if(ruleService.isPriceInRange(stock)) {
			
			prevStockFactor = stock.getStockFactor();
			
			newStockFactor = stockService.updateFactor(stock);
			
			if(prevStockFactor !=  null) {
			
			if(newStockFactor.getQuarterEnded().isAfter(prevStockFactor.getQuarterEnded())) {
				//SAVE IT TO STORAGE
				StockFactorIO stockFactorIO = new StockFactorIO(nseSymbol, newStockFactor.getMarketCap(), newStockFactor.getDebtEquity(), newStockFactor.getCurrentRatio(), newStockFactor.getQuickRatio(), newStockFactor.getDividend(), newStockFactor.getBookValue(), newStockFactor.getEps(), newStockFactor.getReturnOnEquity(), newStockFactor.getReturnOnCapital(), newStockFactor.getFaceValue(), newStockFactor.getQuarterEnded());
				
				queueService.send(stockFactorIO, QueueConstants.HistoricalQueue.UPDATE_FACTORS_QUEUE);
			}
			}
		}
		
		ResearchIO researchIO = new ResearchIO(nseSymbol, ResearchType.FUNDAMENTAL, ResearchTrigger.BUY);
		
		this.processResearch(researchIO);
		
		LOGGER.debug(QueueConstants.MTQueue.UPDATE_FACTOR_TXN_QUEUE.toUpperCase() +" : " + nseSymbol + " : END");
	}
	
	private void processResearch(ResearchIO researchIO) {
		queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
	}
	
	
}
