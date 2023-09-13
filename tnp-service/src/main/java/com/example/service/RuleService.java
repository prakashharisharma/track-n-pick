package com.example.service;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Sector;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.util.io.model.StockIO.IndiceType;
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

		//if (stock.getPrimaryIndice() == IndiceType.NIFTY50) {
			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt()) {
				isPriceInRange = true;
			}

		//}
		
		return isPriceInRange;
	}

	public boolean isUndervalued(Stock stock) {
		boolean isUndervalued = false;

		if (this.isUndervaluedPre(stock)) {

			StockPrice stockPrice = stock.getStockPrice();

			if (isPriceInRange(stock)) {

				double pe = stockService.getPe(stock);

				double pb = stockService.getPb(stock);

				double sectorPb = stock.getSector().getSectorPb();

				double sectorPe = stock.getSector().getSectorPe();

				double pbDiff = sectorPb - pb;
				
				double peDiff = sectorPe - pe;

				if (pe > 0.0) {

					LOGGER.debug(stock.getNseSymbol() + " : " + stockPrice + " PE : " + pe + " PB : " + pb);

					//if (stock.getPrimaryIndice() == IndiceType.NIFTY50) {

						if (peDiff > 5.0) {
							if (pb < 2.5) {
								isUndervalued = true;
							}

						}
					//}

					// }
				}
			}

		}

		return isUndervalued;

	}

	public boolean isOvervalued(Stock stock) {
		boolean isOvervalued = false;

		if (!this.isUndervaluedPre(stock)) {
			isOvervalued = true;
		} else {
			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);

			double sectorPb = stock.getSector().getSectorPb();

			double sectorPe = stock.getSector().getSectorPe();

			if (pe <= 0.0) {
				isOvervalued = true;
			} else {
				if (pe > sectorPe) {

					LOGGER.debug(" RULE 4 ");
					isOvervalued = true;

				}else if(pb > 2.5) {
					LOGGER.debug(" RULE 4 ");
					isOvervalued = true;
				}
			}

		}

		return isOvervalued;
	}

	private boolean isUndervaluedPre(Stock stock) {

		boolean isUndervalued = false;

		if (stock != null) {

			//if (stock.getPrimaryIndice() == IndiceType.NIFTY50) {
				isUndervalued = this.isUndervaluedPre50(stock);
			//}

		}
		return isUndervalued;
	}

	private boolean isUndervaluedPre50(Stock stock) {

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

}
