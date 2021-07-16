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
import com.example.service.StockService;
import com.example.util.io.model.StockFactorIO;

@Component
public class UpdateStockFactorConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStockFactorConsumer.class);

	@Autowired
	private StockService stockService;

	@Autowired
	private QueueService queueService;

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_MASTER_FACTOR_QUEUE)
	public void receiveMessage(@Payload Stock stock, @Headers MessageHeaders headers, Message message, Session session)
			throws InterruptedException {

		LOGGER.debug("UPDATING FACTOR  " + stock.getNseSymbol() + " : " + stock.getPrimaryIndice());

		StockFactor prevStockFactor = null;

		StockFactor newStockFactor = null;

		prevStockFactor = stock.getStockFactor();

		newStockFactor = stockService.updateFactor(stock);

		if (prevStockFactor != null) {

			if (newStockFactor.getQuarterEnded().isAfter(prevStockFactor.getQuarterEnded())) {
				// SAVE IT TO STORAGE
				StockFactorIO stockFactorIO = new StockFactorIO(stock.getNseSymbol(), newStockFactor.getMarketCap(),
						newStockFactor.getDebtEquity(), newStockFactor.getCurrentRatio(),
						newStockFactor.getQuickRatio(), newStockFactor.getDividend(), newStockFactor.getBookValue(),
						newStockFactor.getEps(), newStockFactor.getReturnOnEquity(),
						newStockFactor.getReturnOnCapital(), newStockFactor.getFaceValue(),
						newStockFactor.getQuarterEnded());

				queueService.send(stockFactorIO, QueueConstants.HistoricalQueue.UPDATE_FACTORS_QUEUE);
			}
		}

		Thread.sleep(50);

	}
}
