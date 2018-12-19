package com.example.chyl.service.bl;

import java.io.IOException;
import java.time.LocalDate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.chyl.model.NSEResult;
import com.example.common.ServiceProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * https://www.nse-india.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?symbol=HDIL
 * @author phsharma
 *
 */
@Service
class NseChylService implements CylhBaseService{

	private static String BASE_URL_NSE = "https://www.nse-india.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?symbol=";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NseChylService.class);
	
	@Override
	public ServiceProvider getServiceProvider() {
		
		return ServiceProvider.NSE;
	}

	@Override
	public StockPrice getChylPrice(Stock stock) throws IOException {
		
		LOGGER.debug("Inside " + getServiceProvider().toString() + " :" + stock.getNseSymbol());
		
		StockPrice stockPrice = stock.getStockPrice();
		
		if (stockPrice==null) {
			
			stockPrice = new StockPrice();
			
		}else {
			
			stockPrice.setLastModified(LocalDate.now());
		}
		
		Document doc = Jsoup.connect(this.getServiceUrl(stock)).get();

		Element body = doc.body();
				
		Element allElement = body.getElementById("responseDiv");

		String response = allElement.text();
	
		// Object mapper instance
		ObjectMapper mapper = new ObjectMapper();

		// Convert JSON to POJO
		try {
			
			NSEResult nseResult = mapper.readValue(response, NSEResult.class);
			
			if(nseResult.getData() == null || nseResult.getData().size() == 0 ) {
				return stockPrice;
			}

			stockPrice.setCurrentPrice(Double.parseDouble(nseResult.getData().get(0).getLastPrice().replace(",", "")));
			
			stockPrice.setYearHigh(Double.parseDouble(nseResult.getData().get(0).getHigh52().replace(",", "")));
			
			stockPrice.setYearLow(Double.parseDouble(nseResult.getData().get(0).getLow52().replace(",", "")));

			LOGGER.debug("Current Price : " + stockPrice.getCurrentPrice() + "Year Low : " + stockPrice.getYearLow() + "Year High : " + stockPrice.getYearHigh());
			
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
		
		return BASE_URL_NSE + stock.getNseSymbol();
	}

}
