package com.example;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainScreener {

	public static void main(String[] args) throws IOException {
		
		Document doc = Jsoup.connect("https://www.screener.in/company/ATUL/").get();

		Element body = doc.body();
		
		Elements allElements = body.getAllElements();
		
		Elements sections =  allElements.first().getElementsByTag("section");
		
		Element data = sections.first();
		
		Elements chilrd = data.getElementsByTag("li");

		System.out.println("*******************************");
		
		for (Element element : chilrd) {
			
			String elementsTxt = element.text();
			
			if(elementsTxt.startsWith("52 weeks")) {
				
				String yesrHighLow = elementsTxt.substring(20);
				
				String yesrHighLows[] = yesrHighLow.split("/");
				
				double yearHigh = Double.parseDouble(yesrHighLows[0]);
				
				double yearLow = Double.parseDouble(yesrHighLows[1]);
				
				System.out.println("Year Low : " + yearLow);
				
				System.out.println("Year High : " + yearHigh);
				
			}else {
				
				String elemtsTxt[] = element.text().split(":");
				
				if(elemtsTxt.length == 1) {
					continue;
				}
				
				System.out.println(elemtsTxt[0] +" : " + elemtsTxt[1].trim());
			}
			
		}
		
		System.out.println("*******************************");
		
	}
	/*
	 * *******************************
	Market Cap : 9,201 Cr.
	Book Value : 740.91
	Stock P/E : 28.74
	Dividend Yield : 0.39 %
	ROCE : 19.00 %
	ROE : 13.13 %
	Sales Growth (3Yrs) : 7.02 %
	Face Value : 10.00
	 * 
	 * 
	 * 
	 */
	
}
