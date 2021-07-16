package com.example.integration.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.model.SectorMasterIN;
import com.example.mq.constants.QueueConstants;
import com.example.integration.processors.master.UpdateSectorsProcessor;
import com.example.util.FileLocationConstants;

@Component
public class SectorMasterUpdateRoute extends RouteBuilder{

	@Autowired
	private UpdateSectorsProcessor updateSectorMasterProcessor;
	
	@Override
	public void configure() throws Exception {
		final DataFormat stockMasterBindy = new BindyCsvDataFormat(SectorMasterIN.class);

		//parse the csv file and push list to jmsqueue
		from("file:"+FileLocationConstants.SECTOR_CSV_LOCATION+"?noop=false")
			.unmarshal(stockMasterBindy)
		.to("jms:"+QueueConstants.IntegrationQueue.UPDATE_SECTORS_QUEUE);
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:"+QueueConstants.IntegrationQueue.UPDATE_SECTORS_QUEUE)
			.process(updateSectorMasterProcessor).log("${body}");
	}

}
