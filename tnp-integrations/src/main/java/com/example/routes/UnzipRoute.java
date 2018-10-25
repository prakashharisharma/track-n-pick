package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;

public class UnzipRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		
		from("file:data/inbox/zip?noop=false&delay=5000&antInclude=*.zip&moveFailed=error")
		.split(new ZipSplitter()) 
        .streaming() 
        .convertBodyTo(String.class) 
        .to("file:data/inbox/csv"); 
		
	}

}
