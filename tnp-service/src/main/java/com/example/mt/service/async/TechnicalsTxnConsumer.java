package com.example.mt.service.async;

import java.time.LocalDate;

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
import com.example.model.stocks.StockTechnicals;
import com.example.mq.constants.QueueConstants;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.service.CleanseService;
import com.example.service.StockService;
import com.example.util.io.model.StockTechnicalsIO;

@Component
public class TechnicalsTxnConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalsTxnConsumer.class);
	
	@Autowired
	private CleanseService cleanseService;
	
	@Autowired
	private StockService stockService;
	@Autowired
	private StockTechnicalsRepository stockTechnicalsRepository;
	
	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE)
	public void receiveMessage(@Payload StockTechnicalsIO stockTechnicalsIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		LOGGER.info("TECHNICALSTXN_CONSUMER START " + stockTechnicalsIO);
		
		if(cleanseService.isNifty500(stockTechnicalsIO.getNseSymbol())) {
			this.processPriceUpdate(stockTechnicalsIO);
		}else {
			LOGGER.debug("NOT IN MASTER, IGNORED..." + stockTechnicalsIO.getNseSymbol());
		}
	}
	
	private void processPriceUpdate(StockTechnicalsIO stockTechnicalsIO) {
		
		Stock stock = stockService.getStockByNseSymbol(stockTechnicalsIO.getNseSymbol());
		
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if (stockTechnicals != null) {
			stockTechnicals.setSma50(stockTechnicalsIO.getSma50());
			stockTechnicals.setPrevSma50(stockTechnicalsIO.getPrevSma50());
			stockTechnicals.setSma100(stockTechnicalsIO.getSma100());
			stockTechnicals.setSma200(stockTechnicalsIO.getSma200());
			stockTechnicals.setPrevSma200(stockTechnicalsIO.getPrevSma200());
			stockTechnicals.setRsi(stockTechnicalsIO.getRsi());
			stockTechnicals.setCurrentTrend(stockTechnicalsIO.getCurrentTrend());
			stockTechnicals.setMidTermTrend(stockTechnicalsIO.getMidTermTrend());
			stockTechnicals.setLongTermTrend(stockTechnicalsIO.getLongTermTrend());
			stockTechnicals.setLastModified(LocalDate.now());
		}else {
			stockTechnicals = new StockTechnicals();
			stockTechnicals.setStock(stock);
			
			stockTechnicals.setSma50(stockTechnicalsIO.getSma50());
			stockTechnicals.setPrevSma50(stockTechnicalsIO.getPrevSma50());
			stockTechnicals.setSma100(stockTechnicalsIO.getSma100());
			stockTechnicals.setSma200(stockTechnicalsIO.getSma200());
			stockTechnicals.setPrevSma200(stockTechnicalsIO.getPrevSma200());
			stockTechnicals.setRsi(stockTechnicalsIO.getRsi());
			stockTechnicals.setCurrentTrend(stockTechnicalsIO.getCurrentTrend());
			stockTechnicals.setMidTermTrend(stockTechnicalsIO.getMidTermTrend());
			stockTechnicals.setLongTermTrend(stockTechnicalsIO.getLongTermTrend());
			stockTechnicals.setLastModified(LocalDate.now());
		}
		
		stockTechnicalsRepository.save(stockTechnicals);
	}
	
}
