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

import com.example.model.ledger.ValuationLedger.Category;
import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.ResearchLedgerService;
import com.example.service.RuleService;
import com.example.service.StockService;
import com.example.service.ValuationLedgerService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;

@Component
public class ResearchFundamentalConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchFundamentalConsumer.class);

	@Autowired
	private StockService stockService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private ValuationLedgerService undervalueLedgerService;

	@Autowired
	private ResearchLedgerService researchLedgerService;
	
	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_FUNDAMENTAL_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());
		if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {
			this.researchFundamental(stock, researchIO.getResearchTrigger());
		}else if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL_TWEAK) {
			this.researchFundamentalTweak(stock, researchIO.getResearchTrigger());
		}
		
		else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}

	private void researchFundamental(Stock stock, ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchTrigger.BUY) {
			if (ruleService.isUndervalued(stock)) {

				this.addToUnderValueLedger(stock, Category.STRONG);

				this.addToResearchLedgerFundamental(stock,Category.STRONG);

				this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.BUY);

			}else if (ruleService.isUndervaluedTweaked(stock)) {

				this.addToUnderValueLedger(stock, Category.TWEAKED);

				this.addToResearchLedgerFundamental(stock,Category.TWEAKED);

				this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.BUY);

			}
		} else if (researchTrigger == ResearchTrigger.SELL) {
			if (ruleService.isOvervalued(stock)) {
				this.removeFromUnderValueLedger(stock,Category.STRONG);
				this.updateResearchLedgerFundamental(stock,Category.STRONG);
				this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.SELL);
			}
		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}

	private void researchFundamentalTweak(Stock stock, ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchTrigger.BUY) {
			if (ruleService.isUndervaluedTweaked(stock)) {

				this.addToUnderValueLedger(stock, Category.TWEAKED);

				this.addToResearchLedgerFundamental(stock,Category.TWEAKED);

				this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.BUY);

			}
		} else if (researchTrigger == ResearchTrigger.SELL) {
			if(ruleService.isOvervaluedTweaked(stock)) {
				this.removeFromUnderValueLedger(stock,Category.TWEAKED);
				this.updateResearchLedgerFundamental(stock,Category.TWEAKED);
				this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.SELL);
			}
		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}
	
	private void addToUnderValueLedger(Stock stock, Category category) {

		String nseSymbol = stock.getNseSymbol();

		if (stockService.isActive(nseSymbol)) {

			undervalueLedgerService.addUndervalued(stock, category);

		} else {
			LOGGER.debug("NOT IN MASTER, IGNORED..." + nseSymbol);
		}
	}
	
	private void addToResearchLedgerFundamental(Stock stock, Category category) {
		researchLedgerService.addResearch(stock, ResearchType.FUNDAMENTAL,category);
	}

	private void removeFromUnderValueLedger(Stock stock, Category category) {
		undervalueLedgerService.addOvervalued(stock,category);

	}
	
	private void updateResearchLedgerFundamental(Stock stock, Category category) {
		researchLedgerService.updateResearch(stock, ResearchType.FUNDAMENTAL,category);
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
