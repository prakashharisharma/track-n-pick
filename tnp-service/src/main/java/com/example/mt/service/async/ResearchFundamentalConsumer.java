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

import com.example.model.ledger.ValuationLedger;
import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.ResearchLedgerFundamentalService;
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
	private ResearchLedgerFundamentalService researchLedgerService;

	@Autowired
	private QueueService queueService;

	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_FUNDAMENTAL_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());
		if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {
			this.researchFundamental(stock, researchIO.getResearchTrigger());
		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}

	private void researchFundamental(Stock stock, ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchTrigger.BUY) {
			if (ruleService.isUndervalued(stock)) {

				this.addToUnderValueLedger(stock);

			}
		} else if (researchTrigger == ResearchTrigger.SELL) {
			if (ruleService.isOvervalued(stock)) {
				this.removeFromUnderValueLedger(stock);

			}
		} else {

			if (ruleService.isUndervalued(stock)) {

				this.addToUnderValueLedger(stock);

			}

			if (ruleService.isOvervalued(stock)) {
				this.removeFromUnderValueLedger(stock);

			}

		}

	}

	private void addToUnderValueLedger(Stock stock) {

		ValuationLedger entryValuation = undervalueLedgerService.addUndervalued(stock);

		this.addToResearchLedgerFundamental(stock, entryValuation);

	}

	private void removeFromUnderValueLedger(Stock stock) {

		ValuationLedger exitValuation = undervalueLedgerService.addOvervalued(stock);

		this.updateResearchLedgerFundamental(stock, exitValuation);
	}

	private void addToResearchLedgerFundamental(Stock stock, ValuationLedger entryValuation) {

		researchLedgerService.addResearch(stock, entryValuation);


			this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.BUY);

	}

	private void updateResearchLedgerFundamental(Stock stock, ValuationLedger exitValuation) {

		boolean addedNew = researchLedgerService.updateResearch(stock, exitValuation);

		if(addedNew) {
			this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchTrigger.SELL);
		}
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
