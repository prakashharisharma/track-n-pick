package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;

import com.example.model.StockPrice;
import com.example.processors.PrintBodyProcessor;
import com.example.processors.RetainMasterRecordsProcessor;

public class CamelBindyCsvRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {

		final DataFormat bindy = new BindyCsvDataFormat(StockPrice.class);
		
		//parse the csv file and push list to jmsqueue
		from("file:data/inbox/csv?noop=false")
			.unmarshal(bindy)
		.to("jms:queue.order.records");
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:queue.order.records")
			.process(new RetainMasterRecordsProcessor())
		.to("jms:queue.order.processedrecords");
		
		//split the processedrecords and push each record to record queue
		from("jms:queue.order.processedrecords")
			.split(body()).streaming()
		.to("jms:queue.order.record");
		
		//operation on individual record
		//from("jms:queue.order.record").process(new PrintBodyProcessor())
		//.log("${body}");
	}
}