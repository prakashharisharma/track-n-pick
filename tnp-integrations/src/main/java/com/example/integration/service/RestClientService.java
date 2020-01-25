package com.example.integration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.util.io.model.StockPriceIO;

@Service
public class RestClientService {

	 @Autowired
	 private RestTemplate restTemplate;

	public List<StockPriceIO>  getNift500(){
		
		//System.out.println("SERVICE");
		
		ResponseEntity<List<StockPriceIO>> stockMasterResponse = restTemplate.exchange("http://localhost:8081/public/api/stocks/active/nifty500",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<StockPriceIO>>() {
        });
		
		List<StockPriceIO> nifty500List = stockMasterResponse.getBody();

		//nifty500List.forEach(System.out::println);
		
		return nifty500List;
		//stocks.forEach(System.out::println);
	}
	
}
