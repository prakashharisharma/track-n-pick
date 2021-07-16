package com.example.external.bhav.service;

import java.io.FileOutputStream;
import java.io.IOException;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class DownloadBhavService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadBhavService.class);
	
	String url1= "https://www.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=01-01-2019&section=EQ";
	
	String url2 = "https://archives.nseindia.com/content/historical/EQUITIES/2019/JAN/cm01JAN2019bhav.csv.zip";
	
	public void downloadFile(String searchUrl, String fileUrl, String fileName) throws IOException{
		
		String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
		
		LOGGER.info("START DOWNLOADING ",searchUrl, fileUrl, fileName);
		
		
		 //get login page
	    /*
		Response res = Jsoup
	    	.connect("https://www.nseindia.com/all-reports")
	        .userAgent(userAgent)
	        .method(Method.GET)
	        .ignoreContentType(true)
	        .execute();
	        */
		System.out.println("fileUrl " + fileUrl);
		System.out.println("searchUrl " + searchUrl);
		
	    Response  res1 = Jsoup
	    	.connect(fileUrl)
	        .userAgent(userAgent)
	        .referrer(searchUrl)
	      //  .cookies(res.cookies())
	        .method(Method.GET)
	        .ignoreContentType(true)
	        .execute();
	    
	    
	    System.out.println(res1.statusCode());
	  //  System.out.println(res1.body());
		
	 // save to file
	    FileOutputStream out = new FileOutputStream(fileName);
	    out.write(res1.bodyAsBytes());
	    out.close();
		
	}
}
