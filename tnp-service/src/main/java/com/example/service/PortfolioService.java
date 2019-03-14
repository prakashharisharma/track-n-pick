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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.type.SectorWiseValue;
import com.example.model.type.SectoralAllocation;
import com.example.model.um.UserProfile;
import com.example.repo.stocks.PortfolioRepository;

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
	private RuleService ruleService;

	@Autowired
	private TradeLedgerService tradeLedgerService;

	@Autowired
	private TradeProfitLedgerService tradeProfitLedgerService;
	
	@CacheEvict(value = "userportfolio", key = "#userProfile.userId",allEntries = true)
	public void addStock(UserProfile userProfile, Stock stock, double price, long quantity) {

		Optional<UserPortfolio> portfolioStockOpt = userProfile.getUserPortfolio().stream()
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

			portfolioStock.setUser(userProfile);

		}

		userProfile.addStockToPortfoliop(portfolioStock);

		tradeLedgerService.executeBuy(userProfile, stock, price, quantity);

		userService.save(userProfile);
	}
	
	@CacheEvict(value = "userportfolio", key = "#userProfile.userId",allEntries = true)
	public void addBonus(UserProfile userProfile, Stock stock, long ratio1, long ratio2) {
		Optional<UserPortfolio> portfolioStockOpt = userProfile.getUserPortfolio().stream()
				.filter(up -> up.getStock().getNseSymbol().equalsIgnoreCase(stock.getNseSymbol())).findFirst();

		UserPortfolio portfolioStock = null;

		if (portfolioStockOpt.isPresent()) {

			portfolioStock = portfolioStockOpt.get();
			
			long portfolioQuantity = portfolioStock.getQuantity();
			
			long bonusQuantity = (portfolioQuantity * ratio2) / ratio1;
			
			portfolioStock.setQuantity(portfolioQuantity + bonusQuantity);

			double existingTotal = portfolioStock.getAveragePrice() * portfolioQuantity;

			double newAverage = (existingTotal) / (portfolioQuantity + bonusQuantity);

			portfolioStock.setAveragePrice(newAverage);

			portfolioStock.setLastTxnDate(LocalDate.now());
			
			tradeLedgerService.executeBonus(userProfile, stock, bonusQuantity);

			userService.save(userProfile);

		}
		
		
	}
	@CacheEvict(value = "userportfolio", key = "#userProfile.userId",allEntries = true)
	public void addSplit(UserProfile userProfile, Stock stock, double existingFaceValue, double newFaceValue) {
		Optional<UserPortfolio> portfolioStockOpt = userProfile.getUserPortfolio().stream()
				.filter(up -> up.getStock().getNseSymbol().equalsIgnoreCase(stock.getNseSymbol())).findFirst();

		UserPortfolio portfolioStock = null;

		if (portfolioStockOpt.isPresent()) {
			portfolioStock = portfolioStockOpt.get();
			
			long portfolioQuantity = portfolioStock.getQuantity();
			
			long bonusQuantity = (long)(existingFaceValue / newFaceValue) * portfolioQuantity;
			
			portfolioStock.setQuantity(portfolioQuantity + bonusQuantity);

			double existingTotal = portfolioStock.getAveragePrice() * portfolioQuantity;

			double newAverage = (existingTotal) / (portfolioQuantity + bonusQuantity);

			portfolioStock.setAveragePrice(newAverage);

			portfolioStock.setLastTxnDate(LocalDate.now());
			
			tradeLedgerService.executeSplit(userProfile, stock, bonusQuantity);

			userService.save(userProfile);
		}
	}
	
	@CacheEvict(value = "userportfolio", key = "#userProfile.userId",allEntries = true)
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

	public List<UserPortfolio> underValuedStocks(UserProfile user) {

		List<UserPortfolio> underValuedStocksList = new ArrayList<>();

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		portfolioList.forEach(up -> {
			if (ruleService.isUndervalued(up.getStock())) {
				underValuedStocksList.add(up);
			}

		});
		
		List<UserPortfolio> portfolioListSorted = underValuedStocksList.stream().sorted(portfolioByProfit())
				.collect(Collectors.toList());

		return new ArrayList<UserPortfolio>(portfolioListSorted);
		
	}

	public List<UserPortfolio> overValuedStocks(UserProfile user) {

		List<UserPortfolio> overValuedStocksList = new ArrayList<>();

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		portfolioList.forEach(stock -> {
			if (ruleService.isOvervalued(stock.getStock())) {
				overValuedStocksList.add(stock);
			}

		});

		List<UserPortfolio> portfolioListSorted = overValuedStocksList.stream().sorted(portfolioByProfit())
				.collect(Collectors.toList());

		return new ArrayList<UserPortfolio>(portfolioListSorted);
		
	}

	@Cacheable(value = "userportfolio", key = "#userProfile.userId")
	public List<UserPortfolio> userPortfolio(UserProfile userProfile) {

		Set<UserPortfolio> portfolioList = userProfile.getUserPortfolio();

		List<UserPortfolio> portfolioListSorted = portfolioList.stream().sorted(portfolioByProfit())
				.collect(Collectors.toList());

		return new ArrayList<UserPortfolio>(portfolioListSorted);
	}

	@Cacheable(value = "currentInvestmentValue", key = "#userProfile.userId")
	public double currentInvestmentValue(UserProfile userProfile) {

		Double totalValue = portfolioRepository.getPortfolioInvestedValue(userProfile);

		if (totalValue == null) {
			totalValue = 0.00;
		}

		return totalValue;
	}

	@Cacheable(value = "currentValue", key = "#userProfile.userId")
	public double currentValue(UserProfile userProfile) {

		Double totalValue = portfolioRepository.getPortfolioCurrentValue(userProfile);

		if (totalValue == null) {
			totalValue = 0.00;
		}
		return totalValue;
	}

	public void updateCylhPrice(UserProfile user) {

		Set<UserPortfolio> portfolioList = user.getUserPortfolio();

		for (UserPortfolio userPortfolioStock : portfolioList) {
			Stock stock = userPortfolioStock.getStock();
			stockService.updateCylhPrice(stock);
		}

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

	@Cacheable(value = "SectorWiseValue", key = "#userProfile.userId")
	public List<SectorWiseValue> SectorWiseValue(UserProfile userProfile) {

		return portfolioRepository.findSectorWiseValue(userProfile);
	}

	@Cacheable(value = "sectoralAllocation", key = "#userProfile.userId")
	public List<SectoralAllocation> sectoralAllocation(UserProfile userProfile) {

		return portfolioRepository.findSectoralAllocation(userProfile);
	}

}
