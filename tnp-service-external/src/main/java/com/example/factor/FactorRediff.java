package com.example.factor;

import java.io.IOException;
import java.time.LocalDate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.common.FactorProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;

@Service
public class FactorRediff implements FactorBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactorRediff.class);
	
	private static String BASE_URL_REDIFF = "https://money.rediff.com/companies/";

	private String ratioURL;

	private StockFactor getMcapFaceValue(Stock stock, StockFactor stockFactor) throws IOException {

		String rediffURL = this.buildURL(stock);

		System.out.println("REDIFFURL " + rediffURL);
		
		Document doc = Jsoup.connect(rediffURL).get();

		Element body = doc.body();

		Element allElement = body.getElementsByClass("zoom-container").first();
		
		if(allElement == null) {
			return stockFactor;
		}
		
		String url = allElement.select("a").first().absUrl("href");

		if(url == null || url.isEmpty()) {
			return stockFactor;
		}
		
		String mcapFaceValueURL = url.replace("/bse/day/chart", "");

		LOGGER.info(mcapFaceValueURL);

		this.setRatioURL(mcapFaceValueURL + "/ratio");

		Elements  allElements = doc.select("span#ltpid");

		allElements = doc.select("span#MarketCap");

		for (Element element : allElements) {

			double marketCap = Double.parseDouble(element.text().replace(",", ""));
			
			stockFactor.setMarketCap(marketCap);

		}
		
		allElements = doc.select("div#div_rcard_more");

		for (Element element : allElements) {

			Elements childs = element.getAllElements();
			int j = 0;
			for (Element child : childs) {
				j++;

				if (j == 16) {
					
					double faceValue = Double.parseDouble(child.text().replace(",", "").trim());
					
					stockFactor.setFaceValue(faceValue);
				}
			}

		}

		return stockFactor;
	}

	private StockFactor getRatios(Stock stock, StockFactor stockFactor) throws IOException {

		String ratioUrl = this.getRatioURL();
		
		if(ratioUrl == null || ratioUrl.isEmpty()) {
			return stockFactor;
		}
		
		Document doc = Jsoup.connect(ratioUrl).get();

		Elements allElement = doc.getElementsByClass("dataTable");

		if(allElement == null) {
			return stockFactor;
		}
		
		int i = 0;

		for (Element element : allElement) {
			
			i++;

			Elements childTr = element.getElementsByTag("tr");

			for (Element childElement : childTr) {

				
				if (childElement.text().startsWith("Adjusted EPS (Rs)")) {

					Elements childTd = childElement.getElementsByTag("td");
					
					int j = 0;
					
					for (Element td : childTd) {
						
						j++;
						if (j == 2) {
							
							double eps = 0.00;
							if(td.text().trim().equalsIgnoreCase("-")) {
								eps =0.00;
							}else {
							
							eps = Double.parseDouble(td.text().replace(",", "").trim());
							}
							stockFactor.setEps(eps);
						}
					}

				}
				
				else if (childElement.text().startsWith("Dividend per share")) {

					Elements childTd = childElement.getElementsByTag("td");
					int j = 0;
					for (Element td : childTd) {
						j++;
						if (j == 2) {
							
							double dividend = 0.00;
							
							if(td.text().trim().equalsIgnoreCase("-")) {
								dividend = 0.00;
							}else {
							
							dividend = Double.parseDouble(td.text().replace(",", "").trim());
							
							}
							stockFactor.setDividend(dividend);
						}
					}

				}
				else if (childElement.text().startsWith("Book value (incl rev res) per share EPS (Rs)")) {

					Elements childTd = childElement.getElementsByTag("td");
					int j = 0;
					for (Element td : childTd) {
						j++;
						if (j == 2) {
							double bookValue = 0.00;
							if(td.text().trim().equalsIgnoreCase("-")) {
								bookValue = 0.00;
							}else {
								bookValue = Double.parseDouble(td.text().replace(",", "").trim());
							}
							stockFactor.setBookValue(bookValue);
						
						}
					}

				}
				else if (childElement.text().startsWith("Adjusted return on net worth")) {

					Elements childTd = childElement.getElementsByTag("td");
					int j = 0;
					for (Element td : childTd) {
						j++;
						if (j == 2) {
							
							double roe = 0.00;
							if(td.text().trim().equalsIgnoreCase("-")) {
								roe = 0.00;
							}else {
							
							roe = Double.parseDouble(td.text().replace(",", "").trim());
							}
							stockFactor.setReturnOnEquity(roe);
							
						}
					}

				}
				else if (childElement.text().startsWith("Return on long term funds")) {

					Elements childTd = childElement.getElementsByTag("td");
					int j = 0;
					for (Element td : childTd) {
						j++;
						if (j == 2) {
							
							double roce = 0.00;
							if(td.text().trim().equalsIgnoreCase("-")) {
								roce = 0.00;
							}else {
								roce = Double.parseDouble(td.text().replace(",", "").trim());
							}
							stockFactor.setReturnOnCapital(roce);
							
						}
					}

				}
				else if (childElement.text().startsWith("Total debt/equity")) {

					Elements childTd = childElement.getElementsByTag("td");
					int j = 0;
					for (Element td : childTd) {
						j++;
						if (j == 2) {
							double debtEquity = 0.00;
							if(td.text().trim().equalsIgnoreCase("-")) {
								debtEquity = 0.00;
							}else {
							
								debtEquity = Double.parseDouble(td.text().replace(",", "").trim());
							}
							
							stockFactor.setDebtEquity(debtEquity);
							
						}
					}

				}else {
					continue;
				}
				
			}

		}
		return stockFactor;
	}

	private String buildURL(Stock stock) {

		String companyName = stock.getCompanyName().replace(".", "");

		String companyNameurlStr = companyName.replaceAll(" ", "-").toLowerCase();
		
		companyNameurlStr = companyNameurlStr.replace("&-", "");
		
		companyNameurlStr = companyNameurlStr.replace("'s", "-s");

		return BASE_URL_REDIFF + companyNameurlStr;
	}

	public String getRatioURL() {
		return ratioURL;
	}

	public void setRatioURL(String ratioURL) {
		this.ratioURL = ratioURL;
	}

	@Override
	public FactorProvider getServiceProvider() {
		return FactorProvider.REDIFF;
	}

	@Override
	public StockFactor getFactor(Stock stock){

		StockFactor stockFactor =null;
		
		if(stock.getStockFactor() == null) {
			
			stockFactor = new StockFactor();
			
		}else {
			
			stockFactor = stock.getStockFactor();
			
			stockFactor.setLastModified(LocalDate.now());
		}
		
		try {
			stockFactor = this.getMcapFaceValue(stock, stockFactor);
			stockFactor = this.getRatios(stock, stockFactor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return stockFactor;
	}
}
