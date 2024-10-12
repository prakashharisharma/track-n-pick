package com.example.mt.service.async;

import javax.jms.Session;

import com.example.model.ledger.CrossOverLedger;
import com.example.model.ledger.ValuationLedger;
import com.example.model.master.Stock;
import com.example.service.*;
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
	private RuleService ruleService;

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

	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		log.info("{} Starting research update.", researchIO.getNseSymbol());

		if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {

			this.researchFundamental(researchIO);
			
		}

		if (researchIO.getResearchType() == ResearchType.TECHNICAL) {

			this.researchTechnical(researchIO);
			
		}

		log.info("{} Completed research update.", researchIO.getNseSymbol());
	}


	private void researchFundamental(ResearchIO researchIO){

		log.info("{} Researching fundamental.", researchIO.getNseSymbol());

		Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());
		if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {
			this.researchFundamental(stock, researchIO.getResearchTrigger());
		}

		log.info("{} Researched fundamental.", researchIO.getNseSymbol());
	}

	private void researchFundamental(Stock stock, ResearchIO.ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchIO.ResearchTrigger.BUY) {
			if (ruleService.isUndervalued(stock)) {

				this.addToUnderValueLedger(stock);

			}
		} else if (researchTrigger == ResearchIO.ResearchTrigger.SELL) {
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

	private void researchTechnical(ResearchIO researchIO){

		log.info("{} Researching technical.", researchIO.getNseSymbol());

		if (researchIO.getResearchType() == ResearchType.TECHNICAL) {

			Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());

			//if(researchLedgerService.isResearchActive(stock)) {
				this.researchTechnical(stock, researchIO.getResearchTrigger());
			//}

		}

		log.info("{} Researched technical.", researchIO.getNseSymbol());
	}

	private void researchTechnical(Stock stock, ResearchIO.ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchIO.ResearchTrigger.BUY) {
			/*
			if (ruleService.isUndervalued(stock)) {
				if (technicalsResearchService.isBullishCrossOver200(stock)) {

					this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.CROSS200);

				} else if (technicalsResearchService.isPriceVolumeBullish(stock)) {
					this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.VIPR);
				}

			}
			 */

			if (technicalsResearchService.isBullishRule1(stock)) {

				double score = technicalsResearchService.breakoutScore(stock);

				this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.RULE1, score);

			}

		} else if (researchTrigger == ResearchIO.ResearchTrigger.SELL) {

		  	if (technicalsResearchService.isPriceVolumeBearish(stock)) {
				this.addBearishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.VIPF);
			}

		} else {

			if (technicalsResearchService.isBullishRule1(stock)) {

				double score = technicalsResearchService.breakoutScore(stock);

				this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.RULE1, score);

			}

			/*
			if (technicalsResearchService.isBullishRule2(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.RULE2);

			}
			 */

			/*
			if (technicalsResearchService.isBullishCrossOver50(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.CROSS50);

			}
 			*/

			/*
			if (technicalsResearchService.isBullishCrossOver20(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.CROSS20);

			}
			*/


			if (technicalsResearchService.isBearishRule1(stock)) {


				this.addBearishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.RULE1);

			}

			/*
			if (technicalsResearchService.isBearishRule2(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.RULE2);

			}
			 */

			/*
			if (technicalsResearchService.isBearishCrossover20(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.CROSS20);

			}
			*/

			/*
			if (technicalsResearchService.isPriceVolumeBullish(stock)) {
				this.addBullishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.VIPR);
			}

			if (technicalsResearchService.isPriceVolumeBearish(stock)) {
				this.addBearishCrossOverLedger(stock, CrossOverLedger.CrossOverCategory.VIPF);
			}
			 */

		}

	}

	private void addBullishCrossOverLedger(Stock stock, CrossOverLedger.CrossOverCategory crossOverCategory, double score) {

		//CrossOverLedger entryCrossOver = crossOverLedgerService.addBullish(stock, crossOverCategory);

		//if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS20 || crossOverCategory == CrossOverLedger.CrossOverCategory.VIPR) {

			this.addToResearchLedgerTechnical(stock, crossOverCategory.ordinal(), score);
		//}

		this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchIO.ResearchTrigger.BUY);

	}

	private void addBearishCrossOverLedger(Stock stock, CrossOverLedger.CrossOverCategory crossOverCategory) {

		//CrossOverLedger exitCrossOver = crossOverLedgerService.addBearish(stock, crossOverCategory);

		//if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS20 || crossOverCategory == CrossOverLedger.CrossOverCategory.VIPF) {
			this.updateResearchLedgerTechnical(stock, crossOverCategory.ordinal());
		//}

		//this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchIO.ResearchTrigger.SELL);

	}

	private void updateResearchLedgerTechnical(Stock stock, int rule) {
		researchTechnicalLedgerService.updateResearch(stock, rule);
	}

	private void addToResearchLedgerTechnical(Stock stock, int rule, double score) {
		researchTechnicalLedgerService.addResearch(stock, rule, score);
	}


	private void updateResearchHistory(ResearchIO researchIO){
		StockResearch stockResearch = new StockResearch(researchIO.getNseSymbol(), researchIO.getResearchType(), researchIO.getResearchTrigger(), researchIO.getResearchPrice(), researchIO.getPe(), researchIO.getPb(), researchIO.getResearchDate());

		researchTemplate.create(stockResearch);
	}

}
