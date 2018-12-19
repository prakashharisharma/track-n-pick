package com.example.chyl.service.bl;

import java.io.IOException;
import java.time.LocalDate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.common.ServiceProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;

/**
 * https://www.screener.in/company/HDIL/
 * 
 * @author phsharma
 *
 */
@Service
class ScreenerChylService implements CylhBaseService {

	private static String BASE_URL_SCREENER = "https://www.screener.in/company/";

	private static final Logger LOGGER = LoggerFactory.getLogger(NseChylService.class);

	@Override
	public ServiceProvider getServiceProvider() {

		return ServiceProvider.SCREENER;
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

		Document doc = Jsoup.connect(this.getServiceUrl(stock)).get();

		Element body = doc.body();

		Elements allElements = body.getAllElements();

		Elements sections = allElements.first().getElementsByTag("section");

		Element data = sections.first();

		Elements chilrd = data.getElementsByTag("li");

		for (Element element : chilrd) {

			String elementsTxt = element.text();

			if (elementsTxt.startsWith("Current Price")) {

				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {
					continue;
				}

				

				stockPrice.setCurrentPrice(Double.parseDouble(elemtsTxt[1].trim().replace(",", "")));
			}

			if (elementsTxt.startsWith("52 weeks")) {

				String yesrHighLow = elementsTxt.substring(20);

				String yesrHighLows[] = yesrHighLow.split("/");

				double yearHigh = Double.parseDouble(yesrHighLows[0]);

				double yearLow = Double.parseDouble(yesrHighLows[1]);

				stockPrice.setYearHigh(yearHigh);

				stockPrice.setYearLow(yearLow);

			}

		}
		LOGGER.debug("Current Price : " + stockPrice.getCurrentPrice() + "Year Low : " + stockPrice.getYearLow() + "Year High : " + stockPrice.getYearHigh());
		return stockPrice;
	}

	@Override
	public String getServiceUrl(Stock stock) {

		return BASE_URL_SCREENER + stock.getNseSymbol() + "/";
	}

}
