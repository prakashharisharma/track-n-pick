package com.example.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;

@Service
public class PrettyPrintService {

	@Autowired
	private MiscUtil miscUtil;
	
	@Autowired
	private FormulaService formulaService;
	
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

	public String printPortFolio(List<UserPortfolio> portfolioList) {
		
		StringBuilder sb = new StringBuilder();

		sb.append("SYNBOL" + "," + "AVG_PRICE" + "," +  "CURRENT_PRICE" + "," +"QTY" + "," + "DEBT_EQUITY" + ","
				+ "MCAP" + ","  + "ROE" + "\n");

		portfolioList.forEach((s) -> {

					sb.append(s.getStock().getNseSymbol() + "," + s.getAveragePrice() + ","
							+ s.getStock().getStockPrice().getCurrentPrice() + "," + s.getQuantity() + ","
							+ s.getStock().getStockFactor().getDebtEquity() + "," + s.getStock().getStockFactor().getMarketCap() + ","
							 + s.getStock().getStockFactor().getReturnOnEquity() + "\n");
				});

		return sb.toString();
	}
	
	public String formatBuyListHTML(List<Stock> fundamentalList, List<Stock> technicalsList) {


		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Our research pick Buy List</h3></div>");
		if(!fundamentalList.isEmpty()) {
			sb.append("<p>Fundamental </p>");
		
		sb.append("<div><table><thead><tr><th>SYMBOL</th><th>INDICE</th><th>CMP</th><th>PE</th><th>PB</th><th>DEBT_EQ</th><th>ROE</th></tr></thead>");

		sb.append("<tbody>");

		fundamentalList.forEach((s) -> {

			double currentPrice = s.getStockPrice().getCurrentPrice();
			
			double eps = s.getStockFactor().getEps();
			double bookValue = s.getStockFactor().getBookValue();
			
			double pe = formulaService.calculatePe(currentPrice, eps);
			double pb= formulaService.calculatePb(currentPrice, bookValue);
			
			sb.append("<tr><td>" + s.getNseSymbol() + "</td><td>" 
					+ s.getPrimaryIndice()+ "</td><td>"
					+ s.getStockPrice().getCurrentPrice() + "</td><td>" 
					+ formatDouble(pe) + "</td><td>"
					+ formatDouble(pb) + "</td><td>"
					+ s.getStockFactor().getDebtEquity() + "</td><td>" 
					+ s.getStockFactor().getReturnOnEquity()
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");

		}
		
		
		if(!technicalsList.isEmpty()) {
			sb.append("<p>Technicals </p>");
		sb.append("<div><table><thead><tr><th>SYMBOL</th><th>INDICE</th><th>CMP</th><th>SMA50</th><th>SMA200</th><th>RSI</th></tr></thead>");

		sb.append("<tbody>");

		technicalsList.forEach((s) -> {

			sb.append("<tr><td>" + s.getNseSymbol() + "</td><td>" 
					+ s.getPrimaryIndice() + "</td><td>"
					+ s.getStockPrice().getCurrentPrice() + "</td><td>" 
					+ s.getTechnicals().getSma50() + "</td><td>"
					+ s.getTechnicals().getSma200() + "</td><td>" 
					+ s.getTechnicals().getRsi()
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");

		}
	
		
		return sb.toString();
	
	}
	
	public String formatSellListHTML(List<Stock> fundamentalList, List<Stock> technicalsList) {


		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Our research Sell List</h3></div>");
		if(!fundamentalList.isEmpty()) {
			sb.append("<p>Fundamenta </p>");
			sb.append("<div><table><thead><tr><th>SYMBOL</th><th>INDICE</th><th>CMP</th><th>PE</th><th>PB</th><th>DEBT_EQ</th><th>ROE</th></tr></thead>");

		sb.append("<tbody>");

		fundamentalList.forEach((s) -> {

double currentPrice = s.getStockPrice().getCurrentPrice();
			
			double eps = s.getStockFactor().getEps();
			double bookValue = s.getStockFactor().getBookValue();
			
			double pe = formulaService.calculatePe(currentPrice, eps);
			double pb= formulaService.calculatePb(currentPrice, bookValue);
			
			sb.append("<tr><td>" + s.getNseSymbol() + "</td><td>" 
					+ s.getPrimaryIndice()+ "</td><td>"
					+ s.getStockPrice().getCurrentPrice() + "</td><td>" 
					+ formatDouble(pe) + "</td><td>"
					+ formatDouble(pb) + "</td><td>"
					+ s.getStockFactor().getDebtEquity() + "</td><td>" 
					+ s.getStockFactor().getReturnOnEquity()
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");

		}
		if(!technicalsList.isEmpty()) {
			sb.append("<p>Technicals </p>");
		
			sb.append("<div><table><thead><tr><th>SYMBOL</th><th>INDICE</th><th>CMP</th><th>SMA50</th><th>SMA200</th><th>RSI</th></tr></thead>");

		sb.append("<tbody>");

		technicalsList.forEach((s) -> {

			sb.append("<tr><td>" + s.getNseSymbol() + "</td><td>" 
					+ s.getPrimaryIndice() + "</td><td>"
					+ s.getStockPrice().getCurrentPrice() + "</td><td>" 
					+ s.getTechnicals().getSma50() + "</td><td>"
					+ s.getTechnicals().getSma200() + "</td><td>" 
					+ s.getTechnicals().getRsi()
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");

		}
		
		
		return sb.toString();
	
	}
	
	public String getDisclaimer() {
		StringBuilder sb = new StringBuilder();
		sb.append("<div><p><span><b><i>DISCLAIMER : </i></b></span><i>tracknpick.com is comprised of reports, if any, embodying a unique system of stock analysis. Accuracy and completeness cannot be guaranteed. Users should be aware of the risks involved in stock investments.</i></p><p><i>All investors are advised to conduct their own independent research into individual stocks before making a purchase decision.You should be aware of the risks involved in stock investing, and you use the material contained herein at your own risk. Neither tracknpick.com nor any of its suppliers guarantee its accuracy or validity, nor are they responsible for any errors or omissions which may have occurred. The analysis, ratings, and/or recommendations made by tracknpick.com, and/or any of its suppliers do not provide, imply, or otherwise constitute a guarantee of performance.</i></p></div>");
		
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

		sb.append("<div><p><span><b><i>DISCLAIMER : </i></b></span><i>tracknpick.com is comprised of reports, if any, embodying a unique system of stock analysis. Accuracy and completeness cannot be guaranteed. Users should be aware of the risks involved in stock investments.</i></p><p><i>All investors are advised to conduct their own independent research into individual stocks before making a purchase decision.You should be aware of the risks involved in stock investing, and you use the material contained herein at your own risk. Neither tracknpick.com nor any of its suppliers guarantee its accuracy or validity, nor are they responsible for any errors or omissions which may have occurred. The analysis, ratings, and/or recommendations made by tracknpick.com, and/or any of its suppliers do not provide, imply, or otherwise constitute a guarantee of performance.</i></p></div>");
		
		}
		
		return sb.toString();
	}

	public String formatResearchListTargetAchivedHTML(List<ResearchLedger> researchList) {/*

		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Our past research achived the target</h3>");
		if(researchList.isEmpty()) {
			sb.append("<p>Don't be in hurry, Wait for market correction! </p></div>");
		}else {
		
		sb.append(
				"<table><thead><tr><th>SYMBOL</th><th>RESEARCH_DATE</th><th>RESEARCH_PRICE</th><th>CURRENT</th><th>LOW52</th><th>HIGH52</th><th>DEBT_EQ</th><th>ROE</th></tr></thead>");

		sb.append("<tbody>");

		researchList.forEach((s) -> {

			sb.append("<tr><td>" + s.getStock().getNseSymbol() + "</td><td>"+ s.getResearchDate() + "</td><td>" + s.getResearchPrice() + "</td><td>"+ s.getStock().getStockPrice().getCurrentPrice() + "</td><td>"
					+ s.getStock().getStockPrice().getYearLow() + "</td><td>" + s.getStock().getStockPrice().getYearHigh() + "</td><td>"
					+ s.getStock().getStockFactor().getDebtEquity() + "</td><td>" + s.getStock().getStockFactor().getReturnOnEquity()
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");

		sb.append("<div><p><span><b><i>DISCLAIMER : </i></b></span><i>tracknpick.com is comprised of reports, if any, embodying a unique system of stock analysis. Accuracy and completeness cannot be guaranteed. Users should be aware of the risks involved in stock investments.</i></p><p><i>All investors are advised to conduct their own independent research into individual stocks before making a purchase decision.You should be aware of the risks involved in stock investing, and you use the material contained herein at your own risk. Neither tracknpick.com nor any of its suppliers guarantee its accuracy or validity, nor are they responsible for any errors or omissions which may have occurred. The analysis, ratings, and/or recommendations made by tracknpick.com, and/or any of its suppliers do not provide, imply, or otherwise constitute a guarantee of performance.</i></p></div>");
		
		}
		
		return sb.toString();
	*/
		return null;
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
	
	public String formatOvervaluedStocksHTML(List<UserPortfolio> stocksList) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Overvalued Stocks in your portfolio</h3>");
		
		if(stocksList.isEmpty()) {
			sb.append("<p>Cheers you are on the ride!  </p></div>");
		}else {
		
		sb.append(
				"<table><thead><tr><th>SYMBOL</th><th>CURRENT</th><th>PE</th><th>PB</th><th>ROE</th><th>ROCE</th><th>DER</th><th>CER</th><th>PROFIT%</th></tr></thead>");

		sb.append("<tbody>");

		stocksList.forEach((s) -> {

			double currentPrice = s.getStock().getStockPrice().getCurrentPrice();
			
			double averagePrice = s.getAveragePrice();
			
			double profir_per = ((currentPrice - averagePrice) * 100)/averagePrice;

			double bookValue = s.getStock().getStockFactor().getBookValue();

			double eps = s.getStock().getStockFactor().getEps();

			double pe = formulaService.calculatePe(currentPrice, eps);

			double pb = formulaService.calculatePb(currentPrice, bookValue);
			
			sb.append("<tr><td>" + s.getStock().getNseSymbol() + "</td><td>" + currentPrice +"</td><td>" + pe + "</td><td>"
					+ pb + "</td><td>" + s.getStock().getStockFactor().getReturnOnEquity() + "</td><td>"
					+ s.getStock().getStockFactor().getReturnOnCapital() + "</td><td>"
					+ s.getStock().getStockFactor().getDebtEquity() + "</td><td>"
					+ s.getStock().getStockFactor().getCurrentRatio() + "</td><td>"
					+ formatDouble(profir_per)  
					+ "</td></tr>");
		});

		sb.append("</tbody></table></div>");
		}
		return sb.toString();
	}
	public String formatUndervaluedStocksHTML(List<UserPortfolio> stocksList) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div><h3>Undervalued Stocks in your portfolio</h3>");
		
		if(stocksList.isEmpty()) {
			sb.append("<p>Don't worry, Be Optimistic, Hold on!</p></div>");
		}else {
		
		sb.append(
				"<table><thead><tr><th>SYMBOL</th><th>CURRENT</th><th>PE</th><th>PB</th><th>ROE</th><th>ROCE</th><th>DER</th><th>CER</th><th>PROFIT%</th></tr></thead>");

		sb.append("<tbody>");

		stocksList.forEach((s) -> {

			double currentPrice = s.getStock().getStockPrice().getCurrentPrice();
			
			double averagePrice = s.getAveragePrice();
			
			double profir_per = ((currentPrice - averagePrice) * 100)/averagePrice;

			double bookValue = s.getStock().getStockFactor().getBookValue();

			double eps = s.getStock().getStockFactor().getEps();

			double pe = formulaService.calculatePe(currentPrice, eps);

			double pb = formulaService.calculatePb(currentPrice, bookValue);
			
			sb.append("<tr><td>" + s.getStock().getNseSymbol() + "</td><td>" + currentPrice +"</td><td>" + pe + "</td><td>"
					+ pb + "</td><td>" + s.getStock().getStockFactor().getReturnOnEquity() + "</td><td>"
					+ s.getStock().getStockFactor().getReturnOnCapital() + "</td><td>"
					+ s.getStock().getStockFactor().getDebtEquity() + "</td><td>"
					+ s.getStock().getStockFactor().getCurrentRatio() + "</td><td>"
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
