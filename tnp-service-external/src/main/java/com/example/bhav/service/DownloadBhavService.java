package com.example.bhav.service;

import java.io.FileOutputStream;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.springframework.stereotype.Service;

@Service
public class DownloadBhavService {

	String url1= "https://www.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=01-01-2019&section=EQ";
	
	String url2 = "https://www.nseindia.com/content/historical/EQUITIES/2019/JAN/cm01JAN2019bhav.csv.zip";
	
	public void downloadFile(String searchUrl, String fileUrl, String fileName) throws IOException{
		
		String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
		
		 //get login page
	    Response res = Jsoup
	    	.connect(searchUrl)
	        .userAgent(userAgent)
	        .method(Method.GET)
	        .ignoreContentType(true)
	        .execute();
		
	    Response  res1 = Jsoup
	    	.connect(fileUrl)
	        .userAgent(userAgent)
	        .referrer(searchUrl)
	        .cookies(res.cookies())
	        .method(Method.GET)
	        .ignoreContentType(true)
	        .execute();
	    
	  //  System.out.println(res1.body());
		
	 // save to file
	    FileOutputStream out = new FileOutputStream(fileName);
	    out.write(res1.bodyAsBytes());
	    out.close();
		
	}
}
