package com.example.integration.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.model.StockMasterIN;
import com.example.mq.constants.QueueConstants;
import com.example.integration.processors.master.UpdateNifty200Processor;
import com.example.integration.processors.master.UpdateNifty50Processor;
import com.example.integration.processors.master.UpdateNifty500Processor;
import com.example.util.FileLocationConstants;


@Component
public class StockMasterUpdateRoute extends RouteBuilder{

	@Autowired
	private UpdateNifty500Processor updateStockMasterProcessor;
	
	@Autowired
	private UpdateNifty50Processor updateNifty50Processor;
	
	@Autowired
	private UpdateNifty200Processor updateNifty200Processor;
	
	@Override
	public void configure() throws Exception {
		final DataFormat stockMasterBindy = new BindyCsvDataFormat(StockMasterIN.class);

		//parse the csv file and push list to jmsqueue
		from("file:"+FileLocationConstants.NIFTY_500_DOWNLOAD_LOCATION+"?noop=false")
			.unmarshal(stockMasterBindy)
		.to("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY500_QUEUE);
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY500_QUEUE)
			.process(updateStockMasterProcessor).log("${body}");
		
		
		//NIFTY50 START
		//parse the csv file and push list to jmsqueue
		from("file:"+FileLocationConstants.NIFTY_50_DOWNLOAD_LOCATION+"?noop=false")
			.unmarshal(stockMasterBindy)
		.to("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY50_QUEUE);
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY50_QUEUE)
		.process(updateNifty50Processor).log("${body}");
				
		//NIFTY50END
		
		//NIFTY200 START
		//parse the csv file and push list to jmsqueue
		from("file:"+FileLocationConstants.NIFTY_200_DOWNLOAD_LOCATION+"?noop=false")
			.unmarshal(stockMasterBindy)
		.to("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY200_QUEUE);
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY200_QUEUE)
			.process(updateNifty200Processor).log("${body}");
		
		//NIFTY200 END
		
		
	}

}
