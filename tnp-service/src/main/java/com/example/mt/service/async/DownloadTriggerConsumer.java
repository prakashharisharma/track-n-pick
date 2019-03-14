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
import com.example.model.ledger.DownloadLedger;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.CalendarService;
import com.example.service.DownloadLedgerService;
import com.example.service.FileNameService;
import com.example.service.SectorService;
import com.example.util.DownloadUtil;
import com.example.util.io.model.DownloadTriggerIO;
import com.example.util.io.model.DownloadTriggerIO.DownloadType;
import com.example.util.io.model.TradingSessionIO;

@Component
public class DownloadTriggerConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadTriggerConsumer.class);

	@Autowired
	private FileNameService fileNameService;

	@Autowired
	private DownloadBhavService downloadBhavService;

	@Autowired
	private SectorService sectorService;
	
	@Autowired
	private DownloadUtil downloadUtil;

	@Autowired
	private CalendarService calendarService;

	@Autowired
	private DownloadLedgerService downloadLedgerService;

	@Autowired
	private QueueService queueService;

	@JmsListener(destination = QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE)
	public void receiveMessage(@Payload DownloadTriggerIO downloadTriggerIO, @Headers MessageHeaders headers,
			Message message, Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE.toUpperCase() +" : " + downloadTriggerIO + " : START");

		DownloadType downloadType = downloadTriggerIO.getDownloadType();

		String referrerURI = null;

		String fileURI = null;

		String fileName = null;

		if (downloadType == DownloadType.BHAV) {

			referrerURI = fileNameService.getNSEBhavReferrerURI(downloadTriggerIO.getDownloadDate());
			fileURI = fileNameService.getNSEBhavDownloadURI(downloadTriggerIO.getDownloadDate());
			fileName = fileNameService.getNSEBhavFileName(downloadTriggerIO.getDownloadDate());

			try {
				if (downloadLedgerService.isBhavDownloadExist(downloadTriggerIO.getDownloadDate())) {
					LOGGER.info("NOT DOWNLOADING for  " + downloadTriggerIO.getDownloadDate().getDayOfWeek().name()
							+ " BHAV ALREADY DOWNLOADED");
				}else if (calendarService.isHoliday(downloadTriggerIO.getDownloadDate())) {
					LOGGER.info("NOT DOWNLOADING for  " + downloadTriggerIO.getDownloadDate().getDayOfWeek().name()
							+ " NSE Holiday");
				}
				else if (downloadTriggerIO.getDownloadDate().getDayOfWeek() == DayOfWeek.SUNDAY
						|| downloadTriggerIO.getDownloadDate().getDayOfWeek() == DayOfWeek.SATURDAY) {
					LOGGER.info("NOT DOWNLOADING for  " + downloadTriggerIO.getDownloadDate().getDayOfWeek().name());
				} 

				else {
					downloadBhavService.downloadFile(referrerURI, fileURI, fileName);

					this.updateDownloadLedger(downloadTriggerIO);
					
					this.updateSectorPEPB();
					
					//
					TradingSessionIO tradingSessionIO = new TradingSessionIO(downloadTriggerIO.getDownloadDate());

					queueService.send(tradingSessionIO, QueueConstants.HistoricalQueue.UPDATE_TRADING_SESSION_QUEUE);
				}
				//
			} catch (IOException e) {
				LOGGER.error("EXCEPTION WHILE DOWNLOADING BHAV " + e);
			}

		} else if (downloadType == DownloadType.NIFTY50) {

			fileURI = fileNameService.getNSENifty50StocksURI();

			fileName = fileNameService.getNSENifty50StocksFileName();

			try {
				downloadUtil.downloadFile(fileURI, fileName);
				
				this.updateDownloadLedger(downloadTriggerIO);
			} catch (IOException e) {
				LOGGER.error("EXCEPTION WHILE DOWNLOADING " + downloadType + " : " + e);
				
			}

		}else if (downloadType == DownloadType.NIFTY100) {

			fileURI = fileNameService.getNSENifty100StocksURI();

			fileName = fileNameService.getNSENifty100StocksFileName();

			try {
				downloadUtil.downloadFile(fileURI, fileName);
				this.updateDownloadLedger(downloadTriggerIO);
			} catch (IOException e) {
				LOGGER.error("EXCEPTION WHILE DOWNLOADING " + downloadType + " : " + e);
			}

		}
		else if (downloadType == DownloadType.NIFTY250) {

			fileURI = fileNameService.getNSENifty250StocksURI();

			fileName = fileNameService.getNSENifty250StocksFileName();

			try {
				downloadUtil.downloadFile(fileURI, fileName);
				this.updateDownloadLedger(downloadTriggerIO);
			} catch (IOException e) {
				LOGGER.error("EXCEPTION WHILE DOWNLOADING " + downloadType + " : " + e);
			}

		} else if (downloadType == DownloadType.NIFTY500) {

			fileURI = fileNameService.getNSEIndex500StocksURI();

			fileName = fileNameService.getNSEIndex500StocksFileName();

			try {
				downloadUtil.downloadFile(fileURI, fileName);
				this.updateDownloadLedger(downloadTriggerIO);
			} catch (IOException e) {
				LOGGER.error("EXCEPTION WHILE DOWNLOADING " + downloadType + " : " + e);
			}

		} else {
			LOGGER.info("NOT A VALID TYPE : " + downloadType);
		}

		LOGGER.debug(QueueConstants.MTQueue.DOWNLOAD_TRIGGER_QUEUE.toUpperCase() +" : " + downloadTriggerIO + " : END");
		
	}
	
	private void updateDownloadLedger(DownloadTriggerIO downloadTriggerIO) {
		DownloadLedger downloadLedger = new DownloadLedger(downloadTriggerIO.getDownloadDate(), downloadTriggerIO.getDownloadType());
		
		downloadLedgerService.save(downloadLedger);
	}
	
	private void updateSectorPEPB() {
		sectorService.updateSectorPEPB();
	}
}
