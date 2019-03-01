package com.example.service;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Sector;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.util.rules.RulesFundamental;
import com.example.util.rules.RulesResearch;

@Transactional
@Service
public class RuleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleService.class);

	@Autowired
	private RulesFundamental rules;

	@Autowired
	private RulesResearch researchRules;

	@Autowired
	private StockService stockService;

	public boolean isPriceInRange(Stock stock) {
		boolean isPriceInRange = false;

		if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt()
				&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt()) {
			isPriceInRange = true;
		}

		return isPriceInRange;
	}

	public boolean isPriceOutRange(Stock stock) {
		boolean isPriceOutRange = false;

		if (stock.getStockPrice().getCurrentPrice() <= rules.getPricegt()
				&& stock.getStockPrice().getCurrentPrice() >= rules.getPricelt()) {
			isPriceOutRange = true;
		}

		return isPriceOutRange;
	}

	public boolean isUndervalued(Stock stock) {
		boolean isUndervalued = false;

		if (this.isUndervaluedPre(stock)) {

			StockPrice stockPrice = stock.getStockPrice();

			if (isPriceInRange(stock)) {

				double pe = stockService.getPe(stock);

				double pb = stockService.getPb(stock);

				LOGGER.debug(stock.getNseSymbol() + " : " + stockPrice + " PE : " + pe + " PB : " + pb);

				if (pe <= researchRules.getPe() && pb <= researchRules.getPb()) {
					LOGGER.debug(" RULE 1 ");

					isUndervalued = true;
				} else if (pe <= stock.getSector().getSectorPe() && pb <= researchRules.getPb()) {

					LOGGER.debug(" RULE 2");

					isUndervalued = true;

				} else if (pb <= stock.getSector().getSectorPb() && pe <= researchRules.getPe()) {
					LOGGER.debug(" RULE 3");
					isUndervalued = true;

				}

				else if (pb <= stock.getSector().getSectorPb() && pe <= stock.getSector().getSectorPe()) {

					LOGGER.debug(" RULE 4 ");

					isUndervalued = true;

				}

			}

		}

		return isUndervalued;
	}

	private boolean isUndervaluedPre(Stock stock) {

		boolean isUndervalued = false;

		Sector sector = stock.getSector();
		
		if (stock != null) {
			if (sector != null) {

				String sectorName = sector.getSectorName();

				if (sectorName != null && !sectorName.isEmpty()) {

					if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {
						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
								&& stock.getStockFactor().getDividend() >= rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio()

						) {

							isUndervalued = true;
						}
					}
				}
			}
		}
		return isUndervalued;
	}

	private boolean isOvervaluedPre(Stock stock) {
		boolean isOvervalued = false;

		if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

			if (stock.getStockFactor().getDividend() < rules.getDividend()

					&& stock.getStockFactor().getReturnOnEquity() < rules.getRoe()
					&& stock.getStockFactor().getReturnOnCapital() < rules.getRoce()
					&& stock.getStockFactor().getCurrentRatio() < rules.getCurrentRatio()
					&& stock.getStockFactor().getQuickRatio() < rules.getQuickRatioBanks()) {
				isOvervalued = true;
			}

		} else {
			if (stock.getStockFactor().getDebtEquity() > rules.getDebtEquity()
					&& stock.getStockFactor().getDividend() < rules.getDividend()
					&& stock.getStockFactor().getReturnOnEquity() < rules.getRoe()
					&& stock.getStockFactor().getReturnOnCapital() < rules.getRoce()
					&& stock.getStockFactor().getCurrentRatio() < rules.getCurrentRatio()

			) {

				isOvervalued = true;
			}
		}

		return isOvervalued;
	}

	public boolean isOvervalued(Stock stock) {
		boolean isOvervalued = false;

		double pe = stockService.getPe(stock);

		double pb = stockService.getPb(stock);

		if (this.isOvervaluedPre(stock)) {
			isOvervalued = true;
		} else if (isPriceOutRange(stock)) {
			isOvervalued = true;
		} else if (pe > stock.getSector().getSectorPe() + 5) {
			isOvervalued = true;
		} else if (pb > stock.getSector().getSectorPb() + 3) {
			isOvervalued = true;
		}

		return isOvervalued;
	}

}
