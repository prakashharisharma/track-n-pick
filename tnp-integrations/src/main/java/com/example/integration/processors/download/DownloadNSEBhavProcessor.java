package com.example.integration.processors.download;

import java.time.LocalDate;

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
public class DownloadNSEBhavProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadNSEBhavProcessor.class);
	

	
	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		LOGGER.info("DOWNLOAD FILE PROCESSOR START ...");
		//downloadUtil.downloadFile(fileNameService.getNSEBhavDownloadURI(),fileNameService.getNSEBhavFileName());
		//downloadBhavService.downloadFile(fileNameService.getNSEBhavReferrerURI(), fileNameService.getNSEBhavDownloadURI(),fileNameService.getNSEBhavFileName());
		
		DownloadTriggerIO downloadTriggerIO = new DownloadTriggerIO(LocalDate.now().minusDays(1), DownloadTriggerIO.DownloadType.BHAV);
		
		queueService.send(downloadTriggerIO, QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE);
		
		LOGGER.info("DOWNLOAD FILE PROCESSOR END ...");
	}

}
