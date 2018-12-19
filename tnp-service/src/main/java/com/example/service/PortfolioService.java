package com.example.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.repo.stocks.PortfolioRepository;
import com.example.util.Rules;

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
	private Rules rules;
	
	@Autowired
	private RuleService ruleService;
	
	@Autowired
	private TradeLedgerService tradeLedgerService;
	
	public void addStock(User user, Stock stock, double price, long quantity) {

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

		} else {

			portfolioStock = new UserPortfolio();

			portfolioStock.setQuantity(quantity);

			portfolioStock.setAveragePrice(price);

			portfolioStock.setStock(stock);

			portfolioStock.setUser(user);

		}

		user.addStockToPortfoliop(portfolioStock);

		tradeLedgerService.executeBuy(user, stock, price, quantity);
		
		userService.save(user);
	}

	public void sellStock(User user, Stock stock, double price, long quantity) {

		UserPortfolio portfolioStock = portfolioRepository.findByPortfolioIdUserAndPortfolioIdStock(user, stock);

		if (portfolioStock != null) {

			long newQuantity = portfolioStock.getQuantity() - quantity;
			
			portfolioStock.setQuantity(newQuantity);

			double existingTotal = portfolioStock.getAveragePrice() * portfolioStock.getQuantity();

			double newTotal = price * quantity;

			double newAverage = (existingTotal - newTotal) / (portfolioStock.getQuantity() - quantity);

			portfolioStock.setAveragePrice(newAverage);

			if(newQuantity > 0) {
				portfolioRepository.save(portfolioStock);
			}else {
				portfolioRepository.delete(portfolioStock);
			}
			tradeLedgerService.executeSell(user, stock, price, quantity);
		}
	}

	public List<UserPortfolio> targetAchived(User user) {

		List<UserPortfolio> targetAchivedList = new ArrayList<>();

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		for (UserPortfolio portfolioStock : portfolioList) {

			double averagePrice = portfolioStock.getAveragePrice();//100

			double per_30 = averagePrice * rules.getProfitPer(); //30

			double profitPrice = averagePrice + per_30; // 130

			double currentPrice = portfolioStock.getStock().getStockPrice().getCurrentPrice();//125

			if (currentPrice >= profitPrice) {
				targetAchivedList.add(portfolioStock);
				
			}
		}

		return targetAchivedList;
	}

	public List<UserPortfolio> userPortfolio(User user){
		
		Set<UserPortfolio> portfolioList = user.getUserPortfolio();
		
		List<UserPortfolio> portfolioListSorted = portfolioList.stream().sorted(portfolioByProfit()).collect(Collectors.toList());
		
		return portfolioListSorted;
	}
	
	public List<UserPortfolio> considerAveraging(User user) {

		List<UserPortfolio> considerAveragingList = new ArrayList<>();

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		for (UserPortfolio portfolioStock : portfolioList) {

			double currentPrice = portfolioStock.getStock().getStockPrice().getCurrentPrice(); //65
			
			double averagePrice = portfolioStock.getAveragePrice(); //100

			double per_30 = averagePrice * rules.getAveragingPer(); //30

			double considerAveragePrice = averagePrice - per_30; // 70

			if (currentPrice <= considerAveragePrice) {
				considerAveragingList.add(portfolioStock);
			}
		}

		List<UserPortfolio> filteredPortfolioList =  ruleService.applyAveragingFilterRule(considerAveragingList);
		
		List<UserPortfolio> sortedConsiderAveragingList = filteredPortfolioList.stream().sorted(byRoeComparator().thenComparing(portfolioByDebtEquityComparator())).limit(rules.getAveragingSize()).collect(Collectors.toList());
		
		return sortedConsiderAveragingList;
	}

	public void updateCylhPrice(User user) {
		
		Set<UserPortfolio> portfolioList = user.getUserPortfolio();
		
		for(UserPortfolio userPortfolioStock: portfolioList) {
			Stock stock = userPortfolioStock.getStock();
			stockService.updateCylhPrice(stock);
		}
		
	}
	
	private Comparator<UserPortfolio> byRoeComparator(){
		return Comparator.comparing(
				userPortfolio -> userPortfolio.getStock().getStockFactor().getReturnOnEquity(), Comparator.reverseOrder()
			    );
	}
	
	private Comparator<UserPortfolio> portfolioByDebtEquityComparator(){
		return Comparator.comparing(
				userPortfolio ->  userPortfolio.getStock().getStockFactor().getDebtEquity()
		);
	}
	
	private Comparator<UserPortfolio> portfolioByProfit(){
		
		return (u1, u2) -> {
			
			double currentPrice_u1 = u1.getStock().getStockPrice().getCurrentPrice();
			
			double avergePrice_u1 = u1.getAveragePrice();
			
			double profiltLoss_u1 = currentPrice_u1 - avergePrice_u1;
			
			double profiltLossPer_u1 = ( profiltLoss_u1 / currentPrice_u1 ) * 100;
			
			double currentPrice_u2 = u2.getStock().getStockPrice().getCurrentPrice();
			
			double avergePrice_u2 = u2.getAveragePrice();
			
			double profiltLoss_u2 = currentPrice_u2 - avergePrice_u2;
			
			double profiltLossPer_u2 = ( profiltLoss_u2 / currentPrice_u2 ) * 100;
			
			return (int) (profiltLossPer_u2 - profiltLossPer_u1);
		};
		
	}
	
}
