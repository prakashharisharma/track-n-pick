package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.model.StockPrice;
import com.example.processors.UpdateStockPriceProcessor;
import com.example.processors.RetainMasterRecordsProcessor;

@Component
public class BhavUpdateRoute extends RouteBuilder{

	@Autowired
	private RetainMasterRecordsProcessor retainMasterRecordsProcessor;
	
	@Autowired
	private UpdateStockPriceProcessor updateStockPriceProcessor;
	
	@Override
	public void configure() throws Exception {

		final DataFormat bindy = new BindyCsvDataFormat(StockPrice.class);
		
		//parse the csv file and push list to jmsqueue
		from("file:data/bhav/nse/csv?noop=false")
			.unmarshal(bindy)
		.to("jms:queue.bhav.csv.stocks");
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:queue.bhav.csv.stocks")
			.process(retainMasterRecordsProcessor)
		.to("jms:queue.bhav.csv.stocksinmaster");
		
		//split the processedrecords and push each record to record queue
		from("jms:queue.bhav.csv.stocksinmaster")
			.split(body()).streaming()
		.to("jms:queue.bhav.csv.stock");
		
		//operation on individual record
		from("jms:queue.bhav.csv.stock").process(updateStockPriceProcessor).log("${body}");
		
	}
}