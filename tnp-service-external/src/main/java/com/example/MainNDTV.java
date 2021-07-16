package com.example;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainNDTV {

	public static void main(String[] args) throws IOException {
		
		Document doc = Jsoup.connect("https://www.ndtv.com/business/stock/atul-ltd_atul").get();
		
		Element body = doc.body();
		
		Elements allElements = body.getAllElements();
		
		Elements sections =  allElements.first().getElementsByTag("table");
		
		System.out.println("*******************************");
		
		int i=0;
		
		for (Element element : sections) {
			i++;
			
			if( i==3) {
			
	
			Elements chilrd = element.getElementsByTag("td");
			System.out.println("**********CHILDS*********************" + i);
			for (Element childElement : chilrd) {
				System.out.println(childElement.text());
			
			}
				System.out.println("*******************************");
			}else {
				continue;
			}
			
			
		}
		
		System.out.println("*******************************");
		
	}
	
	/*

	Total Debt to Equity (D/E) Ratio
	0.00
	 * 
	 * 
	 * 
	 */
	
}
