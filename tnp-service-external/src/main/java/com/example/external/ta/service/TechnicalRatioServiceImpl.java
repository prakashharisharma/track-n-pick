package com.example.external.ta.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.util.MiscUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Deprecated
@Service
public class TechnicalRatioServiceImpl implements TechnicalRatioService {

	private static final String API_KEY = "MCAF9B429I44328U";

	private static final String API_KEY_2 = "7U95RZIQ6VXANCGP";

	private static final String SMA_50_URL = "https://www.alphavantage.co/query?function=SMA&symbol=NSE:NSESYMBOL&interval=daily&time_period=50&series_type=close&apikey=";

	private static final String SMA_200_URL = "https://www.alphavantage.co/query?function=SMA&symbol=NSE:NSESYMBOL&interval=daily&time_period=200&series_type=close&apikey=";

	private static final String RSI_URL = "https://www.alphavantage.co/query?function=RSI&symbol=NSE:NSESYMBOL&interval=daily&time_period=14&series_type=close&apikey=";

	private static int counter = 0;

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private MiscUtil miscUtil;

	enum TechnicalRatio {
		SMA_50, SMA_200, RSI
	}

	@Override
	public StockTechnicals retrieveTechnicals(Stock stock) {

		StockTechnicals stockTechnicals = stock.getTechnicals();

		if (stockTechnicals == null) {
			stockTechnicals = new StockTechnicals();

		}

		double rsi = this.getRSI(stock);

		try {
			miscUtil.delay(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double sma50 = this.get50SMA(stock);

		try {
			miscUtil.delay(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double sma200 = this.get200SMA(stock);

		stockTechnicals.setStock(stock);
		stockTechnicals.setSma50(sma50);
		stockTechnicals.setSma200(sma200);
		stockTechnicals.setRsi(rsi);
		stockTechnicals.setLastModified(miscUtil.currentDate());

		return stockTechnicals;
	}

	@Override
	public double get50SMA(Stock stock) {
		String SMA = null;
		// Send request with GET method and default Headers.
		RestTemplate restTemplate = new RestTemplate();

		String data = null;
		try {
			data = restTemplate.getForObject(this.buildURL(stock, TechnicalRatio.SMA_50), String.class);
		} catch (Exception e) {

		}
		if (data != null) {

			ObjectMapper mapper = new ObjectMapper();

			JsonNode rootNode;

			JsonNode techAnalysisNode;

			try {
				rootNode = mapper.readTree(data);

				if (rootNode != null) {

					techAnalysisNode = rootNode.path("Technical Analysis: SMA").path(miscUtil.currentDate().toString());

					if (techAnalysisNode != null) {
						if (techAnalysisNode.toString().equalsIgnoreCase("")) {
							SMA = "0.00";
						} else {
							SMA = techAnalysisNode.get("SMA").asText();
						}
					}

				}

			} catch (IOException e) {
				SMA = "0.00";
			}
		} else {
			SMA = "0.00";
		}
		return Double.parseDouble(SMA);
	}

	@Override
	public double get200SMA(Stock stock) {

		String SMA = null;

		RestTemplate restTemplate = new RestTemplate();
		String data = null;
		try {

			// Send request with GET method and default Headers.
			data = restTemplate.getForObject(this.buildURL(stock, TechnicalRatio.SMA_200), String.class);

		} catch (Exception e) {

		}
		if (data != null) {
			ObjectMapper mapper = new ObjectMapper();

			JsonNode rootNode;
			JsonNode techAnalysisNode;

			try {
				rootNode = mapper.readTree(data);

				if (rootNode != null) {
					techAnalysisNode = rootNode.path("Technical Analysis: SMA").path(miscUtil.currentDate().toString());

					if (techAnalysisNode != null) {
						if (techAnalysisNode.toString().equalsIgnoreCase("")) {
							SMA = "0.00";
						} else {
							SMA = techAnalysisNode.get("SMA").asText();
						}
					}
				}

			} catch (IOException e) {
				SMA = "0.00";
			}
		} else {
			SMA = "0.00";
		}
		return Double.parseDouble(SMA);
	}

	@Override
	public double getRSI(Stock stock) {

		System.out.println(stock.getNseSymbol());
		String SMA = null;
		RestTemplate restTemplate = new RestTemplate();
		String data = null;
		try {
			// Send request with GET method and default Headers.
			data = restTemplate.getForObject(this.buildURL(stock, TechnicalRatio.RSI), String.class);
		} catch (Exception e) {

		}
		if (data != null) {

			ObjectMapper mapper = new ObjectMapper();

			JsonNode rootNode;
			JsonNode techAnalysisNode;

			try {
				rootNode = mapper.readTree(data);
				if (rootNode != null) {
					techAnalysisNode = rootNode.path("Technical Analysis: RSI").path(miscUtil.currentDate().toString());
					if (techAnalysisNode != null) {
						if (techAnalysisNode.toString().equalsIgnoreCase("")) {
							SMA = "0.00";
						} else {
							SMA = techAnalysisNode.get("RSI").asText();
						}
					}
				}

			} catch (IOException e) {
				SMA = "0.00";
			}
		} else {
			SMA = "0.00";
		}
		return Double.parseDouble(SMA);
	}

	private String buildURL(Stock stock, TechnicalRatio type) {
		counter++;
		String final_URL = null;

		String api_key;
		if (counter % 2 == 0) {
			api_key = API_KEY;
		} else {
			api_key = API_KEY_2;
		}

		if (TechnicalRatio.SMA_50 == type) {
			final_URL = SMA_50_URL.replace("NSESYMBOL", stock.getNseSymbol()) + api_key;

		} else if (TechnicalRatio.SMA_200 == type) {
			final_URL = SMA_200_URL.replace("NSESYMBOL", stock.getNseSymbol()) + api_key;
		} else if (TechnicalRatio.RSI == type) {
			final_URL = RSI_URL.replace("NSESYMBOL", stock.getNseSymbol()) + api_key;
		}

		System.out.println(final_URL);

		return final_URL;
	}

}
