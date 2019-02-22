package com.example.integration.processors.update;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.TriggerType;
import com.example.util.io.model.UpdateTriggerIO;

// This processor will update research List
@Service
public class ResearchUpdateProcessor implements Processor{

	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		System.out.println("EmailResearchListProcessor START");
	
		UpdateTriggerIO updateTriggerIO = new UpdateTriggerIO(TriggerType.UPDATE_RESEARCH);
		
		queueService.send(updateTriggerIO, QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE);
		
		System.out.println("EmailResearchListProcessor END");
		
	}

}
