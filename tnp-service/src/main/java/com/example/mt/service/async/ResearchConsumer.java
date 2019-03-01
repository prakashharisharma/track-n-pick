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

import com.example.model.ledger.UndervalueLedger;
import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.repo.ledger.UndervalueLedgerRepository;
import com.example.service.CleanseService;
import com.example.service.ResearchLedgerService;
import com.example.service.RuleService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.util.FormulaService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;;

@Component
public class ResearchConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchConsumer.class);

	@Autowired
	private CleanseService cleanseService;

	@Autowired
	private StockService stockService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private UndervalueLedgerRepository undervalueLedgerRepository;

	@Autowired
	private FormulaService formulaService;

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

				this.addToResearchLedgerTechnical(stock);
				
				this.addToResearchHistory(stock, ResearchType.TECHNICAL, ResearchTrigger.BUY);

			}
		} else if (researchTrigger == ResearchTrigger.SELL) {

			if (technicalsResearchService.isBearishCrossover(stock)) {
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

		if (cleanseService.isNifty500(nseSymbol)) {
			double currentPrice = stock.getStockPrice().getCurrentPrice();

			double bookValue = stock.getStockFactor().getBookValue();

			double eps = stock.getStockFactor().getEps();

			double pe = formulaService.calculatePe(currentPrice, eps);

			double pb = formulaService.calculatePb(currentPrice, bookValue);

			UndervalueLedger undervalueLedger = new UndervalueLedger();

			undervalueLedger.setStockId(stock);
			undervalueLedger.setPb(pb);
			undervalueLedger.setPe(pe);
			undervalueLedger.setResearchDate(LocalDate.now());

			undervalueLedgerRepository.save(undervalueLedger);

		} else {
			LOGGER.debug("NOT IN MASTER, IGNORED..." + nseSymbol);
		}

		

	}
	
	private void addToResearchHistory(Stock stock, ResearchType researchType, ResearchTrigger researchTrigger ) {
		
		double currentPrice = stock.getStockPrice().getCurrentPrice();

		double bookValue = stock.getStockFactor().getBookValue();

		double eps = stock.getStockFactor().getEps();

		double pe = formulaService.calculatePe(currentPrice, eps);

		double pb = formulaService.calculatePb(currentPrice, bookValue);
		
		ResearchIO researchIO = new ResearchIO(stock.getNseSymbol(), researchType, researchTrigger, currentPrice, pe, pb);
		
		
		if(!researchLedgerService.isResearchExist(stock, researchType, researchTrigger)){
			queueService.send(researchIO, QueueConstants.HistoricalQueue.UPDATE_RESEARCH_QUEUE);
		}else {
			LOGGER.debug("RESEARCH ALREADY EXIST..." + stock.getNseSymbol());
		}
		
	}

}
