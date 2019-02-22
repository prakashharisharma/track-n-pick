package com.example.integration.processors.update;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.TriggerType;
import com.example.util.io.model.UpdateTriggerIO;

@Service
public class UpdateMonthlyValueProcessor implements Processor{

	@Autowired
	private QueueService queueService;
	@Override
	public void process(Exchange arg0) throws Exception {
		

		UpdateTriggerIO updateTriggerIO = new UpdateTriggerIO(TriggerType.UPDATE_MONTHLY_VALUE);
		
		queueService.send(updateTriggerIO, QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE);
		
	}

}
