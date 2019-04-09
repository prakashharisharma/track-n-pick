package com.example.mt.service.async;

import java.util.List;

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

import com.example.model.ledger.ResearchLedger;
import com.example.model.um.UserProfile;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.FundsLedgerService;
import com.example.service.PerformanceLedgerService;
import com.example.service.ResearchLedgerService;
import com.example.service.SectorService;
import com.example.service.StockService;
import com.example.service.UserService;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;
import com.example.util.io.model.UpdateTriggerIO.TriggerType;
import com.example.util.io.model.UpdateTriggerIO;

@Component
public class UpdateTriggerConsumer {

	@Autowired
	private QueueService queueService;

	@Autowired
	private ResearchLedgerService researchLedgerService;

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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTriggerConsumer.class);

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE)
	public void receiveMessage(@Payload UpdateTriggerIO updateTriggerIO, @Headers MessageHeaders headers,
			Message message, Session session) throws InterruptedException {

		LOGGER.info(QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE.toUpperCase() + " : " + updateTriggerIO + " : START");

		if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_RESEARCH) {
			this.updateSectorPEPB();
			
			this.updateResearch();
			
		} else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_CYRO) {
			this.updateCYRO();
		} else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_FYRO) {
			this.updateFYRO();
		} else if (updateTriggerIO.getTrigger() == TriggerType.UPDATE_MONTHLY_VALUE) {
			this.updateMonthlyValue();
		} else if (updateTriggerIO.getTrigger() == TriggerType.RESET_MASTER) {
			this.resetMaster();
		}
		LOGGER.info(QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE.toUpperCase() + " : " + updateTriggerIO + " : END");
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

		List<ResearchLedger> researchLedgerList = researchLedgerService.allActiveResearch();

		// Sell Research Fundamental and Technical
		researchLedgerList.forEach(researchLedger -> {
			ResearchIO researchIO = new ResearchIO();

			researchIO.setNseSymbol(researchLedger.getStock().getNseSymbol());
		

			if (researchLedger.getResearchType() == ResearchType.TECHNICAL) {

				researchIO.setResearchType(ResearchType.TECHNICAL);
				researchIO.setResearchTrigger(ResearchTrigger.SELL);
				this.process(researchIO);
			} else if (researchLedger.getResearchType() == ResearchType.FUNDAMENTAL) {
				researchIO.setResearchType(ResearchType.FUNDAMENTAL);
				researchIO.setResearchTrigger(ResearchTrigger.SELL);
				this.process(researchIO);
			} else {
				LOGGER.debug("INVALID RESEARCH TYPE" + researchLedger.getResearchType()
						+ researchLedger.getStock().getNseSymbol());
			}

		});

		// Buy Research Technical for fundamental

		researchLedgerList.forEach(researchLedger -> {
			ResearchIO researchIO = new ResearchIO();
			researchIO.setNseSymbol(researchLedger.getStock().getNseSymbol());

			if (researchLedger.getResearchType() == ResearchType.FUNDAMENTAL) {
				researchIO.setResearchType(ResearchType.TECHNICAL);
				researchIO.setResearchTrigger(ResearchTrigger.BUY);
				this.process(researchIO);
			}

		});

		// Sell Research Fundamental for Technicals

		researchLedgerList.forEach(researchLedger -> {
			ResearchIO researchIO = new ResearchIO();
			researchIO.setNseSymbol(researchLedger.getStock().getNseSymbol());

			if (researchLedger.getResearchType() == ResearchType.TECHNICAL) {
				researchIO.setResearchType(ResearchType.FUNDAMENTAL);
				researchIO.setResearchTrigger(ResearchTrigger.SELL);
				this.process(researchIO);
			}

		});

	}

	private void process(ResearchIO researchIO) {
		queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
	}
	
	
	private void updateSectorPEPB() {
		sectorService.updateSectorPEPB();
		
		System.out.println("updateSectorPEPB END");
	}
}
