package com.example;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MyStocks {
	
	public static void main(String[] args) throws IOException {
		

		Document doc = Jsoup.connect("http://www.mystocks.co.in/stocks/THOMASCOOK.html").get();

		
		Element body = doc.body();
		
		Elements allElements = body.getAllElements();
		
		Elements sections =  allElements.first().getElementsByTag("table");
		
		System.out.println("*******************************");
		
		int i=0;
		
		for (Element element : sections) {
			i++;
	

			if(i==2 ) {
			
			Elements chilrd = element.getElementsByTag("tr");
			
			int j=0;
			for (Element childElement : chilrd) {
				j++;
				
				if(j==2) {
					//System.out.println("*************CURR");
					//System.out.println(childElement.text());
					
					String curr[] = childElement.text().split(" ");
					
					System.out.println("Current : " + curr[3]);
				}
				
				//System.out.println(childElement.text());
			
			}
			
			}else if(i==12) {

				int j=0;
				Elements chilrd = element.getElementsByTag("tr");
				
				for (Element childElement : chilrd) {
					j++;
					
					if(j==2) {
						
						//System.out.println(childElement.text());
						
						String yearLowHighStr = childElement.text().replaceAll(" -", "");
						
						String yearLowHigh[] = yearLowHighStr.split(" ");
						
						System.out.println("yearHigh : " + yearLowHigh[0]);
						System.out.println("yearLow : " + yearLowHigh[1]);

					}
				//	System.out.println(childElement.text());
				
				}
			}
			else {
				continue;
			}
				System.out.println("*******************************");
			
		}
		
	}

}
