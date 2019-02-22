package com.example.external.factor;

import java.io.IOException;
import java.net.MalformedURLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.external.common.FactorProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;

@Service
public class FactorNdtvScreener implements FactorBaseService {

	private static String BASE_URL_NDTV = "https://www.ndtv.com/business/stock/";

	private static String BASE_URL_SCREENER = "https://www.screener.in/company/";

	@Override
	public FactorProvider getServiceProvider() {
		return FactorProvider.NDTV_SCREENER;
	}

	@Override
	public StockFactor getFactor(Stock stock) throws MalformedURLException, IOException {

		StockFactor stockFactor = new StockFactor();
		
		stockFactor = this.screenerFactor(stock, stockFactor);
		
		stockFactor = this.ndtvFactor(stock, stockFactor);
		
		return stockFactor;
	}

	private StockFactor ndtvFactor(Stock stock, StockFactor stockFactor) throws IOException {

		String url = this.buildNdtvURL(stock);
		
		Document doc = Jsoup.connect(url).get();
		
		Elements allElements = doc.select("table#keyfunda");
		
		for (Element element : allElements) {
			
			Elements chilrd = element.getElementsByTag("td");
			
			int j=0;
			
			for (Element childElement : chilrd) {
				
				j++;
			/*	if(j==2) {
					System.out.println( "Market Cap : "+ childElement.text());
				}
				if(j==4) {
					System.out.println( "EPS : "+ childElement.text());
				}
				if(j==6) {
					System.out.println( "Stock P/E : "+ childElement.text());
				}
				if(j==8) {
					System.out.println( "Book Value : "+ childElement.text());
				}
				if(j==10) {
					System.out.println( "PB : "+ childElement.text());
				}
				if(j==16) {
					System.out.println( "ROCE : "+ childElement.text());
				}*/
				if(j==20) {
					
					double debtEquity = Double.parseDouble(childElement.text());
					
					//System.out.println( "DEBT/EQUITY : "+ childElement.text());
					
					stockFactor.setDebtEquity(debtEquity);;
				}
			}
			
		}
		
		return stockFactor;
	}

	private String buildNdtvURL(Stock stock) {

		String companyNmae = stock.getCompanyName().replace(".", "_");

		String companyNameWithUnderScore = companyNmae.replaceAll(" ", "-");

		String companyNameWithNseSymbol = companyNameWithUnderScore + stock.getNseSymbol();

		return BASE_URL_NDTV + companyNameWithNseSymbol.toLowerCase();
	}

	private StockFactor screenerFactor(Stock stock, StockFactor stockFactor) throws IOException {

		String url = this.buildScreenerUrl(stock);

		Document doc = Jsoup.connect(url).get();

		Element body = doc.body();

		Elements allElements = body.getAllElements();

		Elements sections = allElements.first().getElementsByTag("section");

		Element data = sections.first();

		Elements chilrd = data.getElementsByTag("li");

		double currentPrice = 0.00;

		for (Element element : chilrd) {

			String elementsTxt = element.text();

			if (elementsTxt.startsWith("Market Cap")) {

				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				}

				//System.out.println("Market Cap" + " : " + elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				double marketCap = Double.parseDouble(elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				stockFactor.setMarketCap(marketCap);
				
			}
			if (elementsTxt.startsWith("Current Price")) {
				
				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				}

				//System.out.println("Current Price" + " : " + elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				currentPrice = Double.parseDouble(elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
			}
			
			if (elementsTxt.startsWith("Book Value")) {
				
				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				}

				//System.out.println("Book Value" + " : " + elemtsTxt[1].trim());
				
				double bookValue = Double.parseDouble(elemtsTxt[1].trim());
				
				//double pb = currentPrice / bookValue;
				
				stockFactor.setBookValue(bookValue);
				
			}
			if (elementsTxt.startsWith("Stock P/E")) {
				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				}

				//System.out.println("Stock PE" + " : " + elemtsTxt[1].trim());
				
				double pe = Double.parseDouble(elemtsTxt[1].trim());
				
				double EPS = currentPrice / pe;
				
				stockFactor.setEps(EPS);
			}
			if (elementsTxt.startsWith("Dividend")) {
				
				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				} 

				//System.out.println("Dividend Yield" + " : " + elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				double dividendYeild = Double.parseDouble(elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				double dividend = (dividendYeild * currentPrice)/100;
				
				stockFactor.setDividend(dividend);
				
			}
			if (elementsTxt.startsWith("ROE")) {
				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				}

				double roe = Double.parseDouble(elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				//System.out.println("ROE" + " : " + elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				stockFactor.setReturnOnEquity(roe);
			}
			if (elementsTxt.startsWith("ROCE")) {
				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				}

				double roce = Double.parseDouble(elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				//System.out.println("ROCE" + " : " + elemtsTxt[1].trim().split(" ")[0].replace(",", ""));
				
				stockFactor.setReturnOnCapital(roce);
			}
			if (elementsTxt.startsWith("Face Value")) {
				String elemtsTxt[] = element.text().split(":");

				if (elemtsTxt.length == 1) {

					continue;
				}

				double faceValue = Double.parseDouble(elemtsTxt[1].trim());
				
				//System.out.println("Face Value" + " : " + elemtsTxt[1].trim());
				stockFactor.setFaceValue(faceValue);
			}

		}
		
		return stockFactor;
	}

	private String buildScreenerUrl(Stock stock) {

		return BASE_URL_SCREENER + stock.getNseSymbol() + "/";
	}
}
