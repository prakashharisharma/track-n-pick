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
public class DownloadNSEBhavProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadNSEBhavProcessor.class);
	
	@Autowired
	private DownloadUtil downloadUtil;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		LOGGER.info("DOWNLOAD FILE PROCESSOR START ...");
		downloadUtil.downloadFile(FileNameUtil.getNSEBhavDownloadURI(),FileNameUtil.getNSEBhavFileName());
		LOGGER.info("DOWNLOAD FILE PROCESSOR END ...");
	}

}
