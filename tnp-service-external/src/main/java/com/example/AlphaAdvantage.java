package com.example;

import java.io.IOException;

import org.springframework.web.client.RestTemplate;

import com.example.external.dylh.model.NseResponseData;
import com.example.model.master.Stock;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlphaAdvantage {

	static final String API_KEY="MCAF9B429I44328U";
	
	static final String SMA_50_URL = "https://www.alphavantage.co/query?function=SMA&symbol=NSE:NSESYMBOL&interval=daily&time_period=50&series_type=close&apikey=MCAF9B429I44328U";

	static final String SMA_200_URL = "https://www.alphavantage.co/query?function=SMA&symbol=NSE:NSESYMBOL&interval=daily&time_period=200&series_type=close&apikey=MCAF9B429I44328U";

	static final String RSI_URL = "https://www.alphavantage.co/query?function=RSI&symbol=NSE:NSESYMBOL&interval=daily&time_period=14&series_type=close&apikey=MCAF9B429I44328U";

	enum Type{
		SMA_50, SMA_200, RSI
	}
	
	public static void main(String[] args) throws JsonProcessingException, IOException {
		get50SMA();
		get200SMA();
		getRSI();
	}

	private static String get50SMA() throws JsonProcessingException, IOException {

		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		String data = restTemplate.getForObject(SMA_50_URL, String.class);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode rootNode = mapper.readTree(data);

		JsonNode locatedNode = rootNode.path("Technical Analysis: SMA").path("2019-01-09");

		System.out.println(locatedNode);

		String SMA = locatedNode.get("SMA").asText();

		System.out.println(SMA);

		return SMA;
	}

	private static String get200SMA() throws JsonProcessingException, IOException {

		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		String data = restTemplate.getForObject(SMA_200_URL, String.class);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode rootNode = mapper.readTree(data);

		JsonNode locatedNode = rootNode.path("Technical Analysis: SMA").path("2019-01-09");

		System.out.println(locatedNode);

		String SMA = locatedNode.get("SMA").asText();

		System.out.println(SMA);

		return SMA;
	}

	private static String getRSI() throws JsonProcessingException, IOException {

		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		String data = restTemplate.getForObject(RSI_URL, String.class);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode rootNode = mapper.readTree(data);

		JsonNode locatedNode = rootNode.path("Technical Analysis: RSI").path("2019-01-09");

		System.out.println(locatedNode);

		String RSI = locatedNode.get("RSI").asText();

		System.out.println(RSI);

		return RSI;
	}
	
	private static String buildURL(Stock stock, Type type) {
		
		String final_URL = null;;
		
		if(Type.SMA_50 == type) {
			final_URL = SMA_50_URL.replace("NSESYMBOL", stock.getNseSymbol()) + API_KEY;
			
		}
		
		return final_URL;
	}
}
