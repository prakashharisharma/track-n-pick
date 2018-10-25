package com.example.processors;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.example.model.StockPrice;
import com.example.util.SpringRESTClient;

public class RetainMasterRecordsProcessor implements Processor {
	
    	@Override
		public void process(Exchange exchange) throws Exception {
		
		@SuppressWarnings("unchecked")
		List<StockPrice> orderList = (List<StockPrice>) exchange.getIn().getBody();
		
		List<StockPrice> masterList = SpringRESTClient.getStocksMaster();
		
		orderList.retainAll(masterList);

		exchange.getOut().setBody(orderList);
		
	}
}
