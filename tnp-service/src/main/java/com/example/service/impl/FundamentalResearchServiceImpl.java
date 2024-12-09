package com.example.service.impl;

import javax.transaction.Transactional;

import com.example.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Sector;
import com.example.model.master.Stock;
import com.example.util.rules.RulesFundamental;
import com.example.util.rules.RulesResearch;

@Transactional
@Service
@Slf4j
public class FundamentalResearchServiceImpl implements FundamentalResearchService{

	@Autowired
	private RulesFundamental rules;

	@Autowired
	private RulesResearch researchRules;

	@Autowired
	private StockService stockService;

	@Override
	public boolean isPriceInRange(Stock stock) {

			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt()) {

				return Boolean.TRUE;
			}

		return Boolean.FALSE;
	}

	@Override
	public boolean isMcapInRange(Stock stock){
		if(stock.getStockFactor().getMarketCap() >= rules.getMcap()){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public double calculateValuation(Stock stock){

			double score = 0.0;

			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);

			double sectorPb = stock.getSector().getSectorPb();

			double sectorPe = stock.getSector().getSectorPe();

			if(pe <= 20 && pb <= 3){
				return 100.0;
			}

			if(pe <= 20 && pb <= sectorPb){
				return  80.0;
			}

			if (pe <= sectorPe && pb <= 3) {
				return 65.0;
			}

			if(pe <= sectorPe && pb <= sectorPb){
				return 55.0;
			}

			if(pe <= sectorPe ){
				return  50.0;
			}

		return score;
	}

	@Override
	public boolean isGoodValuation(Stock stock){
		return (this.calculateValuation(stock) >= 50.0) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public boolean isUndervalued(Stock stock) {

		if (this.isFinancialsStable(stock) && this.isGoodValuation(stock)) {
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	@Override
	public boolean isOvervalued(Stock stock) {

		if (!this.isUndervalued(stock)) {
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	private boolean isFinancialsStable(Stock stock) {

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

						if (
								this.isMcapInRange(stock)
								&& stock.getStockFactor().getDividend() >= rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {

						if (
								this.isMcapInRange(stock)
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

}
