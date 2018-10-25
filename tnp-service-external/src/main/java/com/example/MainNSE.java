package com.example;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MainNSE {

	
	public static void main(String[] args) throws IOException {
		
		Document doc = Jsoup.connect("https://www.nse-india.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?symbol=HDIL").get();

		
		Element body = doc.body();
				
		Element allElement = body.getElementById("responseDiv");

		System.out.println(allElement.text());
		
	}
}
