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

import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.CrossOverLedger.CrossOverCategory;
import com.example.model.ledger.ValuationLedger.Category;
import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.BreakoutLedgerService;
import com.example.service.CrossOverLedgerService;
import com.example.service.ResearchLedgerService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;

@Component
public class ResearchTechnicalConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchTechnicalConsumer.class);

	@Autowired
	private StockService stockService;
	
	@Autowired
	private CrossOverLedgerService crossOverLedgerService;

	@Autowired
	private TechnicalsResearchService technicalsResearchService;

	@Autowired
	private ResearchLedgerService researchLedgerService;

	@Autowired
	private BreakoutLedgerService breakoutLedgerService;
	
	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_TECHNICAL_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.info("TECHCOMSUMER 1" + researchIO.getNseSymbol() +" " + researchIO.getResearchType());
	
		if (researchIO.getResearchType() == ResearchType.TECHNICAL) {
			Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());
			
			this.researchTechnical(stock, researchIO.getResearchTrigger());

		} else if (researchIO.getResearchType() == ResearchType.TECHNICAL_TWEAK) {
			
			Stock stock = stockService.getStockByNseSymbol(researchIO.getNseSymbol());
			
			this.researchTechnicalTweak(stock, researchIO.getResearchTrigger());

			this.researchPositiveBreakout(stock);

			this.researchNegativeBreakout(stock);

		}else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}
	}

	private void researchTechnical(Stock stock, ResearchTrigger researchTrigger) {

		if (researchTrigger == ResearchTrigger.BUY) {
			
			if (technicalsResearchService.isBullishCrossOver200(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverCategory.CROSS200);

					this.addToResearchLedgerTechnical(stock, Category.STRONG);
					this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.BUY);
			} 
			
			if (technicalsResearchService.isBullishCrossOver100(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverCategory.CROSS100);


			}

		} else if (researchTrigger == ResearchTrigger.SELL) {
			
			if (technicalsResearchService.isBearishCrossover200(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS200);
				
			} 
			
			if (technicalsResearchService.isBearishCrossover100(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS100);
				
				
					this.updateResearchLedgerTechnical(stock, Category.STRONG);
					this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.SELL);
				

			}

		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}

	private void researchTechnicalTweak(Stock stock, ResearchTrigger researchTrigger) {
	
		LOGGER.info("TECHCOMSUMER 2" + stock.getNseSymbol() +" " + researchTrigger);
		
			if (technicalsResearchService.isBullishCrossOver50(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverCategory.CROSS50);
				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.BUY);
			}
			
			
			if (technicalsResearchService.isBullishCrossOver200(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverCategory.CROSS200);

				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.BUY);

			} 
			
			if (technicalsResearchService.isBullishCrossOver100(stock)) {

				this.addBullishCrossOverLedger(stock, CrossOverCategory.CROSS100);
				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.BUY);

			}
	

			if (technicalsResearchService.isBearishCrossover50(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS50);
				
				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.SELL);
				
			} 
			
			if (technicalsResearchService.isBearishCrossover200(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS200);
				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.SELL);

			} 
			
			if (technicalsResearchService.isBearishCrossover100(stock)) {

				this.addBearishCrossOverLedger(stock, CrossOverCategory.CROSS100);
				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.SELL);

			}

	}
	
	private void researchPositiveBreakout(Stock stock) {

		LOGGER.info("RESEARCH_CONSUMER researchPositiveBreakout " + stock.getNseSymbol());

		if (technicalsResearchService.isPositiveBreakout200(stock)) {
			breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.CROSS200);
		}
		if (technicalsResearchService.isPositiveBreakout100(stock)) {
			breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.CROSS100);
		}
		if (technicalsResearchService.isPositiveBreakout50(stock)) {
			breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.CROSS50);
		}

	}

	private void researchNegativeBreakout(Stock stock) {

		LOGGER.info("RESEARCH_CONSUMER researchNegativeBreakout " + stock.getNseSymbol());

		if (technicalsResearchService.isNegativeBreakout50(stock)) {
			breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.CROSS50);
		}
		if (technicalsResearchService.isNegativeBreakout100(stock)) {
			breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.CROSS100);
		}
		if (technicalsResearchService.isNegativeBreakout200(stock)) {
			breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.CROSS200);
		}

	}
	
	private void updateResearchLedgerTechnical(Stock stock, Category category) {
		researchLedgerService.updateResearch(stock, ResearchType.TECHNICAL,category);
	}

	private void addToResearchLedgerTechnical(Stock stock, Category category) {
		researchLedgerService.addResearch(stock, ResearchType.TECHNICAL,category);
	}

	private void addBullishCrossOverLedger(Stock stock, CrossOverCategory crossOverCategory) {
		String nseSymbol = stock.getNseSymbol();

		LOGGER.info("TECHCOMSUMER 3" + stock.getNseSymbol() +" " + crossOverCategory);
		
		
		if (stockService.isActive(nseSymbol)) {

			LOGGER.info("TECHCOMSUMER 4" + stock.getNseSymbol() +" " + crossOverCategory);
			
			
			crossOverLedgerService.addBullish(stock, crossOverCategory);

		} else {
			LOGGER.debug("NOT IN MASTER, IGNORED..." + nseSymbol);
		}
	}


	private void addBearishCrossOverLedger(Stock stock, CrossOverCategory crossOverCategory) {
		crossOverLedgerService.addBearish(stock, crossOverCategory);

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
