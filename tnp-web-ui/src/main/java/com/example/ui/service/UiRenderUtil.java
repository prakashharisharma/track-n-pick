package com.example.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.model.ledger.PerformanceLedger;
import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.type.SectorWiseValue;
import com.example.model.type.SectoralAllocation;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.PerformanceLedgerRepository;
import com.example.service.DividendLedgerService;
import com.example.service.ExpenseService;
import com.example.service.FundsLedgerService;
import com.example.service.PerformanceLedgerService;
import com.example.service.PortfolioService;
import com.example.service.TradeLedgerService;
import com.example.service.TradeProfitLedgerService;
import com.example.ui.model.ChartPerformance;
import com.example.ui.model.ChartType;
import com.example.ui.model.RenderSectorWiseValue;
import com.example.ui.model.UIOverallGainLoss;
import com.example.ui.model.UIRenderStock;
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
	
	public List<UIRenderStock> renderPortfolio(List<UserPortfolio> userPortfolioList) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (UserPortfolio userPortfolioStock : userPortfolioList) {

			double currentPrice = userPortfolioStock.getStock().getStockPrice().getCurrentPrice();
			double averagePrice = userPortfolioStock.getAveragePrice();

			double profitPer = Double
					.parseDouble(miscUtil.formatDouble(calculateProfitPer(currentPrice, averagePrice)));

			portfolioList.add(new UIRenderStock(userPortfolioStock, profitPer));
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

			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(
					researchStock.getStock().getStockPrice().getCurrentPrice(), researchStock.getResearchPrice())));

			portfolioList.add(new UIRenderStock(researchStock, profitPer));
		}

		return portfolioList;
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
		
		double ytdRealizedGainPer = miscUtil.calculatePer(ytdInvestmentValue, ytdRealizedGain);
		
		double ytdUnrealizedGainPer = miscUtil.calculatePer(ytdInvestmentValue, ytdUnrealizedGain);
		
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
		
		double fyRealizedGainPer = miscUtil.calculatePer(fyInvestmentValue, fyRealizedGain);
		
		double fyUnrealizedGainPer = miscUtil.calculatePer(fyInvestmentValue, fyUnrealizedGain);
		
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
		
		//List<SectorWiseValue> sectorWiseValueList = portfolioService.sectoralAllocation(user);
		List<SectoralAllocation> sectorWiseValueList = portfolioService.sectoralAllocation(user);
		
		List<RenderSectorWiseValue> renderList = new ArrayList<>();
		
		sectorWiseValueList.forEach( sw -> {
			
			renderList.add(new RenderSectorWiseValue(sw.getSectorName(), miscUtil.calculatePer(currentInvestmentValue, sw.getAllocation())));
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
