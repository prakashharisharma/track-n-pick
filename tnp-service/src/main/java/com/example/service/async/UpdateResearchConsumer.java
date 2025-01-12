package com.example.service.async;

import javax.jms.Session;

import com.example.model.ledger.CrossOverLedger;
import com.example.model.ledger.ValuationLedger;
import com.example.model.master.Stock;
import com.example.service.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.storage.model.StockResearch;
import com.example.storage.repo.ResearchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.mq.constants.QueueConstants;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchType;;

@Component
@Slf4j
public class UpdateResearchConsumer {

	@Autowired
	private StockService stockService;

	@Autowired
	private FundamentalResearchService fundamentalResearchService;

	@Autowired
	private ValuationLedgerService undervalueLedgerService;

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerService;

	@Autowired
	private ResearchLedgerTechnicalService researchTechnicalLedgerService;

	@Autowired
	private CrossOverLedgerService crossOverLedgerService;

	@Autowired
	private TechnicalsResearchService technicalsResearchService;

	@Autowired
	private ResearchTemplate researchTemplate;

	@Autowired
	private ResearchExecutorService researchExecutorService;

	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		log.info("{} Starting research update.", researchIO.getNseSymbol());
		try {
			Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());

			if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {

				this.researchFundamental(stock);

			}
			if (researchIO.getResearchType() == ResearchType.TECHNICAL) {

				this.researchTechnical(stock);

			}
		}catch(Exception e){
			log.error("An error occured while processing research {}", researchIO.getNseSymbol(), e);
		}

		log.info("{} Completed research update.", researchIO.getNseSymbol());
	}


	private void researchFundamental(Stock stock){

		//log.info("{} Researching fundamental.", researchIO.getNseSymbol());

		if(stock!=null) {
			researchExecutorService.executeFundamental(stock);
		}

		/*
		if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {
			this.researchFundamental(stock, researchIO.getResearchTrigger());
		}
		 */


		//log.info("{} Researched fundamental.", researchIO.getNseSymbol());
	}

	private void researchFundamental(Stock stock, ResearchIO.ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchIO.ResearchTrigger.BUY) {
			if (fundamentalResearchService.isUndervalued(stock)) {

				this.addToUnderValueLedger(stock);

			}
		} else if (researchTrigger == ResearchIO.ResearchTrigger.SELL) {
			if (fundamentalResearchService.isOvervalued(stock)) {
				this.removeFromUnderValueLedger(stock);

			}
		} else {

			if (fundamentalResearchService.isUndervalued(stock)) {

				this.addToUnderValueLedger(stock);

			}

			if (fundamentalResearchService.isOvervalued(stock)) {
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


		this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchIO.ResearchTrigger.BUY);

	}

	private void updateResearchLedgerFundamental(Stock stock, ValuationLedger exitValuation) {

		boolean addedNew = researchLedgerService.updateResearch(stock, exitValuation);

		if(addedNew) {
			this.addToResearchHistory(stock, ResearchType.FUNDAMENTAL, ResearchIO.ResearchTrigger.SELL);
		}
	}

	private void addToResearchHistory(Stock stock, ResearchType researchType, ResearchIO.ResearchTrigger researchTrigger) {

		double currentPrice = stock.getStockPrice().getCurrentPrice();

		double pe = stockService.getPe(stock);

		double pb = stockService.getPb(stock);

		ResearchIO researchIO = new ResearchIO(stock.getNseSymbol(), researchType, researchTrigger, currentPrice, pe,
				pb);

		this.updateResearchHistory(researchIO);
	}

	private void researchTechnical(Stock stock){

		//log.info("{} Researching technical.", researchIO.getNseSymbol());

		//if (researchIO.getResearchType() == ResearchType.TECHNICAL) {

			//Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());

			researchExecutorService.executeTechnical(stock);
		//}

		//log.info("{} Researched technical.", researchIO.getNseSymbol());
	}


	private void addBearishCrossOverLedger(Stock stock, CrossOverLedger.CrossOverCategory crossOverCategory, double score) {

		this.updateResearchLedgerTechnical(stock, crossOverCategory.ordinal(), score);

	}

	private void updateResearchLedgerTechnical(Stock stock, int rule, double score) {
		researchTechnicalLedgerService.updateResearch(stock, score);
	}



	private void updateResearchHistory(ResearchIO researchIO){
		StockResearch stockResearch = new StockResearch(researchIO.getNseSymbol(), researchIO.getResearchType(), researchIO.getResearchTrigger(), researchIO.getResearchPrice(), researchIO.getPe(), researchIO.getPb(), researchIO.getResearchDate());

		researchTemplate.create(stockResearch);
	}

}
