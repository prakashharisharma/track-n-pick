package com.example.chyl.service.bl;

import java.io.IOException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.chyl.model.RediffResult;
import com.example.common.ServiceProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * https://money.rediff.com/money1/currentstatus.php?companycode=HDIL
 * 
 * @author phsharma
 *
 */
@Service
class RediffChylService implements CylhBaseService {

	private static final String BASE_URL_REDIFF = "https://money.rediff.com/money1/currentstatus.php?companycode=";

	private static final Logger LOGGER = LoggerFactory.getLogger(RediffChylService.class);

	@Override
	public ServiceProvider getServiceProvider() {

		return ServiceProvider.REDIFF;
	}

	@Override
	public StockPrice getChylPrice(Stock stock) throws IOException {

		LOGGER.debug("Inside " + getServiceProvider().toString() + " : " + stock.getNseSymbol());

		StockPrice stockPrice = stock.getStockPrice();

		if (stockPrice == null) {

			stockPrice = new StockPrice();

		} else {

			stockPrice.setLastModified(LocalDate.now());
		}

		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		String rediffResponse = restTemplate.getForObject(this.getServiceUrl(stock), String.class);

		if(rediffResponse == null || rediffResponse.isEmpty()) {
			return stockPrice;
		}
		
		
		// Object mapper instance
		ObjectMapper mapper = new ObjectMapper();

		// Convert JSON to POJO
		try {

			RediffResult rediffResult = mapper.readValue(rediffResponse, RediffResult.class);

			
			if(rediffResult != null) {
			double currentPrice = Double.parseDouble(rediffResult.getLastTradedPrice().replace(",", ""));

			stockPrice.setCurrentPrice(currentPrice);

			LOGGER.debug("Current Price : " + currentPrice);

			double yearHigh = Double.parseDouble(rediffResult.getFiftyTwoWeekHigh().replace(",", ""));

			stockPrice.setYearHigh(yearHigh);

			LOGGER.debug("Year High : " + yearHigh);

			double yearLow = Double.parseDouble(rediffResult.getFiftyTwoWeekLow().replace(",", ""));

			stockPrice.setYearLow(yearLow);

			LOGGER.debug("Year Low : " + yearLow);
			}else {
				return stockPrice;
			}

		} catch (JsonParseException e) {

			e.printStackTrace();
		} catch (JsonMappingException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return stockPrice;
	}

	@Override
	public String getServiceUrl(Stock stock) {

		return BASE_URL_REDIFF + stock.getNseSymbol();
	}

	public static void main(String[] args) {

		String REDIFF_YEAR_LOW_HIGH_URL_WITH_SYMBOL = BASE_URL_REDIFF + "HDIL";

		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		String stockPrice = restTemplate.getForObject(REDIFF_YEAR_LOW_HIGH_URL_WITH_SYMBOL, String.class);

		// {"LastTradedPrice":"3,159.35","Volume":"7,021","PercentageDiff":"-1.05","FiftyTwoWeekHigh":"4,824.00","FiftyTwoWeekLow":"3,150.00","LastTradedTime":"14
		// Sep,15:59:33","ChangePercent":"-1.05","Change":"-33.40","MarketCap":"223.87","High":"3,239.05","Low":"3,150.00","PrevClose":"3,192.75","BonusSplitStatus":"0","BonusSplitRatio":"0-0"}

		System.out.println(stockPrice);

		// Object mapper instance
		ObjectMapper mapper = new ObjectMapper();

		// Convert JSON to POJO
		try {

			RediffResult sp = mapper.readValue(stockPrice, RediffResult.class);

			System.out.println(sp);

			System.out.println(sp.getLastTradedPrice());

			System.out.println(sp.getFiftyTwoWeekHigh());

			System.out.println(sp.getFiftyTwoWeekLow());

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
