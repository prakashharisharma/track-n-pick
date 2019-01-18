package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.model.StockMaster;
import com.example.processors.UpdateNifty50Processor;
import com.example.processors.UpdateStockMasterProcessor;


@Component
public class StockMasterUpdateRoute extends RouteBuilder{

	@Autowired
	private UpdateStockMasterProcessor updateStockMasterProcessor;
	
	@Autowired
	private UpdateNifty50Processor updateNifty50Processor;
	
	@Override
	public void configure() throws Exception {
		final DataFormat stockMasterBindy = new BindyCsvDataFormat(StockMaster.class);

		//parse the csv file and push list to jmsqueue
		from("file:data/master/stocks/csv?noop=false")
			.unmarshal(stockMasterBindy)
		.to("jms:queue.master.csv.stocks");
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:queue.master.csv.stocks")
			.process(updateStockMasterProcessor).log("${body}");
		
		
		//NIFTY50 START
		//parse the csv file and push list to jmsqueue
		from("file:data/nifty50/stocks/csv?noop=false")
			.unmarshal(stockMasterBindy)
		.to("jms:queue.nifty50.csv.stocks");
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:queue.nifty50.csv.stocks")
			.process(updateNifty50Processor).log("${body}");
		
		
		
		//NIFTY50 END
		
	}

}
