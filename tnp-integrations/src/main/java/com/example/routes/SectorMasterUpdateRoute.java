package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.model.SectorMaster;
import com.example.model.StockMaster;
import com.example.processors.UpdateSectorMasterProcessor;

@Component
public class SectorMasterUpdateRoute extends RouteBuilder{

	@Autowired
	private UpdateSectorMasterProcessor updateSectorMasterProcessor;
	
	@Override
	public void configure() throws Exception {
		final DataFormat stockMasterBindy = new BindyCsvDataFormat(SectorMaster.class);
		

		//parse the csv file and push list to jmsqueue
		from("file:data/master/sectors/csv?noop=false")
			.unmarshal(stockMasterBindy)
		.to("jms:queue.master.csv.sectors");
		
		//parse the records queue and retain all the records that are in master and push to processedrecords queue
		from("jms:queue.master.csv.sectors")
			.process(updateSectorMasterProcessor).log("${body}");
	}

}
