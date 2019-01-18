package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.springframework.stereotype.Component;

@Component
public class UnzipRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		
		from("file:data/bhav/nse/zip?noop=false&delay=5000&antInclude=*.zip&moveFailed=error")
		.split(new ZipSplitter()) 
        .streaming() 
        .convertBodyTo(String.class) 
        .to("file:data/bhav/nse/csv"); 
		
		from("file:data/bhav/nse/temp/zip?noop=false&delay=5000&antInclude=*.zip&moveFailed=error")
		.split(new ZipSplitter()) 
        .streaming() 
        .convertBodyTo(String.class) 
        .to("file:data/bhav/nse/temp/csv"); 
		
	}

}
