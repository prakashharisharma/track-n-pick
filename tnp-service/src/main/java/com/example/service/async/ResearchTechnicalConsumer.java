package com.example.service.async;

import javax.jms.Session;

import com.example.service.impl.FundamentalResearchService;
import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.model.ledger.CrossOverLedger;
import com.example.model.ledger.CrossOverLedger.CrossOverCategory;
import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.CrossOverLedgerService;
import com.example.service.ResearchLedgerTechnicalService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;

@Component
@Deprecated
public class ResearchTechnicalConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchTechnicalConsumer.class);

	@Autowired
	private StockService stockService;

	@Autowired
	private CrossOverLedgerService crossOverLedgerService;

	@Autowired
	private TechnicalsResearchService technicalsResearchService;

	@Autowired
	private ResearchLedgerTechnicalService researchLedgerService;

	@Autowired
	private QueueService queueService;

	@Autowired
	private FundamentalResearchService fundamentalResearchService;

	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_TECHNICAL_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.info("TECHCOMSUMER 1" + researchIO.getNseSymbol() + " " + researchIO.getResearchType());

		if (researchIO.getResearchType() == ResearchType.TECHNICAL) {

			Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());

			this.researchTechnical(stock, researchIO.getResearchTrigger());

		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}
	}

	private void researchTechnical(Stock stock, ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchTrigger.BUY) {
			if (fundamentalResearchService.isUndervalued(stock)) {
				if (technicalsResearchService.isBullishCrossOver200(stock)) {

					this.addBullishCrossOverLedger(stock, CrossOverCategory.CROSS200);

				}


			}

		} else if (researchTrigger == ResearchTrigger.SELL) {

			if (technicalsResearchService.isBearishCrossover100(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS100);

			}

		} else {

			if (technicalsResearchService.isBullishCrossOver200(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverCategory.CROSS200);

			}



			if (technicalsResearchService.isBearishCrossover200(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS200);

			}

			if (technicalsResearchService.isBearishCrossover100(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS100);

			}


		}

	}

	private void addBullishCrossOverLedger(Stock stock, CrossOverCategory crossOverCategory) {

		LOGGER.info("TECHCOMSUMER 3" + stock.getNseSymbol() + " " + crossOverCategory);

		LOGGER.info("TECHCOMSUMER 4" + stock.getNseSymbol() + " " + crossOverCategory);

		CrossOverLedger entryCrossOver = crossOverLedgerService.addBullish(stock, crossOverCategory);

		if (crossOverCategory == CrossOverCategory.CROSS200 || crossOverCategory == CrossOverCategory.VIPR
				|| crossOverCategory == CrossOverCategory.BO50BULLISH
				|| crossOverCategory == CrossOverCategory.BO200BULLISH) {

			this.addToResearchLedgerTechnical(stock, entryCrossOver);
		}

		this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.BUY);

	}

	private void addBearishCrossOverLedger(Stock stock, CrossOverCategory crossOverCategory) {

		CrossOverLedger exitCrossOver = crossOverLedgerService.addBearish(stock, crossOverCategory);

		if (crossOverCategory == CrossOverCategory.CROSS100 || crossOverCategory == CrossOverCategory.VIPF
				|| crossOverCategory == CrossOverCategory.BO50BEARISH
				|| crossOverCategory == CrossOverCategory.BO200BEARISH) {
			this.updateResearchLedgerTechnical(stock, exitCrossOver);
		}
	}

	private void updateResearchLedgerTechnical(Stock stock, CrossOverLedger exitCrossOver) {
		//researchLedgerService.updateResearch(stock, exitCrossOver);
	}

	private void addToResearchLedgerTechnical(Stock stock, CrossOverLedger entryCrossOver) {
		//researchLedgerService.addResearch(stock, entryCrossOver);
	}

	private void addToResearchHistory(Stock stock, ResearchType researchType, ResearchTrigger researchTrigger) {

		double currentPrice = stock.getStockPrice().getCurrentPrice();

		double pe = stockService.getPe(stock);

		double pb = stockService.getPb(stock);

		ResearchIO researchIO = new ResearchIO(stock.getNseSymbol(), researchType, researchTrigger, currentPrice, pe,
				pb);

		queueService.send(researchIO, QueueConstants.HistoricalQueue.UPDATE_RESEARCH_QUEUE);

	}

	

}
