package com.example.util;


import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.model.StockPrice;



public class SpringRESTClient {

	public static void main(String[] args) {

		getStocksMaster();
	}

	public static List<StockPrice> getStocksMaster() {
	
		URI uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(8081).path("stocks").path("/").build().toUri();

		System.out.println(uri.toString());
		
		RestTemplate restTemplate = new RestTemplate();
		
		StockPrice[] result = restTemplate.getForObject(uri, StockPrice[].class);
		
		List<StockPrice> priceList = Arrays.asList(result);
		
		/*for(StockPrice stock : priceList) {
			System.out.println(stock);
		}*/
		
		return priceList;
	}

	
}

