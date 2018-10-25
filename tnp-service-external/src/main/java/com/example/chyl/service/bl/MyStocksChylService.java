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
 * 
 * http://www.mystocks.co.in/stocks/HDIL.html
 * 
 * @author phsharma
 *
 */
@Service
class MyStocksChylService implements CylhBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MyStocksChylService.class);

	private static String BASE_URL_MYSTOCKS = "http://www.mystocks.co.in/stocks/";

	@Override
	public ServiceProvider getServiceProvider() {

		return ServiceProvider.MYSTOCKS;
	}

	@Override
	public StockPrice getChylPrice(Stock stock) throws IOException {

		LOGGER.debug("Inside " + getServiceProvider().toString());

		StockPrice stockPrice = stock.getStockPrice();

		if (stockPrice == null) {

			stockPrice = new StockPrice();

		} else {

			stockPrice.setLastModified(LocalDate.now());
		}

		String serviceUrl = this.getServiceUrl(stock);

		Document doc = Jsoup.connect(serviceUrl).get();

		Element body = doc.body();

		Elements allElements = body.getAllElements();

		Elements sections = allElements.first().getElementsByTag("table");

		int i = 0;

		for (Element element : sections) {
			i++;

			if (i == 2) {

				Elements chilrd = element.getElementsByTag("tr");

				int j = 0;

				for (Element childElement : chilrd) {

					j++;

					if (j == 2) {

						String curr[] = childElement.text().split(" ");

						LOGGER.debug("Current : " + curr[3]);

						stockPrice.setCurrentPrice(Double.parseDouble(curr[3]));
					}

				}

			} else if (i == 12) {

				int j = 0;
				Elements chilrd = element.getElementsByTag("tr");

				for (Element childElement : chilrd) {
					j++;

					if (j == 2) {

						String yearLowHighStr = childElement.text().replaceAll(" -", "");

						String yearLowHigh[] = yearLowHighStr.split(" ");
						stockPrice.setYearHigh(Double.parseDouble(yearLowHigh[0]));

						LOGGER.debug("yearHigh : " + yearLowHigh[0]);

						stockPrice.setYearLow(Double.parseDouble(yearLowHigh[1]));

						LOGGER.debug("yearLow : " + yearLowHigh[1]);
					}

				}
			} else {
				continue;
			}

		}

		return stockPrice;
	}

	@Override
	public String getServiceUrl(Stock stock) {

		return BASE_URL_MYSTOCKS + stock.getNseSymbol() + ".html";
	}

	public static void main(String[] args) throws IOException {

		Document doc = Jsoup.connect("http://www.mystocks.co.in/stocks/HDIL.html").get();

		Element body = doc.body();

		Elements allElements = body.getAllElements();

		Elements sections = allElements.first().getElementsByTag("table");

		System.out.println("*******************************");

		int i = 0;

		for (Element element : sections) {
			i++;

			if (i == 2) {

				Elements chilrd = element.getElementsByTag("tr");

				int j = 0;
				for (Element childElement : chilrd) {
					j++;

					if (j == 2) {
						// System.out.println("*************CURR");
						// System.out.println(childElement.text());

						String curr[] = childElement.text().split(" ");

						System.out.println("Current : " + curr[3]);
					}

					// System.out.println(childElement.text());

				}

			} else if (i == 12) {

				int j = 0;
				Elements chilrd = element.getElementsByTag("tr");

				for (Element childElement : chilrd) {
					j++;

					if (j == 2) {

						// System.out.println(childElement.text());

						String yearLowHighStr = childElement.text().replaceAll(" -", "");

						String yearLowHigh[] = yearLowHighStr.split(" ");

						System.out.println("yearHigh : " + yearLowHigh[0]);
						System.out.println("yearLow : " + yearLowHigh[1]);

					}
					// System.out.println(childElement.text());

				}
			} else {
				continue;
			}
			System.out.println("*******************************");

		}

	}

}
