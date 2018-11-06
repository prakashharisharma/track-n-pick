package com.example.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;

@Service
public class PrettyPrintService {

	@Autowired
	private MiscUtil miscUtil;
	
	public String printWatchList(List<Stock> watchList) {

		StringBuilder sb = new StringBuilder();

		sb.append("SYNBOL" + "," + "CURRENT_PRICE" + "," + "YEAR_LOW" + "," + "YEAR_HIGH" + "," + "DEBT_EQUITY" + ","
				+ "MCAP" + "," + "DIVIDEND" + "," + "ROE" + "\n");

		watchList.forEach((s) -> {

					sb.append(s.getNseSymbol() + "," + s.getStockPrice().getCurrentPrice() + ","
							+ s.getStockPrice().getYearLow() + "," + s.getStockPrice().getYearHigh() + ","
							+ s.getStockFactor().getDebtEquity() + "," + s.getStockFactor().getMarketCap() + ","
							+ s.getStockFactor().getDividend() + "," + s.getStockFactor().getReturnOnEquity() + "\n");
				});

		return sb.toString();
	}

	public String formatWatchListHTML(List<Stock> watchList) {

		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Our research pick</h3>");
		if(watchList.isEmpty()) {
			sb.append("<p>Don't be in hurry, Wait for market correction! </p></div>");
		}else {
		
		sb.append(
				"<table><thead><tr><th>SYMBOL</th><th>CURRENT</th><th>LOW52</th><th>HIGH52</th><th>DEBT_EQ</th><th>ROE</th></tr></thead>");

		sb.append("<tbody>");

		watchList.forEach((s) -> {

			sb.append("<tr><td>" + s.getNseSymbol() + "</td><td>" + s.getStockPrice().getCurrentPrice() + "</td><td>"
					+ s.getStockPrice().getYearLow() + "</td><td>" + s.getStockPrice().getYearHigh() + "</td><td>"
					+ s.getStockFactor().getDebtEquity() + "</td><td>" + s.getStockFactor().getReturnOnEquity()
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");
		}
		return sb.toString();
	}

	public String formatPortfolioHTML(List<UserPortfolio> stocksList) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Your portfolio</h3>");
		
		if(stocksList.isEmpty()) {
			sb.append("<p>Market is in shape, Buy some quality stocks and build your portfolio! </p></div>");
		}else {
		
		sb.append(
				"<table><thead><tr><th>SYMBOL</th><th>AVG.</th><th>CURRENT</th><th>LOW52</th><th>HIGH52</th><th>PROFIT%</th></tr></thead>");

		sb.append("<tbody>");

		stocksList.forEach((s) -> {

			double currentPrice = s.getStock().getStockPrice().getCurrentPrice();
			
			double averagePrice = s.getAveragePrice();
			
			double profir_per = ((currentPrice - averagePrice) * 100)/averagePrice;
			
			sb.append("<tr><td>" + s.getStock().getNseSymbol() + "</td><td>" + s.getAveragePrice() +"</td><td>" + s.getStock().getStockPrice().getCurrentPrice() + "</td><td>"
					+ s.getStock().getStockPrice().getYearLow() + "</td><td>" + s.getStock().getStockPrice().getYearHigh() + "</td><td>"
					+ formatDouble(profir_per)  
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");
		}
		return sb.toString();
	}
	
	public String formatBookProfitHTML(List<UserPortfolio> stocksList) {

		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Target achieved</h3>");
		
		if(stocksList.isEmpty()) {
			sb.append("<p>Don't worry, Be Optimistic, Hold on! </p></div>");
		}else {
		
		sb.append(
				"<table><thead><tr><th>SYMBOL</th><th>AVG.</th><th>CURRENT</th><th>LOW52</th><th>HIGH52</th><th>PROFIT%</th></tr></thead>");

		sb.append("<tbody>");

		stocksList.forEach((s) -> {

			double currentPrice = s.getStock().getStockPrice().getCurrentPrice();
			
			double averagePrice = s.getAveragePrice();
			
			double profir_per = ((currentPrice - averagePrice) * 100)/averagePrice;
			
			sb.append("<tr><td>" + s.getStock().getNseSymbol() + "</td><td>" + s.getAveragePrice() +"</td><td>" + s.getStock().getStockPrice().getCurrentPrice() + "</td><td>"
					+ s.getStock().getStockPrice().getYearLow() + "</td><td>" + s.getStock().getStockPrice().getYearHigh() + "</td><td>"
					+ formatDouble(profir_per)  
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");
		}
		return sb.toString();
	}
	
	public String formatAveragingHTML(List<UserPortfolio> stocksList) {

		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Consider averaging </h3>");
		
		if(stocksList.isEmpty()) {
			sb.append("<p>Cheers you are on the ride! </p></div>");
		}else {
		
		sb.append(
				"<table><thead><tr><th>SYMBOL</th><th>CURRENT</th><th>LOW52</th><th>HIGH52</th><th>DOWN%</th><th>ROE</th></tr></thead>");

		sb.append("<tbody>");

		stocksList.forEach((s) -> {

			double currentPrice = s.getStock().getStockPrice().getCurrentPrice(); //80
			
			double averagePrice= s.getAveragePrice(); // 100
			
			double loss = averagePrice - currentPrice; // 20
			
			double lossPer = (loss / averagePrice) * 100;
			
			sb.append("<tr><td>" + s.getStock().getNseSymbol() + "</td><td>" + s.getStock().getStockPrice().getCurrentPrice() + "</td><td>"
					+ s.getStock().getStockPrice().getYearLow() + "</td><td>" + s.getStock().getStockPrice().getYearHigh() + "</td><td>"
					+ miscUtil.formatDouble(lossPer) + "</td><td>" + s.getStock().getStockFactor().getReturnOnEquity()
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");
		}
		return sb.toString();
	}
	
	public String formatEmailBody(String name, String tableData) {

		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><style>");
		sb.append("table {border-collapse: collapse;width: 100%;}");
		sb.append("th, td {text-align: left;padding: 8px;}");
		sb.append("tr:nth-child(even){background-color: #f2f2f2}");
		sb.append("th {background-color: #5d5249;color: white;}");
		sb.append("</style></head><body>");
		sb.append("<div><p>Dear <span><b>" + name + "</b><span>,</p><p>Your track-n-pick reserach report for "
				+ LocalDate.now() + " :</p><div>");
		sb.append(tableData);
		sb.append("</body></html>");

		return sb.toString();

	}

	public String formatEmailBody(String name, String portfolioTable, String bookProfitTable, String considerAveragingTable) {

		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><style>");
		sb.append("table {border-collapse: collapse;width: 100%;}");
		sb.append("th, td {text-align: left;padding: 8px;}");
		sb.append("tr:nth-child(even){background-color: #f2f2f2}");
		sb.append("th {background-color: #5d5249;color: white;}");
		sb.append("</style></head><body>");
		sb.append("<div><p>Dear <span><b>" + name + "</b><span>,</p><p>Your track-n-pick reserach report for "
				+ LocalDate.now() + " :</p><div>");
		sb.append(portfolioTable);
		sb.append(bookProfitTable);
		sb.append(considerAveragingTable);
		sb.append("</body></html>");

		return sb.toString();

	}

	private String formatDouble(double value) {
		
		DecimalFormat dec = new DecimalFormat("#0.00");
		
		return dec.format(value);
	}
}
