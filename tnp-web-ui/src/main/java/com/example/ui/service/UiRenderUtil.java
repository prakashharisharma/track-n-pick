package com.example.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.stocks.UserPortfolio;
import com.example.ui.model.PortfolioStock;
import com.example.util.MiscUtil;

@Service
public class UiRenderUtil {

	@Autowired
	private MiscUtil miscUtil;
	
	public List<PortfolioStock> renderPortfolio(List<UserPortfolio> userPortfolioList){
		
		List<PortfolioStock> portfolioList = new ArrayList<>();
		
		for(UserPortfolio userPortfolioStock : userPortfolioList) {
			
			double currentPrice =  userPortfolioStock.getStock().getStockPrice().getCurrentPrice();
			double averagePrice = userPortfolioStock.getAveragePrice();
			
			double profitPer = Double.parseDouble(miscUtil.formatDouble(calculateProfitPer(currentPrice, averagePrice)));
			
			portfolioList.add(new PortfolioStock(userPortfolioStock.getStock().getNseSymbol(), userPortfolioStock.getQuantity(), averagePrice, currentPrice, userPortfolioStock.getStock().getStockPrice().getYearLow(), userPortfolioStock.getStock().getStockPrice().getYearHigh(), profitPer));
		}
		
		return portfolioList;
	}
	
	private double calculateProfitPer(double currentPrice, double averagePrice) {
		
		double profit = currentPrice - averagePrice;
		
		double profitPer = (profit / averagePrice) * 100 ;
		
		return profitPer;
	}
	
}
