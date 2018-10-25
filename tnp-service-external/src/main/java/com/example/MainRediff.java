package com.example;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainRediff {

	public static void main(String[] args) throws IOException {
		
		Document doc = Jsoup.connect("https://money.rediff.com/companies/atul-ltd").get();

		
		Element body = doc.body();
		
				
		Element allElement = body.getElementsByClass("zoom-container").first();

		
		String url = allElement.select("a").first().absUrl("href");
		
		System.out.println(url);
		System.out.println("*********************");
		String newURL = url.replace("bse/day/chart", "ratio");
		System.out.println(newURL);
		
		Document doc1 = Jsoup.connect(newURL).get();
		Element body1 = doc1.body();
		Elements allElements = body1.getAllElements();
		
		Elements sections =  allElements.first().getElementsByTag("table");
		
		System.out.println("*******************************");
		
		int i=0;
		
		for (Element element : sections) {
			i++;
	
			if(i==2) {
			
			System.out.println("**********CHILDS*********************" + i);
			Elements chilrd = element.getElementsByTag("tr");
			
			for (Element childElement : chilrd) {
				System.out.println(childElement.text());
			
			}
			}else {
				continue;
			}
				System.out.println("*******************************");
			
		}
		
	}
}
