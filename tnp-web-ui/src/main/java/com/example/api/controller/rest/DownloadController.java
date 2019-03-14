package com.example.api.controller.rest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.ui.model.UIDownload;
import com.example.util.io.model.DownloadTriggerIO;
import com.example.util.io.model.DownloadTriggerIO.DownloadType;

@RestController
@RequestMapping("/api/download")
public class DownloadController {

	
	@Autowired
	private QueueService queueService;
	
	
	@PostMapping(value = "/initiate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> initiateDownload(@RequestBody UIDownload uiDownload) {

		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String downloadDateStr = uiDownload.getDownloadDate();

        LocalDate downloadDate = LocalDate.parse(downloadDateStr, formatter);

        DownloadType downloadType= null;
        
        if(uiDownload.getDownloadType().equalsIgnoreCase("BHAV")) {
        	downloadType = DownloadType.BHAV;
        }else if (uiDownload.getDownloadType().equalsIgnoreCase("NIFTY50")) {
        	downloadType = DownloadType.NIFTY50;
        }else if (uiDownload.getDownloadType().equalsIgnoreCase("NIFTY100")) {
        	downloadType = DownloadType.NIFTY100;
        }else if (uiDownload.getDownloadType().equalsIgnoreCase("NIFTY250")) {
        	downloadType = DownloadType.NIFTY250;
        }else if (uiDownload.getDownloadType().equalsIgnoreCase("NIFTY500")) {
        	downloadType = DownloadType.NIFTY500;
        }

        DownloadTriggerIO downloadTriggerIO = new DownloadTriggerIO(downloadDate, downloadType);
        
        queueService.send(downloadTriggerIO, QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE);
        
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
