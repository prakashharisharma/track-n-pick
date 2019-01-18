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
public class DownloadNSE500StocksMasterProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadNSE500StocksMasterProcessor.class);
	
	@Autowired
	private DownloadUtil downloadUtil;
	
	@Autowired
	private FileNameService fileNameService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		LOGGER.info("DOWNLOAD FILE PROCESSOR START ...");
		downloadUtil.downloadFile(fileNameService.getNSEIndex500StocksURI(),fileNameService.getNSEIndex500StocksFileName());
		LOGGER.info("DOWNLOAD FILE PROCESSOR END ...");

	}

}
