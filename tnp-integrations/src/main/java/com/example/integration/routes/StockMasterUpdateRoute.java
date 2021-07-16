package com.example.integration.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.model.StockMasterIN;
import com.example.integration.model.StockPriceIN;
import com.example.mq.constants.QueueConstants;
import com.example.integration.processors.master.UpdateNifty1000Processor;
import com.example.integration.processors.master.UpdateNifty100Processor;
import com.example.integration.processors.master.UpdateNifty250Processor;
import com.example.integration.processors.master.UpdateNifty50Processor;
import com.example.integration.processors.master.UpdateNifty750Processor;
import com.example.integration.processors.master.UpdateNifty500Processor;
import com.example.util.FileLocationConstants;

@Component
public class StockMasterUpdateRoute extends RouteBuilder {

	@Autowired
	private UpdateNifty1000Processor updateNifty1000Processor;
	
	@Autowired
	private UpdateNifty750Processor updateNifty750Processor;
	
	@Autowired
	private UpdateNifty500Processor updateNifty500Processor;

	@Autowired
	private UpdateNifty50Processor updateNifty50Processor;

	@Autowired
	private UpdateNifty250Processor updateNifty250Processor;
	
	@Autowired
	private UpdateNifty100Processor updateNifty100Processor;

	
	@Override
	public void configure() throws Exception {
		
		final DataFormat stockMasterBindy = new BindyCsvDataFormat(StockMasterIN.class);

		final DataFormat stockPriceBindy = new BindyCsvDataFormat(StockPriceIN.class);

		// parse the csv file and push list to jmsqueue
		from("file:"+FileLocationConstants.NIFTY_1000_DOWNLOAD_LOCATION+"?noop=false").unmarshal(stockPriceBindy)
		.to("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY1000_QUEUE);

		// parse the records queue and retain all the records that are in master and
		// push to processedrecords queue
		from("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY1000_QUEUE).process(updateNifty1000Processor)
			.log("${body}");
		
		
		// parse the csv file and push list to jmsqueue
		from("file:"+FileLocationConstants.NIFTY_750_DOWNLOAD_LOCATION+"?noop=false").unmarshal(stockPriceBindy)
		.to("jms:"+QueueConstants.IntegrationQueue.UPDATE_NIFTY750_QUEUE);

		// parse the records queue and retain all the records that are in master and
		// push to processedrecords queue
		from("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY750_QUEUE).process(updateNifty750Processor)
			.log("${body}");
		
		
		// parse the csv file and push list to jmsqueue
		from("file:" + FileLocationConstants.NIFTY_500_DOWNLOAD_LOCATION + "?noop=false").unmarshal(stockMasterBindy)
				.to("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY500_QUEUE);

		// parse the records queue and retain all the records that are in master and
		// push to processedrecords queue
		from("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY500_QUEUE).process(updateNifty500Processor)
				.log("${body}");

		// NIFTY50 START
		// parse the csv file and push list to jmsqueue
		from("file:" + FileLocationConstants.NIFTY_50_DOWNLOAD_LOCATION + "?noop=false").unmarshal(stockMasterBindy)
				.to("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY50_QUEUE);

		// parse the records queue and retain all the records that are in master and
		// push to processedrecords queue
		from("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY50_QUEUE).process(updateNifty50Processor)
				.log("${body}");

		// NIFTY50END

		// NIFTY100 START
		// parse the csv file and push list to jmsqueue
		from("file:" + FileLocationConstants.NIFTY_100_DOWNLOAD_LOCATION + "?noop=false").unmarshal(stockMasterBindy)
				.to("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY100_QUEUE);

		// parse the records queue and retain all the records that are in master and
		// push to processedrecords queue
		from("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY100_QUEUE).process(updateNifty100Processor)
				.log("${body}");

		// NIFTY100 END

		// NIFTY200 START
		// parse the csv file and push list to jmsqueue
		from("file:" + FileLocationConstants.NIFTY_250_DOWNLOAD_LOCATION + "?noop=false").unmarshal(stockMasterBindy)
				.to("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY250_QUEUE);

		// parse the records queue and retain all the records that are in master and
		// push to processedrecords queue
		from("jms:" + QueueConstants.IntegrationQueue.UPDATE_NIFTY250_QUEUE).process(updateNifty250Processor)
				.log("${body}");

		// NIFTY200 END

	}

}
