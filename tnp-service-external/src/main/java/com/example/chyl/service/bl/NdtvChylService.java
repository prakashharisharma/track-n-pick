package com.example.chyl.service.bl;

import java.io.IOException;
import java.net.MalformedURLException;
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
 * https://www.ndtv.com/business/stock/the-new-india-assurance-company-ltd_niacl
 * 
 * @author phsharma
 *
 */
@Service
class NdtvChylService implements CylhBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NdtvChylService.class);

	private static String BASE_URL_NDTV = "https://www.ndtv.com/business/stock/";

	@Override
	public ServiceProvider getServiceProvider() {
		return ServiceProvider.NDTV;
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

		String ndtv_url = this.getServiceUrl(stock);

		Document doc = Jsoup.connect(ndtv_url).get();

		Elements allElements = doc.select("div#nsesensex");

		int loopcounter = 0;
		for (Element element : allElements) {

			Elements chilrd = element.getElementsByTag("span");

			int i = 0;

			for (Element childElement : chilrd) {

				i++;

				if (i == 1) {

					loopcounter++;

					stockPrice.setCurrentPrice(Double.parseDouble(childElement.text()));

					LOGGER.debug("Current Price : " + childElement.text());

				}
			}
		}

		if (loopcounter == 0) {

			throw new MalformedURLException("MALFUNCTION URL");
		}

		doc = Jsoup.connect(ndtv_url).get();

		allElements = doc.select("div#nse52SliderDiv");

		for (Element element : allElements) {

			Elements chilrd = element.getElementsByTag("span");

			int i = 0;

			for (Element childElement : chilrd) {

				i++;
				if (i == 4) {

					stockPrice.setYearHigh(Double.parseDouble(childElement.text()));

					LOGGER.debug("Year High : " + childElement.text());

				}
				if (i == 3) {
					stockPrice.setYearLow(Double.parseDouble(childElement.text()));

					LOGGER.debug("Year Low : " + childElement.text());
				}

			}

		}

		return stockPrice;
	}

	@Override
	public String getServiceUrl(Stock stock) {

		String companyNmae = stock.getCompanyName().replace(".", "_");

		String companyNameWithUnderScore = companyNmae.replaceAll(" ", "-");

		String companyNameWithNseSymbol = companyNameWithUnderScore + stock.getNseSymbol();

		return BASE_URL_NDTV + companyNameWithNseSymbol.toLowerCase();
	}

	public static void main(String[] args) throws IOException {

		Document doc = Jsoup.connect("https://www.ndtv.com/business/stock/8k-miles-software-services-ltd_8kmilesoft")
				.get();

		Elements allElements = doc.select("div#nsesensex");

		int loopcounter = 0;

		for (Element element : allElements) {

			Elements chilrd = element.getElementsByTag("span");

			int i = 0;

			for (Element childElement : chilrd) {

				i++;

				if (i == 1) {
					loopcounter++;
					System.out.println("Current Price" + " : " + childElement.text());

				}
			}
		}

		if (loopcounter == 0) {

			throw new MalformedURLException("MALFUNCTION URL");
		}

		doc = Jsoup.connect("https://www.ndtv.com/business/stock/the-new-india-assurance-company-ltd_niacl").get();

		allElements = doc.select("div#nse52SliderDiv");

		for (Element element : allElements) {

			Elements chilrd = element.getElementsByTag("span");

			int i = 0;

			for (Element childElement : chilrd) {

				i++;
				if (i == 4) {

					System.out.println("Year High " + " : " + childElement.text());
				}
				if (i == 3) {

					System.out.println("Year Low " + " : " + childElement.text());
				}

			}

		}
	}

}
