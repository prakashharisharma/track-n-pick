package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.util.DownloadUtil;
import com.example.util.FileNameUtil;

@Service
public class DownloadNSE500StocksMasterProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadNSE500StocksMasterProcessor.class);
	
	@Autowired
	private DownloadUtil downloadUtil;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		LOGGER.info("DOWNLOAD FILE PROCESSOR START ...");
		downloadUtil.downloadFile(FileNameUtil.getNSEIndex500StocksURI(),FileNameUtil.getNSEIndex500StocksFileName());
		LOGGER.info("DOWNLOAD FILE PROCESSOR END ...");

	}

}