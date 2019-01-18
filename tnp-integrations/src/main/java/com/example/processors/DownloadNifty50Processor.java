package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.service.FileNameService;
import com.example.util.DownloadUtil;

@Service
public class DownloadNifty50Processor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadNifty50Processor.class);
	
	@Autowired
	private DownloadUtil downloadUtil;
	
	@Autowired
	private FileNameService fileNameService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		LOGGER.info("DOWNLOAD NIFTY 50 FILE PROCESSOR START ...");
		downloadUtil.downloadFile(fileNameService.getNSENifty50StocksURI(),fileNameService.getNSENifty50StocksFileName());
		LOGGER.info("DOWNLOAD NIFTY 50 FILE PROCESSOR END ...");

	}

}
