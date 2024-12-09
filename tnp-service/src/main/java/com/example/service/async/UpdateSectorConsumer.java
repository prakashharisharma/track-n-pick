package com.example.service.async;

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

import com.example.model.master.Sector;
import com.example.mq.constants.QueueConstants;
import com.example.service.SectorService;
import com.example.util.io.model.SectorIO;

@Component
public class UpdateSectorConsumer {

	
	@Autowired
	private SectorService sectorService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSectorConsumer.class);
	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_MASTER_SECTOR_QUEUE)
	public void receiveMessage(@Payload SectorIO sectorIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		this.process(sectorIO);
		
	}
	
	private void process(SectorIO sectorIO) {
		if(sectorService.isExist(sectorIO.getSectorName())) {
			Sector sector = sectorService.update(sectorIO.getSectorName(), sectorIO.getSectorPe(), sectorIO.getSectorPb(), sectorIO.getVariationPe(), sectorIO.getVariationPb());
			
			LOGGER.info("UPDATED : " + sector);
			
		}else {
			Sector sector = sectorService.add(sectorIO.getSectorName(), sectorIO.getSectorPe(), sectorIO.getSectorPb(), sectorIO.getVariationPe(), sectorIO.getVariationPb());
			
			LOGGER.info("ADDED : " + sector);
		}
	}
	
}
