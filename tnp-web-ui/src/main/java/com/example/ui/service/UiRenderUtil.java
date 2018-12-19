package com.example.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.ui.model.UIRenderStock;
import com.example.util.MiscUtil;

@Service
public class UiRenderUtil {

	@Autowired
	private MiscUtil miscUtil;

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

}
