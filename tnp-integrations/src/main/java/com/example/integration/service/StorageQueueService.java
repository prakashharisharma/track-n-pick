package com.example.integration.service;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.StockPrice;

@Service
public class StorageQueueService {

	@Autowired
	CamelContext camelContext;

    public void processPrce(StockPrice stockPrice) {
    	//Endpoint myQueue = (Endpoint) SpringApplicationContextHelper.getInstance().getBean("myQueue");
       
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("jms:queue.storage.bhav.stockprice", stockPrice);
    }
}
