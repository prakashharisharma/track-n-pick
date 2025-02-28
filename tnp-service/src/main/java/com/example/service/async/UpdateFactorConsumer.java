package com.example.service.async;

import javax.jms.Session;

import com.example.service.impl.FundamentalResearchService;
import com.example.storage.repo.FactorTemplate;
import com.example.util.io.model.StockPriceIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.Message;
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
import com.example.service.StockService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;
import com.example.util.io.model.StockFactorIO;

@Component
@Slf4j
public class UpdateFactorConsumer {

	@Autowired
	private QueueService queueService;

	@Autowired
	private StockService stockService;

	@Autowired
	private FundamentalResearchService fundamentalResearchService;

	@Autowired
	private FactorTemplate factorTemplate;

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_FACTOR_TXN_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

			log.info("{} Starting factors update.", stockPriceIO.getNseSymbol());

			try {
				this.updateFactorsTxn(stockPriceIO);
			}catch(Exception e){
				log.error("An error occured while updating factors {}", stockPriceIO.getHigh(), e);
			}

			log.info("{} Completed factors update.", stockPriceIO.getNseSymbol());

	}

	private void processFactorsUpdate(StockPriceIO stockPriceIO){

		this.updateFactorsTxn(stockPriceIO);
	}

	private void updateFactorsTxn(StockPriceIO stockPriceIO){

		log.info("{} Updating transactional factors.", stockPriceIO.getNseSymbol());

		Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

		StockFactor prevStockFactor = null;

		StockFactor newStockFactor = null;

		prevStockFactor = stock.getFactor();

		newStockFactor = stockService.updateFactor(stock);

		if (prevStockFactor != null) {

			if (newStockFactor.getQuarterEnded().isAfter(prevStockFactor.getQuarterEnded())) {
				StockFactorIO stockFactorIO = new StockFactorIO(stockPriceIO.getNseSymbol(), newStockFactor.getMarketCap(),
						newStockFactor.getDebtEquity(), newStockFactor.getCurrentRatio(),
						newStockFactor.getQuickRatio(), newStockFactor.getDividend(), newStockFactor.getBookValue(),
						newStockFactor.getEps(), newStockFactor.getReturnOnEquity(),
						newStockFactor.getReturnOnCapital(), newStockFactor.getFaceValue(),
						newStockFactor.getQuarterEnded());

				log.info("{} Updated transactional factors.", stockPriceIO.getNseSymbol());
				this.updateFactorsHistory(stockFactorIO);
			}
		}

		ResearchIO researchIO = new ResearchIO(stockPriceIO.getNseSymbol(), ResearchType.FUNDAMENTAL, ResearchTrigger.BUY_SELL);

		this.processResearch(researchIO);
	}

	private void updateFactorsHistory(StockFactorIO stockFactorIO){

		log.info("{} Updating historical factors.", stockFactorIO.getNseSymbol());

		com.example.storage.model.StockFactor stockFactor = new com.example.storage.model.StockFactor(stockFactorIO.getNseSymbol(), stockFactorIO.getMarketCap(), stockFactorIO.getDebtEquity(), stockFactorIO.getCurrentRatio(), stockFactorIO.getQuickRatio(), stockFactorIO.getDividend(), stockFactorIO.getBookValue(), stockFactorIO.getEps(), stockFactorIO.getReturnOnEquity(), stockFactorIO.getReturnOnCapital(), stockFactorIO.getFaceValue(), stockFactorIO.getQuarterEndedInstant());

		factorTemplate.create(stockFactor);

		log.info("{} Updated historical factors.", stockFactorIO.getNseSymbol());
	}

	private void processResearch(ResearchIO researchIO) {

		queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);

	}

}
