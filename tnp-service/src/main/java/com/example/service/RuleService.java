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

			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt()) {

				return Boolean.TRUE;
			}

		return Boolean.FALSE;
	}

	public boolean isUndervalued(Stock stock) {

		if (this.isUndervaluedPre(stock)) {

			StockPrice stockPrice = stock.getStockPrice();

			if (isPriceInRange(stock)) {

				double pe = stockService.getPe(stock);

				double pb = stockService.getPb(stock);

				double sectorPb = stock.getSector().getSectorPb();

				double sectorPe = stock.getSector().getSectorPe();

				double pbDiff = sectorPb - pb;
				
				double peDiff = sectorPe - pe;

				if(pe < sectorPe && pb < sectorPb){
					return Boolean.TRUE;
				}

				if(pe <= 20 && pb < sectorPb){
					return  Boolean.TRUE;
				}

				if (pe < sectorPe && pb <= 3) {
					return Boolean.TRUE;
				}
			}

		}

		return Boolean.FALSE;

	}

	public boolean isOvervalued(Stock stock) {
		boolean isOvervalued = false;

		if (!this.isUndervaluedPre(stock)) {
			isOvervalued = true;
		}/*
		else {
			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);

			double sectorPb = stock.getSector().getSectorPb();

			double sectorPe = stock.getSector().getSectorPe();

			if (pe <= 0.0 || pe < sectorPe) {
				isOvervalued = true;
			}
		}
		*/
		return isOvervalued;
	}

	private boolean isUndervaluedPre(Stock stock) {

		if (stock != null) {
				return this.isUndervaluedPre50(stock);
		}

		return Boolean.FALSE;
	}
	private boolean isUndervaluedPre50(Stock stock) {

		boolean isUndervalued = false;

		Sector sector = stock.getSector();

		if (stock != null) {

			if (sector != null) {

				String sectorName = stock.getSector().getSectorName();

				if (sectorName != null && !sectorName.isEmpty()) {


					if (sectorName.equalsIgnoreCase("Financial Institution")
					|| sectorName.equalsIgnoreCase("Non Banking Financial Company NBFC")
							|| sectorName.equalsIgnoreCase("Other Bank")
							|| sectorName.equalsIgnoreCase("Private Sector Bank")
							|| sectorName.equalsIgnoreCase("Public Sector Bank")) {

						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDividend() >= rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {

						/*
						if(stock.getStockFactor().getReturnOnEquity() >= 50.0
								&& stock.getStockFactor().getReturnOnCapital() >= 50.0){
							isUndervalued = true;
						}
						*/

						if (
								stock.getStockFactor().getMarketCap() >= rules.getMcap() &&
								 stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
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

}
