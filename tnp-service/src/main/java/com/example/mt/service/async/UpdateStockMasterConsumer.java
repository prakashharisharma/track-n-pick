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
import com.example.mq.constants.QueueConstants;
import com.example.service.SectorService;
import com.example.service.StockService;
import com.example.util.io.model.StockIO;

@Component
public class UpdateStockMasterConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStockMasterConsumer.class);
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private SectorService sectorService;
	
	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_MASTER_STOCK_QUEUE)
	public void receiveMessage(@Payload StockIO stockIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		LOGGER.debug("PROCESSING  " + stockIO.getNseSymbol() + " : " + stockIO.getIndice());
		
			Thread.sleep(50);
			this.process(stockIO);
	}
	
	private void process(StockIO stockIO){
		
		Stock stock = stockService.getStockByNseSymbol(stockIO.getNseSymbol());
		
		if(stock!=null) {
			
			stock.setPrimaryIndice(stockIO.getIndice());
			stock.setActive(true);
			
			stockService.save(stock);
			LOGGER.debug("UPDATED " + stockIO.getNseSymbol() + " : " + stockIO.getIndice());
		}else {
			stock = stockService.add(stockIO.getIsin(), stockIO.getCompanyName(), stockIO.getNseSymbol(),stockIO.getIndice(), sectorService.getOrAddSectorByName(stockIO.getSector()));
			LOGGER.debug("ADDED " + stockIO.getNseSymbol() + " : " + stockIO.getIndice());
		}
		
		
	}
}
