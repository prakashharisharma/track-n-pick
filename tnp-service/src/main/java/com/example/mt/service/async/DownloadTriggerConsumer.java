package com.example.mt.service.async;

import java.io.IOException;
import java.time.DayOfWeek;

import javax.jms.Session;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.external.bhav.service.DownloadBhavService;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.CalendarService;
import com.example.service.FileNameService;
import com.example.util.DownloadUtil;
import com.example.util.io.model.DownloadTriggerIO;
import com.example.util.io.model.DownloadType;
import com.example.util.io.model.TradingSessionIO;

@Component
public class DownloadTriggerConsumer{

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadTriggerConsumer.class);
	
	@Autowired
	private FileNameService fileNameService;
	
	@Autowired
	private DownloadBhavService downloadBhavService;
	
	@Autowired
	private DownloadUtil downloadUtil;
	
	@Autowired
	private CalendarService calendarService;
	
	
	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE)
	public void receiveMessage(@Payload DownloadTriggerIO downloadTriggerIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		System.out.println("DT_CONSUMER START " + downloadTriggerIO);
		
		DownloadType downloadType = downloadTriggerIO.getDownloadType();
		
		String referrerURI = null;
		
		String fileURI = null;
		
		String fileName = null;
		
		if(downloadType == DownloadType.BHAV) {
			
			referrerURI = fileNameService.getNSEBhavReferrerURI(downloadTriggerIO.getDownloadDate());
			fileURI = fileNameService.getNSEBhavDownloadURI(downloadTriggerIO.getDownloadDate());
			fileName = fileNameService.getNSEBhavFileName(downloadTriggerIO.getDownloadDate());
			

			try {
				
				if(downloadTriggerIO.getDownloadDate().getDayOfWeek() == DayOfWeek.SUNDAY || downloadTriggerIO.getDownloadDate().getDayOfWeek() == DayOfWeek.SATURDAY ) {
					LOGGER.info("NOT DOWNLOADING for  " + downloadTriggerIO.getDownloadDate().getDayOfWeek().name());
				}else {
				downloadBhavService.downloadFile(referrerURI, fileURI,fileName);
				
				//
				TradingSessionIO tradingSessionIO = new TradingSessionIO(calendarService.previousWorkingDay(downloadTriggerIO.getDownloadDate()));
				
				
				queueService.send(tradingSessionIO, QueueConstants.HistoricalQueue.UPDATE_TRADING_SESSION_QUEUE);
				}
				//
			} catch (IOException e) {
				LOGGER.debug("EXCEPTION WHILE DOWNLOADING BHAV " + e);
				e.printStackTrace();
			}
			
		}else if(downloadType == DownloadType.NIFTY50){
			
			fileURI = fileNameService.getNSENifty50StocksURI();
			
			fileName = fileNameService.getNSENifty50StocksFileName();
			
			try {
				downloadUtil.downloadFile(fileURI,fileName);
			} catch (IOException e) {
				LOGGER.debug("EXCEPTION WHILE DOWNLOADING "+ downloadType +" : "  + e);
				e.printStackTrace();
			}
			
		}else if(downloadType == DownloadType.NIFTY200){
			
			fileURI = fileNameService.getNSENifty200StocksURI();
			
			fileName = fileNameService.getNSENifty200StocksFileName();
			
			try {
				downloadUtil.downloadFile(fileURI,fileName);
			} catch (IOException e) {
				LOGGER.debug("EXCEPTION WHILE DOWNLOADING "+ downloadType +" : "  + e);
				e.printStackTrace();
			}
			
		}else if(downloadType == DownloadType.NIFTY500){
			
			fileURI = fileNameService.getNSEIndex500StocksURI();
			
			fileName = fileNameService.getNSEIndex500StocksFileName();
			
			try {
				downloadUtil.downloadFile(fileURI,fileName);
			} catch (IOException e) {
				LOGGER.debug("EXCEPTION WHILE DOWNLOADING "+ downloadType +" : "  + e);
				e.printStackTrace();
			}
			
		}
		else {
			LOGGER.debug("NOT A VALID TYPE : " + downloadType);
		}
		
	}
}
