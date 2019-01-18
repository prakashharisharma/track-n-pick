package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.model.StockPrice;
import com.example.processors.RetainMasterRecordsProcessor;
import com.example.processors.TempStoragePriceUpdateProcessor;
import com.example.processors.UpdateStockPriceProcessor;
import com.example.processors.UpdateStoragePriceProcessor;

@Component
public class BhavUpdateRoute extends RouteBuilder{

	@Autowired
	private RetainMasterRecordsProcessor retainMasterRecordsProcessor;

	@Autowired
	private UpdateStockPriceProcessor updateStockPriceProcessor;
	
	@Autowired
	private UpdateStoragePriceProcessor updateStoragePriceProcessor;
	
	@Autowired
	private TempStoragePriceUpdateProcessor tempStoragePriceUpdateProcessor;
	
	@Override
	public void configure() throws Exception {

		final DataFormat bindy = new BindyCsvDataFormat(StockPrice.class);
		
		//STORAGE BEGIN
		
		//parse the csv file and push list to jmsqueue
		from("jms:queue.storage.bhav.stockprice")
			.process(updateStoragePriceProcessor)
			.log("${body}");
		
		
		//parse the csv file and push list to jmsqueue
		from("file:data/bhav/nse/temp/csv?noop=false")
			.unmarshal(bindy)
		.to("jms:queue.bhav.csv.stocks.temp");
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:queue.bhav.csv.stocks.temp")
			.process(tempStoragePriceUpdateProcessor)
			.log("${body}");
		
		// STORAGE END
		
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