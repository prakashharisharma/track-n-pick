package com.example.service.async;

import java.util.List;

import javax.jms.Session;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.model.ledger.ResearchLedgerFundamental;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.FundsLedgerService;
import com.example.service.PerformanceLedgerService;
import com.example.service.ResearchLedgerFundamentalService;
import com.example.service.ResearchLedgerTechnicalService;
import com.example.service.SectorService;
import com.example.service.StockFactorService;
import com.example.service.StockService;
import com.example.service.UserService;
import com.example.util.MiscUtil;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;
import com.example.util.io.model.UpdateTriggerIO;
import com.example.util.io.model.UpdateTriggerIO.TriggerType;

@Component
@Slf4j
public class UpdateTriggerConsumer {

	@Autowired
	private QueueService queueService;

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerService;

	@Autowired
	private ResearchLedgerTechnicalService researchLedgerTechnicalService;
	
	@Autowired
	private FundsLedgerService fundsLedgerService;
	@Autowired
	private UserService userService;

	@Autowired
	private PerformanceLedgerService performanceLedgerService;
	@Autowired
	private StockService stockService;

	@Autowired
	private SectorService sectorService;
	
	@Autowired
	private StockFactorService stockFactorService;
	
	@Autowired
	private MiscUtil miscUtil;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTriggerConsumer.class);

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE)
	public void receiveMessage(@Payload UpdateTriggerIO updateTriggerIO, @Headers MessageHeaders headers,
			Message message, Session session) throws InterruptedException {

		log.info(QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE.toUpperCase() + " : " + updateTriggerIO + " : START");

		if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_RESEARCH) {
			this.updateResearch();
			
		} else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_CYRO) {
			this.updateCYRO();
		} else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_FYRO) {
			this.updateFYRO();
		} else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_MONTHLY_VALUE) {
			this.updateMonthlyValue();
		} else if (updateTriggerIO.getTrigger() == TriggerType.RESET_MASTER) {
			this.resetMaster();
		}else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_FACTORS) {
			this.processFactorsUpdate();
		}else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_SECGORS_PE_PB) {
			this.updateSectorPEPB();
		}
		log.info(QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE.toUpperCase() + " : " + updateTriggerIO + " : END");
	}

	private void processFactorsUpdate() {
		List<Stock> stockList = stockFactorService.stocksToUpdateFactor();
		
		stockList.forEach(stock -> {
			
			queueService.send(stock, QueueConstants.MTQueue.UPDATE_MASTER_FACTOR_QUEUE);
			
			try {
				Thread.sleep(miscUtil.getInterval());
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
		});
			
	}
	
	private void resetMaster() {
		stockService.resetMaster();
	}

	// UPDATE_CYRO, UPDATE_FYRO, UPDATE_MONTHLY_VALUE, UPDATE_RESEARCH
	private void updateCYRO() {
		List<UserProfile> activeUsers = userService.activeUsers();

		for (UserProfile user : activeUsers) {
			fundsLedgerService.addCYROFund(user);
		}

	}

	private void updateFYRO() {

		List<UserProfile> activeUsers = userService.activeUsers();

		for (UserProfile user : activeUsers) {
			fundsLedgerService.addFYROFund(user);
		}
	}

	private void updateMonthlyValue() {
		List<UserProfile> activeUsers = userService.activeUsers();

		for (UserProfile user : activeUsers) {
			performanceLedgerService.updateMonthlyPerformance(user);
		}
	}

	private void updateResearch() {

		this.updateSectorPEPB();

		List<Stock> stockList = stockService.getActiveStocks();

		// Sell Research Fundamental
		stockList.forEach(stock -> {
			ResearchIO researchIO = new ResearchIO();
			researchIO.setNseSymbol(stock.getNseSymbol());
			researchIO.setResearchType(ResearchType.FUNDAMENTAL);
			this.processFundamental(researchIO);
			researchIO.setResearchType(ResearchType.TECHNICAL);
			this.processTechnical(researchIO);
		});


	}


	private void processFundamental(ResearchIO researchIO) {

		queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
	}
	
	private void processTechnical(ResearchIO researchIO) {
		queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
	}

	private void updateSectorPEPB() {

		sectorService.updateSectorPEPB();
		
		//System.out.println("updateSectorPEPB END");
	}
}
