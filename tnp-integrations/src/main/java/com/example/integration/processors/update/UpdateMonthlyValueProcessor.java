package com.example.integration.processors.update;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.UpdateTriggerIO.TriggerType;
import com.example.util.io.model.UpdateTriggerIO;

@Service
public class UpdateMonthlyValueProcessor implements Processor{
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMonthlyValueProcessor.class);
	
	@Autowired
	private QueueService queueService;
	@Override
	public void process(Exchange arg0) throws Exception {
		LOGGER.info("UpdateMonthlyValueProcessor : START");

		UpdateTriggerIO updateTriggerIO = new UpdateTriggerIO(TriggerType.UPDATE_MONTHLY_VALUE);
		
		LOGGER.debug("UpdateMonthlyValueProcessor : Queuinh to Update ... " + updateTriggerIO);
		
		queueService.send(updateTriggerIO, QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE);
		
		LOGGER.info("UpdateMonthlyValueProcessor : END");
		
	}

}
