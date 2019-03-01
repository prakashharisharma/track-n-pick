package com.example.integration.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.model.StockPriceIN;
import com.example.mq.constants.QueueConstants;
import com.example.integration.processors.bhav.BhavUpdateProcessor;
import com.example.util.FileLocationConstants;

@Component
public class BhavUpdateRoute extends RouteBuilder {

	@Autowired
	private BhavUpdateProcessor retainMasterRecordsProcessor;
	
	@Override
	public void configure() throws Exception {

		final DataFormat bindy = new BindyCsvDataFormat(StockPriceIN.class);

		// parse the csv file and push list to jmsqueue
		from("file:"+FileLocationConstants.BHAV_CSV_LOCATION+"?noop=false").unmarshal(bindy).to("jms:"+QueueConstants.IntegrationQueue.PROCESS_BHAV_QUEUE);

		//from("jms:queue.bhav.csv.stocks").process(retainMasterRecordsProcessor).log("${body}");
		
		// parse the records queue and retain all the records that are in master and
		// push to processedrecords queue
		from("jms:"+QueueConstants.IntegrationQueue.PROCESS_BHAV_QUEUE).process(retainMasterRecordsProcessor).log("${body}");

	}
}