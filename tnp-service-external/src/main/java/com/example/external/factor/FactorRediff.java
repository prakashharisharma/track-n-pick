package com.example.external.factor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.external.common.FactorProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;

@Service
public class FactorRediff implements FactorBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactorRediff.class);
	
	private static String BASE_URL_REDIFF = "https://money.rediff.com/companies/";

	static Map<String, String> dateMap = new HashMap<>();
	
	static Map<String, String> resultQuarterMap = new HashMap<>();
	
	static {
		dateMap.put("MAR", "31");
		dateMap.put("JUN", "30");
		dateMap.put("SEP", "30");
		dateMap.put("DEC", "31");
	}
	
	static {
		resultQuarterMap.put("MAR", "DEC");
		resultQuarterMap.put("JUN", "MAR");
		resultQuarterMap.put("SEP", "JUN");
		resultQuarterMap.put("DEC", "SEP");
	}
	
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

			int trCount = 0;
			
			for (Element childElement : childTr) {
				trCount++;
			
				if (trCount == 1) { // Period
					Elements chilTheadTh = element.getElementsByTag("th");

					int j = 0;

					for (Element th : chilTheadTh) {

						j++;
						if (j == 2) {
							String quarterStr= th.text();
							
							String quarterStrArr[] = quarterStr.split("'");
							
							String Month = quarterStrArr[0].trim().toUpperCase();
							
							String date = dateMap.get(Month);
							
							String resultMonth = resultQuarterMap.get(Month);
							
							String dateStr = date+"-"+resultMonth+"-" + "20"+quarterStrArr[1].trim();
							
							DateTimeFormatter formatter=new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH);
							
							LocalDate quarterEnded= LocalDate.parse(dateStr,formatter);
							
							stockFactor.setQuarterEnded(quarterEnded);
							//Store Quarter 
						}
					}
				}
				
				
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

				}
				else if (childElement.text().startsWith("Current ratio")) {

					Elements childTd = childElement.getElementsByTag("td");
					int j = 0;
					for (Element td : childTd) {
						j++;
						if (j == 2) {
							double currentRatio = 0.00;
							if(td.text().trim().equalsIgnoreCase("-")) {
								currentRatio = 0.00;
							}else {
							
								currentRatio = Double.parseDouble(td.text().replace(",", "").trim());
							}
							
							stockFactor.setCurrentRatio(currentRatio);
							
						}
					}

				}
				else if (childElement.text().startsWith("Quick ratio")) {

					Elements childTd = childElement.getElementsByTag("td");
					int j = 0;
					for (Element td : childTd) {
						j++;
						if (j == 2) {
							double quickRatio = 0.00;
							if(td.text().trim().equalsIgnoreCase("-")) {
								quickRatio = 0.00;
							}else {
							
								quickRatio = Double.parseDouble(td.text().replace(",", "").trim());
							}
							
							stockFactor.setQuickRatio(quickRatio);
							
						}
					}

				}
				
				
				else {
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
			
			
		}
		
		try {
			stockFactor.setLastModified(LocalDate.now());
			stockFactor = this.getMcapFaceValue(stock, stockFactor);
			stockFactor = this.getRatios(stock, stockFactor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stockFactor.setStock(stock);
		
		return stockFactor;
	}
}
