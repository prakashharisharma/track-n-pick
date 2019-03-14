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

@Service
public class DownloadNifty250Processor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadNifty250Processor.class);

	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		LOGGER.info("DOWNLOAD NIFTY 200 FILE PROCESSOR : START");
		
		DownloadTriggerIO downloadTriggerIO = new DownloadTriggerIO(DownloadTriggerIO.DownloadType.NIFTY250);
		
		LOGGER.debug("DOWNLOAD NIFTY 200 FILE PROCESSOR : Queuinh to Download ... " + downloadTriggerIO);
		
		queueService.send(downloadTriggerIO, QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE);
		
		LOGGER.info("DOWNLOAD NIFTY 200 FILE PROCESSOR : END");
	}

}
