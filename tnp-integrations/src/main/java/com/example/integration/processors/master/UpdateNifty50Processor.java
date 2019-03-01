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

@Service
public class UpdateNifty50Processor implements Processor {

private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNifty50Processor.class);
	

	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		LOGGER.info("UpdateNifty50Processor : START");
		
		@SuppressWarnings("unchecked")
		List<StockMasterIN> nseNifty50List = (List<StockMasterIN>) exchange.getIn().getBody();
		nseNifty50List.forEach(stockIn -> {
			StockIO stockIO = new StockIO(stockIn.getCompanyName(), stockIn.getSector(), stockIn.getNseSymbol(), stockIn.getSeries(), stockIn.getIsin(), IndiceType.NIFTY50);
			
			LOGGER.debug("UpdateNifty50Processor : Queuing to update master " + stockIO);
			
			queueService.send(stockIO, QueueConstants.MTQueue.UPDATE_MASTER_STOCK_QUEUE);
			
		});
		LOGGER.info("UpdateNifty50Processor : END");
	}
	
}
