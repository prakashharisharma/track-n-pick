package com.example.integration.processors.master;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.integration.model.SectorMasterIN;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.SectorIO;

@Service
public class UpdateSectorsProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSectorsProcessor.class);

	@Autowired
	private QueueService queueService;

	@Override
	public void process(Exchange exchange) throws Exception {

		LOGGER.info("UpdateSectorsProcessor : START");
		@SuppressWarnings("unchecked")
		List<SectorMasterIN> newSectorsList = (List<SectorMasterIN>) exchange.getIn().getBody();

		Set<SectorMasterIN> newSectorsSet = new HashSet<>(newSectorsList);

		for (SectorMasterIN sectorMaster : newSectorsSet) {
			SectorIO sectorIO = new SectorIO(sectorMaster.getSectorName(), sectorMaster.getSectorPe(),
					sectorMaster.getSectorPb());

			LOGGER.debug("UpdateSectorsProcessor : Queuing to update master " + sectorMaster);
			
			queueService.send(sectorIO, QueueConstants.MTQueue.UPDATE_MASTER_SECTOR_QUEUE);
		}

		LOGGER.info("UpdateSectorsProcessor : END");
	}

}
