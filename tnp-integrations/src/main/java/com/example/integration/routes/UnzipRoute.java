package com.example.integration.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.springframework.stereotype.Component;

import com.example.util.FileLocationConstants;

@Component
public class UnzipRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		
		from("file:"+FileLocationConstants.NSE_BHAV_ZIP_LOCATION +"?noop=false&delay=5000&antInclude=*.zip&moveFailed=error")
		.split(new ZipSplitter()) 
        .streaming() 
        .convertBodyTo(String.class) 
        .to("file:"+FileLocationConstants.NSE_BHAV_CSV_LOCATION);


		from("file:"+FileLocationConstants.BSE_BHAV_ZIP_LOCATION +"?noop=false&delay=5000&antInclude=*.zip&moveFailed=error")
				.split(new ZipSplitter())
				.streaming()
				.convertBodyTo(String.class)
				.to("file:"+FileLocationConstants.BSE_BHAV_CSV_LOCATION);
		
	}

}
