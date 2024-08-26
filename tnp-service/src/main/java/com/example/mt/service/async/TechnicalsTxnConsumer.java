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
import com.example.mq.producer.QueueService;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.service.PortfolioService;
import com.example.service.ResearchLedgerFundamentalService;
import com.example.service.StockService;
import com.example.util.io.model.StockTechnicalsIO;

@Component
public class TechnicalsTxnConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalsTxnConsumer.class);

	@Autowired
	private StockService stockService;
	@Autowired
	private StockTechnicalsRepository stockTechnicalsRepository;

	@Autowired
	private QueueService queueService;

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerFundamentalService;

	@Autowired
	private PortfolioService portfolioService;

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE)
	public void receiveMessage(@Payload StockTechnicalsIO stockTechnicalsIO, @Headers MessageHeaders headers,
			Message message, Session session) throws InterruptedException {

		LOGGER.info("TECHNICALSTXN_CONSUMER START " + stockTechnicalsIO);

		if (stockService.isActive(stockTechnicalsIO.getNseSymbol())) {
			this.processPriceUpdate(stockTechnicalsIO);
		} else {
			LOGGER.debug("NOT IN MASTER, IGNORED..." + stockTechnicalsIO.getNseSymbol());
		}
	}

	private void processPriceUpdate(StockTechnicalsIO stockTechnicalsIO) {

		Stock stock = stockService.getStockByNseSymbol(stockTechnicalsIO.getNseSymbol());

		StockTechnicals stockTechnicals = stock.getTechnicals();

		if (stockTechnicals != null) {
			stockTechnicals.setSma5(stockTechnicalsIO.getSma5());
			stockTechnicals.setSma10(stockTechnicalsIO.getSma10());
			stockTechnicals.setSma20(stockTechnicalsIO.getSma20());
			stockTechnicals.setSma50(stockTechnicalsIO.getSma50());
			stockTechnicals.setSma100(stockTechnicalsIO.getSma100());
			stockTechnicals.setSma200(stockTechnicalsIO.getSma200());

			stockTechnicals.setPrevSma5(stockTechnicalsIO.getPrevSma5());
			stockTechnicals.setPrevSma10(stockTechnicalsIO.getPrevSma10());
			stockTechnicals.setPrevSma20(stockTechnicalsIO.getPrevSma20());
			stockTechnicals.setPrevSma50(stockTechnicalsIO.getPrevSma50());
			stockTechnicals.setPrevSma100(stockTechnicalsIO.getPrevSma100());
			stockTechnicals.setPrevSma200(stockTechnicalsIO.getPrevSma200());

			stockTechnicals.setEma5(stockTechnicalsIO.getEma5());
			stockTechnicals.setEma10(stockTechnicalsIO.getEma10());
			stockTechnicals.setEma20(stockTechnicalsIO.getEma20());
			stockTechnicals.setEma50(stockTechnicalsIO.getEma50());
			stockTechnicals.setEma100(stockTechnicalsIO.getEma100());
			stockTechnicals.setEma200(stockTechnicalsIO.getEma200());

			stockTechnicals.setPrevEma5(stockTechnicalsIO.getPrevEma5());
			stockTechnicals.setPrevEma10(stockTechnicalsIO.getPrevEma10());
			stockTechnicals.setPrevEma20(stockTechnicalsIO.getPrevEma20());
			stockTechnicals.setPrevEma50(stockTechnicalsIO.getPrevEma50());
			stockTechnicals.setPrevEma100(stockTechnicalsIO.getPrevEma100());
			stockTechnicals.setPrevEma200(stockTechnicalsIO.getPrevEma200());


			stockTechnicals.setRsi(stockTechnicalsIO.getRsi());

			stockTechnicals.setLastModified(LocalDate.now());

			stockTechnicals.setSok(stockTechnicalsIO.getSok());
			stockTechnicals.setSod(stockTechnicalsIO.getSod());
			stockTechnicals.setObv(stockTechnicalsIO.getObv());
			stockTechnicals.setRocv(stockTechnicalsIO.getRocv());

			stockTechnicals.setVolume(stockTechnicalsIO.getVolume());
			stockTechnicals.setAvgVolume(stockTechnicalsIO.getAvgVolume());

		} else {
			stockTechnicals = new StockTechnicals();
			stockTechnicals.setStock(stock);

			stockTechnicals.setSma5(stockTechnicalsIO.getSma5());
			stockTechnicals.setSma10(stockTechnicalsIO.getSma10());
			stockTechnicals.setSma20(stockTechnicalsIO.getSma20());
			stockTechnicals.setSma50(stockTechnicalsIO.getSma50());
			stockTechnicals.setSma100(stockTechnicalsIO.getSma100());
			stockTechnicals.setSma200(stockTechnicalsIO.getSma200());

			stockTechnicals.setPrevSma5(stockTechnicalsIO.getPrevSma5());
			stockTechnicals.setPrevSma10(stockTechnicalsIO.getPrevSma10());
			stockTechnicals.setPrevSma20(stockTechnicalsIO.getPrevSma20());
			stockTechnicals.setPrevSma50(stockTechnicalsIO.getPrevSma50());
			stockTechnicals.setPrevSma100(stockTechnicalsIO.getPrevSma100());
			stockTechnicals.setPrevSma200(stockTechnicalsIO.getPrevSma200());

			stockTechnicals.setEma5(stockTechnicalsIO.getEma5());
			stockTechnicals.setEma10(stockTechnicalsIO.getEma10());
			stockTechnicals.setEma20(stockTechnicalsIO.getEma20());
			stockTechnicals.setEma50(stockTechnicalsIO.getEma50());
			stockTechnicals.setEma100(stockTechnicalsIO.getEma100());
			stockTechnicals.setEma200(stockTechnicalsIO.getEma200());

			stockTechnicals.setPrevEma5(stockTechnicalsIO.getPrevEma5());
			stockTechnicals.setPrevEma10(stockTechnicalsIO.getPrevEma10());
			stockTechnicals.setPrevEma20(stockTechnicalsIO.getPrevEma20());
			stockTechnicals.setPrevEma50(stockTechnicalsIO.getPrevEma50());
			stockTechnicals.setPrevEma100(stockTechnicalsIO.getPrevEma100());
			stockTechnicals.setPrevEma200(stockTechnicalsIO.getPrevEma200());

			stockTechnicals.setRsi(stockTechnicalsIO.getRsi());

			stockTechnicals.setLastModified(LocalDate.now());

			stockTechnicals.setSok(stockTechnicalsIO.getSok());
			stockTechnicals.setSod(stockTechnicalsIO.getSod());
			stockTechnicals.setObv(stockTechnicalsIO.getObv());
			stockTechnicals.setRocv(stockTechnicalsIO.getRocv());



			stockTechnicals.setVolume(stockTechnicalsIO.getVolume());
			stockTechnicals.setAvgVolume(stockTechnicalsIO.getAvgVolume());

		}

		stockTechnicalsRepository.save(stockTechnicals);

		this.processResearch(stock, stockTechnicalsIO);

	}

	private void processResearch(Stock stock, StockTechnicalsIO stockTechnicalsIO) {

		if (researchLedgerFundamentalService.isResearchActive(stock)) {
			queueService.send(stockTechnicalsIO, QueueConstants.MTQueue.RESEARCH_BREAKOUT_QUEUE);
		} else if (portfolioService.isPortfolioStock(stock)) {
			queueService.send(stockTechnicalsIO, QueueConstants.MTQueue.RESEARCH_BREAKOUT_QUEUE);
		} else {
			LOGGER.info("RESEARCH_CONSUMER processToTechnicals, Skip as This is not Active in research or Portfolio "
					+ stock.getNseSymbol());
		}
	}
}
