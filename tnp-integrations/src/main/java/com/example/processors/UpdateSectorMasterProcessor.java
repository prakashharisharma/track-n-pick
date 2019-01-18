package com.example.processors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.SectorMaster;
import com.example.model.master.Sector;
import com.example.service.SectorService;


@Service
public class UpdateSectorMasterProcessor implements Processor{

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSectorMasterProcessor.class);
	
	@Autowired
	private SectorService sectorService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		LOGGER.info("SECTOR UPDATE PROCESSOR START");
		@SuppressWarnings("unchecked")
		List<SectorMaster> newSectorsList = (List<SectorMaster>) exchange.getIn().getBody();
		
		Set<SectorMaster> newSectorsSet = new HashSet<>(newSectorsList);
		
		for(SectorMaster sectorMaster : newSectorsSet) {
			
			if(sectorService.isExist(sectorMaster.getSectorName())) {
				Sector sector = sectorService.update(sectorMaster.getSectorName(), sectorMaster.getSectorPe(), sectorMaster.getSectorPb());
				
				LOGGER.info("UPDATED : " + sector);
				
			}else {
				Sector sector = sectorService.add(sectorMaster.getSectorName(), sectorMaster.getSectorPe(), sectorMaster.getSectorPb());
				
				LOGGER.info("ADDED : " + sector);
			}
		}
		
		LOGGER.info("SECTOR UPDATE PROCESSOR END");
	}

}
