package com.example.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.type.SectorWiseValue;
import com.example.model.type.SectoralAllocation;
import com.example.model.um.UserProfile;
import com.example.repo.stocks.PortfolioRepository;
import com.example.util.rules.RulesFundamental;
import com.example.util.rules.RulesNotification;
import com.example.util.rules.RulesResearch;

@Transactional
@Service
public class PortfolioService {

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private StockService stockService;

	@Autowired
	private RulesFundamental rules;

	@Autowired
	private RulesNotification notificationRules;
	
	@Autowired
	private RulesResearch researchRules;
	
	@Autowired
	private RuleService ruleService;

	@Autowired
	private TradeLedgerService tradeLedgerService;

	@Autowired
	private TradeProfitLedgerService tradeProfitLedgerService;

	public void addStock(UserProfile user, Stock stock, double price, long quantity) {

		Optional<UserPortfolio> portfolioStockOpt = user.getUserPortfolio().stream()
				.filter(up -> up.getStock().getNseSymbol().equalsIgnoreCase(stock.getNseSymbol())).findFirst();

		UserPortfolio portfolioStock = null;

		if (portfolioStockOpt.isPresent()) {

			portfolioStock = portfolioStockOpt.get();

			portfolioStock.setQuantity(portfolioStock.getQuantity() + quantity);

			double existingTotal = portfolioStock.getAveragePrice() * portfolioStock.getQuantity();

			double newTotal = price * quantity;

			double newAverage = (existingTotal + newTotal) / (portfolioStock.getQuantity() + quantity);

			portfolioStock.setAveragePrice(newAverage);

			portfolioStock.setLastTxnDate(LocalDate.now());
			
		} else {

			portfolioStock = new UserPortfolio();

			portfolioStock.setQuantity(quantity);

			portfolioStock.setAveragePrice(price);
			
			portfolioStock.setFirstTxnDate(LocalDate.now());

			portfolioStock.setStock(stock);

			portfolioStock.setUser(user);

		}

		user.addStockToPortfoliop(portfolioStock);

		tradeLedgerService.executeBuy(user, stock, price, quantity);

		userService.save(user);
	}

	public void sellStock(UserProfile user, Stock stock, double price, long quantity) {

		UserPortfolio portfolioStock = portfolioRepository.findByPortfolioIdUserAndPortfolioIdStock(user, stock);

		if (portfolioStock != null) {

			long newQuantity = portfolioStock.getQuantity() - quantity;

			portfolioStock.setQuantity(newQuantity);

			double existingTotal = portfolioStock.getAveragePrice() * portfolioStock.getQuantity();

			double newTotal = price * quantity; // 100 * 50; // 50*50

			double newAverage = (existingTotal - newTotal) / (portfolioStock.getQuantity() - quantity);

			double netProfit = (newTotal - (portfolioStock.getAveragePrice() * quantity));

			portfolioStock.setAveragePrice(newAverage);

			portfolioStock.setLastTxnDate(LocalDate.now());
			
			if (newQuantity > 0) {
				portfolioRepository.save(portfolioStock);
			} else {
				portfolioRepository.delete(portfolioStock);
			}
			tradeLedgerService.executeSell(user, stock, price, quantity);

			tradeProfitLedgerService.addProfitEntry(user, stock, quantity, netProfit);
		}
	}

	public List<UserPortfolio> targetAchived(UserProfile user) {

		List<UserPortfolio> targetAchivedList = new ArrayList<>();

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		for (UserPortfolio portfolioStock : portfolioList) {

			double averagePrice = portfolioStock.getAveragePrice();// 100

			double per_30 = averagePrice * researchRules.getProfitPer(); // 30

			double profitPrice = averagePrice + per_30; // 130

			double currentPrice = portfolioStock.getStock().getStockPrice().getCurrentPrice();// 125

			if (currentPrice >= profitPrice) {
				targetAchivedList.add(portfolioStock);

			}
		}

		return targetAchivedList;
	}

	@Cacheable(value="userportfolio", key = "#userProfile.userId" )
	public List<UserPortfolio> userPortfolio(UserProfile userProfile) {

		Set<UserPortfolio> portfolioList = userProfile.getUserPortfolio();

		List<UserPortfolio> portfolioListSorted = portfolioList.stream().sorted(portfolioByProfit())
				.collect(Collectors.toList());

		
		 
		//return portfolioListSorted;
		return new ArrayList<UserPortfolio>(portfolioListSorted);
	}

	public List<UserPortfolio> considerAveraging(UserProfile user) {

		List<UserPortfolio> considerAveragingList = new ArrayList<>();

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		for (UserPortfolio portfolioStock : portfolioList) {

			double currentPrice = portfolioStock.getStock().getStockPrice().getCurrentPrice(); // 65

			double averagePrice = portfolioStock.getAveragePrice(); // 100

			double per_30 = averagePrice * researchRules.getAveragingPer(); // 30

			double considerAveragePrice = averagePrice - per_30; // 70

			if (currentPrice <= considerAveragePrice) {
				considerAveragingList.add(portfolioStock);
			}
		}

		List<UserPortfolio> filteredPortfolioList = ruleService.applyAveragingFilterRule(considerAveragingList);

		List<UserPortfolio> sortedConsiderAveragingList = filteredPortfolioList.stream()
				.sorted(byRoeComparator().thenComparing(portfolioByDebtEquityComparator()))
				.limit(notificationRules.getAveragingSize()).collect(Collectors.toList());

		return sortedConsiderAveragingList;
	}

	@Cacheable(value="currentInvestmentValue", key = "#userProfile.userId" )
	public double currentInvestmentValue(UserProfile userProfile) {

		Double totalValue=portfolioRepository.getPortfolioInvestedValue(userProfile);
		
		if(totalValue == null) {
			totalValue=0.00;
		}
		
		/*Map<String, Double> stockMap = new HashMap<>();
		
		portfolioList.forEach( up -> {
			
			stockMap.put(up.getStock().getNseSymbol(), (up.getAveragePrice() * up.getQuantity()));
			
		});
		
		for (Map.Entry<String, Double> item : stockMap.entrySet()) {
		    String key = item.getKey();
		    Double value = item.getValue();
		    totalValue = totalValue + value;
		    
		}*/
		
		return totalValue;
	}
	
	@Cacheable(value="currentValue", key = "#userProfile.userId" )
	public double currentValue(UserProfile userProfile) {

		Double totalValue=portfolioRepository.getPortfolioCurrentValue(userProfile);
		
		if(totalValue == null) {
			totalValue=0.00;
		}
		
		/*Map<String, Double> stockMap = new HashMap<>();
		
		portfolioList.forEach( up -> {
			
			stockMap.put(up.getStock().getNseSymbol(), (up.getStock().getStockPrice().getCurrentPrice() * up.getQuantity()));
			
		});
		
		for (Map.Entry<String, Double> item : stockMap.entrySet()) {
		    String key = item.getKey();
		    Double value = item.getValue();
		    totalValue = totalValue + value;
		    
		}*/
		
		return totalValue;
	}

	public void updateCylhPrice(UserProfile user) {

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		for (UserPortfolio userPortfolioStock : portfolioList) {
			Stock stock = userPortfolioStock.getStock();
			stockService.updateCylhPrice(stock);
		}

	}

	private Comparator<UserPortfolio> byRoeComparator() {
		return Comparator.comparing(userPortfolio -> userPortfolio.getStock().getStockFactor().getReturnOnEquity(),
				Comparator.reverseOrder());
	}

	private Comparator<UserPortfolio> portfolioByDebtEquityComparator() {
		return Comparator.comparing(userPortfolio -> userPortfolio.getStock().getStockFactor().getDebtEquity());
	}

	private Comparator<UserPortfolio> portfolioByProfit() {

		return (u1, u2) -> {

			double currentPrice_u1 = u1.getStock().getStockPrice().getCurrentPrice();

			double avergePrice_u1 = u1.getAveragePrice();

			double profiltLoss_u1 = currentPrice_u1 - avergePrice_u1;

			double profiltLossPer_u1 = (profiltLoss_u1 / currentPrice_u1) * 100;

			double currentPrice_u2 = u2.getStock().getStockPrice().getCurrentPrice();

			double avergePrice_u2 = u2.getAveragePrice();

			double profiltLoss_u2 = currentPrice_u2 - avergePrice_u2;

			double profiltLossPer_u2 = (profiltLoss_u2 / currentPrice_u2) * 100;

			return (int) (profiltLossPer_u2 - profiltLossPer_u1);
		};

	}

	@Cacheable(value="SectorWiseValue", key = "#userProfile.userId" )
	public List<SectorWiseValue> SectorWiseValue(UserProfile userProfile){
		
		return portfolioRepository.findSectorWiseValue(userProfile);
	}
	

	@Cacheable(value="sectoralAllocation", key = "#userProfile.userId" )
	public List<SectoralAllocation> sectoralAllocation(UserProfile userProfile){
		
		return portfolioRepository.findSectoralAllocation(userProfile);
	}
	
	
}
