package com.example.integration.processors.master;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.integration.model.StockMasterIN;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.StockIO.IndiceType;
import com.example.util.io.model.StockIO;
import com.example.util.io.model.UpdateTriggerIO;
import com.example.util.io.model.UpdateTriggerIO.TriggerType;

@Service
public class UpdateNifty500Processor implements Processor{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNifty500Processor.class);

	@Autowired
	private QueueService queueService;

	
	@Override
	public void process(Exchange exchange) throws Exception {
		LOGGER.info("UpdateNifty500Processor : START");
		
		UpdateTriggerIO updateTriggerIO = new UpdateTriggerIO(TriggerType.RESET_MASTER);
		
		LOGGER.debug("UpdateNifty200Processor : Queuing to reset master ");
		
		queueService.send(updateTriggerIO, QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE);
		
		Thread.sleep(5000);
		
		//ADD TO NIFTY 500
		
		@SuppressWarnings("unchecked")
		List<StockMasterIN> nseMasterList = (List<StockMasterIN>) exchange.getIn().getBody();

		
		nseMasterList.forEach(stockIn -> {
			StockIO stockIO = new StockIO(stockIn.getCompanyName(), stockIn.getSector(), stockIn.getNseSymbol(), stockIn.getSeries(), stockIn.getIsin(), IndiceType.NIFTY500);
			
			LOGGER.debug("UpdateNifty200Processor : Queuing to update master " + stockIO);
			
			queueService.send(stockIO, QueueConstants.MTQueue.UPDATE_MASTER_STOCK_QUEUE);
			
		});
		
		LOGGER.info("UpdateNifty500Processor : END");
	}

}
