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

		if (stock.getPrimaryIndice() == IndiceType.NIFTY50) {
			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt()) {
				isPriceInRange = true;
			}
		} else if (stock.getPrimaryIndice() == IndiceType.NIFTY100) {
			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt100()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt100()) {
				isPriceInRange = true;
			}
		} else if (stock.getPrimaryIndice() == IndiceType.NIFTY250) {
			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt250()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt250()) {
				isPriceInRange = true;
			}
		} else if (stock.getPrimaryIndice() == IndiceType.NIFTY500) {
			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt500()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt500()) {
				isPriceInRange = true;
			}
		} else if (stock.getPrimaryIndice() == IndiceType.NIFTY750) {
			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt750()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt750()) {
				isPriceInRange = true;
			}
		}else if (stock.getPrimaryIndice() == IndiceType.NIFTY1000) {
			if (stock.getStockPrice().getCurrentPrice() > rules.getPricegt1000()
					&& stock.getStockPrice().getCurrentPrice() < rules.getPricelt1000()) {
				isPriceInRange = true;
			}
		}
		
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

					if (stock.getPrimaryIndice() == IndiceType.NIFTY50) {

						if (peDiff > 5.0) {
							if (pb < 2.5) {
								isUndervalued = true;
							} else if (pbDiff > 0.20) {
								isUndervalued = true;
							}

						}
					} else if (stock.getPrimaryIndice() == IndiceType.NIFTY100) {

						if (peDiff > 6.0) {
							if (pb < 2.5) {
								isUndervalued = true;
							} else if (pbDiff > 0.2) {
								isUndervalued = true;
							}
						}
					} else if (stock.getPrimaryIndice() == IndiceType.NIFTY250) {
						if (pbDiff > 0.5) {
							if (peDiff > 7.0) {
								isUndervalued = true;
							}
						}
					} else if (stock.getPrimaryIndice() == IndiceType.NIFTY500) {
						if (pbDiff > 1.0) {
							if (peDiff > 8.0) {
								isUndervalued = true;
							}
						}
					}else if (stock.getPrimaryIndice() == IndiceType.NIFTY750) {
						if (pbDiff > 1.0) {
							if (peDiff > 10.0) {
								isUndervalued = true;
							}
						}
					}else if (stock.getPrimaryIndice() == IndiceType.NIFTY1000) {
						if (pbDiff > 1.0) {
							if (peDiff > 12.0) {
								isUndervalued = true;
							}
						}
					}

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

				}/*else if(pb > sectorPb) {
					LOGGER.debug(" RULE 4 ");
					isOvervalued = true;
				}*/
			}

		}

		return isOvervalued;
	}

	private boolean isUndervaluedPre(Stock stock) {

		boolean isUndervalued = false;

		if (stock != null) {

			if (stock.getPrimaryIndice() == IndiceType.NIFTY50) {
				isUndervalued = this.isUndervaluedPre50(stock);
			} else if (stock.getPrimaryIndice() == IndiceType.NIFTY100) {
				isUndervalued = this.isUndervaluedPre100(stock);
			} else if (stock.getPrimaryIndice() == IndiceType.NIFTY250) {
				isUndervalued = this.isUndervaluedPre250(stock);
			} else if (stock.getPrimaryIndice() == IndiceType.NIFTY500) {
				isUndervalued = this.isUndervaluedPre500(stock);
			} else if (stock.getPrimaryIndice() == IndiceType.NIFTY750) {
				isUndervalued = this.isUndervaluedPre750(stock);
			}else if (stock.getPrimaryIndice() == IndiceType.NIFTY1000) {
				isUndervalued = this.isUndervaluedPre1000(stock);
			}

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

	private boolean isUndervaluedPre100(Stock stock) {

		boolean isUndervalued = false;

		Sector sector = stock.getSector();

		if (stock != null) {

			if (sector != null) {

				String sectorName = sector.getSectorName();

				if (sectorName != null && !sectorName.isEmpty()) {

					if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe100()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce100()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio100()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {
						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
								&& stock.getStockFactor().getDividend() >= rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe100()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce100()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio100()

						) {

							isUndervalued = true;
						}
					}
				}
			}
		}
		return isUndervalued;
	}

	private boolean isUndervaluedPre250(Stock stock) {

		boolean isUndervalued = false;

		Sector sector = stock.getSector();

		if (stock != null) {

			if (sector != null) {

				String sectorName = sector.getSectorName();

				if (sectorName != null && !sectorName.isEmpty()) {

					if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe250()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce250()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio250()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {
						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
								&& stock.getStockFactor().getDividend() >= rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe250()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce250()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio250()

						) {

							isUndervalued = true;
						}
					}
				}
			}
		}
		return isUndervalued;
	}

	private boolean isUndervaluedPre500(Stock stock) {

		boolean isUndervalued = false;

		Sector sector = stock.getSector();

		if (stock != null) {

			if (sector != null) {

				String sectorName = sector.getSectorName();

				if (sectorName != null && !sectorName.isEmpty()) {

					if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe500()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce500()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio500()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {
						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe500()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce500()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio500()

						) {

							isUndervalued = true;
						}
					}
				}
			}
		}
		return isUndervalued;
	}
	private boolean isUndervaluedPre750(Stock stock) {

		boolean isUndervalued = false;

		Sector sector = stock.getSector();

		if (stock != null) {

			if (sector != null) {

				String sectorName = sector.getSectorName();

				if (sectorName != null && !sectorName.isEmpty()) {

					if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe750()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce750()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio750()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {
						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe750()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce750()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio750()

						) {

							isUndervalued = true;
						}
					}
				}
			}
		}
		return isUndervalued;
	}
	private boolean isUndervaluedPre1000(Stock stock) {

		boolean isUndervalued = false;

		Sector sector = stock.getSector();

		if (stock != null) {

			if (sector != null) {

				String sectorName = sector.getSectorName();

				if (sectorName != null && !sectorName.isEmpty()) {

					if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe1000()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce1000()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio1000()
								&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()) {
							isUndervalued = true;
						}

					} else {
						if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
								&& stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
								&& stock.getStockFactor().getDividend() > rules.getDividend()
								&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe1000()
								&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce1000()
								&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio1000()

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
