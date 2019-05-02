package com.example.ui.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.PerformanceLedger;
import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.type.IndicedAllocation;
import com.example.model.type.SectoralAllocation;
import com.example.model.um.UserProfile;
import com.example.service.DividendLedgerService;
import com.example.service.ExpenseService;
import com.example.service.FundsLedgerService;
import com.example.service.PerformanceLedgerService;
import com.example.service.PortfolioService;
import com.example.service.RuleService;
import com.example.service.TradeLedgerService;
import com.example.service.TradeProfitLedgerService;
import com.example.ui.model.ChartPerformance;
import com.example.ui.model.ChartType;
import com.example.ui.model.RenderIndiceAllocation;
import com.example.ui.model.RenderSectorWiseValue;
import com.example.ui.model.UIOverallGainLoss;
import com.example.ui.model.UIRenderStock;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;

@Service
public class UiRenderUtil {

	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private FundsLedgerService fundsLedgerService;
	
	@Autowired
	private TradeProfitLedgerService tradeProfitLedgerService;
	
	@Autowired
	private MiscUtil miscUtil;
	
	@Autowired
	private DividendLedgerService dividendLedgerService;

	@Autowired
	private PerformanceLedgerService  performanceLedgerService;
	
	@Autowired
	private TradeLedgerService tradeLedgerService;
	
	@Autowired
	private ExpenseService expenseService;
	
	@Autowired
	private FormulaService formulaService;
	
	@Autowired
	private RuleService ruleService;
	
	public List<UIRenderStock> renderPortfolio(List<UserPortfolio> userPortfolioList) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (UserPortfolio userPortfolioStock : userPortfolioList) {

			double currentPrice = userPortfolioStock.getStock().getStockPrice().getCurrentPrice();
			double averagePrice = userPortfolioStock.getAveragePrice();


			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(currentPrice, averagePrice)));

			boolean overValued = ruleService.isOvervalued(userPortfolioStock.getStock());
			
			portfolioList.add(new UIRenderStock(userPortfolioStock, profitPer, overValued));
		}

		return portfolioList;
	}

	public List<UIRenderStock> renderWatchList(List<Stock> userWatchList) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (Stock stock : userWatchList) {

			portfolioList.add(new UIRenderStock(stock));
		}

		return portfolioList;
	}

	public List<UIRenderStock> renderResearchList(List<ResearchLedger> researchList) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (ResearchLedger researchStock : researchList) {

			double currentPrice = researchStock.getStock().getStockPrice().getCurrentPrice();
			
			double bookValue= researchStock.getStock().getStockFactor().getBookValue();
			
			double eps = researchStock.getStock().getStockFactor().getEps();
			
			double pe= formulaService.calculatePe(currentPrice, eps);
			
			double pb= formulaService.calculatePb(currentPrice, bookValue);

			
			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(
					researchStock.getStock().getStockPrice().getCurrentPrice(), researchStock.getEntryPrice())));

			portfolioList.add(new UIRenderStock(researchStock, profitPer,pe, pb));
			
			
			
		}

		return portfolioList;
	}

	public List<UIRenderStock> renderAdvancedResearchList(List<ResearchLedger> researchList) {

		List<UIRenderStock> resultList = new ArrayList<>();

		for (ResearchLedger researchStock : researchList) {

			double currentPrice = researchStock.getStock().getStockPrice().getCurrentPrice();
			
			double bookValue= researchStock.getStock().getStockFactor().getBookValue();
			
			double eps = researchStock.getStock().getStockFactor().getEps();
			
			double pe= formulaService.calculatePe(currentPrice, eps);
			
			double pb= formulaService.calculatePb(currentPrice, bookValue);


			double sectorPe = researchStock.getStock().getSector().getSectorPe();
			
			double peDifference = sectorPe - pe;
			
			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(
					researchStock.getStock().getStockPrice().getCurrentPrice(), researchStock.getEntryPrice())));

			resultList.add(new UIRenderStock(researchStock, profitPer,pe, pb,peDifference));
			
		}

		resultList.stream().sorted(Comparator.comparing(UIRenderStock::getPeDifference).thenComparing(UIRenderStock::getIndice));
		
		return resultList;
	}
	
	private double calculateProfitPer(double currentPrice, double averagePrice) {

		double profit = currentPrice - averagePrice;

		double profitPer = (profit / averagePrice) * 100;

		return profitPer;
	}

	
	public UIOverallGainLoss renderUIPerformance(UserProfile user) {
		UIOverallGainLoss uIOverallGainLoss = new UIOverallGainLoss();
		
		this.renderYTDPerformance(user, uIOverallGainLoss);
		this.renderFYTDPerformance(user, uIOverallGainLoss);
		this.renderFYSummary(user, uIOverallGainLoss);
		
		return uIOverallGainLoss;
	}
	
	
	private UIOverallGainLoss renderYTDPerformance(UserProfile user, UIOverallGainLoss uIOverallGainLoss) {
		
		double currentValue = portfolioService.currentValue(user);
		
		double ytdInvestmentValue = fundsLedgerService.currentYearInvestment(user);
		
		double ytdNetProfit = tradeProfitLedgerService.currentYearProfit(user);
		
		double ytdDividend = dividendLedgerService.currentYearDividend(user);
		
		double ytdRealizedGain = ytdNetProfit + ytdDividend;
		
		double ytdUnrealizedGain = currentValue - ytdInvestmentValue;
		
		double ytdRealizedGainPer = formulaService.calculatePercentRate(ytdInvestmentValue, ytdRealizedGain);
		
		double ytdUnrealizedGainPer = formulaService.calculatePercentRate(ytdInvestmentValue, ytdUnrealizedGain);
		
		uIOverallGainLoss.setYtdInvestmentValue(ytdInvestmentValue);
		
		uIOverallGainLoss.setCurrentValue(currentValue);
		
		uIOverallGainLoss.setYtdRealizedGainPer(ytdRealizedGainPer);
		uIOverallGainLoss.setYtdUnrealizedGainPer(ytdUnrealizedGainPer);
		
		return uIOverallGainLoss;
	}
	
	private UIOverallGainLoss renderFYTDPerformance(UserProfile user, UIOverallGainLoss uIOverallGainLoss) {
		
		double currentValue = portfolioService.currentValue(user);
		
		double fyInvestmentValue = fundsLedgerService.currentFinYearInvestment(user);
		
		double fyNetProfit = tradeProfitLedgerService.currentFinYearProfit(user);
		
		double fyDividend = dividendLedgerService.currentFinYearDividend(user);
		
		double fyRealizedGain = fyNetProfit + fyDividend;
		
		double fyUnrealizedGain = currentValue - fyInvestmentValue;
		
		double fyRealizedGainPer = formulaService.calculatePercentRate(fyInvestmentValue, fyRealizedGain);
		
		double fyUnrealizedGainPer = formulaService.calculatePercentRate(fyInvestmentValue, fyUnrealizedGain);
		
		uIOverallGainLoss.setFyInvestmentValue(fyInvestmentValue);
		uIOverallGainLoss.setFyRealizedGainPer(fyRealizedGainPer);
		uIOverallGainLoss.setFyUnrealizedGainPer(fyUnrealizedGainPer);
		
		uIOverallGainLoss.setFyNetDividends(fyDividend);
		uIOverallGainLoss.setFyNetGain(fyNetProfit);
		
		return uIOverallGainLoss;
	}
	
	private UIOverallGainLoss renderFYSummary(UserProfile user, UIOverallGainLoss uIOverallGainLoss) {
		
		double fyNetExpense = expenseService.currentFinYearExpenses(user);
		
		double fyBrokeragePaid = tradeLedgerService.getFYBrokeragePaid(user);
		
		uIOverallGainLoss.setFyNetExpense(fyNetExpense + fyBrokeragePaid);
		
		double fyNetTaxLiability = uIOverallGainLoss.getFyNetGain() - fyNetExpense ;
		
		if(fyNetTaxLiability < 0.00) {
			fyNetTaxLiability = 0.00;
		}
		
		uIOverallGainLoss.setFyNetTaxLiability(fyNetTaxLiability);
		
		double fyNetTaxPaid = tradeLedgerService.getFYNetTaxPaid(user);
		
		uIOverallGainLoss.setFyNetTaxPaid(fyNetTaxPaid);
		
		return uIOverallGainLoss;
	}
	
	public List<RenderSectorWiseValue> sectoralAllocation(UserProfile user){
		
		double currentInvestmentValue = portfolioService.currentInvestmentValue(user);
		
		
		List<SectoralAllocation> sectorWiseValueList = portfolioService.sectoralAllocation(user);
		
		List<RenderSectorWiseValue> renderList = new ArrayList<>();
		
		sectorWiseValueList.forEach( sw -> {
			
			renderList.add(new RenderSectorWiseValue(sw.getSectorName(), formulaService.calculatePercentRate(currentInvestmentValue, sw.getAllocation())));
		});
		
		return renderList;
	}
	
	public List<RenderIndiceAllocation> indicedAllocation(UserProfile user){
		
		double currentInvestmentValue = portfolioService.currentInvestmentValue(user);
		
		
		List<IndicedAllocation> indiceWiseValueList = portfolioService.indicedAllocation(user);
		
		List<RenderIndiceAllocation> renderList = new ArrayList<>();
		
		indiceWiseValueList.forEach( sw -> {
			
			renderList.add(new RenderIndiceAllocation(sw.getIndice().toString(), formulaService.calculatePercentRate(currentInvestmentValue, sw.getAllocation())));
		});
		
		return renderList;
	}
	
	
	public List<ChartPerformance> yearlyPerformance(UserProfile user,ChartType chartType ) {
		
		List<ChartPerformance> chartPerformanceList = new ArrayList<ChartPerformance>();
		
		
		List<PerformanceLedger> performanceLedgerList =  performanceLedgerService.yearlyPerformance(user);
		
		if(chartType == ChartType.PERFORMANCE_CV) {
		performanceLedgerList.forEach( pl -> {
			
			chartPerformanceList.add(new ChartPerformance(pl.getPerformanceDate().toString(), pl.getPortfolioValue()));
		} );
		}else if(chartType == ChartType.PERFORMANCE_IV) {
			performanceLedgerList.forEach( pl -> {
				
				chartPerformanceList.add(new ChartPerformance(pl.getPerformanceDate().toString(), pl.getInvestmentValue()));
			} );
		}
		
		return chartPerformanceList;
		
	}
	
}
