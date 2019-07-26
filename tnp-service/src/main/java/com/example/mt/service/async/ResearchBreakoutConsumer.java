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
import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.BreakoutLedgerService;
import com.example.service.PortfolioService;
import com.example.service.ResearchLedgerFundamentalService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;
import com.example.util.io.model.StockTechnicalsIO;

@Component
public class ResearchBreakoutConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchBreakoutConsumer.class);

	@Autowired
	private StockService stockService;

	@Autowired
	private TechnicalsResearchService technicalsResearchService;

	@Autowired
	private BreakoutLedgerService breakoutLedgerService;

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerFundamentalService;

	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private QueueService queueService;

	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_BREAKOUT_QUEUE)
	public void receiveMessage(@Payload StockTechnicalsIO stockTechnicalsIO, @Headers MessageHeaders headers,
			Message message, Session session) throws InterruptedException {

		Stock stock = stockService.getStockByNseSymbol(stockTechnicalsIO.getNseSymbol());

		this.researchPositiveBreakout(stock);

		this.researchNegativeBreakout(stock);

		this.processResearch(stock);
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

	private void processResearch(Stock stock) {

	//	if (researchLedgerFundamentalService.isResearchActive(stock)) {

			//LOGGER.info("RESEARCH_CONSUMER processToTechnicals, active in research processing to Technicals research " + stock.getNseSymbol());
			
			ResearchIO researchIO = new ResearchIO(stock.getNseSymbol(), ResearchType.TECHNICAL, ResearchTrigger.BUY);

			queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
			
		//}else if(portfolioService.isPortfolioStock(stock)) {
			
			//LOGGER.info("RESEARCH_CONSUMER processToTechnicals, active in portfolio processing to Technicals research " + stock.getNseSymbol());
			
		//	ResearchIO researchIO = new ResearchIO(stock.getNseSymbol(), ResearchType.TECHNICAL, ResearchTrigger.BUY);

		//	queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
		//}
		//else {
		//	LOGGER.info("RESEARCH_CONSUMER processToTechnicals, Skip as This is not Active in research or Portfolio " + stock.getNseSymbol());
		//}
	}//
}
