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
import com.example.mq.producer.QueueService;
import com.example.service.CrossOverLedgerService;
import com.example.service.ResearchLedgerService;
import com.example.service.RuleService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.service.UndervalueLedgerService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;;

@Component
public class ResearchConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchConsumer.class);

	@Autowired
	private StockService stockService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private UndervalueLedgerService undervalueLedgerService;

	@Autowired
	private CrossOverLedgerService crossOverLedgerService;
	
	@Autowired
	private TechnicalsResearchService technicalsResearchService;

	@Autowired
	private ResearchLedgerService researchLedgerService;

	@Autowired
	private QueueService queueService;

	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.info("RESEARCH_CONSUMER START " + researchIO);

		Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());

		if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {
			this.researchFundamental(stock, researchIO.getResearchTrigger());
		} else if (researchIO.getResearchType() == ResearchType.TECHNICAL) {
			this.researchTechnical(stock, researchIO.getResearchTrigger());
		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}

	private void researchFundamental(Stock stock, ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchTrigger.BUY) {
			if (ruleService.isUndervalued(stock)) {

				this.addToUnderValueLedger(stock);

				this.addToResearchLedgerFundamental(stock);

				this.researchTechnical(stock, ResearchTrigger.BUY);

				this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.BUY);

			}
		} else if (researchTrigger == ResearchTrigger.SELL) {
			if (ruleService.isOvervalued(stock)) {
				this.removeFromUnderValueLedger(stock);
				this.updateResearchLedgerFundamental(stock);
				this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.SELL);
			}
		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}

	private void researchTechnical(Stock stock, ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchTrigger.BUY) {
			if (technicalsResearchService.isBullishCrossOver(stock)) {

				this.addToCrossOverLedger(stock);
				
				this.addToResearchLedgerTechnical(stock);

				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.BUY);

			}
		} else if (researchTrigger == ResearchTrigger.SELL) {

			if (technicalsResearchService.isBearishCrossover(stock)) {
				
				this.removeFromCrossOverLedger(stock);
				
				this.updateResearchLedgerTechnical(stock);

				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.SELL);
			}
		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}

	private void updateResearchLedgerFundamental(Stock stock) {
		researchLedgerService.updateResearch(stock, ResearchType.FUNDAMENTAL);
	}

	private void updateResearchLedgerTechnical(Stock stock) {
		researchLedgerService.updateResearch(stock, ResearchType.TECHNICAL);
	}

	private void addToResearchLedgerFundamental(Stock stock) {
		researchLedgerService.addResearch(stock, ResearchType.FUNDAMENTAL);
	}

	private void addToResearchLedgerTechnical(Stock stock) {
		researchLedgerService.addResearch(stock, ResearchType.TECHNICAL);
	}

	private void addToUnderValueLedger(Stock stock) {

		String nseSymbol = stock.getNseSymbol();

		if (stockService.isActive(nseSymbol)) {

			undervalueLedgerService.add(stock);

		} else {
			LOGGER.debug("NOT IN MASTER, IGNORED..." + nseSymbol);
		}
	}

	private void addToCrossOverLedger(Stock stock) {
		String nseSymbol = stock.getNseSymbol();

		if (stockService.isActive(nseSymbol)) {

			crossOverLedgerService.add(stock);

		} else {
			LOGGER.debug("NOT IN MASTER, IGNORED..." + nseSymbol);
		}
	}
	
	private void removeFromUnderValueLedger(Stock stock) {
		undervalueLedgerService.remove(stock);

	}

	private void removeFromCrossOverLedger(Stock stock) {
		crossOverLedgerService.remove(stock);

	}
	
	private void addToResearchHistory(Stock stock, ResearchType researchType, ResearchTrigger researchTrigger) {

		double currentPrice = stock.getStockPrice().getCurrentPrice();

		double pe = stockService.getPe(stock);

		double pb = stockService.getPb(stock);

		ResearchIO researchIO = new ResearchIO(stock.getNseSymbol(), researchType, researchTrigger, currentPrice, pe,
				pb);

		if (!researchLedgerService.isResearchStorageNotified(stock, researchType, researchTrigger)) {

			queueService.send(researchIO, QueueConstants.HistoricalQueue.UPDATE_RESEARCH_QUEUE);

			researchLedgerService.updateResearchNotifiedStorage(stock, researchType);

		} else {
			LOGGER.debug("RESEARCH ALREADY EXIST..." + stock.getNseSymbol());
		}

	}

}
