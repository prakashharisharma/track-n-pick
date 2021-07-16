package com.example;

import java.io.FileOutputStream;
import java.io.IOException;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DownloadBhav {

	public static void main(String[] args) throws IOException {
		
		String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
		
		 //get login page
	    Response res = Jsoup
	    	.connect("https://www.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=01-01-2019&section=EQ")
	        .userAgent(userAgent)
	        .method(Method.GET)
	        .ignoreContentType(true)
	        .execute();
		
	    Response  res1 = Jsoup
	    	.connect("https://www.nseindia.com/content/historical/EQUITIES/2019/JAN/cm01JAN2019bhav.csv.zip")
	        .userAgent(userAgent)
	        .referrer("https://www.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=01-01-2019&section=EQ")
	        .cookies(res.cookies())
	        .method(Method.GET)
	        .ignoreContentType(true)
	        .execute();
	    
	    System.out.println(res1.body());
		
	 // save to file
	    FileOutputStream out = new FileOutputStream("D:\\test.zip");
	    out.write(res1.bodyAsBytes());
	    out.close();
		
	}
	
	
	
}
