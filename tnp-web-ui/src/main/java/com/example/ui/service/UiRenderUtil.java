package com.example.ui.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.model.ledger.*;
import com.example.model.stocks.StockTechnicals;
import com.example.model.type.FundTransactionType;
import com.example.model.type.StockTransactionType;
import com.example.repo.ledger.FundsLedgerRepository;
import com.example.service.*;
import com.example.util.io.model.ResearchIO;
import org.decampo.xirr.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.type.IndicedAllocation;
import com.example.model.type.SectoralAllocation;
import com.example.model.um.UserProfile;
import com.example.ui.model.ChartPerformance;
import com.example.ui.model.ChartType;
import com.example.ui.model.RenderIndiceAllocation;
import com.example.ui.model.RenderSectorWiseValue;
import com.example.ui.model.UIOverallGainLoss;
import com.example.ui.model.UIRenderStock;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;

import static java.time.temporal.ChronoUnit.YEARS;

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

	@Autowired
	private TechnicalsResearchService technicalsResearchService;
	@Autowired
	private FundsLedgerRepository fundsLedgerRepository;

	@Autowired
	private ResearchLedgerTechnicalService researchLedgerTechnicalService;
	
	public List<UIRenderStock> renderPortfolio(List<UserPortfolio> userPortfolioList,UserProfile userProfile) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (UserPortfolio userPortfolioStock : userPortfolioList) {

			double currentPrice = userPortfolioStock.getStock().getStockPrice().getCurrentPrice();

			double averagePrice = userPortfolioStock.getAveragePrice();

			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(currentPrice, averagePrice)));

			//boolean overValued = ruleService.isOvervalued(userPortfolioStock.getStock());

			//boolean deathCross = technicalsResearchService.isBearishCrossover200(userPortfolioStock.getStock());

			//double rsi = userPortfolioStock.getStock().getTechnicals().getRsi();

			double invested = userPortfolioStock.getQuantity() * userPortfolioStock.getAveragePrice();

			double currentValue = userPortfolioStock.getQuantity() * currentPrice;

			UIRenderStock uiRenderStock = new UIRenderStock(userPortfolioStock, profitPer, miscUtil.formatDouble(invested,"00"), miscUtil.formatDouble(currentValue,"00"));

			uiRenderStock.setXirr(this.calculateXirr(userPortfolioStock, userProfile));

			boolean bearish = researchLedgerTechnicalService.isActive(userPortfolioStock.getStock(), ResearchIO.ResearchTrigger.SELL);

			uiRenderStock.setBearish(bearish);

			portfolioList.add(uiRenderStock);
		}

		return portfolioList;
	}

	private double calculateXirr(UserPortfolio userPortfolio, UserProfile userProfile){

		List<TradeLedger> tradeLedgers = tradeLedgerService.getCashFlows(userProfile, userPortfolio.getStock().getStockId());

		List<Transaction> transactions = new ArrayList<>();
		for (TradeLedger tradeLedger : tradeLedgers) {
			if(tradeLedger.getTransactionType() == StockTransactionType.SELL) {
				transactions.add(new Transaction( tradeLedger.getPrice() * tradeLedger.getQuantity(), tradeLedger.getTransactionDate()));
				System.out.println("DATE " + tradeLedger.getTransactionDate() + " AMOUNT " + tradeLedger.getPrice() * tradeLedger.getQuantity() + " STO " + userPortfolio.getStock().getStockId());

			}else{
				transactions.add(new Transaction( -1 * tradeLedger.getPrice() * tradeLedger.getQuantity(), tradeLedger.getTransactionDate()));
				System.out.println("DATE " + tradeLedger.getTransactionDate() + " AMOUNT " + -1 * tradeLedger.getPrice() * tradeLedger.getQuantity() + " STO " + userPortfolio.getStock().getStockId() + " " +userPortfolio.getStock().getNseSymbol());

			}
		}

		double currValue = userPortfolio.getQuantity() * userPortfolio.getStock().getStockPrice().getCurrentPrice();

		System.out.println("DATE " + LocalDate.now().plusDays(1) + " AMOUNT " + currValue);

		transactions.add(new Transaction( currValue, LocalDate.now().plusDays(1)));

		double xirr = Double.parseDouble(miscUtil.formatDouble( formulaService.calculateXirr(transactions)));
		System.out.println(xirr);
		return xirr;
	}

	public List<UIRenderStock> renderWatchList(List<Stock> userWatchList) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (Stock stock : userWatchList) {

			portfolioList.add(new UIRenderStock(stock));
		}

		return portfolioList;
	}

	public List<UIRenderStock> renderResearchList(List<ResearchLedgerFundamental> researchList) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (ResearchLedgerFundamental researchStock : researchList) {

			double currentPrice = researchStock.getStock().getStockPrice().getCurrentPrice();
			
			double bookValue = researchStock.getStock().getStockFactor().getBookValue();
			
			double eps = researchStock.getStock().getStockFactor().getEps();
			
			double pe= formulaService.calculatePe(currentPrice, eps);
			
			double pb= formulaService.calculatePb(currentPrice, bookValue);

			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(
					researchStock.getStock().getStockPrice().getCurrentPrice(), researchStock.getEntryValuation().getPrice())));

			boolean goldenCross = technicalsResearchService.isBullishCrossOver200(researchStock.getStock());

			TechnicalsResearchService.RsiTrend rsiTrend = technicalsResearchService.currentTrend(researchStock.getStock());

			portfolioList.add(new UIRenderStock(researchStock, profitPer,pe, pb, goldenCross, rsiTrend));
			
		}

		return portfolioList;
	}

	public List<UIRenderStock> renderResearchTechnicalList(List<ResearchLedgerTechnical> researchList) {

		List<UIRenderStock> portfolioList = new ArrayList<>();

		for (ResearchLedgerTechnical researchStock : researchList) {

			//double currentPrice = researchStock.getStock().getStockPrice().getCurrentPrice();
			
			//double bookValue= researchStock.getStock().getStockFactor().getBookValue();
			
			//double eps = researchStock.getStock().getStockFactor().getEps();
			
			//double pe= formulaService.calculatePe(currentPrice, eps);
			
			//double pb= formulaService.calculatePb(currentPrice, bookValue);

			
			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(
					researchStock.getStock().getStockPrice().getCurrentPrice(), researchStock.getResearchPrice())));

			UIRenderStock uiRenderStock = new UIRenderStock(researchStock.getStock());

			uiRenderStock.setResearchPrice(researchStock.getResearchPrice());

			StockTechnicals stockTechnicals = researchStock.getStock().getTechnicals();

			if(stockTechnicals!=null) {
				uiRenderStock.setEma5(stockTechnicals.getEma5());
				uiRenderStock.setEma10(stockTechnicals.getEma10());
				uiRenderStock.setEma20(stockTechnicals.getEma20());
				uiRenderStock.setEma50(stockTechnicals.getEma50());
				uiRenderStock.setEma100(stockTechnicals.getEma100());
				uiRenderStock.setEma200(stockTechnicals.getEma200());
				uiRenderStock.setRsi(stockTechnicals.getRsi());
			}

			portfolioList.add(uiRenderStock);

		}

		return portfolioList;
	}

	
	public List<UIRenderStock> renderAdvancedResearchList(List<ResearchLedgerFundamental> researchList) {

		List<UIRenderStock> resultList = new ArrayList<>();

		for (ResearchLedgerFundamental researchStock : researchList) {

			double currentPrice = researchStock.getStock().getStockPrice().getCurrentPrice();
			
			double bookValue = researchStock.getStock().getStockFactor().getBookValue();
			
			double eps = researchStock.getStock().getStockFactor().getEps();
			
			double pe = formulaService.calculatePe(currentPrice, eps);
			
			double pb = formulaService.calculatePb(currentPrice, bookValue);

			double sectorPe = researchStock.getStock().getSector().getSectorPe();

			double sectorPb = researchStock.getStock().getSector().getSectorPb();
			
			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(
					researchStock.getStock().getStockPrice().getCurrentPrice(), researchStock.getEntryValuation().getPrice())));

			boolean isBullish = researchLedgerTechnicalService.isActive(researchStock.getStock(), ResearchIO.ResearchTrigger.BUY);

			resultList.add(new UIRenderStock(researchStock, profitPer,pe, pb,sectorPe, sectorPb, isBullish));
			
		}

		resultList.stream().sorted(Comparator.comparing(UIRenderStock::getPeDifference).thenComparing(UIRenderStock::getIndice));
		
		return resultList;
	}
	
	public List<UIRenderStock> renderAdvancedResearchTechnicalList(List<ResearchLedgerTechnical> researchList) {

		List<UIRenderStock> resultList = new ArrayList<>();

		for (ResearchLedgerTechnical researchStock : researchList) {

			double currentPrice = researchStock.getStock().getStockPrice().getCurrentPrice();
			
			double bookValue= researchStock.getStock().getStockFactor().getBookValue();
			
			double eps = researchStock.getStock().getStockFactor().getEps();
			
			double pe= formulaService.calculatePe(currentPrice, eps);
			
			double pb= formulaService.calculatePb(currentPrice, bookValue);


			double sectorPe = researchStock.getStock().getSector().getSectorPe();
			
			double peDifference = sectorPe - pe;
			
			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(
					researchStock.getStock().getStockPrice().getCurrentPrice(), researchStock.getResearchPrice())));

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
		this.renderFyXirrAndCagr(user, uIOverallGainLoss);
		
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

	private void renderFyXirrAndCagr(UserProfile userProfile, UIOverallGainLoss uIOverallGainLoss){

		List<FundsLedger> fundsLedgerList =  fundsLedgerRepository.findAllTransactioninCurrentFyByUserId(1, LocalDate.of(2024, 4, 1), LocalDate.now());


		List<Transaction> transactions = new ArrayList<>();

		double fyInitialCapital = 60000.00;
		for (FundsLedger fundsLedger : fundsLedgerList) {
			if(fundsLedger.getTransactionType() == FundTransactionType.WITHDRAW) {
				transactions.add(new Transaction( fundsLedger.getAmount(), fundsLedger.getTransactionDate()));
				//System.out.println("DATE " + fundsLedger.getTransactionDate() + " AMOUNT " + fundsLedger.getAmount() + " TYPE " + fundsLedger.getTransactionType());

			}else{
				transactions.add(new Transaction( -1 * fundsLedger.getAmount(), fundsLedger.getTransactionDate()));
				//System.out.println("DATE " + fundsLedger.getTransactionDate() + " AMOUNT " + -1 * fundsLedger.getAmount() + " TYPE " + fundsLedger.getTransactionType());

				if(fundsLedger.getTransactionType() == FundTransactionType.FYRO && fundsLedger.getAmount() != 0.00){
					fyInitialCapital = fundsLedger.getAmount();
				}
			}
		}

		double currValue = portfolioService.currentValue(userProfile);

		//System.out.println("DATE " + LocalDate.now().plusDays(1) + " AMOUNT " + currValue + " TYPE " + "CURR");

		transactions.add(new Transaction( currValue, LocalDate.now().plusDays(1)));

		double xirr = formulaService.calculateXirr(transactions);

		uIOverallGainLoss.setFyXirr(xirr);

		//System.out.println("XIRR " + xirr);

		LocalDate beginningDate = miscUtil.currentFinYearFirstDay();

		long years = YEARS.between(beginningDate, LocalDate.now());

		//System.out.println("CAGR YEARS "  + years);

		double cagr = formulaService.calculateCagr(fyInitialCapital, currValue, years);

		uIOverallGainLoss.setFyCagr(cagr);

		//System.out.println("CAGR " + cagr);
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
