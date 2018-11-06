package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.util.DownloadUtil;
import com.example.util.FileNameUtil;

@Service
public class DownloadNSEBhavProcessor implements Processor {

	@Autowired
	private DownloadUtil downloadUtil;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		downloadUtil.downloadFile(FileNameUtil.getNSEBhavDownloadURI(),FileNameUtil.getNSEBhavFileName());
	}

}
