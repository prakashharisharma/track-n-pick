package com.example.integration.processors.download;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.DownloadTriggerIO;
import com.example.util.io.model.DownloadType;

@Service
public class DownloadNifty200Processor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadNifty200Processor.class);

	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		LOGGER.info("DOWNLOAD NIFTY 200 FILE PROCESSOR START ...");
		//downloadUtil.downloadFile(fileNameService.getNSENifty200StocksURI(),fileNameService.getNSENifty200StocksFileName());
		LOGGER.info("DOWNLOAD NIFTY 200 FILE PROCESSOR END ...");
		DownloadTriggerIO downloadTriggerIO = new DownloadTriggerIO(DownloadType.NIFTY200);
		
		queueService.send(downloadTriggerIO, QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE);
	}

}
