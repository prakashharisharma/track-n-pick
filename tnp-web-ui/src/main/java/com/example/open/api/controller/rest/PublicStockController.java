package com.example.open.api.controller.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.CrossOverLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.BreakoutLedgerService;
import com.example.service.CrossOverLedgerService;
import com.example.service.RuleService;
import com.example.service.StockService;
import com.example.ui.model.RecentCrossOverIO;
import com.example.ui.model.StockDetailsIO;
import com.example.ui.model.StockSearch;
import com.example.util.FormulaService;
import com.example.util.io.model.StockPriceIO;

@RestController
@RequestMapping("/public/api/stocks")
public class PublicStockController {

	@Autowired
	private StockService stockService;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private CrossOverLedgerService crossOverLedgerService;

	@Autowired
	private BreakoutLedgerService breakoutLedgerService;

	@GetMapping(value = "/{stockId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getStockFundamentals(@PathVariable long stockId) {

		Stock stock = stockService.getStockById(stockId);

		if (stock != null) {

			StockPrice stockPrice = stock.getStockPrice();

			StockFactor stockFactor = stock.getStockFactor();

			StockTechnicals stockTechnicals = stock.getTechnicals();

			double pe = formulaService.calculatePe(stockPrice.getCurrentPrice(), stockFactor.getEps());

			double pb = formulaService.calculatePb(stockPrice.getCurrentPrice(), stockFactor.getBookValue());

			String valuation = "NUETRAL";

			if (ruleService.isUndervalued(stock)) {
				valuation = "UNDERVALUE";
			}else if (ruleService.isOvervalued(stock)) {
				valuation = "OVERVALUED";
			}
			else {
				valuation = "NUETRAL";
			}

			double sok = stockTechnicals.getSok();

			double sod = stockTechnicals.getSod();

			long obv = stockTechnicals.getObv();

			double rocv = stockTechnicals.getRocv();

			double ema20 = stockTechnicals.getSma20();

			double ema50 = stockTechnicals.getSma50();

			double ema100 = stockTechnicals.getSma100();

			double ema200 = stockTechnicals.getSma200();

			String crossOver = this.getCrossOver(stock);
			
			String breakOut = this.getBreakOut(stock);
			
			StockDetailsIO stockDetails = new com.example.ui.model.StockDetailsIO(stock.getNseSymbol(),
					stock.getSector().getSectorName(), stockPrice.getCurrentPrice(), stockPrice.getYearLow(),
					stockPrice.getYearHigh(), stockFactor.getMarketCap(), stockFactor.getDebtEquity(),
					stockFactor.getCurrentRatio(), stockFactor.getQuickRatio(), stockFactor.getDividend(), pb, pe,
					stock.getSector().getSectorPe(), stock.getSector().getSectorPb(), stockFactor.getReturnOnEquity(),
					stockFactor.getReturnOnCapital(), stockTechnicals.getRsi(), valuation, stock.getPrimaryIndice(),
					sok, sod, obv, rocv, ema20, ema50, ema100, ema200);
			
			stockDetails.setCrossOver(crossOver);
			stockDetails.setBreakOut(breakOut);

			return ResponseEntity.ok(stockDetails);
		} else {
			return ResponseEntity.ok("NOT FOUND");
		}
	}

	@GetMapping(value = "/searchstock", produces = { MediaType.APPLICATION_ATOM_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<StockSearch>> searchStock1(@RequestParam String query) {

		List<StockSearch> stocksList = new ArrayList<>();

		List<Stock> stocksMasterList = stockService.activeStocks();

		stocksMasterList.forEach(s -> {
			stocksList.add(new StockSearch(s.getStockId(), s.getCompanyName() + " - [" + s.getNseSymbol() + "]"));
		});

		List<StockSearch> searchResult = stocksList.stream()
				.filter(s -> s.getCompanyNameAndSymbol().toLowerCase().contains(query.toLowerCase()))
				.collect(Collectors.toList());

		if (searchResult.isEmpty()) {

			List<StockSearch> noSearchResult = new ArrayList<>();

			noSearchResult.add(new StockSearch(0, "No Result Found"));

			return ResponseEntity.ok(noSearchResult);
		}

		return ResponseEntity.ok(searchResult);
	}
	@GetMapping(value = "/recentcrossover", produces = { 
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<RecentCrossOverIO> recentCrossOver() {
		
		RecentCrossOverIO recentCrossOverIO = new RecentCrossOverIO(this.getRecentCrossOver());
		
		return ResponseEntity.ok(recentCrossOverIO);
	}
	
	private String getCrossOver(Stock stock) {
		StringBuilder crossOverHtml = new StringBuilder("<p>");

		List<CrossOverLedger> crossOverList = crossOverLedgerService.getCrossOver(stock);

		crossOverList.forEach(cl -> {
			if (cl.getCrossOverType() == CrossOverLedger.CrossOverType.BULLISH) {
				crossOverHtml.append("<font color='green'>");

				crossOverHtml.append(cl.getCrossOverType().toString() + " of " + cl.getCrossOverCategory().toString()
						+ " on " + cl.getResearchDate().toString());
				crossOverHtml.append("</font>");
			} else {
				crossOverHtml.append("<font color='red'>");
				crossOverHtml.append(cl.getCrossOverType().toString() + " of " + cl.getCrossOverCategory().toString()
						+ " on " + cl.getResearchDate().toString());
				crossOverHtml.append("</font>");
			}
			crossOverHtml.append("<br/>");

		});
		crossOverHtml.append("</p>");

		return crossOverHtml.toString();
	}

	private String getBreakOut(Stock stock) {
		StringBuilder breakOutHtml = new StringBuilder("<p>");

		List<BreakoutLedger> breakoutList = breakoutLedgerService.getBreakouts(stock);

		breakoutList.forEach(bl -> {
			if (bl.getBreakoutType() == BreakoutLedger.BreakoutType.POSITIVE) {
				breakOutHtml.append("<font color='green'>");
				breakOutHtml.append(bl.getBreakoutType().toString() +" of " + bl.getBreakoutCategory().toString() +" on " + bl.getBreakoutDate().toString());
				breakOutHtml.append("</font>");
			} else {
				breakOutHtml.append("<font color='red'>");
				breakOutHtml.append(bl.getBreakoutType().toString() +" of " + bl.getBreakoutCategory().toString() +" on " + bl.getBreakoutDate().toString());
				breakOutHtml.append("</font>");
			}
			breakOutHtml.append("<br/>");
		});

		breakOutHtml.append("</p>");
		return breakOutHtml.toString();
	}

	private String getRecentCrossOver() {
		StringBuilder recentHtml = new StringBuilder("<p>");
		
		List<CrossOverLedger> crossOverList = crossOverLedgerService.getRecentCrossOver();
		
		crossOverList.forEach(cl -> {
			if (cl.getCrossOverType() == CrossOverLedger.CrossOverType.BULLISH) {
				recentHtml.append("<font color='green'>");

				recentHtml.append(cl.getStockId().getNseSymbol());
				recentHtml.append("&nbsp;&nbsp;&nbsp;--&nbsp;&nbsp;&nbsp;");
				recentHtml.append(cl.getResearchDate().toString());
				recentHtml.append("&nbsp;&nbsp;&nbsp;--&nbsp;&nbsp;&nbsp;");
				recentHtml.append("</font>");
			} else {
				recentHtml.append("<font color='red'>");
				recentHtml.append(cl.getStockId().getNseSymbol());
				recentHtml.append("&nbsp;&nbsp;&nbsp;--&nbsp;&nbsp;&nbsp;");
				recentHtml.append(cl.getResearchDate().toString());
				recentHtml.append("&nbsp;&nbsp;&nbsp;--&nbsp;&nbsp;&nbsp;");
				recentHtml.append("</font>");
			}
			
		});
		
		recentHtml.append("</p>");
		
		return recentHtml.toString();
	}
	
	@GetMapping(value = "/active/nifty500", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<StockPriceIO>> getStocksMasterNifty500() {

		List<StockPriceIO> stockSearchList = new ArrayList<>();
		
		List<Stock> stockList = stockService.nifty500();
		
		stockList.forEach(s -> {
			stockSearchList.add(new StockPriceIO(s.getNseSymbol(), s.getIsinCode()));
		});
		
		return ResponseEntity.ok(stockSearchList);
	}
}
