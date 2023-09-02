package com.example.mt.service.async;

import java.time.LocalDate;

import javax.jms.Session;

import com.example.service.SectorService;
import com.example.util.io.model.StockIO;
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
import com.example.model.stocks.StockPrice;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.repo.stocks.StockPriceRepository;
import com.example.service.StockService;
import com.example.util.io.model.StockPriceIO;

@Component
public class PriceTxnConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceTxnConsumer.class);

	@Autowired
	private QueueService queueService;

	@Autowired
	private StockService stockService;

	@Autowired
	private SectorService sectorService;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.info("PRICETXN_CONSUMER START " + stockPriceIO);

		if (stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol()) != null){

			if (stockService.isActive(stockPriceIO.getNseSymbol())) {

				this.processPriceUpdate(stockPriceIO);

			}
	}else {
			StockIO stockIO = new StockIO(stockPriceIO.getNseSymbol(), "NIFTY", stockPriceIO.getNseSymbol(), stockPriceIO.getSeries(), stockPriceIO.getIsin(), StockIO.IndiceType.NSE);

			Stock stock = stockService.add(stockIO.getIsin(), stockIO.getCompanyName(), stockIO.getNseSymbol(),stockIO.getIndice(), sectorService.getOrAddSectorByName(stockIO.getSector()));

			LOGGER.debug("NOT IN MASTER, IGNORED..." + stockPriceIO.getNseSymbol());

			queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);
		}

	}

	private void processPriceUpdate(StockPriceIO stockPriceIO) {

		Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

		StockPrice stockPrice = stock.getStockPrice();

		if (stockPrice != null) {
			stockPrice.setLastModified(LocalDate.now());
			stockPrice.setCurrentPrice(stockPriceIO.getClose());
			stockPrice.setPrevClose(stockPriceIO.getPrevClose());
			stockPrice.setOpenPrice(stockPriceIO.getOpen());
			//stockPrice.setYearHigh(stockPriceIO.getYearHigh());
			//stockPrice.setYearLow(stockPriceIO.getYearLow());
			stockPrice.setBhavDate(stockPriceIO.getTimestamp());
			
			
		} else {
			stockPrice = new StockPrice();
			stockPrice.setStock(stock);
			stockPrice.setLastModified(LocalDate.now());
			stockPrice.setCurrentPrice(stockPriceIO.getClose());
			stockPrice.setPrevClose(stockPriceIO.getPrevClose());
			stockPrice.setOpenPrice(stockPriceIO.getOpen());
			//stockPrice.setYearHigh(stockPriceIO.getYearHigh());
			//stockPrice.setYearLow(stockPriceIO.getYearLow());
			stockPrice.setBhavDate(stockPriceIO.getTimestamp());
		}

		stockPriceRepository.save(stockPrice);

		this.processFactor(stockPriceIO.getNseSymbol());
	}

	private void processFactor(String nseSymbol) {
		queueService.send(nseSymbol, QueueConstants.MTQueue.UPDATE_FACTOR_TXN_QUEUE);
	}
}
