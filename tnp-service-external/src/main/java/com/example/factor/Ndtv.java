package com.example.factor;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Ndtv {

	public static void main(String[] args) throws IOException{
		
		Document doc = Jsoup.connect("https://www.ndtv.com/business/stock/ABB-India-Ltd_abb").get();
		
		Elements allElements = doc.select("table#keyfunda");
		
		for (Element element : allElements) {
			
			Elements chilrd = element.getElementsByTag("td");
			
			int j=0;
			
			for (Element childElement : chilrd) {
				
				j++;
				if(j==2) {
					System.out.println( "Market Cap : "+ childElement.text());
				}
				if(j==4) {
					System.out.println( "EPS : "+ childElement.text());
				}
				if(j==6) {
					System.out.println( "Stock P/E : "+ childElement.text());
				}
				if(j==8) {
					System.out.println( "Book Value : "+ childElement.text());
				}
				if(j==10) {
					System.out.println( "PB : "+ childElement.text());
				}
				if(j==16) {
					System.out.println( "ROCE : "+ childElement.text());
				}
				if(j==20) {
					System.out.println( "DEBT/EQUITY : "+ childElement.text());
				}
			}
			
		}
		
	}

}
