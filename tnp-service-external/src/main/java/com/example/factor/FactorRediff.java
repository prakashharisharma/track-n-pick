package com.example.factor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.common.FactorProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;

@Service
public class FactorRediff implements FactorBaseService {

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

		System.out.println(mcapFaceValueURL);

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
							//System.out.println("Book Value : "+ td.text());
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
							
							//System.out.println("ROE : " + td.text());
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
							
							//System.out.println("ROCE : " + td.text());
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
							
							//System.out.println("Debt/Equity : "+td.text());
						}
					}

				}else {
					continue;
				}
				
			}

		}
		return stockFactor;
	}

	public static void main(String[] args) throws IOException {

		FactorRediff fbs = new FactorRediff();

		Stock stock = new Stock("ABB India Ltd.", "ABB", "321654");

		StockFactor sf = new StockFactor();

		fbs.getMcapFaceValue(stock, sf);
		
		fbs.getRatios(stock, sf);

		/*
		 * 
		 * Document doc =
		 * Jsoup.connect("https://money.rediff.com/companies/ambuja-cements-ltd").get();
		 * 
		 * Element body = doc.body();
		 * 
		 * Element allElement = body.getElementsByClass("zoom-container").first();
		 * 
		 * String url = allElement.select("a").first().absUrl("href");
		 * 
		 * System.out.println(url);
		 * 
		 * System.out.println("*********************");
		 * 
		 * String mcapFaceValueURL = url.replace("/bse/day/chart", "");
		 * 
		 * System.out.println( "mcapFaceValueURL " + mcapFaceValueURL);
		 * 
		 * String ratioURL = mcapFaceValueURL + "/ratio";
		 * 
		 * System.out.println("ratioURL " + ratioURL);
		 * 
		 * Document doc1 = Jsoup.connect(ratioURL).get(); Element body1 = doc1.body();
		 * Elements allElements = body1.getAllElements();
		 * 
		 * Elements sections = allElements.first().getElementsByTag("table");
		 * 
		 * System.out.println("*******************************");
		 * 
		 * int i = 0;
		 * 
		 * for (Element element : sections) { i++;
		 * 
		 * if (i == 2) {
		 * 
		 * System.out.println("**********CHILDS*********************" + i);
		 * 
		 * Elements chilrd = element.getElementsByTag("tr");
		 * 
		 * for (Element childElement : chilrd) {
		 * System.out.println(childElement.text());
		 * 
		 * } } else { continue; } System.out.println("*******************************");
		 * 
		 * }
		 * 
		 */}

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
	public StockFactor getFactor(Stock stock) throws MalformedURLException, IOException {

		StockFactor stockFactor =null;
		
		if(stock.getStockFactor() == null) {
			
			stockFactor = new StockFactor();
			
		}else {
			
			stockFactor = stock.getStockFactor();
			
			stockFactor.setLastModified(LocalDate.now());
		}
		
		stockFactor = this.getMcapFaceValue(stock, stockFactor);
		
		stockFactor = this.getRatios(stock, stockFactor);
		
		return stockFactor;
	}
}
